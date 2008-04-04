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
import net.sf.refactorit.refactorings.promotetemptofield.InitializeInMethod;
import net.sf.refactorit.refactorings.promotetemptofield.PromoteTempToField;
import net.sf.refactorit.refactorings.promotetemptofield.ui.UserInput;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.test.refactorings.NullContext;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 *
 * @author  RISTO A
 * @author  juri
 */
public class UserInputTest extends TestCase {
  StringBuffer log;

  private BinLocalVariable var;

  public UserInputTest(String name) {super(name);
  }

  public static Test suite() {
    return new TestSuite(UserInputTest.class);
  }

  public void setUp() throws Exception {
    Project p = Utils.createTestRbProjectFromArray(new String[] {
        "class X {",
        "  void m() {",
        "    class Local{}",
        "    Object i = new Local();",
        "  }",
        "}"
    });

    var = (BinLocalVariable) ItemByNameFinder.findVariable(p, "i");

    log = new StringBuffer();
  }

  public void testInitialSetName() {
    new UserInput(var, new UserInput.View() {
      public void setName(String name) {
        log.append("setName " + name);
      }
    });

    assertEquals("setName i", log.toString());
  }

  public void testFinalModifierEnabledAtFirst() {
    new UserInput(var,
        new UserInput.View() {
      public void setFinalModifierEnabled(boolean b) {
        log.append("setFinalModifierEnabled " + b);
      }
    }


    ,
        new AllowedModifiers() {
      public boolean finalAllowed(FieldInitialization initLocation,
          BinLocalVariable var) {
        return true;
      }
    });

    assertEquals("setFinalModifierEnabled true", log.toString());
  }

  public void testFinalModifierNotCheckedAtFirst() {
    new UserInput(var, new UserInput.View() {
      public void setFinalModifierChecked(boolean b) {
        log.append("setFinalModifierChecked " + b);
      }
    });

    assertEquals("setFinalModifierChecked false", log.toString());
  }

  public void testAddInitializeLocation() {
    new UserInput(var, new UserInput.View() {
      public void addInitializeLocation(
          FieldInitialization i, boolean enabled, boolean selected,
          char mnemonic
          ) {
        log.append(i + " ");
      }
    });
    assertEquals(
        "InitializeInField " +
        "InitializeInMethod " +
        "InitializeInConstructor ", log.toString());
  }

  public void testInitialEnablementOfAMethod() {
    new UserInput(var,
        new UserInput.View() {
      public void addInitializeLocation(
          FieldInitialization initLocation, boolean enabled, boolean selected,
          char mnemonic
          ) {
        log.append("addInitializeLocation " + initLocation + " " + enabled
            + " ");
      }
    }


    ,
        new AllowedModifiers() {
      public boolean initializationAllowed(FieldInitialization initLocation,
          BinLocalVariable var) {
        return initLocation instanceof InitializeInMethod;
      }
    });

    assertEquals(
        "addInitializeLocation InitializeInField false " +
        "addInitializeLocation InitializeInMethod true " +
        "addInitializeLocation InitializeInConstructor false ", log.toString());
  }

  public void testSelection() {
    new UserInput(var, new UserInput.View() {
      public void addInitializeLocation(FieldInitialization i, boolean enabled,
          boolean selected, char mnemonic) {
        log.append(i + " " + selected + " ");
      }
    }


    , PromoteTempToField.INITIALIZE_IN_FIELD);

    assertEquals(
        "InitializeInField true " +
        "InitializeInMethod false " +
        "InitializeInConstructor false ", log.toString());
  }

  public void testChangingInitMethods() {
    UserInput i = new UserInput(var, new UserInput.View() {
      public void setFinalModifierEnabled(boolean b) {
        log.append("setFinalModifierEnabled " + b);
      }
    }


    , PromoteTempToField.INITIALIZE_IN_FIELD);

    assertEquals(PromoteTempToField.INITIALIZE_IN_FIELD, i.getInitLocation());

    log = new StringBuffer();
    i.setInitializeLocation(PromoteTempToField.INITIALIZE_IN_METHOD);
    assertEquals(PromoteTempToField.INITIALIZE_IN_METHOD, i.getInitLocation());
    assertEquals("setFinalModifierEnabled false", log.toString());
  }

  public void testCreationOfRefactoringInstance() {
    PromoteTempToField p = new PromoteTempToField(new NullContext(var.getOwner().
        getProject()),
        var, var.getName(), 0, PromoteTempToField.INITIALIZE_IN_FIELD);

    assertEquals(var.getName(), p.getNewName());
    assertEquals(0, p.getModifiers());
    assertEquals(PromoteTempToField.INITIALIZE_IN_FIELD,
        p.getInitializeLocation());
  }

  public void testCreationOfRefactoringInstanceInUserInput() {
    UserInput i = new UserInput(var, new UserInput.View());
    i.setInitializeLocation(PromoteTempToField.INITIALIZE_IN_CONSTRUCTOR);

    i.setAccessModifiers(BinModifier.PRIVATE);
    PromoteTempToField p = new PromoteTempToField(
        new NullContext(var.getOwner().getProject()), var, "", 0,
        PromoteTempToField.DEFAULT_INITIALIZATION);
    i.initializeRefactoring(p, "someNewName", false,false);

    assertEquals("someNewName", p.getNewName());
    assertTrue(BinModifier.hasFlag(p.getModifiers(), BinModifier.PRIVATE));
    assertEquals(PromoteTempToField.INITIALIZE_IN_CONSTRUCTOR,
        p.getInitializeLocation());
  }

  public void testNoFinalFlagOnRefactoringInstance() {
    UserInput i = new UserInput(var, new UserInput.View());

    PromoteTempToField p = new PromoteTempToField(
        new NullContext(var.getOwner().getProject()), var, "", 0,
        PromoteTempToField.DEFAULT_INITIALIZATION);
    i.initializeRefactoring(p, var.getName(), false,false);
    assertFalse(BinModifier.hasFlag(p.getModifiers(), BinModifier.FINAL));
  }

  public void testFinalFlagOnRefactoringInstance() {
    UserInput i = new UserInput(var, new UserInput.View());

    i.setAccessModifiers(BinModifier.PUBLIC);
    PromoteTempToField p = new PromoteTempToField(
        new NullContext(var.getOwner().getProject()), var, "", 0,
        PromoteTempToField.DEFAULT_INITIALIZATION);
    i.initializeRefactoring(p, var.getName(), true, false);
    assertTrue(BinModifier.hasFlag(p.getModifiers(), BinModifier.FINAL));

    assertTrue(BinModifier.hasFlag(p.getModifiers(), BinModifier.PUBLIC));
  }

  public void testFinalSelectedButNotAllowedOnRefactoringInstance() {
    UserInput i = new UserInput(var, new UserInput.View(), new AllowedModifiers() {
      public boolean finalAllowed(FieldInitialization fi, BinLocalVariable var) {
        return false;
      }
    });

    PromoteTempToField p = new PromoteTempToField(
        new NullContext(var.getOwner().getProject()), var, "", 0,
        PromoteTempToField.DEFAULT_INITIALIZATION);
    i.initializeRefactoring(p, var.getName(), true, false);
    assertFalse(BinModifier.hasFlag(p.getModifiers(), BinModifier.FINAL));
  }

  public void testOkPressed() {
    UserInput i = new UserInput(var, new UserInput.View());

    assertFalse(i.wasOkPressed());
    i.okPressed();
    assertTrue(i.wasOkPressed());
  }

  public void testOkButtonDisabling() {
    UserInput i = new UserInput(var, new UserInput.View() {
      public void setOkButtonEnabled(boolean b) {
        log = new StringBuffer("setOkButtonEnabled " + b);
      }
    });

    i.notifyNameChanged("newField");
    assertEquals("setOkButtonEnabled true", log.toString());

    i.notifyNameChanged("new Field");
    assertEquals("setOkButtonEnabled false", log.toString());
  }

  public void testSettingAccessModifiers() {
    UserInput i = new UserInput(var, new UserInput.View());

    i.setAccessModifiers(BinModifier.PRIVATE);
    assertEquals(BinModifier.PRIVATE, i.getAccessModifiers());
  }

  public void testRememberingInitMethod() {
    UserInput i;
    PromoteTempToField p = new PromoteTempToField(
        new NullContext(var.getOwner().getProject()), var, "", 0,
        PromoteTempToField.DEFAULT_INITIALIZATION);

    i = new UserInput(var, new UserInput.View());
    i.setInitializeLocation(PromoteTempToField.INITIALIZE_IN_FIELD);
    i.initializeRefactoring(p, "newName", false, false);

    i = new UserInput(var, new UserInput.View());
    assertEquals(PromoteTempToField.INITIALIZE_IN_FIELD, i.getInitLocation());

    i = new UserInput(var, new UserInput.View());
    i.setInitializeLocation(PromoteTempToField.INITIALIZE_IN_METHOD);
    i.initializeRefactoring(p, "newName", false, false);

    i = new UserInput(var, new UserInput.View());
    assertEquals(PromoteTempToField.INITIALIZE_IN_METHOD, i.getInitLocation());
  }
}
