/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.loader;




import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.ProgressListener;
import net.sf.refactorit.loader.ASTTreeCache;
import net.sf.refactorit.loader.ProjectChangedListener;
import net.sf.refactorit.loader.RebuildLogic;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.NullDialogManager;
import net.sf.refactorit.vfs.Source;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests how loading projects works after interruption.
 *
 * @author Risto
 * @author Tonis
 */
public class RebuildTest extends TestCase {
  public static final String testName = "Project loading and canceling test";

  /** Project under test. */
  Project project;

  public static Test suite() {
    return new TestSuite(RebuildTest.class);
  }

  public void setUp() throws Exception {
    project = Utils.createTestRbProject(Utils.getTestProjects().getProject(
        "bookstore2"));
  }

  public void testInterruptLoad() throws Exception {
    //project.load();

    ProgressListener listener = new ProgressListener() {
      public void progressHappened(final float percent) {
        if (percent > 20) {
          Thread.currentThread().interrupt();
        }
      }

      public void showMessage(String msg) {}
    };

    project.getProjectLoader().build(listener, false);

    // We can't be sure that interrupted message was readed before loading was completed
    // especially for small project but for most cases should be.

    assertTrue(!project.getProjectLoader().isLoadingCompleted());
    project.getProjectLoader().build(null, false);
    assertTrue(project.getProjectLoader().isLoadingCompleted());

//    final Iterator errors = project.getUserFriendlyErrors();
//    if (errors.hasNext()) {
//      cat.error("Got following errors:");
//      int errorIndex = 0;
//      while (errors.hasNext()) {
//        final Object exception = errors.next();
//        cat.error("Error #" + (errorIndex + 1) + ": " + exception);
//        errorIndex++;
//      }
//
//      fail("Got " + errorIndex + " errors");
//    } else if (project.hasCriticalUserErrors()) {
//    }
  }

  public void testCleanRebuild() throws Exception {
    project.getProjectLoader().build();

    assertTrue(project.getProjectLoader().isLoadingCompleted());

    final List sources = project.getCompilationUnits();

    project.getProjectLoader().markProjectForCleanup();

    //assertEquals( sources.size(), rebuildLogic.getSourceListToRebuild().size()  ) ;
    ProjectChangedListener listener = new ProjectChangedListener() {
      public void rebuildStarted(Project p) {
        RebuildLogic rebuildLogic = project.getProjectLoader().getRebuildLogic();
        assertTrue(rebuildLogic.getSourceListToRebuild().size() == sources.size());
        assertASTCache(rebuildLogic.getSourceListToRebuild(), false);
      }

      public void rebuildPerformed(Project pr) {}
    };

    project.getProjectLoader().addProjectChangedListener(listener);
    project.getProjectLoader().build(null, false);

    project.getProjectLoader().removeProjectChangedListener(listener);

    assertTrue(project.getProjectLoader().isLoadingCompleted());

//     project.markProjectForCleanup();
//
//     //RebuildLogic rebuildLogic = project.getRebuildLogic();
//     rebuildLogic.analyzeChanges();
//
//     assertEquals( sources.size(), rebuildLogic.getSourceListToRebuild().size()  ) ;
  }

  public void testRebuild() throws Exception {
    project.getProjectLoader().build();
    assertTrue(project.getProjectLoader().isLoadingCompleted());
    final List sources = project.getCompilationUnits();

    project.getProjectLoader().markProjectForRebuild();

    ProjectChangedListener listener = new ProjectChangedListener() {
      public void rebuildStarted(Project p) {
        RebuildLogic rebuildLogic = project.getProjectLoader().getRebuildLogic();
        assertTrue(rebuildLogic.getSourceListToRebuild().size() == sources.size());
        assertASTCache(rebuildLogic.getSourceListToRebuild(), true);
      }

      public void rebuildPerformed(Project pr) {}
    };

    project.getProjectLoader().addProjectChangedListener(listener);
    project.getProjectLoader().build(null, false);

    project.getProjectLoader().removeProjectChangedListener(listener);

//    RebuildLogic rebuildLogic = project.getRebuildLogic();
//    rebuildLogic.analyzeChanges();
//    assertASTCache(rebuildLogic.getSourceListToRebuild(), true);
    /////   assertEquals( 0, rebuildLogic.getSourceListToRebuild().size()  ) ;
//    project.build(null);

    assertTrue(project.getProjectLoader().isLoadingCompleted());
  }

  void assertASTCache(Collection sources, boolean exist) {
    ASTTreeCache cache = project.getProjectLoader().getAstTreeCache();

    for (Iterator i = sources.iterator(); i.hasNext(); ) {
      Source item = (Source) i.next();
      assertEquals(exist, null != cache.checkCacheFor(item));
    }
  }

  public static void main(String args[]) throws Exception {
    System.err.println("sleeping 5 sec -- attach debugger now");
    Thread.sleep(5000);

    DialogManager.setInstance(new NullDialogManager());

//    new ProjectLoadRebuildTest().testRemovingJarFromClasspath();
  }
}
