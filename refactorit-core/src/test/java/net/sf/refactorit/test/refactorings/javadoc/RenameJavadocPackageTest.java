/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.refactorings.javadoc;



import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.rename.RenamePackage;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.refactorings.NullContext;
import net.sf.refactorit.test.refactorings.RefactoringTestCase;

import org.apache.log4j.Category;

import junit.framework.Test;
import junit.framework.TestSuite;


public class RenameJavadocPackageTest extends RefactoringTestCase {

  private static final Category cat =
      Category.getInstance(RenameJavadocPackageTest.class.getName());

  public RenameJavadocPackageTest(String name) {
    super(name);
  }

  public String getTemplate() {
    return "RenameJavadoc/PackageTests/<stripped_test_name>/<in_out>";
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite(RenameJavadocPackageTest.class);
    suite.setName("Rename Javadoc Package");
    return suite;
  }

  private void renameMustWork(String packageName,
      boolean renamePrefix) throws Exception {
    cat.info("Testing " + getStrippedTestName());

    Project project = getMutableProject();

    BinPackage _package = project.getPackageForName(packageName);

    String newName = packageName + "_ren";
    RenamePackage renamePackage
        = new RenamePackage(new NullContext(project), _package);
    renamePackage.setRenameInJavadocs(true);
    renamePackage.setRenamePrefix(renamePrefix);
    renamePackage.setPrefix(packageName);
    renamePackage.setNewName(newName);
    renamePackage.setDisableRelocation(true);

    RefactoringStatus status = renamePackage.checkPreconditions();
    status.merge(renamePackage.checkUserInput());

    status.merge(renamePackage.apply());

    /*if (!status.isOk()) {
     if ( !RefactorItConstants.runNotImplementedTests && status.isInfoOrWarning() ) {
        String msg="This failure should be caused by the not implemented feature, RefactorIT doesn't rename directories";
        System.out.println(msg +": msg= "+
                           status.getAllMessages());
      } else {
        assertTrue("Renaming " + _package.getQualifiedName() + " -> " + newName
             + " succeeded: " + status.getAllMessages(), status.isOk());
      }
         }*/

    RwRefactoringTestUtils.assertSameSources("", getExpectedProject(), project);

    cat.info("SUCCESS");
  }

  public void testWork1() throws Exception {
    renameMustWork("com.p1", false);
  }

  public void testWork2() throws Exception {
    renameMustWork("com.p1.p3", false);
  }

  public void testWork3() throws Exception {
    renameMustWork("com.p1", true);
  }
}
