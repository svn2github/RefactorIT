/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.where;


import net.sf.refactorit.query.usage.filters.BinInterfaceSearchFilter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * Obtains filter options for BinInterface
 *
 * @author Vladislav Vislogubov
 */
public class InterfacePanel extends JPanel {
  private JCheckBox usages = new JCheckBox("Usages", true);
  private JCheckBox statements = new JCheckBox("Import Statements", true);
  private JCheckBox methodUsages = new JCheckBox("Method Usages", false);
  private JCheckBox fieldUsages = new JCheckBox("Field Usages", false);
  private JCheckBox implementors = new JCheckBox("Implementers", true);
  private JCheckBox nonJavaFiles = new JCheckBox(
      "Qualified Names in Non-Java Files", false);

  private JCheckBox supertypes = new JCheckBox("Check Supertypes", false);
  private JCheckBox subtypes = new JCheckBox("Check Subtypes", false);
  private JCheckBox skipSelf = new JCheckBox("Skip Self Usages", true);

  private JButton ok;

  public InterfacePanel(JButton ok, BinInterfaceSearchFilter filter) {
    super();

    this.ok = ok;

    if (filter != null) {
      usages.setSelected(filter.isUsages());
      statements.setSelected(filter.isImportStatements());
      methodUsages.setSelected(filter.isMethodUsages());
      fieldUsages.setSelected(filter.isFieldUsages());
      supertypes.setSelected(filter.isIncludeSupertypes());
      subtypes.setSelected(filter.isIncludeSubtypes());
      implementors.setSelected(filter.isImplementers());
      nonJavaFiles.setSelected(filter.isSearchNonJavaFiles());
      skipSelf.setSelected(filter.isSkipSelf());
    }

    init();
  }

  void checkFlags() {
    if (!usages.isSelected() && !statements.isSelected() &&
        !methodUsages.isSelected() && !fieldUsages.isSelected() &&
        !implementors.isSelected() && !nonJavaFiles.isSelected()) {
      ok.setEnabled(false);
    } else {
      ok.setEnabled(true);
    }

    subtypes.setEnabled(methodUsages.isSelected() || fieldUsages.isSelected());
    supertypes.setEnabled(methodUsages.isSelected());
  }

  private void init() {
//		setPreferredSize( new Dimension( 300, 270 ) );
    setLayout(new GridBagLayout());

    usages.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        checkFlags();
      }
    });

    statements.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        checkFlags();
      }
    });

    methodUsages.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        checkFlags();
      }
    });

    fieldUsages.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        checkFlags();
      }
    });

    implementors.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        checkFlags();
      }
    });

    nonJavaFiles.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        checkFlags();
      }
    });

    GridBagConstraints constraints = new GridBagConstraints();

    JPanel searchP = new JPanel(new GridLayout(6, 1));
    searchP.setBorder(BorderFactory.createTitledBorder("Search"));
    ((TitledBorder) searchP.getBorder()).setTitleColor(Color.black);

    searchP.add(usages);
    searchP.add(statements);
    searchP.add(methodUsages);
    searchP.add(fieldUsages);
    searchP.add(implementors);
    searchP.add(nonJavaFiles);
//    searchP.add(skipSelf);

    constraints.insets = new Insets(5, 5, 5, 5);
    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    add(searchP, constraints);

    JPanel inheritanceP = new JPanel(new GridLayout(2, 1));
    inheritanceP.setBorder(BorderFactory.createTitledBorder("Inheritance"));
    ((TitledBorder) searchP.getBorder()).setTitleColor(Color.black);

    inheritanceP.add(supertypes);
    inheritanceP.add(subtypes);

    constraints.insets = new Insets(0, 5, 3, 5);
    constraints.gridx = 1;
    constraints.gridy = 2;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.SOUTH;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    add(inheritanceP, constraints);
  }

  public boolean isUsages() {
    return usages.isSelected();
  }

  public boolean isMethodUsages() {
    return methodUsages.isSelected();
  }

  public boolean isStatements() {
    return statements.isSelected();
  }

  public boolean isFieldUsages() {
    return fieldUsages.isSelected();
  }

  public boolean isNonJavaFiles() {
    return nonJavaFiles.isSelected();
  }

  public boolean isSupertypes() {
    return supertypes.isSelected();
  }

  public boolean isSubtypes() {
    return subtypes.isSelected();
  }

  public boolean isImplementors() {
    return implementors.isSelected();
  }

  public boolean isSkipSelf() {
    return skipSelf.isSelected();
  }

}
