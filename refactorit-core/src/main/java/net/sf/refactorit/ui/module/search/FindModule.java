/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.search;

import net.sf.refactorit.ui.module.AbstractRefactorItModule;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;



public class FindModule extends AbstractRefactorItModule {
  private static final RefactorItAction[] actions = {new FindAction()};

  // Register with ModuleManager
  static {
    ModuleManager.registerModule(new FindModule());
  }

  public String getName() {
    return "Structure Search";
  }

  public RefactorItAction[] getActions() {
    return actions;
  }

}
