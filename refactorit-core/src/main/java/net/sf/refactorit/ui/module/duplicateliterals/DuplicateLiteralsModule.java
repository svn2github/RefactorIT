/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.duplicateliterals;

import net.sf.refactorit.ui.module.AbstractRefactorItModule;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;



public class DuplicateLiteralsModule extends AbstractRefactorItModule {
  private static final RefactorItAction[] actions = {new DuplicateLiteralsAction()};

  // Register with ModuleManager
  static {
    ModuleManager.registerModule(new DuplicateLiteralsModule());
  }

  public String getName() {
    return "Find Duplicate Strings";
  }

  public RefactorItAction[] getActions() {
    return actions;
  }

}
