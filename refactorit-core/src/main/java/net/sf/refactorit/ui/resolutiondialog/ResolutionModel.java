/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.ui.resolutiondialog;


import net.sf.refactorit.refactorings.conflicts.resolution.ConflictResolution;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;
import net.sf.refactorit.ui.treetable.TreeTableModel;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import java.util.List;


/**
 *
 * @author vadim
 */
public class ResolutionModel extends BinTreeTableModel {

  public ResolutionModel(Object root, List resolutions) {
    super(new BinTreeTableNode(root, false));

    createTree(resolutions);
  }

  private void createTree(List resolutions) {
    BinTreeTableNode root = (BinTreeTableNode) getRoot();
    for (int i = 0, max = resolutions.size(); i < max; i++) {
      ConflictResolution resolution = (ConflictResolution) resolutions.get(i);
      ResolutionNode resolutionNode = new ResolutionNode(resolution);
      root.addChild(resolutionNode);

      List downMembers = resolution.getDownMembers();
      for (int j = 0, maxJ = downMembers.size(); j < maxJ; j++) {
        resolutionNode.addChild(new ResolutionNode(downMembers.get(j),
            resolution));
      }
    }
  }

  public void expandPath(JTree tree) {
    BinTreeTableNode root = (BinTreeTableNode) getRoot();
    TreePath path;

    List children = root.getChildren();
    for (int i = 0, max = children.size(); i < max; i++) {
      path = new TreePath(((BinTreeTableNode) children.get(i)).getPath());
      tree.expandPath(path);
    }
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
