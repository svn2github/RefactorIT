/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.netbeans;

import net.sf.refactorit.netbeans.common.NBShortcutsInstaller;
import junit.framework.TestCase;

/**
 *
 * @author risto
 */
public class ShortcutTest extends TestCase {
  private static final String KEYCODE = "CAS-P";
  private static final String ANOTHER_KEYCODE = "CAS-Q";
  
  private static final String INSTANCE_CLASS = 
      "net.sf.refactorit.netbeans.common.action.WhereAction";
  private static final String ANOTHER_INSTANCE_CLASS = 
      "net.sf.refactorit.netbeans.common.action.RenameAction";
  private static final String DUMMY_OTHER_MODULE_INSTANCE_CLASS =
      "com.dummy.other.module.BlahBlah";
  
  public void tearDown() {
    NBShortcutsInstaller.installShortcuts();
  }
  
  public void testInstallingShortcut() {
    NBShortcutsInstaller.installShortcut(KEYCODE, INSTANCE_CLASS);
    
    assertEquals(INSTANCE_CLASS, NBShortcutsInstaller.getInstanceClass(
        NBShortcutsInstaller.getExistingShortcut(KEYCODE)));
  }
  
  public void testSettingShortcutForSecondTime() {
    NBShortcutsInstaller.installShortcut(KEYCODE, INSTANCE_CLASS);
    NBShortcutsInstaller.installShortcut(KEYCODE, INSTANCE_CLASS);
    
    assertEquals(INSTANCE_CLASS, NBShortcutsInstaller.getInstanceClass(
        NBShortcutsInstaller.getExistingShortcut(KEYCODE)));
  }
  
  public void testShortcutOccupiedByAnotherModule() throws Exception {
    try {
      NBShortcutsInstaller.installShortcut(KEYCODE, DUMMY_OTHER_MODULE_INSTANCE_CLASS);
      NBShortcutsInstaller.installShortcut(KEYCODE, INSTANCE_CLASS);

      assertEquals(DUMMY_OTHER_MODULE_INSTANCE_CLASS, NBShortcutsInstaller.getInstanceClass(
          NBShortcutsInstaller.getExistingShortcut(KEYCODE)));
    } finally {
      NBShortcutsInstaller.getExistingShortcut(KEYCODE).delete();
      assertNull(NBShortcutsInstaller.getExistingShortcut(KEYCODE));
    }
  }
  
  public void testOtherRitActionForSameKeycode() {
    NBShortcutsInstaller.installShortcut(KEYCODE, ANOTHER_INSTANCE_CLASS);
    NBShortcutsInstaller.installShortcut(KEYCODE, INSTANCE_CLASS);
    
    assertEquals(ANOTHER_INSTANCE_CLASS, NBShortcutsInstaller.getInstanceClass(
        NBShortcutsInstaller.getExistingShortcut(KEYCODE)));
  }
  
  public void testCleaningShortcutsOnStartup() {
    NBShortcutsInstaller.installShortcut(KEYCODE, ANOTHER_INSTANCE_CLASS);
    NBShortcutsInstaller.installShortcuts(); // Invoked on startup
    NBShortcutsInstaller.installShortcut(KEYCODE, INSTANCE_CLASS);
    
    assertEquals(INSTANCE_CLASS, NBShortcutsInstaller.getInstanceClass(
        NBShortcutsInstaller.getExistingShortcut(KEYCODE)));
  }
  
  public void testTwoShortcutsForAction() {
    NBShortcutsInstaller.installShortcut(ANOTHER_KEYCODE, INSTANCE_CLASS);
    NBShortcutsInstaller.installShortcut(KEYCODE, INSTANCE_CLASS);
    
    assertEquals(INSTANCE_CLASS, NBShortcutsInstaller.getInstanceClass(
        NBShortcutsInstaller.getExistingShortcut(KEYCODE)));
    assertEquals(INSTANCE_CLASS, NBShortcutsInstaller.getInstanceClass(
        NBShortcutsInstaller.getExistingShortcut(ANOTHER_KEYCODE)));
  }
  
  public void testDeleteAllShortcuts() {
    NBShortcutsInstaller.installShortcut(KEYCODE, INSTANCE_CLASS);
    
    NBShortcutsInstaller.deleteAllShortcuts();
    
    assertNull(NBShortcutsInstaller.getExistingShortcut(KEYCODE));
  }
}
