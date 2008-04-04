/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.metric;


import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.metrics.MetricsAction;
import net.sf.refactorit.metrics.MetricsModel;
import net.sf.refactorit.ui.module.AbstractRefactorItModule;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;

import java.util.ResourceBundle;


/**
 * Metrics module.
 */
public class MetricsModule extends AbstractRefactorItModule {

  private static final RefactorItAction[] actions = {new MetricsAction()};

  private static ResourceBundle resLocalizedStrings
      = ResourceUtil.getBundle(MetricsModel.class);

  // Register with ModuleManager
  static {
    ModuleManager.registerModule(new MetricsModule());
  }

  private MetricsModule() {}

  public String getName() {
    return resLocalizedStrings.getString("module.name");
  }

  public RefactorItAction[] getActions() {
    return actions;
  }
}
