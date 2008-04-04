/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.conflicts;

import net.sf.refactorit.source.edit.Editor;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Aqris Software AS</p>
 * @author Tonis Vaga
 * @version 1.0
 */

public class MockConflict extends Conflict {
  ConflictType conflictType;

  /**
   * @param conflictType
   */
  public MockConflict(ConflictType conflictType) {
    this.conflictType = conflictType;
  }

  public void resolve() {
    /**@todo Implement this net.sf.refactorit.refactorings.conflicts.Conflict abstract method*/
  }

  public ConflictType getType() {
    return conflictType;
  }

  public Editor[] getEditors() {
    /**@todo Implement this net.sf.refactorit.refactorings.conflicts.Conflict abstract method*/
    throw new java.lang.UnsupportedOperationException(
        "Method getEditors() not yet implemented.");
  }

  public int getSeverity() {
    /**@todo Implement this net.sf.refactorit.refactorings.conflicts.Conflict abstract method*/
    throw new java.lang.UnsupportedOperationException(
        "Method getSeverity() not yet implemented.");
  }

  public boolean isResolvable() {
    /**@todo Implement this net.sf.refactorit.refactorings.conflicts.Conflict abstract method*/
    throw new java.lang.UnsupportedOperationException(
        "Method isResolvable() not yet implemented.");
  }

  public String getDescription() {
    /**@todo Implement this net.sf.refactorit.refactorings.conflicts.Conflict abstract method*/
    throw new java.lang.UnsupportedOperationException(
        "Method getDescription() not yet implemented.");
  }

}
