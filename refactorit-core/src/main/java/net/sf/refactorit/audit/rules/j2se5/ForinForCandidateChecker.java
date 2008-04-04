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

/**
 * @author Juri Reinsalu
 */
public abstract class ForinForCandidateChecker {
  abstract boolean isForinCandidate(BinForStatement statement);

  protected BinForStatement forStatement;

  protected BinForStatement getForConstruct() {
    return this.forStatement;
  }
}
