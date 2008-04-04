/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.tree;


import net.sf.refactorit.classmodel.BinPackage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Node representing java package.
 *
 * @author Igor Malinin
 */
public final class PackageNode extends BranchNode implements Comparator {
  private final BinPackage bin;
  final List nodes = new ArrayList(5);

  public PackageNode(UITreeNode parent, BinPackage bin) {
    super(parent);

    this.bin = bin;
  }

  public final int getType() {
    return UITreeNode.NODE_PACKAGE;
  }

  public final String getDisplayName() {
    return bin.getQualifiedName();
  }

  public final String getSecondaryText() {
    return null;
  }

  public final boolean matchesFor(String str) {
    String binName = bin.getQualifiedName().toLowerCase();
    return (binName.startsWith(str.toLowerCase()));
  }

  public final Object getBin() {
    return bin;
  }

  public final UITreeNode getChildAt(int index) {
    return (UITreeNode) nodes.get(index);
  }

  public final int getChildCount() {
    return nodes.size();
  }

  public final int getIndexOf(UITreeNode child) {
    return nodes.indexOf(child);
  }

  public final void sortChildren() {
    Collections.sort(nodes, this);
  }

  /** For sorting of children which are all types. */
  public final int compare(Object o1, Object o2) {
    String name1 = ((TypeNode) o1).getDisplayName();
    String name2 = ((TypeNode) o2).getDisplayName();

    return name1.compareTo(name2);
  }
}
