/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.fixmescanner;


import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.ui.tree.TitledValue;
import net.sf.refactorit.ui.treetable.PositionableTreeNode;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;


public class FileOrderedViewBuilder implements ViewBuilder {
  private HashMap treeNodesForPackages = new HashMap();
  private DefaultTreeModel model;

  private CompilationUnit compilationUnit;
  private DefaultMutableTreeNode currentFileNode;

  private static ResourceBundle resLocalizedStrings =
      ResourceUtil.getBundle(FixmeScannerTreeModel.class);

  public FileOrderedViewBuilder(DefaultTreeModel model) {
    this.model = model;
  }

  public void startNewFile(CompilationUnit compilationUnit) {
    this.compilationUnit = compilationUnit;

    this.currentFileNode = new DefaultMutableTreeNode(
        new TitledValue(
        removeFolders(this.compilationUnit.getSource().getRelativePath()),
        this.compilationUnit
        )
        );

    if (hasTreeNode(this.compilationUnit.getPackage())) {
      getTreeNodeFor(this.compilationUnit.getPackage()).add(this.currentFileNode);
    } else {
      createTreeNodeFor(this.compilationUnit.getPackage()).add(this.currentFileNode);
    }
  }

  public void addComment(Comment comment) {
    PositionableTreeNode newNode = new PositionableTreeNode(
        CommentBodyEditor.trimAllLines(comment.getText()), this.compilationUnit,
        comment.getStartLine());
    this.currentFileNode.add(newNode);
  }

  public void finish() {
    DefaultMutableTreeNode root = (DefaultMutableTreeNode)this.model.getRoot();

    // Sort package names
    sortChildrenByName(root);

    // Sort file names
    for (int i = 0; i < root.getChildCount(); i++) {
      DefaultMutableTreeNode child = (DefaultMutableTreeNode) root.getChildAt(i);
      sortChildrenByName(child);
    }
  }

  private void sortChildrenByName(DefaultMutableTreeNode node) {
    List children = new ArrayList();
    for (int i = 0; i < node.getChildCount(); i++) {
      children.add(node.getChildAt(i));
    }
    node.removeAllChildren();

    Collections.sort(children, new StringUtil.ToStringComparator());

    for (Iterator i = children.iterator(); i.hasNext(); ) {
      node.add((DefaultMutableTreeNode) i.next());
    }
  }

  private boolean hasTreeNode(BinPackage aPackage) {
    return getTreeNodeFor(aPackage) != null;
  }

  private DefaultMutableTreeNode getTreeNodeFor(BinPackage aPackage) {
    return (DefaultMutableTreeNode) treeNodesForPackages.get(aPackage);
  }

  private DefaultMutableTreeNode createTreeNodeFor(BinPackage aPackage) {
    DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new TitledValue(
        getDisplayName(aPackage), aPackage));
    ((DefaultMutableTreeNode) model.getRoot()).add(newNode);
    treeNodesForPackages.put(aPackage, newNode);

    return newNode;
  }

  private String getDisplayName(BinPackage aPackage) {
    if ("".equals(aPackage.getQualifiedName())) {
      return resLocalizedStrings.getString("untitled-package.name");
    } else {
      return aPackage.getQualifiedName();
    }
  }

  private String removeFolders(String fileName) {
    int lastSlashPosition = Math.max(fileName.lastIndexOf("\\"),
        fileName.lastIndexOf("/"));
    return fileName.substring(lastSlashPosition + 1);
  }
}
