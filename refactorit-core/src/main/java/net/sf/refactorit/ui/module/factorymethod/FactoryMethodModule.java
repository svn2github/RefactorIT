/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.factorymethod;

import net.sf.refactorit.ui.module.AbstractRefactorItModule;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;



public class FactoryMethodModule extends AbstractRefactorItModule {

  // Register with ModuleManager
  static {
    ModuleManager.registerModule(new FactoryMethodModule());
  }

  private FactoryMethodModule() {
  }

  public RefactorItAction[] getActions() {
    return action;
  }

  public String getName() {
    return "Create Factory Method";
  }

  private static final net.sf.refactorit.ui.module.RefactorItAction[] action
      = {new FactoryMethodAction()};
}
