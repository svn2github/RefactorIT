/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.refactoring.rename;

import net.sf.refactorit.classmodel.statements.BinLabeledStatement;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.module.RefactorItContext;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;


public class JRenameLabelDialog extends AbstractRenameDialog {
  private JTextField oldNameField;
  private JLabel oldNameLabel;
  private JTextField prefix;
  private JLabel statusLabel;
  
  private RenameButtonAccessController rbac;
  
  private BinLabeledStatement statement;
  public JRenameLabelDialog(RefactorItContext context, BinLabeledStatement statement) {
    super(context, resLocalizedStrings.getString("title.label"), "refact.rename.label");
    this.statement = statement;
    
    rbac.setInitialName(statement.getLabelIdentifierName());
    oldNameField.setText(statement.getLabelIdentifierName());
    renameTo.setText(statement.getLabelIdentifierName());
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

}
