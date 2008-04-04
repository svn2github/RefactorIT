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
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.query.BinItemVisitor;


public final class BinInstanceofExpression extends BinExpression {

  public BinInstanceofExpression(BinExpression leftExpression,
      BinExpression rightExpression,
      ASTImpl rootAst) {
    super(rootAst);
    this.leftExpression = leftExpression;
    this.rightExpression = rightExpression;
  }

  public final BinTypeRef getReturnType() {
    return BinPrimitiveType.BOOLEAN_REF;
  }

  public final BinExpression getLeftExpression() {
    return leftExpression;
  }

  public final BinExpression getRightExpression() {
    return rightExpression;
  }

  public final void accept(BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public final void defaultTraverse(BinItemVisitor visitor) {
    leftExpression.accept(visitor);
    rightExpression.accept(visitor);
  }

  public final void clean() {
    leftExpression.clean();
    leftExpression = null;
    rightExpression.clean();
    rightExpression = null;
    super.clean();
  }

  public final boolean isSame(BinItem other) {
    if (!(other instanceof BinInstanceofExpression)) {
      return false;
    }
    final BinInstanceofExpression expr = (BinInstanceofExpression) other;
    return this.leftExpression.isSame(expr.leftExpression)
        && this.rightExpression.isSame(expr.rightExpression);
  }

  private BinExpression leftExpression;
  private BinExpression rightExpression;

}
