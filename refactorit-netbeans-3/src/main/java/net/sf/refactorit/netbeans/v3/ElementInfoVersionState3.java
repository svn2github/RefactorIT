/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.v3;


import net.sf.refactorit.netbeans.common.ElementInfoVersionState;

import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.nodes.Node;

/**
 * @author risto
 */
public class ElementInfoVersionState3 implements ElementInfoVersionState {

  public boolean isFolderNode(Node node) {
    return node.getCookie(DataFolder.class) != null &&
      (!refactoringsShouldBeProjectWide(node));
  }

  public boolean refactoringsShouldBeProjectWide(Node node) {
    return 
        isAllFileSystemsNodeOrAllMembersNode(node) ||
        isFileSystemMountNode(node) ||
        isProjectNode(node);
  }

  private boolean isAllFileSystemsNodeOrAllMembersNode(Node node) {
    return node.getCookie(DataObject.class) == null;
  }

  private boolean isProjectNode(Node node) {
    if (node.getCookie(DataShadow.class) != null) {
      DataShadow dataShadow = (DataShadow) node.getCookie(DataShadow.class);
      return dataShadow.getOriginal().getPrimaryFile().isRoot();
    }

    return false;
  }

  private boolean isFileSystemMountNode(Node node) {
    if (node.getCookie(DataObject.class) != null) {
      DataObject dataObject = (DataObject) node.getCookie(DataObject.class);
      return dataObject.getPrimaryFile().isRoot();
    }

    return false;
  }
}
