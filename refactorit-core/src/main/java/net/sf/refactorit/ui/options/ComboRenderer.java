/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.options;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import java.awt.Component;
import java.awt.Font;


public class ComboRenderer extends DefaultTableCellRenderer {
  public Component getTableCellRendererComponent(JTable table,
      Object value, boolean isSelected, boolean hasFocus, int row, int column
      ) {
    super.getTableCellRendererComponent(table,
        value, isSelected, hasFocus, row, column);

    if (value instanceof Separator) {
      String item = ((Separator) value).getValue();
      if (item.equals(",")) {
        item += " (comma)";
      } else if (item.equals(".")) {
        item += " (dot)";
      } else if (item.equals(" ")) {
        item += " (space)";
      }
      setText(item);
      Font f = getFont();
      setFont(new Font(f.getName(), Font.BOLD, f.getSize() + 1));
    } else {
      setText(value.toString());
    }

    return this;
  }
}
