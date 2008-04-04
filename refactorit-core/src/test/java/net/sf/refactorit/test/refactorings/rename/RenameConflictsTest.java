/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings.rename;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.rename.RenameType;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.test.refactorings.NullContext;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class RenameConflictsTest extends TestCase {
  public RenameConflictsTest(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(RenameConflictsTest.class);
  }

  public void testImportedClassWillHaveNameOfAnotherClassInUsersPackage() throws
      Exception {
    Project project = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
        "package a;import b.UsedInAnotherPackage;" +
        "\n" +
        "public class User {\n" +
        "  UsedInOwnPackage field1;\n" +
        "  UsedInAnotherPackage field2;\n" +
        "}\n",
        "User.java", "a"
        ),
        new Utils.TempCompilationUnit(
        "package a;public class UsedInOwnPackage {}",
        "UsedInOwnPackage.java", "a"
        ),
        new Utils.TempCompilationUnit(
        "package b;public class UsedInAnotherPackage {}",
        "UsedInAnotherPackage.java", "b"
        )
    });

    RenameType rename = createRenameType(project, "b.UsedInAnotherPackage",
        "UsedInOwnPackage");
    RefactoringStatus status = RenameTestUtil.canBeSuccessfullyChanged(rename);
    if (status != null) {
      assertTrue(status.getAllMessages(), false);
    }
  }

  private RenameType createRenameType(final Project project,
      final String nameBefore, final String nameAfter) {
    BinCIType toRename = project.getTypeRefForName(nameBefore).getBinCIType();

    RenameType rename = new RenameType(new NullContext(project), toRename);
    rename.setNewName(nameAfter);

    return rename;
  }

}
