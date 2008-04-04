/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.common.util.cvsutil;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author risto
 */
public class AllTests {

  public static Test suite() {
    TestSuite result = new TestSuite();
    result.addTestSuite(CvsEntriesLineTest.class);
    return result;
  }

}
