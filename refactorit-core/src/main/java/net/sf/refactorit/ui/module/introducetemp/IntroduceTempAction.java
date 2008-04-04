/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.introducetemp;

import net.sf.refactorit.classmodel.BinSelection;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.introducetemp.IntroduceTemp;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.module.RefactorItContext;


/**
 * @author Anton Safonov
 */
public class IntroduceTempAction extends AbstractRefactorItAction {
  public static final String KEY = "refactorit.action.IntroduceTempAction";
  public static final String NAME = "Introduce Explaining Variable";

  public String getName() {
    return NAME;
  }

  public char getMnemonic() {
    return 'B';
  }

  public boolean isReadonly() {return false;
  }

  public boolean isMultiTargetsSupported() {
    return false;
  }

  public String getKey() {
    return KEY;
  }

  public boolean isAvailableForType(Class type) {
    return type != null && BinSelection.class.isAssignableFrom(type);
  }

  public boolean run(final RefactorItContext context, Object inObject) {
    BinSelection selection = (BinSelection) inObject;

    IntroduceTemp extractor = new IntroduceTemp(context, selection);

    RefactoringStatus status = extractor.checkPreconditions();

    if (!status.isOk()) {
      RitDialog.showMessageDialog(context,
          status.getAllMessages(),
          "Not possible to introduce explaining variable",
          status.getJOptionMessageType());
      return false;
    }

    if (!showIntroduceTempDialog(context, extractor)) {
      return false;
    }

    if ((extractor.apply()).isOk()) {
      context.setState(selection); // small hack
      return true;
    }

    return false;
  }

  /** Overrides super! */
  public void updateEnvironment(final RefactorItContext context) {
    BinSelection selection = (BinSelection) context.getState();
    SourceCoordinate start
        = selection.getStartSourceCoordinate();
    context.show(selection.getCompilationUnit(), start.getLine(),
        GlobalOptions.getOption("source.selection.highlight").equals("true")
        );

    super.updateEnvironment(context);
  }

 public boolean showIntroduceTempDialog(
      IdeWindowContext context, IntroduceTemp extractor
  ) {
    RefactoringStatus status = null;

    final IntroduceTempDialog dlg = new IntroduceTempDialog(context, extractor);
    dlg.setReplaceAll(false);

    do {
      dlg.show();

      if (!dlg.isOkPressed()) {
        return false;
      }

//      String newName
//          = JOptionPane.showInputDialog(parent, "Input new variable name");
//      if (newName == null) {
//        return false;
//      }
//
      extractor.setNewVarName(dlg.getNewVarName());
      extractor.setReplaceAll(dlg.isReplaceAll());
      extractor.setDeclareFinal(dlg.isMakeFinal());
      extractor.setDeclareInForStatement(dlg.isIntroduceInFor());

      status = extractor.checkUserInput();
      if (status.isCancel()) {
        dlg.dispose();
        return false;
      }

      if (!status.isOk()) {
        RitDialog.showMessageDialog(context,
            status.getAllMessages(), "Problem",
            status.getJOptionMessageType());
      }
    } while (status.isErrorOrFatal());

    dlg.dispose();
    return true;
  }
}
