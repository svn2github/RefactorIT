/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.common.util.cvsutil;

import net.sf.refactorit.utils.cvsutil.CvsEntriesLine;

import junit.framework.TestCase;

/**
 * @author risto
 */
public class CvsEntriesLineTest extends TestCase {
  public void testSimpleParse() {
    CvsEntriesLine l = new CvsEntriesLine(
        "/CrLfBinary.java/1.947/Tue Dec 14 09:20:28 2004/-kb/");
    
    assertFalse(l.isDirectory());
    assertEquals("CrLfBinary.java", l.getName());
    assertEquals("1.947", l.getVersion());
    assertEquals("Tue Dec 14 09:20:28 2004", l.getTime());
    assertEquals("-kb", l.getOptions());
  }
  
  public void testMissingTokens() {
    CvsEntriesLine l = new CvsEntriesLine(
      "/CrLfBinary.java/1.947///");
    
    assertEquals("", l.getTime());
  }
  
  public void testDirectory() {
    CvsEntriesLine l = new CvsEntriesLine("D/foldername////");
    
    assertTrue(l.isDirectory());
    assertEquals("foldername", l.getName());
  }
  
  public void testFileRemoval() {
    CvsEntriesLine normalFile = 
        new CvsEntriesLine("/X.java/1.947///");
    CvsEntriesLine removedFile = 
        new CvsEntriesLine("/X.java/-1.947///");
  
    assertFalse(normalFile.isRemoved());
    assertTrue(removedFile.isRemoved());
  }
  
  public void testUncommitedAdd() {
    CvsEntriesLine normalFile = 
      new CvsEntriesLine("/CrLfBinary.java/-1.969/Wed Dec 15 08:58:48 2004/-kb/");
    CvsEntriesLine addedFile = 
        new CvsEntriesLine("/CrLfBinary2.java/0/Wed Dec 15 08:59:14 2004/-kb/");
  
    assertFalse(normalFile.isUncommittedAdd());
    assertTrue(addedFile.isUncommittedAdd());
  }
  
  public void testBinary() {
    CvsEntriesLine normalFile = 
        new CvsEntriesLine("/X.java/1.947/Tue Dec 14 09:20:28 2004//");
    CvsEntriesLine binaryFile =
        new CvsEntriesLine("/X.java/1.947/Tue Dec 14 09:20:28 2004/-kb/");

    assertFalse(normalFile.isBinary());
    assertTrue(binaryFile.isBinary());
  }
}
