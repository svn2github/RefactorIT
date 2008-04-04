/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings.promotetemptofield;



import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinItemVisitableUtil;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.test.Utils;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/** @author  RISTO A */
public class AllowedFieldNamesTest extends TestCase {
  public AllowedFieldNamesTest(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(AllowedFieldNamesTest.class);
  }

  // First some older, more funcional-level tests that tested an older
  // implementation, but are also valid for current one.
  //
  // (Sidenote: the old implementation is replaced because it was written in big
  // ugly steps and it got confusing really fast; the last test in here was
  // really hard to make run with that old implementation. So these tests are
  // bad examples in general.)

  public void testWhichFieldNamesShadeExistingFields() throws Exception {
    Project p = Utils.createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
        new Utils.TempCompilationUnit(new String[] {
        "class X extends Super {",
        "  int existing;",
        "",
        "  public void m() {",
        "    int i = usedSuperField;",
        "    int x = super.directlyUsedSuperField;",
        "  }",
        "}"}),
        new Utils.TempCompilationUnit(new String[] {
        "class Super {",
        "  int unusedSuperField;",
        "  int usedSuperField;",
        "  int directlyUsedSuperField;",
        "}"})
    });
    BinCIType type = p.getTypeRefForName("X").getBinCIType();

    assertFalse(type.canCreateField("existing"));
    assertTrue(type.canCreateField("notExisting"));
    assertTrue(type.canCreateField("unusedSuperField"));
    assertFalse(type.canCreateField("usedSuperField"));
    assertTrue(type.canCreateField("directlyUsedSuperField"));
  }

  public void testInnerClassShadesOuterClassField() throws Exception {
    Project p = Utils.createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
        new Utils.TempCompilationUnit(new String[] {
        "class X {",
        "  int outerField1;",
        "  int outerField2;",
        "  int outerField3;",
        "",
        "  public void m() {",
        "    class Local {",
        "      void localMethod() {",
        "        int i = outerField1;",
        "      }",
        "    }",
        "    class LocalSub extends Local {",
        "      int i = outerField2;",
        "    }",
        "  }",
        "}"})});
    BinCIType type = findLocalType(p);

    assertTrue(type.canCreateField("newField"));
    assertFalse(type.canCreateField("outerField1"));
    assertFalse(type.canCreateField("outerField2"));
    assertTrue(type.canCreateField("outerField3"));
  }

  public void testOuterClassShadesInnerClassVairable() throws Exception {
    Project p = Utils.createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
        new Utils.TempCompilationUnit(new String[] {
        "class X {",
        "  public void m() {",
        "    int usedOuterVar;",
        "    int unusedOuterVar;",
        "    class Local {",
        "      void localMethod() {",
        "        int i = usedOuterVar;",
        "      }",
        "    }",
        "  }",
        "}"})});

    BinCIType type = findLocalType(p);

    assertTrue(type.canCreateField("unusedOuterVar"));
    assertFalse(type.canCreateField("usedOuterVar"));
  }

  // Here are the later tests, written for the current implementation, in smaller steps:

  public void testCanCreateFieldThatDoesNotExist() throws Exception {
    Project p = Utils.createTestRbProjectFromArray(new String[] {
        "class X {",
        "}"});
    BinCIType type = p.getTypeRefForName("X").getBinCIType();

    assertTrue(type.canCreateField("aNewField"));
  }

  public void testCannotCreateFieldThatAlreadyExists() throws Exception {
    Project p = Utils.createTestRbProjectFromArray(new String[] {
        "class X {",
        "  int existingField;",
        "}"});
    BinCIType type = p.getTypeRefForName("X").getBinCIType();

    assertFalse(type.canCreateField("existingField"));
  }

  public void testCannotCreateFieldThatExistsInSupertypeAndIsUsed() throws
      Exception {
    Project p = Utils.createTestRbProjectFromArray(new String[] {
        "class X extends Super {",
        "  void m() {",
        "    int i = superField;",
        "  }",
        "}",
        "class Super {",
        "  int superField;",
        "}"});
    BinCIType type = p.getTypeRefForName("X").getBinCIType();

    assertFalse(type.canCreateField("superField"));
  }

  public void testCannotCreateFieldThatExistsInSuperSupertypeAndIsUsed() throws
      Exception {
    Project p = Utils.createTestRbProjectFromArray(new String[] {
        "class X extends Super {",
        "  void m() {",
        "    int i = superSuperField;",
        "  }",
        "}",
        "class Super extends SuperSuper {",
        "}",
        "class SuperSuper {",
        "  int superSuperField;",
        "}"});
    BinCIType type = p.getTypeRefForName("X").getBinCIType();

    assertFalse(type.canCreateField("superSuperField"));
  }

  public void testCanCreateFieldThatExistsInSupertypeAndIsNotUsed() throws
      Exception {
    Project p = Utils.createTestRbProjectFromArray(new String[] {
        "class X extends Super {",
        "  void m() {",
        "  }",
        "}",
        "class Super {",
        "  int unusedSuperField;",
        "}"});
    BinCIType type = p.getTypeRefForName("X").getBinCIType();

    assertTrue(type.canCreateField("unusedSuperField"));
  }

  public void testCannotCreateFieldThatExistsInSupertypeAndIsUseds() throws
      Exception {
    Project p = Utils.createTestRbProjectFromArray(new String[] {
        "class X extends Super {",
        "  void m() {",
        "  }",
        "}",
        "class Super {",
        "  int unusedSuperField;",
        "}"});
    BinCIType type = p.getTypeRefForName("X").getBinCIType();

    assertTrue(type.canCreateField("unusedSuperField"));
  }

  public void testCannotCreateFieldThatExistsInOuterTypeAndIsUsedWithinInner() throws
      Exception {
    Project p = Utils.createTestRbProjectFromArray(new String[] {
        "class X {",
        "  int outerField;",
        "  void m() {",
        "    class Local {",
        "      void m() {",
        "        int usage = outerField;",
        "      }",
        "   }",
        "  }",
        "}"});
    BinCIType type = findLocalType(p);

    assertFalse(type.canCreateField("outerField"));
  }

  public void testGetFieldInvocations() throws Exception {
    Project p = Utils.createTestRbProjectFromArray(new String[] {
        "class X {",
        "  int outerField;",
        "  void m() {",
        "    int invocation = outerField;",
        "  }",
        "}"});
    BinCIType type = p.getTypeRefForName("X").getBinCIType();

    List fields = BinItemVisitableUtil.getFieldsInvokedIn(type);
    assertEquals(1, fields.size());
    assertEquals("outerField", ((BinField) fields.get(0)).getName());
  }

  public void testFieldInvocationIsNotOnOwnerField() throws Exception {
    Project p = Utils.createTestRbProjectFromArray(new String[] {
        "class X {",
        "  int outerField;",
        "  void m() {",
        "    int invocation = outerField;",
        "  }",
        "}"});

    BinCIType type = p.getTypeRefForName("X").getBinCIType();
    BinField f = (BinField) BinItemVisitableUtil.getFieldsInvokedIn(p).get(0);

    assertFalse(f.isOwnedByOuterTypeOf(type));
  }

  public void testFieldInvocationIsOnOwnerField() throws Exception {
    Project p = Utils.createTestRbProjectFromArray(new String[] {
        "class X {",
        "  int outerField;",
        "  void m() {",
        "    class Local {",
        "      int invocation = outerField;",
        "    }",
        "  }",
        "}"});

    BinCIType type = findLocalType(p);
    BinField f = (BinField) BinItemVisitableUtil.getFieldsInvokedIn(p).get(0);

    assertTrue(f.isOwnedByOuterTypeOf(type));
  }

  public void testGetVariableInvocations() throws Exception {
    Project p = Utils.createTestRbProjectFromArray(new String[] {
        "class X {",
        "  void m() {",
        "    int var = 0;",
        "    int invocation = var;",
        "  }",
        "}"});
    BinCIType type = p.getTypeRefForName("X").getBinCIType();

    List vars = BinItemVisitableUtil.getLocalVariablesInvokedIn(type);
    assertEquals(1, vars.size());
    assertEquals("var", ((BinLocalVariable) vars.get(0)).getName());
  }

  public void testVariableIsOnParentOfType() throws Exception {
    Project p = Utils.createTestRbProjectFromArray(new String[] {
        "class X {",
        "  void m() {",
        "    int var = 0;",
        "    class Local {",
        "      int invocation = var;",
        "    }",
        "  }",
        "}"});

    BinCIType type = findLocalType(p);
    BinLocalVariable var = (BinLocalVariable)
        BinItemVisitableUtil.getLocalVariablesInvokedIn(p).get(0);

    assertTrue(var.isOwnedByOuterTypeOf(type));
  }

  public void testVariableIsNotOnParentOfType() throws Exception {
    Project p = Utils.createTestRbProjectFromArray(new String[] {
        "class X {",
        "  void m() {",
        "    int var = 0;",
        "    int invocation = var;",
        "  }",
        "}"});

    BinCIType type = findLocalType(p);
    BinLocalVariable var = (BinLocalVariable)
        BinItemVisitableUtil.getLocalVariablesInvokedIn(p).get(0);

    assertFalse(var.isOwnedByOuterTypeOf(type));
  }

  public void testCannotCreateFieldIfItShadowsOuterTypeVariableUsage() throws
      Exception {
    Project p = Utils.createTestRbProjectFromArray(new String[] {
        "class X {",
        "  void m() {",
        "    int var = 0;",
        "    class Local {",
        "      int invocation = var;",
        "    }",
        "  }",
        "}"});

    BinCIType type = findLocalType(p);

    assertFalse(type.canCreateField("var"));
  }

  public void
      testCannotCreateFieldIfItShadowsOuterTypeVariableUsageInSubclassOfLocal() throws
      Exception {
    Project p = Utils.createTestRbProjectFromArray(new String[] {
        "class X {",
        "  void m() {",
        "    int var = 0;",
        "    class Local {",
        "    }",
        "    class Sub extends Local {",
        "      int invocation = var;",
        "    }",
        "  }",
        "}"});

    BinCIType type = findLocalType(p);

    assertFalse(type.canCreateField("var"));
  }

  public void
      testCanCreateFieldIfItShadowsOuterTypeVariableUsageButHasDifferentName() throws
      Exception {
    Project p = Utils.createTestRbProjectFromArray(new String[] {
        "class X {",
        "  void m() {",
        "    int var = 0;",
        "    class Local {",
        "      int invocation = var;",
        "    }",
        "  }",
        "}"});

    BinCIType type = findLocalType(p);

    assertTrue(type.canCreateField("nonexisting"));
  }

  public void testCanCreateFieldIfItShadowsOuterTypeVariableUsageInSubclassOfLocalButHasDifferentName() throws
      Exception {
    Project p = Utils.createTestRbProjectFromArray(new String[] {
        "class X {",
        "  void m() {",
        "    int var = 0;",
        "    class Local {",
        "    }",
        "    class Sub extends Local {",
        "      int invocation = var;",
        "    }",
        "  }",
        "}"});

    BinCIType type = findLocalType(p);

    assertTrue(type.canCreateField("nonexisting"));
  }

  // -- Util methods

  private static BinCIType findLocalType(Project p) {
    final BinCIType type[] = new BinCIType[] {null};

    p.accept(new BinItemVisitor() {
      public void visit(BinCIType x) {
        if (x.getName().equals("Local")) {
          type[0] = x;
        }

        super.visit(x);
      }
    });

    return type[0];
  }
}
