/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings.rename;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author Anton Safonov
 */
public class AllTests {

  /** Hidden constructor. */
  private AllTests() {}

  public static Test suite() throws Exception {
    final TestSuite suite = new TestSuite("Rename");

    suite.addTest(RenameLabelTest.suite());
    suite.addTest(RenamePackageTest.suite());
    suite.addTest(RenameFieldTest.suite());
    suite.addTest(RenameMethodTest.suite());
    suite.addTest(RenameLocalTest.suite());
    suite.addTest(RenameTypeTest.suite());
    suite.addTest(RenameConflictsTest.suite());
    suite.addTest(RenameWithStaticImportsTest.suite());
    

    return suite;
  }
}
