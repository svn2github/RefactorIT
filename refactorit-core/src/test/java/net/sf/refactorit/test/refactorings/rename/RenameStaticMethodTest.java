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


public class RenameStaticMethodTest extends RefactoringTestCase {

  public RenameStaticMethodTest(String name) {
    super(name);
  }

  public String getTemplate() {
    return "RenameMethod/imported/RenameStaticMethod/<test_name>/<in_out>";
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

    RenameMethod renamer = new RenameMethod(new NullContext(project), m);
    renamer.setNewName(newMethodName);

    RefactoringStatus status = renamer.checkPreconditions();
    status.merge(renamer.checkUserInput());

    status.merge(renamer.apply());

    if (!status.isOk()) {
      fail("Should have worked" + debugString);
    }
  }

//  private void renameMustWork(String methodName, String newMethodName, String[] signatures) throws Exception{
//    renameMustWork(methodName, newMethodName, signatures, true);
//  }

  private void renameMustWork(boolean updateReferences) throws Exception {
    renameMustWork("m", "k", new String[0], updateReferences);
  }

  private void renameMustWork() throws Exception {
    renameMustWork(true);
  }

  /********** tests *********/
  public void testFail0() throws Exception {
    renameMustFail();
  }

  public void testFail1() throws Exception {
    renameMustFail();
  }

  public void testFail2() throws Exception {
    renameMustFail();
  }

  //testFail3 deleted

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
    renameMustFail();
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
//		helper2_0("m", "k", new String[]{Signature.SIG_INT});
  }

  public void test8() throws Exception {
//		helper2_0("m", "k", new String[]{Signature.SIG_INT});
  }

  public void test9() throws Exception {
//		helper2_0("m", "k", new String[]{Signature.SIG_INT}, false);
  }
}
