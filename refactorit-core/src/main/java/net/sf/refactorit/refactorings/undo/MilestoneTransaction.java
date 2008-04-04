/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.undo;



import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourcePath;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


/**
 * @author Tonis Vaga
 */
public class MilestoneTransaction extends AbstractUndoableEdit implements
    RepositoryTransaction, Serializable {
  boolean deleteSources;

  Project project;

  SourceContentUndo edit;

  List sourcesToDelete;
  BackupRepository rep;

  Date date;

  long finishTime;

  RefactoringStatus status = new RefactoringStatus();

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.writeObject(edit);
    out.writeObject(rep);
    out.writeObject(date);
    out.writeLong(finishTime);
  }

  private void readObject(ObjectInputStream in) throws IOException,
      ClassNotFoundException {

    edit = (SourceContentUndo) in.readObject();
    rep = (BackupRepository) in.readObject();
    date = (Date) in.readObject();
    finishTime = in.readLong();

    project = IDEController.getInstance().getActiveProject();
  }

  public MilestoneTransaction(Project project, String dir) {
    Assert.must(dir != null, "wrong value");

    rep = new BackupRepository(dir);
    rep.clean();

    this.project = project;

    edit = createProjectContentUndo();
    date = new Date(System.currentTimeMillis());

//    addEdit(new AbstractUndoableEdit()); // one fake edit to get things to work
  }

  private SourceContentUndo createProjectContentUndo() {
    List list = getProjectSources();
    setFinishTime(System.currentTimeMillis());
    return new SourceContentUndo(list, this, false);
  }

  public boolean isSignificant() {
    return true;
  }

  private List getProjectSources() {
    return BackupManagerUtil.createSourcesList(this.project.getCompilationUnits());
  }

  public SourcePath getSourcePath() {
    return project.getPaths().getSourcePath();
  }

//  public boolean canUndo() {
//    return inUndoState;
//  }
//  public boolean canRedo() {
//    return !inUndoState;
//
//  }

  public void redo() throws javax.swing.undo.CannotRedoException {
    AppRegistry.getLogger(this.getClass()).debug("\n\nMilestone REDO " + getInfo() + " started");

    restoreMilestone(false);
    super.redo();

    AppRegistry.getLogger(this.getClass()).debug("Milestone REDO " + getInfo() + " finished\n");
  }

  public void undo() throws javax.swing.undo.CannotUndoException {
    AppRegistry.getLogger(this.getClass()).debug("\n\nMilestone UNDO " + getInfo() + " started");

    restoreMilestone(true);
    super.undo();

    AppRegistry.getLogger(this.getClass()).debug("Milestone UNDO " + getInfo() + " finished\n");
  }

  /**
   * @param undoing undoing
   * @throws CannotUndoException
   */
  private void restoreMilestone(final boolean undoing) throws
      CannotUndoException {
    SourceContentUndo reverseEdit = createProjectContentUndo();

    edit.undo();

    if (deleteSources) {
      deleteSources();
    }

    edit = reverseEdit;

//      inUndoState=!inUndoState;
  }

  public RefactoringStatus checkAndResolveConflicts(final boolean undoing) {
    if (!checkAndResolve(undoing)) {
      status = new RefactoringStatus("", RefactoringStatus.CANCEL);
    } else {
      status = new RefactoringStatus();
    }
    return status;
  }

  public String getInfo() {
    return date.toString();
  }

  private boolean checkAndResolve(boolean undo) {
    UndoableStatus status = edit.getUndoStatus();

    if (status.getStatus() == UndoableStatus.ERROR) {
      // FIXME: add msg
      return false;
    }

    if (status instanceof ModifiedStatus) {
      status.resolve();
      if (!status.isOk()) {
        return false;
      }
    }

    sourcesToDelete = getSourcesForDeleting(undo);

    DialogManager dlgMgr = DialogManager.getInstance();

    if (sourcesToDelete.size() > 0) {
      StringBuffer msg = null;

      if (sourcesToDelete.size() <= 20) {
        msg = new StringBuffer(
            "<html>RefactorIT will delete following new files<p><ul>");
        for (int i = 0; i < sourcesToDelete.size(); i++) {
          msg.append("<li>" +
              ((Source) sourcesToDelete.get(i)).getDisplayPath() + "</li>");
        }
        msg.append("</ul>");
      } else {
        msg = new StringBuffer("<html>RefactorIT will delete " +
            sourcesToDelete.size() + " new files");
      }

      msg.append("<p><CENTER>Do you want to continue?</CENTER><html>");

      int result = dlgMgr.showCustomYesNoQuestion(
          IDEController.getInstance().createProjectContext(),
          "", msg.toString(), DialogManager.YES_BUTTON);

      deleteSources = (result == DialogManager.YES_BUTTON);

      if (!deleteSources) {
        return false;
      }
    }

    return true;
  }

  private List getSourcesForDeleting(boolean undo) {
    SourceHeader headers[] = edit.getUndoHeaderContent();

    Set oldFileNames = extractFileNames(headers);

    List sources = getProjectSources();

    List newSources = new LinkedList();

    for (Iterator i = sources.iterator(); i.hasNext(); ) {
      Source item = (Source) i.next();

      if (!oldFileNames.contains(item.getAbsolutePath())) {
        newSources.add(item);
      }
    }

    return newSources;
  }

  public static Set extractFileNames(SourceHeader[] headers) {
    Set result = new HashSet(headers.length);
    for (int i = 0; i < headers.length; i++) {
      result.add(headers[i].getAbsolutePath());
    }

    return result;
  }

  public void deleteSources() {
    if (sourcesToDelete == null) {
      return;
    }

    for (Iterator i = sourcesToDelete.iterator(); i.hasNext(); ) {
      Source item = (Source) i.next();

      // remove it when, undo be moved to Editors
      IUndoableTransaction transaction = RitUndoManager.
          getCurrentTransaction();
      IUndoableEdit undo = null;
      if (transaction != null) {
        undo = transaction.createDeleteFileUndo(item);
        transaction.addEdit(undo);
      }

      if (!item.delete()) {
        AppRegistry.getLogger(this.getClass()).debug("Deleting " + item.getAbsolutePath() + " failed");
      } else {

      }
    }
  }

  public BackupRepository getRepository() {
    return rep;
  }

  public long getFinishTime() {
    return finishTime;
  }

  public String getPresentationName() {
    return getInfo();
  }

  public void setFinishTime(final long finishTime) {
    this.finishTime = finishTime;
  }

  public RefactoringStatus getStatus() {
    return this.status;
  }

  public RefactoringStatus checkAndResolveRedoConflicts() {
    return checkAndResolveConflicts(false);
  }

  public RefactoringStatus checkAndResolveUndoConflicts() {
    return checkAndResolveConflicts(true);
  }
}
