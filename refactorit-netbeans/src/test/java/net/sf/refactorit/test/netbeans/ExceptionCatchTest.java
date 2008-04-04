/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.netbeans;

import net.sf.refactorit.netbeans.common.RefactorItActions;
import net.sf.refactorit.test.TestsWithNullDialogManager;
import net.sf.refactorit.ui.panel.ResultArea;



/**
 * @author risto
 */
public class ExceptionCatchTest extends TestsWithNullDialogManager {
  public ExceptionCatchTest(String name) {
    super(name);
  }

  public void testResultAreaMenu() {
    ResultArea.onMenuClick(null, null, null);

    assertTrue(dialogManager.customErrorString,
        dialogManager.customErrorString.indexOf("NullPointerException") >= 0);
  }

  public void testExplorerMenu() {
    RefactorItActions.onExplorerMenuClick(-1, null, null, null);

    assertTrue(dialogManager.customErrorString,
        dialogManager.customErrorString.indexOf("NullPointerException") >= 0);
  }
}
