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
import net.sf.refactorit.classmodel.expressions.BinNewExpression;
import net.sf.refactorit.query.AbstractIndexer;
import net.sf.refactorit.test.Utils;

import org.apache.log4j.Category;

import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test driver for {@link net.sf.refactorit.loader.MethodBodyLoader}.
 */
public final class SourceMethodBodyLoaderTest extends TestCase {
  /** Logger instance. */
  private static final Category cat =
      Category.getInstance(SourceMethodBodyLoaderTest.class.getName());

  public SourceMethodBodyLoaderTest(final String name) {
    super(name);
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite(SourceMethodBodyLoaderTest.class);
    suite.setName("SourceMethodBodyLoader tests");
    return suite;
  }

  /**
   * Test for bug #1222.
   */
  public void testBug1222() throws Exception {
    cat.info("Testing bug #1222.");

    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject(
        "bug1222"));
    project.getProjectLoader().build();

    final AbstractIndexer visitor = new AbstractIndexer();
    visitor.visit(project);

    assertTrue("Project has no critical errors",
        !(project.getProjectLoader().getErrorCollector()).hasCriticalUserErrors());

    assertTrue("Project has no user-friendly errors",
        !(project.getProjectLoader().getErrorCollector()).hasUserFriendlyErrors());

    cat.info("SUCCESS");
  }

  /**
   * Test for bug #1300.
   */
  public void testBug1249() throws Exception {
    cat.info("Testing bug #1249.");

    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject(
        "bug1249"));
    project.getProjectLoader().build();

    final AbstractIndexer visitor = new AbstractIndexer() {
      public void visit(BinNewExpression expr) {
        expr.getConstructor(); // here it will report that invocation is wrong
      }
    };
    visitor.visit(project);

    assertTrue("Project has no critical errors",
        !(project.getProjectLoader().getErrorCollector()).hasCriticalUserErrors());

    assertTrue("Project has user-friendly errors",
        (project.getProjectLoader().getErrorCollector()).hasUserFriendlyErrors());

    int count = 0;
    final Iterator it = (project.getProjectLoader().getErrorCollector()).getUserFriendlyErrors();
    while (it.hasNext()) {
      it.next();
      ++count;
    }

    assertEquals("Project user-friendly errors amount", 3, count);

    cat.info("SUCCESS");
  }

}
