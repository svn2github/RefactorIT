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
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.source.format.BinIncDecExpressionFormatter;
import net.sf.refactorit.source.format.BinItemFormatter;


public final class BinIncDecExpression extends BinExpression {
  public BinIncDecExpression(BinExpression expression, int type,
      ASTImpl rootAst) {
    super(rootAst);
    this.expression = expression;
    this.type = type;
  }

  public BinTypeRef getReturnType() {
    return expression.getReturnType();
  }

  public BinExpression getExpression() {
    return expression;
  }

  public int getType() {
    return type;
  }

  public void accept(net.sf.refactorit.query.BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public void defaultTraverse(net.sf.refactorit.query.BinItemVisitor visitor) {
    expression.accept(visitor);
  }

  public void clean() {
    expression.clean();
    expression = null;
    super.clean();
  }

  public boolean isSame(BinItem other) {
    if (!(other instanceof BinIncDecExpression)) {
      return false;
    }
    final BinIncDecExpression expr = (BinIncDecExpression) other;
    return this.type == expr.type
        && this.expression.isSame(expr.expression);
  }

  public BinItemFormatter getFormatter() {
    return new BinIncDecExpressionFormatter(this);
  }

  private BinExpression expression;
  private final int type; // inc or dec, post or pre
}
