/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
/* $Id: IntroduceTempModule.java,v 1.10 2004/10/29 08:41:57 tvaga Exp $ */
package net.sf.refactorit.ui.module.introducetemp;

import net.sf.refactorit.ui.module.AbstractRefactorItModule;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;



/**
 * @author Anton Safonov
 */
public class IntroduceTempModule extends AbstractRefactorItModule {
  private static final RefactorItAction[] actions
      = {new IntroduceTempAction()};

  static {
    ModuleManager.registerModule(new IntroduceTempModule());
  }

  public String getName() {
    return "Introduce Explaining Variable";
  }

  public RefactorItAction[] getActions() {
    return actions;
  }
}
