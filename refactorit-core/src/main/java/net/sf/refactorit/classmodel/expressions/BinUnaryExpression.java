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
import net.sf.refactorit.source.format.BinItemFormatter;
import net.sf.refactorit.source.format.BinUnaryExpressionFormatter;


public class BinUnaryExpression extends BinExpression {
  public BinUnaryExpression(BinExpression expression, ASTImpl rootNode) {
    super(rootNode);
    this.expression = expression;
  }

  public final BinTypeRef getReturnType() {
    return expression.getReturnType();
  }

  public final BinExpression getExpression() {
    return expression;
  }

  public final int getType() {
    return getRootAst().getType();
  }

  public final void accept(net.sf.refactorit.query.BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public final void defaultTraverse(net.sf.refactorit.query.BinItemVisitor visitor) {
    expression.accept(visitor);
  }

  public final void clean() {
    expression.clean();
    expression = null;
    super.clean();
  }

  public final boolean isSame(BinItem other) {
    if (!(other instanceof BinUnaryExpression)) {
      return false;
    }
    final BinUnaryExpression expr = (BinUnaryExpression) other;
    return this.getType() == expr.getType()
        && this.expression.isSame(expr.expression);
  }

  public final BinItemFormatter getFormatter() {
    return new BinUnaryExpressionFormatter(this);
  }
  
  public static BinUnaryExpression createSynthetic(
      BinExpression expression, ASTImpl ast) {
    return new BinUnaryExpression(expression, ast) {

      private ASTImpl rootAst;

      protected void setRootAst(final ASTImpl rootAst) {
        this.rootAst = rootAst;
      }

      public ASTImpl getRootAst() {
        return rootAst;
      }
    }; 
  }
  
  private BinExpression expression;
}
