/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.vcs;


import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.ui.DialogManager;

import org.apache.log4j.Logger;

/**
 * @author risto
 */
public class ErrorDialog {
  private static Logger log = Logger.getLogger(ErrorDialog.class); 

  static void error(String message) {
    DialogManager.getInstance().showInformation(
        IDEController.getInstance().createProjectContext(),
        "info.failed.cvs.command", message);
    log.warn(message);
  }

}
