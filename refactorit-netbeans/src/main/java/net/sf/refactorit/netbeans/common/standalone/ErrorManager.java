/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.netbeans.common.standalone;

import javax.swing.JOptionPane;

import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.utils.RefactorItLogAppender;

import org.apache.log4j.Logger;


/**
 *
 * @author risto
 */
public class ErrorManager {
  private static final Logger log = Logger.getLogger(ErrorManager.class);
  
  public static BackupErrorDialog backupErrorDialog = new BackupErrorDialog();
  

  public static void showAndLogInternalError(Throwable e) {
    log.warn(e.getMessage(), e);
    
    final String errorMessage = GlobalOptions.REFACTORIT_NAME + " error: " + e + "\n\n" +
              "Please send a log file to Support@RefactorIT.com\n" +
              "or submit it at http://www.refactorit.com/?id=1196. It's located at:\n" +
              RefactorItLogAppender.getLogFileLocation();
    
    try {
      DialogManager.getInstance().showCustomError(
          IDEController.getInstance().createProjectContext(), errorMessage);
      
    } catch(Exception e2) {
      log.warn(e2.getMessage(), e2);
      
      backupErrorDialog.show(errorMessage);
    }
  }
  
  public static class BackupErrorDialog {
    public void show(String error) {
      JOptionPane.showMessageDialog(null, error, "RefactorIT Internal Error",
          JOptionPane.ERROR_MESSAGE);
    }
  }
}
