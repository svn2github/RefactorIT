/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.notused;


import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.query.notused.ExcludeFilterRule;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 *
 * @author Tanel Alumae
 * @author Igor Malinin
 */
public class NotUsedDialog {
  final RitDialog dialog;

  boolean isOkPressed;

  private NotUsedDialog(IdeWindowContext context) {
    dialog = RitDialog.create(context);
    dialog.setTitle("Search for not used code");
  }

  private void init(FilterPanel panel) {
    dialog.setSize(600, 400);

    JPanel center = new JPanel(new BorderLayout());
    center.setBorder(BorderFactory.createEtchedBorder());
    center.add(createHelpPanel(), BorderLayout.NORTH);
    center.add(panel);

    Container content = dialog.getContentPane();
    content.add(center);
    content.add(createButtonsPanel(), BorderLayout.SOUTH);
  }

  private JComponent createHelpPanel() {
    return DialogManager.getHelpPanel("Press F1 for help");
  }

  public static ExcludeFilterRule[] show(FilterPanel panel) {
    NotUsedDialog dialog = new NotUsedDialog(
        IDEController.getInstance().createProjectContext());

    dialog.init(panel);

    dialog.show();

    if (dialog.isOkPressed) {
      return panel.getSelectedRules();
    }

    return null;
  }

  private void show() {
    dialog.show();
  }

  public JPanel createButtonsPanel() {
    JPanel result = new JPanel();

    JButton okButton = new JButton("Ok");
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        isOkPressed = true;
        dialog.dispose();
      }
    });

    result.add(okButton);
    
    final ActionListener cancelActionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dialog.dispose();
      }
    };

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(cancelActionListener);

    result.add(cancelButton);

    JButton helpButton = new JButton("Help");
    result.add(helpButton);

    HelpViewer.attachHelpToDialog(dialog, helpButton, "refact.notUsed");
    SwingUtil.initCommonDialogKeystrokes(dialog, okButton, cancelButton, 
        helpButton, cancelActionListener);

    return result;
  }
}
