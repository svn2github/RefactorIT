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
 *		class BinIfThenElseStatement
 *		Purpose :	Defines "if-then-else" statement
 */

public final class BinIfThenElseStatement extends BinStatement {

  public BinIfThenElseStatement(BinExpression condition,
      BinStatementList trueList,
      BinStatementList falseList,
      ASTImpl rootAst) {
    super(rootAst);
    this.condition = condition;
    this.trueList = trueList;
    this.falseList = falseList;
  }

  public final BinExpression getCondition() {
    return condition;
  }

  public final BinStatementList getTrueList() {
    return trueList;
  }

  public final BinStatementList getFalseList() {
    return falseList;
  }

  public final void accept(net.sf.refactorit.query.BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public final void defaultTraverse(net.sf.refactorit.query.BinItemVisitor visitor) {
    condition.accept(visitor);
    if (trueList != null) {
      trueList.accept(visitor);
    }
    if (falseList != null) {
      falseList.accept(visitor);
    }
  }

  public final void clean() {
    condition.clean();
    condition = null;
    if (trueList != null) {
      trueList.clean();
      trueList = null;
    }
    if (falseList != null) {
      falseList.clean();
      falseList = null;
    }
    super.clean();
  }

  private BinExpression condition;
  private BinStatementList trueList;
  private BinStatementList falseList;

}
