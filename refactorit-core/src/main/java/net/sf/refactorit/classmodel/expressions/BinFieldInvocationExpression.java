/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel.expressions;

import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.parser.ASTImpl;


public class BinFieldInvocationExpression extends BinMemberInvocationExpression {

  public BinFieldInvocationExpression(final BinField field,
      final BinExpression expression,
      final BinTypeRef invokedOn, final ASTImpl rootAst) {
    super(field, expression, invokedOn, rootAst);
  }

  public final BinTypeRef getReturnType() {
    if (returnType == null) {
      returnType = getField().getTypeRef();
      convertReturnType();
    }

    return returnType;
  }

  private void convertReturnType() {
    if (getExpression() != null) {
      final BinTypeRef orig = returnType;

      returnType = convertTypeParameter(
          getExpression().getReturnType(), returnType);

      // converted, but again type parameter, so repeat
      if (returnType != orig && returnType.getBinType().isTypeParameter()) {
        convertReturnType();
      }
//System.err.println("converted field: " + getField().getTypeRef() + " --> " + returnType);
    }
  }

  public final BinField getField() {
    return (BinField) getMember();
  }

  public final void accept(final net.sf.refactorit.query.BinItemVisitor
      visitor) {
    visitor.visit(this);
  }

  public final boolean isSame(BinItem other) {
    if (!(other instanceof BinFieldInvocationExpression)) {
      return false;
    }
    final BinFieldInvocationExpression expr
        = (BinFieldInvocationExpression) other;
    return this.getField().isSame(expr.getField())
        && isBothNullOrSame(this.getInvokedOn(), expr.getInvokedOn()) // isn't it too strict?
        && isBothNullOrSame(this.getExpression(), expr.getExpression());
  }

  public final boolean hasDot() {
    return getExpression() != null;
  }

  private BinTypeRef returnType;
}
