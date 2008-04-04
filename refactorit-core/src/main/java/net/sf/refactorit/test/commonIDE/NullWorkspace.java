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
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.BidirectionalMap;
import net.sf.refactorit.commonIDE.AbstractWorkspace;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.WorkspaceManager;
import net.sf.refactorit.refactorings.undo.MilestoneManager;
import net.sf.refactorit.test.TestProject;

import java.util.ArrayList;
import java.util.Iterator;


public class NullWorkspace extends AbstractWorkspace {

  public NullWorkspace(WorkspaceManager manager) {
    super(manager);
  }

  public Project getProject(String name) {
    return (Project) getProjects().getValueByKey(name);
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
    BidirectionalMap map = new BidirectionalMap();
    for (Iterator it = ideProjects.iterator(); it.hasNext(); ) {
      TestProject project = (TestProject) it.next();
      map.put(project.getUniqueID(), project);
    }
    return map;
  }

  private static final int MAX_IDE_PROJECTS = 3;
  private ArrayList ideProjects = new ArrayList(MAX_IDE_PROJECTS);

  public void addIdeProject(Object project) {
    if (ideProjects.size() >= MAX_IDE_PROJECTS) {
      ideProjects.remove(0);
    }
    ideProjects.add(project);
    synchronizeProjects();
  }

  public TestProject getLastIdeProject() {
    if (ideProjects.size() > 0) {
      int index = ideProjects.size() - 1;
      Object obj = ideProjects.get(index);
      return (TestProject) obj;
    } else {
      return null;
    }
  }

  public final void closeProject(Project project) {
    try {
      project.clean();
      MilestoneManager.clear(); // serializes transaction

      Object key = getProjects().getKeyByValue(project);
      for (int i = 0; i < ideProjects.size(); i++) {
        if (key.equals(((TestProject) ideProjects.get(i)).getUniqueID())) {
          ideProjects.remove(i);
          break;
        }
      }
    } catch (Error error) {
      AppRegistry.getExceptionLogger().error(error, this); // if we fail, let IDE close anyway
    }
  }
}
