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
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.BidirectionalMap;
import net.sf.refactorit.refactorings.undo.MilestoneManager;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class AbstractWorkspace implements Workspace {
  private BidirectionalMap projects;

  public WorkspaceManager manager;

  public AbstractWorkspace(WorkspaceManager manager) {
    this.manager = manager;
  }

  // A - set of actual keys (from IDE)
  // B - set of current keys (saved)
  // projects out of date are in B/A set
  // projects that are new are in A/B set
  // function creates new projects and removes old ones
  protected final void synchronizeProjects() {
    BidirectionalMap ideProjects = getIdeProjects();
    if (ideProjects == null) {
      return;
    }

    Set ideKeys = ideProjects.getKeySetCopy();
    Set ourKeys = getProjects().getKeySetCopy();

    HashSet keySetToRemove = new HashSet(ourKeys);
    keySetToRemove.removeAll(ideKeys);

    HashSet keySetToAdd = new HashSet(ideKeys);
    keySetToAdd.removeAll(ourKeys);

    for (Iterator it = keySetToRemove.iterator(); it.hasNext();) {
      Object key = it.next();
      Project project = (Project) getProjects().getValueByKey(key);
      closeProject(project);
      getProjects().removeByKey(key);
    }

    for (Iterator it = keySetToAdd.iterator(); it.hasNext();) {
      Object ideProjectKey = it.next();
      Object ideProject = ideProjects.getValueByKey(ideProjectKey);
      Project project
          = IDEController.getInstance().createNewProject(ideProject);
      addProject(project, ideProjectKey);
    }
  }

  public final Project getActiveProject() {
    boolean projectChangedInIde = IDEController.getInstance().isProjectChangedInIDE();
    if (projectChangedInIde) {
      synchronizeProjects();
    }

    Object activeIdeProject = IDEController.getInstance()
        .getActiveProjectFromIDE();

    if (activeIdeProject == null) {
      return IDEController.getInstance().getCachedActiveProject();
    }

    Project result = getOrCreateProjectByIdentifier(activeIdeProject, 
        getWorkspaceManager().getIdeProjectIdentifier(activeIdeProject));

    if (projectChangedInIde && result != null) {
      // inline!
      IDEController.getInstance().setActiveProject(result);
      //IDEController.getInstance().setActiveIDEProject(activeIdeProject);
      //this.ideProject = activeIdeProject;
      // ------
    }
    return result;
  }

  private Project getOrCreateProjectByIdentifier(final Object activeIdeProject, final Object identifier) {
    Project result = getProjectByIdentifier(identifier);
    if (result == null) {
      result = IDEController.getInstance().createNewProject(
          activeIdeProject);
      IDEController.getInstance().setActiveProject(result);
      //IDEController.getInstance().setActiveIDEProject(activeIdeProject);
      if (result != null) {
        addProject(result, identifier);
      }
    }

    return result;
  }

  public WorkspaceManager getWorkspaceManager() {
    return this.manager;
  }

  public void closeProject(Project project) {
    try {
      IDEController.getInstance().serializeProjectCache(project, false);
      project.clean();
      MilestoneManager.clear(); // serializes transaction
    } catch (Error error) {
      AppRegistry.getExceptionLogger().error(error, this); // if we fail, let IDE close anyway
    }
  }

  /**
   * adds project to workspace. NB: project must have unique name!
   * @param pr
   */
  public final void addProject(final Project pr, final Object projectKey) {
    assert pr != null;
    assert projectKey != null;

    getProjects().put(projectKey, pr);
    pr.setWorkspace(this);
  }

  public final Project getProjectByIdentifier(Object projectKey) {
    assert projectKey != null;
    return (Project) getProjects().getValueByKey(projectKey);
  }

  public final void clear() {
    getProjects().clear();
    if (this.manager != null) {
      this.manager.clear();
    }
  }

  /**
   * @see net.sf.refactorit.commonIDE.Workspace#getProjects()
   */
  public final BidirectionalMap getProjects() {
    if(projects == null) {
      projects = new BidirectionalMap();
    }
    return projects;
  }

  public BidirectionalMap getSyncronizedProjects() {
    synchronizeProjects();
    return getProjects();
  }

}
