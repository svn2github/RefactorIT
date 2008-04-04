/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.classmodel.statements;

import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.parser.ASTImpl;


/**
 *		class BinThrowStatement
 *		Purpose :	Defines "throw" statement
 */

public final class BinThrowStatement extends BinStatement {

  public BinThrowStatement(BinExpression expression, ASTImpl rootAst) {
    super(rootAst);
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
    expression.clean();
    expression = null;
    super.clean();
  }

  private BinExpression expression;
}
