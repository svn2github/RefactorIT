/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.options;


import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;


/**
 * Insert the type's description here.
 *
 * @author Vladislav Vislogubov
 * @author Anton Safonov
 */
class JFontChooserDialog {
  private static ResourceBundle resLocalizedStrings
      = ResourceUtil.getBundle(JFontChooserDialog.class);

  final RitDialog dialog;

  /**
   * A font selection pane along with "OK", "Cancel", and "Reset"
   * buttons. If the "OK" or "Cancel" buttons are pressed, the dialog is
   * automatically hidden (but not disposed).  If the "Reset"
   * button is pressed, the color-chooser's color will be reset to the
   * color which was set the last time <code>show</code> was invoked on the
   * dialog and the dialog will remain showing.
   *
   * @param parent         the parent component for the dialog
   * @param title          the title for the dialog
   * @param modal          a boolean. When true, the remainder of the program
   *                       is inactive until the dialog is closed.
   * @param chooserPane    the color-chooser to be placed inside the dialog
   * @param okListener     the ActionListener invoked when "OK" is pressed
   * @param cancelListener the ActionListener invoked when "Cancel" is pressed
   */
  public JFontChooserDialog(
      final IdeWindowContext context, final String title, final JFontChooser chooserPane,
      final ActionListener okListener, final ActionListener cancelListener
  ) {
    dialog = RitDialog.create(context);
    dialog.setTitle(title);
    dialog.setSize(400, 300);

    final ActionListener fullCancelListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(cancelListener != null) {
          cancelListener.actionPerformed(e);
        }
        
        dialog.dispose();
      }
    };

    JButton okButton = new JButton(resLocalizedStrings.getString("button.ok"));
    okButton.addActionListener(okListener);
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dialog.dispose();
      }
    } );

    final JButton cancelButton = new JButton(resLocalizedStrings.getString(
        "button.cancel"));
    cancelButton.addActionListener(fullCancelListener);

    JButton helpButton = new JButton("Help");

    HelpViewer.attachHelpToDialog(
        dialog, helpButton, "getStart.refactoritOptions");
    
    SwingUtil.initCommonDialogKeystrokes(dialog, okButton, cancelButton, 
        helpButton, fullCancelListener);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(1, 3, 10, 0));
    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);
    buttonPanel.add(helpButton);

    JPanel south = new JPanel();
    south.setLayout(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.weightx = 1.0;
    constraints.insets = new Insets(4, 10, 4, 10);
    south.add(buttonPanel, constraints);

    dialog.getContentPane().setLayout(new BorderLayout());

    JPanel center = new JPanel(new BorderLayout());
    center.setBorder(
        BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(3, 3, 3, 3),
        BorderFactory.createEtchedBorder()));

    center.add(
        DialogManager.getHelpPanel("Select desired font name, style and size"),
        BorderLayout.NORTH);
    center.add(chooserPane, BorderLayout.CENTER);

    dialog.getContentPane().add(center, BorderLayout.CENTER);
    dialog.getContentPane().add(south, BorderLayout.SOUTH);
  }

  public void show() {
    dialog.show();
  }
}
