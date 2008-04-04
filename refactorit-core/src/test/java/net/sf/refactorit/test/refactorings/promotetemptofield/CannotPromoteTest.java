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
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.query.ItemByNameFinder;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.promotetemptofield.FieldInitialization;
import net.sf.refactorit.refactorings.promotetemptofield.PromoteTempToField;
import net.sf.refactorit.test.refactorings.NullContext;
import net.sf.refactorit.test.refactorings.RefactoringTestCase;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 * @author  RISTO A
 */
public class CannotPromoteTest extends RefactoringTestCase {
  public CannotPromoteTest(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(CannotPromoteTest.class);
  }

  public String getTemplate() {
    return "PromoteTempToField/cannotPromote/A_<test_name>.java";
  }

  public void assertFails(String varName, int modifiers,
      FieldInitialization init) throws Exception {
    BinLocalVariable var
        = (BinLocalVariable) ItemByNameFinder.findVariable(getInProject(), varName);
    assertFails(var, modifiers, init);
  }

  private void assertFails(final BinLocalVariable var, final int modifiers,
      final FieldInitialization init) {
    PromoteTempToField ref = new PromoteTempToField(
        new NullContext(
        var.getOwner().getProject()), var, var.getName(),
        modifiers, init);

    RefactoringStatus status =
      ref.apply();

    assertTrue(status.isErrorOrFatal());
  }

  private Project getInProject() throws Exception {
    return getMutableProject(getInitialProject());
  }

  public void testFail0() throws Exception {
    assertFails("i", BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_METHOD);
  }

  public void testFail1() throws Exception {
    assertFails("e", BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_METHOD);
  }

  public void testFail2() throws Exception {
    assertFails("l", BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_METHOD);
  }

  public void testFail2InitInField() throws Exception {
    assertFails("l", BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_FIELD);
  }

  public void testFail2InitInConstructor() throws Exception {
    assertFails("l", BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_CONSTRUCTOR);
  }

  public void testFail3() throws Exception {
    assertFails("l", BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_CONSTRUCTOR);
  }

  // Test 4 skipped -- that case works for us (see CanPromoteTest.testFail4)

  public void testFail5() throws Exception {
    BinCIType type = getInProject().getTypeRefForName("p.A").getBinCIType();
    BinLocalVariable var = (BinLocalVariable) ItemByNameFinder.findVariable(
        type.getDeclaredMethods()[0], "i");
    assertFails(var, BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_METHOD);
  }

  public void testFail5Supertype() throws Exception {
    BinCIType type = getInProject().getTypeRefForName("p.A").getBinCIType();
    BinLocalVariable var
        = (BinLocalVariable) ItemByNameFinder.findVariable(type, "i");
    assertFails(var, BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_METHOD);
  }

  public void testFail6() throws Exception {
    assertFails("i", BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_CONSTRUCTOR);
  }
  
  public void testFail7ExtendedFor() throws Exception {
    assertFails("s", BinModifier.PRIVATE,
        PromoteTempToField.INITIALIZE_IN_METHOD);
  }
}
