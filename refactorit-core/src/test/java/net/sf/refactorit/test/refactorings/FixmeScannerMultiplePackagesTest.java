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
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.ui.module.fixmescanner.FixmeScannerTreeModel;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class FixmeScannerMultiplePackagesTest extends TestCase {
  private Project testProject;
  private List fixmeWords;
  private int fileCount;

  public FixmeScannerMultiplePackagesTest(String name) {
    super(name);
  }

  public static Test suite() throws Exception {
    final TestSuite suite = new TestSuite(FixmeScannerMultiplePackagesTest.class);
    suite.setName("FIXME Scanner Multiple Packages");
    return suite;
  }

  public void setUp() throws Exception {
    testProject = Utils.createTestRbProject(
        Utils.getTestProjects().getProject("FIXME Scanner Multiple Packages")
        );
    testProject.getProjectLoader().build();

    fileCount = 5;

    fixmeWords = new ArrayList(2);
    fixmeWords.add("FIXME");
    fixmeWords.add("TODO");
  }

  public void testFixmeCount() {
    FixmeScannerTreeModel model = new FixmeScannerTreeModel(testProject.
        getCompilationUnits(), fixmeWords, true, true, 0, 0);

    int fixmeCount = model.getChildCount(model.getRoot());

    assertEquals(12 * fileCount, fixmeCount);
  }

  public void testFindingInRange() {
    int methodStartLine = 17;
    int methodEndLine = 23;

    FixmeScannerTreeModel model = new FixmeScannerTreeModel(testProject.
        getCompilationUnits(), fixmeWords, true, false, methodStartLine,
        methodEndLine);

    int fixmeCount = model.getChildCount(model.getRoot());

    assertEquals(fileCount, fixmeCount);
  }
}
