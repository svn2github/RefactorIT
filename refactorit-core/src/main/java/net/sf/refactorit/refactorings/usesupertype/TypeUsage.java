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
import net.sf.refactorit.classmodel.BinExpressionList;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.expressions.BinMemberInvocationExpression;
import net.sf.refactorit.classmodel.statements.BinReturnStatement;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.refactorings.ImportManager;
import net.sf.refactorit.transformations.TransformationList;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * Class for collecting specific  member usages.
 * <pre>
 * what = getWhat();
 * usg = getUsages().get(i);
 * </pre>
 * 1) If usg instanceof BinMethod it means what must be assignable form
 *  method return value.
 *
 * 2) If usg <code>instanceof</code> BinMemberInvocation => what must have
 *  this member(method or field).
 *
 * 3) If usg instanceof BinVariable => what must be assignable from usg type
 *
 * @author Tonis Vaga
 */

abstract class TypeUsage {
  private ArrayList usages;
  private Set dependsFrom;

  private BinItem what;
  private static final Logger log = AppRegistry.getLogger(TypeUsage.class);

  public TypeUsage(BinItem what) {
    usages = new ArrayList();
    this.what = what;
  }
  /**
   * @param superInf
   * @param resolvedMembers
   * @param failureReasons list of reasons
   * @return true if can change this usage to supertype
   */
  public boolean checkCanUseSuper(SuperClassInfo superInf,
      List resolvedMembers, List failureReasons) {

    if (UseSuperTypeRefactoring.debug) {
      log.debug("usages for " + getWhat() + " are :" + getUsages());
    }
    if ( isFromClasspathOrJsp(getWhat()) ) {
      addFailureReason(failureReasons,"Can't modify declaration");
      return false;
    }
    
    boolean bResult = true;

    for (Iterator iter = getUsages().iterator(); iter.hasNext(); ) {
      BinItem item = (BinItem) iter.next();
      if (!checkBinItem(item, superInf, resolvedMembers)) {
        bResult=false;
        
        if ( failureReasons == null ) {
          break;
        } else {
          // continue to collect all failure reasons
          addFailureReason(failureReasons,item);
        }
      }
    }

    return bResult;
  }

  public BinItem getWhat() {
    return what;
  }

  public abstract void addTypeEditors(
      BinCIType type, final TransformationList transList,
      ImportManager importManager);

  /**
   * Adds usage
   *  Not that it can already exist because we use later set to remove duplicated elements 
   * @param dependent
   */
  void addUsage(BinItem dependent) {
    if (dependent != null) {
      usages.add(dependent);
    }
  }

  Collection getUsages() {
    return usages;
  }

  /**
   * Returns return value dependencies
   */
  public Set getDependsFrom() {
    if (dependsFrom != null) {
      return dependsFrom;
    }

    dependsFrom = new HashSet(getUsages());

    ArrayList toAdd = null;
    Iterator iter = dependsFrom.iterator();

    while (iter.hasNext()) {
      BinItem item = (BinItem) iter.next();

      if (!(item instanceof BinVariable) && !(item instanceof BinMethod)) {
        iter.remove();
      }

      if (item instanceof BinReturnStatement) {
        if (toAdd == null) {
          toAdd = new ArrayList();
        }

        toAdd.add(((BinReturnStatement) item).getMethod());
      }
    }

    if (toAdd != null) {
      dependsFrom.addAll(toAdd);
    }

    return dependsFrom;
  }

  /**
   * 
   * @param binItem
   * @return true if can change usage which depends from binItem to superclass
   */
  boolean checkBinItem(BinItem binItem, SuperClassInfo superInf,
      List resolvedMembers) {

    BinTypeRef superRef = superInf.getSupertypeRef();
  
    boolean bResult = true;

    if (binItem == null) {
      return true;
    }

    if (binItem instanceof BinReturnStatement) {
      BinReturnStatement rStatement = (BinReturnStatement) binItem;
      BinMethod method = (BinMethod) rStatement.getParentMember();
      BinTypeRef returnType = rStatement.getReturnExpression().getReturnType();

      return resolvedMembers.contains(method)
          || isDerivedFrom(superRef, returnType);
    }

    if (binItem instanceof BinExpressionList) {
      Assert.must(false, "BinExpression not supp");
      return true;
    }

    if (binItem instanceof BinVariable) {
      BinTypeRef typeR = ((BinVariable) binItem).getTypeRef();
      if(typeR.isArray()){
        typeR=typeR.getNonArrayType();
      }
      bResult = resolvedMembers.contains(binItem)
          || isDerivedFrom(superRef,typeR);
      return bResult;
    } else if (binItem instanceof BinMethod) {
      if (!resolvedMembers.contains(binItem)
          && !isDerivedFrom(superRef,((BinMethod) binItem).getReturnType()) ) {
        return false;
      }
    } else if (binItem instanceof BinMemberInvocationExpression) {
      BinMemberInvocationExpression invExpr = (BinMemberInvocationExpression)
          binItem;
      if (invExpr.getMember().getOwner().isArray()) {
        return true;
      }
      BinMember parentMember = invExpr.getParentMember();
      BinCIType typeContext = parentMember.getOwner().getBinCIType();
      BinMember superMember = superInf.hasAccessibleMemberWithSignature(invExpr.
          getMember(), typeContext);
      if (superMember == null) {
        bResult = false;
      }
    } else {
      log.debug("case not supported " + binItem);
    }

    return bResult;
  }
  
  private static boolean isDerivedFrom(BinTypeRef subtype, BinTypeRef supertype) {
    
    BinTypeRef nonArrayType = supertype.getNonArrayType();
    
    // TODO: check this type parameter and generics thing
    return supertype.getBinCIType().isTypeParameter()
        || supertype.getProject().getObjectRef() == nonArrayType
        || subtype.isDerivedFrom(nonArrayType);
  }
  
  boolean isFromClasspathOrJsp(BinItem binItem) {
    BinMember itemMember;
    
    if ( binItem instanceof BinMember ) {
      itemMember=(BinMember) binItem;
    } else if (binItem.getParentMember() != null ) {
        itemMember=binItem.getParentMember();
    } else {
      log.warn("can't determine usage source for "+binItem+", ignoring");
      return true;
    }
    return itemMember.isPreprocessedSource() || itemMember.getCompilationUnit() == null;
  }

  public String toString() {
    return this.getClass().getName() + ":" + getWhat();
  }
  public int hashCode() {
    return what.hashCode();
  }
  public boolean equals(Object obj) {
    if ( !(obj instanceof TypeUsage) ) {
      return false;
    }
    return what == ((TypeUsage)obj).what;
  }

  /**
   * @param failureReasons
   * @param reason
   */
  public static void addFailureReason(List failureReasons, Object reason) {
    if ( failureReasons != null ) {
      failureReasons.add(reason);
    }
  
  }
}
