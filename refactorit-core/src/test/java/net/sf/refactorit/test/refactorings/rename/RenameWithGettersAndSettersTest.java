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
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.rename.RenameField;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.test.refactorings.NullContext;
import net.sf.refactorit.utils.GetterSetterUtils;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class RenameWithGettersAndSettersTest extends TestCase {
  public RenameWithGettersAndSettersTest(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(RenameWithGettersAndSettersTest.class);
  }

  private Project createProjectWithLotsOfFields() throws Exception {
    return Utils.createTestRbProjectFromString(
        "public class X { \n" +
        "  public String noMethods; \n" +
        "  public String wrongMethods; \n" +

        "  public String hasSetter; \n" +
        "  public int hasGetter; \n" +
        "  public boolean hasBoth; \n" +

        "  public void getWrongMethods() {} \n" +
        "  public void setWrongMethods(int x) {} \n" +

        "  public void setHasSetter(String s) {hasSetter=s;} \n" +

        "  public int getHasGetter() {return hasGetter;} \n" +

        "  public boolean isHasBoth() {return hasBoth;} \n" +
        "  public void setHasBoth(boolean b) {hasBoth=b;} \n" +

        "}", "X.java", null
        );
  }

  public void testGetSetterAndGetGetter() throws Exception {
    Project project = createProjectWithLotsOfFields();

    CompilationUnit file = (CompilationUnit) project.getCompilationUnits().get(0);
    BinTypeRef type = (BinTypeRef) file.getDefinedTypes().get(0);

    BinField noMethods = type.getBinCIType().getDeclaredField("noMethods");
    BinField wrongMethods = type.getBinCIType().getDeclaredField("wrongMethods");
    BinField hasSetter = type.getBinCIType().getDeclaredField("hasSetter");
    BinField hasGetter = type.getBinCIType().getDeclaredField("hasGetter");
    BinField hasBoth = type.getBinCIType().getDeclaredField("hasBoth");

    assertEquals("must be no getter", null,
        GetterSetterUtils.getGetterMethodFor(noMethods));
    assertEquals("must be no setter", null,
        GetterSetterUtils.getSetterMethodFor(noMethods));

    assertEquals("must be no getter", null,
        GetterSetterUtils.getGetterMethodFor(wrongMethods));
    assertEquals("must be no setter", null,
        GetterSetterUtils.getSetterMethodFor(wrongMethods));

    assertTrue("must be no getter",
        null == GetterSetterUtils.getGetterMethodFor(hasSetter));
    assertTrue("must be setter",
        null != GetterSetterUtils.getSetterMethodFor(hasSetter));

    assertTrue("must be getter",
        null != GetterSetterUtils.getGetterMethodFor(hasGetter));
    assertTrue("must be no setter",
        null == GetterSetterUtils.getSetterMethodFor(hasGetter));

    assertTrue("must be getter",
        null != GetterSetterUtils.getGetterMethodFor(hasBoth));
    assertTrue("must be setter",
        null != GetterSetterUtils.getSetterMethodFor(hasBoth));
  }

  public void testRenameBoth() throws Exception {
    Project before = createProjectWithLotsOfFields();

    Project after = Utils.createTestRbProjectFromString(
        "public class X { \n" +
        "  public String noMethods; \n" +
        "  public String wrongMethods; \n" +

        "  public String hasSetter; \n" +
        "  public int hasGetter; \n" +
        "  public boolean hasBothWithNewName; \n" +

        "  public void getWrongMethods() {} \n" +
        "  public void setWrongMethods(int x) {} \n" +

        "  public void setHasSetter(String s) {hasSetter=s;} \n" +

        "  public int getHasGetter() {return hasGetter;} \n" +

        "  public boolean isHasBothWithNewName() {return hasBothWithNewName;} \n" +
        "  public void setHasBothWithNewName(boolean b) {hasBothWithNewName=b;} \n" +

        "}", "X.java", null
        );

    assertRenameField(before, after, "hasBoth", "hasBothWithNewName");
  }

  public void testRenameMethodsInEntireInheritanceHierarchy() throws Exception {
    Project before = Utils.createTestRbProjectFromString(
        "public class X { \n" +
        "  private String f; \n" +
        "  public void setF(String f){ this.f=f; } \n" +
        "} \n" +
        "public class Y extends X { \n" +
        "  public void setF(String f){} \n" +
        "} \n",
        "X.java", null
        );

    Project after = Utils.createTestRbProjectFromString(
        "public class X { \n" +
        "  private String f2; \n" +
        "  public void setF2(String f){ this.f2=f; } \n" +
        "} \n" +
        "public class Y extends X { \n" +
        "  public void setF2(String f){} \n" +
        "} \n",
        "X.java", null
        );

    assertRenameField(before, after, "f", "f2");
  }

  public void testBooleanGetters() throws Exception {
    Project before = Utils.createTestRbProjectFromString(
        "public class X { \n" +
        "  private boolean f; \n" +
        "  public void setF(boolean f){ this.f=f; } \n" +
        "  public boolean getF() { return this.f; } \n" +
        "} \n",
        "X.java", null
        );

    Project after = Utils.createTestRbProjectFromString(
        "public class X { \n" +
        "  private boolean f2; \n" +
        "  public void setF2(boolean f){ this.f2=f; } \n" +
        "  public boolean getF2() { return this.f2; } \n" +
        "} \n",
        "X.java", null
        );

    assertRenameField(before, after, "f", "f2");
  }
  
  public void testBooleanGetters2() throws Exception {
    Project before = Utils.createTestRbProjectFromString(
        "public class X { \n" +
        "  private boolean f; \n" +
        "  public boolean isF() { return this.f; } \n" +        
        "  public void setF(boolean f){ this.f=f; } \n" +
        "  public boolean getF() { return this.f; } \n" +
        "} \n",
        "X.java", null
        );

    Project after = Utils.createTestRbProjectFromString(
        "public class X { \n" +
        "  private boolean f2; \n" +
        "  public boolean isF2() { return this.f2; } \n" +   
        "  public void setF2(boolean f){ this.f2=f; } \n" +
        "  public boolean getF() { return this.f2; } \n" +
        "} \n",
        "X.java", null
        );

    assertRenameField(before, after, "f", "f2");
  }
  
  /**
   * This tests a bug that was accidentally found -- the bug was caused by old
   * optimizations in RenameField that did not apply anymore.
   */
  public void testRenamingPrivateFieldsWithAccessorsUsedInOtherFiles() throws
      Exception {
    Project before = Utils.createTestRbProjectFromString(
        new Utils.TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
        "public class X { \n" +
        "  private String f; \n" +
        "  public void setF(String f){ this.f=f; } \n" +
        "} \n",
        "X.java", null
        ),
        new Utils.TempCompilationUnit(
        "public class Y extends X { \n" +
        "  public void setF(String f){} \n" +
        "} \n",
        "Y.java", null
        )
    }
        );

    Project after = Utils.createTestRbProjectFromString(
        new Utils.TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
        "public class X { \n" +
        "  private String f2; \n" +
        "  public void setF2(String f){ this.f2=f; } \n" +
        "} \n",
        "X.java", null
        ),
        new Utils.TempCompilationUnit(
        "public class Y extends X { \n" +
        "  public void setF2(String f){} \n" +
        "} \n",
        "Y.java", null
        )
    }
        );

    assertRenameField(before, after, "f", "f2");
  }

  public void testRenamingGetterInSuperclass() throws Exception {
    Project before = Utils.createTestRbProjectFromString(
        new Utils.TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
        "public class Super { \n" +
        "  public String getF(){return \"\";} \n" +
        "} \n",
        "Super.java", null
        ),
        new Utils.TempCompilationUnit(
        "public class X extends Super { \n" +
        "  private String f; \n" +
        "  public String getF(){ return this.f; } \n" +
        "} \n",
        "X.java", null
        )
    }
        );

    Project after = Utils.createTestRbProjectFromString(
        new Utils.TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
        "public class Super { \n" +
        "  public String getF2(){return \"\";} \n" +
        "} \n",
        "Super.java", null
        ),
        new Utils.TempCompilationUnit(
        "public class X extends Super { \n" +
        "  private String f2; \n" +
        "  public String getF2(){ return this.f2; } \n" +
        "} \n",
        "X.java", null
        )
    }
        );

    assertRenameField(before, after, "f", "f2");
  }

  public void testRenameConflictInSuperclass() throws Exception {
    Project before = Utils.createTestRbProjectFromString(
        new Utils.TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
        "public class Super { \n" +
        "  public String getF(){return \"\";} \n" +
        "  public String getF2(){return \"\";} \n" +
        "} \n",
        "Super.java", null
        ),
        new Utils.TempCompilationUnit(
        "public class X extends Super { \n" +
        "  private String f; \n" +
        "  public String getF(){ return this.f; } \n" +
        "} \n",
        "X.java", null
        )
    }
        );

    assertRenameConflict(before, "f", "f2");
  }

  public void testRenameConflictInSameClassAsField() throws Exception {
    Project before = Utils.createTestRbProjectFromString(
        new Utils.TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
        "public class Super { \n" +
        "  public String getF(){return \"\";} \n" +
        "} \n",
        "Super.java", null
        ),
        new Utils.TempCompilationUnit(
        "public class X extends Super { \n" +
        "  private String f; \n" +
        "  public String getF(){ return this.f; } \n" +
        "  public String getF2(){return \"\";} \n" +
        "} \n",
        "X.java", null
        )
    }
        );

    assertRenameConflict(before, "f", "f2");
  }

  public void testRenameConflictInSubclass() throws Exception {
    Project before = Utils.createTestRbProjectFromString(
        new Utils.TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
        "public class X{ \n" +
        "  private String f; \n" +
        "  public String getF(){return this.f;} \n" +
        "} \n",
        "X.java", null
        ),
        new Utils.TempCompilationUnit(
        "public class Sub extends X{ \n" +
        "  public String getF(){return \"\"; } \n" +
        "  public String getF2(){return \"\";} \n" +
        "} \n",
        "Sub.java", null
        )
    }
        );

    assertRenameConflict(before, "f", "f2");
  }

  public void assertRenameConflict(Project project, String fieldNameBefore,
      String fieldNameAfter) {
    RenameField rename = createRenameField(project, fieldNameBefore,
        fieldNameAfter);
    RefactoringStatus status = RenameTestUtil.canBeSuccessfullyChanged(rename);
    assertTrue(status != null);
  }

  /** The "before" project must be mutable. The field must be in class "X" */
  public void assertRenameField(Project before, Project after,
      String fieldNameBefore, String fieldNameAfter) {
    RenameField rename = createRenameField(before, fieldNameBefore,
        fieldNameAfter);
    RefactoringStatus status = RenameTestUtil.canBeSuccessfullyChanged(rename);
    if (status != null) {
      assertTrue(status.getAllMessages(), false);
    }

    RwRefactoringTestUtils.assertSameSources(
        "renamed fields w/getters and setters", before, after);
  }

  private RenameField createRenameField(Project project, String fieldNameBefore,
      String fieldNameAfter) {
    BinField f = project.getTypeRefForName("X").getBinCIType()
        .getDeclaredField(fieldNameBefore);

    RenameField rename = new RenameField(new NullContext(project), f);
    rename.setRenameGettersAndSetters(true);

    rename.setNewName("first_a_wrong_name_to_check_name_resetting");
    rename.setNewName(fieldNameAfter);

    return rename;
  }
}
