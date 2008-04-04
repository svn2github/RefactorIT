/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
/* $Id: InlineModule.java,v 1.10 2004/10/29 08:41:57 tvaga Exp $ */
package net.sf.refactorit.ui.module.inline;

import net.sf.refactorit.ui.module.AbstractRefactorItModule;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;



/**
 * @author Anton Safonov
 */
public class InlineModule extends  AbstractRefactorItModule {

  private static final RefactorItAction[] inlineActions = {new InlineAction()};

  static {
    ModuleManager.registerModule(new InlineModule());
  }

  public String getName() {
    return "Inline";
  }

  public RefactorItAction[] getActions() {
    return inlineActions;
  }
}
