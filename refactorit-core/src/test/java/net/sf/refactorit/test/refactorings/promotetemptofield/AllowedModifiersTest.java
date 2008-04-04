/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings.promotetemptofield;

import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.query.ItemByNameFinder;
import net.sf.refactorit.refactorings.promotetemptofield.AllowedModifiers;
import net.sf.refactorit.refactorings.promotetemptofield.FieldInitialization;
import net.sf.refactorit.refactorings.promotetemptofield.PromoteTempToField;
import net.sf.refactorit.test.refactorings.RefactoringTestCase;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 * @author  RISTO A
 */
public class AllowedModifiersTest extends RefactoringTestCase {
  public AllowedModifiersTest(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(AllowedModifiersTest.class);
  }

  public String getTemplate() {
    return "PromoteTempToField/allowedModifiers/A_<test_name>.java";
  }

  private void assertAllowedModifiers(boolean expectedCanEnableSettingFinal,
      boolean expectedCanEnableSettingStatic,
      boolean expectedCanEnableInitInField,
      boolean expectedCanEnableInitInMethod,
      boolean expectedCanEnableInitInConstructors) throws Exception {

    assertAllowedModifiers(PromoteTempToField.INITIALIZE_IN_METHOD,
        BinModifier.PRIVATE,
        expectedCanEnableSettingFinal,
        expectedCanEnableSettingStatic, expectedCanEnableInitInField,
        expectedCanEnableInitInMethod, expectedCanEnableInitInConstructors);
  }

  private void assertAllowedModifiers(FieldInitialization initializeMethod,
      int accessModifier,
      boolean expectedCanEnableSettingFinal,
      boolean expectedCanEnableSettingStatic,
      boolean expectedCanEnableInitInField,
      boolean expectedCanEnableInitInMethod,
      boolean expectedCanEnableInitInConstructors) throws Exception {

    Project inProject = getInitialProject();
    inProject.getProjectLoader().build();

    BinLocalVariable var = (BinLocalVariable) ItemByNameFinder.findVariable(inProject,
        "i");
    AllowedModifiers m = new AllowedModifiers();

    assertEquals(expectedCanEnableSettingFinal, m.finalAllowed(initializeMethod,
        var));
    assertEquals(expectedCanEnableSettingStatic, m.mustBeStatic(var));
    assertEquals(expectedCanEnableInitInField,
        m.initializationAllowed(PromoteTempToField.INITIALIZE_IN_FIELD, var));
    assertEquals(expectedCanEnableInitInMethod,
        m.initializationAllowed(PromoteTempToField.INITIALIZE_IN_METHOD, var));
    assertEquals(expectedCanEnableInitInConstructors,
        m.initializationAllowed(PromoteTempToField.INITIALIZE_IN_CONSTRUCTOR,
        var));
  }

  public void testEnablement0() throws Exception {
    boolean expectedCanEnableInitInConstructors = true;
    boolean expectedCanEnableInitInMethod = true;
    boolean expectedCanEnableInitInField = true;
    boolean expectedCanEnableSettingStatic = false; // true for Eclipse
    boolean expectedCanEnableSettingFinal = true;

    FieldInitialization initializeIn = PromoteTempToField.INITIALIZE_IN_FIELD;
    int accessModifier = BinModifier.PRIVATE;

    assertAllowedModifiers(initializeIn, accessModifier,
        expectedCanEnableSettingFinal, expectedCanEnableSettingStatic,
        expectedCanEnableInitInField, expectedCanEnableInitInMethod,
        expectedCanEnableInitInConstructors);
  }

  // No need for this -- our architechture is different
  /*public void testEnablement1() throws Exception{
     boolean expectedCanEnableInitInConstructors	= false;
     boolean expectedCanEnableInitInMethod			= false;
     boolean expectedCanEnableInitInField			= false;
     boolean expectedCanEnableSettingStatic			= true;
     boolean expectedCanEnableSettingFinal			= false;
   assertAllowedModifiers(expectedCanEnableSettingFinal, expectedCanEnableSettingStatic, expectedCanEnableInitInField, expectedCanEnableInitInMethod, expectedCanEnableInitInConstructors);
    }

    public void testEnablement2() throws Exception{
     boolean expectedCanEnableInitInConstructors	= false;
     boolean expectedCanEnableInitInMethod			= false;
     boolean expectedCanEnableInitInField			= false;
     boolean expectedCanEnableSettingStatic			= true;
     boolean expectedCanEnableSettingFinal			= false;
   assertAllowedModifiers(expectedCanEnableSettingFinal, expectedCanEnableSettingStatic, expectedCanEnableInitInField, expectedCanEnableInitInMethod, expectedCanEnableInitInConstructors);
    }*/

  public void testEnablement3() throws Exception {
    boolean expectedCanEnableInitInConstructors = true;
    boolean expectedCanEnableInitInMethod = true;
    boolean expectedCanEnableInitInField = true;
    boolean expectedCanEnableSettingStatic = false; // true for Eclipse
    boolean expectedCanEnableSettingFinal = false;
    assertAllowedModifiers(expectedCanEnableSettingFinal,
        expectedCanEnableSettingStatic, expectedCanEnableInitInField,
        expectedCanEnableInitInMethod, expectedCanEnableInitInConstructors);
  }

  /** I don't know why this is needed; will not implement this one right now */
  /*public void testEnablement4() throws Exception{
     boolean expectedCanEnableInitInConstructors	= false;
     boolean expectedCanEnableInitInMethod			= true;
     boolean expectedCanEnableInitInField			= true;
     boolean expectedCanEnableSettingStatic			= true;
     boolean expectedCanEnableSettingFinal			= true;

     FieldInitialization initializeIn = PromoteTempToField.INITIALIZE_IN_FIELD;
     int accessModifier= BinModifier.PRIVATE;

   assertAllowedModifiers(initializeIn, accessModifier,
      expectedCanEnableSettingFinal, expectedCanEnableSettingStatic, expectedCanEnableInitInField, expectedCanEnableInitInMethod, expectedCanEnableInitInConstructors);
    }*/

  public void testEnablement5() throws Exception {
    boolean expectedCanEnableInitInConstructors = false;
    boolean expectedCanEnableInitInMethod = true;
    boolean expectedCanEnableInitInField = true;
    boolean expectedCanEnableSettingStatic = false; // true for Eclipse
    boolean expectedCanEnableSettingFinal = true;

    FieldInitialization initializeIn = PromoteTempToField.INITIALIZE_IN_FIELD;
    int accessModifier = BinModifier.PRIVATE;

    assertAllowedModifiers(initializeIn, accessModifier,
        expectedCanEnableSettingFinal, expectedCanEnableSettingStatic,
        expectedCanEnableInitInField, expectedCanEnableInitInMethod,
        expectedCanEnableInitInConstructors);
  }

  public void testEnablement6() throws Exception {
    boolean expectedCanEnableInitInConstructors = false; // true for Eclipse (!?)
    boolean expectedCanEnableInitInMethod = true;
    boolean expectedCanEnableInitInField = true;
    boolean expectedCanEnableSettingStatic = false; // true for Eclipse
    boolean expectedCanEnableSettingFinal = false;
    assertAllowedModifiers(expectedCanEnableSettingFinal,
        expectedCanEnableSettingStatic, expectedCanEnableInitInField,
        expectedCanEnableInitInMethod, expectedCanEnableInitInConstructors);
  }

  public void testEnablement7() throws Exception {
    boolean expectedCanEnableInitInConstructors = false;
    boolean expectedCanEnableInitInMethod = true;
    boolean expectedCanEnableInitInField = true;
    boolean expectedCanEnableSettingStatic = true; // false for Eclipse
    boolean expectedCanEnableSettingFinal = false;
    assertAllowedModifiers(expectedCanEnableSettingFinal,
        expectedCanEnableSettingStatic, expectedCanEnableInitInField,
        expectedCanEnableInitInMethod, expectedCanEnableInitInConstructors);
  }

  public void testEnablement8() throws Exception {
    boolean expectedCanEnableInitInConstructors = true;
    boolean expectedCanEnableInitInMethod = true;
    boolean expectedCanEnableInitInField = true;
    boolean expectedCanEnableSettingStatic = false;
    boolean expectedCanEnableSettingFinal = true;

    FieldInitialization initializeIn = PromoteTempToField.
        INITIALIZE_IN_CONSTRUCTOR;
    int accessModifier = BinModifier.PRIVATE;

    assertAllowedModifiers(initializeIn, accessModifier,
        expectedCanEnableSettingFinal, expectedCanEnableSettingStatic,
        expectedCanEnableInitInField, expectedCanEnableInitInMethod,
        expectedCanEnableInitInConstructors);
  }

  /** I don't know why this is needed; will not implement this one right now */
  /*public void testEnablement9() throws Exception{
     boolean expectedCanEnableInitInConstructors	= false;
     boolean expectedCanEnableInitInMethod			= true;
     boolean expectedCanEnableInitInField			= true;
     boolean expectedCanEnableSettingStatic			= true;
     boolean expectedCanEnableSettingFinal			= false;

   FieldInitialization initializeIn = PromoteTempToField.INITIALIZE_IN_METHOD;
     int accessModifier= BinModifier.PRIVATE;

   assertAllowedModifiers(initializeIn, accessModifier,
      expectedCanEnableSettingFinal, expectedCanEnableSettingStatic, expectedCanEnableInitInField, expectedCanEnableInitInMethod, expectedCanEnableInitInConstructors);
    }*/
}
