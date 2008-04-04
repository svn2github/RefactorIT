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

import junit.framework.Test;
import junit.framework.TestSuite;


public class RenameMethodInInterfaceTest extends RefactoringTestCase {
  public RenameMethodInInterfaceTest(String name) {
    super(name);
  }

  public String getTemplate() {
    return "RenameMethod/imported/RenameMethodInInterface/<test_name>/<in_out>";
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite(RenameStaticMethodTest.class);
    suite.setName("Imported RenameStaticMethodTests");
    return suite;
  }

  protected void tearDown() throws Exception {
    DialogManager.setInstance(new NullDialogManager());
  }

  private void renameMustFail(String methodName, String newMethodName,
      String[] signatures) throws Exception {
    final Project project
        = RwRefactoringTestUtils.createMutableProject(getInitialProject());
    project.getProjectLoader().build();

    BinTypeRef aRef =
        project.findTypeRefForName("p.I");

    BinTypeRef[] params = RenameMethodTest.convertSingature(project, signatures);
    // RIGHT
    BinMethod m = MethodInvocationRules.getMethodDeclaration(aRef.getBinCIType(),
        aRef, methodName, params);

    String debugString = RenameMethodTest.filesToString(project);
    assertNotNull(m);

    RenameMethod renamer = new RenameMethod(new NullContext(project), m);
    renamer.setNewName(newMethodName);

    RefactoringStatus status = renamer.checkPreconditions();
    status.merge(renamer.checkUserInput());

    status.merge(renamer.apply());

    if (status.isOk()) {
      fail("Should have failed " + debugString);
    }
  }

  private void renameMustFail() throws Exception {
    renameMustFail("m", "k", new String[0]);
  }

  private void doRenameTest(String methodName, String newMethodName,
      String[] signatures, boolean shouldPass,
      boolean updateReferences) throws Exception {
    final Project project
        = RwRefactoringTestUtils.createMutableProject(getInitialProject());
    project.getProjectLoader().build();

    BinTypeRef aRef =
        project.findTypeRefForName("p.I");

    BinTypeRef[] params = RenameMethodTest.convertSingature(project, signatures);
    BinMethod m = MethodInvocationRules.getMethodDeclaration(aRef.getBinCIType(),
        aRef, methodName, params);

    String debugString = RenameMethodTest.filesToString(project);

    assertNotNull(m);

    RenameMethod renamer = new RenameMethod(new NullContext(project), m);
    renamer.setNewName(newMethodName);

    RefactoringStatus status = renamer.checkPreconditions();
    status.merge(renamer.checkUserInput());

    status.merge(renamer.apply());

    if (!status.isOk()) {
      fail("Should have worked " + debugString);
    }
  }

//  private void doRenameTest(String methodName, String newMethodName, String[] signatures, boolean shouldPass) throws Exception{
//    doRenameTest(methodName, newMethodName, signatures, shouldPass, true);
//  }
//
//  private void renameMustWork(String methodName, String newMethodName, String[] signatures) throws Exception{
//    doRenameTest(methodName, newMethodName, signatures, true);
//  }

  private void renameMustWork(boolean updateReferences) throws Exception {
    doRenameTest("m", "k", new String[0], true, updateReferences);
  }

  private void renameMustWork() throws Exception {
    renameMustWork(true);
  }

//  private void renameShouldFail() throws Exception{
////		printTestDisabledMessage("search engine bug");
//    doRenameTest("m", "k", new String[0], false);
//  }


  /********tests************/
  public void testFail0() throws Exception {
    renameMustFail();
  }

  public void testFail1() throws Exception {
    renameMustFail();
  }

  public void testFail3() throws Exception {
    renameMustFail();
  }

  public void testFail4() throws Exception {
    renameMustFail();
  }

  public void testFail5() throws Exception {
    renameMustFail();
  }

  public void testFail6() throws Exception {
    renameMustFail();
  }

  public void testFail7() throws Exception {
    renameMustFail();
  }

  public void testFail8() throws Exception {
//		helper1_0("m", "k", new String[]{Signature.SIG_INT});
  }

  public void testFail9() throws Exception {
    renameMustFail();
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
    renameMustFail();
  }

  public void testFail14() throws Exception {
    renameMustFail();
  }

  public void testFail15() throws Exception {
    renameMustFail();
  }

  public void testFail16() throws Exception {
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
    renameMustFail("m", "k", new String[] {"QString;"});
  }

  public void testFail22() throws Exception {
    renameMustFail("m", "k", new String[] {"QObject;"});
  }

  public void testFail23() throws Exception {
    renameMustFail("toString", "k", new String[0]);
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

  public void testFail29() throws Exception {
    renameMustFail();
  }

  public void testFail30() throws Exception {
    renameMustFail("toString", "k", new String[0]);
  }

  public void testFail31() throws Exception {
    renameMustFail("toString", "k", new String[0]);
  }

  public void testFail32() throws Exception {
    renameMustFail("m", "toString", new String[0]);
  }

  public void testFail33() throws Exception {
    renameMustFail("m", "toString", new String[0]);
  }

  public void testFail34() throws Exception {
    renameMustFail("m", "equals", new String[] {"QObject;"});
  }

  public void testFail35() throws Exception {
    renameMustFail("m", "equals", new String[] {"Qjava.lang.Object;"});
  }

  public void testFail36() throws Exception {
    renameMustFail("m", "getClass", new String[0]);
  }

  public void testFail37() throws Exception {
    renameMustFail("m", "hashCode", new String[0]);
  }

  public void testFail38() throws Exception {
    renameMustFail("m", "notify", new String[0]);
  }

  public void testFail39() throws Exception {
    renameMustFail("m", "notifyAll", new String[0]);
  }

  public void testFail40() throws Exception {
    //helper1_0("m", "wait", new String[]{Signature.SIG_LONG, Signature.SIG_INT});
  }

  public void testFail41() throws Exception {
//		helper1_0("m", "wait", new String[]{Signature.SIG_LONG});
  }

  public void testFail42() throws Exception {
    renameMustFail("m", "wait", new String[0]);
  }

  public void testFail43() throws Exception {
    renameMustFail("m", "wait", new String[0]);
  }

  public void test0() throws Exception {
    renameMustWork();
  }

  public void test1() throws Exception {
    renameMustWork();
  }

  public void test2() throws Exception {
    renameMustWork();
  }

  public void test3() throws Exception {
    renameMustWork();
  }

  public void test4() throws Exception {
    renameMustWork();
  }

  public void test5() throws Exception {
    renameMustWork();
  }

  public void test6() throws Exception {
    renameMustWork();
  }

  public void test7() throws Exception {
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

  //test13 became testFail45
  //public void test13() throws Exception{
  //	helper2();
  //}
  public void test14() throws Exception {
    renameMustWork();
  }

  public void test15() throws Exception {
    renameMustWork();
  }

  public void test16() throws Exception {
    renameMustWork();
  }

  public void test17() throws Exception {
    renameMustWork();
  }

  public void test18() throws Exception {
    renameMustWork();
  }

  public void test19() throws Exception {
    renameMustWork();
  }

  public void test20() throws Exception {
    renameMustWork();
  }

  //anonymous inner class
  public void test21() throws Exception {
//		printTestDisabledMessage("must fix - incorrect warnings");
    //helper2_fail();
  }

  public void test22() throws Exception {
    renameMustWork();
  }

  //test23 became testFail45
  //public void test23() throws Exception{
  //	helper2();
  //}

  public void test24() throws Exception {
    renameMustWork();
  }

  public void test25() throws Exception {
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

  //anonymous inner class
  public void test31() throws Exception {
    renameMustWork();
  }

  //anonymous inner class
  public void test32() throws Exception {
    renameMustWork();
  }

  public void test33() throws Exception {
    renameMustWork();
  }

  public void test34() throws Exception {
    renameMustWork();
  }

  public void test35() throws Exception {
    renameMustWork();
  }

  public void test36() throws Exception {
    renameMustWork();
  }

  public void test37() throws Exception {
    renameMustWork();
  }

  public void test38() throws Exception {
    renameMustWork();
  }

  public void test39() throws Exception {
    renameMustWork();
  }

  public void test40() throws Exception {
    renameMustWork();
  }

  public void test41() throws Exception {
    renameMustWork();
  }

  public void test42() throws Exception {
    renameMustWork();
  }

  public void test43() throws Exception {
    renameMustWork();
  }

  public void test44() throws Exception {
    renameMustWork();
  }

  public void test45() throws Exception {
    renameMustWork();
  }

  public void test46() throws Exception {
    renameMustWork(false);
  }

  public void test47() throws Exception {
    renameMustWork();
  }
}
