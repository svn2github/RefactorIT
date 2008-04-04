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
import net.sf.refactorit.ui.RuntimePlatform;
import net.sf.refactorit.utils.FileUtil;
import net.sf.refactorit.utils.PathElement;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourcePath;

import org.apache.log4j.Logger;

import javax.swing.undo.CannotRedoException;


/**
 *
 *
 * @author Tonis Vaga
 */
public class RenameFileUndo extends SingleUndoableEdit {
  SourceInfo from, to;
  private static final Logger log = AppRegistry.getLogger(RenameFileUndo.class);

  RenameFileUndo(Source from, Source destDir, String newName,
      UndoableTransaction transaction) {
    super(transaction);

    this.from = new SourceInfo(from);
    this.to = new SourceInfo(destDir, newName);
  }

  public void undo() {
    super.undo();

    rename(to, from);
  }

  private void rename(final SourceInfo from, final SourceInfo to) {
    SourcePath sourcePath = getTransaction().getSourcePath();
    Source src = UndoUtil.findSource(sourcePath, from);

    if (src == null) {
      final String msg = "RefactorIT cannot undo rename,  file " + from
          + " not found";
      RuntimePlatform.console.println(msg);
      return;
    }

    PathElement destElement
        = FileUtil.extractPathElement(to.getRelativePath(), to.getSeparatorChar());

    SourceInfo destParentInfo = new SourceInfo(
        to.getRootPath(), destElement.dir, to.getSeparatorChar());

    Source destParent = UndoUtil.findSource(sourcePath, destParentInfo);

    if (destParent == null) {
      final String msg = "RefactorIT cannot Undo/Redo rename, directory "
          + destParentInfo + " not found";
      RuntimePlatform.console.println(msg);
      return;
    }

    char separator = destParent.getSeparatorChar();

    Source destination = destParent.getChild(destElement.file);
    if (destination != null) {
      log.debug("RenameFileUndo found existing file "
          + destination.getDisplayPath() + " and deleted");

      // remove it when, undo be moved to Editors
      IUndoableTransaction transaction
          = RitUndoManager.getCurrentTransaction();
      if (transaction != null) {
        IUndoableEdit undo = transaction.createDeleteFileUndo(destination);
        transaction.addEdit(undo);
      }

      destination.delete();

//      if ( destination.length() == 0 ) {
//        destination.delete();
//        DebugInfo.trace("RenameFileUndo found empty file "+destination.getRelativePath()+" and deleted");
//      } else {
//
//        String newName=destination.getName()+".RITbackup";
//        if ( destination.getChild(newName) != null ) {
//          destination.getChild(newName).delete();
//        }
//        String oldPath=destination.getRelativePath();
//        destination.renameTo(destParent,newName);
//        RuntimePlatform.console.println("RefactorIT undo: moved existing file " +
//                                        oldPath + " to " +
//                                        destParent.getAbsolutePath() +
//                                        destParent.getSeparatorChar() +
//                                        newName);

//        if ( (destination=destParent.getChild(destElement.file)) != null ) {
//        	// die hard ;)
//        	DebugInfo.trace(destElement.file+" existed after renameTo, calling delete");
//        	destination.delete();
//        }
//      }
    }

    if (RitUndoManager.debug) {
      System.out.println("[tonisdebug]Rename file undo: renaming file " +
          src.getAbsolutePath() + " to " + destParent.getAbsolutePath() +
          separator + destElement.file);
    }

    Source parent = src.getParent();
    String myName = src.getName();
    Source result = renameToImpl(src, destParent, destElement.file);

    if (result == null || destParent.getChild(destElement.file) == null) {
      log.debug("Source.renameTo probably failed for "
      + src.getDisplayPath());
    }

    if (parent.getChild(myName) != null) {
      log.debug("file " + myName + " not deleted after rename");
    }
  }

  private static Source renameToImpl(Source src, Source destParent,
      String fileName) {
    //NB renameTo is problematic
//    if ( RuntimePlatform.runningNetBeans() ) {
//
//      try {
//        byte content[] = src.getContent();
//        Source dest = null;
//
//        if ( (dest = destParent.getChild(fileName)) == null) {
//          dest = destParent.createNewFile(fileName);
//          DebugInfo.trace("new file " + fileName + " created");
//        }
//        Assert.must(dest != null);
//        BufferedOutputStream output = new BufferedOutputStream(dest.
//            getOutputStream(), 64 * 1024);
//        output.write(content);
//        output.flush();
//        output.close();
//
//        src.delete();
//
//        return dest;
//      } catch (IOException e) {
//        e.printStackTrace(); //To change body of catch statement use Options | File Templates.
//        return null;
//      }
//    }
//    return null;

    log.debug("renaming " + src.getAbsolutePath() + " to " +
        destParent.getAbsolutePath() + destParent.getSeparatorChar() + fileName);

    // remove it when, undo be moved to Editors
    IUndoableTransaction undoTransaction = RitUndoManager.getCurrentTransaction();
    IUndoableEdit undo = null;
    if (undoTransaction != null) {
      undo = undoTransaction.createRenameFileUndo(src, destParent, fileName);
    }

    Source result = src.renameTo(destParent, fileName);

    if (undoTransaction != null && result != null) {
      undoTransaction.addEdit(undo);
    }


    return result;
  }

  public void redo() throws CannotRedoException {
    super.redo();
    rename(from, to);
  }
}
