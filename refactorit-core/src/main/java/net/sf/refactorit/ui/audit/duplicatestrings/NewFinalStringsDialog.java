/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.audit.duplicatestrings;


import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.TreeRefactorItContext;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
 /**
 * @author Oleg Tsernetsov
 * @author Aleksei Sosnovski
 */
public class NewFinalStringsDialog {
  final RitDialog dialog;

  final TreeRefactorItContext refContext;

  private TransformationList transformations = new TransformationList();

  public MultiFieldCreatePanel panel;

  boolean isOkPressed = false;

  boolean isRenameOk = false;

  JButton okButton;

  public NewFinalStringsDialog(List fields,
      TreeRefactorItContext winContext, List classes) {
    refContext = winContext;
    dialog = RitDialog.create(winContext);
    dialog.setTitle("Multiple Fields Create");
    dialog.setSize(590, 400);
    init(new MultiFieldCreatePanel(fields, classes));
  }

  public boolean isOkPressed() {
    return isOkPressed;
  }

  private void init(MultiFieldCreatePanel panel) {
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
        .getHelpPanel("Specify names for fields You want to create");
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

          isRenameOk = true;

          if (isRenameOk) {
            isOkPressed = true;
            dialog.dispose();
          } else {
            RitDialog.showMessageDialog(dialog.getContext(),
                "Unable to perform creation");
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

    return result;
  }

  public void clickOk() {
    okButton.doClick();
  }

  public TransformationList getRenameTransformations() {
    return transformations;
  }
}
