/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.dependencies;


import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.ui.module.AbstractRefactorItModule;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;

import java.util.ResourceBundle;


/**
 * @author Anton Safonov
 */
public class DependenciesModule extends AbstractRefactorItModule {

  private static final ResourceBundle resLocalizedStrings
      = ResourceUtil.getBundle(DependenciesModule.class);

  // Register with ModuleManager
  static {
    ModuleManager.registerModule(new DependenciesModule());
  }

  private DependenciesModule() {
  }

  public RefactorItAction[] getActions() {
    return action;
  }


  public String getName() {
    return resLocalizedStrings.getString("module.name");
  }

  private static final RefactorItAction[] action
      = {new DependenciesAction(), new DrawDependenciesAction(),
    new DependencyLoopsAction()};
}
