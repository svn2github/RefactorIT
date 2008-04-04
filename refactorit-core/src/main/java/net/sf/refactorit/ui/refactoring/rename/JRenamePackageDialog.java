/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.refactoring.rename;


import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.rename.RenamePackage;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.module.RefactorItContext;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class JRenamePackageDialog extends AbstractRenameDialog {
  // GUI widgets for grabbing user's input
  private JCheckBox renamePrefix;
  private JLabel oldNameLabel;
  private JTextField prefix;

  private JLabel statusLabel;

  private JCheckBox renameInJavadocs;
  private JCheckBox renameInNonJavaFiles;

  private String originalName;

  private RefactorItContext context;
  private BinPackage aPackage;

  public JRenamePackageDialog(RefactorItContext context, BinPackage aPackage) {
    super(context, resLocalizedStrings.getString("title.package"), "refact.rename.package");

    this.context = context;
    this.aPackage = aPackage;

    originalName = aPackage.getQualifiedName();

    prefix.setText(originalName);
    renameTo.setText(originalName);
    renameInJavadocs.setSelected("true".equals(
        GlobalOptions.getOption("rename.package.in_javadocs", "true")));
    renamePrefix.setSelected("true".equals(
        GlobalOptions.getOption("rename.package.prefix", "true")));
    renameInNonJavaFiles.setSelected("true".equals(
        GlobalOptions.getOption("rename.package.in_non_java_files", "true")));

    updateOldName();
    //hack
    //otherwise when renaming default package user input is not checked at this moment
    checkUserInput();
  }

//  private static boolean pacakgeSuppotsVcsSomehow(BinPackage aPackage) {
//    for(Iterator i = aPackage.getAllTypes(); i.hasNext(); ) {
//      BinCIType type = ((BinTypeRef)i.next()).getBinCIType();
//      if(type.getCompilationUnit().getSource().supportsVcs()) {
//        return true;
//      }
//    }
//
//    return false;
//  }

  public boolean isRenamePrefix() {
    GlobalOptions.setOption("rename.package.prefix", (renamePrefix.isSelected() ? "true" : "false"));

    return renamePrefix.isSelected();
  }

  public String getPrefix() {
    return prefix.getText().trim();
  }

  protected Container createContentPane() {
    JPanel container = new JPanel(new BorderLayout(2, 2));

    JPanel centerer = new JPanel(new BorderLayout(3, 3));
    // Create "Center" panel
    {
      final JPanel center = new JPanel(new GridBagLayout());
      center.setBorder(
          BorderFactory.createCompoundBorder(
          BorderFactory.createEmptyBorder(3, 3, 3, 3),
          BorderFactory.createEtchedBorder())
          );

      GridBagConstraints constraints = new GridBagConstraints();
      constraints.gridx = 0;
      constraints.gridy = 0;
      constraints.fill = GridBagConstraints.HORIZONTAL;
      constraints.anchor = GridBagConstraints.NORTH;
      constraints.insets = new Insets(0, 0, 0, 0);
      constraints.weightx = 1.0;
      constraints.weighty = 1.0;
      constraints.gridwidth = 2;
      center.add(DialogManager.getHelpPanel(
          "Specify a new package name for the selected element"
          ), constraints);

      // Init Constraints and set defaults
      final GridBagConstraints gbc = new GridBagConstraints();
      gbc.insets = new Insets(5, 5, 0, 2);

      gbc.weighty = 1;
      gbc.gridx = 0;
      gbc.gridy = 3;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.anchor = GridBagConstraints.WEST;
      gbc.gridwidth = GridBagConstraints.REMAINDER;
      renamePrefix
          = new JCheckBox("Rename prefixes of all matching packages", false);
      center.add(renamePrefix, gbc);
      renamePrefix.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          updateOldName();
        }
      });

      // rename in javadocs
      {
        renameInJavadocs = new JCheckBox("Rename in javadocs also", false);

        gbc.insets = new Insets(5, 5, 0, 2);
        gbc.weightx = 0;
        gbc.ipadx = 0;
        //gbc.gridwidth = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        gbc.gridx = 0;
        gbc.gridy = 4;
        center.add(renameInJavadocs, gbc);
      }

      // rename in non java files
      {
        renameInNonJavaFiles = new JCheckBox(
            "Rename qualified names in non-java files", false);

        gbc.insets = new Insets(2, 5, 5, 5);
        gbc.weightx = 0;
        gbc.ipadx = 0;
        //gbc.gridwidth = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        gbc.gridx = 0;
        gbc.gridy = 5;
        center.add(renameInNonJavaFiles, gbc);
      }

      // Attach Labels
      {
        gbc.weightx = 0;
        gbc.ipadx = 0;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 1;
        oldNameLabel = new JLabel("Prefix:", JLabel.RIGHT);
        center.add(oldNameLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        center.add(new JLabel("Rename to:", JLabel.RIGHT), gbc);
      }

      // Attach TextFields
      {
        prefix = new JTextField();

        gbc.weightx = 1;
        gbc.ipadx = 4;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 1;
        gbc.gridy = 1;
        center.add(prefix, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        center.add(renameTo, gbc);

        // Lister for text-editing events
        DocumentListener listener = new DocumentListener() {
          public void insertUpdate(DocumentEvent event) {
            checkUserInput();
          }

          public void changedUpdate(DocumentEvent event) {
            checkUserInput();
          }

          public void removeUpdate(DocumentEvent event) {
            checkUserInput();
          }
        };

        // Attach listeners
        prefix.getDocument().addDocumentListener(listener);
        renameTo.getDocument().addDocumentListener(listener);
      }

      centerer.add(BorderLayout.CENTER, center);
    }

    statusLabel = new JLabel(" ");
    centerer.add(BorderLayout.SOUTH, statusLabel);
    container.add(BorderLayout.CENTER, centerer);

    // Create "South" panel
    {
      container.add(createButtonPanel(), BorderLayout.SOUTH);
      help.setNextFocusableComponent(prefix);
      prefix.setNextFocusableComponent(renameTo);
    }

    return container;
  }

  void updateOldName() {
    boolean select = renamePrefix.isSelected();
    prefix.setEnabled(select);
    oldNameLabel.setEnabled(select);
    if (!select) {
      prefix.setText(originalName);
      renameTo.setText(renameTo.getText()); // force it to check name
    }
  }

  void checkUserInput() {
    rename.setEnabled(userInputOk());
  }

  private boolean userInputOk() {
    RenamePackage renamer = new RenamePackage(context, aPackage);
    renamer.setRenamePrefix(renamePrefix.isSelected());
    if (renamePrefix.isSelected()) {
      renamer.setPrefix(prefix.getText());
    }
    renamer.setNewName(renameTo.getText());
    RefactoringStatus status = renamer.checkUserInput();

    if (status.isOk()) {
      this.statusLabel.setText(" ");
    } else {
      this.statusLabel.setText(" " + status.getFirstMessage());
    }

    return status.isOk();
  }

  public boolean isRenameInJavadocs() {
    GlobalOptions.setOption("rename.package.in_javadocs", (renameInJavadocs.isSelected() ? "true" : "false"));
    return this.renameInJavadocs.isSelected();
  }

  public boolean isRenameInNonJavaFiles() {
    GlobalOptions.setOption("rename.in_non_java_files", (renameInNonJavaFiles.isSelected() ? "true" : "false"));
    return this.renameInNonJavaFiles.isSelected();
  }
}
