/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.edit;

import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.undo.IUndoableEdit;
import net.sf.refactorit.refactorings.undo.IUndoableTransaction;
import net.sf.refactorit.refactorings.undo.RitUndoManager;
import net.sf.refactorit.refactorings.undo.SourceInfo;
import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.transformations.SimpleSourceHolder;
import net.sf.refactorit.vfs.Source;

import java.io.File;


public class DirCreator extends AbstractFilesystemEditor {
  public static final String PACKAGE_NOT_RELOCATED_MSG
      = "Renamed package successfully, but physical directory was not renamed.";

  private SourceHolder parentDir;
  private String newDirName;
  private boolean addToVcs;

  public DirCreator(SourceHolder parentDir, SourceHolder target,
      String newDirName, boolean addToVcs) {
    super(target);

    this.parentDir = parentDir;
    this.newDirName = newDirName;
    this.addToVcs = addToVcs;

    if (getTarget() instanceof SimpleSourceHolder) {
      ((SimpleSourceHolder) getTarget()).setName(newDirName);
      ((SimpleSourceHolder) getTarget()).setDisplayPath(
          this.parentDir.getDisplayPath() + File.separator + newDirName);
    }
  }

  public RefactoringStatus changeInFilesystem(LineManager e) {
    RefactoringStatus status = new RefactoringStatus();

    IUndoableTransaction transaction = RitUndoManager.getCurrentTransaction();
    IUndoableEdit undo = null;

    Source parent = parentDir.getSource();
    newDirName = newDirName.replace('.', Source.RIT_SEPARATOR_CHAR);

    if (transaction != null) {
      SourceInfo sourceInfo = new SourceInfo(parent, newDirName, true);
      undo = transaction.createCreateFileUndo(sourceInfo);

      // add undo recursively for each subdirectory in name
      for (int i = 0; i < newDirName.length(); i++) {
        if (newDirName.charAt(i) == Source.RIT_SEPARATOR_CHAR) {
          sourceInfo = new SourceInfo(parent.getAbsolutePath(),
              newDirName.substring(0, i), parent.getSeparatorChar(), true);
          IUndoableEdit un = transaction.createCreateFileUndo(sourceInfo);
          transaction.addEdit(un);
        }
      }
    }

    Source newDir = parent.mkdirs(newDirName, addToVcs);

    if (newDir == null) {
      status.addEntry(DirCreator.PACKAGE_NOT_RELOCATED_MSG,
          RefactoringStatus.WARNING);
    } else {
      getTarget().setSource(newDir);
      if (transaction != null) {
        transaction.addEdit(undo);
      }
    }

    return status;
  }

  public SourceHolder getTargetDir() {
    return this.parentDir;
  }

}
