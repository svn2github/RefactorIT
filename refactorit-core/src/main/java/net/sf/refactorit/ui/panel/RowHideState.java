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
import net.sf.refactorit.ui.tree.MultilineRowTree;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;
import net.sf.refactorit.ui.treetable.ParentTreeTableNode;

import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class RowHideState {
  private List hiddenRows = new ArrayList();
  private boolean showHiddenRows = ParentTreeTableNode.
      DEFAULT_SHOW_HIDDEN_CHILDREN;

  /**
   * Note 1: Root nodes must *not* be hidden.
   * Note 2: Does *not* save hidden rows; there is another method for that.
   */
  public void hideSelectedRow(JTree tree) {
    TreePath[] selectionPaths = tree.getSelectionPaths();
    boolean hideAll = !mostNodesAreHidden(selectionPaths);

    for (int i = 0; i < selectionPaths.length; i++) {
      if (!(selectionPaths[i].getLastPathComponent() instanceof
          BinTreeTableNode)) {
        // FIXME: implement support for  MultlineRowTree -- FIXME Scanner $$$
        return;
      }

      BinTreeTableNode selectedNode = (BinTreeTableNode) selectionPaths[i].
          getLastPathComponent();

      selectedNode.setHiddenRecursively(hideAll);
    }

    fireSomethingChangedInModel(tree);
  }

  private boolean mostNodesAreHidden(TreePath[] treePaths) {
    int hiddenCount = 0;
    int visibleCount = 0;

    for (int i = 0; i < treePaths.length; i++) {
      if (((ParentTreeTableNode) treePaths[i].getLastPathComponent()).isHidden()) {
        hiddenCount++;
      } else {
        visibleCount++;
      }
    }

    return hiddenCount > visibleCount;
  }

  public void restoreHideState(Project project, JTree tree) {
    TreeModel model = tree.getModel();

    if (!(model.getRoot() instanceof ParentTreeTableNode)) {
      // FIXME: implement support for MultilineTreeModel -- for FIXME Scanner $$$
      return;
    }

    ((ParentTreeTableNode) model.getRoot()).setShowHiddenChildren(
        showHiddenRows);
    
    /* 
     * the list is looked though backwards, to avoid unwanted effects during
     * resolving situation described in lower comment (did 'unhide' rows in
     * cases when it shouldn`t, just because the child row was not hidden yet
     * in this cycle)
     */
    for (int i = hiddenRows.size()-1; i >= 0; i--){
      TreePathReference reference = (TreePathReference) hiddenRows.get(i);
      TreePath path = reference.getPath(project, model);
      if (path != null) {
        ParentTreeTableNode node = (ParentTreeTableNode) path.
            getLastPathComponent();
                
        // avoid situation when parent node is hidden while children are visible
        if (!node.hasVisibleChildren()){
          node.setHidden(true);
        }
      }
    }
        
    fireSomethingChangedInModel(tree);
  }

  /** @return list of {@link TreePathReference} */
  public List getHiddenRows() {
    return this.hiddenRows;
  }

  private void fireSomethingChangedInModel(JTree tree) {
    if (tree.getModel() instanceof BinTreeTableModel) {
      ((BinTreeTableModel) tree.getModel()).fireSomethingChanged();
    } else if (tree instanceof MultilineRowTree) {
      MultilineRowTree mlTree = (MultilineRowTree) tree;

      TreeModelEvent e = new TreeModelEvent(mlTree,
          new Object[] {tree.getModel().getRoot()});

      for (Iterator i = mlTree.getModelListeners(); i.hasNext(); ) {
        ((TreeModelListener) i.next()).treeStructureChanged(e);
      }
    }
  }

  public void saveHideState(TreeModel model) {
    if (!(model.getRoot() instanceof ParentTreeTableNode)) {
      // FIXME: also implement support for MultilineRowTree -- for FIXME Scanner $$$
      return;
    }
    
    saveHideState((ParentTreeTableNode) model.getRoot(), null);
  }

  private void saveHideState(ParentTreeTableNode node, TreePath parentPath) {
    final TreePath nodePath;
    if (parentPath == null) {
      nodePath = new TreePath(node.getPath());
    } else {
      nodePath = parentPath.pathByAddingChild(node);
    }

    if (node.isHidden()) {
      hiddenRows.add(new TreePathReference(nodePath));
    }

    final List allChildren = node.getAllChildren();
    for (int i = 0, max = allChildren.size(); i < max; ++i) {
      saveHideState((ParentTreeTableNode) allChildren.get(i), nodePath);
    }
  }

  public boolean selectedNodeCanBeHidden(JTree tree) {
    return!selectedNodeIsRoot(tree);
  }

  private boolean selectedNodeIsRoot(JTree tree) {
    return tree.getSelectionPath().getPathCount() < 2;
  }

  public void showHiddenRows(TreeModel m) {
    // FIXME: row hiding should also be implented some day for MultlineRowTree (FIXME Scanner) $$$

    if (m instanceof BinTreeTableModel) {
      BinTreeTableModel model = (BinTreeTableModel) m;

      model.showHiddenRows();
      this.showHiddenRows = true;
    }
  }

  public void hideHiddenRows(TreeModel m) {
    // FIXME: row hiding should also be implented some day for MultlineRowTree (FIXME Scanner) $$$

    if (m instanceof BinTreeTableModel) {
      BinTreeTableModel model = (BinTreeTableModel) m;

      model.hideHiddenRows();
      this.showHiddenRows = false;
    }
  }

  public boolean isShowHiddenRows() {
    return this.showHiddenRows;
  }

  public void reset() {
    hiddenRows.clear();
  }
}
