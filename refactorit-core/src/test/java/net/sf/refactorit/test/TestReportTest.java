/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test;

/**
 * Test contained in test report.
 */
class TestReportTest {

  /** Name. */
  private final String name;

  /** Path. */
  private final String path;

  /** Stack trace of error. */
  private final String errorStackTrace;

  /** Stack trace of failure. */
  private final String failureStackTrace;

  /**
   * Creates new TestReportTest
   *
   * @param name name of the test.
   * @param path full path to this test from root. For example, if test name is
   *             <code>someTest</code> and it is located
   *             under test suite <code>def</code>, which in turn in contained
   *             root test suite <code>abc</code>, then path is
   *             <code>abc/def/someTest</code>
   * @param failure stack trace of failure if any reported during this test.
   * @param error stack trace of error if any reported during this test.
   */
  TestReportTest(String name,
      String path,
      String errorStackTrace,
      String failureStackTrace) {

    this.name = name;
    this.path = path;
    this.errorStackTrace = errorStackTrace;
    this.failureStackTrace = failureStackTrace;
  }

  /**
   * Gets name of this test.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets path of this test.
   */
  public String getPath() {
    return path;
  }

  /**
   * Gets error stack trace.
   *
   * @return stack trace or <code>null</code> if no error was reported.
   */
  String getErrorStackTrace() {
    return errorStackTrace;
  }

  /**
   * Gets failure stack trace.
   *
   * @return stack trace or <code>null</code> if no failure was reported.
   */
  String getFailureStackTrace() {
    return failureStackTrace;
  }

  /**
   * Gets string representation of this test.
   *
   * @return string representation.
   */
  public String toString() {
    final StringBuffer result = new StringBuffer(getPath());
    if (getErrorStackTrace() != null) {
      result.append(", 1 error");
    }
    if (getFailureStackTrace() != null) {
      result.append(", 1 failure");
    }
    return result.toString();
  }

  /**
   * Whether this test passed.
   *
   * @return <code>true</code> if this test passed; <code>false</code>
   *         otherwise.
   */
  public boolean wasSuccessful() {
    return ((getErrorStackTrace() == null) && (getFailureStackTrace() == null));
  }
}
