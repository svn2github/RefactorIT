/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.notused;


import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.ui.module.AbstractRefactorItModule;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;

import java.util.ResourceBundle;


/**
 * @author Anton Safonov
 */
public class NotUsedModule extends AbstractRefactorItModule {

  private static ResourceBundle resLocalizedStrings
      = ResourceUtil.getBundle(NotUsedModule.class);

  // Register with ModuleManager
  static {
    ModuleManager.registerModule(new NotUsedModule());
  }

  private NotUsedModule() {
  }

  public RefactorItAction[] getActions() {
    return action;
  }

  public String getName() {
    return resLocalizedStrings.getString("module.name");
  }

  private static final net.sf.refactorit.ui.module.RefactorItAction[] action
      = {new NotUsedAction()};
}
