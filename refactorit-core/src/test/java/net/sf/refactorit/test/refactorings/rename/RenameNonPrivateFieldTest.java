/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings.rename;

import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.rename.RenameField;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.refactorings.NullContext;
import net.sf.refactorit.test.refactorings.RefactoringTestCase;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.NullDialogManager;

import junit.framework.Test;
import junit.framework.TestSuite;


public class RenameNonPrivateFieldTest extends RefactoringTestCase {
//  private static final String PROJECTS_PATH =
//      "RenameField/imported/RenameNonPrivateField/";

  public RenameNonPrivateFieldTest(String name) {
    super(name);
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite(RenameNonPrivateFieldTest.class);
    suite.setName("Rename Non Private Field");
    return suite;
  }

  protected void tearDown() {
    DialogManager.setInstance(new NullDialogManager());
  }

  public String getTemplate() {
    return "RenameField/imported/RenameNonPrivateField/<test_name>/<in_out>";
  }

  private void renameMustFail(String fieldName,
      String newFieldName) throws Exception {

    final Project project
        = RwRefactoringTestUtils.createMutableProject(getInitialProject());
    project.getProjectLoader().build();
    BinTypeRef aRef = project.findTypeRefForName("p.A");

    // HACK HERE
    if (aRef == null) {
      BinTypeRef bRef = project.findTypeRefForName("p.B");
      aRef = bRef.getBinCIType().getDeclaredType("A");
    }

    final BinField renamable = aRef.getBinCIType().getDeclaredField(fieldName);
    final RenameField renamer
        = new RenameField(new NullContext(project), renamable);
    renamer.setNewName(newFieldName);

    RefactoringStatus status = renamer.checkPreconditions();
    status.merge(renamer.checkUserInput());

    status.merge(renamer.apply());

    assertTrue("Renaming " + renamable.getQualifiedName() + " -> "
        + newFieldName + " should fail",
        !status.isOk());
  }

  private void renameMustFail() throws Exception {
    renameMustFail("f", "g");
  }

  private void renameMustWork(String fieldName,
      String newFieldName,
      boolean updateReferences) throws Exception {
    final Project project =
        RwRefactoringTestUtils.createMutableProject(getInitialProject());
    project.getProjectLoader().build();
    BinTypeRef aRef = project.findTypeRefForName("p.A");
    final BinField renamable = aRef.getBinCIType().getDeclaredField(fieldName);

    final RenameField renamer
        = new RenameField(new NullContext(project), renamable);
    renamer.setNewName(newFieldName);

    RefactoringStatus status = RenameTestUtil.canBeSuccessfullyChanged(renamer);

    if (status != null) {
      assertTrue(
          "Renaming " + renamable.getQualifiedName() + " -> " + newFieldName
          + " succeeded: " + status.getAllMessages(),
          status.isOk());
    }

    RwRefactoringTestUtils.assertSameSources(
        "Renamed " + renamable.getQualifiedName() + " -> " + newFieldName,
        getExpectedProject(),
        project);
  }

  private void renameMustWork(String fieldName,
      String newFieldName) throws Exception {
    renameMustWork(fieldName, newFieldName, true);
  }

  private void renameMustWork() throws Exception {
    renameMustWork(true);
  }

  private void renameMustWork(boolean updateReferences) throws Exception {
    renameMustWork("f", "g", updateReferences);
  }

  //--------- tests ----------
  public void testFail0() throws Exception {
    renameMustFail();
  }

  public void testFail1() throws Exception {
    renameMustFail();
  }

  public void testFail2() throws Exception {
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
    renameMustFail();
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
    //printTestDisabledMessage("1GKZ8J6: ITPJCORE:WIN2000 - search: missing field occurrecnces");
    renameMustFail();
  }

  public void testFail14() throws Exception {
    //printTestDisabledMessage("1GKZ8J6: ITPJCORE:WIN2000 - search: missing field occurrecnces");
    renameMustFail();
  }

  // ------
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
    //printTestDisabledMessage("1GKZ8J6: ITPJCORE:WIN2000 - search: missing field occurrecnces");
  }

  public void test5() throws Exception {
    renameMustWork();
  }

  public void test6() throws Exception {
    //printTestDisabledMessage("1GKB9YH: ITPJCORE:WIN2000 - search for field refs - incorrect results");
    renameMustWork();
  }

  public void test7() throws Exception {
    renameMustWork();
  }

  public void test8() throws Exception {
    //printTestDisabledMessage("1GD79XM: ITPJCORE:WINNT - Search - search for field references - not all found");
    renameMustWork();
  }

  public void test9() throws Exception {
    renameMustWork();
  }

  public void test10() throws Exception {
    renameMustWork();
  }

  public void test11() throws Exception {
    renameMustWork();
  }

  public void test12() throws Exception {
    //System.out.println("\nRenameNonPrivateField::" + name() + " disabled (1GIHUQP: ITPJCORE:WINNT - search for static field should be more accurate)");
    renameMustWork();
  }

  public void test13() throws Exception {
    //System.out.println("\nRenameNonPrivateField::" + name() + " disabled (1GIHUQP: ITPJCORE:WINNT - search for static field should be more accurate)");
    renameMustWork();
  }

  public void test14() throws Exception {
    // We do not have an easy rename option without updating references
    // renameMustWork(false);
  }

  public void test15() throws Exception {
    // We do not have an easy rename option without updating references
    //renameMustWork(false);
  }

  public void testBug5821() throws Exception {
    renameMustWork("test", "test1");
  }
}
