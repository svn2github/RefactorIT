/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.gotomodule.actions;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.statements.BinLabeledStatement;
import net.sf.refactorit.classmodel.statements.BinThrowStatement;


/**
 * @author Anton Safonov
 */
public class GotoAction extends GoToModuleAction {
  public static final String KEY = "refactorit.action.GoToAction";

  public boolean isAvailableForType(Class type) {
    if (BinVariable.class.isAssignableFrom(type)
        || BinFieldInvocationExpression.class.isAssignableFrom(type)) {
      return true;
    } else if (BinCIType.class.isAssignableFrom(type)
        || BinMethod.Throws.class.equals(type)
        || BinThrowStatement.class.equals(type)
        || BinMethod.class.isAssignableFrom(type)
        || BinMethodInvocationExpression.class.isAssignableFrom(type)
        || BinLabeledStatement.class.equals(type)) {
      return true;
    }
    return false;
  }

  public boolean isPreprocessedSourcesSupported(Class cl) {
    if (!BinItem.class.isAssignableFrom(cl)) {
      return false;
    }
    return true;
  }

  public String getName() {
    return "Go To ...";
  }

  public String getKey() {
    return KEY;
  }

  public boolean isReadonly() {
    return true;
  }

  public Object unwrapTarget(Object target) {
    target = super.unwrapTarget(target);

    if (target instanceof BinConstructor) {
      target = ((BinConstructor) target).getOwner().getBinCIType();
    }

    if (target instanceof BinItem) {
      return target;
    }

    return null;
  }
}
