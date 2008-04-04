/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.inline;

import net.sf.refactorit.refactorings.inlinemethod.InlineMethod;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * @author tanel
 *
 */
public class DeleteOrCommentMethodDialog {

  final IdeWindowContext context;
  RitDialog dialog;

  boolean isOkPressed;

  JPanel contentPanel = new JPanel();

  private final JButton buttonOk = new JButton("Ok");
  private final JButton buttonCancel = new JButton("Cancel");
  private final JButton buttonHelp = new JButton("Help");

  private JRadioButton deleteButton   = new JRadioButton("Delete method"  , true);
  private JRadioButton commentButton    = new JRadioButton("Comment out"   , false);
  private JRadioButton leaveButton = new JRadioButton("Leave as is", false);

  public DeleteOrCommentMethodDialog (IdeWindowContext context) {
    this.context = context;

    contentPanel.setLayout(new BorderLayout());
    contentPanel.add(createCenterPanel(), BorderLayout.CENTER);
    contentPanel.add(createButtonsPanel(), BorderLayout.SOUTH);

  }

  /**
   * Allowed to be called several times - recreates dialog as needed.
   */
  public void show() {
    isOkPressed = false;

    dialog = RitDialog.create(context);
    dialog.setTitle("Inline Method");
    dialog.setContentPane(contentPanel);

    buttonOk.requestFocus();

    HelpViewer.attachHelpToDialog(dialog, buttonHelp, "refact.inline_method");
    SwingUtil.initCommonDialogKeystrokes(dialog, buttonOk, buttonCancel, buttonHelp);

    dialog.show();
  }

  private JComponent createCenterPanel() {
    JPanel center = new JPanel(new BorderLayout());
    center.setBorder(BorderFactory.createEtchedBorder());

    center.add(DialogManager.getHelpPanel(
        "Specify what to do with the method declaration after inlining all invocations"),
        BorderLayout.NORTH);
    center.add(createMainPanel(), BorderLayout.CENTER);

    return center;
  }

  private JComponent createMainPanel() {
    JPanel main = new JPanel(new GridBagLayout());

    GridBagConstraints constr = new GridBagConstraints();
    constr.anchor = GridBagConstraints.WEST;
    constr.weightx = 1;
    constr.weighty = 1;
    constr.fill = GridBagConstraints.HORIZONTAL;

    Box typeBox = Box.createHorizontalBox();
    typeBox.add(new JLabel("What to do after inlining all method invocations?"));

    constr.insets = new Insets(5, 7, 5, 5);
    constr.gridy = 0;
    main.add(typeBox, constr);


    ButtonGroup bgroup = new ButtonGroup();
    bgroup.add(deleteButton);
    bgroup.add(commentButton);
    bgroup.add(leaveButton);

    JPanel radioPanel = new JPanel();
    radioPanel.setLayout(new GridLayout(3, 1));
    radioPanel.add(deleteButton);
    radioPanel.add(commentButton);
    radioPanel.add(leaveButton);

	  constr.insets = new Insets(0, 7, 5, 5);
	  constr.gridy = 1;
	  main.add(radioPanel, constr);


//    constr.insets = new Insets(0, 7, 5, 5);
//    constr.gridy = 1;
//    main.add(inputBox, constr);
//
//    constr.insets = new Insets(0, 5, 5, 5);
//    int num = introducer.getOccurencesNumber();
//    if (num > 1) {
//      constr.gridy = 2;
//      main.add(replaceAll, constr);
//      replaceAll.setMnemonic('R');
//      replaceAll.setText(replaceAll.getText() + " (" + num + " occurences)");
//    }
//    constr.gridy = 3;
//    main.add(makeFinal, constr);
//    makeFinal.setMnemonic('f');

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

    return downPanel;
  }

  public boolean isOkPressed() {
    return isOkPressed;
  }

  public int getSelectedAction() {
    if (deleteButton.isSelected()) {
      return InlineMethod.DELETE_METHOD_DECLARATION;
    } else if (commentButton.isSelected()) {
      return InlineMethod.COMMENT_METHOD_DECLARATION;
    } else {
      return InlineMethod.LEAVE_METHOD_DECLARATION;
    }
  }

  public void dispose() {
    dialog.dispose();
  }



  public static void main(String[] args) {
  }

}
