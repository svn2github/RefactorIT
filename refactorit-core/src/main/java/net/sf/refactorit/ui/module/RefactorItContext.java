/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module;

import net.sf.refactorit.classmodel.Project;


/**
 * Context of module execution.
 * Provides access to project and source editing area.
 *
 * @author  Igor Malinin
 * @author  Anton Safonov
 */
public interface RefactorItContext extends IdeWindowContext {
  /**
   * Return RefactorIT project object.
   */
  Project getProject();

  void rebuildAndUpdateEnvironment();

}
