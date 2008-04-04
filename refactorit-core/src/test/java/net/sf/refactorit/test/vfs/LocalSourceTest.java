/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.vfs;


import net.sf.refactorit.vfs.local.LocalSource;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class LocalSourceTest extends TestCase {
  
  public LocalSourceTest(String name) {
    super(name);
  }
 
  public void test_sourceContentString() throws Exception {
    File temp = File.createTempFile("refactoritTest", ".java");
    
    // Write to temp file
    BufferedWriter out = new BufferedWriter(new FileWriter(temp));
    out.write("aString; \n class java {};");
    out.close();
    
    LocalSource localSource = new LocalSource(temp);
    assertEquals(
        "LocalSource must completely correspond to the physical source file",
        "aString; \n class java {};", localSource.getContentString());

    if(!temp.delete()) {
      // Delete temp file when tests exits.
      temp.deleteOnExit();
    }
  }
  
  public void test_sourceContentOfLine() throws Exception {
    File temp = File.createTempFile("refactoritTest", ".java");
    
    // Write to temp file
    BufferedWriter out = new BufferedWriter(new FileWriter(temp));
    out.write("aString;\nclass java {};\nfoo");
    out.close(); 
    
    LocalSource localSource = new LocalSource(temp);
       
    assertEquals("LocalSource must have 'aString;' on the line 1: ",
        "aString;", localSource.getContentOfLine(1));
    assertEquals("LocalSource must have 'class java {};' on the line 2: ",
        "class java {};", localSource.getContentOfLine(2));
    assertEquals("LocalSource must have 'foo' on the line 3: ",
        "foo", localSource.getContentOfLine(3));    
    
    if(!temp.delete()) {
      // Delete temp file when tests exits.
      temp.deleteOnExit();
    }
  }  
  
  public static Test suite() throws Throwable {
    return new TestSuite(LocalSourceTest.class);
  }
}
