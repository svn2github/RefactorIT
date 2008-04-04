/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.exceptions;

import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.expressions.BinConstructorInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinNewExpression;
import net.sf.refactorit.classmodel.statements.BinThrowStatement;
import net.sf.refactorit.classmodel.statements.BinTryStatement;
import net.sf.refactorit.common.util.CFlowContext;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.common.util.ProgressListener;
import net.sf.refactorit.query.DelegateVisitor;
import net.sf.refactorit.query.ProgressMonitor;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * @author Oleg Tsernetsov
 *
 * Searches try clauses where specific methods are called. Collects data
 * about throws in try blocks, that are not thrown by specific methods
 */

public class TryClauseIndexer extends DelegateVisitor {
  private List stack = new ArrayList();

  private MultiValueMap violated;

  private MultiValueMap tryBlockMethods = new MultiValueMap();

  private MultiValueMap alienThrows = new MultiValueMap();

  public static ProgressMonitor.Progress FIND_EXISTING = new ProgressMonitor.Progress(
      0, 100);

  private int totalUnits = 0, iUnit = 0;

  private ProgressListener listener;

  public TryClauseIndexer(MultiValueMap violated) {
    this.violated = violated;
  }

  public void visit(final Project p) {
    listener = (ProgressListener) CFlowContext.get(ProgressListener.class
        .getName());
    totalUnits = p.getCompilationUnits().size();
  }

  public void visit(CompilationUnit c) {
    if (listener != null) {
      listener.progressHappened(FIND_EXISTING
          .getPercentage(++iUnit, totalUnits));
    }
  }

  public void visit(BinTryStatement.TryBlock s) {
    stack.add(0, s);
    super.visit(s);
  }

  public void leave(BinTryStatement.TryBlock s) {
    if (!tryBlockMethods.containsKey(s)) {
      alienThrows.clearKey(s);
    }
    stack.remove(0);
  }

  public void visit(BinMethodInvocationExpression expr) {
    registerMethod(expr.getMethod());
    super.visit(expr);
  }

  public void visit(BinConstructorInvocationExpression expr) {
    registerMethod(expr.getConstructor());
    super.visit(expr);
  }

  public void visit(BinNewExpression expr) {
    registerMethod(expr.getConstructor());
    super.visit(expr);
  }

  public void visit(BinThrowStatement throwStatement) {
    if (stack.size() > 0) {
      alienThrows.put(stack.get(0), throwStatement.getExpression()
          .getReturnType());
    }
  }

  private void registerMethod(BinMethod meth) {
    if (meth != null && stack.size() > 0) {
      Object tryBlock = stack.get(0);
      List thrown = RedundantSearchHelper.getThrownTypeList(meth.getThrows());
      if (violated.keySet().contains(meth)) {
        tryBlockMethods.put(tryBlock, meth);
        thrown.removeAll(violated.get(meth));
        alienThrows.putAll(tryBlock, thrown);
      } else {
        alienThrows.putAll(tryBlock, thrown);
      }
    }
  }

  public MultiValueMap getTryBlockMethods() {
    return tryBlockMethods;
  }

  public MultiValueMap getAlienThrows() {
    return alienThrows;
  }
}
