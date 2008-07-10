/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 *
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.loader.jdk5;


import net.sf.refactorit.test.Utils;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * JDK 5.0 tests from NetBeans 4.0.
 * @author Ahti Kitsik
 */
public class AutoLoadingTest extends LoadingTest {

  protected static final String jdk50BaseDirName = "misc/jdk15";

  /** Hidden constructor. */
  private AutoLoadingTest() {}

  public static Test suite() {
    final TestSuite suite = new TestSuite("Imported from NB4");

    final Test jdk50Tests
        = LoadingTest.suite(
        new File(Utils.getTestFileDirectory(), jdk50BaseDirName),
        TestSourceAndDirectoryFilter.INSTANCE);
    if (jdk50Tests != null) {
      suite.addTest(jdk50Tests);
    }

    return suite;
  }
}
