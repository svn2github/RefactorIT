/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings.rename;

import net.sf.refactorit.refactorings.AbstractRefactoring;
import net.sf.refactorit.refactorings.RefactoringStatus;


public class RenameTestUtil {

  /**
   * Checks and performs given refactoring.
   * @param refactoring
   * @return null on success, message if failed
   */
  public static RefactoringStatus canBeSuccessfullyChanged(
      AbstractRefactoring refactoring) {
    RefactoringStatus status = refactoring.checkPreconditions();
    if (status != null && !status.isOk()) {
      return status;
    }

    status = refactoring.checkUserInput();
    if (status != null && !status.isOk() && !status.isInfo()) {
      return status;
    }

    status = refactoring.apply();

    if (status != null && !status.isOk()) {
      return status;
    }

    return null;
  }
}
