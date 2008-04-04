/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.promotetemptofield.ui;

import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.refactorings.NameUtil;
import net.sf.refactorit.refactorings.promotetemptofield.AllowedModifiers;
import net.sf.refactorit.refactorings.promotetemptofield.FieldInitialization;
import net.sf.refactorit.refactorings.promotetemptofield.PromoteTempToField;


/**
 *
 * @author  RISTO A
 * @author juri
 */
public class UserInput {
  private static final String INIT_METHOD_OPTION =
      "promote.temp.to.field.dialog.default.init.location";

  private BinLocalVariable var;
  private View view;
  private AllowedModifiers allowedModifiers;

  private FieldInitialization initLocation;
  private int accessModifiers = BinModifier.PRIVATE;

  private boolean okWasPressed = false;

  public UserInput(BinLocalVariable var, View view) {
    this(var, view, new AllowedModifiers());
  }

  public UserInput(BinLocalVariable var, View view,
      FieldInitialization selection) {
    this(var, view, new AllowedModifiers(), selection);
  }

  public UserInput(BinLocalVariable var, View view,
      AllowedModifiers allowedModifiers) {
    this(var, view, allowedModifiers, getSavedInitLocation());
  }

  public UserInput(BinLocalVariable var, View view,
      AllowedModifiers allowedModifiers, FieldInitialization selected) {
    this.var = var;
    this.view = view;
    this.allowedModifiers = allowedModifiers;
    this.initLocation = selected;

    view.setName(var.getName());

    view.setFinalModifierChecked(false);
    
    setFinalModifierEnablement();
    setStaticModifierEnablement();
    initStaticIsChecked();
    addInitLocations();
  }

  private void addInitLocations() {
    for (int i = 0; i < PromoteTempToField.ALL_INIT_LOCATIONS.length; i++) {
      FieldInitialization initLocation = PromoteTempToField.ALL_INIT_LOCATIONS[
          i];
      view.addInitializeLocation(initLocation,
          allowedModifiers.initializationAllowed(initLocation, var),
          initLocation == this.initLocation, initLocation.getMnemonic());
    }
  }

  private void setFinalModifierEnablement() {
    view.setFinalModifierEnabled(allowedModifiers.finalAllowed(initLocation,
        var));
  }
  
  private void setStaticModifierEnablement() {
    view.setStaticModifierEnabled(!allowedModifiers.mustBeStatic(var));
    
  }
  
  public void initStaticIsChecked() {
    view.setStaticModifierChecked(allowedModifiers.mustBeStatic(var));
  }
  
  public void setInitializeLocation(FieldInitialization initLocation) {
    this.initLocation = initLocation;
    setFinalModifierEnablement();
    setStaticModifierEnablement();
  }

  public FieldInitialization getInitLocation() {
    return initLocation;
  }

  public void setAccessModifiers(int accessModifiers) {
    this.accessModifiers = accessModifiers;
  }

  public int getAccessModifiers() {
    return accessModifiers;
  }

  public void initializeRefactoring(PromoteTempToField p, String newName,
      boolean finalModifier, boolean staticModifier) {
    if (finalModifier && allowedModifiers.finalAllowed(initLocation, var)) {
      accessModifiers |= BinModifier.FINAL;
    }
    if (staticModifier && allowedModifiers.staticAllowed( var)) {
      accessModifiers |= BinModifier.STATIC;
    }

    p.setVariable(var);
    
    p.setNewName(newName);
    p.setModifiers(accessModifiers);
    p.setFieldInitialization(initLocation);

    saveSettings();
  }

  private void saveSettings() {
    GlobalOptions.setOption(INIT_METHOD_OPTION, initLocation.getDisplayName());
    GlobalOptions.save();
  }

  private static FieldInitialization getSavedInitLocation() {
    return PromoteTempToField.getInitializationLocationForDisplayName(
        GlobalOptions.getOption(INIT_METHOD_OPTION));
  }

  public void okPressed() {
    okWasPressed = true;
  }

  public boolean wasOkPressed() {
    return okWasPressed;
  }

  public void notifyNameChanged(String name) {
    view.setOkButtonEnabled(NameUtil.isValidIdentifier(name));
  }

  public static class View {
    public void setName(String name) {}

    public void setFinalModifierEnabled(boolean b) {}
    
    public void setStaticModifierEnabled(boolean b) {}

    public void setFinalModifierChecked(boolean b) {}

    public void setStaticModifierChecked(boolean b) {}

    public void setOkButtonEnabled(boolean b) {}

    public void addInitializeLocation(FieldInitialization initLocation,
        boolean enabled, boolean selected, char mnemonic) {}
  }
}
