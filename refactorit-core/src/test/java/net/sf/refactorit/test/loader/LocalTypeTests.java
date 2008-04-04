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
import net.sf.refactorit.query.AbstractIndexer;
import net.sf.refactorit.source.SourceParsingException;
import net.sf.refactorit.test.Utils;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class LocalTypeTests extends TestCase {

  public LocalTypeTests(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(LocalTypeTests.class);
  }

  public static Project createAndLoadProject(String projectFolderName) throws
      Exception {
    final Project result = Utils.createTestRbProject(projectFolderName);

    try {
      result.getProjectLoader().build();
    } catch (SourceParsingException e) {
      assertTrue("SPE should just inform user",
          e.justInformsThatUserFriendlyErrorsExist());
    }
    AbstractIndexer ai = new AbstractIndexer();
    ai.visit(result);
    return result;
  }

  private void mustFail(String project) throws Exception {
    Project p = createAndLoadProject(project);
    assertTrue("Project must have errors", (p.getProjectLoader().getErrorCollector()).hasErrors());
  }

  private void mustWork(String project) throws Exception {
    Project p = createAndLoadProject(project);
    assertTrue("Project must NOT have errors",
        !(p.getProjectLoader().getErrorCollector()).hasErrors() && p.getProjectLoader().isLoadingCompleted()
        );
  }

  public void testFail1() throws Exception {
    mustFail("ProjectLoader/LocalTypeTests/fail1");
  }

  public void testFail2() throws Exception {
    mustFail("ProjectLoader/LocalTypeTests/fail2");
  }

  public void testFail3() throws Exception {
    mustFail("ProjectLoader/LocalTypeTests/fail3");
  }

  public void testFail4() throws Exception {
    mustFail("ProjectLoader/LocalTypeTests/fail4");
  }

  public void testFail5() throws Exception {
    mustFail("ProjectLoader/LocalTypeTests/fail5");
  }

  public void testWork1() throws Exception {
    mustWork("ProjectLoader/LocalTypeTests/work1");
  }
}
