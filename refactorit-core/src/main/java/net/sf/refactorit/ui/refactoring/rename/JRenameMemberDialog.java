/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.refactoring.rename;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinCatchParameter;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.utils.GetterSetterUtils;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;


public class JRenameMemberDialog extends AbstractRenameDialog {
  private JTextField oldNameField;

  private JCheckBox renameInJavadocs;
  private JCheckBox renameInNonJavaFiles;
  private JCheckBox semanticRename;
  private JCheckBox renameGettersAndSetters;
  private JCheckBox renameInHierarchy;

  private RenameButtonAccessController rbac;

  public JRenameMemberDialog(
      IdeWindowContext context, String title,
      BinMember member, String helpContextId
  ) {
    super(context, title, helpContextId);

    rbac.setInitialName(member.getName());
    oldNameField.setText(member.getQualifiedName());
    renameTo.setText(member.getName());

    renameInJavadocs.setSelected("true".equals(
        GlobalOptions.getOption("rename.member.in_javadocs", "true")));
    renameInNonJavaFiles.setSelected("true".equals(
        GlobalOptions.getOption("rename.member.in_non_java_files", "true")));
    semanticRename.setSelected("true".equals(
        GlobalOptions.getOption("rename.member.semantic_rename", "true")));
    renameGettersAndSetters.setSelected("true".equals(
        GlobalOptions.getOption("rename.getters_and_setters", "true")));
    if (member instanceof BinConstructor) {
      renameInJavadocs.setVisible(false);
    }
    if (member instanceof BinField) {
      boolean renameGS
          = GetterSetterUtils.getSetterMethodFor((BinField) member) != null
          || GetterSetterUtils.getGetterMethodFor((BinField) member) != null;
      renameGettersAndSetters.setVisible(true);
      renameGettersAndSetters.setEnabled(renameGS);
      if (!renameGS) {
        renameGettersAndSetters.setSelected(renameGS);
      }
    } else {
      renameGettersAndSetters.setVisible(false);
    }
    if (member instanceof BinCIType) {
      renameInNonJavaFiles.setVisible(true);
      semanticRename.setVisible(true);
    } else {
      renameInNonJavaFiles.setVisible(false);
      semanticRename.setVisible(false);
    }

    if (member instanceof BinParameter
        && !(member instanceof BinCatchParameter)) {
      renameInHierarchy.setVisible(true);
    } else {
      renameInHierarchy.setVisible(false);
    }
  }

  protected Container createContentPane() {
    JPanel container = new JPanel(new BorderLayout());

    // Create "Center" panel
    {
      JPanel center = new JPanel(new GridBagLayout());
      center.setBorder(
          BorderFactory.createCompoundBorder(
          BorderFactory.createEmptyBorder(3, 3, 3, 3),
          BorderFactory.createEtchedBorder())
          );

      // Init Constraints and set defaults
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.insets = new Insets(5, 5, 5, 5);

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
          "Specify a new name for the selected element"
          ), constraints);

      // Attach label(s)
      {
        gbc.weightx = 0;
        gbc.weighty = 1;
        gbc.ipadx = 0;
        gbc.anchor = GridBagConstraints.NORTHEAST;

        gbc.gridx = 0;
        gbc.gridy = 1;
        center.add(new JLabel(resLocalizedStrings.getString("label.name"),
            JLabel.RIGHT), gbc);
      }

      // Attach textfield(s)
      {
        oldNameField = new JTextField();

        gbc.weightx = 1;
        gbc.ipadx = 4;
        //gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        gbc.gridx = 1;
        gbc.gridy = 1;
        center.add(oldNameField, gbc);

        oldNameField.setEditable(false);
        oldNameField.setBackground(container.getBackground());
      }

      // Attach label(s)
      {
        gbc.weightx = 0;
        gbc.ipadx = 0;
        //gbc.gridwidth = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHEAST;

        gbc.gridx = 0;
        gbc.gridy = 2;
        center.add(new JLabel(resLocalizedStrings.getString("label.rename"),
            JLabel.RIGHT), gbc);
      }

      // Attach textfield(s)
      {
        gbc.weightx = 1;
        gbc.ipadx = 4;
        //gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        gbc.gridx = 1;
        gbc.gridy = 2;
        center.add(renameTo, gbc);

        // Listen for changes in type's name
        this.rbac = new RenameButtonAccessController(rename);
        renameTo.getDocument().addDocumentListener(this.rbac);
      }

      // rename in javadocs
      {
        renameInJavadocs = new JCheckBox("Rename in javadocs also", false);

        gbc.weightx = 0;
        gbc.ipadx = 0;
        //gbc.gridwidth = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridwidth = 2;

        gbc.gridx = 0;
        gbc.gridy = 3;
        center.add(renameInJavadocs, gbc);
      }

      // rename in non-java files
      {
        renameInNonJavaFiles = new JCheckBox(
            "Rename qualified names in non-java files", false);

        gbc.weightx = 0;
        gbc.ipadx = 0;
        //gbc.gridwidth = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridwidth = 2;

        gbc.gridx = 0;
        gbc.gridy = 4;
        center.add(renameInNonJavaFiles, gbc);
      }

      // do deep semantic rename
      {
        semanticRename = new JCheckBox(
            "Semantic rename", false);

        gbc.weightx = 0;
        gbc.ipadx = 0;
        //gbc.gridwidth = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridwidth = 2;

        gbc.gridx = 0;
        gbc.gridy = 5;
        center.add(semanticRename, gbc);
      }


      // rename field getters and setters
      {
        renameGettersAndSetters = new JCheckBox(
            "Rename field getters and setters", false);

        gbc.weightx = 0;
        gbc.ipadx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridwidth = 2;

        gbc.gridx = 0;
        gbc.gridy = 6;
        center.add(renameGettersAndSetters, gbc);
      }

      // rename in hierarchy
      {
        renameInHierarchy = new JCheckBox(
            "Rename in the inheritance hierarchy", false);

        gbc.weightx = 0;
        gbc.ipadx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridwidth = 2;

        gbc.gridx = 0;
        gbc.gridy = 7;
        center.add(renameInHierarchy, gbc);
      }


      // Add to container
      container.add(BorderLayout.CENTER, center);
    }

    // Create "South" panel
    {
      container.add(createButtonPanel(), BorderLayout.SOUTH);
      help.setNextFocusableComponent(renameTo);
    }

    // Return result
    return container;
  }

  public boolean isRenameInJavadocs() {
    GlobalOptions.setOption("rename.member.in_javadocs", (renameInJavadocs.isSelected() ? "true" : "false"));

    return this.renameInJavadocs.isSelected();
  }

  public boolean isRenameInNonJavaFiles() {
    GlobalOptions.setOption("rename.in_non_java_files", (renameInNonJavaFiles.isSelected() ? "true" : "false"));
    return this.renameInNonJavaFiles.isSelected();
  }

  public boolean isSemanticRename() {
    GlobalOptions.setOption("rename.semantic_rename", (semanticRename.isSelected() ? "true" : "false"));
    return this.semanticRename.isSelected();
  }

  public boolean isRenameGettersAndSetters() {
    GlobalOptions.setOption("rename.getters_and_setters", (renameGettersAndSetters.isSelected() ? "true" : "false"));

    return this.renameGettersAndSetters.isSelected();
  }

  public boolean isRenameInHierarchy() {
    return this.renameInHierarchy.isSelected();
  }
}
