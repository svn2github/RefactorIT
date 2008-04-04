/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test;

import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.NullDialogManager;

import junit.framework.TestCase;


public class TestsWithNullDialogManager extends TestCase {
  protected NullDialogManager dialogManager;
  private DialogManager old;
  
  public TestsWithNullDialogManager(String name) {
    super(name);
  }
  
  public TestsWithNullDialogManager() {
    super();
  }

  public void setUp() {
    old = DialogManager.getInstance();
    
    dialogManager = new NullDialogManager();
    DialogManager.setInstance(dialogManager);
  }
  
  public void tearDown() {
    DialogManager.setInstance(old);
  }

}
