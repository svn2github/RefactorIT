/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.loader;

import junit.framework.Test;
import junit.framework.TestSuite;


public class AllTests {
  /** Hidden constructor. */
  private AllTests() {}

  public static Test suite() throws Exception {
    final TestSuite suite = new TestSuite("Source loading");
    suite.addTest(CommentTest.suite());
    suite.addTest(SourceMethodBodyLoaderTest.suite());
    suite.addTest(ProjectTest.suite());
    suite.addTest(ProjectLoadTest.suite());
    suite.addTest(JacksTest.suite());
    suite.addTest(ParsingErrorsTest.suite());
    suite.addTest(LocalTypeTests.suite());
    suite.addTest(RebuildTest.suite());
    suite.addTest(RebuildTestWithCustomProjects.suite());
    suite.addTest(CacheTest.suite());
    suite.addTest(RebuildArrayTypesTest.suite());
    suite.addTest(net.sf.refactorit.test.loader.jdk5.AllTests.suite());
    return suite;
  }
}
