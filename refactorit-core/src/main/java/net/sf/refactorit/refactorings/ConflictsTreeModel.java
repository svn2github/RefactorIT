/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings;

import net.sf.refactorit.ui.treetable.BinTreeTable;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;


/**
 * @author Anton Safonov
 */
public class ConflictsTreeModel extends BinTreeTableModel {
  private BinTreeTable tree = null;

  public ConflictsTreeModel() {
    super(new ConflictsTreeNode("root", false));
  }

  public void update(RefactoringStatus status) {
    //System.err.println("Status: " + status);

    ConflictsTreeNode root = (ConflictsTreeNode)this.getRoot();

    root.removeAllChildren();
    root.addChildren(status.getEntries());
    root.sortAllChildren(ConflictsTreeNode.conflictsComparator);

    fireSomethingChanged();
    if (tree != null) {
      tree.expandAll();
    }
  }

  public void setTree(BinTreeTable tree) {
    this.tree = tree;
  }

}
