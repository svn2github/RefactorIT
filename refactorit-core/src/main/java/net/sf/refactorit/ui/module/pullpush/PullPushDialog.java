/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.pullpush;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.refactorings.conflicts.ConflictResolver;
import net.sf.refactorit.ui.module.MoveDialog;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.tree.BinTree;
import net.sf.refactorit.ui.tree.InheritanceNode;
import net.sf.refactorit.ui.tree.InheritanceSuperAndSubNode;
import net.sf.refactorit.ui.tree.InheritanceTreeModel;
import net.sf.refactorit.ui.tree.JClassTree;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import java.awt.BorderLayout;
import java.util.ResourceBundle;


/**
 *
 * @author vadim
 */
public class PullPushDialog extends MoveDialog {
  private static ResourceBundle bundle = ResourceUtil.getBundle(PullPushDialog.class);
  JClassTree classTree;
  JClassTree interfTree;
  private int rowOfOwner = 1;

  public PullPushDialog(ConflictResolver resolver, RefactorItContext context) {
    super(resolver, context, bundle.getString("dialog.title"),
        "closeActionOfPullPush", "refact.pull_up_push_down");
  }

  protected JComponent createHierarchyPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    JTabbedPane tabbedPane = new JTabbedPane();

    tabbedPane.addTab("Class Hierarchy", createClassHierarchy());
    tabbedPane.addTab("Interface Hierarchy", createInterfaceHierarchy());

    panel.add(new JScrollPane(tabbedPane), BorderLayout.CENTER);

    return panel;
  }

  private JScrollPane createClassHierarchy() {
    BinCIType lastSuper = findLastSuper(resolver.getNativeType());
    if (lastSuper == resolver.getNativeType()) {
      lastSuper = null;
    }

    TreeModel model = new InheritanceTreeModel(
        new InheritanceSuperAndSubNode(null, lastSuper,
        resolver.getNativeType()));

    classTree = new JClassTree(model, context);
    classTree.setRootVisible(true);
    classTree.getSelectionModel().setSelectionMode(TreeSelectionModel.
        SINGLE_TREE_SELECTION);
    InheritanceTreeModel.expandTree(classTree.getPathForRow(0), classTree);
    classTree.setSelectionRow(rowOfOwner);

    addListenerForClassTree(classTree);

    return new JScrollPane(classTree);
  }

  private BinCIType findLastSuper(BinCIType type) {
    BinTypeRef superRef = type.getTypeRef().getSuperclass();

    if (superRef == null) {
      return type;
    } else {
      if (superRef.getBinCIType().isFromCompilationUnit()) {
        rowOfOwner++;
        return findLastSuper(superRef.getBinCIType());
      }

      return type;
    }
  }

  private void addListenerForClassTree(BinTree tree) {
    tree.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        if (!e.isAddedPath()) {
          return;
        }

        Object bin = ((InheritanceNode)
            e.getPath().getLastPathComponent()).getBin();

        interfTree.clearSelection();

        setSelectedClass((BinCIType) bin);
      }
    });
  }

  private void addInterfaceNodes(BinTypeRef typeRef, BinTreeTableNode parent) {
    BinTypeRef[] interfaces = typeRef.getInterfaces();

    for (int i = 0; i < interfaces.length; i++) {
      BinTreeTableNode child = new BinTreeTableNode(interfaces[i].getBinCIType());
      parent.addChild(child);
      if (interfaces[i].getInterfaces().length > 0) {
        addInterfaceNodes(interfaces[i], child);
      }
    }
  }

  private JScrollPane createInterfaceHierarchy() {
    BinTreeTableNode root = new BinTreeTableNode("");
    BinTreeTableModel model = new BinTreeTableModel(root);

    if (resolver.getNativeType().getTypeRef().getInterfaces().length > 0) {
      BinTreeTableNode child = new BinTreeTableNode(resolver.getNativeType());
      root.addChild(child);
      addInterfaceNodes(resolver.getNativeType().getTypeRef(), child);
    }

    interfTree = new JClassTree(model, context);
    expandAll(interfTree);
    interfTree.getSelectionModel().setSelectionMode(TreeSelectionModel.
        SINGLE_TREE_SELECTION);

    addListenerForInterfaceTree(interfTree);

    return new JScrollPane(interfTree);
  }

  private void expandAll(JClassTree tree) {
    BinTreeTableNode root = (BinTreeTableNode) tree.getModel().getRoot();
    expandRecursively(tree, new TreePath(root));
  }

  private void expandRecursively(JClassTree tree, TreePath path) {
    final BinTreeTableNode node = (BinTreeTableNode) path.getLastPathComponent();
    tree.expandPath(path);

    for (int i = 0, max = node.getChildCount(); i < max; i++) {
      expandRecursively(tree, path.pathByAddingChild(node.getChildAt(i)));
    }
  }

  private void addListenerForInterfaceTree(BinTree tree) {
    tree.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        if (!e.isAddedPath()) {
          return;
        }

        Object bin = ((BinTreeTableNode)
            e.getPath().getLastPathComponent()).getBin();

        classTree.clearSelection();

        setSelectedClass((BinCIType) bin);
      }
    });
  }
}
