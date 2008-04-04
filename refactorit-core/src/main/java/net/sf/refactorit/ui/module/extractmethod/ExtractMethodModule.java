/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.extractmethod;

import net.sf.refactorit.ui.module.AbstractRefactorItModule;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;



public class ExtractMethodModule extends AbstractRefactorItModule {
  private static final RefactorItAction[] actions = {new ExtractMethodAction(),
      new CreateConstructorAction()
  };

  // Register with ModuleManager
  static {
    ModuleManager.registerModule(new ExtractMethodModule());
  }

  public String getName() {
    return "Extract Method";
  }

  public RefactorItAction[] getActions() {
    return actions;
  }
}
