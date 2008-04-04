/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.classmodel;

import junit.framework.Test;
import junit.framework.TestSuite;


public class AllTests {
  /** Hidden constructor. */
  private AllTests() {}

  public static Test suite() throws Exception {
    final TestSuite suite = new TestSuite("Classmodel tests");
    suite.addTest(BinCITypeTest.suite());
    suite.addTest(BinInitializerTest.suite());
    suite.addTest(TypeConversionRulesTest.suite());
    suite.addTest(BinMethodTest.suite());
    suite.addTest(MethodInvocationRulesTest.suite());
    suite.addTest(BinStringConcatenationExpressionTest.suite());
    suite.addTest(BinMethodInvocationExpressionTest.suite());
    suite.addTest(BinModifierTest.suite());
    suite.addTest(BinArithmeticalExpressionTest.suite());
    suite.addTest(CompilationUnitTest.suite());
    suite.addTest(DuplicateInterfacesTest.suite());
    return suite;
  }
}
