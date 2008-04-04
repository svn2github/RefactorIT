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
import net.sf.refactorit.common.util.BidirectionalMap;


/**
  Default workspace implementation
*/
public class DefaultWorkspace extends AbstractWorkspace {

  
  public DefaultWorkspace(WorkspaceManager manager) {
    super(manager);
  }

  /**
   * @see net.sf.refactorit.commonIDE.Workspace#getProject(java.lang.String)
   */
  public Project getProject(String name) {
    return (Project) getProjects().getValueByKey(name);
  }

  public BidirectionalMap getIdeProjects() {
    BidirectionalMap ideProjects = new BidirectionalMap();
    Object project = IDEController.getInstance().getActiveProjectFromIDE();
    if(project != null) {
      ideProjects.put(IDEController.getInstance().getWorkspaceManager()
          .getIdeProjectIdentifier(project), project);
    }
    return ideProjects;
  }
  
  public Project getProject(Object ideProject) {
    Object key = getIdeProjects().getKeyByValue(ideProject);
    Project project = (Project)getProjects().getValueByKey(key);
    if(project == null) {
      project = IDEController.getInstance().createNewProject(ideProject);
      getProjects().put(key, project);
    }
    return project;
  }

}
