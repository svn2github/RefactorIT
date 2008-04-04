/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jbuilder;


import com.borland.jbuilder.node.JBProject;

import java.util.List;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.commonIDE.Workspace;
import net.sf.refactorit.commonIDE.WorkspaceManager;


public class JBWorkspaceManager extends WorkspaceManager {
  private static WorkspaceManager manager;

  public Workspace getWorkspace() {
    if (workspace == null) {
      workspace = new JBWorkspace(this);
    }
    return workspace;
  }

  public static WorkspaceManager getInstance() {
    if (manager == null) {
      manager = new JBWorkspaceManager();
    }
    return manager;
  }

  protected Object getIdeProject(String projectIdent) {
    return getWorkspace().getIdeProjects().getValueByKey(projectIdent);
  }

  public List getReferencedInProjects(Object projectIdentificator) {
    return CollectionUtil.EMPTY_ARRAY_LIST;
  }

  public List getDependsOnProjects(Object projectIdentificator) {
    return CollectionUtil.EMPTY_ARRAY_LIST;
  }

  public Object getIdeProjectIdentifier(Object ideProject) {
    assert(ideProject != null);
    try {
      return ((JBProject) ideProject).getLongDisplayName();
    } catch (ClassCastException e) {
      return ideProject.toString();
    }
  }

  public Object getProjectIdentifier(Project activeProject) {
    return getWorkspace().getProjects().getKeyByValue(activeProject);
  }

}
