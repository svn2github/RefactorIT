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
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinInitializer;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;

import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * Package tree model.
 *
 * @author Igor Malinin
 */
public final class InheritanceTreeModel implements TreeModel {
  public static final InheritanceTreeModel empty
      = new InheritanceTreeModel();

  private final List listeners = new ArrayList();

  private final UITreeNode root;
  private BinCIType type;
  private BinMember member;

  private InheritanceTreeModel() {
    root = new EmptyRootNode();
  }

  public InheritanceTreeModel(BinCIType type) {
    this.type = type;
    root = new InheritanceNode(null, type);
    refresh();
  }

  /**
   * Creates the Inheritance tree from Inheritance node
   * as root node.
   *
   * @param rootNode the root node from where the inheritance begins
   * it may be upwards inheritance or downwards inheritance. It depends
   * which subclass of {@link InheritanceNode} is provided into
   * constructor.
   * @see InheritanceSubTypeNode
   */
  public InheritanceTreeModel(InheritanceNode rootNode) {
    this.type = rootNode.getBinCIType();
    root = rootNode;
    refresh();
  }

  public final void setBinMember(BinMember bin) {
    member = bin;

    refresh();
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

  private void refresh() {
    refresh(new TreePath(root));
  }

  private final Set visited = new HashSet();

  private void refresh(TreePath path) {
    if (!(path.getLastPathComponent() instanceof InheritanceNode)) {
      return; // FIXME is it correct, or we should do something still?
    }

    InheritanceNode node = (InheritanceNode) path.getLastPathComponent();
    if (visited.contains(node.getBinCIType())) {
      return;
    }

    List list = node.getMembers();

    int size = list.size();

    if (size > 0) {
      Object first = list.get(0);
      if (first instanceof TypeNode.Member) {
        list.remove(0);
        fireTreeNodesRemoved(new TreeModelEvent(
            this, path, new int[] {0}
            , new Object[] {first}));
      }
    }

    visited.add(node.getBinCIType());

    Iterator i = list.iterator();
    while (i.hasNext()) {
      refresh(path.pathByAddingChild(i.next()));
    }

    visited.remove(node.getBinCIType());

    if (member != null) {
      String name = member.getName();
      TypeNode.Member add = null;
      if (member instanceof BinField) {
        BinField f = node.getBinCIType().getDeclaredField(name);
        if (f != null) {
          add = new TypeNode.Field(node, f);
        }
      } else if (member instanceof BinInitializer) {
        // initializers are not overridable
      } else {
        BinMethod m = node.getBinCIType()
            .getDeclaredMethod(member.getName(),
            ((BinMethod) member).getParameters());
        if (m != null) {
          add = new TypeNode.Method(node, m);
        }
      }

      if (add != null) {
        list.add(0, add);
        fireTreeNodesInserted(new TreeModelEvent(
            this, path, new int[] {0}
            , new Object[] {add}));
      }
    }
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

  /**
   * Removes a listener previously added with <B>addTreeModelListener()</B>.
   *
   * @see     #addTreeModelListener(TreeModelListener)
   * @param   l       the listener to remove
   */
  public final void removeTreeModelListener(TreeModelListener l) {
    listeners.remove(l);
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

  private static final Set expandVisited = new HashSet();

  public static void expandTree(TreePath path, JTree tree) {
    Object node = path.getLastPathComponent();
    if (node instanceof InheritanceNode) {
      InheritanceNode in = (InheritanceNode) node;
      if (!in.getBinCIType().isFromCompilationUnit()
          || expandVisited.contains(in.getBinCIType())) {
        return;
      }

      tree.expandPath(path);

      expandVisited.add(in.getBinCIType());

      Iterator i = in.getMembers().iterator();
      while (i.hasNext()) {
        expandTree(path.pathByAddingChild(i.next()), tree);
      }

      expandVisited.remove(in.getBinCIType());
    }
  }
}
