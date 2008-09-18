/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.eclipse;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.BidirectionalMap;
import net.sf.refactorit.commonIDE.AbstractWorkspace;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.WorkspaceManager;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;


public class EclipseWorkspace extends AbstractWorkspace {
  public EclipseWorkspace(WorkspaceManager manager) {
    super(manager);
  }

  public Project getProject(String key) {
    return (Project) getProjects().getValueByKey(key);
  }

//  public BidirectionalMap getProjects() {
//    synchronizeProjects();
//    return projects;
//  }

//  public Project getActiveProject() {
//	    Object activeIdeProject = IDEController.getInstance().getActiveProjectFromIDE();
//	    Object ideProject = IDEController.getInstance().getIDEProject();
//	    
//	    Object activeIdeProjectID = IDEController.getInstance().getWorkspaceManager()
//        .getIdeProjectIdentifier(activeIdeProject);
//	    
//	    Project project = (Project)projects.getValueByKey(activeIdeProjectID);
//	    
//	    if(project == null || activeIdeProject != ideProject) {
//	      project = IDEController.getInstance().createNewProject(activeIdeProject);
//	      if(project != null) {
//	        projects.put(activeIdeProjectID, project);
//	      }
//	    }
//	    
//	    return project;
//  }

  public BidirectionalMap getIdeProjects() {
    // TODO: optimize me
    IProject[] ideProjects = ResourcesPlugin
        .getWorkspace().getRoot().getProjects();

    BidirectionalMap projects = new BidirectionalMap(ideProjects.length, 1f);

    for (int i = 0; i < ideProjects.length; i++) {
      IProject project = ideProjects[i];

      if (project != null && project.isOpen()) {
        projects.put(IDEController.getInstance().getWorkspaceManager()
            .getIdeProjectIdentifier(project), project);
      }
    }

    return projects;
  }
  
  public Project getProject(Object ideProject) {
    Object key = getIdeProjects().getKeyByValue(ideProject);

    Project project = (Project)getProjects().getValueByKey(key);
    if (project == null) {
      project = IDEController.getInstance().createNewProject(ideProject);
      getProjects().put(key, project);
    }

    return project;
  }
}
