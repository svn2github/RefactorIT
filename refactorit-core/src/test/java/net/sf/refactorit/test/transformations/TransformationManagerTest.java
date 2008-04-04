/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.transformations;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.rename.RenameType;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.test.refactorings.NullContext;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.transformations.TransformationManager;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TransformationManagerTest extends TestCase {
  public TransformationManagerTest(String name) {
    super(name);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite(TransformationManagerTest.class);
    suite.setName("Transformation Manager tests");
    return suite;
  }

  public void testAddTransformationList() throws Exception {
    Project project = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
        "public class A {\n" +
        "  A a, b;\n" +
        "}",
        "A.java", ""
        )
    });

    Project after = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
        "public class B {\n" +
        "  B a, b;\n" +
        "}",
        "B.java", ""
        )
    });

    RenameType renameType = new RenameType(new NullContext(project),
        project.getTypeRefForName("A").getBinCIType());
    renameType.setNewName("B");

    TransformationManager manager = new TransformationManager(renameType);
    TransformationList transList = renameType.checkAndExecute();

    manager.add(transList);

    RefactoringStatus status = manager.performTransformations();
System.err.println("Controller: " + IDEController.getInstance());

    Assert.assertTrue("refactoring status was not OK: " +
        status.getAllMessages(),
        status.isOk() || status.isInfoOrWarning());

    RwRefactoringTestUtils.assertSameSources("performed rename", project, after);
  }

  public void testAddManyTransformationList() throws Exception {
    Project project = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
        "public class A {\n" +
        "  A a1, b1;\n" +
        "}",
        "A.java", ""
        ),
        new Utils.TempCompilationUnit(
            "public class B {\n" +
            "  B a2, b2;\n" +
            "}",
            "B.java", ""
            )
    });

    Project after = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
            "public class C {\n" +
            "  C a1, b1;\n" +
            "}",
            "C.java", ""
            ),
            new Utils.TempCompilationUnit(
                "public class D {\n" +
                "  D a2, b2;\n" +
                "}",
                "D.java", ""
                )
    });

    RenameType renameTypeA = new RenameType(new NullContext(project),
        project.getTypeRefForName("A").getBinCIType());
    renameTypeA.setNewName("C");
    TransformationList transListA = renameTypeA.checkAndExecute();

    RenameType renameTypeB = new RenameType(new NullContext(project),
        project.getTypeRefForName("B").getBinCIType());
    renameTypeB.setNewName("D");
    TransformationList transListB = renameTypeB.checkAndExecute();

    TransformationManager manager = new TransformationManager(renameTypeA);
    manager.add(transListA);
    manager.add(transListB);

    RefactoringStatus status = manager.performTransformations();

    Assert.assertTrue("refactoring status was not OK: " +
        status.getAllMessages(),
        status.isOk() || status.isInfoOrWarning());

    RwRefactoringTestUtils.assertSameSources("performed rename", project, after);
  }
}
