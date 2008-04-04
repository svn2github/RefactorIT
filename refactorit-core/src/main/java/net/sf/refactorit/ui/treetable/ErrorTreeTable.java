/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.treetable;


import net.sf.refactorit.ui.module.RefactorItContext;

import javax.swing.tree.TreePath;


public class ErrorTreeTable extends BinTreeTable {
  public ErrorTreeTable (final BinTreeTableModel model,
      final RefactorItContext context){
    super (model, JTreeTable.CHECKBOX_STYLE, context);
  }

  //expands till compilation units, errors remain hidden
  //and must be opened manually
  void expandRecursively(final TreePath path) {
    if (path == null) {
      return;
    }

    final Object node = path.getLastPathComponent();
    if (node instanceof ParentTreeTableNode) {
      final ParentTreeTableNode in = (ParentTreeTableNode) node;

      getTree().expandPath(path);

      for (int i = 0, max = in.getChildCount(); i < max; i++) {
        ParentTreeTableNode child = (ParentTreeTableNode) in.getChildAt(i);
        if (child.getChildCount() > 0 &&
            ((ParentTreeTableNode) child.getChildAt(0)).getChildCount() > 0) {
          expandRecursively(path.pathByAddingChild(child));
        }
      }
    }
  }

  public void smartExpand() {
    final int count = getCUnitsCount((ParentTreeTableNode) getBinTreeTableModel().getRoot());
    if (count <= 7) {
      expandAll();
    }
  }

  //Returns number of compilation units with errors
  private int getCUnitsCount(final ParentTreeTableNode node) {
    final int children = node.getChildCount();
    if (children > 0 &&
        ((BinTreeTableNode) node.getChildAt(0)).getChildCount() == 0) {
      return 1; // compilation unit, children are errors
    }

    int count = 0;
    for (int i = 0; i < children; i++) {
      count += getCUnitsCount((ParentTreeTableNode) node.getChildAt(i));
      if (count > 7) { // shortcut
        break;
      }
    }

    return count;
  }
}
