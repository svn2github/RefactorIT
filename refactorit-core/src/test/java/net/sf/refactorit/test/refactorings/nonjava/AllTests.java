/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings.nonjava;

import junit.framework.Test;
import junit.framework.TestSuite;


public class AllTests {

  /** Hidden constructor. */
  private AllTests() {}

  public static Test suite() throws Exception {
    final TestSuite suite = new TestSuite("Non java files");

    suite.addTest(RenameClassTest.suite());
    suite.addTest(WhereUsedPackageTest.suite());
    suite.addTest(MoveClassTest.suite());

    return suite;
  }
}
