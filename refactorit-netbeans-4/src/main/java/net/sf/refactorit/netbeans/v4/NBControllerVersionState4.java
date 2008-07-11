/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.v4;

import net.sf.refactorit.netbeans.common.NBControllerVersionState;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;


/**
 * @author Juri Reinsalu
 */
public class NBControllerVersionState4 implements NBControllerVersionState {
  private NB40ProjectChangeTracker projectTracker;

  public NBControllerVersionState4() {
    projectTracker = NB40ProjectChangeTracker.getInstance();
    //activeProject = projectTracker.addProjectChangeListener(this);
  }

  public Object getActiveProjectFromIDE(){
    return projectTracker.getActiveProject();
  }

  public void beginTransaction() {
    //MDRManager.getDefault().getDefaultRepository().beginTrans(true);
  }

  public void endTransaction() {
    //MDRManager.getDefault().getDefaultRepository().endTrans();
  }

  public Object[] getIdeProjects() {
    Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
    return openProjects;
  }
}
