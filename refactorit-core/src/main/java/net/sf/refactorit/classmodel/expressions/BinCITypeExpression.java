/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel.expressions;

import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinTypeRefManager;
import net.sf.refactorit.classmodel.BinTypeRefVisitor;
import net.sf.refactorit.parser.ASTImpl;


public class BinCITypeExpression extends BinExpression
    implements BinTypeRefManager {

  public BinCITypeExpression(final BinTypeRef returnType,
      final BinExpression expression, final ASTImpl rootAst) {
    super(rootAst);
    this.returnType = returnType;
    this.expression = expression;
  }

  public final BinTypeRef getReturnType() {
    return this.returnType;
  }

  public final BinExpression getExpression() {
    return this.expression;
  }

  public final void accept(net.sf.refactorit.query.BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public final void defaultTraverse(net.sf.refactorit.query.BinItemVisitor visitor) {
    if (expression != null) {
      expression.accept(visitor);
    }
  }

  public final void accept(BinTypeRefVisitor visitor) {
    if (this.returnType != null) {
      this.returnType.accept(visitor);
    }
  }

  public final void clean() {
    if (expression != null) {
      expression.clean();
      expression = null;
    }
    returnType = null;

    super.clean();
  }

  public final ASTImpl getClickableNode() {
    return returnType.getNode();
  }

  public final boolean isSame(final BinItem other) {
    if (!(other instanceof BinCITypeExpression)) {
      return false;
    }
    final BinCITypeExpression expr = (BinCITypeExpression) other;
    return isBothNullOrSame(this.getReturnType(), expr.getReturnType())
        && isBothNullOrSame(this.expression, expr.expression);
  }

  private BinTypeRef returnType;
  private BinExpression expression;
}
