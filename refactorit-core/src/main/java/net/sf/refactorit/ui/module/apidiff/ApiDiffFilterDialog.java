/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.apidiff;


import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.tree.UITreeNode;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;


class ApiDiffFilterDialog {
  private static final ResourceBundle bundle =
      ResourceUtil.getBundle(ApiDiffDialog.class);

  final RitDialog dialog;

  boolean okPressed;

  public ApiDiffFilterDialog(IdeWindowContext context) {
    dialog = RitDialog.create(context);

    init(context);
  }

  private void init(IdeWindowContext context) {
    dialog.setTitle(bundle.getString("filter.dialog.title"));
    dialog.setSize(500, 123);

    JPanel panel = new JPanel(new BorderLayout());
    ApiDiffFilterModel model = createMembersModel(context);
    JTable table = new JTable(model);

    panel.add(table.getTableHeader(), BorderLayout.NORTH);
    panel.add(table, BorderLayout.CENTER);

    JScrollPane scrollPane = new JScrollPane(panel);
    dialog.getContentPane().add(scrollPane, BorderLayout.CENTER);
    dialog.getContentPane().add(createButtonsPanel(), BorderLayout.SOUTH);
  }

  private ApiDiffFilterModel createMembersModel(IdeWindowContext context) {
    ApiDiffFilterModel model = new ApiDiffFilterModel(context);

    model.addRow(new ApiDiffFilterNode(UITreeNode.NODE_TYPE_FIELD));
    model.addRow(new ApiDiffFilterNode(UITreeNode.NODE_TYPE_METHOD));
    model.addRow(new ApiDiffFilterNode(UITreeNode.NODE_CLASS));

    return model;
  }

  private JComponent createButtonsPanel() {
    JButton buttonOk = new JButton(bundle.getString("button.ok"));
    JButton buttonCancel = new JButton(bundle.getString("button.cancel"));
    JButton buttonHelp = new JButton("Help");

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(1, 3, 4, 0));

    buttonOk.setNextFocusableComponent(buttonCancel);
    buttonOk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ApiDiffFilterDialog.this.okPressed = true;
        dialog.dispose();
      }
    });

    buttonPanel.add(buttonOk);

    buttonCancel.setSelected(true);
    buttonCancel.setNextFocusableComponent(buttonOk);
    final ActionListener cancelActionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dialog.dispose();
      }
    };
    buttonCancel.addActionListener(cancelActionListener);

    buttonPanel.add(buttonCancel);

    buttonHelp.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      }
    });
    buttonPanel.add(buttonHelp);
    
    SwingUtil.initCommonDialogKeystrokes(dialog, buttonOk, buttonCancel,
        buttonHelp, cancelActionListener);

    JPanel downPanel = new JPanel(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 1;
    constraints.gridy = 2;
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.EAST;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.insets = new Insets(3, 0, 3, 20);
    downPanel.add(buttonPanel, constraints);

    return downPanel;
  }

  public boolean getOkWasPressed() {
    return okPressed;
  }

  public void show() {
    dialog.show();
  }
}
