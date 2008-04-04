/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.delegate;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.ui.module.RefactorItContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 *
 *
 * @author Tonis Vaga
 */
public class AddDelegatesRefactoring extends CreateClassMethodsRefactoring {
  public static String key = "refactoring.adddelegates";

  Object target;

  private Collection delegatesCollect;

  private AddDelegatesModel model;

  private List selectedFields = new ArrayList();

  private BinClass owner;

  public BinCIType getTarget() {
    return owner;
  }

  public Collection getMethodsToCreate() {
    return this.delegatesCollect;
  }

  static class CanDelegateChecker {
    BinCIType fieldOwner;

    /**
     * @param owner
     */
    public CanDelegateChecker(BinField field) {
      this.fieldOwner = field.getOwner().getBinCIType();
      this.field = field;

      fieldType = field.getTypeRef().getBinCIType();
      ownerMethods = fieldOwner.getAccessibleMethods(fieldOwner);
    }

    private boolean check(BinMethod method) {
      if (fieldOwner.hasMemberWithSignature(method) != null) {
//        DebugInfo.trace("member " + methods[index] + " already exists in " +
//                        owner.getName());
        return false;
      }

//      if ( !method.isAccessible(fieldType,fieldOwner) ) {
//        return false;
//      } else {
//        if ( method.getName().indexOf("clone") > -1 ) {
//          System.out.println("method "+method+" is accessible, invokedOn: "+
//                             fieldType+", "+fieldOwner);
//        }
//      }

      BinCIType invokedOn = fieldType;
      BinCIType context = fieldOwner;

      boolean superCall = false;

      if (!method.isProtectedAccessible(invokedOn, context, superCall)) {
        return false;
      }

      for (int j = 0; j < ownerMethods.length; j++) {
        if (ownerMethods[j].sameSignature(method)
            && (ownerMethods[j].isFinal()
            || !ownerMethods[j].getReturnType().equals(method.getReturnType())
            || ownerMethods[j].isStatic())) {
          return false;
        }
      }

      return true;
    }

    private BinMethod ownerMethods[];
    private BinField field;
    private BinCIType fieldType;

    List getCanDelegateList() {
      List result = new ArrayList();

      if (!(field.getTypeRef().getBinType() instanceof BinCIType)) {
        return result;
      }

      BinCIType fieldType = (BinCIType) field.getTypeRef().getBinType();
      BinMethod methods[] = fieldType.getAccessibleMethods(fieldOwner);

      for (int index = 0; index < methods.length; index++) {
        if (check(methods[index])) {
          result.add(methods[index]);
        }
      }

      return result;
    }
  }


  public static List createDelegateMethodsList(BinField field) {
    CanDelegateChecker checker = new CanDelegateChecker(field);

    return checker.getCanDelegateList();
  }

  public RefactoringStatus checkPreconditions() {
    Object targets[] = null;

    if (target instanceof Object[]) {
      targets = (Object[]) target;
    } else {
      targets = new Object[] {target};
    }

    RefactoringStatus result = new RefactoringStatus();

    for (int i = 0; i < targets.length; i++) {
      result.merge(checkBinItem((BinMember) targets[i]));
    }

    return result;
  }

  private RefactoringStatus checkBinItem(BinMember member) {
    String msg = "";

    int status = RefactoringStatus.OK;

    if (member instanceof BinClass) {
      if (!((BinClass) member).isFromCompilationUnit()) {
        msg = member.getName() + " is not in source path";
        status = RefactoringStatus.ERROR;
      } else {
        owner = (BinClass) member;
        if (getDelegateFields(owner).length == 0) {
          msg = "There are no fields in " + member.getName() + " to delegate";
          status = RefactoringStatus.ERROR;
        }
      }
    } else if (member instanceof BinField) {
      BinCIType type = member.getOwner().getBinCIType();

      if (type == null || !type.isFromCompilationUnit()) {
        msg = "Type " + type.getName() + " is not from sourcefile";
        status = RefactoringStatus.ERROR;
      } else if (type.isInterface()) {
        msg = "Can't create delegates for interface";
        status = RefactoringStatus.ERROR;
      } else if (((BinField) member).getTypeRef().isPrimitiveType()) {
        msg = "Can't create delegates for primitive type";
        status = RefactoringStatus.ERROR;
      } else if (createDelegateMethodsList((BinField) member).size() == 0) {
        msg = "No new methods to delegate for selected field";
        status = RefactoringStatus.ERROR;
      } else {
        selectedFields.add(member);
        this.owner = (BinClass) member.getOwner().getBinCIType();
      }
    } else {
      Assert.must(false, "unknown BinType");
    }

    RefactoringStatus rStatus = new RefactoringStatus(msg, status);

    return rStatus;
  }

  public RefactoringStatus checkUserInput() {
    Map map = model.getSelectedMap();

    // if there are methods with same signature set will filter them( quick HACK ;)

    delegatesCollect = new HashSet();

    final List keySet = new ArrayList(map.keySet());

    // sort for test fix, also determines that retain subclass field method
    // if clashes
    Collections.sort(keySet, new Comparator() {
      public int compare(Object param1, Object param2) {
        BinField field1 = (BinField) param1;
        BinField field2 = (BinField) param2;

        if (field1.getOwner().isDerivedFrom(field2.getOwner())) {
          return -1;
        }

        if (field2.getOwner().isDerivedFrom(field2.getOwner())) {
          return 1;
        }

        return field1.getName().compareTo(field2.getName());
      }
    });

    for (Iterator iter = keySet.iterator(); iter.hasNext(); ) {
      BinField field = (BinField) iter.next();

      Iterator iter2 = ((Collection) map.get(field)).iterator();
      while (iter2.hasNext()) {
        BinMethod method = (BinMethod) iter2.next();
        delegatesCollect.add(createMethodSkeleton(field, method));
      }
    }

    return new RefactoringStatus();
  }

  /**
   * @param owner
   * @param binMethod
   * @param field
   */
  private static MethodSkeleton createMethodSkeleton(
      final BinCIType owner, final BinMethod binMethod, final BinField field
      ) {
    return new DelegateMethodSkeleton(field, owner, binMethod);
  }

  public AddDelegatesRefactoring(RefactorItContext context, Object object) {
    super("Add Delegate Method", context);

    this.target = object;
  }

  /**
   * precond: checkPreconditions must be called before this
   */
  public AddDelegatesModel getModel() {
    model = new AddDelegatesModel(owner, selectedFields);
    return model;
  }

  private MethodSkeleton createMethodSkeleton(BinField field, BinMethod method) {
    MethodSkeleton skeleton = createMethodSkeleton(owner, method, field);

    if (method.isStatic()) {
      // clear static
      skeleton.setIsStatic(false);
    }

    return skeleton;
  }

  public void setModel(AddDelegatesModel selectedModel) {
    this.model = selectedModel;
  }

  public static BinField[] getDelegateFields(BinClass clazz) {
    List list = new ArrayList();

    BinField[] allFields = (BinField[]) clazz
        .getAccessibleFields(clazz).toArray(BinField.NO_FIELDS);

    for (int i = 0; i < allFields.length; i++) {
      if (allFields[i].getTypeRef().getBinType().isPrimitiveType()) {
        continue;
      }

      list.add(allFields[i]);
    }

    return (BinField[]) list.toArray(BinField.NO_FIELDS);
  }

  public String getDescription() {
    Map map = model.getSelectedMap();
    final List keySet = new ArrayList(map.keySet());
    String fields = "";
    for (int i = 0; i < keySet.size(); i++) {
      fields += ((BinField) keySet.get(i)).getName() + ", ";
    }
    fields = fields.substring(0, fields.length() - 2);

    return "Add delegate methods to " + getTarget().getName() + " of " + fields
        + ".";
  }

  public String getKey() {
    return key;
  }

}
