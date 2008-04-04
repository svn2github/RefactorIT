/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.classmodel.statements;


import net.sf.refactorit.classmodel.BinCatchParameter;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.classmodel.Scope;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.query.BinItemVisitor;

import java.util.HashMap;


/**
 *		class BinTryStatement
 *		Purpose :	Defines "try" statement
 */

public final class BinTryStatement extends BinStatement {

  public BinTryStatement(TryBlock tryBlock, CatchClause[] catches,
      Finally aFinally, ASTImpl rootAst) {
    super(rootAst);

    this.tryBlock = tryBlock;
    this.catches = catches;
    this.finallyBlock = aFinally;
  }

  public final TryBlock getTryBlock() {
    return this.tryBlock;
  }

  public final CatchClause[] getCatches() {
    return this.catches;
  }

  public final Finally getFinally() {
    return this.finallyBlock;
  }

  public static final class TryBlock extends BinStatement {

    public TryBlock(BinStatementList statementList, ASTImpl rootAst) {
      super(rootAst);
      if (Assert.enabled) {
        Assert.must(rootAst != null, "Root ast == null");
      }
      this.statementList = statementList;
    }

    public final BinStatementList getStatementList() {
      return this.statementList;
    }

    public final void accept(BinItemVisitor visitor) {
      visitor.visit(this);
    }

    public final void defaultTraverse(BinItemVisitor visitor) {
      this.statementList.accept(visitor);
    }

    public final void clean() {
      statementList.clean();
      statementList = null;
      super.clean();
    }

    public BinStatementList statementList;
  }


  public static final class CatchClause extends BinStatement implements Scope {

    public CatchClause(BinCatchParameter param,
        BinStatementList statementList, ASTImpl node) {
      super(node);
      this.param = param;
      this.statementList = statementList;

      this.param.setCatchClause(this);
    }

    public final BinCatchParameter getParameter() {
      return this.param;
    }

    public final BinStatementList getStatementList() {
      return this.statementList;
    }

    public final void accept(BinItemVisitor visitor) {
      visitor.visit(this);
    }

    public final void defaultTraverse(BinItemVisitor visitor) {
      this.param.accept(visitor);
      this.statementList.accept(visitor);
    }

    public final void initScope(HashMap variableMap, HashMap typeMap) {
//      this.myScopeRules = new ScopeRules(this, variableMap, typeMap);
    }

//    public ScopeRules getScopeRules() {
//      return this.myScopeRules;
//    }

    public final boolean contains(Scope other) {
      if (other instanceof LocationAware) {
        return contains((LocationAware) other);
      } else {
        return false;
      }
    }

    public final void clean() {
//      myScopeRules = null;
      param = null;
      statementList.clean();
      statementList = null;
      super.clean();
    }

//    private ScopeRules myScopeRules;

    public BinCatchParameter param;
    public BinStatementList statementList;
  }


  public static final class Finally extends BinStatement {

    public Finally(BinStatementList statementList, ASTImpl node) {
      super(node);
      this.statementList = statementList;
    }

    public final BinStatementList getStatementList() {
      return this.statementList;
    }

    public final void accept(BinItemVisitor visitor) {
      visitor.visit(this);
    }

    public final void defaultTraverse(BinItemVisitor visitor) {
      this.statementList.accept(visitor);
    }

    public final void clean() {
      statementList.clean();
      statementList = null;
      super.clean();
    }

    public BinStatementList statementList;
  }


  public final void accept(BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public final void defaultTraverse(BinItemVisitor visitor) {

    this.tryBlock.accept(visitor);

    for (int i = 0; i < catches.length; ++i) {
      this.catches[i].accept(visitor);
    }

    if (finallyBlock != null) {
      this.finallyBlock.accept(visitor);
    }
  }

  public final String toString() {
    String result = "try: ";
    for (int i = 0, max = this.catches.length; i < max; i++) {
      result += (i != 0 ? ", " : "")
          + this.catches[i].getParameter().getQualifiedName();
    }
    return result;
  }

  public final void clean() {
    tryBlock.clean();
    tryBlock = null;
    for (int i = 0; i < catches.length; ++i) {
      this.catches[i].clean();
    }

    if (finallyBlock != null) {
      this.finallyBlock.clean();
    }
    super.clean();
  }

  public TryBlock tryBlock;
  public final CatchClause[] catches;
  public final Finally finallyBlock;
}
