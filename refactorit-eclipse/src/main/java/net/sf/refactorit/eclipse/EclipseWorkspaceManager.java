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
import net.sf.refactorit.commonIDE.Workspace;
import net.sf.refactorit.commonIDE.WorkspaceManager;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class EclipseWorkspaceManager extends WorkspaceManager {
  private static WorkspaceManager manager;

  private EclipseWorkspaceManager() {
    // singleton
  }

  protected Object getIdeProject(String projectIdent) {
    return getWorkspace().getIdeProjects().getValueByKey(projectIdent);
  }

  public static WorkspaceManager getInstance() {
    if (manager == null) {
      manager = new EclipseWorkspaceManager();
    }

    return manager;
  }

  public Workspace getWorkspace() {
    if (workspace == null) {
      workspace = new EclipseWorkspace(this);
    }

    return workspace;
  }

  public Object getIdeProjectIdentifier(Object ideProject) {
      return ((IProject) ideProject).getName();
  }

  public Object getProjectIdentifier(Project activeProject) {
    return getWorkspace().getProjects().getKeyByValue(activeProject);
  }

  public List getReferencedInProjects(Object projectIdentificator) {
    BidirectionalMap ideProjects = getWorkspace().getIdeProjects();
    Set ideProjectSet = ideProjects.getValueSetCopy();
    ArrayList referencedProjects = new ArrayList(3);

    for (Iterator it = ideProjectSet.iterator(); it.hasNext(); ) {
      IProject ideProject = (IProject)it.next();
      
      if (hasJavaNature(ideProject)) {
        // accept only java projects
        IJavaProject javaProject = JavaCore.create(ideProject);
        try {
          String[] requiredProjectKeys = javaProject.getRequiredProjectNames();

          if (Arrays.asList(requiredProjectKeys).contains(projectIdentificator)) {
            referencedProjects.add(getWorkspace().getProject(ideProject));
          }
        } catch (JavaModelException e) {
        	// should not happen
          e.printStackTrace();
        }
      }
    }

    return referencedProjects;
  }

  public List getDependsOnProjects(Object projectIdentificator) {
    BidirectionalMap ideProjects = getWorkspace().getIdeProjects();
    IProject project = (IProject)ideProjects.getValueByKey(projectIdentificator);

    if (project == null) {
      throw new RuntimeException("Specified project (" + projectIdentificator
          + ") cannot be located");
    }

    ArrayList dependentProjects = new ArrayList(3);

    try {
      IJavaProject javaProject = JavaCore.create(project);
      String[] requiredProjectKeys = javaProject.getRequiredProjectNames();

      for (int i = 0; i < requiredProjectKeys.length; i++) {
        Object candidateProject =
          ideProjects.getValueByKey(requiredProjectKeys[i]);

        if (candidateProject == null) {
          continue;
        }

        dependentProjects.add(getWorkspace().getProject(candidateProject));
      }
    } catch (JavaModelException e) {
      e.printStackTrace();
    }

    return dependentProjects;
  }
  
  public static boolean hasJavaNature(IProject project) { 
    try {
      return project.hasNature(JavaCore.NATURE_ID);
    } catch (CoreException e) {
      // project does not exist or is not open
    }

    return false;
  }
}
