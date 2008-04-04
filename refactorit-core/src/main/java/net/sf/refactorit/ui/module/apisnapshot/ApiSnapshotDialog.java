/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.apisnapshot;


import net.sf.refactorit.common.util.FileExtensionFilter;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.refactorings.apisnapshot.SnapshotIO;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ResourceBundle;


public class ApiSnapshotDialog {
  ResourceBundle bundle = ResourceUtil.getBundle(ApiSnapshotModule.class);

  final RitDialog dialog;

  JTextField fileName = new JTextField();
  JTextField title = new JTextField();

  String resultTitle;
  String resultFileName;

  ApiSnapshotDialog(IdeWindowContext context) {
    dialog = RitDialog.create(context);
    dialog.setTitle(bundle.getString("dialog.title"));

    dialog.setSize(400, 200);

    JPanel contentPanel = new JPanel();
    dialog.setContentPane(contentPanel);

    contentPanel.setLayout(new BorderLayout());
    contentPanel.add(createCenterPanel(), BorderLayout.CENTER);
    contentPanel.add(createButtonsPanel(), BorderLayout.SOUTH);
  }

  private JComponent createCenterPanel() {
    JPanel center = new JPanel(new GridBagLayout());
    //center.setBorder( BorderFactory.createTitledBorder( bundle.getString( "panel.title" ) ) );
    //((TitledBorder)center.getBorder()).setTitleColor( Color.black );
    center.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(3, 3, 3, 3),
        BorderFactory.createEtchedBorder())
        );

    JPanel north = new JPanel(new BorderLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    north.add(fileName, BorderLayout.CENTER);

    JButton fileChooseButton = new JButton("...");
    fileChooseButton.setMargin(new Insets(0, 10, 0, 10));
    fileChooseButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        File startDir = SnapshotIO.browserStartLocation;
        JFileChooser fileChooser = startDir == null ? new JFileChooser()
            : new JFileChooser(startDir);
        fileChooser.addChoosableFileFilter(new FileExtensionFilter(".snp",
            "RefactorIT Snapshot Files (*.snp)"));
        fileChooser.setMultiSelectionEnabled(false);

        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        int res = RitDialog.showFileDialog(
            IDEController.getInstance().createProjectContext(), fileChooser);
        if (res == JFileChooser.APPROVE_OPTION) {
          if (fileChooser.getSelectedFile() != null) {
            String fileNameStr = fileChooser.getSelectedFile().getAbsolutePath();
            if (fileNameStr.indexOf(".") < 0) {
              fileNameStr += ".snp";
            }
            fileName.setText(fileNameStr);
            SnapshotIO.browserStartLocation = fileChooser.getSelectedFile().
                getParentFile();
          }
        }
      }
    });

    north.add(fileChooseButton, BorderLayout.EAST);

    constraints.gridx = 1;
    constraints.gridy = 0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.NORTH;
    constraints.insets = new Insets(0, 0, 0, 0);
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.gridwidth = 2;
    center.add(DialogManager.getHelpPanel(
        "Select desired file and enter name for its snapshot"),
        constraints);

    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.SOUTH;
    constraints.insets = new Insets(0, 5, 5, 5);
    constraints.weightx = 0.0;
    constraints.weighty = 1.0;
    constraints.gridwidth = 1;
    JLabel l1 = new JLabel(bundle.getString("label.file"));
    l1.setForeground(Color.black);
    center.add(l1, constraints);

    constraints.gridx = 2;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.SOUTH;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.gridwidth = 1;
    constraints.insets = new Insets(0, 5, 0, 5);
    center.add(north, constraints);

    constraints.gridx = 1;
    constraints.gridy = 2;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.NORTH;
    constraints.insets = new Insets(5, 5, 5, 5);
    constraints.weightx = 0.0;
    constraints.weighty = 1.0;
    constraints.gridwidth = 1;
    JLabel l2 = new JLabel(bundle.getString("label.title"));
    l2.setForeground(Color.black);
    center.add(l2, constraints);

    constraints.gridx = 2;
    constraints.gridy = 2;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.NORTH;
    constraints.insets = new Insets(5, 5, 5, 5);
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.gridwidth = 2;
    center.add(title, constraints);

    return center;
  }

  private JComponent createButtonsPanel() {
    JButton buttonOk = new JButton(bundle.getString("button.ok"));
    JButton buttonCancel = new JButton(bundle.getString("button.cancel"));
    JButton buttonHelp = new JButton("Help");

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(1, 3, 4, 0));
    buttonCancel.setSelected(true);

    buttonOk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        resultFileName = fileName.getText().trim();
        resultTitle = title.getText().trim();

        if ("".equals(resultFileName)) {
          RitDialog.showMessageDialog(
              dialog.getContext(), bundle.getString("fields.not.filled"));

          return;
        }
        dialog.dispose();
      }
    });
    buttonPanel.add(buttonOk);

    ActionListener cancelActionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        resultFileName = null;
        resultTitle = null;

        dialog.dispose();
      }
    };
    buttonCancel.addActionListener(cancelActionListener);
    buttonPanel.add(buttonCancel);

    HelpViewer.attachHelpToDialog(dialog, buttonHelp, "refact.apids");
    buttonPanel.add(buttonHelp);
    
    SwingUtil.initCommonDialogKeystrokes(dialog, buttonOk, buttonCancel, 
        buttonHelp, cancelActionListener);

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
    buttonOk.setNextFocusableComponent(buttonCancel);
    buttonCancel.setNextFocusableComponent(buttonOk);
    return downPanel;
  }

  boolean okPressed() {
    return resultFileName != null && resultTitle != null;
  }

  String getChosenFileName() {
    return this.resultFileName;
  }

  String getChosenTitle() {
    return this.resultTitle;
  }

  public void show() {
    dialog.show();
  }
}
