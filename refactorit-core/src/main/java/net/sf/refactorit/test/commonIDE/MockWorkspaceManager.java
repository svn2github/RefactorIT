/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.commonIDE;


import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.cli.Arguments;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.commonIDE.Workspace;
import net.sf.refactorit.commonIDE.WorkspaceManager;

import java.util.List;

public class MockWorkspaceManager extends WorkspaceManager {
  private static WorkspaceManager manager;

  public static WorkspaceManager getInstance() {
    if(manager == null) {
      manager = new MockWorkspaceManager();
    }

    return manager;
  }

  public Workspace getWorkspace() {
    if(workspace == null) {
      workspace = new MockWorkspace(this);
    }

    return workspace;
  }

  protected Object getIdeProject(String projectIdent) {
    return getWorkspace().getIdeProjects().getValueByKey(projectIdent);
  }

  public Object getIdeProjectIdentifier(Object ideProject) {
    assert (ideProject != null);
    return ((Arguments) ideProject).getUniqueID();
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
