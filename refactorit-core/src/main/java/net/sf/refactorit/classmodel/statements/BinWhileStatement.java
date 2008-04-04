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
 *		class BinWhileStatement
 *		Purpose :	Defines Do and While statement
 */

public final class BinWhileStatement extends BinStatement {

  public BinWhileStatement(BinExpression condition, boolean isDoWhile,
      ASTImpl rootAst) {
    super(rootAst);
    this.condition = condition;
    this.isDoWhile = isDoWhile;
  }

  public final BinExpression getCondition() {
    return condition;
  }

  public final BinStatementList getStatementList() {
    return statementList;
  }

  public final void setStatementList(BinStatementList slist) {
    statementList = slist;
  }

  public final boolean isDoWhile() {
    return isDoWhile;
  }

  public final void accept(net.sf.refactorit.query.BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public final void defaultTraverse(net.sf.refactorit.query.BinItemVisitor visitor) {
    if (isDoWhile) {
      statementList.accept(visitor);
      condition.accept(visitor);
    } else {
      condition.accept(visitor);
      statementList.accept(visitor);
    }
  }

  public final void clean() {
    condition.clean();
    condition = null;
    statementList.clean();
    statementList = null;
    super.clean();
  }

  private BinExpression condition;
  private BinStatementList statementList;
  private final boolean isDoWhile;

}
