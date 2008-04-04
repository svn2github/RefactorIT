/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.where;


import net.sf.refactorit.query.usage.filters.BinMethodSearchFilter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * Obtains filter options for BinMethod
 *
 * @author Vladislav Vislogubov
 */
public class MethodPanel extends JPanel {
  JButton ok;

  JCheckBox usages = new JCheckBox("Usages", true);
  JCheckBox overridden = new JCheckBox("Overrides", true);

  JRadioButton interfaceUsage = new JRadioButton("interface", true);
  JRadioButton implementationUsage = new JRadioButton("implementation", false);

  private JCheckBox supertypes = new JCheckBox("Check Supertypes", true);
  private JCheckBox subtypes = new JCheckBox("Check Subtypes", true);

  private JCheckBox skipSelf = new JCheckBox("Skip Self Usages", true);

  public MethodPanel(JButton ok, BinMethodSearchFilter filter) {
    super();

    this.ok = ok;

    if (filter != null) {
      usages.setSelected(filter.isUsages());
      overridden.setSelected(filter.isOverrides());
      supertypes.setSelected(filter.isIncludeSupertypes());
      subtypes.setSelected(filter.isIncludeSubtypes());
      interfaceUsage.setSelected(!filter.isImplementationSearch());
      implementationUsage.setSelected(filter.isImplementationSearch());
      skipSelf.setSelected(filter.isSkipSelf());
    }

    init();
  }

  private void init() {
//		setPreferredSize(new Dimension(300, 200));
    setLayout(new GridBagLayout());

    usages.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (!overridden.isSelected() && !usages.isSelected()) {
          ok.setEnabled(false);
        } else {
          ok.setEnabled(true);
        }
      }
    });

    overridden.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (!overridden.isSelected() && !usages.isSelected()) {
          ok.setEnabled(false);
        } else {
          ok.setEnabled(true);
        }
      }
    });

    GridBagConstraints constraints = new GridBagConstraints();

    JPanel searchP = new JPanel(new GridLayout(2, 1));
    searchP.setBorder(BorderFactory.createTitledBorder("Search"));
    ((TitledBorder) searchP.getBorder()).setTitleColor(Color.black);

    Box usagesBox = Box.createHorizontalBox();
    usagesBox.add(usages);
    final JLabel rightParenthesis = new JLabel("( ");
    usagesBox.add(rightParenthesis);
    usagesBox.add(interfaceUsage);
    usagesBox.add(implementationUsage);
    final JLabel leftParenthesis = new JLabel(")");
    usagesBox.add(leftParenthesis);

    ButtonGroup group = new ButtonGroup();
    group.add(interfaceUsage);
    group.add(implementationUsage);

    ChangeListener changeListener = new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        rightParenthesis.setEnabled(usages.isSelected());
        interfaceUsage.setEnabled(usages.isSelected());
        implementationUsage.setEnabled(usages.isSelected());
        leftParenthesis.setEnabled(usages.isSelected());
      }
    };

    usages.addChangeListener(changeListener);

    searchP.add(usagesBox);
    searchP.add(overridden);
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

  public boolean isOverridden() {
    return overridden.isSelected();
  }

  public boolean isSupertypes() {
    return supertypes.isSelected();
  }

  public boolean isSubtypes() {
    return subtypes.isSelected();
  }

  public boolean isImplementationSearch() {
    return implementationUsage.isSelected();
  }

  public boolean isSkipSelf() {
    return skipSelf.isSelected();
  }

  public void setImplementationSearch(final boolean implementation) {
    implementationUsage.setSelected(implementation);
  }
}
