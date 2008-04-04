/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;


public interface IUndoManager {

  void commitTransaction();

  /**
   * Can use only one transaction at time!! If not commited, info will be lost.
   * @param name name
   * @param details details
   * @return transaction
   */
  UndoableTransaction createTransaction(String name, String details);

  void redo() throws CannotRedoException;

  void rollbackTransaction();

  void undo() throws CannotUndoException;

  String getRedoPresentationName();

  String getUndoPresentationName();

  boolean canUndo();

  boolean canRedo();

  /**
   *
   * @param undo true if get undo details, false if redo details
   */
  public String getPresentationNameWIthDetails(boolean undo);

}
