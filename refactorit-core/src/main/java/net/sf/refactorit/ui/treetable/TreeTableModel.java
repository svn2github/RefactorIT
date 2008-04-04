/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.treetable;

import javax.swing.tree.TreeModel;


/**
 * TreeTableModel is the model used by a JTreeTable. It extends TreeModel
 * to add methods for getting inforamtion about the set of columns each
 * node in the TreeTableModel may have. Each column, like a column in
 * a TableModel, has a name and a type associated with it. Each node in
 * the TreeTableModel can return a value for each of the columns and
 * set that value if isCellEditable() returns true.
 */
public interface TreeTableModel extends TreeModel {
  /**
   * Returns the number ofs availible column.
   */
  int getColumnCount();

  /**
   * Returns the name for column number <code>column</code>.
   */
  String getColumnName(int column);

  /**
   * Returns the type for column number <code>column</code>.
   */
  Class getColumnClass(int column);

  /**
   * Returns the value to be displayed for node <code>node</code>,
   * at column number <code>column</code>.
   */
  Object getValueAt(Object node, int column);

  /**
   * Indicates whether the the value for node <code>node</code>,
   * at column number <code>column</code> is editable.
   */
  boolean isCellEditable(Object node, int column);

  /**
   * Sets the value for node <code>node</code>,
   * at column number <code>column</code>.
   */
  void setValueAt(Object value, Object node, int column);
}
