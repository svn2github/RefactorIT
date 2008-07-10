/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test;

import net.sf.refactorit.classmodel.Project;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;


public class BinItemReferenceTestOnRefactorItSource extends BinItemReferencingTests {
  private static Project project;

  public BinItemReferenceTestOnRefactorItSource(String testName) {
    super(testName);
  }

  public static Test suite() {
    return new TestSuite(BinItemReferenceTestOnRefactorItSource.class);
  }

  public void setUp() throws Exception {
    super.setUp();

    if (project == null) { // Saves time when project is already loaded
      project = Utils.createTestRbProject(
          Utils.getTestProjects().getProject("RefactorIT"));

      project.getProjectLoader().build();
    }
  }

  protected Project getLoadedProject() {
    return project;
  }

  protected List getCompilationUnitsToTest() {
    return project.getCompilationUnits();
  }
}
