/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.apidiff;


import net.sf.refactorit.query.usage.filters.ApiDiffFilter;
import net.sf.refactorit.ui.module.IdeWindowContext;

import javax.swing.table.AbstractTableModel;

import java.util.ArrayList;
import java.util.List;


public class ApiDiffFilterModel extends AbstractTableModel {
  public static final String[] accessStrings
      = {"public", "protected", "private", "package private"};

  private static final String[] columns
      = {" ", "public", "protected", "private", "package private"};

  IdeWindowContext context;
  List rows = new ArrayList();

  public ApiDiffFilterModel(IdeWindowContext context) {
    this.context = context;
  }

  public int getColumnCount() {
    return columns.length;
  }

  public String getColumnName(int column) {
    return columns[column];
  }

  public Class getColumnClass(int columnIndex) {
    final Object value = getValueAt(0, columnIndex);

    if (value == null) {
      return null;
    }

    return value.getClass();
  }

  public boolean isCellEditable(int row, int col) {
    return (col > 0);
  }

  public int getRowCount() {
    return rows.size();
  }

  public Object getValueAt(int rowIndex, int columnIndex) {
    if (rowIndex >= getRowCount()) {
      return null;
    }

    final ApiDiffFilterNode node = findNodeForRow(rowIndex);
    if (node == null) {
      return null;
    }

    switch (columnIndex) {
      case 0:
        return node.getName();

      case 1:
        return node.getPublic(getState());

      case 2:
        return node.getProtected(getState());

      case 3:
        return node.getPrivate(getState());

      case 4:
        return node.getPackagePrivate(getState());
    }

    return null;
  }

  public void setValueAt(Object value, int rowIndex, int columnIndex) {
    final ApiDiffFilterNode node = findNodeForRow(rowIndex);
    if (node == null) {
      return;
    }

    switch (columnIndex) {
      case 0:
        break;

      case 1:
        node.setPublic((Boolean) value, getState());
        break;

      case 2:
        node.setProtected((Boolean) value, getState());
        break;

      case 3:
        node.setPrivate((Boolean) value, getState());
        break;

      case 4:
        node.setPackagePrivate((Boolean) value, getState());
        break;
    }
  }

  public void addRow(ApiDiffFilterNode node) {
    rows.add(node);
    fireTableDataChanged();
  }

  private ApiDiffFilterNode findNodeForRow(int rowIndex) {
    return (ApiDiffFilterNode) rows.get(rowIndex);
  }

  private ApiDiffFilter getState() {
    return (ApiDiffFilter) context.getState();
  }
}
