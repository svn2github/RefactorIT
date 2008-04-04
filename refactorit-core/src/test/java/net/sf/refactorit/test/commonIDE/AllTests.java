/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.commonIDE;

import net.sf.refactorit.commonIDE.CacheTest;
import net.sf.refactorit.commonIDE.WorkspaceTest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * @author Tonis Vaga
 */
public class AllTests extends TestCase {
  public AllTests() {
  }

  public AllTests(String name) {
    super(name);
  }

  public static Test suite() {
    TestSuite result = new TestSuite(AllTests.class.getName());
    result.addTestSuite(CacheTest.class);
    result.addTestSuite(WorkspaceTest.class);
    result.addTestSuite(ItemByCoordinateFinderTest.class);
    return result;
  }

}
