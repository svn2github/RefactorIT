/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.table;


import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.ui.TunableComponent;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.io.PrintWriter;
import java.io.StringWriter;


public class BinTable extends JTable implements TunableComponent {
  private boolean stoppingEditing = false;

  public BinTable() {
    super();

    init();
  }

  public BinTable(TableModel model) {
    super(model);

    init();
  }

  private void init() {
    setRowSelectionAllowed(true);
    setColumnSelectionAllowed(false);
  }

  public void optionsChanged() {
    setFont(Font.decode(GlobalOptions.getOption("tree.font")));
    setBackground(Color.decode(GlobalOptions.getOption("tree.background")));
    setForeground(Color.decode(GlobalOptions.getOption("tree.foreground")));
    super.setSelectionBackground(
        Color.decode(GlobalOptions.getOption("tree.selection.background")));
    super.setSelectionForeground(
        Color.decode(GlobalOptions.getOption("tree.selection.foreground")));

    FontMetrics fm = getFontMetrics(getFont());
    int height = fm.getMaxAscent() + fm.getMaxDescent() + fm.getLeading();
    setRowHeight((height < 16) ? 16 : height);
  }

  public Color getSelectedBackground() {
    return super.getSelectionBackground();
  }

  public Color getSelectedForeground() {
    return super.getSelectionForeground();
  }

  /**
   * Hack to get table to update it's values if we are e.g. closing the dialog
   * with the table being edited. In this case ordinary table will return its
   * old values if asked later.
   */
  public void stopEditing() {
    if (stoppingEditing) {
      return;
    }
    stoppingEditing = true;
    TableCellEditor editor = getCellEditor();
    if (editor != null) {
      editor.stopCellEditing();
    }
    stoppingEditing = false;
  }

  /** overrides */
  public void removeEditor() {
    // small hack for jdk-1.4.x - e.g. when you press an Ok button in a Dialog
    // it succeeds to remove the editor before actionPerformed() is called,
    // so we don't have a chance to fetch the new value if the table was edited
    StringWriter writer = new StringWriter();
    new Exception("removeEditor").printStackTrace(new PrintWriter(writer));
    if (writer.getBuffer().toString().indexOf("Focus") != -1) {
      stopEditing();
    }
    super.removeEditor();
  }
}
