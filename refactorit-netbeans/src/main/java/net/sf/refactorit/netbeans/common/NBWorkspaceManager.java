/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.BidirectionalMap;
import net.sf.refactorit.commonIDE.Workspace;
import net.sf.refactorit.commonIDE.WorkspaceManager;


public class NBWorkspaceManager extends WorkspaceManager {
  private static WorkspaceManager manager;

  public Workspace getWorkspace() {
    if(workspace == null) {
      workspace = new NBWorkspace(this);
    }
    return workspace;
  }

  public static WorkspaceManager getInstance() {
    if(manager == null) {
      manager = new NBWorkspaceManager();
    }

    return manager;
  }

  protected Object getIdeProject(String projectIdent) {
    return getWorkspace().getIdeProjects().getValueByKey(projectIdent);
  }

  public List getReferencedInProjects(Object projectIdentificator) {
    BidirectionalMap ideProjects = getWorkspace().getIdeProjects();
    Set ideProjectSet = ideProjects.getValueSetCopy();
    ArrayList referencedProjects = new ArrayList(3);

    for (Iterator it = ideProjectSet.iterator(); it.hasNext();) {
      Object ideProject = it.next();
      Object[] requiredProjectKeys = VersionSpecific.getInstance()
          .getRequiredProjectKeys(ideProject);
      if (Arrays.asList(requiredProjectKeys).contains(projectIdentificator)) {
        referencedProjects.add(getWorkspace().getProject(ideProject));
      }
    }

    return referencedProjects;
  }

  public List getDependsOnProjects(Object projectIdentificator) {
    //return CollectionUtil.EMPTY_ARRAY_LIST;
    // FIXME: uncomment this code after  NBController.createNewProjectFromIdeProject() is fixed
    BidirectionalMap ideProjects = getWorkspace().getIdeProjects();
    Object ideProject = ideProjects.getValueByKey(projectIdentificator);
    if (ideProject == null) {
      throw new RuntimeException("Specified project (" + projectIdentificator
          + ") cannot be located");
    }
    Object[] requiredProjectKeys = VersionSpecific.getInstance().getRequiredProjectKeys(ideProject);
    ArrayList dependentProjects = new ArrayList(3);
    for(int i = 0; i < requiredProjectKeys.length; i++) {
        Object candidateProject = ideProjects.getValueByKey(requiredProjectKeys[i]);
        if(candidateProject == null) {
          continue;
        }
        Project project = getWorkspace().getProject(candidateProject);
        dependentProjects.add(project);
      }
    return dependentProjects;
  }

  public Object getIdeProjectIdentifier(Object ideProject) {
    assert (ideProject != null);
    return VersionSpecific.getInstance().getUniqueProjectIdentifier(ideProject);
}

  public Object getProjectIdentifier(Project activeProject) {
    return getWorkspace().getProjects().getKeyByValue(activeProject);
  }

}
