/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings;



import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.CFlowContext;
import net.sf.refactorit.common.util.ProgressListener;
import net.sf.refactorit.query.ProgressMonitor;
import net.sf.refactorit.refactorings.apisnapshot.SnapshotBuilder;
import net.sf.refactorit.test.Utils;

import java.util.Calendar;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class SnapshotBuilderTest extends TestCase {
  String log;

  private String expectedLog =
      SnapshotBuilder.PROJECT_PARSE_PROGRESS.getPercentage(0, 2) + " " +
      SnapshotBuilder.PROJECT_PARSE_PROGRESS.getPercentage(1, 2) + " " +
      SnapshotBuilder.SNAPSHOT_BUILD_PROGRESS.getPercentage(0, 2) + " " +
      SnapshotBuilder.SNAPSHOT_BUILD_PROGRESS.getPercentage(1, 2) + " ";

  public SnapshotBuilderTest(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(SnapshotBuilderTest.class);
  }

  public void setUp() throws Exception {
    CFlowContext.add(ProgressListener.class.getName(), new ProgressListener() {
      public void progressHappened(float percentage) {
        log += percentage + " ";
      }

      public void showMessage(String message) {}
    });

    log = "";
  }

  public void tearDown() {
    CFlowContext.remove(ProgressListener.class.getName());
  }

  /** No cacheing, because that could prevent some first-time-loading problems */
  private static Project getSampleProject() throws Exception {
    return Utils.createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
        "package a;\n\n" +
        "public class X {}",
        "X.java", "a"
        ),
        new Utils.TempCompilationUnit(
        "package a.subpackage;\n\n" +
        "public class Y {}",
        "Y.java", "a.subpackage"
        )
    });
  }

  public void testOnePackage() throws Exception {
    Project p = getSampleProject();

    new SnapshotBuilder().createSnapshot(
        p.getPackageForName("a"), "", Calendar.getInstance(), p);

    assertEquals(expectedLog, log);
  }

  public void testSubpackages() throws Exception {
    Project p = getSampleProject();

    new SnapshotBuilder().createSnapshot(
        p.getPackageForName("a"), "", Calendar.getInstance(), p);

    assertEquals(expectedLog, log);
  }

  public void testWholeProject() throws Exception {
    Project p = getSampleProject();

    new SnapshotBuilder().createSnapshot(p, "", Calendar.getInstance(), p);

    assertEquals(expectedLog, log);
  }

  public void testMultipleTargets() throws Exception {
    Project p = Utils.createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
        "package a;\n\n" +
        "public class X {}",
        "X.java", "a"
        ),
        new Utils.TempCompilationUnit(
        "package b;\n\n" +
        "public class Y {}",
        "Y.java", "b"
        )
    });

    new SnapshotBuilder().createMultiTargetSnapshot(new Object[] {
        p.getPackageForName("a"), p.getPackageForName("b")
    }
        , "", Calendar.getInstance(), p);

    assertEquals(expectedLog, log);
  }

  public void testProgressMonitorProgress() {
    assertEquals("50.0..75.0",
        new ProgressMonitor.Progress(50, 100).subdivision(0, 2).toString());
    assertEquals("75.0..100.0",
        new ProgressMonitor.Progress(50, 100).subdivision(1, 2).toString());

    assertEquals("50.0..100.0",
        new ProgressMonitor.Progress(50, 100).subdivision(0, 1).toString());
  }

  public static void assertEquals(String expected, String got) {
    assertEquals("need <" + expected + ">, got <" + got + ">", expected, got);
  }
}
