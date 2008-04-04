/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.inline;

import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.RefactorItAction;
import net.sf.refactorit.ui.module.RefactorItContext;


/**
 * @author Anton Safonov
 */
public class InlineAction extends AbstractRefactorItAction {
  public static final String KEY = "refactorit.action.InlineAction";
  public static final String NAME = "Inline";

  public boolean isAvailableForType(Class type) {
    if (BinMethod.class.equals(type)
        || BinMethodInvocationExpression.class.equals(type)
        || BinVariable.class.isAssignableFrom(type)
        || BinFieldInvocationExpression.class.equals(type)
        || BinVariableUseExpression.class.equals(type)) {
      return true;
    }

    return false;
  }

  private static final RefactorItAction inlineMethodAction
      = new InlineMethodAction();
  private static final RefactorItAction inlineVariableAction
      = new InlineVariableAction();

  public String getName() {
    return NAME;
  }

  public char getMnemonic() {
    return 'I';
  }

  public boolean isReadonly() {
    return false;
  }

  public boolean isMultiTargetsSupported() {
    return false;
  }

  public String getKey() {
    return KEY;
  }

  public boolean run(final RefactorItContext context, Object inObject) {
    if (inlineVariableAction.isAvailableForType(inObject.getClass())) {
      return inlineVariableAction.run(context, inObject);
    } else {
      return inlineMethodAction.run(context, inObject);
    }
  }
}
