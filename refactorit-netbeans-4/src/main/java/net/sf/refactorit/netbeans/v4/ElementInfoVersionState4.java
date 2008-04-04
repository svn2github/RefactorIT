/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.v4;


import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.netbeans.common.ElementInfoVersionState;
import net.sf.refactorit.netbeans.common.projectoptions.PathItemReference;
import net.sf.refactorit.netbeans.common.projectoptions.PathUtil;
import net.sf.refactorit.netbeans.common.vfs.NBSource;

import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

import java.util.List;

/**
 * @author risto
 *
 */
public class ElementInfoVersionState4 implements ElementInfoVersionState {

  public boolean isFolderNode(Node node) {
    return getDataFolder(node) != null;
  }

  private DataFolder getDataFolder(Node node) {
    return (DataFolder) node.getCookie(DataFolder.class);
  }

  public boolean refactoringsShouldBeProjectWide(Node node) {
    return 
        isSourcepathRootFolder(node) || 
        isProjectNode(node) || 
        isAllMembersNode(node);
  }

  private boolean isSourcepathRootFolder(Node node) {
    NBSource folderSource = getSourceIfFolder(node);
    if(folderSource == null) {
      return false;
    }
    Object ideProject = IDEController.getInstance().getActiveProjectFromIDE();
    List sourceRoots = PathItemReference.toSources(
        PathUtil.getInstance().getAutodetectedSourcepath(ideProject, false));
    return sourceRoots.contains(folderSource);
  }
  
  private NBSource getSourceIfFolder(Node node) {
    DataFolder folder = getDataFolder(node);
    if(folder == null) {
      return null;
    } else {
      return NBSource.getSource(folder.getPrimaryFile());
    }
  }

  private boolean isAllMembersNode(Node node) {
    return node.getCookie(DataObject.class) == null;
  }

  private boolean isProjectNode(Node node) {
    // FIXME: HACK
    return node.getClass().getName().indexOf("ProjectsRootNode") >= 0;
  }

}

