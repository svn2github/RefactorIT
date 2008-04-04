/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.common;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;


/**
 *
 *
 * @author Igor Malinin
 */
public class TypeCellRenderer extends DefaultTableCellRenderer {
  private JPanel panel = new JPanel(new BorderLayout());

  private JButton button = new JButton("...");
  {
    button.setMargin(new Insets(0, 0, 0, 0));
  }

  public TypeCellRenderer() {
    panel.add(this);
    panel.add(button, BorderLayout.EAST);
  }

  public Component getTableCellRendererComponent(
      JTable table, Object value,
      boolean isSelected, boolean hasFocus,
      int row, int column
      ) {
    super.getTableCellRendererComponent(table,
        value, isSelected, hasFocus, row, column);

    return panel;
  }
}
