/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.undo;

import net.sf.refactorit.common.util.CFlowContext;
import net.sf.refactorit.common.util.ProgressListener;

import javax.swing.undo.AbstractUndoableEdit;


public abstract class SingleUndoableEdit extends AbstractUndoableEdit implements
    IUndoableEdit {

  private RepositoryTransaction transaction;

  /**
   * @param transaction
   */
  public SingleUndoableEdit(RepositoryTransaction transaction) {
    this.transaction = transaction;
  }

  public UndoableStatus getUndoStatus() {
    return UndoableStatus.OK_STATUS;
  }

  public UndoableStatus getRedoStatus() {
    return UndoableStatus.OK_STATUS;
  }

  public boolean isSignificant() {
    return true;
  }

  void setTransaction(RepositoryTransaction tr) {
    this.transaction = tr;
  }

  public RepositoryTransaction getTransaction() {
    return this.transaction;
  }

  public void undo() {
    super.undo();
    showProgress();
  }

  public void redo() {
    super.redo();
    showProgress();
  }

  private void showProgress() {
    if (getTransaction() instanceof UndoableTransaction) {
      ProgressListener listener = (ProgressListener)
          CFlowContext.get(ProgressListener.class.getName());

      UndoableTransaction tr = ((UndoableTransaction) getTransaction());
      tr.increaseAppliedEditors();
      if(listener != null) {
        listener.progressHappened(tr.progress.
            getPercentage(tr.getAppliedEditors() - 1, tr.getEditorsAmount()));
      }
    }
  }
}
