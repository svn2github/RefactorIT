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
import java.io.FileFilter;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Eclipse Generics tests for Push Down.
 * @author Anton Safonov
 */
public class PushDownGenericsTest extends LoadingTest {

  /** Hidden constructor. */
  private PushDownGenericsTest() {}

  private static final class PushDownGenericsTestsExtractor implements FileFilter {
    static final FileFilter INSTANCE = new PushDownGenericsTestsExtractor();

    public boolean accept(File file) {
      return (file.isDirectory() && !"out".equals(file.getName())
          && !file.getName().startsWith("Push")) ||
          (file.isFile() && file.getName().endsWith(".java"));
    }
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite("PushDown Generics Eclipse tests");

    final Test pushDownGenericsTests = LoadingTest.suite(
        new File(Utils.getTestProjectsDirectory(), "PushDownTests"),
        PushDownGenericsTestsExtractor.INSTANCE);
    if (pushDownGenericsTests != null) {
      suite.addTest(pushDownGenericsTests);
    }

    return suite;
  }
}
