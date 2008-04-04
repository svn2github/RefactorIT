/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel.expressions;

import net.sf.refactorit.classmodel.BinConvertorTypeRef;
import net.sf.refactorit.classmodel.BinExpressionList;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinTypeRefManager;
import net.sf.refactorit.classmodel.BinTypeRefVisitor;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.query.BinItemVisitor;


public class BinMethodInvocationExpression extends
    BinMemberInvocationExpression implements
    MethodOrConstructorInvocationExpression, BinTypeRefManager {

  /**
   * @param method which was invoked.
   * @param expression before dot. <CODE>null</CODE> in case of dotless
   * invocation.
   * @param expressionList parameters.
   * @param invokedOn either a return type of expression before dot or enclosing
   * type for dotless invocations.
   */
  public BinMethodInvocationExpression(final BinMethod method,
      final BinExpression expression,
      final BinExpressionList expressionList,
      final BinTypeRef invokedOn, final ASTImpl rootAst) {
    super(method, expression, invokedOn, rootAst);

    this.expressionList = expressionList;
    //System.out.println(BinMethodUtil.toString(expressionList.getExpressions()));
//    if (Assert.enabled) {
//      Assert.must(rootAst.getType() == JavaTokenTypes.METHOD_CALL, "Wrong ast: " + rootAst);
//    }
  }

  public final BinTypeRef getReturnType() {
//System.err.println("getReturnType: " + this.toString());
    if (returnType == null) {
      returnType = getMethod().getReturnType();
      convertReturnType();
    }

    return returnType;
  }

  private void convertReturnType() {
    BinTypeRef orig = returnType;

    if (returnType.getBinType().isTypeParameter()) {
      returnType = BinConvertorTypeRef.findTypeArgumentInMethodParams(
          returnType, BinParameter.parameterTypes(getMethod().getParameters()),
          getExpressionList().getExpressionTypes());
    }

//      BinExpression[] exprs = getExpressionList().getExpressions();
//      for (int i = 0, max = exprs.length; i < max; i++) {
//        returnType = BinConvertorTypeRef.convertTypeParameter(
//            returnType, getMethod().getTypeParameters(), exprs[i]);
//        if (returnType != orig) { // something changed
//          break;
//        }
//      }

    if (returnType == orig && getExpression() != null) { // nothing changed yet
      returnType = convertTypeParameter(
          getExpression().getReturnType(), returnType);
    }

    if (returnType instanceof BinConvertorTypeRef) {
      ((BinConvertorTypeRef) returnType).setMethodParams(
          getMethod().getTypeParameters(),
          getExpressionList().getExpressionTypes());
      ((BinConvertorTypeRef) returnType).setMethodTypeArgs(getTypeArguments());
    }
//System.err.println("converted method: " + getMethod().getReturnType() + " --> " + returnType);

    // converted, but again type parameter, so repeat
    if (returnType != orig && returnType.getBinType().isTypeParameter()) {
      convertReturnType();
    }
  }

  public final BinMethod getMethod() {
    return (BinMethod) getMember();
  }

  public final BinExpressionList getExpressionList() {
    return this.expressionList;
  }

  public final void accept(final BinItemVisitor visitor) {
    //try {
      visitor.visit(this);
    /*} catch (RuntimeException ex) {
      System.err.println("Exception on visiting: " + getNameAst().getText());
      ex.printStackTrace();
      throw ex;
    }*/
  }

//  public final BinTypeRef getResolverType() {
//    return this.resolverRef;
//  }
//
//  public final void setResolverType(final BinTypeRef resolverRef) {
//    this.resolverRef = resolverRef;
//  }

  public final void defaultTraverse(final BinItemVisitor visitor) {
    super.defaultTraverse(visitor);

    // parameters
    if (expressionList != null) {
      expressionList.accept(visitor);
    }
  }

  public final void clean() {
    if (expressionList != null) {
      expressionList.clean();
      expressionList = null;
    }
//    resolverRef = null;
    super.clean();
  }

  public final boolean isSame(BinItem other) {
    if (!(other instanceof BinMethodInvocationExpression)) {
      return false;
    }
    final BinMethodInvocationExpression expr
        = (BinMethodInvocationExpression) other;
    return this.getMethod().isSame(expr.getMethod())
        && isBothNullOrSame(this.getInvokedOn(), expr.getInvokedOn()) // isn't it too strict?
        && isBothNullOrSame(this.getExpression(), expr.getExpression())
        && isBothNullOrSame(this.expressionList, expr.expressionList);
  }

  public void setTypeArguments(BinTypeRef[] typeArguments) {
    this.typeArguments = typeArguments;
  }

  public BinTypeRef[] getTypeArguments() {
    return this.typeArguments;
  }

  public void accept(BinTypeRefVisitor visitor) {
    if (typeArguments != null) {
      for (int i = 0, max = typeArguments.length; i < max; i++) {
        typeArguments[i].accept(visitor);
      }
    }
  }

  private BinExpressionList expressionList;

  // The base BinCIType that was used for resolving specified BinMethod
//  private BinTypeRef resolverRef = null;

  private BinTypeRef returnType;

  private BinTypeRef[] typeArguments;
}
