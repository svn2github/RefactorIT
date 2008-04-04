/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.rename;

import net.sf.refactorit.ui.module.AbstractRefactorItModule;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;
import net.sf.refactorit.ui.refactoring.rename.RenameAction;



/**
 * @author Anton Safonov
 */
public class RenameModule extends AbstractRefactorItModule {
  private static final RefactorItAction[] action = {new RenameAction()};

  static {
    ModuleManager.registerModule(new RenameModule());
  }

  private RenameModule() {
  }

  public RefactorItAction[] getActions() {
    return action;
  }

  /**
   * @return java.lang.String
   */
  public String getName() {
    return null;
  }
}
