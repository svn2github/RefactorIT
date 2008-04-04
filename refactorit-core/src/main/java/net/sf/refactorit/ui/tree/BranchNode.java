/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.tree;

/**
 * Signals that this node can have children nodes.
 *
 * @author Igor Malinin
 */
public abstract class BranchNode extends AbstractNode {
  /**
   * Insert the method's description here.
   * Creation date: (5/16/2001 6:23:49 AM)
   * @param parent net.sf.refactorit.ui.tree.TreeNode
   */
  public BranchNode(UITreeNode parent) {
    super(parent);
  }

  /**
   * Insert the method's description here.
   *
   * @return net.sf.refactorit.ui.tree.TreeNode
   * @param index int
   */
  public abstract UITreeNode getChildAt(int index);

  /**
   * Insert the method's description here.
   *
   * @return int
   */
  public abstract int getChildCount();

  /**
   * Insert the method's description here.
   *
   * @return int
   * @param child net.sf.refactorit.ui.tree.TreeNode
   */
  public abstract int getIndexOf(UITreeNode child);

  public final UITreeNode getChild(final String childName) {
    for (int i = 0, max = getChildCount(); i < max; i++) {
      final UITreeNode child = getChildAt(i);
      if (childName.equals(child.getDisplayName())) {
        return child;
      }
    }
    return null;
  }
}
