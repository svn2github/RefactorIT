/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.inline;

import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.refactorings.Refactoring;
import net.sf.refactorit.refactorings.inlinevariable.InlineVariable;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.RefactorItContext;


/**
 * @author Risto
 */
public class InlineVariableAction extends AbstractRefactorItAction {
  public static final String KEY = "refactorit.action.InlineVariableAction";
  public static final String NAME = "Inline Variable";

  public String getName() {
    return NAME;
  }

  public char getMnemonic() {
    return 'V';
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

  public boolean isAvailableForType(Class type) {
    return BinVariable.class.isAssignableFrom(type)
        || BinVariableUseExpression.class.equals(type)
        || BinFieldInvocationExpression.class.equals(type);
  }

  public Refactoring createRefactoring(RefactorItContext context, Object object) {
    return new InlineVariable(context, (BinVariable) object);
  }
}
