/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.classmodel.statements;

import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.parser.ASTImpl;



/**
 * Defines statement which contains single expression.
 */
public final class BinExpressionStatement extends BinStatement {

  public BinExpressionStatement(BinExpression expression, ASTImpl rootAst) {
    super(rootAst);
    if (Assert.enabled) {Assert.must(expression != null,
        "statement expr - expression missing");
    }
    this.expression = expression;
  }

  public final BinExpression getExpression() {
    return expression;
  }

  public final void accept(net.sf.refactorit.query.BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public final void defaultTraverse(net.sf.refactorit.query.BinItemVisitor visitor) {
    expression.accept(visitor);
  }

  public final void clean() {
    if (expression != null) {
      expression.clean();
      expression = null;
    }
    super.clean();
  }

  public final boolean isSame(BinItem other) {
    if (!(other instanceof BinExpressionStatement)) {
      return false;
    }
    final BinExpressionStatement x = (BinExpressionStatement) other;
    return this.expression.isSame(x.expression);
  }

  private BinExpression expression;
}
