/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.tree;


import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.HtmlUtil;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import java.awt.Component;


/**
 * Basic TreeCellRenderer used by all RefactorIT trees.
 *
 * @author Igor Malinin
 */
public final class HtmlTableCellRenderer extends DefaultTableCellRenderer {
  /*
   * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
   */
  public final Component getTableCellRendererComponent(
      JTable table, Object value,
      boolean selected, boolean hasFocus,
      int row, int column) {
    super.getTableCellRendererComponent(
        table, value, selected, hasFocus, row, column);

    setContents(value.toString());

    return this;
  }

  public final void setContents(String text) {
    if (text.startsWith("<HTML>")) {
      if (Assert.enabled) {
        Assert.must(false, "Content-Type is already HTML! " + text);
      }
      setText(text);
    } else {
      // wrap with right root style
      // JVM 1.3 bug: ignore label font for HTML labels; make it into body style
      setText(HtmlUtil.styleBody(text, getFont()));
    }
  }
}
