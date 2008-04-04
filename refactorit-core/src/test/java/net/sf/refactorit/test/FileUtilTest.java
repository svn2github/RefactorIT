/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test;

import net.sf.refactorit.utils.FileUtil;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class FileUtilTest extends TestCase {

  public static Test suite() {
    return new TestSuite(FileUtilTest.class);
  }

  public FileUtilTest(String name) {
    super(name);
  }

  public void testGetCommonPath() {
    assertEquals("", FileUtil.getCommonPath("", ""));
    assertEquals("com.asf", FileUtil.getCommonPath("com.asf.a", "com.asf.b"));
    assertEquals("com", FileUtil.getCommonPath("com", "com.b"));
    assertEquals("com", FileUtil.getCommonPath("com.b", "com"));

    assertEquals("", FileUtil.getCommonPath("com", "company"));
    assertEquals("com.abc", FileUtil.getCommonPath("com.abc.aa", "com.abc.ab"));
  }

}
