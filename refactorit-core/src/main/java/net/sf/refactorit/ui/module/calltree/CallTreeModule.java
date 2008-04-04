/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.calltree;


import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.ui.module.AbstractRefactorItModule;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;

import java.util.ResourceBundle;


/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      AQRIS Development
 * @author Anton Safonov
 */

public class CallTreeModule extends AbstractRefactorItModule {

  private static final ResourceBundle resLocalizedStrings
      = ResourceUtil.getBundle(CallTreeModule.class);

  // Register with ModuleManager
  static {
    ModuleManager.registerModule(new CallTreeModule());
  }

  private CallTreeModule() {
  }

  public RefactorItAction[] getActions() {
    return action;
  }



  public String getName() {
    return resLocalizedStrings.getString("module.name");
  }

  private static final net.sf.refactorit.ui.module.RefactorItAction[] action
      = {new CallTreeAction()};
}
