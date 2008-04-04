/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.pullpush;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinInterface;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.pullpush.PullPush;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.RefactorItActionUtils;
import net.sf.refactorit.ui.module.RefactorItContext;

import java.util.ArrayList;
import java.util.List;


/**
 * @author vadim
 */
public class PullPushAction extends AbstractRefactorItAction {
  public static final String KEY = "refactorit.action.PullPushAction";
  public static final String NAME = "Pull Up / Push Down";

  public String getKey() {
    return KEY;
  }

  public String getName() {
    return NAME;
  }

  public char getMnemonic() {
    return 'P';
  }

  public boolean isReadonly() {
    return false;
  }

  public boolean isMultiTargetsSupported() {
    return false;
  }

  public boolean run(RefactorItContext context, Object object) {
    Object target = RefactorItActionUtils.unwrapTarget(object);
    BinCIType nativeType = findNativeType(target);
    List selectedTargets = createMembersList(target);

    PullPush pullPush = new PullPush(context, nativeType, selectedTargets);
    RefactoringStatus status = pullPush.checkPreconditions();

    if (status.isInfoOrWarning()) {
      RitDialog.showMessageDialog(context,
          status.getAllMessages(), "Note",
          status.getJOptionMessageType());
    } else if (!status.isOk()) {
      RitDialog.showMessageDialog(context,
          status.getAllMessages(), "Problems with Pull Up / Push Down",
          status.getJOptionMessageType());
      return false;
    }

    pullPush.checkUserInput();

    PullPushDialog dialog = new PullPushDialog(pullPush.getResolver(), context);
    dialog.show();

    if (dialog.isOkPressed()) {
      status = pullPush.apply();
      if (!status.isOk()) {
        if (status.hasSomethingToShow()) {
          RitDialog.showMessageDialog(context,
              status.getAllMessages(), "Problems with Pull Up / Push Down",
              status.getJOptionMessageType());
        }
        return false;
      }
    } else {
      status.addEntry("", RefactoringStatus.CANCEL);
    }

    return status.isOk();
  }

  private List createMembersList(Object target) {
    List result = new ArrayList();

    if (target instanceof Object[]) {
      for (int i = 0; i < ((Object[]) target).length; i++) {
        Object obj = ((Object[]) target)[i];
        if (obj instanceof BinField
            || (obj instanceof BinMethod && !(obj instanceof BinConstructor))) {
          result.add(obj);
        }
      }
    } else if (target instanceof BinField
        || (target instanceof BinMethod && !(target instanceof BinConstructor))) {
      result.add(target);
    }

    return result;
  }

  private BinCIType findNativeType(Object target) {
    if (target instanceof Object[]) {
      target = ((Object[]) target)[0];
    }

    if (target instanceof BinCIType) {
      return (BinCIType) target;
    }

    if (target instanceof BinMember) {
      return ((BinMember) target).getOwner().getBinCIType();
    }

    return null;
  }

  public boolean isAvailableForType(Class type) {
    return BinClass.class.isAssignableFrom(type)
        || BinInterface.class.isAssignableFrom(type)
        || BinMethod.class.isAssignableFrom(type)
        || BinMethodInvocationExpression.class.isAssignableFrom(type)
        || BinConstructor.class.isAssignableFrom(type)
        || BinField.class.isAssignableFrom(type)
        || BinFieldInvocationExpression.class.isAssignableFrom(type);
  }
}
