/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.netbeans.standalone;

import net.sf.refactorit.netbeans.common.standalone.IdeVersion;

import junit.framework.TestCase;

/**
 *
 * @author risto
 */
public class IdeVersionTest extends TestCase {
  private IdeVersion v;
  
  public void testParsing() {
    v = new IdeVersion("NetBeans IDE 3.6 (Build {0})", "200404071636");
    assertEquals("NetBeans IDE", v.getName());
    assertEquals("3.6", v.getVersion());
    
    v = new IdeVersion("NetBeans IDE 4.0 (Build {0})", "200412081800");
    assertEquals("NetBeans IDE", v.getName());
    assertEquals("4.0", v.getVersion());
    
    v = new IdeVersion("NetBeans IDE Dev (Build {0})", "200409220845");
    assertEquals("NetBeans IDE", v.getName());
    assertEquals("Dev build 200409220845", v.getVersion());
    
    v = new IdeVersion("NetBeans IDE 4.0 RC2 (Build {0})", "200412011810");
    assertEquals("NetBeans IDE", v.getName());
    assertEquals("4.0 RC2", v.getVersion());
    
    v = new IdeVersion("Sun ONE Studio 5, Standard Edition (Build {0})",
        "0000000000");
    assertEquals("Sun ONE Studio", v.getName());
    assertEquals("5", v.getVersion());
  }
}
