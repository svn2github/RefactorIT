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
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.commonIDE.Workspace;
import net.sf.refactorit.commonIDE.WorkspaceManager;
import net.sf.refactorit.test.TestProject;

import java.util.List;

public class NullWorkspaceManager extends WorkspaceManager {
  private static WorkspaceManager manager;

  private final MultiValueMap referencedIn = new MultiValueMap(3);
  private final MultiValueMap dependsOn = new MultiValueMap(3);

  public static WorkspaceManager getInstance() {
    if(manager == null) {
      manager = new NullWorkspaceManager();
    }

    return manager;
  }

  public Workspace getWorkspace() {
    if(workspace == null) {
      workspace = new NullWorkspace(this);
    }

    return workspace;
  }

  protected Object getIdeProject(String projectIdent) {
    return getWorkspace().getIdeProjects().getValueByKey(projectIdent);
  }

  public Object getIdeProjectIdentifier(Object ideProject) {
    assert (ideProject != null);
    return ((TestProject)ideProject).getUniqueID();
  }

  public Object getProjectIdentifier(Project activeProject) {
    return getWorkspace().getProjects().getKeyByValue(activeProject);
  }

  public List getReferencedInProjects(Object projectIdentificator) {
    Project project = (Project) getWorkspace().getProjects().getValueByKey(
        projectIdentificator);
    List referenced = this.referencedIn.get(project);
    if (referenced != null) {
      return referenced;
    }
    return CollectionUtil.EMPTY_ARRAY_LIST;
  }

  public List getDependsOnProjects(Object projectIdentificator) {
    Project project = (Project) getWorkspace().getProjects().getValueByKey(
        projectIdentificator);
    List depends = this.dependsOn.get(project);
    if (depends != null) {
      return depends;
    }
    return CollectionUtil.EMPTY_ARRAY_LIST;
  }

  public void linkProjects(Project depends, Project referenced) {
    this.dependsOn.put(depends, referenced);
    this.referencedIn.put(referenced, depends);
  }

  public void clear() {
    this.dependsOn.clear();
    this.referencedIn.clear();
  }
}
