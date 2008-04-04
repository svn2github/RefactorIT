/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.tree;

public abstract class AbstractNode implements UITreeNode, FastNavigateable {
  private final UITreeNode parent;

  /**
   * AbstractNode constructor comment.
   */
  public AbstractNode(UITreeNode parent) {
    this.parent = parent;
  }

  /**
   * Insert the method's description here.
   * @return boolean
   */
  public boolean equals(Object o) {
    if (o == null || o.getClass() != getClass()) {
      return false;
    }

    return getDisplayName().equals(((AbstractNode) o).getDisplayName());
  }

  /**
   * Insert the method's description here.
   * @return net.sf.refactorit.ui.tree.TreeNode
   */
  public final UITreeNode getParent() {
    return parent;
  }

  /**
   * Insert the method's description here.
   * @return java.lang.String
   */
  public String toString() {
    String text = getSecondaryText();
    if (text != null) {
      return getDisplayName() + " " + text;
    }

    return getDisplayName();
  }
}
