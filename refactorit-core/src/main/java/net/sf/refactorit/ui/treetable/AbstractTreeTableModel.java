/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.treetable;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

import java.util.ArrayList;
import java.util.List;


/**
 * An abstract implementation of the TreeTableModel interface,
 * handling the list of listeners.
 */
public abstract class AbstractTreeTableModel implements TreeTableModel {
  private final EventListenerList listenerList = new EventListenerList();

  private final Object root;

  public AbstractTreeTableModel(final Object root) {
    this.root = root;
  }

  //
  // Default implmentations for methods in the TreeModel interface.
  //

  public final Object getRoot() {
    return root;
  }

  public final boolean isLeaf(final Object node) {
    return getChildCount(node) == 0;
  }

  public final void valueForPathChanged(
      final TreePath path, final Object newValue
      ) {
    // do nothing
  }

  // This is not called in the JTree's default mode: use a naive implementation.
  public final int getIndexOfChild(final Object parent, final Object child) {
    final int len = getChildCount(parent);
    for (int i = 0; i < len; i++) {
      if (getChild(parent, i).equals(child)) {
        return i;
      }
    }

    return -1;
  }

  public final void addTreeModelListener(final TreeModelListener l) {
    listenerList.add(TreeModelListener.class, l);
  }

  public final void removeTreeModelListener(final TreeModelListener l) {
    listenerList.remove(TreeModelListener.class, l);
  }

  public final void fireSomethingChanged() {
    final TreeModelEvent e = new TreeModelEvent(this, new Object[] {getRoot()});

    final Object[] listeners = listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      ((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
    }
  }

  /**
   * Notify all listeners that have registered interest for
   * notification on this event type.  The event instance
   * is lazily created using the parameters passed into
   * the fire method.
   *
   * @see EventListenerList
   */
  protected final void fireTreeNodesChanged(
      final Object source, final Object[] path,
      final int[] childIndices, final Object[] children
      ) {
    // Guaranteed to return a non-null array
    final Object[] listeners = listenerList.getListenerList();

    TreeModelEvent e = null;
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == TreeModelListener.class) {
        // Lazily create the event:
        if (e == null) {
          e = new TreeModelEvent(source, path, childIndices, children);
        }

        ((TreeModelListener) listeners[i + 1]).treeNodesChanged(e);
      }
    }
  }

  /**
   * Notify all listeners that have registered interest for
   * notification on this event type.  The event instance
   * is lazily created using the parameters passed into
   * the fire method.
   *
   * @see EventListenerList
   */
  protected final void fireTreeNodesInserted(
      final Object source, final Object[] path,
      final int[] childIndices, final Object[] children
      ) {
    // Guaranteed to return a non-null array
    final Object[] listeners = listenerList.getListenerList();

    TreeModelEvent e = null;
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == TreeModelListener.class) {
        // Lazily create the event:
        if (e == null) {
          e = new TreeModelEvent(source, path, childIndices, children);
        }

        ((TreeModelListener) listeners[i + 1]).treeNodesInserted(e);
      }
    }
  }

  /**
   * Notify all listeners that have registered interest for
   * notification on this event type.  The event instance
   * is lazily created using the parameters passed into
   * the fire method.
   *
   * @see EventListenerList
   */
  protected final void fireTreeNodesRemoved(
      final Object source, final Object[] path,
      final int[] childIndices, final Object[] children
      ) {
    // Guaranteed to return a non-null array

    final Object[] listeners = listenerList.getListenerList();
    TreeModelEvent e = null;
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == TreeModelListener.class) {
        // Lazily create the event:
        if (e == null) {
          e = new TreeModelEvent(source, path, childIndices, children);
        }

        ((TreeModelListener) listeners[i + 1]).treeNodesRemoved(e);
      }
    }
  }

  /**
   * Notify all listeners that have registered interest for
   * notification on this event type.  The event instance
   * is lazily created using the parameters passed into
   * the fire method.
   *
   * @see EventListenerList
   */
  protected final void fireTreeStructureChanged(
      final Object source, final Object[] path,
      final int[] childIndices, final Object[] children
      ) {
    // Guaranteed to return a non-null array
    final Object[] listeners = listenerList.getListenerList();

    TreeModelEvent e = null;
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == TreeModelListener.class) {
        // Lazily create the event:
        if (e == null) {
          e = new TreeModelEvent(source, path, childIndices, children);
        }

        ((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
      }
    }
  }

  //
  // Default implementations for methods in the TreeTableModel interface.
  //
  public Class getColumnClass(final int column) {
    return (column == 0) ? TreeTableModel.class : Object.class;
  }

  public final List getAllChildrenRecursively() {
    return getAllChildrenRecursively(new ArrayList(), getRoot());
  }

  private List getAllChildrenRecursively(final List list, final Object node) {
    list.add(node);

    for (int i = 0; i < getChildCount(node); i++) {
      getAllChildrenRecursively(list, getChild(node, i));
    }

    return list;
  }

  /**
   * By default, make the column with the Tree in it the only editable one.
   * Making this column editable causes the JTable to forward mouse
   * and keyboard events in the Tree column to the underlying JTree.
   */
  public final boolean isCellEditable(final Object node, final int column) {
    return getColumnClass(column) == TreeTableModel.class;
  }

  public final void setValueAt(
      final Object value, final Object node, final int column
      ) {
    // do nothing
  }

  // Left to be implemented in the subclass:

  /*
   *  public Object getChild( Object parent, int index )
   *  public int getChildCount( Object parent )
   *  public int getColumnCount()
   *  public String getColumnName( Object node, int column )
   *  public Object getValueAt( Object node, int column )
   */
}
