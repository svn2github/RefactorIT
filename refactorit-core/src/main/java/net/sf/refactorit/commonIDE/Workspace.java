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
 * Workspace
 *
 * @author <a href="mailto:tonis.vaga@aqris.com>Tonis Vaga</a>
 * @version $Revision: 1.11 $ $Date: 2005/12/09 12:02:59 $
 */
public interface Workspace {
  void addProject(Project pr, Object projectKey);
  Project getProject(String name);
  Project getProject(Object ideProject);
  Project getProjectByIdentifier(Object projectKey);
  BidirectionalMap getProjects();
  BidirectionalMap getSyncronizedProjects();
  BidirectionalMap getIdeProjects();
  Project getActiveProject();
  void closeProject(Project project);
  void clear();

  WorkspaceManager getWorkspaceManager();
}
