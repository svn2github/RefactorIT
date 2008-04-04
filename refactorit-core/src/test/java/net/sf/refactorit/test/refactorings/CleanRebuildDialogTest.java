/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.refactorings;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.test.TestsWithNullDialogManager;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.ui.module.CleanAction;
import net.sf.refactorit.ui.module.RebuildAction;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 * @author risto
 */
public class CleanRebuildDialogTest extends TestsWithNullDialogManager {
  private static Project p; static {
    try {
      p = Utils.createSimpleProject();
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  } 
  
  public static Test suite() {
    return new TestSuite(CleanRebuildDialogTest.class);
  }
  
  public void setUp() {
    super.setUp();
  }
  
  public void testDialogShownOnClean() {
    new CleanAction().run(
            IDEController.getInstance().createProjectContext());
    
    assertEquals("rebuild.done", dialogManager.infoString);
  }
  
  public void testDialogShownOnRebuild() {
    new RebuildAction().run(
            IDEController.getInstance().createProjectContext());
    
    assertEquals("rebuild.done", dialogManager.infoString);
  }
}
