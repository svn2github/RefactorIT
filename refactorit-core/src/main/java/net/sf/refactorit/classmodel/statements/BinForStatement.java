/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.classmodel.statements;

import net.sf.refactorit.classmodel.BinExpressionList;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinSourceConstruct;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.classmodel.Scope;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.JavaTokenTypes;

import java.util.HashMap;


/**
 * Defines "for" statement.
 */
public final class BinForStatement extends BinStatement implements Scope {

  public BinForStatement(BinSourceConstruct initSourceConstruct,
      BinExpression condition, BinExpressionList iteratorExpressionList,
      ASTImpl rootAst) {
    super(rootAst);
    this.initSourceConstruct = initSourceConstruct;
    this.condition = condition;
    this.iteratorExpressionList = iteratorExpressionList;
  }

  public final BinSourceConstruct getInitSourceConstruct() {
    return initSourceConstruct;
  }

  public final BinExpressionList iteratorExpressionList() {
    return iteratorExpressionList;
  }

  public final BinExpression getCondition() {
    return condition;
  }

  public final BinStatementList getStatementList() {
    // FIXME: needs to return Statement. StatementList shall to be/is a child of Statement
    return statementList;
  }

  public final void setStatementList(BinStatementList slist) {
    statementList = slist;
  }

  public final void accept(net.sf.refactorit.query.BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public final void defaultTraverse(net.sf.refactorit.query.BinItemVisitor visitor) {

    if (initSourceConstruct != null) {
      initSourceConstruct.accept(visitor);
    }

    if (condition != null) {
      condition.accept(visitor);
    }

    statementList.accept(visitor);

    if (iteratorExpressionList != null) {
      iteratorExpressionList.accept(visitor);
    }
  }

  public final void initScope(HashMap variableMap, HashMap typeMap) {
//    myScopeRules = new ScopeRules(this, variableMap, typeMap);
  }

//  public ScopeRules getScopeRules() {
//    return myScopeRules;
//  }

  public final boolean contains(Scope other) {
    if (other instanceof LocationAware) {
      return contains((LocationAware) other);
    } else {
      return false;
    }
  }

  public final void clean() {
//    myScopeRules = null;
    if (initSourceConstruct != null) {
      initSourceConstruct.clean();
      initSourceConstruct = null;
    }
    if (condition != null) {
      condition.clean();
      condition = null;
    }
    if (iteratorExpressionList != null) {
      iteratorExpressionList.clean();
      iteratorExpressionList = null;
    }
    statementList.clean();
    statementList = null;
    super.clean();
  }

  public final boolean isSame(BinItem other) {
    if (!(other instanceof BinForStatement)) {
      return false;
    }
    final BinForStatement x = (BinForStatement) other;
    return isBothNullOrSame(this.initSourceConstruct, x.initSourceConstruct)
        && isBothNullOrSame(this.condition, x.condition)
        && isBothNullOrSame(this.iteratorExpressionList,
        x.iteratorExpressionList)
        && this.statementList.isSame(x.statementList);
  }

  public boolean isForEachStatement() {
    return getRootAst().getFirstChild().getType() == JavaTokenTypes.FOR_EACH_CLAUSE;
  }
  
//  private ScopeRules myScopeRules;

  /** BinVariableDeclaration or BinExpressionList, anything else? */
  private BinSourceConstruct initSourceConstruct;

  private BinExpression condition;
  private BinExpressionList iteratorExpressionList;
  private BinStatementList statementList;
}
