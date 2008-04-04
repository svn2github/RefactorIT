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
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.utils.FileUtil;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourcePath;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import java.io.IOException;


/**
 * @author Tonis Vaga
 */
public class DeleteFileUndo extends SingleUndoableEdit {
  boolean directory;
  SourceInfo srcInfo;
  IUndoableEdit contentUndo;

  DeleteFileUndo(Source src, UndoableTransaction transaction) {
    super(transaction);

    AppRegistry.getLogger(this.getClass()).debug(
        "[tonisdebug]: DeleteFileUndo created for " + src.getName());

    this.srcInfo = new SourceInfo(src);

//    Assert.must(src.exists() , "file " + src.getAbsolutePath() + " does not exist");

    directory = src.isDirectory();
    if (!directory) {
      contentUndo = new SourceContentUndo(src, transaction);
    }
  }

  public void redo() throws CannotRedoException {
    super.redo();

    Source dest = UndoUtil.findSource(getTransaction().getSourcePath(), srcInfo);

    if (dest == null) {
      AppRegistry.getLogger(this.getClass()).debug("DeleteFileUndo.redo():" + srcInfo + " does not exists");
    } else {
      if (!directory) {
        contentUndo = new SourceContentUndo(dest, this.getTransaction());
      }

      // remove it when, undo be moved to Editors
      IUndoableTransaction transaction = RitUndoManager.
          getCurrentTransaction();
      IUndoableEdit undo = null;
      if (transaction != null) {
        undo = transaction.createDeleteFileUndo(dest);
        transaction.addEdit(undo);
      }

      if (!dest.delete()) {
        AppRegistry.getLogger(this.getClass()).debug(
            "DeleteFileUndo.redo(): " + srcInfo + " deleting failed");
      }
    }
  }

  public void undo() throws CannotUndoException {
    super.undo();

    SourcePath sourcePath = getTransaction().getSourcePath();

    Source parent = UndoUtil.findParent(sourcePath, srcInfo, directory);
    if (parent == null) {
      return;
    }

    String targetName = "";

    if (directory) {
      int index = srcInfo.getRelativePath().lastIndexOf(srcInfo.getSeparatorChar());
      if (index < -1 || index == srcInfo.getRelativePath().length() - 1) {
        Assert.must(false, "wrong relativePath");
      } else {
        targetName = srcInfo.getRelativePath().substring(index + 1);
      }
    } else {
      targetName = FileUtil.extractPathElement(srcInfo.getRelativePath(),
          parent.getSeparatorChar()).file;
    }

    if (parent.getChild(targetName) == null) {
      try {
        if (directory) {
          parent.mkdir(targetName);
        } else {

          //should remove, when be moved on Editors
          IUndoableTransaction trans = RitUndoManager.getCurrentTransaction();
          IUndoableEdit undo = null;

          if (trans != null) {
            undo = trans.createCreateFileUndo(new SourceInfo(parent, targetName));
          }

          Source res = parent.createNewFile(targetName);

          if (trans != null && res !=null) {
            trans.addEdit(undo);
          }


        }
      } catch (IOException ex) {
        net.sf.refactorit.common.util.AppRegistry.getExceptionLogger().error(ex, this);
      }
    }

    if (!directory) {
      if (contentUndo.canUndo()) {
        contentUndo.undo();
      } else {
        AppRegistry.getLogger(this.getClass()).debug("CANNOT UNDO " + srcInfo.getRelativePath() + " content!!!");
      }
    }
  }
}
