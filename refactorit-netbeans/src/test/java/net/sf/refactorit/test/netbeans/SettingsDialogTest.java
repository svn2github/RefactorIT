/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.netbeans;

import java.io.IOException;

import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.netbeans.common.projectoptions.NBProjectOptions;
import net.sf.refactorit.netbeans.common.projectoptions.ui.SettingsDialog;
import net.sf.refactorit.netbeans.common.vfs.NBSource;
import net.sf.refactorit.test.netbeans.swingtests.MockDialogFactory;
import net.sf.refactorit.test.netbeans.vfs.NbTestCase;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.dialog.RitDialogFactory;
import net.sf.refactorit.ui.dialog.SwingDialog;
import net.sf.refactorit.vfs.Source;

/**
 *
 * @author risto
 */
public class SettingsDialogTest extends NbTestCase {
  RitDialogFactory old;
  MockDialogFactory mockDialogFactory;
  
  public void mySetUp() { 
    old = RitDialog.getDialogFactory();
    mockDialogFactory = new MockDialogFactory();
    RitDialog.setDialogFactory(mockDialogFactory);
  }
  
  public void myTearDown() {
    RitDialog.setDialogFactory(old);
  }
  
  public void testBugRIM483() throws Exception {
    Source root = getRoot();
    Source deletedRoot = root.mkdir("will_be_deleted");
    setSourcepathRoot((NBSource) deletedRoot);
    deletedRoot.delete();
    
    mockDialogFactory.setDialogShowListener(new net.sf.refactorit.test.netbeans.swingtests.DialogShowListener() {
      boolean firstTime = true;
      
      public void run(SwingDialog d) {
        if(firstTime) { // First time it's the SettingsDialog itself; next time it's the FS chooser dialog.
          firstTime = false;
          SettingsDialog.clickAddIgnoredSource();
        }
      }
    } );
    
    Object ideProject = IDEController.getInstance().getActiveProjectFromIDE();
    
    Object projectKey = IDEController.getInstance().getWorkspaceManager()
        .getIdeProjectIdentifier(ideProject);
    NBProjectOptions projectOptions = NBProjectOptions.getInstance(projectKey);
    
    
    SettingsDialog.showAndEditOptions(IDEController.getInstance().createProjectContext(),
        ideProject, projectOptions);
  }
}
