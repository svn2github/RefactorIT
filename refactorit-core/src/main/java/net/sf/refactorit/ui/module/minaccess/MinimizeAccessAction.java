/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.minaccess;



import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinInterface;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.CFlowContext;
import net.sf.refactorit.common.util.ProgressListener;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.minaccess.MinimizeAccess;
import net.sf.refactorit.refactorings.minaccess.MinimizeAccessTableModel;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.JProgressDialog;
import net.sf.refactorit.ui.SearchingInterruptedException;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.module.RefactorItContext;

import javax.swing.JOptionPane;


/**
 * @author vadim
 * @author tonis
 */
public class MinimizeAccessAction extends AbstractRefactorItAction {
  public static final String KEY = "refactorit.action.MinimizeAccessAction";
  public static final String NAME = "Minimize Access Rights";

  public boolean isAvailableForType(Class type) {
    return BinClass.class.isAssignableFrom(type)
        // NOTE this way this module appears in generic "Class" menu and is more
        // usable, anyway we work most of the time with classes and very rarely
        // someone invokes this action being within an interface and it'l refuse anyway
        || BinInterface.class.isAssignableFrom(type)
        || BinMethod.class.isAssignableFrom(type)
        || BinMethodInvocationExpression.class.isAssignableFrom(type)
        || BinField.class.equals(type)
        || BinFieldInvocationExpression.class.isAssignableFrom(type);
  }

  public boolean isMultiTargetsSupported() {
    return false;
  }

  public boolean isReadonly() {return false;
  }

  public String getName() {
    return NAME;
  }

  public char getMnemonic() {
    return 'Z';
  }

  public String getKey() {
    return KEY;
  }

  public boolean run(final RefactorItContext context, final Object object) {
    BinCIType target;
    if (object instanceof BinConstructor
        || object instanceof BinMethod || object instanceof BinField) {
      target = ((BinMember) object).getOwner().getBinCIType();
    } else if (object instanceof BinCIType) {
      target = (BinCIType) object;
    } else {
      AppRegistry.getLogger(this.getClass()).debug("MinimizeAccess called with invalid BinItem");
      return false;
    }

    MinimizeAccess minAccess = new MinimizeAccess(context, target);

    RefactoringStatus status = minAccess.checkPreconditions();

    if (!status.isOk()) {
      String allMessages = status.getAllMessages();
      if (allMessages.length() > 0) {
        RitDialog.showMessageDialog(context,
            allMessages, "Problems with Minimize Access Rights",
            status.getJOptionMessageType());
      }
    }

    if (status.isErrorOrFatal()) {
      return false;
    }

    MinimizeAccessTableModel model = showMinimizeRightsDialog(
        context, (BinMember) object /*target*/);
    if (model == null) {
      return false;
    }

    minAccess.setNodes(model.getNodes());
    status = minAccess.apply();

    return status.isOk();
  }

  private MinimizeAccessTableModel showMinimizeRightsDialog(
      IdeWindowContext context, final BinMember target
  ) {
    boolean minimizeConstructors = true;
    if ((target instanceof BinClass) &&
        ((BinClass) target).getDeclaredConstructors().length > 0) {
      final int res = DialogManager.getInstance().showYesNoCancelQuestion(
          context,
          "question.minimize.constructor.access",
          "Would you like to minimize access modifiers of " +
          "constructors also?",
          DialogManager.YES_BUTTON);
      if (res == DialogManager.CANCEL_BUTTON) {
        return null;
      } else if (res == DialogManager.NO_BUTTON) {
        minimizeConstructors = false;
      }
    }

    final boolean minimizeConstructorsVal = minimizeConstructors;

    final MinimizeAccessTableModel modelArray[] = new MinimizeAccessTableModel[
        1];

    try {
      JProgressDialog.run(context, new Runnable() {
        public void run() {
          CFlowContext.remove(ProgressListener.class.getName());
          modelArray[0] = new MinimizeAccessTableModel(target,
              minimizeConstructorsVal);

        }
      }, target.getName(), true);
    } catch (SearchingInterruptedException ex) {
      return null;
    }
    MinimizeAccessTableModel model = modelArray[0];

    if (model.getNodes().size() == 0) {
      RitDialog.showMessageDialog(context,
          "The members of " + target.getQualifiedName() +
          " have the strictest access modifiers already",
          "", JOptionPane.INFORMATION_MESSAGE);
      return null;
    }

    MinimizeAccessDialog dialog = new MinimizeAccessDialog(context, model);

    while (true) {
      dialog.show();

      if (dialog.getChangeWasPressed()) {
        if (model.isAnythingSelected()) {
          return model;
        }

        RitDialog.showMessageDialog(context,
            "There are no selected members.", "", JOptionPane.WARNING_MESSAGE);
      }

      return null;
    }
  }
}
