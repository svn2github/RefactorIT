/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.netbeans.standalone;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * These tests don't have to be ran under NetBeans, they can be ran standaolne
 * with JUnit as usual (and they could be added to the main test suite).
 *
 * @author risto
 */
public class AllStandaloneTests extends TestCase {

  public AllStandaloneTests(String name) {
    super(name);
  }

  public static Test suite() {
    TestSuite result = new TestSuite();

    result.addTestSuite(IdeVersionTest.class);
    result.addTestSuite(ErrorManagerTest.class);
    result.addTestSuite(FileBasedOptionsTest.class);

    return result;
  }
}
