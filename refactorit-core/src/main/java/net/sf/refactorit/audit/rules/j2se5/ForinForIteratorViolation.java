/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.j2se5;


import net.sf.refactorit.audit.AwkwardSourceConstruct;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Juri Reinsalu
 */
public class ForinForIteratorViolation extends AwkwardSourceConstruct {
  private BinVariable iteratorVariable;
  private BinVariable iterableVariable;
  private BinExpression iterableExpression;
  private BinMethodInvocationExpression nextCallExpression;

  ForinForIteratorViolation(ForinForIteratorTraversalCandidateChecker checker) {
    super(checker.getForConstruct(), "J2SE 5.0 construct for/in candidate", "refact.audit.forin");
    this.iteratorVariable=checker.getIteratorVariable();
    this.iterableVariable=checker.getIterableVariable();
    this.iterableExpression=checker.getIterableExpression();
    this.nextCallExpression=checker.getNextCallExpression();
  }

  public List getCorrectiveActions() {
    List list = new ArrayList(1);
    list.add(ForinForIteratorCorrectiveAction.getInstance());
    return list;
  }

  public BinVariable getIterableVariable() {
    return this.iterableVariable;
  }

  /**
   * @return
   */
  public BinExpression getIterableExpression() {
    return this.iterableExpression;
  }

  /**
   * @return
   */
  public BinMethodInvocationExpression getNextCallExpression() {
    return this.nextCallExpression;
  }

}
