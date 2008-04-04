/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.apisnapshot;


import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.ui.module.AbstractRefactorItModule;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;

import java.util.ResourceBundle;


public class ApiSnapshotModule extends AbstractRefactorItModule {
  protected ApiSnapshotAction[] action = new ApiSnapshotAction[] {new
      ApiSnapshotAction()};
  protected ResourceBundle bundle = ResourceUtil.getBundle(ApiSnapshotModule.class);

  static {
    ModuleManager.registerModule(new ApiSnapshotModule());
  }

  public RefactorItAction[] getActions() {
    return action;
  }


  public String getName() {
    return bundle.getString("action.name");
  }
}
