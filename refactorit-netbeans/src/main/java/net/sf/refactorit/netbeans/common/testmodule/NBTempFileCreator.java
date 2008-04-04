/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.testmodule;



import java.io.IOException;

import net.sf.refactorit.common.exception.SystemException;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.exception.ErrorCodes;
import net.sf.refactorit.netbeans.common.vfs.NBSource;
import net.sf.refactorit.netbeans.common.vfs.NBSourcePath;
import net.sf.refactorit.refactorings.undo.IUndoableEdit;
import net.sf.refactorit.refactorings.undo.IUndoableTransaction;
import net.sf.refactorit.refactorings.undo.RitUndoManager;
import net.sf.refactorit.refactorings.undo.SourceInfo;
import net.sf.refactorit.test.TempFileCreator;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourcePath;



public class NBTempFileCreator extends TempFileCreator {
  public SourcePath createSourcePath(final Source root) throws SystemException {
    return new TempFileCreator.TestSourcePath(root);
  }

  public Source createRootDirectory() {
    Object ideProject = IDEController.getInstance().getActiveProjectFromIDE();
    Source mountRoot = new NBSourcePath(ideProject).getRootSources()[0];
    NBSource result = (NBSource) mountRoot.mkdir(TempFileCreator.TempNameGenerator.createDir());

    result.fakeRoot();
    return result;
  }

  public Source createRootFile() throws SystemException {
    NBSource result;
    try {
      String filename = tempFilePrefix + tempFileSuffix;
      Source rootDirectory = createRootDirectory();

      IUndoableTransaction trans = RitUndoManager.getCurrentTransaction();
      IUndoableEdit undo = null;

      if (trans != null) {
        undo = trans.createCreateFileUndo(
            new SourceInfo(rootDirectory, filename));
      }

      result = (NBSource) rootDirectory.createNewFile(filename);

      if (trans != null && result !=null) {
        trans.addEdit(undo);
      }

    } catch (IOException e) {
      AppRegistry.getExceptionLogger().error(e,this);
      throw new SystemException(ErrorCodes.INTERNAL_ERROR,e);
    }
    result.fakeRoot();
    return result;
  }
}
