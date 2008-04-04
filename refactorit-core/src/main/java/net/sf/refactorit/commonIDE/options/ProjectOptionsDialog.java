/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.commonIDE.options;


import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.projectoptions.ProjectOptions;

import javax.swing.JButton;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 *
 *
 * @author Tonis Vaga
 */
public class ProjectOptionsDialog {
  final RitDialog dialog;

  boolean okPressed;

  private Project project;

  private ProjectOptionsPanel optionsPanel;

  public ProjectOptionsDialog(Project project) {
    this.project = project;

    optionsPanel = new ProjectOptionsPanel(project.getOptions());

    JPanel buttonPanel = createButtonPanel();

    JPanel contentPane = new JPanel(new BorderLayout());
    contentPane.add(optionsPanel);
    contentPane.add(buttonPanel, BorderLayout.SOUTH);

    dialog = RitDialog.create(IDEController.getInstance()
        .createProjectContext());
    dialog.setTitle("Project Options for '" + project.getName() + "'");
    dialog.setContentPane(contentPane);
  }


  public void show() {
    dialog.show();
  }

  public boolean isOkPressed() {
    return okPressed;
  }

  public ProjectOptions getOptions() {
    return project.getOptions();
  }

  public void updateSettings() {
    optionsPanel.updateSettings();
  }


  /**
   * creates a typical panel with two buttons on it: OK and Cancel
   * Also, this method assigns to brand new panel neccesary listeners as well
   * @return buttonPanel
   */
  private JPanel createButtonPanel() {
    JPanel buttonPanel = new JPanel();

    JButton okButton = new JButton("OK");

    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        okPressed = true;
        dialog.dispose();
      }
    });

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dialog.dispose();
      }
    });

    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);
    return buttonPanel;
  }
}
