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


/**
 *
 * @author Igor Malinin
 */

class FontRenderer extends DefaultTableCellRenderer {
  public Component getTableCellRendererComponent(JTable table,
      Object value, boolean isSelected, boolean hasFocus, int row, int column
      ) {
    super.getTableCellRendererComponent(table,
        value, isSelected, hasFocus, row, column);

    Font f = (Font) value;

    setFont(f);

    StringBuffer buf = new StringBuffer(f.getName());
    if (f.isBold()) {
      buf.append("-bold");
    }
    if (f.isItalic()) {
      buf.append("-italic");
    }
    buf.append('-').append(f.getSize());

    setText(buf.toString());

    return this;
  }
}
