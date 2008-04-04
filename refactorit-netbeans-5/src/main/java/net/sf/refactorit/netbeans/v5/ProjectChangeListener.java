/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.v5;

import org.netbeans.api.project.Project;

/**
 * @author jura
 */
public interface ProjectChangeListener {
  public void ideProjectChanged(Project oldProject, Project newProject);
}
