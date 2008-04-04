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


public class RenamePrivateFieldTest extends RefactoringTestCase {
//  private static final String PROJECTS_PATH=
//    "RenameField/imported/RenamePrivateField/";

  public RenamePrivateFieldTest(String name) {
    super(name);
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite(RenamePrivateFieldTest.class);
    suite.setName("Rename Private Field");
    return suite;
  }

  protected void tearDown() {
    DialogManager.setInstance(new NullDialogManager());
  }

  public String getTemplate() {
    return "RenameField/imported/RenamePrivateField/<test_name>/<in_out>";
  }

  private void renameMustFail(String fieldName,
      String newFieldName,
      String typeName,
      boolean renameGetter,
      boolean renameSetter) throws Exception {

    final Project project =
        RwRefactoringTestUtils.createMutableProject(getInitialProject());
    project.getProjectLoader().build();

    BinTypeRef aRef =
        project.findTypeRefForName("p." + typeName);

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
        + newFieldName + " should fail", !status.isOk());
  }

  private void renameMustFail(String fieldName,
      String newFieldName) throws Exception {

    renameMustFail(fieldName, newFieldName, "A", false, false);
  }

  private void renameMustFail() throws Exception {
    renameMustFail("f", "g");
  }

  //--

  private void renameMustWork(String fieldName,
      String newFieldName,
      boolean updateReferences,
      boolean updateJavaDoc,
      boolean updateComments,
      boolean updateStrings,
      boolean renameGettersAndSetters,
      boolean expectedGetterSetterRename) throws Exception {

    final Project project =
        RwRefactoringTestUtils.createMutableProject(getInitialProject());
    project.getProjectLoader().build();

    BinTypeRef aRef = project.findTypeRefForName("p.A");
    BinField renamable = aRef.getBinCIType().getDeclaredField(fieldName);

    final RenameField renamer
        = new RenameField(new NullContext(project), renamable);
    renamer.setNewName(newFieldName);
    renamer.setRenameGettersAndSetters(renameGettersAndSetters);
    
    RefactoringStatus status = renamer.checkPreconditions();
    status.merge(renamer.checkUserInput());

    status.merge(renamer.apply());

    assertTrue("Renaming " + renamable.getQualifiedName() + " -> "
        + newFieldName
        + " succeeded: " + status.getAllMessages(),
        status.isOk());

    RwRefactoringTestUtils.assertSameSources(
        "Renamed " + renamable.getQualifiedName() + " -> " + newFieldName,
        getExpectedProject(),
        project);
  }

  private void renameMustWork(boolean updateReferences) throws Exception {
    renameMustWork("f", "g", updateReferences, false, false, false, false, false);
  }

  private void renameMustWork() throws Exception {
    renameMustWork(true);
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
    renameMustFail("gg", "f", "A", false, false);
  }

  public void NONtestFail9() throws Exception {
    renameMustFail("y", "e", "getE", true, true);
  }

  public void NONtestFail10() throws Exception {
    renameMustFail("y", "e", "setE", true, true);
  }

  // ------
  public void test0() throws Exception {
    renameMustWork();
  }

  public void test1() throws Exception {
    renameMustWork();
  }

  public void test2() throws Exception {
    renameMustWork(false);
  }

  public void NONtest3() throws Exception {
    renameMustWork("f", "gg",
        true, true, true, true, false, false);
  }

  public void NONtest4() throws Exception {
    renameMustWork("fMe", "fYou",
        true, false, false, false, true, true);
  }

  public void NONtest5() throws Exception {
    //regression test for 9895
    renameMustWork("fMe", "fYou",
        true, false, false, false, true, true);
  }

  public void NONtest6() throws Exception {
    //regression test for 9895 - opposite case
    renameMustWork("me", "you",
        true, false, false, false, true, true);
  }
  
  public void test7() throws Exception {
    //regression test for 1748
    renameMustWork();
  }
}
