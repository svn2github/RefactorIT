/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.apidiff;


import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.ui.module.AbstractRefactorItModule;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;

import java.util.ResourceBundle;


public class ApiDiffModule extends AbstractRefactorItModule {
  protected ApiDiffAction[] action = new ApiDiffAction[] {new ApiDiffAction()};
  protected ResourceBundle bundle = ResourceUtil.getBundle(ApiDiffModule.class);

  static {
    ModuleManager.registerModule(new ApiDiffModule());
  }

  public RefactorItAction[] getActions() {
    return action;
  }


  public String getName() {
    return bundle.getString("action.name");
  }
}
