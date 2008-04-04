/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.extract;


import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.tree.BinTree;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import java.awt.Container;
import java.awt.Dimension;


public class FlowDebug {
  final RitDialog dialog;

  private VariableUseAnalyzer analyzer;
  private JTabbedPane beforeAfter = new JTabbedPane(JTabbedPane.VERTICAL);

  public FlowDebug(IdeWindowContext context, VariableUseAnalyzer analyzer) {
    dialog = RitDialog.create(context);
    this.analyzer = analyzer;
    init();
  }

  public void show() {
    JTabbedPane tabs = new JTabbedPane();
    beforeAfter.add("after", tabs);
    addTreesToTabs(tabs);

    dialog.show();
  }

  private void init() {
    Dimension dim = dialog.getMaximumSize();
    dialog.setSize(dim.width - 10, dim.height - 30);

    Container cp = dialog.getContentPane();
    cp.add(beforeAfter);

    JTabbedPane tabs = new JTabbedPane();
    beforeAfter.add("before", tabs);
    addTreesToTabs(tabs);
  }

  private void addTreesToTabs(final JTabbedPane tabs) {
    BinLocalVariable[] vars = analyzer.getAllVariables();

    for (int i = 0; i < vars.length; ++i) {
      TreeNode root = buildTree(analyzer.topBlock, vars[i]);
      DefaultTreeModel model = new DefaultTreeModel(root);
      BinTree tree = new BinTree(model);
      JScrollPane scroll = new JScrollPane(tree);
      tree.expandAll();
      tree.optionsChanged();
      scroll.setHorizontalScrollBarPolicy(
          JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

      tabs.add(vars[i].getName(), scroll);
    }
  }

  private DefaultMutableTreeNode buildTree(
      FlowAnalyzer.Flow flow, BinLocalVariable var
  ) {
    DefaultMutableTreeNode node =
        new DefaultMutableTreeNode(flow.toString() + " -- "
        + flow.getDirectInfo(var));
    if (flow.children != null) {
      for (int i = 0; i < flow.children.size(); ++i) {
        node.add(buildTree((FlowAnalyzer.Flow) flow.children.get(i), var));
      }
    }
    return node;
  }
}
