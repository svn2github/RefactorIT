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
import net.sf.refactorit.classmodel.expressions.BinArrayUseExpression;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Juri Reinsalu
 */
public class ForinForArrViolation extends AwkwardSourceConstruct {
  private BinVariable arrVariable;
  private BinArrayUseExpression[] arrUses;
  private BinVariable iteratorVariable;

  public ForinForArrViolation(ForinArrayTraversalCandidateChecker checker) {
    super(checker.getForConstruct(), "J2SE 5.0 construct for/in candidate", "refact.audit.forin");
    this.arrVariable = checker.getArrVariable(checker.getForConstruct());
    this.iteratorVariable=checker.getIteratorVariable(checker.getForConstruct());
    this.arrUses=checker.getArrUses();
  }

  public List getCorrectiveActions() {
    List list = new ArrayList(1);
    list.add(ForinForArrCorrectiveAction.getInstance());
    return list;
  }

  public BinVariable getArrayVariable() {
    return this.arrVariable;
  }

  public BinArrayUseExpression[] getArrayUses() {
    return this.arrUses;
  }

  public BinVariable getIteratorVariable() {
     return this.iteratorVariable;
  }

}
