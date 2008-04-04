/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings;

import net.sf.refactorit.common.util.graph.DigraphTopologicalSorterTest;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.query.dependency.DependenciesIndexer;
import net.sf.refactorit.query.usage.filters.SearchFilter;
import net.sf.refactorit.refactorings.apisnapshot.SnapshotDiff;
import net.sf.refactorit.refactorings.undo.UndoManagerTest;
import net.sf.refactorit.test.refactorings.inlinemethod.InlineMethodTest;

import junit.framework.Test;
import junit.framework.TestSuite;


public class AllTests {
  /** Hidden constructor. */
  private AllTests() {}

  public static Test suite() throws Exception {
    final TestSuite suite = new TestSuite("Refactorings");

    suite.addTest(WhereUsedTest.suite());
    suite.addTest(SearchFilter.Tests.suite());
    if (!IDEController.runningNetBeans()) {
      suite.addTest(NotUsedTest.suite());
    }
    if (!IDEController.runningNetBeans()) {
      suite.addTest(FixmeScannerTest.suite());
    }
    if (!IDEController.runningNetBeans()) {
      suite.addTest(FixmeScannerMultiplePackagesTest.suite());
    }
    if (!IDEController.runningNetBeans()) {
      suite.addTest(FixmeTimestampTest.suite());
    }
    if (!IDEController.runningNetBeans()) {
      suite.addTest(net.sf.refactorit.test.metrics.AllTests.suite());
    }
    suite.addTest(DependenciesIndexer.TestDriver.suite());
    suite.addTest(MoveTypeTest.suite());
    suite.addTest(ExtractMethodTest.suite());
    suite.addTest(SnapshotDiff.SelfTest.suite());
    if (!IDEController.runningNetBeans()) {
      suite.addTest(ExtractSuperTest.suite());
    }
    suite.addTest(net.sf.refactorit.test.refactorings.rename.AllTests.suite());
    suite.addTest(net.sf.refactorit.test.refactorings.javadoc.AllTests.suite());
    suite.addTest(EncapsulateFieldTest.suite());
    if (!IDEController.runningNetBeans()) {
      suite.addTest(net.sf.refactorit.test.refactorings.structure.AllTests.suite());
    }
    suite.addTest(MinimizeAccessRightsTest.suite());
    suite.addTest(MoveMemberTest.suite());
    suite.addTest(UndoManagerTest.suite());
    suite.addTest(AddDelegatesTest.suite());
    suite.addTest(OverrideMethodsTest.suite());
    suite.addTest(ChangeMethodSignatureTest.suite());
    suite.addTestSuite(DigraphTopologicalSorterTest.class);
    suite.addTest(net.sf.refactorit.test.refactorings.usesupertype.AllTests.suite());
    suite.addTest(SnapshotBuilderTest.suite());
    suite.addTest(EjbUtilTest.suite());
    suite.addTest(FactoryMethodTest.suite());
    suite.addTest(CleanImportsTest.suite());
    suite.addTest(CreateMissingMethodTest.suite());
    suite.addTest(CallTreeTest.suite());
    suite.addTest(net.sf.refactorit.test.refactorings.inlinevariable.AllTests.
        suite());
    suite.addTest(net.sf.refactorit.test.refactorings.promotetemptofield.AllTests.
        suite());
    suite.addTest(net.sf.refactorit.test.refactorings.introducetemp.AllTests.
        suite());
    suite.addTest(net.sf.refactorit.test.refactorings.nonjava.AllTests.suite());
    suite.addTest(InlineMethodTest.suite());
    suite.addTest(WhereCaughtTest.suite());
    suite.addTest(CleanRebuildDialogTest.suite());
    suite.addTest(ImportUtilsTest.suite());
    return suite;
  }
}
