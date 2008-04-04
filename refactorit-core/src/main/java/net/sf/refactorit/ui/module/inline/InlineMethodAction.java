/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.inline;

import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.inlinemethod.InlineMethod;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.RefactorItContext;


/**
 * @author Anton Safonov
 */
public class InlineMethodAction extends AbstractRefactorItAction {
  public static final String KEY = "refactorit.action.InlineMethodAction";
  public static final String NAME = "Inline Method";

  private RefactoringStatus lastRunStatus;

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
    InlineMethod inliner = new InlineMethod(context, (BinItem) inObject);

    
    RefactoringStatus status = inliner.checkPreconditions();

    if (status.isCancel()) {
      status = null;
      return false;
    }

    if (!status.isOk()) {
      RitDialog.showMessageDialog(context,
          status.getAllMessages(), "Not possible to inline method",
          status.getJOptionMessageType());
      return false;
    }

    status = inliner.checkUserInput();
    if (!status.isOk()) {
      RitDialog.showMessageDialog(context,
          status.getAllMessages(), "Not possible to inline method",
          status.getJOptionMessageType());
      return false;
    }

    if (inliner.getMethodDeclarationAction() == InlineMethod.DELETE_METHOD_DECLARATION) {
      DeleteOrCommentMethodDialog dialog = new DeleteOrCommentMethodDialog(context);
      dialog.show();
      if (!dialog.isOkPressed()) {
        return false;
      }
      inliner.setMethodDeclarationAction(dialog.getSelectedAction());
    }
    
    status = inliner.apply();//TransformationManager.performTransformationFor(inliner);

    if (!status.isOk()) {
      if (status.hasSomethingToShow()) {
        DialogManager.getInstance().showCustomError(
            context, status.getAllMessages());
      }
    }
    lastRunStatus = status;
    return lastRunStatus.isOk();
  }

  public RefactoringStatus getLastRunStatus() {
    return lastRunStatus;
  }

  /**
   * @see net.sf.refactorit.ui.module.RefactorItAction#isAvailableForType(java.lang.Class)
   */
  public boolean isAvailableForType(Class type) {
    throw new UnsupportedOperationException("method not implemented yet");
    //return false;
  }
}
