/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.netbeans.vfs;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author risto
 */
public class FileNotFoundReasonTest {
  public static Test suite() {
    TestSuite result = new TestSuite();
    
    result.addTestSuite(FileNotFoundReasonGeneralTest.class);
    result.addTestSuite(FileNotFoundReasonFsCapabilitiesTest.class);
    result.addTestSuite(FileNotFoundReasonFileNotOnAutodetectedPathTest.class);
    result.addTestSuite(FileNotFoundReasonInSourceWindowTest.class);
    
    return result;
  }
}
