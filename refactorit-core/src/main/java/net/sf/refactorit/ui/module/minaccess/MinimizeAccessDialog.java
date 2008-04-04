/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.minaccess;


import net.sf.refactorit.common.util.HtmlUtil;
import net.sf.refactorit.refactorings.minaccess.MinimizeAccessNode;
import net.sf.refactorit.refactorings.minaccess.MinimizeAccessTableModel;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.table.BinTable;
import net.sf.refactorit.ui.tree.NodeIcons;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;


/**
 * @author vadim
 */
public class MinimizeAccessDialog {
  final RitDialog dialog;

  boolean changePressed;

  private final JButton buttonOk = new JButton("Ok");
  private final JButton buttonSelectAll = new JButton("Select All");
  private final JButton buttonCancel = new JButton("Cancel");
  private final JButton buttonHelp = new JButton("Help");
  
  private ActionListener cancelListener = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      changePressed = false;
      dialog.dispose();
    }
  };

  public MinimizeAccessDialog(
      IdeWindowContext context, MinimizeAccessTableModel model
  ) {
    dialog = RitDialog.create(context);
    dialog.setTitle("Minimize Access Rights");
    dialog.setContentPane(createMainPanel(context, model));
    dialog.setSize(600, 400);
    
    SwingUtil.initCommonDialogKeystrokes(dialog, buttonOk, buttonCancel, buttonHelp,
        cancelListener);
    
    SwingUtil.invokeLater(new Runnable() {
      public void run() {
        buttonOk.requestFocus();
      }
    } );
  }

  private JPanel createMainPanel(IdeWindowContext context,
      MinimizeAccessTableModel model) {
    JPanel main = new JPanel(new BorderLayout());
    JPanel north = new JPanel(new GridBagLayout());
    JPanel center = new JPanel(new GridBagLayout());

    GridBagConstraints constraints = new GridBagConstraints();

    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.NORTH;
    constraints.insets = new Insets(0, 0, 0, 0);
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    north.add(DialogManager.getHelpPanel("Change access modifiers for the members of " +
        model.getType().getQualifiedName()), constraints);

    constraints.gridx = 0;
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.weighty = 1.0;
    constraints.weightx = 1.0;
    center.add(getScrollPane(context, model), constraints);

    main.add(north, BorderLayout.NORTH);
    main.add(center, BorderLayout.CENTER);
    main.add(createButtonPanel(model), BorderLayout.SOUTH);

    return main;
  }

  private JScrollPane getScrollPane(IdeWindowContext context,
      MinimizeAccessTableModel model) {
    BinTable table = new BinTable(model);
    table.optionsChanged();

    table.setDefaultRenderer(MinimizeAccessNode.class,
        new MinimizeAccessRenderer());
    table.setDefaultRenderer(Boolean.class, new MinimizeAccessRenderer());
    table.setDefaultRenderer(String.class, new MinimizeAccessRenderer());

    table.setDefaultEditor(String.class,
        new MinimizeAccessEditor(new JComboBox(new Object[] {})));

//		table.getColumnModel().getColumn(3).setPreferredWidth(100);
//		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    return new JScrollPane(table);
  }

  private JPanel createButtonPanel(final MinimizeAccessTableModel model) {
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(1, 4, 4, 0));

    buttonOk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        changePressed = true;
        dialog.dispose();
      }
    });
    buttonOk.setSelected(true);
    buttonOk.setNextFocusableComponent(buttonSelectAll);
    buttonPanel.add(buttonOk);

    buttonSelectAll.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        model.selectAllCheckBoxes();
      }
    });
    buttonSelectAll.setMnemonic(KeyEvent.VK_S);
    buttonSelectAll.setNextFocusableComponent(buttonCancel);
    buttonPanel.add(buttonSelectAll);

    buttonCancel.addActionListener(cancelListener);
    buttonCancel.setDefaultCapable(false);
    buttonCancel.setNextFocusableComponent(buttonHelp);
    buttonPanel.add(buttonCancel);

    buttonPanel.add(buttonHelp);
    HelpViewer.attachHelpToDialog(dialog, buttonHelp, "refact.min_access");

    JPanel downPanel = new JPanel(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();

    constraints.gridx = 1;
    constraints.gridy = 0;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.insets = new Insets(3, 10, 3, 20);
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.EAST;

    downPanel.add(buttonPanel, constraints);

    return downPanel;
  }

  public boolean getChangeWasPressed() {
    return changePressed;
  }

  public void show() {
    dialog.show();
  }
}


class MinimizeAccessEditor extends DefaultCellEditor {
  public MinimizeAccessEditor(JComboBox box) {
    super(box);
  }

  public Component getTableCellEditorComponent(JTable table, Object value,
      boolean isSelected, int row, int column) {
    MinimizeAccessNode node = (MinimizeAccessNode) value;

    JComboBox component = (JComboBox) getComponent();

    if (component.getItemCount() != 0) {
      component.removeAllItems();
    }

    Object[] o = node.getStricterAccessesAsStrings();
    for (int i = 0; i < o.length; i++) {
      component.addItem(o[i]);
    }

    component.setSelectedItem(node.getSelectedAccess());
    component.setBackground(table.getBackground());
    component.setForeground(table.getForeground());

    return component;
  }
}


// TODO HtmlTableCellRenderer?
class MinimizeAccessRenderer extends DefaultTableCellRenderer {
  private static final JCheckBox checkBox = new JCheckBox();
  private static final JComboBox comboBox = new JComboBox();
  private static final JLabel label = new JLabel();

  public Component getTableCellRendererComponent(JTable table, Object value,
      boolean isSelected, boolean hasFocus,
      int row, int column) {
    super.getTableCellRendererComponent(
        table, value, isSelected, hasFocus, row, column);

    Component component = null;
    setFont(table.getFont());

    if (column == 0) {
      component = this;
      MinimizeAccessNode node = (MinimizeAccessNode) value;

      setIcon(NodeIcons.getBinIcon(node.getType(), node.getBin(), true));
      setText(node.getDisplayName());
    } else if (column == 3) {
      component = comboBox;
      MinimizeAccessNode node = (MinimizeAccessNode) value;

      comboBox.setModel(new DefaultComboBoxModel(node.
          getStricterAccessesAsStrings()));
      comboBox.setSelectedItem(node.getSelectedAccess());
      comboBox.setEnabled(node.isSelected());
      comboBox.setBackground(table.getBackground());
      comboBox.setForeground(table.getForeground());
    } else if (column == 4) {
      component = checkBox;

      checkBox.setSelected(((Boolean) value).booleanValue());
      checkBox.setHorizontalAlignment(JLabel.CENTER);
      checkBox.setBackground(table.getBackground());
      checkBox.setForeground(table.getForeground());
    } else {
      component = label;
      label.setText(HtmlUtil.styleBody((String) value, getFont()));
    }

    return component;
  }
}
