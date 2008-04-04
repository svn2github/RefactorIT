/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.j2se5;

import net.sf.refactorit.classmodel.statements.BinForStatement;
import net.sf.refactorit.classmodel.statements.BinWhileStatement;


/**
 * @author Juri Reinsalu
 */
public class ForinRule extends J2Se5AuditRule {
  public static final String NAME = "forin";
  public static final String ACRONYM = NAME;

  public void visit(BinForStatement statement) {
    // :) System.out.println("ZAPUSKAETSA :)!!!!!!!!!!");
    ForinForCandidateChecker checker = new ForinArrayTraversalCandidateChecker();
    if (checker.isForinCandidate(statement)) {
      this.addViolation(new ForinForArrViolation((ForinArrayTraversalCandidateChecker)checker));
      super.visit(statement);
      return;
    }
    checker=new ForinForIteratorTraversalCandidateChecker();
    if(checker.isForinCandidate(statement)) {
      this.addViolation(new ForinForIteratorViolation((ForinForIteratorTraversalCandidateChecker)checker));
      super.visit(statement);
      return;
    }
    // ForinForIteratorTraversalCandidateChecker arrChecker = new
    // ForinArrayTraversalCandidateChecker();
    super.visit(statement);
  }

  public void visit(BinWhileStatement statement) {
    ForinWhileIteratorTraversalCandidateChecker checker=new ForinWhileIteratorTraversalCandidateChecker();
    if(checker.isForinCandidate(statement)) {
      this.addViolation(new ForinWhileIteratorViolation(checker));
      super.visit(statement);
      return;
    }
    super.visit(statement);
  }
}
