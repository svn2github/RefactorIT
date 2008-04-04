/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.ui.module.createmissingmethod;

import net.sf.refactorit.refactorings.createmissing.CreateMethodContext;
import net.sf.refactorit.ui.module.IdeWindowContext;

import javax.swing.table.AbstractTableModel;



/**
 *
 * @author  tanel
 */
public class CreateMissingMethodModel extends AbstractTableModel {

  private static final String[] columns = {" ", "Method"};
  CreateMethodContext[] nodes;
  private IdeWindowContext context;

  /** Creates a new instance of CreateMissingMethodModel */
  public CreateMissingMethodModel(IdeWindowContext context,
      CreateMethodContext[] nodes) {
    this.nodes = nodes;
    this.context = context;
  }

  /** Returns the number of rows in the model. A
   * <code>JTable</code> uses this method to determine how many rows it
   * should display.  This method should be quick, as it
   * is called frequently during rendering.
   *
   * @return the number of rows in the model
   * @see #getColumnCount
   *
   */
  public int getRowCount() {
    return nodes.length;
  }

  /** Returns the number of columns in the model. A
   * <code>JTable</code> uses this method to determine how many columns it
   * should create and display by default.
   *
   * @return the number of columns in the model
   * @see #getRowCount
   *
   */
  public int getColumnCount() {
    return 2;
  }

  public String getColumnName(int column) {
    return columns[column];
  }

  public Class getColumnClass(int columnIndex) {
    switch (columnIndex) {
      case 0:
        return Boolean.class;
      case 1:
        return String.class;
      default:
        return null;
    }
  }

  public boolean isCellEditable(int rowIndex, int columnIndex) {
    if (columnIndex == 0) {
      return true;
    } else {
      return false;
    }
  }

  public Object getValueAt(int rowIndex, int columnIndex) {
    if (rowIndex >= getRowCount()) {
      return null;
    } else {
      if (columnIndex == 0) {
        return new Boolean(nodes[rowIndex].isSelected());
      } else {
        return nodes[rowIndex].getMethodName();
      }
    }
  }

  public void setValueAt(Object object, int rowIndex, int columnIndex) {
    if (columnIndex == 0) {
      nodes[rowIndex].setSelected(((Boolean) object).booleanValue());
    }
  }

}
