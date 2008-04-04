/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.where;


import net.sf.refactorit.query.usage.filters.BinPackageSearchFilter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * Obtains filter options for BinPackage
 *
 * @author Vladislav Vislogubov
 */
public class PackagePanel extends JPanel {
  private JCheckBox usages = new JCheckBox("Usages", true);
  private JCheckBox imports = new JCheckBox("Imports", true);
  private JCheckBox statements = new JCheckBox("Package Statements", true);
  private JCheckBox subpackages = new JCheckBox("Subpackages", false);
  private JCheckBox nonJavaFiles = new JCheckBox(
      "Qualified Names in Non-Java Files", false);

  private JCheckBox supertypes = new JCheckBox("Check Superclasses", false);
  private JCheckBox subtypes = new JCheckBox("Check Subclasses", false);

  private JButton ok;

  public PackagePanel(JButton ok, BinPackageSearchFilter filter) {
    super();

    this.ok = ok;

    if (filter != null) {
      usages.setSelected(filter.isUsages());
      imports.setSelected(filter.isImports());
      statements.setSelected(filter.isPackageStatements());
      subpackages.setSelected(filter.isIncludeSubPackages());
      nonJavaFiles.setSelected(filter.isSearchNonJavaFiles());
      supertypes.setSelected(filter.isIncludeSupertypes());
      subtypes.setSelected(filter.isIncludeSubPackages());
    }

    init();
  }

  void checkFlags() {
    if (!usages.isSelected() && !imports.isSelected() &&
        !statements.isSelected() && !subpackages.isSelected() &&
        !nonJavaFiles.isSelected()) {
      ok.setEnabled(false);
    } else {
      ok.setEnabled(true);
    }
  }

  private void init() {
    setPreferredSize(new Dimension(300, 250));
    setLayout(new GridBagLayout());

    usages.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        checkFlags();
      }
    });

    imports.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        checkFlags();
      }
    });

    statements.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        checkFlags();
      }
    });

    subpackages.addActionListener(new ActionListener() {
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

    JPanel searchP = new JPanel(new GridLayout(5, 1));
    searchP.setBorder(BorderFactory.createTitledBorder("Search"));
    ((TitledBorder) searchP.getBorder()).setTitleColor(Color.black);

    searchP.add(usages);
    searchP.add(imports);
    searchP.add(statements);
    searchP.add(subpackages);
    searchP.add(nonJavaFiles);

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

  public boolean isImports() {
    return imports.isSelected();
  }

  public boolean isStatements() {
    return statements.isSelected();
  }

  public boolean isSubpackages() {
    return subpackages.isSelected();
  }

  public boolean isSupertypes() {
    return supertypes.isSelected();
  }

  public boolean isSubtypes() {
    return subtypes.isSelected();
  }

  public boolean isNonJavaFiles() {
    return nonJavaFiles.isSelected();
  }
}
