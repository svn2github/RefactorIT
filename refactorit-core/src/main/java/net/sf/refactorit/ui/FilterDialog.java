/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui;


import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;


public class FilterDialog {
  private static final ResourceBundle resLocalizedStrings =
      ResourceUtil.getBundle(FilterDialog.class);

  final RitDialog dialog;

  JButton okButton = new JButton(resLocalizedStrings.getString("button.ok"));
  JButton cancelButton = new JButton(resLocalizedStrings.getString(
      "button.cancel"));

  JCheckBox[] editableButtons;
  JCheckBox[] originalButtons;

  boolean okPressed;
  boolean somethingChanged;

  JComboBox comboBox;
  int comboBoxSelection;

  /**
   * Opens a dialog that lets users edit the checkboxes; it "Cancel"
   * is pressend then no changes will be written to checkboxes,
   * but if "Ok" is pressed then the buttons will contain user
   * selection state after this method returns.
   *
   * @return  true if "OK" was pressed *and* some checkbox
   *     selections were changed, false otherwise.
   */
  public static boolean showDialog(
      IdeWindowContext context, JCheckBox[] buttons, String title
  ) {
    final FilterDialog dialog = new FilterDialog(
        context, title, buttons, "", null);
    dialog.show();

    return dialog.okPressed && dialog.somethingChanged;
  }

  public static boolean showDialog(
      IdeWindowContext context, JCheckBox[] buttons,
      String title, String comboBoxTitle, JComboBox comboBox
  ) {
    final FilterDialog dialog = new FilterDialog(context, title, buttons,
        comboBoxTitle, comboBox);
    dialog.show();

    return dialog.okPressed && dialog.somethingChanged;
  }

  private void show() {
    dialog.show();
  }

  private FilterDialog(
      IdeWindowContext context, String title, JCheckBox[] buttons,
      String comboTitle, JComboBox comboBox
  ) {
    dialog = RitDialog.create(context);
    dialog.setTitle(title);

    Panel contentPane = new Panel();
    dialog.setContentPane(contentPane);

    contentPane.setLayout(new BorderLayout());

    contentPane.add(createChoicesPanel(buttons, comboTitle, comboBox),
        BorderLayout.CENTER);

    contentPane.add(createOkCancelButtonsPanel(), BorderLayout.SOUTH);

    this.okButton.requestFocus();

    SwingUtil.initCommonDialogKeystrokes(dialog, okButton, cancelButton, 
        new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        okPressed = false;
        dialog.dispose();
      }
    } );
  }

  private JPanel createChoicesPanel(
      JCheckBox[] buttons, String title, JComboBox comboBox) {
    JComponent[] components
        = {createDropDownArea(title, comboBox), createCheckBoxArea(buttons)};
    return SwingUtil.combineInNorth(components);
  }

  private JPanel createDropDownArea(String title, JComboBox comboBox) {
    this.comboBox = comboBox;

    if (comboBox == null) {
      return new JPanel();
    }

    this.comboBoxSelection = comboBox.getSelectedIndex();

    JPanel result = new JPanel();

    result.setLayout(new BorderLayout());
    result.add(new JLabel(title), BorderLayout.WEST);
    result.add(comboBox, BorderLayout.EAST);

    comboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        somethingChanged = true;
      }
    });

    return result;
  }

  private JPanel createCheckBoxArea(JCheckBox[] buttons) {
    this.originalButtons = buttons;
    this.editableButtons = createClones(originalButtons);

    JPanel result = new JPanel();
    result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));

    for (int i = 0; i < this.editableButtons.length; i++) {
      result.add(this.editableButtons[i]);
    }

    return result;
  }

  private JPanel createOkCancelButtonsPanel() {
    JPanel result = new JPanel();

    addActionListenersToButtons();

    result.add(okButton);
    result.add(cancelButton);

    return result;
  }

  private void addActionListenersToButtons() {
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        okPressed = true;

        copySettings(FilterDialog.this.editableButtons,
            FilterDialog.this.originalButtons);
        dialog.dispose();
      }
    });

    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        okPressed = false;

        if (comboBox != null) {
          // Restore old selection (unlike with checkboxes we do not create a copy of the combo box).
          comboBox.setSelectedIndex(comboBoxSelection);
        }

        dialog.dispose();
      }
    });
  }

  private JCheckBox[] createClones(JCheckBox[] buttons) {
    JCheckBox[] result = new JCheckBox[buttons.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = createClone(buttons[i]);
    }

    return result;
  }

  private JCheckBox createClone(JCheckBox button) {
    JCheckBox result = new JCheckBox(button.getText());
    result.setSelected(button.isSelected());

    return result;
  }

  void copySettings(JCheckBox[] fromButtons, JCheckBox[] toButtons) {
    for (int i = 0; i < fromButtons.length; i++) {
      copySettings(fromButtons[i], toButtons[i]);
    }
  }

  private void copySettings(JCheckBox fromButton, JCheckBox toButton) {
    if (toButton.isSelected() != fromButton.isSelected()) {
      toButton.setSelected(fromButton.isSelected());
      this.somethingChanged = true;
    }
  }
}
