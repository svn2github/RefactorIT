/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.classmodel.statements;

import net.sf.refactorit.classmodel.BinSourceConstruct;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.query.BinItemVisitor;


/**
 *		class BinSwitchStatement
 *		Purpose :	Defines "switch" statement
 */

public final class BinSwitchStatement extends BinStatement {

  public BinSwitchStatement(BinExpression condition, ASTImpl rootAst) {
    super(rootAst);
    this.condition = condition;
  }

  public final BinExpression getCondition() {
    return condition;
  }

  public final CaseGroup[] getCaseGroupList() {
    return caseGroupList;
  }

  public final void setCaseGroupList(CaseGroup[] caseGroupList) {
    this.caseGroupList = caseGroupList;
  }

  /**
   * FIXME: CaseGroup is never visited
   * When adding a visitor here some day be sure to update at least VariableUseAnalyzer
   */
  public static final class CaseGroup extends BinSourceConstruct {
    public CaseGroup(Case[] caseList, BinStatementList statementList,
        ASTImpl node) {
      super(node);
      this.caseList = caseList;
      this.statementList = statementList;
    }

    public final Case[] getCaseList() {
      return caseList;
    }

    public final BinStatementList getStatementList() {
      return statementList;
    }

    public final void clean() {
      for (int c = 0; c < caseList.length; ++c) {
        caseList[c].clean();
      }

      if (statementList != null) {
        statementList.clean();
        statementList = null;
      }
    }

    private final Case[] caseList;
    private BinStatementList statementList;
  }


  public static final class Case extends BinSourceConstruct {
    public Case(BinExpression expression, ASTImpl node) {
      super(node);
      this.expression = expression;
    }

    public final boolean isCase() {
      return expression != null;
    }

    public final BinExpression getExpression() {
      return expression;
    }

    public final void accept(BinItemVisitor visitor) {
      visitor.visit(this);
    }

    public final void defaultTraverse(BinItemVisitor visitor) {
      // Check expression's existence
      if (expression != null) {
        expression.accept(visitor);
      }
    }

    public final void clean() {
      if (expression != null) {
        expression.clean();
        expression = null;
      }
      super.clean();
    }

    private BinExpression expression;
  }


  public final void accept(BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public final void defaultTraverse(BinItemVisitor visitor) {
    condition.accept(visitor);

    for (int i = 0; i < caseGroupList.length; ++i) {
      CaseGroup gr = caseGroupList[i];

      Case[] caseList = gr.getCaseList();
      for (int c = 0; c < caseList.length; ++c) {
        caseList[c].accept(visitor);
      }

      gr.getStatementList().accept(visitor);
    }
  }

  public final void clean() {
    condition.clean();
    condition = null;

    for (int i = 0; i < caseGroupList.length; ++i) {
      caseGroupList[i].clean();
    }
    super.clean();
  }

  private BinExpression condition;
  private CaseGroup[] caseGroupList;
}
