/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.shell;


import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.ui.module.AbstractRefactorItModule;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;

import java.util.ResourceBundle;


/**
 * Shell module.
 */
public class ShellModule extends AbstractRefactorItModule {
  private static final RefactorItAction[] actions = {new ShellAction()};

  private static ResourceBundle resLocalizedStrings =
      ResourceUtil.getBundle(ShellModule.class);

  // Register with ModuleManager
  static {
    ModuleManager.registerModule(new ShellModule());
  }

  private ShellModule() {}

  public String getName() {
    return resLocalizedStrings.getString("module.name");
  }

  public RefactorItAction[] getActions() {
    return actions;
  }
}
