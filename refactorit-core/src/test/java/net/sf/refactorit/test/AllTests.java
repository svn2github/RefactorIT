/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.parser.FastJavaLexer;

import junit.framework.Test;
import junit.framework.TestSuite;


public class AllTests {
  /** @used Hidden constructor. */
  private AllTests() {}

  public static Test suite() throws Throwable {
    try {
      final TestSuite suite = new TestSuite("All RB tests");

      Project.getDefaultOptions().setJvmMode(FastJavaLexer.JVM_14); // default

      suite.addTest(net.sf.refactorit.test.audits.AllTests.suite());
      suite.addTest(net.sf.refactorit.test.classmodel.AllTests.suite());
      suite.addTest(net.sf.refactorit.test.loader.AllTests.suite());
      suite.addTest(net.sf.refactorit.test.wtk.AllTests.suite());
      suite.addTest(net.sf.refactorit.test.refactorings.AllTests.suite());
      suite.addTest(net.sf.refactorit.test.source.AllTests.suite());
      suite.addTest(LinePositionUtilTest.suite());
      suite.addTest(net.sf.refactorit.source.edit.AllTests.suite());
      suite.addTest(BinItemReferenceTest.suite());
      suite.addTest(RwRefactoringTestUtils.TestDriver.suite());
      suite.addTest(net.sf.refactorit.ui.PackageModel.TestDriver.suite());
      suite.addTest(net.sf.refactorit.jsp.TestDriver.suite());
      suite.addTest(net.sf.refactorit.test.FileUtilTest.suite());
      suite.addTest(net.sf.refactorit.test.commonIDE.AllTests.suite());
      suite.addTest(net.sf.refactorit.test.cli.AllTests.suite());
      suite.addTest(net.sf.refactorit.test.vfs.LocalSourceTest.suite());
      suite.addTest(net.sf.refactorit.test.vfs.SourceTest.suite());
      suite.addTest(net.sf.refactorit.test.transformations.AllTests.suite());
      suite.addTest(net.sf.refactorit.test.settings.AllTests.suite());
      suite.addTest(net.sf.refactorit.common.util.AllTests.suite());
      suite.addTest(net.sf.refactorit.test.reports.AllTests.suite());
      return suite;
    } catch (Throwable e) {
      // Here we catch ExceptionInInitializerError, for example.
      // (JUnit hides such exceptions, so we have to catch and show them ourselves.)

      e.printStackTrace(System.out);
      throw e;
    }
  }
}
