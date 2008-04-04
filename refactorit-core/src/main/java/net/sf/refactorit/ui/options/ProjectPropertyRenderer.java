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
import javax.swing.table.DefaultTableCellRenderer;

import java.awt.Component;


public class ProjectPropertyRenderer extends DefaultTableCellRenderer {

  public Component getTableCellRendererComponent(JTable table, Object value,
      boolean isSelected, boolean hasFocus, int row, int column) {
    OptionsTableModel model = (OptionsTableModel) table.getModel();
    ProjectProperty property = (ProjectProperty) value;
    return model.getEditorComponentForProperty(property);
  }
}
