/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.edit;

import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.undo.IUndoableEdit;
import net.sf.refactorit.refactorings.undo.IUndoableTransaction;
import net.sf.refactorit.refactorings.undo.RitUndoManager;
import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.transformations.SimpleSourceHolder;
import net.sf.refactorit.vfs.Source;

import java.util.ArrayList;



/**
 * Renames a given File to the new one.
 * @author Jevgeni Holodkov
 * @author Anton Safonov
 */
public class FileRenamer extends AbstractFilesystemEditor {
  private SourceHolder targetDir;
  private String oldName;
  private String newName;
  private boolean mover = false;

  public static final String FILE_NOT_RELOCATED_MSG = "Package and folder name mismatch. Cannot relocate file: ";
  public static final String FILE_NOT_RENAMED_MSG = "Failed to rename file: ";

  public FileRenamer(SourceHolder source, SourceHolder targetDir,
      String oldName, String newName) {
    super(source);
    this.oldName = oldName;
    this.newName = newName;
    this.targetDir = targetDir;

    if (Assert.enabled) {
      Assert.must(this.newName != null && this.newName.trim().length() > 0,
          "Must have legal new name, given: \"" + this.newName + "\"");
    }
  }

  /** This one for rename */
  public FileRenamer(SourceHolder source, String oldName, String newName) {
    this(source, new SimpleSourceHolder(source.getSource().getParent(),
        source.getProject()), oldName, newName);
  }

  /** This one for move (and rename package) */
  public FileRenamer(SourceHolder source, SourceHolder targetDir, String newName) {
    this(source, targetDir, StringUtil.replace(source.getName(), ".java", ""),
        StringUtil.replace(newName, ".java", ""));
    mover = true;
  }

  public RefactoringStatus apply(LineManager manager) {
    RefactoringStatus status = new RefactoringStatus();

    status.merge(super.apply(manager));

    // check: can we rename file?? or not
//    Source destinationDir = (Source) transObj.get();
//
//    String futureName = "";
//
//    CompilationUnit compilationUnit = this.getInputCompilationUnit();
//
//    if (compilationUnit.getName().equals(this.oldName + ".java")) {
//      futureName = this.oldName + ".java";
//    } else // if it is not .java file
//    if ( StringUtil.replace(compilationUnit.getSource().getName(),
//          ".java", "").equals(compilationUnit.getSource().getName())) {
//      futureName = this.newName;
//    }

    return status;
  }

  public RefactoringStatus changeInFilesystem(LineManager manager) {
    RefactoringStatus status = new RefactoringStatus();

    Source source = getTarget().getSource();

    if (source.getName().equals(this.oldName + ".java")) { // .java file

      IUndoableTransaction undoTransaction
          = RitUndoManager.getCurrentTransaction();
      IUndoableEdit undo = null;

      String filename = this.newName + ".java";

      if (undoTransaction != null) {
        undo = undoTransaction.createRenameFileUndo(
            source,
            targetDir.getSource(),
            filename);
      }

      source = source.renameTo(targetDir.getSource(), filename);

      if (source != null) {
        if (undoTransaction != null) {
          undoTransaction.addEdit(undo);
        }

        source.renameFormFileIfExists(this.oldName, this.newName);
      }
    } else
    if (StringUtil.replace(source.getName(), ".java", "").equals(source.getName())) {
      // if it is not .java file

      IUndoableTransaction undoTransaction
          = RitUndoManager.getCurrentTransaction();
      IUndoableEdit undo = null;
      if (undoTransaction != null) {
        undo = undoTransaction.createRenameFileUndo(
            source,
            targetDir.getSource(),
            this.newName);
      }

      source = source.renameTo(targetDir.getSource(), this.newName);

      if (undoTransaction != null && source != null) {
        undoTransaction.addEdit(undo);
      }
    }

    if (source == null) {
      status.addEntry(FileRenamer.FILE_NOT_RELOCATED_MSG
          + getTarget().getSource().getRelativePath(),
          RefactoringStatus.WARNING);
    } else {
      getTarget().setSource(source);
    }

    return status;
  }

  public boolean isMover() {
      return this.mover;
  }

  public String getName() {

    return this.getTarget().getSource().getRelativePath();
  }

  public String getDestination(ArrayList editors) {
    return findPath(editors);
  }

  public String getNewName() {
    Source source = getTarget().getSource();
    if (StringUtil.replace(source.getName(), ".java", "").equals(source.getName())) {
      // if it is not .java file
      return newName;
    } else {
      return newName + ".java";
    }
  }

  public String findPath(ArrayList editors) {
    String path = "";


    SourceHolder curDir;
//    path = newName.replace('.', Source.RIT_SEPARATOR_CHAR);
//    path = path.substring(0, path.length()-oldName.length());
    curDir = this.targetDir;

    if (curDir.getSource() != null) {
      path = curDir.getSource().getRelativePath().replace('.', Source.RIT_SEPARATOR_CHAR);
      return path + Source.RIT_SEPARATOR_CHAR;
    }

    int a = 1000;
    while ((a--) > 0) {
      for (int i = 0; i < editors.size(); i++) {
        if (editors.get(i) instanceof DirCreator) {
          DirCreator dc;
          dc = (DirCreator) editors.get(i);
          if (dc.getTarget() == curDir) {
            if (dc.getTarget().getSource() != null) {
              path = dc.getTarget().getSource().getRelativePath().replace('.',
                  Source.RIT_SEPARATOR_CHAR) + Source.RIT_SEPARATOR_CHAR + path;
              return path;
            } else {
              path = dc.getTarget().getName().replace('.', Source.RIT_SEPARATOR_CHAR) + Source.RIT_SEPARATOR_CHAR + path;
              curDir = dc.getTargetDir();
              if (curDir.getSource() != null) {
                path = curDir.getSource().getRelativePath().replace('.',
                    Source.RIT_SEPARATOR_CHAR) + Source.RIT_SEPARATOR_CHAR + path;
                return path;
              }
              break;
            }
          }
        }
      }
    }

    return "";
  }


  public String getOldName() {
    return this.oldName;
  }
}
