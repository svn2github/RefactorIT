/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.common;

import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.refactorings.Refactoring;
import net.sf.refactorit.refactorings.promotetemptofield.PromoteTempToField;
import net.sf.refactorit.refactorings.promotetemptofield.ui.UserInputDialog;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.RefactorItContext;


public class ConvertTempToFieldAction extends AbstractRefactorItAction {
  public static final String KEY = "refactorit.convert.temp.to.field";
  public static final String NAME = "Convert Temp To Field";

  public ConvertTempToFieldAction() {}

  public String getName() {
    return NAME;
  }

  public String getKey() {
    return KEY;
  }

  public Refactoring createRefactoring(RefactorItContext context, Object object) {
    BinLocalVariable var = (BinLocalVariable) object;

    return new PromoteTempToField(context, var, var.getName(), 0,
        PromoteTempToField.DEFAULT_INITIALIZATION);
  }

  public boolean readUserInput(Refactoring refactoring) {
    return new UserInputDialog().show((PromoteTempToField) refactoring);
  }

  public boolean isMultiTargetsSupported() {
    return false;
  }

  public boolean isAvailableForType(Class type) {
    return BinLocalVariable.class == type;
  }

  public boolean isReadonly() {
    return false;
  }

  public char getMnemonic() {
    return 'F';
  }
}
