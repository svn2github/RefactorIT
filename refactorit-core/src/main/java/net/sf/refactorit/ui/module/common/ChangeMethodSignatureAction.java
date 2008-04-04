/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.common;

import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.refactorings.Refactoring;
import net.sf.refactorit.refactorings.changesignature.ChangeMethodSignatureRefactoring;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.RefactorItContext;


/**
 * @author Igor Malinin
 */
public class ChangeMethodSignatureAction extends AbstractRefactorItAction {
  public static final String KEY = "refactorit.changeMethodSignature";
  public static final String NAME = "Change Method Signature";

  public String getKey() {
    return KEY;
  }

  public String getName() {
    return NAME;
  }

  public boolean isReadonly() {
    return false;
  }

  public boolean isMultiTargetsSupported() {
    return false;
  }

  public Refactoring createRefactoring(RefactorItContext context, Object object) {
    return new ChangeMethodSignatureRefactoring((BinMethod) object);
  }

  public boolean readUserInput(Refactoring refactoring) {
    ChangeMethodSignatureDialog dialog = new ChangeMethodSignatureDialog(
        ((ChangeMethodSignatureRefactoring) refactoring));
    dialog.show();
    return dialog.isOkPressed();
  }

  public boolean isAvailableForType(Class type) {
    return BinMethod.class.isAssignableFrom(type)
        || BinMethodInvocationExpression.class.isAssignableFrom(type);
  }

  public char getMnemonic() {
    return 'C';
  }
}
