/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings.rename;

import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.MethodInvocationRules;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.rename.RenameMethod;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.refactorings.NullContext;
import net.sf.refactorit.test.refactorings.RefactoringTestCase;
import net.sf.refactorit.utils.RefactorItConstants;

import junit.framework.Test;
import junit.framework.TestSuite;


public class RenamePrivateMethodTest extends RefactoringTestCase {
//  private static final String PROJECTS_PATH =
//    "RenameMethod/imported/RenamePrivateMethod/";

  public RenamePrivateMethodTest(String name) {
    super(name);
  }

  public String getTemplate() {
    return "RenameMethod/imported/RenamePrivateMethod/<test_name>/<in_out>";
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite(RenamePrivateMethodTest.class);
    suite.setName("Imported RenamePrivateMethodTests");
    return suite;
  }

  protected void tearDown() throws Exception {
  }

  private void renameMustFail(String methodName, String newMethodName,
      String[] signatures) throws Exception {
    final Project project =
        RwRefactoringTestUtils.createMutableProject(getInitialProject());
    project.getProjectLoader().build();

    BinTypeRef aRef =
        project.findTypeRefForName("p.A");

    BinTypeRef[] params = RenameMethodTest.convertSingature(project, signatures);
    BinMethod m = MethodInvocationRules.getMethodDeclaration(aRef.getBinCIType(),
        aRef, methodName, params);

    String debugString = RenameMethodTest.filesToString(project);

    assertNotNull(m);

    RenameMethod renamer
        = new RenameMethod(new NullContext(project), m);
    renamer.setNewName(newMethodName);

    RefactoringStatus status = renamer.checkPreconditions();
    status.merge(renamer.checkUserInput());

    status.merge(renamer.apply());

    if (status.isOk()) {
      fail("Should have failed" + debugString);
    }

    /*		IType classA= getType(createCUfromTestFile(getPackageP(), "A"), "A");
         RenameMethodRefactoring ref= RenameMethodRefactoring.createInstance(classA.getMethod(methodName, signatures));
         ref.setNewName(newMethodName);
         RefactoringStatus result= performRefactoring(ref);
         assertNotNull("precondition was supposed to fail", result);
     */
  }

  private void renameMustFail() throws Exception {
    renameMustFail("m", "k", new String[0]);
  }

  private void renameMustWork(String methodName, String newMethodName,
      String[] signatures, boolean updateReferences) throws Exception {
    final Project project =
        RwRefactoringTestUtils.createMutableProject(getInitialProject());
    project.getProjectLoader().build();

    BinTypeRef aRef =
        project.findTypeRefForName("p.A");

    BinTypeRef[] params = RenameMethodTest.convertSingature(project, signatures);
    BinMethod m = MethodInvocationRules.getMethodDeclaration(aRef.getBinCIType(),
        aRef, methodName, params);

    String debugString = RenameMethodTest.filesToString(project);

    assertNotNull(m);

    RenameMethod renamer
        = new RenameMethod(new NullContext(project), m);
    renamer.setNewName(newMethodName);

    RefactoringStatus status = renamer.checkPreconditions();
    status.merge(renamer.checkUserInput());

    status.merge(renamer.apply());

    if (!status.isOk()) {
      fail("Should have worked" + debugString);
    }
  }

  private void renameMustWork(String methodName, String newMethodName,
      String[] signatures) throws Exception {
    renameMustWork(methodName, newMethodName, signatures, true);
  }

  private void renameMustWork(boolean updateReferences) throws Exception {
    renameMustWork("m", "k", new String[0], updateReferences);
  }

  private void renameMustWork() throws Exception {
    renameMustWork(true);
  }

  /******* tests ******************/
  public void testFail0() throws Exception {
    renameMustFail();
  }

  public void testFail1() throws Exception {
    renameMustFail();
  }

  public void testFail2() throws Exception {
    if (RefactorItConstants.runNotImplementedTests) {
      renameMustFail();
    }
  }

  //testFail3 deleted

  //testFail4 deleted

  public void testFail5() throws Exception {
    if (RefactorItConstants.runNotImplementedTests) {
      renameMustFail();
    }
  }

  public void test0() throws Exception {
    renameMustWork();
  }

  public void test10() throws Exception {
    renameMustWork();
  }

  public void test11() throws Exception {
    renameMustWork();
  }

  public void test12() throws Exception {
    renameMustWork();
  }

  public void test13() throws Exception {
    renameMustWork();
  }

  public void test14() throws Exception {
    renameMustWork();
  }

  public void test15() throws Exception {
    renameMustWork("m", "k", new String[] {"I"});
  }

  public void test16() throws Exception {
    renameMustWork("m", "fred", new String[] {"I"});
  }

  public void test17() throws Exception {
    renameMustWork("m", "kk", new String[] {"I"});
  }

  public void test18() throws Exception {
    /*		ICompilationUnit cu= createCUfromTestFile(getPackageP(), "A");
        ICompilationUnit cuC= createCUfromTestFile(getPackageP(), "C");

        IType classB= getType(cu, "B");
        RenameMethodRefactoring ref= RenameMethodRefactoring.createInstance(classB.getMethod("m", new String[]{"I"}));
        ref.setNewName("kk");

        assertEquals("was supposed to pass", null, performRefactoring(ref));
        assertEquals("invalid renaming A", getFileContents(getOutputTestFileName("A")), cu.getSource());
        assertEquals("invalid renaming C", getFileContents(getOutputTestFileName("C")), cuC.getSource());
     */
  }

  public void test2() throws Exception {
    renameMustWork("m", "fred", new String[0]);
  }

  public void test20() throws Exception {
    renameMustWork("m", "fred", new String[] {"I"});
  }

  public void test23() throws Exception {
    renameMustWork("m", "k", new String[0]);
  }

  public void test24() throws Exception {
    renameMustWork("m", "k", new String[] {"QString;"});
  }

  public void test25() throws Exception {
    renameMustWork("m", "k", new String[] {"[QString;"});
  }

  public void test26() throws Exception {
    renameMustWork("m", "k", new String[0]);
  }

  public void test27() throws Exception {
    renameMustWork("m", "k", new String[0], false);
  }

  public void testAnon0() throws Exception {
    renameMustWork();
  }
}
