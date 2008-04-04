/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.loader.jdk5;


import net.sf.refactorit.parser.FastJavaLexer;
import net.sf.refactorit.test.ProjectMetadata;
import net.sf.refactorit.test.loader.ProjectLoadTest;

import org.apache.log4j.Category;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;


public abstract class LoadingTest {

  /** Logger instance. */
  protected static final Category cat
      = Category.getInstance(LoadingTest.class.getName());

  protected static final class TestSourceAndDirectoryFilter implements FileFilter {
    static final FileFilter INSTANCE = new TestSourceAndDirectoryFilter();

    public boolean accept(File file) {
      return ((file.isDirectory()) ||
          (file.isFile() && file.getName().endsWith(".java")));
    }
  }

  /**
   * Creates test suite ot of Jdk50 tests directory.
   * @param directory directory.
   * @param customFilter to filter out wrong dirs and files.
   * @return test suite or <code>null</code> null if no tests found.
   */
  protected static Test suite(File directory, FileFilter customFilter) {
    cat.debug("Generating test suite for " + directory);
    final File[] testFilesAndDirectories = directory.listFiles(customFilter);
    if (testFilesAndDirectories == null) {
      cat.debug("No files/directories found");
      return null;
    }

    final TestSuite suite = new TestSuite(directory.getName());

    for (int i = 0; i < testFilesAndDirectories.length; i++) {
      final File file = testFilesAndDirectories[i];
      if (file.isDirectory()) {
        // directory
        final Test directoryTests = suite(file, customFilter);
        if (directoryTests == null || directoryTests.countTestCases() == 0) {
          continue; // Don't add empty test suites
        }
        suite.addTest(directoryTests);
      } else {
        // .pass definition file
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

    final List sourcePaths = new ArrayList(1);
    final List classPaths = new ArrayList(1);
    final File sourcePathElement = test;

    if (sourcePathElement.exists()) {
      sourcePaths.add(sourcePathElement.getAbsolutePath());
    }

    sourcePaths.add(test.getParentFile().getAbsolutePath());
    classPaths.add(test.getParentFile().getAbsolutePath());

    String prName = test.getParentFile().getName() + "/" + test.getName();
    File superParent = test.getParentFile().getParentFile();
    if (!AutoLoadingTest.jdk50BaseDirName.equals(superParent.getName())) {
      prName = superParent.getName() + "/" + prName;
    }

    final ProjectMetadata metadata =
        new ProjectMetadata(
            prName,
            prName,
            null,
            sourcePaths,
            classPaths,
            null,
            true,
            FastJavaLexer.JVM_50);
    return new ProjectLoadTest(metadata);
  }

}
