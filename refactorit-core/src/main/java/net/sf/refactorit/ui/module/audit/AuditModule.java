/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.audit;

import net.sf.refactorit.ui.module.AbstractRefactorItModule;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;


public class AuditModule extends AbstractRefactorItModule {
  private static final RefactorItAction[] actions = {new AuditAction()};

  static {
    ModuleManager.registerModule(new AuditModule());
  }

  public String getName() {
    return "Audits";
  }

  public RefactorItAction[] getActions() {
    return actions;
  }
}
