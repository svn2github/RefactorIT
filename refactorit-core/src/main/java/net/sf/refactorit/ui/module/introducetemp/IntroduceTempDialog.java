/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.introducetemp;


import net.sf.refactorit.refactorings.introducetemp.IntroduceTemp;
import net.sf.refactorit.source.format.BinFormatter;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * @author Anton Safonov
 */
public class IntroduceTempDialog {
//  private static ResourceBundle resLocalizedStrings =
//      ResourceUtil.getBundle(IntroduceTempDialog.class);

  final IdeWindowContext context;
  RitDialog dialog;

  boolean isOkPressed;

  JPanel contentPanel = new JPanel();

  private final JButton buttonOk = new JButton("Ok");
  private final JButton buttonCancel = new JButton("Cancel");
  private final JButton buttonHelp = new JButton("Help");

  final JTextField newNameText = new JTextField();

  private final JCheckBox makeFinal = new JCheckBox("Make final", false);
  private final JCheckBox replaceAll
      = new JCheckBox("Replace all occurences of expression", true);
  private final JCheckBox introduceInFor =
      new JCheckBox("Introduce into FOR initializer");

  public IntroduceTempDialog(
      IdeWindowContext context, final IntroduceTemp introducer
  ) {
    this.context = context;

    contentPanel.setLayout(new BorderLayout());
    contentPanel.add(createCenterPanel(introducer), BorderLayout.CENTER);
    contentPanel.add(createButtonsPanel(), BorderLayout.SOUTH);

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        newNameText.selectAll();
      }
    });
  }

  public String getNewVarName() {
    return this.newNameText.getText();
  }

  public boolean isReplaceAll() {
    return replaceAll.isSelected();
  }
  
  public void setReplaceAll(boolean replaceAll) {
    this.replaceAll.setSelected(replaceAll);
  }

  public boolean isMakeFinal() {
    return makeFinal.isSelected();
  }

  /**
   * Allowed to be called several times - recreates dialog as needed.
   */
  public void show() {
    isOkPressed = false;

    dialog = RitDialog.create(context);
    dialog.setTitle("Introduce Explaining Variable");
    dialog.setContentPane(contentPanel);

    buttonOk.requestFocus();

    HelpViewer.attachHelpToDialog(dialog, buttonHelp, "refact.introduce_temp");
    SwingUtil.initCommonDialogKeystrokes(dialog, buttonOk, buttonCancel, buttonHelp);

    dialog.show();
  }

  private JComponent createCenterPanel(IntroduceTemp introducer) {
    JPanel center = new JPanel(new BorderLayout());
    center.setBorder(BorderFactory.createEtchedBorder());

    center.add(DialogManager.getHelpPanel(
        "Specify details for the new variable"),
        BorderLayout.NORTH);
    center.add(createMainPanel(introducer), BorderLayout.CENTER);

    return center;
  }

  private JComponent createMainPanel(final IntroduceTemp introducer) {
    JPanel main = new JPanel(new GridBagLayout());

    GridBagConstraints constr = new GridBagConstraints();
    constr.anchor = GridBagConstraints.WEST;
    constr.weightx = 1;
    constr.weighty = 1;
    constr.fill = GridBagConstraints.HORIZONTAL;

    Box typeBox = Box.createHorizontalBox();
    typeBox.add(new JLabel("Variable of type:"));
    typeBox.add(Box.createHorizontalStrut(3));
    typeBox.add(new JLabel(BinFormatter.formatQualified(introducer.getVarType())));

    Box inputBox = Box.createHorizontalBox();
    JLabel nameLabel = new JLabel("Name:");
    inputBox.add(nameLabel);
    inputBox.add(Box.createHorizontalStrut(3));
    inputBox.add(this.newNameText);
    this.newNameText.setText(introducer.getPossibleName());

    constr.insets = new Insets(5, 7, 5, 5);
    constr.gridy = 0;
    main.add(typeBox, constr);

    constr.insets = new Insets(0, 7, 5, 5);
    constr.gridy = 1;
    main.add(inputBox, constr);

    constr.insets = new Insets(0, 5, 5, 5);
    int num = introducer.getOccurencesNumber();
    if (num > 1) {
      constr.gridy = 2;
      main.add(replaceAll, constr);
      replaceAll.setMnemonic('R');
      replaceAll.setText(replaceAll.getText() + " (" + num + " occurences)");
    }
    constr.gridy = 3;
    main.add(makeFinal, constr);
    makeFinal.setMnemonic('f');


    constr.insets = new Insets(0, 5, 5, 5);
    if(introducer.canBeDeclaredInForStatement()) {
      constr.gridy = 4;

      makeFinal.setSelected(false);
      makeFinal.setEnabled(false);
      replaceAll.setSelected(false);
      replaceAll.setEnabled(false);

      introduceInFor.setSelected(true);

      introduceInFor.addActionListener(new ActionListener() {

        boolean replaceAllOldValue = introducer.getOccurencesNumber() > 1;
        boolean makeFinalOldValue = false;

        public void actionPerformed(ActionEvent event) {
          if (introduceInFor.isSelected()) {

            replaceAllOldValue = replaceAll.isSelected();
            replaceAll.setSelected(false);
            replaceAll.setEnabled(false);

            makeFinalOldValue = makeFinal.isSelected();
            makeFinal.setSelected(false);
            makeFinal.setEnabled(false);
          } else {
            replaceAll.setSelected(replaceAllOldValue);
            replaceAll.setEnabled(true);

            makeFinal.setSelected(makeFinalOldValue);
            makeFinal.setEnabled(true);
          }
        }
      });
      main.add(introduceInFor, constr);
    }

    return main;
  }

  private JComponent createButtonsPanel() {
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(1, 3, 4, 0));
    buttonCancel.setSelected(true);

    buttonOk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        isOkPressed = true;
        dialog.dispose();
      }
    });
    buttonPanel.add(buttonOk);

    buttonCancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dialog.dispose();
      }
    });
    buttonPanel.add(buttonCancel);

    buttonPanel.add(buttonHelp);

    JPanel downPanel = new JPanel(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.EAST;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.insets = new Insets(3, 60, 3, 20);
    downPanel.add(buttonPanel, constraints);

    buttonOk.setNextFocusableComponent(buttonCancel);
    buttonCancel.setNextFocusableComponent(buttonHelp);
    buttonHelp.setNextFocusableComponent(newNameText);

    return downPanel;
  }

  public boolean isOkPressed() {
    return isOkPressed;
  }

  public void dispose() {
    dialog.dispose();
  }

  public boolean isIntroduceInFor() {
    return introduceInFor.isSelected();
  }
}
