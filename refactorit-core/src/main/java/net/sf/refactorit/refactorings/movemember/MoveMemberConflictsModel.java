/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.movemember;


import net.sf.refactorit.refactorings.ConflictsTreeNode;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.ui.treetable.BinTreeTable;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;
import net.sf.refactorit.ui.treetable.TreeTableModel;

import javax.swing.tree.TreePath;

import java.util.List;


/**
 *
 * @author Vadim Hahhulin, Anton Safonov
 */
public abstract class MoveMemberConflictsModel extends BinTreeTableModel {
  private BinTreeTable treeTable;

  public MoveMemberConflictsModel(Object root) {
    super(new ConflictsTreeNode(root, false));
  }

  public abstract void update();

  protected void expandPath() {
    if (treeTable != null) {
      ConflictsTreeNode root = (ConflictsTreeNode) getRoot();
      TreePath path = new TreePath(root.getPath());
      for (int i = 0, max = root.getChildCount(); i < max; i++) {
        treeTable.getTree().expandPath(path.pathByAddingChild(root.getChildAt(i)));
      }
    }
  }

  protected BinTreeTableNode addConflictNode(RefactoringStatus.Entry entry,
      BinTreeTableNode node) {
    ConflictsTreeNode conflictNode = new ConflictsTreeNode(entry, false);
    node.addChild(conflictNode);

    List subEntries = entry.getSubEntries();
    for (int i = 0, max = subEntries.size(); i < max; i++) {
      addConflictNode((RefactoringStatus.Entry) subEntries.get(i), conflictNode);
    }

    return conflictNode;
  }

  public void setTreeTable(BinTreeTable treeTable) {
    this.treeTable = treeTable;
  }

  public Class getColumnClass(int column) {
    switch (column) {
      case 0:
        return TreeTableModel.class;
    }

    return null;
  }

  public boolean isShowing(int column) {
    return true;
  }

  public Object getChild(Object node, int num) {
    return ((BinTreeTableNode) node).getChildAt(num);
  }

  public int getChildCount(Object node) {
    return ((BinTreeTableNode) node).getChildCount();
  }

  public Object getValueAt(Object node, int column) {
    switch (column) {
      case 0:
        return node;
    }

    return null;
  }

  public boolean isCellEditable(int column) {
    return false;
  }
}
