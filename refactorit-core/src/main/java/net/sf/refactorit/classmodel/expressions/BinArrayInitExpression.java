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
import net.sf.refactorit.source.format.BinArrayInitExpressionFormatter;
import net.sf.refactorit.source.format.BinItemFormatter;


public final class BinArrayInitExpression extends BinExpression {
  public BinArrayInitExpression(BinExpression[] expressions, ASTImpl rootAst) {
    super(rootAst);
    this.expressions = expressions;
  }

  public final BinTypeRef getReturnType() {
    return null;
  }

  public final BinExpression[] getExpressions() {
    return expressions;
  }

  public final void accept(net.sf.refactorit.query.BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public final void defaultTraverse(net.sf.refactorit.query.BinItemVisitor visitor) {
    for (int i = 0; i < expressions.length; ++i) {
      expressions[i].accept(visitor);
    }
  }

  public final void clean() {
    for (int i = 0; i < expressions.length; ++i) {
      expressions[i].clean();
    }
    expressions = null;
    super.clean();
  }

  public final boolean isSame(BinItem other) {
    if (!(other instanceof BinArrayInitExpression)) {
      return false;
    }
    final BinArrayInitExpression expr = (BinArrayInitExpression) other;
    if (this.expressions.length != expr.expressions.length) {
      return false;
    }
    for (int i = 0; i < expressions.length; ++i) {
      if (!this.expressions[i].isSame(expr.expressions[i])) {
        return false;
      }
    }
    return true;
  }
  public final BinItemFormatter getFormatter() {
    return new BinArrayInitExpressionFormatter(this);
  }
  private BinExpression[] expressions;
}
