/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings.promotetemptofield;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/** @author  RISTO A */
public class AllTests extends TestCase {

  public static Test suite() {
    TestSuite result = new TestSuite();
    result.addTest(VariableModifiersTest.suite());
    result.addTest(BracketFinderTest.suite());
    result.addTest(RenameLocalTest.suite());
    result.addTest(UserInputTest.suite());
    result.addTest(LocalClassUsageTest.suite());
    result.addTest(AllowedFieldNamesTest.suite());
    result.addTest(CanPromoteTest.suite());
    result.addTest(CannotPromoteTest.suite());
    result.addTest(AllowedModifiersTest.suite());
    return result;
  }
}
