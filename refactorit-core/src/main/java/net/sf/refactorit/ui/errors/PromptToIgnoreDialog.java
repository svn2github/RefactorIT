/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.errors;


import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.module.IdeWindowContext;

import javax.swing.JOptionPane;

import java.awt.GridLayout;

/**
 *
 * @author Aleksei sosnovski
 */
public class PromptToIgnoreDialog extends JWarningDialog {
  private static final String msgHalf1 = "Project has ";
  private static final String msgHalf2 = " sources with persistent " +
      "errors. If these sources are not used during " +
      "compilation, add them to ignored sources";



  public PromptToIgnoreDialog(IdeWindowContext context, int noOfSources) {
    super (context, "warning.prompt_to_ignore",
        msgHalf1 + noOfSources + msgHalf2, JOptionPane.QUESTION_MESSAGE, "");

    buttonPanel.removeAll();
    buttonPanel.setLayout(new GridLayout(0, 2, 5, 0));
    buttonYes.setText("Add to ignored");
    buttonPanel.add(buttonYes);
    buttonPanel.add(buttonCancel);
  }

  public int display() {
    if (!box.isSelected()) {
      return DialogManager.CANCEL_BUTTON;
    }

    dialog.show();

    saveLastTimeValue("warning.prompt_to_ignore", result, box.isSelected(),
        this.QUESTION_MESSAGE);

    return result;
  }
}
