/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.edit;

import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.undo.IUndoableEdit;
import net.sf.refactorit.refactorings.undo.IUndoableTransaction;
import net.sf.refactorit.refactorings.undo.RitUndoManager;
import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.vfs.Source;


/**
 * Erases both files and directories.
 * @author Kirill Buhalko
 * @author Anton Safonov
 */
public class FileEraser extends AbstractFilesystemEditor {

  public static final String FILE_NOT_REMOVED_MSG = "Could not remove file: ";

  public static final String DIR_NOT_REMOVED_MSG = "Could not remove directory: ";

  private boolean isPreviewEnabled = false;

  public FileEraser(SourceHolder target, boolean isPreviewEnabled) {
    this(target);
    this.isPreviewEnabled = isPreviewEnabled;
  }

  public FileEraser(SourceHolder target) {
    super(target);

    org.apache.log4j.Logger.getLogger(FileEraser.class).debug(
        "$$$ Erasing: " + target.getSource().getAbsolutePath());
  }

  public RefactoringStatus changeInFilesystem(LineManager manager) {
    RefactoringStatus status = new RefactoringStatus();

    Source source = getTarget().getSource();

    IUndoableTransaction transaction = RitUndoManager.getCurrentTransaction();
    IUndoableEdit undo = null;

    if (transaction != null) {
      undo = transaction.createDeleteFileUndo(source);
    }
    if (source.isFile()) {
      if (!source.delete()) {
        status.addEntry(FILE_NOT_REMOVED_MSG + source.getRelativePath(), // FIXME fix getDisplayPath and use here
            RefactoringStatus.WARNING);
      } else {

        if (transaction != null) {
          transaction.addEdit(undo);
        }
      }

      return status;
    }

    // check if dir is empty after move
    Source[] roots = getTarget().getProject().getPaths().getSourcePath()
        .getRootSources();

    for (;;) {
      boolean rootSource = false;
      for (int i = 0; i < roots.length; i++) {
        if (source.getAbsolutePath().equals(roots[i].getAbsolutePath())) {
          rootSource = true;
          break;
        }
      }

      if (!rootSource && source.getChildren().length == 0) {
        // FIXME: this questioning may cause serious problems under Eclipse!!!
        if (source.exists()) {
          int canDelete = DialogManager.getInstance().showYesNoQuestion(
              IDEController.getInstance().createProjectContext(),
              // FIXME: correct warning sentence - it is more generic now
              "warning.movetype.remove.empty.folder",
              "Delete folder \"" + source.getName() + "\" (it will be empty)?",
              DialogManager.YES_BUTTON);
          if (canDelete == DialogManager.YES_BUTTON) {
            // assumption - it will check for emptyness first

            if (transaction != null) {
              undo = transaction.createDeleteFileUndo(source);
            }

            Source parent = source.getParent();

            if (!source.delete()) {
              status.addEntry(DIR_NOT_REMOVED_MSG + source.getRelativePath(), // FIXME fix getDisplayPath and use here
                  RefactoringStatus.WARNING);
              break;
            } else {
              source = parent;
              if (transaction != null) {
                transaction.addEdit(undo);
              }
            }
          } else {
            break;
          }
        } else {
          break;
        }
      } else {
        break;
      }
    }

    return status;
  }

  public boolean isPreviewEnabled() {
    return isPreviewEnabled;
  }

  public RefactoringStatus apply(LineManager manager) {
    // should load sources for preview
    if (getTarget().getSource() != null && getTarget().getSource().isFile()) {
      try {
        manager.loadSource(getTarget(), true);
      } catch (Exception e) {
        AppRegistry.getExceptionLogger().warn(e, this);
      }
    }
    return super.apply(manager);
  }

  public String getName() {
    return getTarget().getSource().getRelativePath();
  }

}
