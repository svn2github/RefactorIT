/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit;


import net.sf.refactorit.ui.module.TreeRefactorItContext;

import javax.swing.KeyStroke;

import java.util.List;
import java.util.Set;


/**
 *
 *
 * @author Igor Malinin
 */
public abstract class CorrectiveAction {
  public abstract String getKey();

  public abstract String getName();

  private boolean testRun = false;

  public String getMultiTargetName() {
    if (!isMultiTargetsSupported()) {
      throw new UnsupportedOperationException();
    }

    return getName();
  }

  public KeyStroke getKeyStroke() {
    return null;
  }

  public boolean isMultiTargetsSupported() {
    return false;
  }

  /**
   * Perform corrections and returns list of modified source files.
   *
   * @param context
   * @param violations
   *
   * @return set of modified CompilationUnits
   */
  public abstract Set run(TreeRefactorItContext context, List violations);

  public boolean isTestRun() {
    return this.testRun;
  }

  public void setTestRun(final boolean testRun) {
    this.testRun = testRun;
  }
}
