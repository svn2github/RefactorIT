/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.classmodel;

import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.test.Utils;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author risto
 */
public class CompilationUnitTest extends TestCase {
  public static Test suite() {
    return new TestSuite(CompilationUnitTest.class);
  }
  
  public void testGuaredBlocks() throws Exception {
    Project p = Utils.createTestRbProjectFromString(
        "public class X {\n" +
        "  // Variables declaration - do not modify//GEN-BEGIN:variables\n" +
        "  private javax.swing.JButton closeButton;\n" +
        "  // End of variables declaration//GEN-END:variables\n" +
        "}",
        "X.java",
        null);
    CompilationUnit c = p.getCompilationUnitForName("X.java");
    
    assertFalse(c.isWithinGuardedBlocks(1, 1));
    assertTrue(c.isWithinGuardedBlocks(3, 3));
  }
  
}
