/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.tree;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * Package tree model.
 *
 * @author Igor Malinin
 */
public final class PackageTreeModel implements TreeModel {
  public static final PackageTreeModel EMPTY
      = new PackageTreeModel();

  private final List listeners = new ArrayList();

  private int filter;

  private Project project;

  private final PackageListNode root;

  private PackageTreeModel() {
    root = new PackageListNode();
  }

  public PackageTreeModel(Project project, String name) {
    this.project = project;

    root = new PackageListNode(name);

    rebuild();
  }

  /**
   * Adds a listener for the TreeModelEvent posted after the tree changes.
   *
   * @see     #removeTreeModelListener(TreeModelListener)
   * @param   l       the listener to add
   */
  public final void addTreeModelListener(TreeModelListener l) {
    listeners.add(l);
  }

  protected final void fireTreeNodesInserted(TreeModelEvent e) {
    for (int i = 0; i < listeners.size(); i++) {
      ((TreeModelListener) listeners.get(i)).treeNodesInserted(e);
    }
  }

  protected final void fireTreeNodesRemoved(TreeModelEvent e) {
    for (int i = 0; i < listeners.size(); i++) {
      ((TreeModelListener) listeners.get(i)).treeNodesRemoved(e);
    }
  }

  protected final void fireTreeStructureChanged(TreeModelEvent e) {
    for (int i = 0; i < listeners.size(); i++) {
      ((TreeModelListener) listeners.get(i)).treeStructureChanged(e);
    }
  }

  /**
   * Returns the child of <I>parent</I> at index <I>index</I> in the parent's
   * child array.  <I>parent</I> must be a node previously obtained from
   * this data source. This should not return null if <i>index</i>
   * is a valid index for <i>parent</i> (that is <i>index</i> >= 0 &&
   * <i>index</i> < getChildCount(<i>parent</i>)).
   *
   * @param   parent  a node in the tree, obtained from this data source
   * @return  the child of <I>parent</I> at index <I>index</I>
   */
  public final Object getChild(Object parent, int index) {
    return ((BranchNode) parent).getChildAt(index);
  }

  /**
   * Returns the number of children of <I>parent</I>.  Returns 0 if the node
   * is a leaf or if it has no children.  <I>parent</I> must be a node
   * previously obtained from this data source.
   *
   * @param   parent  a node in the tree, obtained from this data source
   * @return  the number of children of the node <I>parent</I>
   */
  public final int getChildCount(Object parent) {
    if (parent instanceof BranchNode) {
      return ((BranchNode) parent).getChildCount();
    }

    return 0;
  }

  /**
   * Returns the index of child in parent.
   */
  public final int getIndexOfChild(Object parent, Object child) {
    return ((BranchNode) parent).getIndexOf((UITreeNode) child);
  }

  /**
   * Returns the root of the tree.  Returns null only if the tree has
   * no nodes.
   *
   * @return  the root of the tree
   */
  public final Object getRoot() {
    return root;
  }

  /**
   * Returns true if <I>node</I> is a leaf.  It is possible for this method
   * to return false even if <I>node</I> has no children.  A directory in a
   * filesystem, for example, may contain no files; the node representing
   * the directory is not a leaf, but it also has no children.
   *
   * @param   node    a node in the tree, obtained from this data source
   * @return  true if <I>node</I> is a leaf
   */
  public final boolean isLeaf(Object node) {
    return!(node instanceof BranchNode)
        || ((BranchNode) node).getChildCount() == 0;
  }

  public final void rebuild() {
    if (project == null) {return;
    }

    final Map pkgNodes = new TreeMap();

    root.packages.clear();

    Iterator i = project.getDefinedTypes().iterator();
    while (i.hasNext()) {
      BinTypeRef ref = (BinTypeRef) i.next();
      BinCIType type = ref.getBinCIType();
      if (type == null) {
        new Exception("ref has no type: " + ref).printStackTrace(System.err);
      }
      if (type.isInnerType()) {
        continue;
      }

      BinPackage pkg = type.getPackage();

      String pkgName = pkg.getQualifiedName();

      PackageNode pkgNode = (PackageNode) pkgNodes.get(pkgName);
      if (pkgNode == null) {
        pkgNode = new PackageNode(root, pkg);
        pkgNodes.put(pkgName, pkgNode);
        root.packages.add(pkgNode);
      }

      TypeNode node = new TypeNode(pkgNode, type);

      pkgNode.nodes.add(node);
    }

    pkgNodes.clear(); // clean it right away

    root.sortChildren();

    Object[] path = {root};
    fireTreeStructureChanged(new TreeModelEvent(this, path));
  }

  public final void filter(int filter) {
    this.filter = filter;

    for (int i = 0, max = root.getChildCount(); i < max; i++) {
      PackageNode pkg = (PackageNode) root.getChildAt(i);
      for (int node = 0, nodeMax = pkg.getChildCount();
          node < nodeMax; node++) {
        filter(new TreePath(new Object[] {
            root, pkg, (TypeNode) pkg.getChildAt(node),
        }));
      }
    }
  }

  private void filter(TreePath path) {
    TypeNode node = (TypeNode) path.getLastPathComponent();

    node.filter(filter);

    List members = node.members;

    if (members != null) {
      int len = members.size();
      if (len == 0 || !(members.get(0) instanceof TypeNode)) {
        // no inner classes - full refresh!
        node.members = null;
        fireTreeStructureChanged(new TreeModelEvent(this, path));

        return;
      }

      for (int i = 0; i < len; i++) {
        Object o = members.get(i);
        if (o instanceof TypeNode) {
          // refresh inner classes
          filter(path.pathByAddingChild(o));
          continue;
        }

        if (i == 0) {
          // no inner classes - full refresh!
          node.members = null;
          fireTreeStructureChanged(new TreeModelEvent(this, path));

          return;
        }

        int diff = len - i;
        if (diff > 0) {
          int[] indexes = new int[diff];
          Object[] nodes = new Object[diff];
          for (int j = i, n = 0; j < len; j++, n++) {
            indexes[n] = j;
            nodes[n] = members.get(j);
          }

          members = new ArrayList(members.subList(0, i));
          node.members = members;

          fireTreeNodesRemoved(new TreeModelEvent(
              this, path, indexes, nodes));
        }

        break;
      }

      int i = members.size();

      node.initFields(members);
      node.initMethods(members);

      len = members.size();

      int diff = len - i;
      if (diff > 0) {
        int[] indexes = new int[diff];
        Object[] nodes = new Object[diff];
        for (int j = i, n = 0; j < len; j++, n++) {
          indexes[n] = j;
          nodes[n] = members.get(j);
        }

        fireTreeNodesInserted(new TreeModelEvent(
            this, path, indexes, nodes));
      }
    }
  }

  /**
   * Removes a listener previously added with <B>addTreeModelListener()</B>.
   *
   * @see     #addTreeModelListener(TreeModelListener)
   * @param   l       the listener to remove
   */
  public final void removeTreeModelListener(TreeModelListener l) {
    listeners.remove(l);
  }

  /**
   * Messaged when the user has altered the value for the item identified
   * by <I>path</I> to <I>newValue</I>.  If <I>newValue</I> signifies
   * a truly new value the model should post a treeNodesChanged
   * event.
   *
   * @param path path to the node that the user has altered.
   * @param newValue the new value from the TreeCellEditor.
   */
  public final void valueForPathChanged(TreePath path, Object newValue) {}
}
