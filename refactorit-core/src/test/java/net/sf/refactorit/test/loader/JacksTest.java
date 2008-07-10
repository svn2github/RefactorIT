/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 *
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.loader;

import net.sf.refactorit.test.ProjectMetadata;
import net.sf.refactorit.test.Utils;

import org.apache.log4j.Category;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import junit.framework.Test;
import junit.framework.TestSuite;



/**
 * Jacks tests.
 */
public class JacksTest {
  /** Logger instance. */
  private static final Category cat =
      Category.getInstance(JacksTest.class.getName());

  /** Hidden constructor. */
  private JacksTest() {}

  public static Test suite() {
    cat.debug("Creating Jacks compiler test suite");
    final TestSuite suite = new TestSuite("Jacks compiler tests");

    final Test jacksTests =
        JacksTest.suite(new File(Utils.getTestFileDirectory(), "misc/jacks"));
    if (jacksTests != null) {
      suite.addTest(jacksTests);
    }
    cat.debug("Jacks compiler test suite contains " + suite.countTestCases()
        + " tests");
    return suite;
  }

  private static final class TestSourceAndDirectoryFilter implements FileFilter {
    static final TestSourceAndDirectoryFilter INSTANCE =
        new TestSourceAndDirectoryFilter();

    public boolean accept(File file) {
      return ((file.isDirectory()) ||
          ((file.isFile()) && (file.getName().endsWith(".test"))));
    }
  }


  /**
   * Creates test suite ot of Jacks tests directory.
   *
   * @param directory directory.
   *
   * @return test suite or <code>null</code> null if no tests found.
   */
  private static Test suite(File directory) {
    cat.debug("Generating test suite for " + directory);
    final File[] testFilesAndDirectories =
        directory.listFiles(TestSourceAndDirectoryFilter.INSTANCE);
    if (testFilesAndDirectories == null) {
      cat.debug("No files/directories found");
      return null;
    }

    final TestSuite suite = new TestSuite(directory.getName());

    for (int i = 0; i < testFilesAndDirectories.length; i++) {
      final File file = testFilesAndDirectories[i];
      if (file.isDirectory()) {
        // directory
        final Test directoryTests = suite(file);
        if ((directoryTests == null)
            || (directoryTests.countTestCases() == 0)) {
          continue; // Don't add empty test suites
        }
        suite.addTest(directoryTests);
      } else {
        // .test definition file
        try {
          suite.addTest(createSourceLoadingTest(file));
        } catch (IOException e) {
          cat.warn("Failed to add test " + file.getAbsolutePath()
              + " to test suite",
              e);
        }
      }
    }

    if (suite.countTestCases() == 0) {
      cat.debug("No tests for " + directory.getAbsolutePath());
      return null;
    } else {
      cat.debug(suite.countTestCases() + " tests for "
          + directory.getAbsolutePath());
      return suite;
    }
  }

  /**
   * Creates source loading test for the specified test file.
   *
   * @param test source file.
   *
   * @return test. Never returns <code>null</code>.
   */
  private static Test createSourceLoadingTest(File test) throws IOException {
    cat.debug("Creating test from " + test.getAbsolutePath());
    final String testName = test.getName();

    final List sourcePaths = new LinkedList();
    BufferedReader in = null;
    try {
      in = new BufferedReader(new FileReader(test));
      // Read comma-separated list of source files
      final String sourceListLine = in.readLine();
      StringTokenizer tokenizer =
          new StringTokenizer(sourceListLine, ",");
      while (tokenizer.hasMoreTokens()) {
        final File sourcePathElement =
            new File(test.getParentFile(), tokenizer.nextToken());
        if (sourcePathElement.exists()) {

// supported that test in a hacky way in lexer :)
//          if( !RefactorItConstants.runNotImplementedTests && sourcePathElement.getName().indexOf("T3510") != -1 ) {
//            continue;
//          }

          sourcePaths.add(sourcePathElement.getAbsolutePath());
        }
      }

      // Read classpath
      final String classpath = in.readLine();
      if (classpath.length() != 0) {
        // Classpath can be separated by either Windows or UNIX separators.
        tokenizer =
            new StringTokenizer(classpath, ";");
        while (tokenizer.hasMoreTokens()) {
          final String classpathElement = tokenizer.nextToken();
          if (".".equals(classpathElement)) {
            sourcePaths.add(test.getParentFile().getAbsolutePath());
          } else {
            sourcePaths.add(
                new File(test.getParentFile(),
                classpathElement).getAbsolutePath());
          }
        }
      }
    } finally {
      if (in != null) {
        in.close();
        in = null;
      }
    }

    final ProjectMetadata metadata =
        new ProjectMetadata(
        testName.substring(0, testName.length() - 5),
        testName,
        null,
        sourcePaths,
        null,
        null,
        true);
    return new ProjectLoadTest(metadata);
  }
}
