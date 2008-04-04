/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.gotomodule;

import net.sf.refactorit.ui.module.AbstractRefactorItModule;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;
import net.sf.refactorit.ui.module.gotomodule.actions.GotoAction;


/**
 * @author Anton Safonov
 */
public class GoToModule extends AbstractRefactorItModule {
  // Register to ModuleManager
  static {
    ModuleManager.registerModule(new GoToModule());
  }

  private GoToModule() {
  }

  public RefactorItAction[] getActions() {
    return allActions;
  }


  public String getName() {
    return "Go To ...";
  }

  private static final RefactorItAction gotoAction = new GotoAction();

  private static final RefactorItAction[] allActions = {gotoAction};
}
