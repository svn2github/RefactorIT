/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.common;

import net.sf.refactorit.ui.module.AbstractRefactorItModule;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;


/**
 *
 *
 * @author Tonis Vaga
 */
public class CommonRefactoringsModule extends AbstractRefactorItModule {

  static {
    ModuleManager.registerModule(new CommonRefactoringsModule());
  }
  private static RefactorItAction[] actions = new RefactorItAction[] {
      new AddDelegatesAction(),
      new OverrideMethodsAction(),
      new ConvertTempToFieldAction(),
      new UseSuperTypeAction(),
      new ChangeMethodSignatureAction(),
  };
  
  public RefactorItAction[] getActions() {
    return actions;
  }


  private CommonRefactoringsModule() {}

  public String getName() {
    return "Common Module";
  }
}
