/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module;

import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;

import javax.swing.JPanel;

import java.awt.Color;


public class SettingsDialog extends JConfirmationDialog {

  private static final Color SETTINGS_HELP_PANEL_COLOR = new Color (0, 128, 128);//new Color( 199, 205, 231);

  public SettingsDialog(String title, String help,
      BinTreeTableModel model, RefactorItContext context, String description,
      String helpTopicId) {
    super(title, help, model, context, description, helpTopicId);
  }
  
  protected JPanel createMessagePanel() {
    return DialogManager.getHelpPanel(help, SETTINGS_HELP_PANEL_COLOR);
  }

}
