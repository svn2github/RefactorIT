/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.loader.jdk5;

import org.apache.log4j.Category;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author Anton Safonov
 */
public class AllTests {
  private static final Category cat
      = Category.getInstance(AllTests.class.getName());

  /** Hidden constructor. */
  private AllTests() {}

  public static Test suite() {
    cat.debug("Creating JDK 5.0 compiler test suite");
    final TestSuite suite = new TestSuite("Jdk50 compiler tests");

    suite.addTest(AutoLoadingTest.suite());
    suite.addTest(PushDownGenericsTest.suite());
    suite.addTest(AssertionTest.suite());
    suite.addTest(AnnotationTest.suite());
    suite.addTest(Compliance_1_5.suite());
    suite.addTest(ForeachStatementTest.suite());
    suite.addTest(GenericTypeSignatureTest.suite());
    suite.addTest(GenericTypeTest.suite());
    suite.addTest(GenericLoadFromBinaryTest.suite());
    suite.addTest(AutoBoxingTest.suite());
    suite.addTest(EnumTest.suite());
    suite.addTest(VarargsTest.suite());
    suite.addTest(GenericsInReturnTypeTest.suite());

// strange suite
//    suite.addTest(JavadocTest_1_5.suite());

    cat.debug("Jdk50 compiler test suite contains " + suite.countTestCases()
        + " tests");
    return suite;
  }
}
