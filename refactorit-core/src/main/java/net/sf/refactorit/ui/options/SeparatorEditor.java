/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.options;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;

import java.awt.Component;


/**
 * @author Vladislav Vislogubov
 */
public class SeparatorEditor extends DefaultCellEditor {
  public SeparatorEditor(JComboBox box) {
    super(box);
  }

  public Component getTableCellEditorComponent(JTable table,
      Object value, boolean isSelected, int row, int column
      ) {
    JComboBox c = (JComboBox)super.getTableCellEditorComponent(table, value,
        isSelected, row, column);
    String item = ((Separator) value).getValue();
    if (item.equals(",")) {
      c.setSelectedIndex(0);
    } else if (item.equals(".")) {
      c.setSelectedIndex(1);
    } else if (item.equals(" ")) {
      c.setSelectedIndex(2);
    } else {
      boolean isNew = true;
      int s = c.getItemCount();
      for (int i = 0; i < s; i++) {
        String obj = (String) c.getItemAt(i);
        if (obj.equals(item)) {
          isNew = false;
          break;
        }
      }

      if (isNew) {
        c.addItem(item);
      }
      c.setSelectedItem(item);
    }

    return c;
  }
}
