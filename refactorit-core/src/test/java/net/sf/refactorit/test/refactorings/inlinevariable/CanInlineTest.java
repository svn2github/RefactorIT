/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings.inlinevariable;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.commonIDE.MockIDEController;
import net.sf.refactorit.test.refactorings.RefactoringTestCase;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.NullDialogManager;
import net.sf.refactorit.utils.RefactorItConstants;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Most were imported from Eclipse
 * The tests that I created myself are marked with special comments.
 *
 * @author  RISTO A
 */
public class CanInlineTest extends RefactoringTestCase {
  private static String fakeTestName = null;

  public CanInlineTest(String name) {
    super(name);
  }

  public String getTemplate() {
    return "InlineTemp/canInline/A_<test_name>_<in_out>.java";
  }

  public static Test suite() {
    return new TestSuite(CanInlineTest.class);
  }

  public void assertWorks(String var) throws Exception {
    Project inProject = getMutableProject(getInitialProject());
    RwRefactoringTestUtils.assertRefactoring(
        AllTests.createRefactoring(var, inProject), getExpectedProject(),
        inProject);
  }

  public String getName() {
    if (fakeTestName != null) {
      return fakeTestName;
    } else {
      return super.getName();
    }
  }

  // Tests

  public void test0() throws Exception {
    assertWorks("x");
  }

  public void test1() throws Exception {
    assertWorks("x");
  }

  public void test2() throws Exception {
    assertWorks("x");
  }

  public void test3() throws Exception {
    assertWorks("x");
  }

  public void test3_division() throws Exception { // created myself
    assertWorks("x");
  }

  public void test3_parentheses() throws Exception { // created myself
    assertWorks("x");
  }

  public void test4() throws Exception {
    assertWorks("x");
  }

  public void test5() throws Exception {
    assertWorks("x");
  }

  public void test6() throws Exception {
    assertWorks("i");
  }

  public void test7() throws Exception {
    assertWorks("i");
  }

  public void test8() throws Exception {
    assertWorks("i");
  }

  public void test9() throws Exception {
    assertWorks("i");
  }

  public void test10() throws Exception {
    assertWorks("test");
  }

  public void test11() throws Exception {
    assertWorks("test");
  }

  public void test12() throws Exception {
    assertWorks("test");
  }

  public void test13() throws Exception {
    assertWorks("j");
  }

  public void test14() throws Exception {
    assertWorks("i");
  }

  public void test14_j() throws Exception { // added myself
    assertWorks("j");
  }

  public void test14_j_middle() throws Exception { // added myself
    assertWorks("j");
  }

  public void test14_complexArray() throws Exception { // added myself
    assertWorks("x");
  }

  public void test14_complexArray2() throws Exception { // added myself
    assertWorks("y");
  }

  // test15 was invalid

  public void test16() throws Exception {
    assertWorks("integer");
  }

  public void test16_usertypecasts() throws Exception { // added myself
    assertWorks("object");
  }

  public void test17() throws Exception {
    assertWorks("var");
  }

  public void test18() throws Exception {
    assertWorks("var");
  }

  public void test19() throws Exception {
    assertWorks("var");
  }

  public void test20() throws Exception {
    assertWorks("d");
  }

  public void test21() throws Exception {
    assertWorks("temp1");
  }

  public void test22() throws Exception {
    assertWorks("temp");
  }

  public void test22_simpletemp() throws Exception {
    assertWorks("temp");
  }

  public void test23() throws Exception {
    assertWorks("xxxx");
  }

  public void test24() throws Exception {
    assertWorks("value");
  }

  public void test25() throws Exception {
    assertWorks("value");
  }

  public void test25_valuabletempcomment() throws Exception {
    assertWorks("value");
  }

  // added the following tests myself:

  public void testListLength() throws Exception {
    assertWorks("length");
  }

  public void testFor() throws Exception {
    assertWorks("i");
  }

  public void testArray() throws Exception {
    assertWorks("x");
  }

  public void testArrayComplex() throws Exception {
    assertWorks("x");
  }

  public void testFieldSimple() throws Exception {
    assertWorks("ONE");
  }

  public void testFieldComplex() throws Exception {
    assertWorks("ONE_COMPLEX");
  }

  public void testFieldInner() throws Exception {
    assertWorks("ONE");
  }

  public void testSimpleMethodCall() throws Exception {
    assertWorks("x");
  }

  public void testSimpleMethodCall_complexInnards() throws Exception {
    assertWorks("o");
  }

  public void testAdditionToMethodCall() throws Exception {
    assertWorks("x");
  }

  public void testNew() throws Exception {
    assertWorks("o");
  }

  public void testComplexTypes() throws Exception {
    assertWorks("instance");
  }

  public void testMultilineDeclaration() throws Exception {
    assertWorks("three");
  }

  public void testMultilineDeclaration_2() throws Exception {
    assertWorks("three");
  }

  public void testManySpaces() throws Exception {
    assertWorks("x");
  }

  public void testArrayAccess() throws Exception {
    assertWorks("x");
  }

  public void testInvocationOnTypecast() throws Exception {
    assertWorks("firstChar");
  }

  /**
   * There is no resolving of names at all in current inline variable implementation,
   * just a simple text replace. This testacse is just one example of failures that are caused by that.
   * Also, if we had full resolve support, we could also inline fields that are used from multiple files.
   */
  public void testResolveNames() throws Exception {
    if (RefactorItConstants.runNotImplementedTests) {
      assertWorks("x");
    }
  }

  /** NOT mandatory, actually -- it's OK for extra parentheses to be removed as long as they are
   * removed in pairs */
  public void testManyParentheses() throws Exception {
    if (RefactorItConstants.runNotImplementedTests) {
      assertWorks("x");
    }
  }

  /** Bug 2174 */
  public void testArrayReturnedFromMethod() throws Exception {
    assertWorks("x");
  }

  public void testLazyInitialization() throws Exception {
    assertWorks("x");
  }

  public void testLazyFieldInitialization() throws Exception {
    assertWorks("x");
  }

  public static void main(String args[]) throws Exception {
    System.err.println("sleeping 5 sec -- attach debugger now");
    Thread.sleep(5000);

    DialogManager.setInstance(new NullDialogManager());
    IDEController.setInstance(new MockIDEController());

    fakeTestName = "test0";
    try {
      new CanInlineTest("").test0();
    } finally {
      fakeTestName = null;
    }
  }
}
