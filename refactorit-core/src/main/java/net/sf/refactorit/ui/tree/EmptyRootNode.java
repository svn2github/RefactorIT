/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.tree;

public final class EmptyRootNode extends AbstractNode {
  public EmptyRootNode() {
    super(null);
  }

  public final int getType() {
    return 0;
  }

  public final Object getBin() {
    return null;
  }

  public final String getDisplayName() {
    return "no data";
  }

  public final String getSecondaryText() {
    return null;
  }

  public final boolean matchesFor(String str) {
    return false;
  }
}
