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
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.movemember.MoveMember;
import net.sf.refactorit.ui.JProgressDialog;
import net.sf.refactorit.ui.SearchingInterruptedException;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.RefactorItActionUtils;
import net.sf.refactorit.ui.module.RefactorItContext;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Anton Safonov, Vadim Hahhulin
 */
public class MoveMemberAction extends AbstractRefactorItAction {
  public static final String KEY = "refactorit.action.MoveMemberAction";
  public static final String NAME = "Move Method/Field";

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
    final BinCIType nativeType = findNativeType(target);
    final List selectedTargets = createMembersList(target);

    final MoveMember[] moverA = new MoveMember[] {null};
    final RefactoringStatus[] result = new RefactoringStatus[] {null};
    try {
      JProgressDialog.run(context, new Runnable() {
        public void run() {
//          CFlowContext.remove(ProgressListener.class.getName());

          moverA[0] = new MoveMember(context, nativeType, selectedTargets);
          result[0] = moverA[0].checkPreconditions();
        }
      }


      , true);
    } catch (SearchingInterruptedException ex) {
      return false;
    }

    RefactoringStatus status = result[0];
    MoveMember mover = moverA[0];

    if (status.isInfoOrWarning()) {
      RitDialog.showMessageDialog(context,
          status.getAllMessages(), "Note",
          status.getJOptionMessageType());
    } else if (!status.isOk()) {
      RitDialog.showMessageDialog(context,
          status.getAllMessages(), "Problems with move member",
          status.getJOptionMessageType());
      return false;
    }

    MoveMemberDialog dialog = new MoveMemberDialog(mover.getResolver(), context);
    dialog.show();

    if (dialog.isOkPressed()) {
      status = mover.apply();
      if (!status.isOk()) {
        if (status.hasSomethingToShow()) {
          RitDialog.showMessageDialog(context,
              status.getAllMessages(), "Problems with move member",
              status.getJOptionMessageType());
        }
        return false;
      }
    } else {
      status.addEntry("", RefactoringStatus.CANCEL);
    }

    return status.isOk();
  }

  private BinCIType findNativeType(Object target) {
    if (target instanceof Object[]) {
      target = ((Object[]) target)[0];
    }

    /*if (target instanceof BinCIType) {
      return (BinCIType) target;
    }*/

    if (target instanceof BinMember) {
      return ((BinMember) target).getOwner().getBinCIType();
    }

    return null;
  }

  private List createMembersList(Object target) {
    List result = new ArrayList();

    if (target instanceof Object[]) {
      for (int i = 0; i < ((Object[]) target).length; i++) {
        Object obj = ((Object[]) target)[i];
        if (isAMoveableMember(obj)) {
          result.add(obj);
        }
      }
    } else if (isAMoveableMember(target)) {
      result.add(target);
    }

    return result;
  }

  private boolean isAMoveableMember(final Object targetItem) {
    return targetItem instanceof BinField
        || (targetItem instanceof BinMethod
            && !(targetItem instanceof BinConstructor))
        || (targetItem instanceof BinCIType
            && ((BinCIType) targetItem).isInnerType());
  }

  /**
   * @see net.sf.refactorit.ui.module.RefactorItAction#isAvailableForType(java.lang.Class)
   */
  public boolean isAvailableForType(Class type) {
    throw new UnsupportedOperationException("method not implemented yet");
    //return false;
  }
}
