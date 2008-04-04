/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module;

import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.rename.RenameField;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.dialog.RitDialog;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * @author Oleg Tsernetsov
 */
public class MultiFieldRenameDialog {
  final RitDialog dialog;

  final TreeRefactorItContext refContext;

  private TransformationList transformations = new TransformationList();

  MultiFieldRenamePanel panel;

  boolean isOkPressed = false;

  boolean isRenameOk = false;

  JButton okButton;

  public MultiFieldRenameDialog(List fields, TreeRefactorItContext winContext) {
    refContext = winContext;
    dialog = RitDialog.create(winContext);
    dialog.setTitle("Multiple Field Rename");
    dialog.setSize(680, 400);
    init(new MultiFieldRenamePanel(fields));
  }

  public boolean isOkPressed() {
    return isOkPressed;
  }

  private void init(MultiFieldRenamePanel panel) {
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
    return DialogManager
        .getHelpPanel("Specify names for fields You want to rename");
  }

  public void show() {
    dialog.show();
  }

  private JPanel createButtonsPanel() {
    JPanel result = new JPanel();

    okButton = new JButton("Ok");
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (panel.isNewNamesOk()) {
          transformations.clear();
          panel.setStatusText("");

          Map fieldNames = panel.getFieldNames();
          RenameField renref;

          isRenameOk = true;
          for (Iterator it = fieldNames.keySet().iterator(); it.hasNext(); ) {
            BinField fld = (BinField) it.next();
            String newName = (String) fieldNames.get(fld);
            if (!newName.equals(fld.getName())) {

              renref = new RenameField(refContext, fld);
              RefactoringStatus preconditions = renref.checkPreconditions();
              isRenameOk = preconditions.isOk();
              
              if(isRenameOk) {
                renref.setNewName(newName);
                RefactoringStatus stat = renref.checkUserInput();
                if (!stat.isOk()) {
                  panel.appendStatusText("Error: Please, specify other name for "
                      + fld.getNameWithAllOwners() + ". "
                      + stat.getFirstMessage() + "\n");
                  isRenameOk = false;
                } else {
                  transformations.merge(renref.performChange());
                }
              }
            }
          }

          if (isRenameOk) {
            isOkPressed = true;
            dialog.dispose();
          } else {
            RitDialog.showMessageDialog(dialog.getContext(),
                "Unable to perform rename action");
          }
        } else {
          RitDialog.showMessageDialog(dialog.getContext(),
              "You should use different variable names");
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

    /*JButton helpButton = new JButton("Help");
     result.add(helpButton);

     HelpViewer.attachHelpToDialog(dialog, helpButton, "...");
     SwingUtil.initCommonDialogKeystrokes(dialog, okButton, cancelButton, 
     helpButton);*/

    return result;
  }

  public void clickOk() {
    okButton.doClick();
  }

  public TransformationList getRenameTransformations() {
    return transformations;
  }
}
