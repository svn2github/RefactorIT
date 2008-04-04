/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.refactorings.javadoc;



import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.rename.RenameField;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.refactorings.NullContext;
import net.sf.refactorit.test.refactorings.RefactoringTestCase;

import org.apache.log4j.Category;

import junit.framework.Test;
import junit.framework.TestSuite;


public class RenameJavadocFieldTest extends RefactoringTestCase {

  private static final Category cat =
      Category.getInstance(RenameJavadocFieldTest.class.getName());

  public RenameJavadocFieldTest(String name) {
    super(name);
  }

  public String getTemplate() {
    return "RenameJavadoc/FieldTests/<stripped_test_name>/<in_out>";
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite(RenameJavadocFieldTest.class);
    suite.setName("Rename Javadoc Fields");
    return suite;
  }

  private void renameMustWork(String fieldName,
      String className) throws Exception {
    cat.info("Testing " + getStrippedTestName());

    final Project project = getMutableProject();

    BinTypeRef aRef = project.findTypeRefForName(className);
    BinField field = aRef.getBinCIType().getDeclaredField(fieldName);

    String newName = fieldName + "_ren";
    RenameField renameField
        = new RenameField(new NullContext(project), field);
    renameField.setRenameInJavadocs(true);
    renameField.setNewName(newName);

    RefactoringStatus status = renameField.checkPreconditions();
    status.merge(renameField.checkUserInput());
    status.merge(renameField.apply());

    assertTrue("Renamed " + field.getQualifiedName() + " -> " + newName
        + " - " + status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", getExpectedProject(), project);

    cat.info("SUCCESS");
  }

  public void testWork1() throws Exception {
    renameMustWork("tmp1", "com.p1.Class1");
  }

  public void testWork2() throws Exception {
    renameMustWork("tmp1", "com.p1.Class1$Inner");
  }

  public void testWork3() throws Exception {
    renameMustWork("tmp1", "com.p2.Class2");
  }

  public void testWork4() throws Exception {
    renameMustWork("tmp1", "com.p1.Class1");
  }

  public void testWork5() throws Exception {
    renameMustWork("tmp1", "com.p1.Class1");
  }

  public void testWork6() throws Exception {
    renameMustWork("tmp1", "com.p1.Class1$Inner1");
  }

  public void testWork7() throws Exception {
    renameMustWork("tmp1", "com.p1.Class1$Inner1$Inner2");
  }
}
