/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui;


import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.IdeWindowContext;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;


/**
 * @author vadim
 */
public class AuxiliaryDialog {
  final RitDialog dialog;
  boolean isOkPressed;

  private JRadioButton[] radioButtons;
  private String[] strings;

  public AuxiliaryDialog(IdeWindowContext context, String title, String[] strings) {
    dialog = RitDialog.create(context);
    dialog.setTitle(title);

    this.strings = strings;
    this.radioButtons = new JRadioButton[strings.length];

    dialog.setSize(650, 150);

    JPanel contentPanel = new JPanel();
    dialog.setContentPane(contentPanel);

    contentPanel.setLayout(new BorderLayout());
    contentPanel.add(createMainPanel(), BorderLayout.CENTER);
    contentPanel.add(createButtonsPanel(), BorderLayout.SOUTH);
  }

  private JComponent createMainPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());

    JLabel label = new JLabel(
        "To resolve this conflict you have several choices:");

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.NORTH;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;

    panel.add(label, constraints);

    constraints.gridy = 1;
    panel.add(createChoiceRadioButtons(), constraints);

    return panel;
  }

  private JPanel createChoiceRadioButtons() {
    JPanel panel = new JPanel();
    panel.setLayout(new GridLayout(0, 1));
    ButtonGroup group = new ButtonGroup();

    for (int i = 0; i < radioButtons.length; i++) {
      radioButtons[i] = new JRadioButton(strings[i]);
      group.add(radioButtons[i]);
      panel.add(radioButtons[i]);
    }

    if (radioButtons[0] != null) {
      radioButtons[0].setSelected(true);
    }

    return panel;
  }

  private JComponent createButtonsPanel() {
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(1, 3, 4, 0));

    Action cancelAction = new AbstractAction("Cancel") {
      public void actionPerformed(ActionEvent arg0) {
        dialog.dispose();
      }
    };

    JButton buttonOK = new JButton("OK");
    buttonOK.setMnemonic(KeyEvent.VK_O);
    buttonOK.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        isOkPressed = true;
        dialog.dispose();
      }
    });
    buttonOK.setEnabled(true);
    buttonPanel.add(buttonOK);

    JButton buttonCancel = new JButton(cancelAction);
    buttonCancel.setSelected(true);
    buttonCancel.setDefaultCapable(false);
    buttonCancel.setMnemonic(KeyEvent.VK_C);
    buttonPanel.add(buttonCancel);

    JPanel downPanel = new JPanel(new GridBagLayout());

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.EAST;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.insets = new Insets(3, 0, 3, 20);

    downPanel.add(buttonPanel, constraints);
    buttonOK.setNextFocusableComponent(buttonCancel);
    buttonOK.setMnemonic(KeyEvent.VK_O);
    buttonCancel.setNextFocusableComponent(buttonOK);

    JRootPane root = dialog.getRootPane();

    try {
      root.registerKeyboardAction(cancelAction,
          KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
          JRootPane.WHEN_IN_FOCUSED_WINDOW);
    } catch (Exception e) {
      // failed to register, let's live without
    }
    root.setDefaultButton(buttonOK);

    return downPanel;
  }

  public int getResult() {
    if (isOkPressed) {
      for (int i = 0; i < radioButtons.length; i++) {
        if (radioButtons[i].isSelected()) {
          return (i + 1);
        }
      }
    }

    return 0;
  }

  public void show() {
    dialog.show();
  }
}
