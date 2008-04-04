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
 *		class BinSynchronizedStatement
 *		Purpose :	Defines "synchronized" statement
 */

public final class BinSynchronizedStatement extends BinStatement {

  public BinSynchronizedStatement(BinExpression expression,
      BinStatementList statementList,
      ASTImpl node) {
    super(node);

    this.expression = expression;
    this.statementList = statementList;
  }

  public final BinExpression getExpr() {
    return expression;
  }

  public final BinStatementList getStatementList() {
    return statementList;
  }

  public final void accept(net.sf.refactorit.query.BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public final void defaultTraverse(net.sf.refactorit.query.BinItemVisitor visitor) {
    expression.accept(visitor);
    statementList.accept(visitor);
  }

  public final void clean() {
    expression.clean();
    expression = null;
    statementList.clean();
    statementList = null;
    super.clean();
  }

  private BinExpression expression;
  private BinStatementList statementList;

}
