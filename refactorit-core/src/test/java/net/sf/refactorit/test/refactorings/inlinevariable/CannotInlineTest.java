/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings.inlinevariable;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.statements.BinLocalVariableDeclaration;
import net.sf.refactorit.classmodel.statements.BinStatement;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.inlinevariable.InlineVariable;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.test.refactorings.NullContext;
import net.sf.refactorit.test.refactorings.RefactoringTestCase;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.utils.RefactorItConstants;

import org.apache.log4j.Category;

import junit.framework.Test;
import junit.framework.TestSuite;


/** @author  RISTO A */
public class CannotInlineTest extends RefactoringTestCase {

  /** Logger instance. */
  private static final Category cat =
      Category.getInstance(CannotInlineTest.class.getName());

  public CannotInlineTest(String name) {
    super(name);
  }

  public String getTemplate() {
    return "InlineTemp/cannotInline/A_<test_name>.java";
  }

  public static Test suite() {
    return new TestSuite(CannotInlineTest.class);
  }

  public void assertFails(String name) throws Exception {
    Project inProject = getInitialProject();
    inProject.getProjectLoader().build();
    assertTrue(AllTests.createRefactoring(name, inProject).checkPreconditions()
        .isErrorOrFatal());
  }

  // Tests

  public void testFail3() throws Exception {
    if (RefactorItConstants.runNotImplementedTests) {
      assertFails("i"); // i not initialized, also, it is assigned to (but it's not used after that assignment)
    }
  }

  public void testFail4() throws Exception {
    assertFails("i"); // assigned to more than once
  }

  public void testFail5() throws Exception {
    assertFails("i"); // assigned to more than once
  }

  public void testFail6() throws Exception {
    assertFails("i"); // assigned to more than once
  }

  public void testFail7() throws Exception {
    assertFails("i"); // should not be possible to inline method parameters (unless they are final)
  }

  public void testFail8() throws Exception {
    assertFails("e"); // should not be possible to inline "catch" parameters
  }

  /*public void testFail9() throws Exception{
   assertFails("temp"); // Eclipse does not support non-static fields; we do, at the moment, because it seems easy to implement (?)
    }*/

  /*public void testFail11() throws Exception{
     assertFailure("file"); // Tests refusal to inline variables with null values. No reason to support this?
    }*/

  // 12 skipped, useless -- dealt with compile errors

  public void testFail13() throws Exception {
    assertFails("i"); // for loop variable -- not final, IOW
  }

  public void testFail14() throws Exception {
    assertFails("arr"); // cannot inline an array if is written to
  }

  // added the following tests myself:

  public void testArrayConst() throws Exception {
    assertFails("x");
  }

  public void testFieldAssignment() throws Exception {
    assertFails("ONE");
  }

  public void testQualifiedFieldUsage() throws Exception {
    assertFails("ONE");
  }

  public void testFieldAccessedInMultipleFiles() throws Exception {
    Project before = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
        "package a;\n\n" +
        "public class X { public int timestamp = System.currentTimeMillis(); }",
        "X.java", "a"),
        new Utils.TempCompilationUnit(
        "package b;\n\n" +
        "public class User { private void m() { System.out.println(a.X.timestamp); } }",
        "User.java", "b")
    });

    BinCIType x = before.getTypeRefForName("a.X").getBinCIType();
    InlineVariable inliner = new InlineVariable(new NullContext(before),
        x.getDeclaredField("timestamp"));

    assertTrue(inliner.checkPreconditions().isErrorOrFatal());
  }

  /** Bug 2175 */
  public void testExceptions() throws Exception {
    cat.info("Testing bug #2175");
    final Project project = Utils.createTestRbProject(Utils.getTestProjects().
        getProject("bug #2175"));
    project.getProjectLoader().build();
    assertFalse("Project ok", (project.getProjectLoader().getErrorCollector()).hasUserFriendlyErrors());
    final BinTypeRef testClassRef = project.getTypeRefForName(
        "TestException");

    assertNotNull("TestClassRef is loaded", testClassRef);
    BinCIType testClass = testClassRef.getBinCIType();
    assertNotNull("TestClass is loaded", testClass);
    BinMethod fMethod = testClass.getDeclaredMethod("f", new BinTypeRef[] {});
    assertNotNull("fMethod is loaded", fMethod);
    BinStatement[] statements = fMethod.getBody().getStatements();
    assertNotNull("statements are loaded", statements);
    BinLocalVariableDeclaration declaration = (BinLocalVariableDeclaration)
        statements[0];
    BinVariable[] variables = declaration.getVariables();
    BinVariable testField = variables[0];

    RefactorItContext context = new NullContext(project);
    assertNotNull("context is not empty", context);
    InlineVariable inlineVariable = new InlineVariable(context, testField);
    RefactoringStatus status = inlineVariable.checkPreconditions();
    assertTrue("RefactorIT status is WARNING or INFO", status.isInfoOrWarning());
    cat.info("SUCCESS!");
  }
}
