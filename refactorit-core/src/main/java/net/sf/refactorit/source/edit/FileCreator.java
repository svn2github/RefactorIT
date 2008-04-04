/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.edit;


import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.parser.ASTTree;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.undo.IUndoableEdit;
import net.sf.refactorit.refactorings.undo.IUndoableTransaction;
import net.sf.refactorit.refactorings.undo.RitUndoManager;
import net.sf.refactorit.refactorings.undo.SourceInfo;
import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.transformations.SimpleSourceHolder;
import net.sf.refactorit.vfs.Source;

import java.io.File;
import java.io.IOException;


/**
 * Creates .java file in the specified path
 * @author Jevgeni Holodkov
 * @author Anton Safonov
 */
public class FileCreator extends AbstractFilesystemEditor {
  public static final int CREATE_SINGLE_FILE = 1;
  public static final int CREATE_CRAZY_FILE = 2;

  private int flags = 0;

  private String name = null;
  private SourceHolder targetDirectory = null;

  public FileCreator(SourceHolder input, SourceHolder targetDirectory,
      String name, int flags) {
    super(input);
    this.targetDirectory = targetDirectory;
    this.name = name;
    this.flags |= flags;

    if (getTarget() instanceof SimpleSourceHolder) {
      ((SimpleSourceHolder) getTarget()).setName(name + ".java");
      ((SimpleSourceHolder) getTarget()).setDisplayPath(
          this.targetDirectory.getDisplayPath() + File.separator + name + ".java");
    }
  }

  public RefactoringStatus changeInFilesystem(LineManager manager) {
    RefactoringStatus status = new RefactoringStatus();

    IUndoableTransaction transaction = RitUndoManager.getCurrentTransaction();
    IUndoableEdit undo = null;

    Source targetDir = targetDirectory.getSource();

    final String failedCreateMessage = "Failed to create a file \'"
        + name + ".java\' in " + targetDir.getRelativePath();

    try {
      Source newFile = null;

      if (isFlagSet(CREATE_CRAZY_FILE)) {
        newFile = createCrazyFile();
      }

      if (isFlagSet(CREATE_SINGLE_FILE)) {
        String filename = name + ".java";

        if (transaction != null) {
          SourceInfo sourceInfo = new SourceInfo(targetDir, filename);
          undo = transaction.createCreateFileUndo(sourceInfo);
        }

        newFile = targetDir.createNewFile(filename);

        if (transaction != null && newFile != null) {
          transaction.addEdit(undo);
        }

      }

      if (newFile == null) {
        status.addEntry(failedCreateMessage, RefactoringStatus.ERROR);
      } else {
        newFile.setASTTree(new ASTTree(0)); // FIXME could be not needed! should be tested
        getTarget().setSource(newFile);
//System.err.println("setSource: " + newFile);
      }
    } catch (IOException e) {
      status.addEntry(failedCreateMessage,
          CollectionUtil.singletonArrayList(e), RefactoringStatus.FATAL);
    }

    return status;
  }

  /**
   * Tries to create a random file with the name: "name"+[0-9]+".java"
   * @return new Source File
   * @throws IOException
   */
  private Source createCrazyFile() throws IOException {
    Source newFile = null;
    Source targetDir = targetDirectory.getSource();
    String filename = name + ".java";

    IUndoableTransaction transaction = RitUndoManager.getCurrentTransaction();
    IUndoableEdit undo = null;

    if (transaction != null) {
      SourceInfo sourceInfo = new SourceInfo(targetDir, filename);
      undo = transaction.createCreateFileUndo(sourceInfo);
    }

    newFile = targetDir.createNewFile(filename);

    if (transaction != null && newFile != null) {
      transaction.addEdit(undo);
    }

    if (newFile == null) {
      for (int i = 0; i < 10; i++) {
        String crazyFileName = name + i + ".java";

        if (transaction != null) {
          SourceInfo sourceInfo = new SourceInfo(targetDir, filename);
          undo = transaction.createCreateFileUndo(sourceInfo);
        }

        newFile = targetDir.createNewFile(crazyFileName);

        if (transaction != null && newFile != null) {
          transaction.addEdit(undo);
        }

        if (newFile != null) {
          break;
        }
      }
    }
    return newFile;
  }

  /**
   * Checks, wheather flag is set or not
   * @param flag
   * @return boolean. Is flag set or not
   */
  private boolean isFlagSet(int flag) {
    return ((flags & flag) == flag);
  }
}
