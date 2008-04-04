/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.tree;

import net.sf.refactorit.commonIDE.IDEController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Root node for package list.
 *
 * @author Igor Malinin
 */
public final class PackageListNode extends BranchNode implements Comparator {
  final List packages = new ArrayList(1);

  private final String name;

  public PackageListNode() {
    super(null); // root
    this.name = "no project";
  }

  public PackageListNode(String name) {
    super(null); // root
    this.name = name;
  }

  public final int getType() {
    return UITreeNode.NODE_UNKNOWN;
  }

  public final String getDisplayName() {
    return name;
  }

  public final String getSecondaryText() {
    return null;
  }

  public final boolean matchesFor(String str) {
    return false;
  }

  public final Object getBin() {
    // FIXME: this will fail if we start supporting multi-projects in standalone
    return IDEController.getInstance().getActiveProject();
  }

  public final UITreeNode getChildAt(int index) {
    return (UITreeNode) packages.get(index);
  }

  public final int getChildCount() {
    return packages.size();
  }

  public final int getIndexOf(UITreeNode child) {
    return packages.indexOf(child);
  }

  public final void sortChildren() {
    Collections.sort(packages, this);
    for (int i = 0, max = this.getChildCount(); i < max; i++) {
      ((PackageNode)this.getChildAt(i)).sortChildren();
    }
  }

  /** For sorting of children which are all packages. */
  public final int compare(Object o1, Object o2) {
    String name1 = ((PackageNode) o1).getDisplayName();
    String name2 = ((PackageNode) o2).getDisplayName();

    return name1.compareTo(name2);
  }
}
