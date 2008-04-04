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

import org.openide.modules.ModuleInstall;


public abstract class RefactorItInstall extends ModuleInstall {
  public void installed() {
    init();
  }

  public void restored() {
    init();
  }
  
  private void init() {
    initVersionSpecifics();
    RefactorItActions.staticInit();
  }

  public boolean closing() {
    IDEController instance = IDEController.getInstance();
    if (instance != null) {
      instance.onIdeExit();
    }
    //WHY?
		NBShortcutsInstaller.deleteAllShortcuts();

    return true;
  }
  
  public void uninstalled() {
    closing();
  }
  
  abstract protected void initVersionSpecifics();
}
