/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.treetable;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;


/**
 * This is a wrapper class takes a TreeTableModel and implements
 * the table model interface. The implementation is trivial, with
 * all of the event dispatching support provided by the superclass:
 * the AbstractTableModel.
 */
public final class TreeTableModelAdapter extends AbstractTableModel {
  private final TreeTableModel model;
  private final JTree tree;

  public TreeTableModelAdapter(final TreeTableModel model, final JTree tree) {
    this.model = model;
    this.tree = tree;

    tree.addTreeExpansionListener(new TreeExpansionListener() {
      // Don't use fireTableRowsInserted() here; the selection model
      // would get updated twice.
      public void treeExpanded(final TreeExpansionEvent event) {
        fireTableDataChanged();
      }

      public void treeCollapsed(final TreeExpansionEvent event) {
        fireTableDataChanged();
      }
    });

    // Install a TreeModelListener that can update the table when
    // tree changes. We use delayedFireTableDataChanged as we can
    // not be guaranteed the tree will have finished processing
    // the event before us.
    if (model != null) {
      model.addTreeModelListener(new TreeModelListener() {
        public void treeNodesChanged(final TreeModelEvent e) {
          delayedFireTableDataChanged();
        }

        public void treeNodesInserted(final TreeModelEvent e) {
          delayedFireTableDataChanged();
        }

        public void treeNodesRemoved(final TreeModelEvent e) {
          delayedFireTableDataChanged();
        }

        public void treeStructureChanged(final TreeModelEvent e) {
          delayedFireTableDataChanged();
        }
      });
    }
  }

  public TreeTableModel getModel() {
    return this.model;
  }

  // Wrappers, implementing TableModel interface.

  public final int getColumnCount() {
    return model.getColumnCount();
  }

  public final String getColumnName(final int column) {
    return model.getColumnName(column);
  }

  public final Class getColumnClass(final int column) {
    return model.getColumnClass(column);
  }

  public final int getRowCount() {
    return tree.getRowCount();
  }

  protected final Object nodeForRow(final int row) {
    final TreePath path = tree.getPathForRow(row);
    if (path == null) {
// START debug code
//System.out.println( "Total row count is : " + getRowCount() );
//System.out.println( "Error in TreeTableModelAdapter.nodeForRow(); Invalid row number: " + row );
//(new Throwable()).printStackTrace();
// END debug code
      return null;
    }

    return path.getLastPathComponent();
  }

  public final Object getValueAt(final int row, final int column) {
    return model.getValueAt(nodeForRow(row), column);
  }

  public final boolean isCellEditable(final int row, final int column) {
    return model.isCellEditable(nodeForRow(row), column);
  }

  public final void setValueAt(
      final Object value, final int row, final int column
      ) {
    model.setValueAt(value, nodeForRow(row), column);
  }

  /**
   * Invokes fireTableDataChanged after all the pending events have been
   * processed. SwingUtilities.invokeLater is used to handle this.
   */
  protected final void delayedFireTableDataChanged() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        fireTableDataChanged();
      }
    });
  }
}
