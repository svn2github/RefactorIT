/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.cleanimports;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.statements.BinThrowStatement;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.cleanimports.CleanImportsRefactoring;
import net.sf.refactorit.refactorings.rename.ConfirmationTreeTableModel;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.RefactorItActionUtils;
import net.sf.refactorit.ui.module.RefactorItContext;

import java.util.ArrayList;
import java.util.List;


public class CleanImportsAction extends AbstractRefactorItAction {
  public static final String KEY = "refactorit.action.CleanImportsAction";
  public static final String NAME = "Clean Imports";

  private RefactorItContext context;

  public boolean isAvailableForType(Class type) {
    if (Project.class.equals(type)
        || BinPackage.class.equals(type)
        || BinCIType.class.isAssignableFrom(type)
        || BinConstructor.class.isAssignableFrom(type)
        || BinMethod.Throws.class.equals(type)
        || BinThrowStatement.class.equals(type)
        ) {
      return true;
    }
    return false;
  }

  public boolean isMultiTargetsSupported() {
    return false;
  }

  public String getName() {
    return NAME;
  }

  public char getMnemonic() {
    return 'E';
  }

  public String getKey() {
    return KEY;
  }

  public boolean run(final RefactorItContext context, final Object inObject) {
    this.context = context;
    final Object object = RefactorItActionUtils.unwrapTarget(inObject);

    if (object instanceof BinCIType
        && !((BinCIType) object).isFromCompilationUnit()) {
      DialogManager.getInstance()
          .showNonSourcePathItemInfo(context, getName(), object);
      return false;
    }

    RefactoringStatus status = new RefactoringStatus();
    CleanImportsRefactoring refactoring = new CleanImportsRefactoring(context,
        object);
    List usagesForConfirmation = refactoring.findUnusedUsages();

    if (usagesForConfirmation == null) {
      return false;
    }

    if (usagesForConfirmation.size() == 0) {
      status.addEntry("No imports to clean were found!", RefactoringStatus.INFO);
    } else {
      List importsToRemove = showUnusedImportsToUser(
          usagesForConfirmation);

      if (importsToRemove.size() > 0) {
        refactoring.setImportsToRemove(importsToRemove);

        // do refactoring
        //status.merge(TransformationManager.performTransformationFor(refactoring));
        status.merge(refactoring.apply());
      } else {
        status.addEntry("", RefactoringStatus.CANCEL);
      }
    }

    if (!status.isOk() && status.hasSomethingToShow()) {
      RitDialog.showMessageDialog(context,
          status.getAllMessages(), "Clean Imports output",
          status.getJOptionMessageType());
    }

    // status check shall be after dialog message!
    if (status.isInfo() || status.isCancel()) {
      return false;
    }

    if (!status.isErrorOrFatal()) {
      return true;
    }

    return false;
  }

  private List showUnusedImportsToUser(List allUnusedUsages) {

    ConfirmationTreeTableModel model =
        new ConfirmationTreeTableModel("", allUnusedUsages);

    model = (ConfirmationTreeTableModel) DialogManager
        .getInstance().showConfirmations("Clean Imports", context, model,
        "Select places to remove imports", "refact.clean_imports");

    if (model == null) {
      // null - when user pressed cancle button, so return empty list to remove
      return new ArrayList();
    }

    return model.getCheckedUsages();
  }

  public boolean isReadonly() {
    return false;
  }
}
