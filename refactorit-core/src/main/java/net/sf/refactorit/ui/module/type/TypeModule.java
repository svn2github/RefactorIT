/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.type;

import net.sf.refactorit.ui.module.AbstractRefactorItModule;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;



/**
 * Type Info module descriptor.
 *
 * @author Igor Malinin
 * @author Anton Safonov
 */
public class TypeModule extends AbstractRefactorItModule {
  private static final RefactorItAction[] allActions
      = {new TypeAction(), new JavadocAction()};

  static {
    ModuleManager.registerModule(new TypeModule());
  }

  /**
   * Type info dialog
   */
  private TypeModule() {
  }

  public RefactorItAction[] getActions() {
    return allActions;
  }

  public String getName() {
    return "Info";
  }
}
