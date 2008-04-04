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
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.NullDialogManager;
import net.sf.refactorit.utils.RefactorItConstants;

import junit.framework.Test;
import junit.framework.TestSuite;



public class RenameVirtualMethodInClassTest extends RefactoringTestCase {
  public RenameVirtualMethodInClassTest(String name) {
    super(name);
  }

  public String getTemplate() {
    return
        "RenameMethod/imported/RenameVirtualMethodInClass/<test_name>/<in_out>";
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite(RenameVirtualMethodInClassTest.class);
    suite.setName("Imported RenameVirtualMethodInClassTests");
    return suite;
  }

  protected void tearDown() throws Exception {
    DialogManager.setInstance(new NullDialogManager());
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
         assertNotNull("precondition was supposed to fail", result);*/
  }

  private void renameMustFail() throws Exception {
    renameMustFail("m", "k", new String[0]);
  }

  private void doRename(String methodName, String newMethodName,
      String[] signatures, boolean shouldPass,
      boolean updateReferences) throws Exception {
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

    if (!status.isErrorOrFatal() && !shouldPass) {
      fail("Should have failed" + debugString);
    }

    if (status.isErrorOrFatal() && shouldPass) {
      fail("Should have worked" + debugString);
    }

    RwRefactoringTestUtils.assertSameSources(
        "Renamed " + m.getQualifiedName() + " -> " + newMethodName + ": "
        + status.getAllMessages(), getExpectedProject(), project);
  }

  private void doRename(String methodName, String newMethodName,
      String[] signatures, boolean shouldPass) throws Exception {
    doRename(methodName, newMethodName, signatures, shouldPass, true);
  }

  private void renameMustWork(String methodName, String newMethodName,
      String[] signatures) throws Exception {
    doRename(methodName, newMethodName, signatures, true);
  }

  private void renameMustWork(boolean updateReferences) throws Exception {
    doRename("m", "k", new String[0], true, updateReferences);
  }

  private void renameMustWork() throws Exception {
    renameMustWork(true);
  }

  /******************************************************************/
  public void testFail0() throws Exception {
    renameMustFail();
  }

  public void testFail1() throws Exception {
    renameMustFail("toString", "fred", new String[0]);
  }

  public void INVALIDtestFail2() throws Exception {
    renameMustFail();
  }

  public void INVALIDtestFail3() throws Exception {
    renameMustFail();
  }

  public void INVALIDtestFail4() throws Exception {
    renameMustFail();
  }

  public void INVALIDtestFail5() throws Exception {
    renameMustFail();
  }

  public void INVALIDtestFail6() throws Exception {
    renameMustFail();
  }

  public void testFail7() throws Exception {
    renameMustFail();
  }

  public void testFail8() throws Exception {
    renameMustFail();
  }

  public void testFail9() throws Exception {
//		helper1_0("m", "k", new String[]{Signature.SIG_INT});
  }

  public void testFail10() throws Exception {
    renameMustFail();
  }

  public void testFail11() throws Exception {
    renameMustFail();
  }

  public void testFail12() throws Exception {
    renameMustFail();
  }

  public void testFail13() throws Exception {
    if (RefactorItConstants.runNotImplementedTests) {
      renameMustFail();
    }
  }

  public void testFail14() throws Exception {
//		helper1_0("m", "k", new String[]{Signature.SIG_INT});
  }

  public void testFail15() throws Exception {
    renameMustFail();
  }

  public void testFail17() throws Exception {
    renameMustFail();
  }

  public void testFail18() throws Exception {
    renameMustFail();
  }

  public void testFail19() throws Exception {
    renameMustFail();
  }

  public void testFail20() throws Exception {
    renameMustFail();
  }

  public void testFail21() throws Exception {
    renameMustFail();
  }

  public void testFail22() throws Exception {
    renameMustFail();
  }

  public void testFail23() throws Exception {
    renameMustFail();
  }

  public void testFail24() throws Exception {
    renameMustFail();
  }

  public void testFail25() throws Exception {
    renameMustFail();
  }

  public void testFail26() throws Exception {
    renameMustFail();
  }

  public void testFail27() throws Exception {
    renameMustFail();
  }

  public void testFail28() throws Exception {
    renameMustFail();
  }

  public void INVALIDtestFail29() throws Exception {
    renameMustFail();
  }

  public void testFail30() throws Exception {
    renameMustFail();
  }

  public void testFail31() throws Exception {
    if (RefactorItConstants.runNotImplementedTests) {
      renameMustFail("m", "k", new String[] {"QString;"});
    }
  }

// seems to be wrong!
  /*	public void testFail32() throws Exception{
      renameMustFail("m", "k", new String[]{"QObject;"});
    }*/

  public void testFail33() throws Exception {
    renameMustFail("toString", "k", new String[0]);
  }

  public void testFail34() throws Exception {
    renameMustFail("m", "k", new String[] {"QString;"});
  }

  public void testFail35() throws Exception {
    if (RefactorItConstants.runNotImplementedTests) {
      renameMustFail();
    }
  }

  public void testFail36() throws Exception {
    renameMustFail();
  }

  public void testFail37() throws Exception {
    renameMustFail();
  }

  public void testFail38() throws Exception {
//		printTestDisabledMessage("must fix - nested type");
    //helper1();
  }

  public void test1() throws Exception {
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
//		helper2_0("m", "k", new String[]{Signature.SIG_INT});
  }

  public void test16() throws Exception {
    //helper2_0("m", "fred", new String[]{Signature.SIG_INT});
  }

  public void test17() throws Exception {
//		printTestDisabledMessage("overloading");
    //helper2_0("m", "kk", new String[]{Signature.SIG_INT});
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

  public void test19() throws Exception {
    renameMustWork("m", "fred", new String[0]);
  }

  public void test2() throws Exception {
    renameMustWork("m", "fred", new String[0]);
  }

  public void test20() throws Exception {
//		helper2_0("m", "fred", new String[]{Signature.SIG_INT});
  }

  public void test21() throws Exception {
//		helper2_0("m", "fred", new String[]{Signature.SIG_INT});
  }

  public void test22() throws Exception {
    renameMustWork();
  }

  //anonymous inner class
  public void test23() throws Exception {
    if (RefactorItConstants.runNotImplementedTests) {
      renameMustFail(); // ???
    }
  }

  public void test24() throws Exception {
    renameMustWork("m", "k", new String[] {"QString;"});
  }

  public void test25() throws Exception {
    //printTestDisabledMessage("waiting for 1GIIBC3: ITPJCORE:WINNT - search for method references - missing matches");
    renameMustWork();
  }

  public void test26() throws Exception {
    renameMustWork();
  }

  public void test27() throws Exception {
    renameMustWork();
  }

  public void test28() throws Exception {
    renameMustWork();
  }

  public void test29() throws Exception {
    renameMustWork();
  }

  public void test30() throws Exception {
    renameMustWork();
  }

  public void test31() throws Exception {
    renameMustWork();
  }

  public void NotSupportedtest32() throws Exception {
    renameMustWork(false);
  }

  public void test33() throws Exception {
    renameMustWork();
  }

  //anonymous inner class
  public void testAnon0() throws Exception {
    //helper2_fail();
    renameMustWork();
  }

}
