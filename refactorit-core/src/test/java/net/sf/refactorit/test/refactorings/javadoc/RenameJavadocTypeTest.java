/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.refactorings.javadoc;



import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.rename.RenameType;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.refactorings.NullContext;
import net.sf.refactorit.test.refactorings.RefactoringTestCase;

import org.apache.log4j.Category;

import junit.framework.Test;
import junit.framework.TestSuite;


public class RenameJavadocTypeTest extends RefactoringTestCase {

  private static final Category cat =
      Category.getInstance(RenameJavadocTypeTest.class.getName());

  public RenameJavadocTypeTest(String name) {
    super(name);
  }

  public String getTemplate() {
    return "RenameJavadoc/TypeTests/<stripped_test_name>/<in_out>";
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite(RenameJavadocTypeTest.class);
    suite.setName("Rename Javadoc Types");
    return suite;
  }

  private void renameMustWork(String typeName,
      String className) throws Exception {
    cat.info("Testing " + getStrippedTestName());

    final Project project = getMutableProject();

    BinTypeRef aRef = project.findTypeRefForName(typeName);
    BinCIType type = aRef.getBinCIType();

    String newName = className + "_ren";
    RenameType renameType = new RenameType(new NullContext(project), type);
    renameType.setRenameInJavadocs(true);
    renameType.setNewName(newName);

    RefactoringStatus status = renameType.checkPreconditions();
    status.merge(renameType.checkUserInput());

    status.merge(renameType.apply());

    assertTrue("Renaming " + type.getQualifiedName() + " -> " + newName
        + " succeeded: " + status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", getExpectedProject(), project);

    cat.info("SUCCESS");
  }

  public void testWork1() throws Exception {
    renameMustWork("com.p1.Class1", "Class1");
  }

  public void testWork2() throws Exception {
    renameMustWork("com.p1.Class1$Inner", "Inner");
  }
}
