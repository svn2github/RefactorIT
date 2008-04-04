/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.move;

import net.sf.refactorit.ui.module.AbstractRefactorItModule;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;


/**
 * @author Anton Safonov
 */
public class MoveModule extends AbstractRefactorItModule {

  private static final RefactorItAction[] actions = {new MoveAction()};

  static {
    ModuleManager.registerModule(new MoveModule());
  }

  private MoveModule() {
  }

  public RefactorItAction[] getActions() {
    return actions;
  }

  public String getName() {
    return "Move";
  }
}
