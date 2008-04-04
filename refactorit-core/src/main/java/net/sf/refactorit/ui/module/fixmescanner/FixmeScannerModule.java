/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.fixmescanner;


import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.ui.module.AbstractRefactorItModule;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;

import java.util.ResourceBundle;


public class FixmeScannerModule extends AbstractRefactorItModule {
  private static ResourceBundle resLocalizedStrings = ResourceUtil.getBundle(
      FixmeScannerModule.class);

  static {
    ModuleManager.registerModule(new FixmeScannerModule());
  }

  private FixmeScannerModule() {}

  public RefactorItAction[] getActions() {
    return action;
  }


  public String getName() {
    return resLocalizedStrings.getString("action.name");
  }

  private static final RefactorItAction[] action = new RefactorItAction[] {new
      FixmeScannerAction()};
}
