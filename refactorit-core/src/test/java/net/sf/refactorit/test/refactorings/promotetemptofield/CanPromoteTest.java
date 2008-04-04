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
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.query.ItemByNameFinder;
import net.sf.refactorit.refactorings.promotetemptofield.FieldInitialization;
import net.sf.refactorit.refactorings.promotetemptofield.PromoteTempToField;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.refactorings.NullContext;
import net.sf.refactorit.test.refactorings.RefactoringTestCase;
import net.sf.refactorit.utils.RefactorItConstants;

import junit.framework.Test;
import junit.framework.TestSuite;


// FIXME: Some more minor problems with newly created constructors:
//   * should be added *after* fields
//   * should not have a first, empty, line? should they have a linebreak after them?

/**
 *
 * @author  RISTO A
 */
public class CanPromoteTest extends RefactoringTestCase {
  public CanPromoteTest(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(CanPromoteTest.class);
  }

  public String getTemplate() {
    return "PromoteTempToField/canPromote/A_<test_name>_<in_out>.java";
  }

  public void assertWorks(String varName, String newName, int modifiers,
      FieldInitialization init) throws Exception {
    BinLocalVariable var = (BinLocalVariable) ItemByNameFinder.findVariable(
        getInProject(), varName);
    assertWorks(var, newName, modifiers, init);
  }

  private void assertWorks(BinLocalVariable var, String newName, int modifiers,
      FieldInitialization init) throws Exception {

    PromoteTempToField p = new PromoteTempToField(
        new NullContext(var.getProject()),
        var, newName, modifiers, init);
    RwRefactoringTestUtils.assertRefactoring(p, getExpectedProject(),
        var.getOwner().getProject());
  }

  private Project getInProject() throws Exception {
    return getMutableProject(getInitialProject());
  }

  public void test0() throws Exception {
    assertWorks("i", "i", BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_METHOD);
  }

  public void test0NoValue() throws Exception {
    assertWorks("i", "i", BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_METHOD);
  }

  public void test0NoValueInitInField() throws Exception {
    assertWorks("i", "i", BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_FIELD);
  }

  public void test0NoValueInitInConstructor() throws Exception {
    assertWorks("i", "i", BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_CONSTRUCTOR);
  }

  public void test1() throws Exception {
    assertWorks("i", "i", BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_FIELD);
  }

  public void test2() throws Exception {
    assertWorks("i", "i", BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_CONSTRUCTOR);
  }

  public void test3() throws Exception {
    assertWorks("i", "i", BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_CONSTRUCTOR);
  }

  public void test4() throws Exception {
    assertWorks("i", "i", BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_CONSTRUCTOR);
  }

  public void test5() throws Exception {
    assertWorks("i", "i", BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_METHOD);
  }

  public void test6() throws Exception {
    assertWorks("i", "i", BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_FIELD);
  }

  public void test7() throws Exception {
    assertWorks("i", "i", BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_FIELD);
  }

  public void test8() throws Exception {
    assertWorks("i", "i", BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_METHOD);
  }

  public void test9() throws Exception {
    if (RefactorItConstants.runNotImplementedTests) {
      assertWorks("i", "field", BinModifier.PRIVATE,
          PromoteTempToField.INITIALIZE_IN_METHOD);
    }
  }

  public void test9_field() throws Exception {
    if (RefactorItConstants.runNotImplementedTests) {
      assertWorks("i", "field", BinModifier.PRIVATE,
          PromoteTempToField.INITIALIZE_IN_FIELD);
    }
  }

  public void test10() throws Exception {
    assertWorks("i", "i", BinModifier.FINAL + BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_CONSTRUCTOR);
  }

  public void test11() throws Exception {
    assertWorks("i", "i", BinModifier.PUBLIC,
        PromoteTempToField.INITIALIZE_IN_METHOD);
  }

  public void test12() throws Exception {
    assertWorks("i", "i", BinModifier.PRIVATE + BinModifier.STATIC,
        PromoteTempToField.INITIALIZE_IN_METHOD);
  }

  public void test13() throws Exception {
    assertWorks("i", "i", BinModifier.PRIVATE + BinModifier.STATIC,
        PromoteTempToField.INITIALIZE_IN_METHOD);
  }

  public void test13NoAssignment() throws Exception {
    assertWorks("i", "i", BinModifier.PRIVATE + BinModifier.STATIC,
        PromoteTempToField.INITIALIZE_IN_METHOD);
  }

  public void test14() throws Exception {
    assertWorks("i", "i", BinModifier.PRIVATE + BinModifier.STATIC,
        PromoteTempToField.INITIALIZE_IN_METHOD);
  }

  public void test14NoValueOnLeft() throws Exception {
    assertWorks("i", "i", BinModifier.PRIVATE + BinModifier.STATIC,
        PromoteTempToField.INITIALIZE_IN_METHOD);
  }

  public void test14NoAssignment() throws Exception {
    assertWorks("i", "i", BinModifier.PRIVATE + BinModifier.STATIC,
        PromoteTempToField.INITIALIZE_IN_METHOD);
  }

  public void test15() throws Exception {
    assertWorks("i", "i", BinModifier.PRIVATE + BinModifier.STATIC,
        PromoteTempToField.INITIALIZE_IN_METHOD);
  }

  public void test15InitInField() throws Exception {
    assertWorks("i", "i", BinModifier.PRIVATE + BinModifier.STATIC,
        PromoteTempToField.INITIALIZE_IN_FIELD);
  }

  public void test15InitInConstructor() throws Exception {
    assertWorks("i", "i", BinModifier.PRIVATE + BinModifier.STATIC,
        PromoteTempToField.INITIALIZE_IN_CONSTRUCTOR);
  }

  public void test15Fqn() throws Exception {
    assertWorks("i", "i", BinModifier.PRIVATE + BinModifier.STATIC,
        PromoteTempToField.INITIALIZE_IN_METHOD);
  }

  public void test16() throws Exception {
    assertWorks("i", "i", BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_CONSTRUCTOR);
  }

  public void test17() throws Exception {
    assertWorks("i", "i", BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_METHOD);
  }

  public void testFqn() throws Exception {
    assertWorks("l", "l", BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_FIELD);
  }

  public void testFqnMethod() throws Exception {
    assertWorks("l", "l", BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_METHOD);
  }

  public void testFqnConstructor() throws Exception {
    assertWorks("l", "l", BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_CONSTRUCTOR);
  }

  public void testIndentInInnerBlock() throws Exception {
    assertWorks("i", "i", BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_METHOD);
  }

  public void testTempUsageWithMethodInit() throws Exception {
    assertWorks("i", "i", BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_METHOD);
  }

  public void testOuterTempUsage() throws Exception {
    assertWorks("i", "i", BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_FIELD);
  }

  public void testTwoFieldLocations() throws Exception {
    assertWorks("i", "i", BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_FIELD);
  }

  public void testShadingOfNotUsedField() throws Exception {
    BinType type = getInProject().getTypeRefForName("p.A").getBinType();
    BinLocalVariable var
        = (BinLocalVariable) ItemByNameFinder.findVariable(type, "i");

    assertWorks(var, var.getName(), BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_FIELD);
  }

  public void testThisPrefixInConstructor() throws Exception {
    BinCIType type = getInProject().getTypeRefForName("p.A").getBinCIType();
    BinLocalVariable var
        = (BinLocalVariable) ItemByNameFinder.findVariable(
        type.getDeclaredMethods()[0], "i");

    assertWorks(var, var.getName(), BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_CONSTRUCTOR);
  }

  public void testTempInConstructor_constructor() throws Exception {
    assertWorks("i", "i", BinModifier.PACKAGE_PRIVATE,
        PromoteTempToField.INITIALIZE_IN_CONSTRUCTOR);
  }

  public void testTempInConstructor_field() throws Exception {
    assertWorks("i", "i", BinModifier.PACKAGE_PRIVATE,
        PromoteTempToField.INITIALIZE_IN_FIELD);
  }

  public void testTempInConstructor_method() throws Exception {
    assertWorks("i", "i", BinModifier.PACKAGE_PRIVATE,
        PromoteTempToField.INITIALIZE_IN_METHOD);
  }

  public void testRename() throws Exception {
    assertWorks("i", "newName", BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_FIELD);
  }

  public void testRenameWithUsages() throws Exception {
    assertWorks("i", "newName", BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_FIELD);
  }

  public void testRenameWithInitInMethod() throws Exception {
    assertWorks("i", "newName", BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_METHOD);
  }

  public void testMethodInitWithLocalUsages() throws Exception {
    assertWorks("i", "i", BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_METHOD);
  }

  public void testFinalTempWithMethodInit() throws Exception {
    assertWorks("i", "i", BinModifier.PACKAGE_PRIVATE,
        PromoteTempToField.INITIALIZE_IN_METHOD);
  }

  public void testArraysInitedInMethod() throws Exception {
    assertWorks("i", "i", BinModifier.PACKAGE_PRIVATE,
        PromoteTempToField.INITIALIZE_IN_METHOD);
  }

  public void testArraysInitedInMethod2() throws Exception {
    assertWorks("i", "i", BinModifier.PACKAGE_PRIVATE,
        PromoteTempToField.INITIALIZE_IN_METHOD);
  }

  public void testArraysInitedInMethod2MultivarDeclaration() throws Exception {
    assertWorks("i", "i", BinModifier.PACKAGE_PRIVATE,
        PromoteTempToField.INITIALIZE_IN_METHOD);
  }

  public void testMultivarWithFinalModifier() throws Exception {
    assertWorks("i", "i", BinModifier.PACKAGE_PRIVATE,
        PromoteTempToField.INITIALIZE_IN_METHOD);
  }

  public void testAutomaticStaticModifier() throws Exception {
    assertWorks("i", "i", BinModifier.PACKAGE_PRIVATE,
        PromoteTempToField.INITIALIZE_IN_METHOD);
  }

  public void testManualStaticModifier() throws Exception {
    assertWorks("i","i", BinModifier.PACKAGE_PRIVATE|BinModifier.STATIC,
            PromoteTempToField.INITIALIZE_IN_METHOD);
  }

  public void testLongVarName() throws Exception {
    assertWorks("array", "array", BinModifier.PACKAGE_PRIVATE,
        PromoteTempToField.INITIALIZE_IN_METHOD);
  }

  public void testNoWhitespace() throws Exception {
    assertWorks("i", "i", BinModifier.PACKAGE_PRIVATE,
        PromoteTempToField.INITIALIZE_IN_FIELD);
  }

  public void testGuardedBlocks() throws Exception {
    assertWorks("i", "i", BinModifier.PACKAGE_PRIVATE,
        PromoteTempToField.INITIALIZE_IN_FIELD);
  }

  public void testInitializeInMethodRim679_1() throws Exception {
    assertWorks("price", "cost", BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_METHOD);
  }

  public void testInitializeInMethodRim679_2() throws Exception {
    assertWorks("price", "cost", BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_METHOD);
  }

  /** Skipped in Eclipse */
  /*public void test18() throws Exception{
   assertWorks("i", "i", BinModifier.PRIVATE, PromoteTempToField.INITIALIZE_IN_CONSTRUCTOR);
    }*/
}
