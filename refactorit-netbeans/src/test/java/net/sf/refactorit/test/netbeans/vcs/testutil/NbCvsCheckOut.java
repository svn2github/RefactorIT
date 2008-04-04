/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.netbeans.vcs.testutil;


import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.netbeans.common.RefactorItActions;
import net.sf.refactorit.netbeans.common.vcs.Vcs;
import net.sf.refactorit.netbeans.common.vfs.NBSource;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourcePath;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;


public class NbCvsCheckOut {
  private FileObject root;

  public NbCvsCheckOut() throws FileStateInvalidException {
    root = getCvsRootSource().getFileObject();
  }

  public void update() {
    Vcs.update(root);
  }

  public void commit() {
    Vcs.commit(root);
  }

  private NBSource getCvsRootSource() throws FileStateInvalidException {
    Source[] roots = IDEController.getInstance().getActiveProject().getPaths().getSourcePath().getRootSources();
    for (int i = 0; i < roots.length; i++) {
      NBSource rootSource = (NBSource) roots[i];
      if (isCvsRoot(rootSource)) {
        return (NBSource) roots[i];
      }
    }

    throw new RuntimeException("Could not find CVS root source");
  }

  private boolean isCvsRoot(NBSource rootSource) throws FileStateInvalidException {
    if(RefactorItActions.isNetBeansFour()) {
      // FIXME: Implement
      return true;
    } else {
      return rootSource.getFileObject().getFileSystem().getClass().getName().indexOf(
          "Vcs") >= 0;
    }
  }
}
