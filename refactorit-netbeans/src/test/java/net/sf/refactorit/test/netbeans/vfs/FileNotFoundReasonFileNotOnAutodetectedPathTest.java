/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.netbeans.vfs;

import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.netbeans.common.FileNotFoundReason;
import net.sf.refactorit.netbeans.common.RefactorItActions;
import net.sf.refactorit.netbeans.common.vfs.NBSource;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourcePath;


/**
 * @author risto
 */
public class FileNotFoundReasonFileNotOnAutodetectedPathTest extends NbTestCase {
  public void testNbProjectFolder() {
    if( RefactorItActions.isNetBeansThree()) {
      return;
    }

    Source root = IDEController.getInstance().getActiveProject().getPaths().getSourcePath().getRootSources()[0];
    NBSource nbProject = (NBSource) root.getParent().getChild("nbproject");
    assertNotNull(nbProject);
    
    assertEquals(FileNotFoundReason.ELEMENT_NOT_ON_AUTODETECTED_PATH, 
        FileNotFoundReason.getFor(nbProject.getFileObject()));
  }
}
