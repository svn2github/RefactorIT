/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.undo;

public class UndoException extends Exception {
  String desc;
  Throwable nested;

  public UndoException(Throwable t, String desc) {
    super(desc);
    nested = t;
    this.desc = desc;
  }
}
