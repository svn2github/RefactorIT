/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.edit;

import junit.framework.Test;
import junit.framework.TestSuite;


public class AllTests {
  /** Hidden constructor. */
  private AllTests() {}

  public static Test suite() throws Exception {
    final TestSuite suite = new TestSuite("source.edit package tests");
    suite.addTest(CompoundASTImpl.TestDriver.suite());
    suite.addTest(LineReader.TestDriver.suite());
    suite.addTest(MoveEditor.TestDriver.suite());
    suite.addTest(Line.TestDriver.suite());

    return suite;
  }
}
