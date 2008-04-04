/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.panel;


import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.ui.audit.AuditTreeTable;
import net.sf.refactorit.ui.audit.AuditTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTable;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.Iterator;


public class ResultsTreeDisplayState {
  private ArrayList expandedTreePaths = new ArrayList();
  private TreePathReference[] selections;

  private int activeColumn;
  
  public void saveExpansionAndScrollState(JTree tree) {
    saveExpansionState(tree);
    saveScrollAndSelectionState(tree);
  }

  public void restoreExpansionAndScrollState(BinTreeTable table,
      Project project) {
    JTree tree = table.getTree();
    restoreExpansionState(tree, project);
    restoreScrollAndSelectionState(tree, project, table);
  }

  public void restoreExpansionAndScrollState(JTree tree, Project project) {
    restoreExpansionState(tree, project);
    restoreScrollAndSelectionState(tree, project, tree);
  }

  private void saveExpansionState(JTree tree) {
    expandedTreePaths.clear();

    for (int row = 0; row < tree.getRowCount(); row++) {
      if (tree.isExpanded(row)) {
        TreePath expandedPath = tree.getPathForRow(row);

        expandedTreePaths.add(new TreePathReference(expandedPath));
      }
    }
  }

  private void restoreExpansionState(JTree tree, Project project) {
    for (Iterator i = expandedTreePaths.iterator(); i.hasNext(); ) {
      TreePathReference ref = (TreePathReference) i.next();

      TreePath pathToItem = ref.getPath(project, tree.getModel());
      if (pathToItem != null) {
        tree.expandPath(pathToItem);
      }
    }
  }

  private void saveScrollAndSelectionState(JTree tree) {
    TreePath path = tree.getSelectionPath();

    if (path == null) {
      selections = null;
    } else {
      int length = path.getPathCount();
      selections = new TreePathReference[length];
      for (int i = 0; i < length; i++) {
        selections[i] = new TreePathReference(path);
        path = path.getParentPath();
      }
    }
  }

  private void restoreScrollAndSelectionState(
      JTree tree, Project project, JComponent component
      ) {
    if (selections == null) {
      return;
    }

    TreePath selection = getSelectionPath(tree, project);
    if (selection == null) {
      return;
    }

    tree.setSelectionPath(selection);

    new ScrollWhenPlacedInsideScrollPane(component, tree, selection);
  }

  private TreePath getSelectionPath(JTree tree, Project project) {
    TreeModel model = tree.getModel();

    for (int i = 0; i < selections.length; i++) {
      TreePathReference ref = selections[i];

      TreePath result = ref.getPath(project, model);
      if (result != null) {
        return result;
      }
    }

    return null;
  }

  static class ScrollWhenPlacedInsideScrollPane implements AncestorListener {
    private final JComponent toBePlacedDirectlyInScrollPane;
    private final JTree treeToScroll;
    private final TreePath path;

    public ScrollWhenPlacedInsideScrollPane(
        JComponent toBePlacedDirectlyInScrollPane,
        JTree treeToScroll, TreePath pathToMakeVisible
        ) {
      this.toBePlacedDirectlyInScrollPane = toBePlacedDirectlyInScrollPane;
      this.treeToScroll = treeToScroll;
      this.path = pathToMakeVisible;

      if (insideScrollPane(toBePlacedDirectlyInScrollPane)) {
        doScroll();
      } else {
        toBePlacedDirectlyInScrollPane.addAncestorListener(this);
      }
    }

    public void ancestorAdded(AncestorEvent event) {
      if (insideScrollPane(toBePlacedDirectlyInScrollPane)) {
        // This remove *MUST* be before doScroll(), otherwise we might
        // become forever-recursive and end up with OutOfMemoryError:
        toBePlacedDirectlyInScrollPane.removeAncestorListener(this);

        doScroll();
      }
    }

    public void ancestorMoved(AncestorEvent event) {}

    public void ancestorRemoved(AncestorEvent event) {}

    private boolean insideScrollPane(JComponent component) {
      Container ancestor = SwingUtilities
          .getAncestorOfClass(JScrollPane.class, component);

      return ancestor != null;
    }

    private Component getRoot(Component component) {
      while (component.getParent() != null) {
        component = component.getParent();
      }

      return component;
    }

    private void doScroll() {
      try {
        treeToScroll.scrollPathToVisible(path);

        toBePlacedDirectlyInScrollPane.invalidate();
        toBePlacedDirectlyInScrollPane.validate();
        toBePlacedDirectlyInScrollPane.repaint();

        getRoot(toBePlacedDirectlyInScrollPane).repaint();
      } catch (Exception e) {
        System.err.println("EXCEPTION -- PLEASE REPORT");
        e.printStackTrace();
      }
    }
  }

  public void saveActiveColumnState(AuditTreeTable auditTreeTable) {
    AuditTreeTableModel model = (AuditTreeTableModel) auditTreeTable
        .getBinTreeTableModel();
    activeColumn = model.getActiveColumnIndex();
  }

  public void restoreActiveColumnState(AuditTreeTable auditTreeTable) {
    AuditTreeTableModel model = (AuditTreeTableModel) auditTreeTable
        .getBinTreeTableModel();
    model.sort(activeColumn);
  }
}
