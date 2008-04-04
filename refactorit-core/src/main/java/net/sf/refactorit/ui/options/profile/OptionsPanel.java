/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.options.profile;

import net.sf.refactorit.ui.checktree.CheckTreeNode;

/**
 * @author Anton Safonov
 */
public interface OptionsPanel {

  void setProfile(Profile profile);
  void setTreeNode(CheckTreeNode treeNode);
  public CheckTreeNode getTreeNode();
}
