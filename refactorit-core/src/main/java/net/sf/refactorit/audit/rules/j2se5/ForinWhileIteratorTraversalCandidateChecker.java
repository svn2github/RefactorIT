/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.j2se5;

import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.classmodel.statements.BinLocalVariableDeclaration;
import net.sf.refactorit.classmodel.statements.BinStatementList;
import net.sf.refactorit.classmodel.statements.BinVariableDeclaration;
import net.sf.refactorit.classmodel.statements.BinWhileStatement;
import net.sf.refactorit.query.BinItemVisitor;

/**
 * @author Juri Reinsalu
 */
public class ForinWhileIteratorTraversalCandidateChecker{
  private BinWhileStatement whileStatement;
  private BinVariable iteratorVar;
  private BinExpression iterableAccessExpr;
  private BinMethodInvocationExpression nextCallExpression;
  private BinVariableDeclaration iteratorDeclStatement;
  private BinVariableUseExpression[] itemVarUsages;
  public BinVariable getIteratorVar() {
    return iteratorVar;
  }

  boolean isForinCandidate(BinWhileStatement whileStatement) {
    this.whileStatement=whileStatement;
    if(!isConditionOk(whileStatement.getCondition())) {
      return false;
    }
    if(!isIteratorUsageOk(whileStatement.getStatementList())) {
      return false;
    }
    BinItem parentScope=whileStatement.getParentMember();
    if(!iteratorHasReasonableDeclaration(parentScope)) {
      return false;
    }
    return true;
  }

  private boolean iteratorHasReasonableDeclaration(BinItem parentScope) {
    LoopExternalIteratorDeclarationAnalyzer declarationAnalyzer=new LoopExternalIteratorDeclarationAnalyzer(getIteratorVar());
    parentScope.defaultTraverse(declarationAnalyzer);
    if(declarationAnalyzer.isExternalUseConflict()) {
      return false;
    }
    if(!declarationAnalyzer.isDeclarationFound()) {
      return false;
    }
    this.iteratorDeclStatement=declarationAnalyzer.getIteratorDeclStatement();
    this.iterableAccessExpr=declarationAnalyzer.getCallToIterable();
    return true;
  }

  public class LoopExternalIteratorDeclarationAnalyzer extends BinItemVisitor{
    BinVariable iteratorVar;
    boolean declarationFound=false;
    BinVariableDeclaration iteratorDeclStatement;
    boolean externalUseConflict=false;
    private BinExpression callToIterable;
    public LoopExternalIteratorDeclarationAnalyzer(BinVariable iteratorVar) {
      this.iteratorVar=iteratorVar; 
    }
    
    public void visit(BinLocalVariableDeclaration varDecl) {
      if(declarationFound) {
        super.visit(varDecl);
        return;
      }
      BinVariable var=varDecl.getVariables()[0];
      if(!this.iteratorVar.isSame(var)) {
        super.visit(varDecl);
        return;
      }
      if (!(var.getExpression() instanceof BinMethodInvocationExpression)) {
        super.visit(varDecl);
        return;
      }
      BinMethodInvocationExpression initializerExpr = (BinMethodInvocationExpression) var.getExpression();
      if(!initializerExpr.getReturnType().isDerivedFrom("java.util.Iterator")) {
        super.visit(varDecl);
        return;
      }
      if(!"iterator".equals(initializerExpr.getMethod().getName())) {
        super.visit(varDecl);
        return;
      }
      // DEBUG System.out.println();
      this.callToIterable=initializerExpr.getExpression();
      this.iteratorDeclStatement=varDecl;
      declarationFound=true;
      super.visit(varDecl);
    }

    public void visit(BinVariableUseExpression varUseExpr) {
      if(!declarationFound) {
        super.visit(varUseExpr);
        return;  
      }
      if(!varUseExpr.getVariable().isSame(getIteratorVar())) {
        super.visit(varUseExpr);
        return;
      }
      if(getWhileStatement().contains(varUseExpr)) {
        super.visit(varUseExpr);
        return;        
      }
      externalUseConflict=true;
      return;
    }
    public boolean isExternalUseConflict() {
      return externalUseConflict;
    }
    public boolean isDeclarationFound() {
      return declarationFound;
    }
    public BinExpression getCallToIterable() {
      return callToIterable;
    }
    public BinVariableDeclaration getIteratorDeclStatement() {
      return iteratorDeclStatement;
    }
  }
  
  private boolean isIteratorUsageOk(BinStatementList statementList) {
    IteratorUseAnalyzer analyzer=new IteratorUseAnalyzer(getIteratorVar());
    statementList.defaultTraverse(analyzer);
    if(!analyzer.isUseConflict()) {
      setNextCallExpression(analyzer.getNextCallExpression());
      return true;
    }
    return false;
  }

  private boolean isConditionOk(BinExpression condition) {
    if(!(isMethodInvocation(condition)))
      return false;
    BinMethodInvocationExpression mInvExpr=(BinMethodInvocationExpression)condition;
    if(!"hasNext".equals(mInvExpr.getMethod().getName()) || mInvExpr.getMethod().getParameters().length!=0) {
      return false;
    }
    if(!(mInvExpr.getExpression()instanceof BinVariableUseExpression)) {
      return false;
    }
    BinVariable iteratorVar=((BinVariableUseExpression)mInvExpr.getExpression()).getVariable();
    if(!iteratorVar.getTypeRef().isDerivedFrom("java.util.Iterator")) {
      return false;
    }
    this.iteratorVar=iteratorVar;
    return true;
  }

  BinVariable getIteratorVariable() {
    return this.iteratorVar;
  }



  /**
   * @param iteratorInitExpr
   * @return
   */
  private boolean isMethodInvocation(BinItemVisitable expression) {
    return (expression instanceof BinMethodInvocationExpression);
  }

  /**
   * @return
   */
  public BinExpression getIterableExpression() {
    return this.iterableAccessExpr;
  }

  /**
   * @return
   */
  public BinMethodInvocationExpression getNextCallExpression() {
    return this.nextCallExpression;
  }

  public void setNextCallExpression(
          BinMethodInvocationExpression nextCallExpression) {
    this.nextCallExpression = nextCallExpression;
  }
  public BinWhileStatement getWhileStatement() {
    return whileStatement;
  }

  public BinVariableDeclaration getIteratorDeclStatement() {
    return this.iteratorDeclStatement;
  }

  public BinVariableUseExpression[] getItemVarUsages() {
    return this.itemVarUsages;
  }

}
