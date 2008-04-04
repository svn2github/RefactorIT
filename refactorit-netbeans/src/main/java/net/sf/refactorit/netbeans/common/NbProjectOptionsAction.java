/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common;

import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.module.ProjectOptionsAction;


/**
 *
 *
 * @author Tonis Vaga
 */
public class NbProjectOptionsAction extends ProjectOptionsAction {
  public NbProjectOptionsAction() {}

  public boolean run(IdeWindowContext context) {
    try {
      RefactorItActions.importFormatterEngineDefaults();
    } catch (RuntimeException e) {}
    
    Object ideProject = IDEController.getInstance().getActiveProjectFromIDE();
    NBController.showSettingsDialog(ideProject);

    return false;
  }

  public char getMnemonic() {
    return 'P';
  }
}
