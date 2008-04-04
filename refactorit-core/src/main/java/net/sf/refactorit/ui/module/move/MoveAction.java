/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.move;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.statements.BinThrowStatement;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.RefactorItAction;
import net.sf.refactorit.ui.module.RefactorItActionUtils;
import net.sf.refactorit.ui.module.RefactorItContext;

import javax.swing.JOptionPane;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Anton Safonov
 */
public class MoveAction extends AbstractRefactorItAction {
  public static final String KEY = "refactorit.action.MoveAction";
  public static final String NAME = "Move";

  private static final RefactorItAction somethingElse = new MoveTypeAction();
  private static final RefactorItAction moveMemberAction = new MoveMemberAction();

  public boolean isReadonly() {
    return false;
  }

  public boolean isMultiTargetsSupported() {
    return true;
  }

  public String getName() {
    return NAME;
  }

  public char getMnemonic() {
    return 'M';
  }

  public String getKey() {
    return KEY;
  }

  public boolean run(final RefactorItContext context, final Object object) {
    Object target = RefactorItActionUtils.unwrapTarget(object);
    Object members = extractMembers(target);

    if (members != null) {
      return moveMemberAction.run(context, members);
    } else if (isInnerClass(target)){
      return promtUserAndRunSelectedAction(context, (BinCIType) target);
    } else {
      return somethingElse.run(context, target);
    }
  }

  private Object extractMembers(final Object target) {
    if (target instanceof Object[]) {
      List mems = new ArrayList();
      for (int i = 0; i < ((Object[]) target).length; i++) {
        if (isMember(((Object[]) target)[i])) {
          mems.add(((Object[]) target)[i]);
        }
      }
      if (mems.size() > 0) {
        return mems.toArray(new Object[mems.size()]);
      }
    } else if (isMember(target)) {
      return target;
    }

    return null;
  }

  private boolean isMember(final Object object) {
    return object instanceof BinField
        || (object instanceof BinMethod && !(object instanceof BinConstructor));
  }

  public boolean isAvailableForType(Class type) {
    return BinMethod.class.isAssignableFrom(type)
        || BinMethodInvocationExpression.class.equals(type)
        || BinField.class.equals(type)
        || BinFieldInvocationExpression.class.equals(type)
        || BinCIType.class.isAssignableFrom(type)
        || BinMethod.Throws.class.equals(type)
        || BinThrowStatement.class.equals(type);
  }

  private boolean isInnerClass(Object target) {
    return target instanceof BinCIType
        && ((BinCIType) target).isInnerType()
        && !((BinCIType) target).isLocal();
  }

  public boolean promtUserAndRunSelectedAction(RefactorItContext context,
      BinCIType target) {
    Object[] options = { "Extract type", "Move into other class/interface" };
    final int result = RitDialog.showOptionDialog(context,
        "'" + target.getName() + "' is an inner class.\nPlease, select which action You would like to perform.",
        "RefactorIT Question",
        JOptionPane.DEFAULT_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        options, options[0]);
    if(result == 0) { // will extract type
      return somethingElse.run(context, target);
    } else if (result == 1) { // will move as a member
      return moveMemberAction.run(context, new Object[] { target });
    } else {
      return false;
    }
  }
}
