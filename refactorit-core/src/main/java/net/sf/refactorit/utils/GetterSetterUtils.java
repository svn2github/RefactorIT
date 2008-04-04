/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.utils;

import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.classmodel.expressions.BinAssignmentExpression;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinLogicalExpression;
import net.sf.refactorit.classmodel.statements.BinReturnStatement;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.refactorings.PropertyNameUtil;
import net.sf.refactorit.refactorings.RefactoringUtil;

import java.util.Iterator;
import java.util.List;


/**
 *
 * @author  tanel
 * @author Arseni Grigorjev
 */
public final class GetterSetterUtils {

  /** Don't instantiate */
  private GetterSetterUtils() {
  }

  // needs refactoring..
  public static BinMethod getGetterMethodFor(BinField field) {
    String[] name = PropertyNameUtil.getDefaultGetterName(field);

    BinMethod[] methods = field.getOwner().getBinCIType().getDeclaredMethods();
    for (int i = 0; i < methods.length; i++) {
      if (GetterSetterUtils.isGetterMethod(methods[i], field, new String[] {name[0]}, false)) {
        return methods[i];
      }
    }
    
    // checking for getBooleans()
    for (int i = 0; i < methods.length; i++) {
      if (GetterSetterUtils.isGetterMethod(methods[i], field, name, false)) {
        return methods[i];
      }
    }

    return null;
  }

  public static BinMethod getSetterMethodFor(BinField field) {
    String name = PropertyNameUtil.getDefaultSetterName(field);

    BinMethod[] methods = field.getOwner().getBinCIType().getDeclaredMethods();
    for (int i = 0; i < methods.length; i++) {
      if (GetterSetterUtils.isSetterMethod(methods[i], field, name, false)) {
        return methods[i];
      }
    }

    return null;
  }

  public static boolean isGetterMethod(BinMethod method,
      boolean checkBehaviour) {
    List fields = method.getOwner().getBinCIType()
        .getAccessibleFields(method.getOwner().getBinCIType());

    if (fields != null){
      for (int i = 0; i < fields.size(); i++){
        BinField field = ((BinField) fields.get(i));
        String[] name = PropertyNameUtil.getDefaultGetterName(field);
        if (GetterSetterUtils.isGetterMethod(method, field, name,
            checkBehaviour)){
          return true;
        }
      }
    }

    return false;
  }

  public static boolean isSetterMethod(BinMethod method,
      boolean checkBehaviour) {
    List fields = method.getOwner().getBinCIType()
        .getAccessibleFields(method.getOwner().getBinCIType());

    if (fields != null){
      for (int i = 0; i < fields.size(); i++){
        BinField field = ((BinField) fields.get(i));
        String name = PropertyNameUtil.getDefaultSetterName(field);
        if (GetterSetterUtils.isSetterMethod(method, field, name,
            checkBehaviour)){
          return true;
        }
      }
    }

    return false;
  }

  public static boolean isGetterMethod(BinMethod method, final BinField field,
      String[] getterName, boolean checkBehaviour) {
    for(int i = 0; i < getterName.length; i++) {
      if (method.getName().equals(getterName[i]) &&
          (method.getParameters().length == 0) &&
          (method.getReturnType().equals(field.getTypeRef()))) {
  
        if (!checkBehaviour){
          return true;
        } else {
          // additional "behaviour check": does the method actually return the
          // field he is "getter" for?
          Iterator returns
              = RefactoringUtil.findReturnStatementsForMethod(method).iterator();
          while (returns.hasNext()){
            BinReturnStatement statement = (BinReturnStatement) returns.next();
  
            ReturnsGivenFieldChecker checker
                = new ReturnsGivenFieldChecker(field);
            checker.reset();
            statement.accept(checker);
  
            if (checker.success){
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  static final class ReturnsGivenFieldChecker extends BinItemVisitor {
    public boolean success = false;
    BinField field = null;

    public ReturnsGivenFieldChecker(BinField field) {
      this.field = field;
    }

    public final void reset(){
      this.success = false;
    }

    public final void visit(BinFieldInvocationExpression x) {
      if (x.getField() == this.field){
        success = true;
      }
    }

    public final void visit(BinLogicalExpression x){
      // don`t search here
    }
  }

  public static boolean isSetterMethod(BinMethod method, BinField field,
      String setterName, boolean checkBehaviour) {
    BinParameter[] parameters = method.getParameters();
    if (method.getName().equals(setterName) &&
        (parameters.length == 1) &&
        (parameters[0].getTypeRef().equals(field.getTypeRef())) &&
        (method.getReturnType().equals(BinPrimitiveType.VOID.getTypeRef()))) {

      if (!checkBehaviour){
        return true;
      } else {
        // additional "behaviour check": does the method actually SET the
        // field he is "setter" for?
        SetsGivenFieldChecker checker = new SetsGivenFieldChecker(field);
        checker.reset();
        method.accept(checker);
        if (checker.success){
          return true;
        }
      }
    }
    return false;
  }

  static final class SetsGivenFieldChecker extends BinItemVisitor {
    public boolean success = false;
    private boolean inAssignment = false;
    BinField field = null;

    public SetsGivenFieldChecker(BinField field) {
      this.field = field;
    }

    public final void reset(){
      this.success = false;
    }

    public final void visit(BinFieldInvocationExpression x) {
      if (inAssignment){
        if (x.getField() == this.field){
          success = true;
        }
      }
    }

    public final void visit(BinAssignmentExpression x){
      if (!success){
        inAssignment = true;
        x.getLeftExpression().accept(this);
        inAssignment = false;
      }
    }
  }

  
  /**
   * Looks for a field, that is accessed or modified by this method 
   * (this method is a setter/getter for this field)
   * @param accessor or mofidier method
   * @return property or null if there is no such property 
   */
  public static BinField getAccessibleProperty(BinMethod method, boolean checkBehaviour) {
    List fields = method.getOwner().getBinCIType().getAccessibleFields(
        method.getOwner().getBinCIType());

    if (fields != null) {
      for (int i = 0; i < fields.size(); i++) {
        BinField field = ((BinField) fields.get(i));
        String setterName = PropertyNameUtil.getDefaultSetterName(field);
        String[] getterName = PropertyNameUtil.getDefaultGetterName(field);
        if (GetterSetterUtils.isSetterMethod(method, field, setterName,
            checkBehaviour) || GetterSetterUtils.isGetterMethod(method, field, getterName,
                checkBehaviour)) {
          return field;
        }
      }
    }

    return null;
  }
}
