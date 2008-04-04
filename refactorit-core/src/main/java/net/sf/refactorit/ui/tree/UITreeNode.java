/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.tree;

public interface UITreeNode {
  // BinItem type-declarations
  int NODE_UNKNOWN = 0;
  int NODE_PACKAGE = 1;
  int NODE_CLASS = 2;
  int NODE_INTERFACE = 3;
  int NODE_TYPE_FIELD = 4;
  int NODE_TYPE_METHOD = 5;
  int NODE_TYPE_CNSTOR = 6;
  int NODE_SOURCE = 7;
  int NODE_ENUM = 10;

  // NODE_UNRESOLVED_TYPE is for BinTypeRefs we do not want getBinType()
  // to be called on so it will not become loaded
  int NODE_UNRESOLVED_TYPE = 8;

  int NODE_NON_JAVA = 9;

  /**
   * Indicates the type of the node's content.
   *
   * @see #getBin()
   */
  int getType();

  /**
   * Returns content.
   */
  Object getBin();

  /**
   * The primary text to display in the JTree or JTreeTable.
   */
  String getDisplayName();

  /**
   * The secondary text to display in the JTree or JTreeTable.
   */
  String getSecondaryText();
}
