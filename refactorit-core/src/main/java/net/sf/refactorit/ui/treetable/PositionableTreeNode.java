/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.treetable;


import net.sf.refactorit.classmodel.CompilationUnit;

import javax.swing.tree.DefaultMutableTreeNode;


public final class PositionableTreeNode extends DefaultMutableTreeNode {
  private final CompilationUnit compilationUnit;
  private final int line;

  public PositionableTreeNode(final String name, final CompilationUnit compilationUnit,
      final int line) {
    super(name);

    this.compilationUnit = compilationUnit;
    this.line = line;
  }

  public final int getLine() {
    return this.line;
  }

  public final CompilationUnit getCompilationUnit() {
    return this.compilationUnit;
  }

  public final boolean equals(final Object other) {
    if (other instanceof PositionableTreeNode) {
      final PositionableTreeNode otherNode = (PositionableTreeNode) other;
      return otherNode.getLine() == this.getLine() &&
          otherNode.getCompilationUnit().equals(this.getCompilationUnit());
    } else {
      return false;
    }
  }

  public final int hashCode() {
    return super.hashCode(); // default
  }
}
