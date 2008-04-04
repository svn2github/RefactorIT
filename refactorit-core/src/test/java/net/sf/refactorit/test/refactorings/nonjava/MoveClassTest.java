/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings.nonjava;



import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.WildcardPattern;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.movetype.MoveType;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.refactorings.NullContext;
import net.sf.refactorit.test.refactorings.RefactoringTestCase;

import org.apache.log4j.Category;

import junit.framework.Test;
import junit.framework.TestSuite;


public class MoveClassTest extends RefactoringTestCase {
  private static final Category cat =
      Category.getInstance(RenameClassTest.class.getName());

  public MoveClassTest(String name) {
    super(name);
  }

  public String getTemplate() {
    return "NonJavaFiles/MoveClass/<stripped_test_name>/<in_out>";
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite(RenameClassTest.class);
    suite.setName(
        "Change qualified class names in non-java files with Move Class");
    return suite;
  }

  private void moveMustWork(String typeName, String className,
      String newPackage) throws Exception {
    cat.info("Testing " + getStrippedTestName());

    final Project project = getMutableProject();

    Project.getDefaultOptions().setNonJavaFilesPatterns(
        new WildcardPattern[] {new WildcardPattern("*.xml")});

    BinTypeRef aRef = project.findTypeRefForName(typeName);
    BinCIType type = aRef.getBinCIType();

    MoveType moveType = new MoveType(new NullContext(project), type);
    moveType.setChangeInNonJavaFiles(true);
    moveType.setTargetPackage(project.getPackageForName(newPackage));

    RefactoringStatus status = moveType.checkPreconditions();
    status.merge(moveType.checkUserInput());

    status.merge(moveType.apply());

    assertTrue("Moving " + type.getQualifiedName() + " -> " + newPackage
        + " succeeded: " + status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", getExpectedProject(), project);

    cat.info("SUCCESS");
  }

  public void testSimple() throws Exception {
    moveMustWork("com.acme.Test", "Test", "com");
  }

}
