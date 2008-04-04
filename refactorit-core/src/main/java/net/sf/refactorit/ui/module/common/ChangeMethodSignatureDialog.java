/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.common;


import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.changesignature.ChangeMethodSignatureRefactoring;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * @author Igor Malinin
 */
public class ChangeMethodSignatureDialog {
  final RitDialog dialog;

  ChangeMethodSignaturePanel panel;

  boolean isOkPressed;
  
  JButton okButton;

  public ChangeMethodSignatureDialog(ChangeMethodSignatureRefactoring ref) {
    dialog = RitDialog.create(ref.getContext());
    dialog.setTitle("Change Method Signature");
    dialog.setSize(620, 500);
    
    init(new ChangeMethodSignaturePanel(ref));
  }
  
  public boolean isOkPressed() {
    return isOkPressed;
  }

  private void init(ChangeMethodSignaturePanel panel) {
    this.panel = panel;

    JPanel center = new JPanel(new BorderLayout());
    center.setBorder(BorderFactory.createEtchedBorder());
    center.add(createHelpPanel(), BorderLayout.NORTH);
    center.add(panel);

    Container content = dialog.getContentPane();
    content.add(center);
    content.add(createButtonsPanel(), BorderLayout.SOUTH);
  }

  private JComponent createHelpPanel() {
    return DialogManager.getHelpPanel(
        "Method parameters, return type and modifier can be changed");
  }

  public void show() {
    dialog.show();
  }

  private JPanel createButtonsPanel() {
    JPanel result = new JPanel();

    okButton = new JButton("Ok");
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ChangeMethodSignatureDialog.this.panel.stopEditing();
        RefactoringStatus status = panel.change.checkCanChange();
        if (status.isErrorOrFatal()) {
          RitDialog.showMessageDialog(dialog.getContext(),
              status.getAllMessages(), "Error",
              status.getJOptionMessageType());
          return;
        } else {
          if (status.isQuestion()) {
            int result = RitDialog.showConfirmDialog(IDEController.getInstance()
                .createProjectContext(), status.getAllMessages(), "Warning",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if(result != JOptionPane.YES_OPTION) {
              return;
            }
          }

          isOkPressed = true;
          dialog.dispose();
        }
      }
    });

    result.add(okButton);

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dialog.dispose();
      }
    });

    result.add(cancelButton);

    JButton helpButton = new JButton("Help");
    result.add(helpButton);

    HelpViewer.attachHelpToDialog(dialog, helpButton, "refact.changeSignature");
    SwingUtil.initCommonDialogKeystrokes(dialog, okButton, cancelButton, 
        helpButton);

    return result;
  }
  
  public void clickOk() {
    okButton.doClick();
  }
  
  public void clickUp() {
    panel.clickUp();
  }
  
  public void clickDown() {
    panel.clickDown();
  }
}
