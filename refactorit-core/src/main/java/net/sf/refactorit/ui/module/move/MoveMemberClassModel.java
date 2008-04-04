/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.move;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;
import net.sf.refactorit.ui.treetable.TreeTableModel;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import java.util.List;


/**
 *
 * @author Vadim Hahhulin, Anton Safonov
 */
public class MoveMemberClassModel extends BinTreeTableModel {
  private BinTreeTableNode locationsNode;
  private BinTreeTableNode projectNode;

  public MoveMemberClassModel(Project project, BinCIType nativeType,
      List probableTargetClasses) {
    super(new BinTreeTableNode("", false));

    initChildren(project, nativeType, probableTargetClasses);
  }

  private void initChildren(final Project project,
      final BinCIType nativeType,
      final List probableTargetClasses) {
    final BinTreeTableNode root = (BinTreeTableNode) getRoot();

    if (probableTargetClasses.size() > 0) {
      locationsNode = new BinTreeTableNode("Probable target classes", false);
      root.addChild(locationsNode);

      for (int i = 0, max = probableTargetClasses.size(); i < max; i++) {
        locationsNode.findParent(((BinType) probableTargetClasses.get(i)).
            getTypeRef(), false);
      }

      locationsNode.sortAllChildren();
    }

    final List definedTypes = project.getDefinedTypes();
    projectNode = new BinTreeTableNode("Project", false);
    root.addChild(projectNode);

    for (int i = 0, max = definedTypes.size(); i < max; i++) {
      final BinCIType type = ((BinTypeRef) definedTypes.get(i)).getBinCIType();
      if (type == nativeType || !type.isFromCompilationUnit()) {
        continue;
      }

      projectNode.findParent(type.getTypeRef(), false); // small feature :)
    }

    projectNode.sortAllChildren();
  }

  public void expandPath(JTree tree) {
    TreePath path;

    if (locationsNode != null) {
      List children = locationsNode.getChildren();
      for (int i = 0, max = children.size(); i < max; i++) {
        path = new TreePath(((BinTreeTableNode) children.get(i)).getPath());
        tree.expandPath(path);
      }
    }

    path = new TreePath(projectNode.getPath());
    tree.expandPath(path);
  }

  public String getColumnName(int column) {
    switch (column) {
      case 0:
        return "Class hierarchy";
    }

    return null;
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
