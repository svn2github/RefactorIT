/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.classmodel;

import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.test.Utils;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 *
 * @author Aleksei Sosnovski
 */

public class DuplicateInterfacesTest extends TestCase {

  public static Test suite() {
    return new TestSuite(DuplicateInterfacesTest.class);
  }

  public void testIfDuplicateInterfacesOccur() {
    Project p = Utils.createTestRbProject(
        Utils.getTestProjects().getProject("Duplicate_Interfaces"));

    try {
      p.getProjectLoader().build();
    } catch (Exception e) {
      fail(e.getMessage());
    }

    BinTypeRef ref = p.getTypeRefForName("Pull3");
    assertNotNull(ref);
    assertEquals(1,ref.getSupertypes().length);
  }

}
