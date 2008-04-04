/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.netbeans.vfs;


import java.io.File;

import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.LoadingProperties;
import net.sf.refactorit.netbeans.common.projectoptions.NBProjectOptions;
import net.sf.refactorit.netbeans.common.projectoptions.PathItemReference;
import net.sf.refactorit.netbeans.common.projectoptions.PathUtil;
import net.sf.refactorit.test.LocalTempFileCreator;
import net.sf.refactorit.test.netbeans.swingtests.MockDialogFactory;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.NullDialogManager;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.dialog.RitDialogFactory;

/**
 * @author risto
 */
public class NBSourcePathTest extends NbTestCase {

  /** This was meant to test RIM-322, but probably did not test it. */
  public void testLogUsageWhileEmptyDirOnSourcepath() {
    File emptyFolder = LocalTempFileCreator.getInstance().
        createRootDirectory().getFileOrNull();
    Object ideProject = IDEController.getInstance().getActiveProjectFromIDE();
    Object projectKey = IDEController.getInstance().getWorkspaceManager()
        .getIdeProjectIdentifier(ideProject);
    NBProjectOptions.getInstance(projectKey).setUserSpecifiedSourcePath(new PathItemReference[] {
        new PathItemReference(emptyFolder)});
    NBProjectOptions.getInstance(projectKey).setUserSpecifiedClassPath(
        PathUtil.getInstance().getAutodetectedClasspath(ideProject));
    NBProjectOptions.getInstance(projectKey).setAutodetectPaths(false);

    IDEController.getInstance().ensureProject(new LoadingProperties(false));

    // Passed here: the StackOverflowException did not occour.
  }

  // @@@ Move to some other place: these 2 are not sourcepath tests
  public void testClasspathSanityCheck() {
    final RitDialogFactory old = RitDialog.getDialogFactory();
    try {
      MockDialogFactory mock = new MockDialogFactory();
      mock.setDialogShowListener(new MockDialogFactory.NullDialogShowListener());
      RitDialog.setDialogFactory(mock);
    
      disablePathsAutodetection();
      Object ideProject = IDEController.getInstance().getActiveProjectFromIDE();
      Object projectKey = IDEController.getInstance().getWorkspaceManager()
          .getIdeProjectIdentifier(ideProject);
      NBProjectOptions.getInstance(projectKey).setUserSpecifiedClassPath(
          new PathItemReference[] {});
      
      IDEController.getInstance().ensureProject();
      
      assertTrue(mock.getOptionDialogLog(), mock.getOptionDialogLog().indexOf(
          "Please fix your classpath") >= 0);
    } finally {
      RitDialog.setDialogFactory(old);
    }
  }
  
  public void testClasspathReleaseOnEnsureProject() {
    final RitDialogFactory old = RitDialog.getDialogFactory();
    try {
      MockDialogFactory mock = new MockDialogFactory();
      mock.setDialogShowListener(new MockDialogFactory.NullDialogShowListener());
      RitDialog.setDialogFactory(mock);
      
      disablePathsAutodetection();
      Object ideProject = IDEController.getInstance().getActiveProjectFromIDE();
      Object projectKey = IDEController.getInstance().getWorkspaceManager()
          .getIdeProjectIdentifier(ideProject);
      NBProjectOptions.getInstance(projectKey).setUserSpecifiedClassPath(
          new PathItemReference[] {});
      
      IDEController.getInstance().ensureProject();
      
      assertTrue(mock.getOptionDialogLog(), mock.getOptionDialogLog().indexOf(
          "Please fix your classpath") >= 0);
      
      // Let's test to see that it appears again on second attempt
      mock.clearOptionDialogLog();
      IDEController.getInstance().ensureProject();
      assertTrue(mock.getOptionDialogLog(), mock.getOptionDialogLog().indexOf(
          "Please fix your classpath") >= 0);
      
      // Now let's restore classpath and see if the message disappears as expected
      ideProject = IDEController.getInstance().getActiveProjectFromIDE();
      projectKey = IDEController.getInstance().getWorkspaceManager()
          .getIdeProjectIdentifier(ideProject);
      NBProjectOptions.getInstance(projectKey).setUserSpecifiedClassPath(
          PathUtil.getInstance().getAutodetectedClasspath(ideProject));
      mock.clearOptionDialogLog();
      IDEController.getInstance().ensureProject();
      assertEquals("", mock.getOptionDialogLog());
    } finally {
      RitDialog.setDialogFactory(old);
    }
  }

}
