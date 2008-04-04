/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.factorymethod;

import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.factorymethod.FactoryMethod;
import net.sf.refactorit.refactorings.factorymethod.FactoryMethodDialog;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.RefactorItContext;


public class FactoryMethodAction extends AbstractRefactorItAction {
  public static final String KEY = "refactorit.action.FactoryMethodAction";
  public static final String NAME = "Create Factory Method";

  public boolean isMultiTargetsSupported() {
    return false;
  }

  public String getName() {
    return NAME;
  }

  public String getKey() {
    return KEY;
  }

  public boolean isAvailableForType(Class type) {
    if (BinConstructor.class.isAssignableFrom(type)) {
      return true;
    }
    return false;
  }
  public boolean isReadonly() {return false;
  }

  /**
   * Module execution.
   *
   * @param context
   * @param parent  any visible component on the screen
   * @param object  Bin object to operate
   * @return  false if nothing changed, true otherwise
   */
  public boolean run(final RefactorItContext context, Object object) {
    BinConstructor target = (BinConstructor) object;

    FactoryMethod factorer = new FactoryMethod(target, context);
    RefactoringStatus status = factorer.checkPreconditions();
    if (!status.isOk()) {
      if (status.getAllMessages().length() > 0) {
        RitDialog.showMessageDialog(context,
            status.getAllMessages(), "Problems with Create Factory Method",
            status.getJOptionMessageType());
      }
    }

    if (status.isErrorOrFatal() || status.isCancel()) {
      return false;
    }

    FactoryMethodDialog dl = new FactoryMethodDialog(
        context, target, factorer.getInvocations());
    dl.show();

    if (dl.getMethodName() == null) {
      return false;
    }

    factorer.setMethodName(dl.getMethodName());
    factorer.setHostingClass(dl.getHostingClass());
    factorer.setOptimizeVisibility(dl.getOptimizeVisibility());

    status = factorer.checkUserInput();
    if (!status.isOk()) {
      if (status.getAllMessages().length() > 0) {
        RitDialog.showMessageDialog(context,
            status.getAllMessages(), "Problems with Create Factory Method",
            status.getJOptionMessageType());
      }
    }

    if (status.isErrorOrFatal() || status.isCancel()) {
      return false;
    }
    status = factorer.apply();//TransformationManager.performTransformationFor(factorer);

    if (status.isCancel()) {
      return false;
    }

    if (!status.isErrorOrFatal()) {
      if (!status.isOk()) {
        if (status.getAllMessages().length() > 0) {
          RitDialog.showMessageDialog(context,
              status.getAllMessages(), "Problems during Create Factory Method",
              status.getJOptionMessageType());
        }
      }
      return true;
    }

    RitDialog.showMessageDialog(context,
        status.getAllMessages(), "Failed to Create Factory Method",
        status.getJOptionMessageType());
    return false;
  }

  public char getMnemonic() {
    return 'Y';
  }
}
