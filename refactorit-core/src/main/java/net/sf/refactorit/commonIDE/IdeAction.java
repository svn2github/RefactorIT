/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.commonIDE;

import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.module.ActionProxy;


public interface IdeAction extends ActionProxy {
  boolean isAvailable();

  /**
   * Return true if should call {@link IDEController#ensureProjectWithoutParsing() } before executing run.
   * NB! run gets called even if ensureProject fails.
   */
  boolean needsEnsureProject();

  boolean run(IdeWindowContext context);
}
