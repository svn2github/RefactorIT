/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.netbeans.standalone;


import java.io.File;

import net.sf.refactorit.netbeans.common.standalone.FileBasedOptions;
import net.sf.refactorit.test.TempFileCreator;
import junit.framework.TestCase;

/**
 *
 * @author risto
 */
public class FileBasedOptionsTest extends TestCase {
  private File folder;
  private FileBasedOptions o;
  
  public void setUp() throws Exception {
    folder = TempFileCreator.getInstance().createRootDirectory().getFileOrNull();
    o = new FileBasedOptions(folder);
  }
  
  public void testFirst() throws Exception {
    assertFalse(o.optionsExist());
    o.createOptions();
    assertTrue(o.optionsExist());
  }
  
  public void testGettingAndSettingAttributes() throws Exception {
    o.setProperty("name1", "value1");
    o.setProperty("name2", "value2");
    
    assertEquals("value1", o.getProperty("name1"));
    assertEquals("value2", o.getProperty("name2"));
  }
  
  public void testPersistance() throws Exception {
    o.setProperty("p", "v");
    
    FileBasedOptions newInstance = new FileBasedOptions(folder);
    assertEquals("v", newInstance.getProperty("p"));
  }
  
  public void testGetSnapshot() throws Exception {
    o.setProperty("name", "old value");
    final Object snapshot = o.getSnapshot();
    
    o.setProperty("name", "new value");
    o.restoreFromSnapshot(snapshot);
    
    assertEquals("old value", o.getProperty("name"));
  }
  
  public void testOptionsFileDeleted() throws Exception {
    o.createOptions();
    o.getOptionsFile().delete();
    o.setProperty("a", "b"); // An IOException used to happen here, during save
  }
}
