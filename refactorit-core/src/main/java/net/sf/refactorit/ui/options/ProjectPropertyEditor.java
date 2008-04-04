/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.options;


import net.sf.refactorit.ui.projectoptions.ProjectProperty;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import java.awt.Component;


public class ProjectPropertyEditor implements TableCellEditor {

  public Component getTableCellEditorComponent(JTable table, Object value,
      boolean isSelected,
      int row, int column) {
    OptionsTableModel model = (OptionsTableModel) table.getModel();
    ProjectProperty property = (ProjectProperty) value;
    return model.getEditorComponentForProperty(property);
  }

  public void addCellEditorListener(javax.swing.event.CellEditorListener
      cellEditorListener) {
  }

  public void cancelCellEditing() {
  }

  public boolean isCellEditable(java.util.EventObject eventObject) {
    return true;
  }

  public void removeCellEditorListener(javax.swing.event.CellEditorListener
      cellEditorListener) {
  }

  public java.lang.Object getCellEditorValue() {
    return null;
  }

  public boolean stopCellEditing() {
    return true;
  }

  public boolean shouldSelectCell(java.util.EventObject eventObject) {
    return true;
  }

}
