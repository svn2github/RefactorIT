/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.usesupertype;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.refactorings.AbstractRefactoring;
import net.sf.refactorit.refactorings.ImportManager;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.usesupertype.UsageInfoCollector.ConvertResult;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.utils.ProgressShower;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * @author Tonis Vaga
 */
public class UseSuperTypeRefactoring extends AbstractRefactoring {
  public static String key = "refactoring.usesupertype";

  private BinTypeRef supertype = null;
  private List subtypes = new ArrayList();

//  private BinTypeRef ObjectRef = null;
  private Set usagesToChange = null;
  private boolean updateInstanceof = false;

  /**
   * Target on which this action was runned
   */
  private BinMember targetItem = null;
  private UsageInfoCollector usgCollector = null;

  private ProgressShower progress = new ProgressShower(0, 100);

  public static final boolean debug = false;
  private static final Logger log = AppRegistry.getLogger(UseSuperTypeRefactoring.class);

  public TransformationList performChange() {
    TransformationList transList = new TransformationList();
    ImportManager importManager = new ImportManager();
    Iterator iter = usagesToChange.iterator();
    while (iter.hasNext()) {
      TypeUsage usage = (TypeUsage) iter.next();

      if (debug) {
        log.debug("changing " + usage + " to type " + supertype);
      }
      usage.addTypeEditors(supertype.getBinCIType(), transList, importManager);
    }

    if (UseSuperTypeRefactoring.debug) {
      System.out.println("total " +
          usagesToChange.size() + " expression type changes");
    }
    importManager.createEditors(transList);

    return transList;
  }

  public RefactoringStatus checkPreconditions() {
    RefactoringStatus okStatus = new RefactoringStatus();

    if ( targetItem == null ) {
      // we let user specify supertype later
      return okStatus;
    }

    // must be checked already in action
    Assert.must(isTargetItemClassSupported(targetItem.getClass()));

    if (supertype.isPrimitiveType()) {
      return new RefactoringStatus(
          "Cannot apply refactoring to primitive types",
          RefactoringStatus.ERROR);
    } else {
      if (targetItem instanceof BinCIType && supertype.isString()) {

        // don't support String, not important and do much special cases
        return new RefactoringStatus(
            "Target not supported: java.lang.String", RefactoringStatus.CANCEL);
      }
      if ( isTargetSingleUsage() ) {
        if ( getPossibleSupertypes().length == 0 ) {
          return new RefactoringStatus("Type of "+getTargetItemDisplayName()+" can't be changed",RefactoringStatus.CANCEL);
        }
      }

    }
    return okStatus;

  }

  private BinTypeRef getTargetType() {
    return UseSuperTypeUtil.getTargetType(targetItem);
  }

  /**
   *
   * @return false if check failed
   */
  private boolean assertSuperTypeIsValid() {
    if ( supertype == null ) {
      return false;
    }
    final BinTypeRef objectRef = supertype.getProject().getObjectRef();

    if ( supertype.equals(objectRef)) {
      return true;
    }
    List supertypeSubclasses = new ArrayList(UseSuperTypeUtil.getAllSubtypes(supertype));
    for (int i = 0; i < subtypes.size(); ++i) {
      BinTypeRef element = (BinTypeRef) subtypes.get(i);

      if ( !supertypeSubclasses.contains(element)) {
        Assert.must(false, supertype.getQualifiedName() + " subtypes "
            + supertypeSubclasses + " doesn't contain "
            + element.getQualifiedName());
        return false;
      }

    }
    return true;
//    return /*!subtype.equals(ObjectRef) && */ subtype == null ? true : !subtype
//        .getAllSupertypes().contains(supertype);
  }

  public RefactoringStatus checkUserInput() {
//    ObjectRef = supertype.getProject().getObjectRef();

    if (!subtypes.isEmpty() ) {
      assertSuperTypeIsValid();
    }

    BinMember target = getTarget();

    List searchTarget=new ArrayList();

    // variable
    if ( !(target instanceof BinCIType) ) {
      searchTarget.add(target);
    } else {
      List subclasses = subtypes;

      if ( subtypes.isEmpty() ) {
        subclasses=new ArrayList(UseSuperTypeUtil.getAllSubtypes(getSupertype()));
      }
      for (int i = 0; i < subclasses.size(); ++i) {
        BinTypeRef type = (BinTypeRef) subclasses.get(i);
        searchTarget.add(type.getBinCIType());
      }
    }
    usgCollector = new UsageInfoCollector(searchTarget);
    usgCollector.collectUsages(progress);

    ConvertResult convertResult = usgCollector.computeConvertResult(supertype.getBinCIType());
    usagesToChange = convertResult.getUsagesToChange();

    if (usagesToChange.size() == 0) {
      final String msg;

      if ( target instanceof BinCIType) {
        msg = "No usages to change found";
      } else {
        // should fail in checkCreconditions
        Assert.must(false);
        msg = "Cannot change "+getTargetItemDisplayName();
      }

      return new RefactoringStatus(msg, RefactoringStatus.CANCEL);
    } else {
      return new RefactoringStatus();
    }
  }

  private BinMember getTarget() {
    return targetItem;
  }

  public UseSuperTypeRefactoring(BinMember targetItem,
      RefactorItContext context) {
    super("Use Supertype Where Possible", context);
    usagesToChange = new HashSet();
    this.targetItem = targetItem;
    supertype=getTargetType();

  }

  public void setUseInstanceOf(boolean updateInstanceOf) {
    updateInstanceof = updateInstanceOf;
  }


  public BinTypeRef getSupertype() {
    return supertype;
  }

  public void setSupertype(BinTypeRef superType) {
    if ( !isTargetSingleUsage() ) {
      targetItem=superType.getBinCIType();
    }
    this.supertype = superType;
  }

  public static boolean isTargetItemClassSupported(Class clazz) {
    if (BinCIType.class.isAssignableFrom(clazz)
        || BinVariable.class.isAssignableFrom(clazz)
        || BinFieldInvocationExpression.class.isAssignableFrom(clazz)
        || BinVariableUseExpression.class.isAssignableFrom(clazz)
        || BinMethod.class.equals(clazz)
        || BinMethodInvocationExpression.class.isAssignableFrom(clazz)) {
      return true;
    }
    return false;
  }

  public String getTargetItemDisplayName() {
    String prefix="";

    if (targetItem instanceof BinMethod) {
      prefix = "return value of ";
    } else if ( targetItem instanceof BinParameter ) {
      prefix="parameter ";
    } else if ( targetItem instanceof BinVariable ) {
      prefix="variable ";
    }
    return prefix+targetItem.getName();
  }


  public String getDescription() {
    return super.getDescription();
  }
  /**
   * sets subtypes to use for seaching, if empty list then search all subtypes
   * @param types
   */
  public void setSubtypes(List types) {
    this.subtypes.clear();

    subtypes.addAll(types);
  }



  public boolean isUpdateInstanceof() {
    return this.updateInstanceof;
  }

  /**
   * @return true if target is single usage(method, variable),
   * if it is class(es) returns false
   */
  public boolean isTargetSingleUsage() {
    return targetItem != null && !(targetItem instanceof BinCIType);
  }

  /**
   * call it only if {@link #isTargetSingleUsage()} == true
   */
  public BinTypeRef[] getPossibleSupertypes() {

    assert isTargetSingleUsage();



    usgCollector = new UsageInfoCollector(Collections.singletonList(getTarget()));
    usgCollector.collectUsages(progress);

    final BinTypeRef targetType = getTargetType();


    Set allSupertypes=new HashSet(targetType.getAllSupertypes());

    List sortedTypes = new ArrayList(allSupertypes);

    List result=new ArrayList();

    List failures=new ArrayList();

    usgCollector.setCollectReport(true);

    for (int i = 0; i < sortedTypes.size(); ++i) {
      BinTypeRef element = (BinTypeRef) sortedTypes.get(i);

      ConvertResult convertResult = usgCollector.computeConvertResult(element.getBinCIType());
      if ( !convertResult.getUsagesToChange().isEmpty() ) {
        result.add(element);
      } else {
        failures.add(convertResult);
      }
    }
    if ( result.isEmpty() )  {
      for (int i = 0; i < failures.size(); ++i) {
        ConvertResult failure = (ConvertResult) failures.get(i);
        log.debug("Converting to "+failure.supertype.getQualifiedName()+" failed because "+failure.getAllFailures());
      }
    }

    return (BinTypeRef[]) result.toArray(new BinTypeRef[result.size()]);
  }

  public String getKey() {
    return key;
  }
}
