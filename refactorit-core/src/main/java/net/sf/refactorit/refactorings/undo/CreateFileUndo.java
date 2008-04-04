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
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourcePath;

import org.apache.log4j.Logger;

import java.io.IOException;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Aqris Software AS</p>
 * @author Tonis Vaga
 * @version 1.0
 */

public class CreateFileUndo extends SingleUndoableEdit {
  SourceInfo fileInfo;
  private static final Logger LOG = AppRegistry.getLogger(CreateFileUndo.class);
  private boolean directory;

  CreateFileUndo(SourceInfo info, UndoableTransaction trans) {
    super(trans);
    fileInfo = info;
    directory = info.isDirectory();

    if ( RitUndoManager.debug ) {
      LOG.debug("created createNewFileUndo for "+info.getRelativePath());
    }
  }

  public void undo() throws javax.swing.undo.CannotUndoException {
    super.undo();
    Source dest;
    dest = UndoUtil.findSource(getTransaction().getSourcePath(), fileInfo);

    if (dest == null) {
      LOG.debug(
          "createFileUndo(): parent not found, file already deleted??");
      return;
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
      RuntimePlatform.console.println("deleting " + dest.getName() + " failed");
    } else {

    }
  }

  public boolean isSignificant() {
    return true;
  }

  public void redo() throws javax.swing.undo.CannotRedoException {
    super.redo();
    Source parent;
    SourcePath sourcePath = getTransaction().getSourcePath();

    parent = UndoUtil.findParent(sourcePath, fileInfo, false);

    String fileName = FileUtil.extractPathElement(fileInfo.getRelativePath(),
        fileInfo.getSeparatorChar()).file;

    if (parent == null) {
      LOG.debug("Undo info: parent == null, will not create file " +
          fileName);
      return;
    }

    try {

      //should remove, when be moved to Editors
      IUndoableTransaction trans = RitUndoManager.getCurrentTransaction();
      IUndoableEdit undo = null;

      if (trans != null) {
        undo = trans.createCreateFileUndo(new SourceInfo(parent, fileName));
      }

      Source res;

      if(directory) {
        res = parent.mkdir(fileName);
      } else {
        res = parent.createNewFile(fileName);
      }


      if (trans != null && res !=null) {
        trans.addEdit(undo);
      }


    } catch (IOException ex) {
      net.sf.refactorit.common.util.AppRegistry.getExceptionLogger().error(ex, this);
    }
  }

}
