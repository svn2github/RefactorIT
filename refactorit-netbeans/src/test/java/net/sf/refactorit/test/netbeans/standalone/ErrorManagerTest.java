/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.netbeans.standalone;

import net.sf.refactorit.netbeans.common.standalone.ErrorManager;
import net.sf.refactorit.test.TestsWithNullDialogManager;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.NullDialogManager;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.utils.FileUtil;


import junit.framework.TestCase;

/**
 *
 * @author risto
 */
public class ErrorManagerTest extends TestsWithNullDialogManager {
  public void testLogFileLocationInErrorDialog() {
    ErrorManager.showAndLogInternalError(new Throwable("Throwable title"));

    assertTrue(dialogManager.customErrorString, 
        dialogManager.customErrorString.indexOf(
        FileUtil.useSystemPS("/.refactorit/refactorit.log")) >= 0);
    assertTrue(dialogManager.customErrorString, 
        dialogManager.customErrorString.indexOf("Throwable title") >= 0);
  }  
  
  public void testExceptionWhenTryingToShowDialog() {
    DialogManager.setInstance(new NullDialogManager() {
      public void showCustomError(IdeWindowContext context, String errorString) {
        throw new NullPointerException();
      }
    } );
    
    final StringBuffer shownErrors = new StringBuffer();
    ErrorManager.backupErrorDialog = new ErrorManager.BackupErrorDialog() {
      public void show(String error) {
        shownErrors.append(error + " ");
      }
    };
    
    ErrorManager.showAndLogInternalError(new Throwable("This is the exception"));
    
    assertTrue(shownErrors.toString(), 
        shownErrors.toString().indexOf("This is the exception") >= 0);
  }
}
