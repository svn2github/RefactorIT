/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.netbeans.vfs;



import java.beans.PropertyVetoException;
import java.io.IOException;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.ProgressListener;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.LoadingProperties;
import net.sf.refactorit.loader.ErrorCollector;
import net.sf.refactorit.netbeans.common.RefactorItActions;
import net.sf.refactorit.netbeans.common.projectoptions.NBProjectOptions;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.NullDialogManager;
import net.sf.refactorit.utils.RefactorItConstants;
import net.sf.refactorit.vfs.Paths;
import net.sf.refactorit.vfs.local.LocalClassPath;
import net.sf.refactorit.vfs.local.LocalJavadocPath;
import net.sf.refactorit.vfs.local.LocalSourcePath;

import junit.framework.TestCase;


/**
 *
 * @author  RISTO A
 */
public class ClassPathReleaseTest extends TestCase {
  private AutomaticTestfileDeleter mountListener = new AutomaticTestfileDeleter();

  public ClassPathReleaseTest(String s) {super(s);
  }

  public void setUp() throws IOException, PropertyVetoException {
    initForTestReRuns();

    mountListener.startListening();
  }

  public void tearDown() {
    mountListener.deleteCreatedFiles();
    mountListener.stopListening();
  }

  public void testSimpleClasspathUpdate() throws Exception {
    if( ! RefactorItConstants.runNotImplementedTests) {
      return;
    }

    // FIXME: Figure out how to "mount" JAR files under NB 4.0, then make this test work

    if(RefactorItActions.isNetBeansFour()) {
      return;
    }

    TestFileCreator.createFile(
        "Scu.java",
        "public class Scu extends LibraryClass {}");

    assertFalse(ensureActiveProject());

    TestFileCreator.mountJarFile(Utils.getSomeJarFile());
    assertTrue(ensureActiveProject());
  }

  public void testBug2081() throws Exception {
    if( ! RefactorItConstants.runNotImplementedTests) {
      return;
    }

    // FIXME: Figure out how to "mount" JAR files under NB 4.0, then make this test work
    if(RefactorItActions.isNetBeansFour()) {
      return;
    }

    TestFileCreator.createFile(
        "X.java",
        "public class X extends LibraryClass {}");
    assertFalse(ensureActiveProject());

    IDEController.getInstance().getActiveProject().getProjectLoader().build(null, false);

    TestFileCreator.mountJarFile(Utils.getSomeJarFile());
    assertTrue(ensureActiveProject());
  }
  
  // Util methods

  private static void initForTestReRuns() {
    IDEController.getInstance().getActiveProject().clean();
  }

  private static boolean ensureActiveProject() {
    IDEController.getInstance().ensureProject(new LoadingProperties(false));
    return ! (IDEController.getInstance().getActiveProject().getProjectLoader().getErrorCollector()).hasErrors();
  }

}
