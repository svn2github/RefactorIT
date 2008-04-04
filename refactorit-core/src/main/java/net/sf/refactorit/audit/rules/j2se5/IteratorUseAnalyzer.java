/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.j2se5;

import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.query.BinItemVisitor;


class IteratorUseAnalyzer extends BinItemVisitor {
  private boolean useConflict = false;
  private boolean nextCalled = false;
  private BinVariable iteratorVar;
  private BinMethodInvocationExpression nextCallExpression;
  
  public IteratorUseAnalyzer(BinVariable iteratorVar) {
    this.iteratorVar = iteratorVar;
  }

  public void visit(BinVariableUseExpression expression) {
    if (useConflict)
      return;
    if (!this.iteratorVar.isSame(expression.getVariable())) {
      super.visit(expression);
      return;
    }
    if (nextCalled) { //only 1 use of i.next() allowed
      useConflict = true;
      return;
    }
    //nothing except .next() is allowed on i
    if (!isMethodInvocation(expression.getParent())) {
      useConflict = true;
      return;
    }
    BinMethodInvocationExpression invExpr = (BinMethodInvocationExpression) expression
            .getParent();
    if (!"next".equals(invExpr.getMethod().getName())
            || invExpr.getMethod().getParameters().length > 0) {
      useConflict = true;
      return;
    }
    nextCalled=true;
    setNextCallExpression(invExpr);
    super.visit(expression);
  }
  
  private boolean isMethodInvocation(BinItemVisitable binItemVisitable) {
    return binItemVisitable instanceof BinMethodInvocationExpression;
  }

  boolean isUseConflict() {
    return this.useConflict;
  }
  public BinMethodInvocationExpression getNextCallExpression() {
    return nextCallExpression;
  }
  public void setNextCallExpression(
          BinMethodInvocationExpression nextCallExpression) {
    this.nextCallExpression = nextCallExpression;
  }
}
