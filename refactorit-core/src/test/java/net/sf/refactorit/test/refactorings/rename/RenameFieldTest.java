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
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.rename.RenameField;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.test.refactorings.NullContext;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class RenameFieldTest extends TestCase {
  public RenameFieldTest(String name) {
    super(name);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite(RenameFieldTest.class, "Rename Field Tests");
    suite.addTest(RenameNonPrivateFieldTest.suite());
    suite.addTest(RenamePrivateFieldTest.suite());
    suite.addTest(RenameWithGettersAndSettersTest.suite());

    return suite;
  }

  public void testBug1779_1() throws Exception {
    Project after = Utils.createTestRbProjectFromString(
        new Utils.TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
        "package a;\n" +
        "\n" +
        "public class X {\n" +
        "  int anotherField = 0;" +
        "  /** getter for {@link #field} */" +
        "  public int getField() {\n" +
        "    return anotherField;\n" +
        "  }\n\n" +
        "  /** setter for {@link #field} */" +
        "  public void setField(int field) {\n" +
        "    this.anotherField = field;\n" +
        "  }\n" +
        "}",
        "X.java", "a"
        )
    }
        );

    bodyOfTest1779(false, false, after);
  }

  public void testBug1779_2() throws Exception {
    Project after = Utils.createTestRbProjectFromString(
        new Utils.TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
        "package a;\n" +
        "\n" +
        "public class X {\n" +
        "  int anotherField = 0;" +
        "  /** getter for {@link #field} */" +
        "  public int getAnotherField() {\n" +
        "    return anotherField;\n" +
        "  }\n\n" +
        "  /** setter for {@link #field} */" +
        "  public void setAnotherField(int field) {\n" +
        "    this.anotherField = field;\n" +
        "  }\n" +
        "}",
        "X.java", "a"
        )
    }
        );

    bodyOfTest1779(false, true, after);
  }

  public void testBug1779_3() throws Exception {
    Project after = Utils.createTestRbProjectFromString(
        new Utils.TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
        "package a;\n" +
        "\n" +
        "public class X {\n" +
        "  int anotherField = 0;" +
        "  /** getter for {@link #anotherField} */" +
        "  public int getField() {\n" +
        "    return anotherField;\n" +
        "  }\n\n" +
        "  /** setter for {@link #anotherField} */" +
        "  public void setField(int field) {\n" +
        "    this.anotherField = field;\n" +
        "  }\n" +
        "}",
        "X.java", "a"
        )
    }
        );

    bodyOfTest1779(true, false, after);
  }

  public void testBug1779_4() throws Exception {
    Project after = Utils.createTestRbProjectFromString(
        new Utils.TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
        "package a;\n" +
        "\n" +
        "public class X {\n" +
        "  int anotherField = 0;" +
        "  /** getter for {@link #anotherField} */" +
        "  public int getAnotherField() {\n" +
        "    return anotherField;\n" +
        "  }\n\n" +
        "  /** setter for {@link #anotherField} */" +
        "  public void setAnotherField(int field) {\n" +
        "    this.anotherField = field;\n" +
        "  }\n" +
        "}",
        "X.java", "a"
        )
    }
        );

    bodyOfTest1779(true, true, after);
  }

  private void bodyOfTest1779(boolean renameInJavadocs,
      boolean renameGettersAndSetters,
      Project after) throws Exception {
    Project before = Utils.createTestRbProjectFromString(
        new Utils.TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
        "package a;\n" +
        "\n" +
        "public class X {\n" +
        "  int field = 0;" +
        "  /** getter for {@link #field} */" +
        "  public int getField() {\n" +
        "    return field;\n" +
        "  }\n\n" +
        "  /** setter for {@link #field} */" +
        "  public void setField(int field) {\n" +
        "    this.field = field;\n" +
        "  }\n" +
        "}",
        "X.java", "a"
        )
    }
        );

    BinField field = before.getTypeRefForName("a.X").getBinCIType()
        .getDeclaredField("field");
    RenameField renameField = new RenameField(new NullContext(before), field);
    renameField.setRenameInJavadocs(true);
    renameField.setRenameGettersAndSetters(true);

    RefactoringStatus status = renameField.checkPreconditions();
    assertTrue(status.getAllMessages(), status.isOk());

    renameField.setNewName("anotherField");
    renameField.setRenameInJavadocs(renameInJavadocs);
    renameField.setRenameGettersAndSetters(renameGettersAndSetters);

    status = renameField.checkUserInput();
    assertTrue(status.getAllMessages(), status.isOk());

    status.merge(renameField.apply());
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", before, after);
  }

}
