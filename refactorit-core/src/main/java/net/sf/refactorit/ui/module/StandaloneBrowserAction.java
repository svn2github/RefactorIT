/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module;

import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.standalone.JRefactorItDialog;


/**
 * @author Tonis Vaga
 */
public class StandaloneBrowserAction extends AbstractIdeAction {
  public static final String KEY = "refactorit.action.BrowserAction";

  public String getName() {
    return "RefactorIT Browser";
  }

  public char getMnemonic() {
    return 'B';
  }

  public String getKey() {
    return KEY;
  }

  public boolean run(IdeWindowContext context) {
    IDEController controller = IDEController.getInstance();

    if (!controller.ensureProject()) {
      return false;
    }

//    Window oldParent = DialogManager.getDialogParent();

    try {
      // Finally, instantiate the RefactorIT Browser and show it
      JRefactorItDialog browser = new JRefactorItDialog(
          controller.createProjectContext(), controller.getActiveProject());
      browser.show();

      return false;
    } finally {
      // FIXME: HACK to revert back right state
      IDEController.setInstance(controller);
//      DialogManager.setDialogParent(oldParent);
    }
  }
}
