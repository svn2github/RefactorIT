/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.commonIDE;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.CollectionUtil;

import java.util.List;

public class DefaultWorkspaceManager extends WorkspaceManager {
  private static WorkspaceManager manager;

  private DefaultWorkspaceManager() {}

  protected Object getIdeProject(String projectIdent) {
    return getWorkspace().getIdeProjects().getValueByKey(projectIdent);
  }


  public static WorkspaceManager getInstance() {
    if(manager == null) {
      manager = new DefaultWorkspaceManager();
    }

    return manager;
  }

  public Workspace getWorkspace() {
    if(workspace == null) {
      workspace = new DefaultWorkspace(this);
    }
    return workspace;
  }


  public Object getIdeProjectIdentifier(Object ideProject) {
    assert (ideProject != null);
    return ideProject.toString();
  }

  public Object getProjectIdentifier(Project activeProject) {
    return getWorkspace().getProjects().getKeyByValue(activeProject);
  }

  public List getReferencedInProjects(Object projectIdentificator) {
    return CollectionUtil.EMPTY_ARRAY_LIST;
  }

  public List getDependsOnProjects(Object projectIdentificator) {
    return CollectionUtil.EMPTY_ARRAY_LIST;
  }
}
