/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.common;


import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.refactorings.Refactoring;
import net.sf.refactorit.refactorings.delegate.OverrideMethodsModel;
import net.sf.refactorit.refactorings.delegate.OverrideMethodsRefactoring;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.JConfirmationDialog;
import net.sf.refactorit.ui.module.RefactorItContext;

import java.awt.Dimension;


/**
 * @author Tonis Vaga
 */
public class OverrideMethodsAction extends AbstractRefactorItAction {
  public static final String KEY = "refactorit.overridemethods";
  public static final String NAME = "Override/Implement Methods";

  public OverrideMethodsAction() {
  }

  public String getName() {
    return NAME;
  }

  public String getKey() {
    return KEY;
  }

  public Refactoring createRefactoring(RefactorItContext context, Object object) {
    return new OverrideMethodsRefactoring(context, (BinClass) object);
  }

  /**
   * @param context
   * @param parent
   * @param refactoring
   * @return true if continue, false if cancel action
   */
  public boolean readUserInput(Refactoring ref) {
    OverrideMethodsRefactoring refactoring = (OverrideMethodsRefactoring) ref;
    OverrideMethodsModel model = refactoring.getModel();

    RefactorItContext context = refactoring.getContext();

    String helpTopicId = "refact.override_methods";

    String description = "Select methods to override or implement";

    if (model.isEmpty()) {
      description =
          "RefactorIT didn't find any methods to override or implement";
    }

    JConfirmationDialog cd = new JConfirmationDialog(getName(), description,
        model, context, null, helpTopicId, new Dimension(600, 400), false);

    cd.show();

    if (!cd.isOkPressed()) {
      return false;
    }

//    OverrideMethodsModel selectedModel = (OverrideMethodsModel) cd.getModel();
//    refactoring.setModel(selectedModel);

    return true;
  }

  public boolean isMultiTargetsSupported() {
    return false;
  }

  public boolean isAvailableForType(Class type) {
    if (BinClass.class == type) {
      return true;
    }
    return false;
  }

  public boolean isReadonly() {
    return false;
  }

  public char getMnemonic() {
    return 'O';
  }
}
