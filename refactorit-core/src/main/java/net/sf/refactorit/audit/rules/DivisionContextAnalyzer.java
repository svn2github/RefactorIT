/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules;

import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinExpressionList;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.expressions.BinArithmeticalExpression;
import net.sf.refactorit.classmodel.expressions.BinArrayInitExpression;
import net.sf.refactorit.classmodel.expressions.BinArrayUseExpression;
import net.sf.refactorit.classmodel.expressions.BinAssignmentExpression;
import net.sf.refactorit.classmodel.expressions.BinCastExpression;
import net.sf.refactorit.classmodel.expressions.BinConditionalExpression;
import net.sf.refactorit.classmodel.expressions.BinConstructorInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinInstanceofExpression;
import net.sf.refactorit.classmodel.expressions.BinLogicalExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinNewExpression;
import net.sf.refactorit.classmodel.expressions.BinStringConcatenationExpression;
import net.sf.refactorit.classmodel.expressions.BinUnaryExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.classmodel.statements.BinExpressionStatement;
import net.sf.refactorit.classmodel.statements.BinReturnStatement;
import net.sf.refactorit.query.BinItemVisitor;

/**
 *
 * @author  Arseni Grigorjev
 */
public class DivisionContextAnalyzer extends BinItemVisitor {
  private String locationString = null;
  private boolean floatingPointContext = false;

  private BinExpression divExpr;

  public DivisionContextAnalyzer(BinExpression divExpr){
    this.divExpr = divExpr;
  }

  private void analyzeExpr(final BinExpression leftExpr, 
      final BinExpression rightExpr, final BinExpression expression, 
      String location) {
    BinExpression otherExpr = (leftExpr == this.divExpr)
        ? rightExpr : leftExpr;

    BinPrimitiveType otherType = getPrimitiveReturnType(otherExpr);
    checkGivenContext(otherType, location);
  }

  public void visit (BinArithmeticalExpression expression){
    analyzeExpr(expression.getLeftExpression(), 
        expression.getRightExpression(), expression, 
        "arithmetical expression");
  }

  public void visit (BinLogicalExpression expression){
    analyzeExpr(expression.getLeftExpression(), 
        expression.getRightExpression(), 
        expression,
        "logical expression");
  }

  public void visit (BinAssignmentExpression expression){
    analyzeExpr(expression.getLeftExpression(), 
        expression.getRightExpression(), 
        expression,
        "assignment expression");
  }

  public void visit (BinReturnStatement statement){
    BinType returnType = statement.getMethod().getReturnType().getBinType();
    checkGivenContext(returnType, "return statement");
  }

  public void visit (BinLocalVariable var){
    BinType varType = var.getTypeRef().getBinType();
    checkGivenContext(varType, "assignment expression");
  }

  public void visit (BinField var){
    BinType varType = var.getTypeRef().getBinType();
    checkGivenContext(varType, "assignment expression");
  }

  public void visit (BinExpressionList exprList){
    String location = "";

    BinExpression[] givenExpressions = exprList.getExpressions();

    BinParameter[] params = null;
    BinItemVisitable parentItem = exprList.getParent();
    if (parentItem instanceof BinMethodInvocationExpression){
      params = ((BinMethodInvocationExpression) parentItem)
          .getMethod().getParameters();
      location = "method invocation";
    } else if (parentItem instanceof BinNewExpression){
      BinConstructor constructor = ((BinNewExpression) parentItem)
          .getConstructor();
      if (constructor == null) {
        return;
      }
      params = constructor.getParameters();
      location = "'new' expression";
    } else if (parentItem instanceof BinConstructorInvocationExpression){
      params = ((BinConstructorInvocationExpression) parentItem)
          .getConstructor().getParameters();
      location = "constructor invocation";
    } else {
      return;
    }

    try {
      for (int i = 0; ; i++){
        if (givenExpressions[i] == this.divExpr){
          BinType paramType = params[i].getTypeRef().getBinType();
          checkGivenContext(paramType, location);
          break;
        }
      }
    } catch (ArrayIndexOutOfBoundsException e){
      // FIXME: shouldn't it be reported?
    }
  }

  /**
   * @param paramType context type
   * @param location where does integer division find itself
   */
  private void checkGivenContext(final BinType paramType, final String location) {
    if (paramType.isPrimitiveType() 
        && ((BinPrimitiveType) paramType).isFloatingPointType()){
      floatingPointContext = true;
      locationString = location;
    }
  }

  public void visit (BinConditionalExpression expr){
    final DivisionContextAnalyzer analyzer = new DivisionContextAnalyzer(expr);
    expr.getParent().accept(analyzer);
    locationString = "conditional expression";
    floatingPointContext = analyzer.isFloatingPointContext();
  }

  public void visit (BinArrayInitExpression e){   
  }

  public void visit (BinArrayUseExpression e){   
  }

  public void visit (BinInstanceofExpression e){   
  }

  public void visit (BinFieldInvocationExpression e){   
  }

  public void visit (BinVariableUseExpression e){   
  }

  public void visit (BinUnaryExpression expr){   
  }

  public void visit (BinCastExpression expr){   
  }

  public void visit (BinStringConcatenationExpression expr){   
  }

  public void visit (BinExpressionStatement expr){   
  }

  public String getLocationString() {
    return this.locationString;
  }

  public boolean isFloatingPointContext() {
    return this.floatingPointContext;
  }

  public BinExpression getLocationExpression() {
    return this.divExpr;
  }
  
  /**
   * returns primitive return type of given expression.
   * if return type not primitive returns null.
   * if given null expression - returns null.
   */
  public static BinPrimitiveType getPrimitiveReturnType(BinExpression expr){
    try{
      BinType btype;
      btype = expr.getReturnType().getBinType();

      if (btype instanceof BinPrimitiveType){
        return (BinPrimitiveType) btype;
      } else {
        return null;
      }
    } catch(NullPointerException e) {
      return null;
    }
  }
}
