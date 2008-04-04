/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jbuilder;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.BidirectionalMap;
import net.sf.refactorit.commonIDE.AbstractWorkspace;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.WorkspaceManager;



public class JBWorkspace extends AbstractWorkspace {

  public JBWorkspace(WorkspaceManager manager) {
    super(manager);
  }

  public Project getProject(String key) {
    return (Project) getProjects().getValueByKey(key);
  }

  public Project getProject(Object ideProject) {
    Object key = getIdeProjects().getKeyByValue(ideProject);
    Project project = (Project) getProjects().getValueByKey(key);
    if (project == null) {
      project = IDEController.getInstance().createNewProject(ideProject);
      getProjects().put(key, project);
    }
    return project;
  }

  public BidirectionalMap getIdeProjects() {
    Object[] ideProjects = JBController.getProjectsFromIDE();
    BidirectionalMap projects = new BidirectionalMap(ideProjects.length, 1f);
    for (int i = 0; i < ideProjects.length; i++) {
      Object project = ideProjects[i];
      projects.put(IDEController.getInstance().getWorkspaceManager()
          .getIdeProjectIdentifier(project), project);
    }
    return projects;
  }

}
