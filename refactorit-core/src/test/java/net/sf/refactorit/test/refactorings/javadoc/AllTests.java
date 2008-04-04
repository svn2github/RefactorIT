/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings.javadoc;

import junit.framework.Test;
import junit.framework.TestSuite;


public class AllTests {

  /** Hidden constructor. */
  private AllTests() {}

  public static Test suite() throws Exception {
    final TestSuite suite = new TestSuite("Javadoc");

    suite.addTest(RenameJavadocParameterTest.suite());
    suite.addTest(RenameJavadocFieldTest.suite());
    suite.addTest(RenameJavadocMethodTest.suite());
    suite.addTest(RenameJavadocTypeTest.suite());
    suite.addTest(RenameJavadocPackageTest.suite());

    suite.addTest(JavadocTest.suite());

    return suite;
  }
}
