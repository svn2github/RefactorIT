/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.undo;


import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.query.ProgressMonitor;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourcePath;

import org.apache.log4j.Logger;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * Undoable refactoring transaction, used addEdit to add undoable edits.
 *
 * @author Tonis Vaga
 */
public class UndoableTransaction extends CompoundEdit implements
    RepositoryTransaction, IUndoableTransaction {
  private Logger log = AppRegistry.getLogger(UndoableTransaction.class);

  RefactoringStatus status = new RefactoringStatus();

  private String name;

  // for debugging
  private String details;
  private BackupRepository repository;
  private long transactionFinishTime;
  private RitUndoManager manager;

  private Map savedModifiedState = new TreeMap();

  public ProgressMonitor.Progress progress = ProgressMonitor.Progress.FULL;
  private int editorsAmount = 0;
  private int editorsApplied = 0;

  /**
   * should used only by UndoManager
   */
  UndoableTransaction(RitUndoManager manager, String name, String details,
      BackupRepository repository) {
    this.manager = manager;
    this.name = name;
    this.repository = repository;
//    this.getRepository().clean();
    this.details = details;

    if (RitUndoManager.debug) {
      log.debug("Created UNDO " + getPresentationName() + " " +
          details + " with repository on " + repository.getBackupDir());
    }
  }

  public String getName() {
    return name;
  }

  public String getDetails() {
    return details;
  }

  /**
   * should use only when not possible to know details before refactoring
   * like: ExtractMethod - we do not know the name of new method when make
   * new transaction
   */
  public void setPresentationDetails(String details) {
    this.details = details;
  }

  public String getPresentationName() {
    String result;

    if (getName().length() + getDetails().length() < 25) {
      result = getName() + " '" + getDetails() + "'";
    } else if (getName().length() < 20) {
      String formDetails = getFormattedDetails();
      result = getName() + " " + formDetails;
    } else {
      result = getName();
    }
    return result;
  }

  private String getFormattedDetails() {
    String formDetails;
    final int maxDetails = 10;

    if (details.length() < maxDetails || details.length() <= 2) {
      formDetails = details;
    } else {

      int startI = details.lastIndexOf(' ');

      if (startI < 0) {
        startI = details.lastIndexOf('.');
      }

      if (startI < 0) {
        startI = 0;
      }
      startI = Math.max(startI, details.length() - maxDetails - 2);
      formDetails = "'..." + getDetails().substring(startI) + "'";
    }
    return formDetails;
  }

  public String getUndoPresentationName() {
    return "Undo " + getPresentationName();
  }

  public String getRedoPresentationName() {
    return "Redo " + getPresentationName();
  }

  RitUndoManager getUndoManager() {
    return this.manager;
  }

  String getBackupDir() {
    return repository.getBackupDir();
  }

  public IUndoableEdit createCreateFileUndo(SourceInfo info) {
    return new CreateFileUndo(info, this);
  }

  public SourcePath getSourcePath() {
    return RitUndoManager.getInstance().getProject().getPaths().getSourcePath();
  }

  public IUndoableEdit createRenameFileUndo(
      Source source,
      Source destDir,
      String newName) {
    return new RenameFileUndo(source, destDir, newName, this);
  }

  public IUndoableEdit createModifiedSourcesUndo(List sources) {
    return new SourceContentUndo(sources, this);
  }

  public BackupRepository getRepository() {
    return this.repository;
  }

  public IUndoableEdit createDeleteFileUndo(Source source) {
    return new DeleteFileUndo(source, this);
  }

  public void redo() throws CannotRedoException {
    if (!checkAndResolveRedoConflicts()) {
      status = new RefactoringStatus("", RefactoringStatus.CANCEL);
      return;
    } else {
      status = new RefactoringStatus(); //OK
    }

    AppRegistry.getLogger(this.getClass()).debug("\n\nREDO " + getName() + " "
        + getDetails() + " started");

    editorsApplied = 0; // reset counter for redo progress bar
    super.redo();
    setFinishTime(System.currentTimeMillis());
    AppRegistry.getLogger(this.getClass()).debug("\nREDO " + getName() + " "
        + getDetails() + " finished\n");
  }

  public void undo() throws CannotUndoException {
    if (!checkAndResolveUndoConflicts()) {
      status = new RefactoringStatus("", RefactoringStatus.CANCEL);
      return;
    } else {
      status = new RefactoringStatus();
    }

    AppRegistry.getLogger(this.getClass()).debug("\n\nUNDO " + getName() + " "
        + getDetails() + " started");

    editorsApplied = 0; // reset counter for undo progress bar
    super.undo();

    setFinishTime(System.currentTimeMillis());

    AppRegistry.getLogger(this.getClass()).debug("UNDO " + getName() + " "
        + getDetails() + " finished\n");

  }

  public void die() {
    super.die();
    if (RitUndoManager.debug) {
      log.debug("[tonisdebug]: Calling die for UNDO " +
          getPresentationName() + " " + details +
          " with repository on " + repository.getBackupDir() + "");
    }
  }

  public void end() {
    super.end();
    setFinishTime(System.currentTimeMillis());
  }

  /**
   * @return time when transaction was finished
   */
  public long getFinishTime() {
    return this.transactionFinishTime;
  }

  private void setFinishTime(long time) {
    transactionFinishTime = time;
  }

  /**
   * @param undo  true if look for undo conflicts,
   *     false if look for redo conflicts
   *
   * @return true if all conflict are resolved
   */
  protected boolean checkAndResolveConflicts(boolean undo) {
    ModifiedStatus modStatus = null;

    for (int i = 0; i < edits.size(); i++) {
      SingleUndoableEdit edit = (SingleUndoableEdit) edits.get(i);
      UndoableStatus status = null;
      if (undo) {
        status = edit.getUndoStatus();
      } else {
        status = edit.getRedoStatus();
      }

      if (!status.isOk()) {
        if (status instanceof ModifiedStatus) {
          if (modStatus != null) {
            modStatus.merge((ModifiedStatus) status);
          } else {
            modStatus = (ModifiedStatus) status;
          }
        } else {
          DialogManager dialogMgr = DialogManager.getInstance();
          dialogMgr.showCustomError(
              IDEController.getInstance().createProjectContext(),
              undo ? "Cannot Undo" : "Cannot Redo", status.getErrorMsg());

          return false;
        }
      }
    }

    if (modStatus != null) {
      modStatus.resolve();
      return modStatus.isOk();
    }

    return true;
  }

  public boolean checkAndResolveRedoConflicts() {
    return checkAndResolveConflicts(false);
  }

  public boolean checkAndResolveUndoConflicts() {
    return checkAndResolveConflicts(true);
  }

  /**
   * @return last undo or redo status
   */
  public RefactoringStatus getUndoRedoStatus() {
    return status;
  }

  public boolean addEdit(IUndoableEdit edit) {
    editorsAmount++;
    return super.addEdit(edit);
  }

  public void rememberLastModifiedFor(final List sources) {
    for (int i = 0, max = sources.size(); i < max; ++i) {
      Source source = (Source) sources.get(i);
      savedModifiedState.put(source.getAbsolutePath(),
          new Long(source.lastModified()));
    }
  }

  public void createLastModifiedRedirect(final Source source) {
    manager.addLastModifiedRedirect(
        source.getAbsolutePath(),
        source.lastModified(),
        (Long) savedModifiedState.get(source.getAbsolutePath()));
  }

  public int getEditorsAmount() {
    return this.editorsAmount;
  }

  public void increaseAppliedEditors() {
    editorsApplied++;
  }

  public int getAppliedEditors() {
    return editorsApplied;
  }

}
