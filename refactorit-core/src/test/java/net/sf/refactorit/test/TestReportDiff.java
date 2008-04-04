/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * Compares two test reports.
 */
public class TestReportDiff {

  /** Last report. */
  private final TestReport lastReport;

  /** Current report being compared against last one. */
  private final TestReport currentReport;

  /**
   * Constructs new diff for the two specified reports.
   *
   * @param lastReport last report.
   * @param currentReport current report to be compared against last one.
   */
  public TestReportDiff(TestReport lastReport, TestReport currentReport) {
    this.lastReport = lastReport;
    this.currentReport = currentReport;
  }

  /**
   * Lists tests that are new.
   *
   * @return list of tests ({@link TestReportTest} instances) new in current
   *          report compared to last report.
   *          Never returns <code>null</code>.
   */
  public List listNewTests() {
    final List newTests = new LinkedList();
    for (final Iterator i = currentReport.getTests().iterator(); i.hasNext(); ) {
      final TestReportTest test = (TestReportTest) i.next();
      if (lastReport.getTest(test.getName()) == null) {
        newTests.add(test);
      }
    }

    return newTests;
  }

  /**
   * Lists tests that are missing.
   *
   * @return list of tests ({@link TestReportTest} instances) that are missing
   *          from current report comapared to last report.
   *          Never returns <code>null</code>.
   */
  public List listMissingTests() {
    final List missingTests = new LinkedList();
    for (final Iterator i = lastReport.getTests().iterator(); i.hasNext(); ) {
      final TestReportTest test = (TestReportTest) i.next();
      if (currentReport.getTest(test.getName()) == null) {
        missingTests.add(test);
      }
    }

    return missingTests;
  }

  /**
   * Lists tests that now pass but were previously broken.
   *
   * @return list of tests ({@link TestReportTest} instances).
   *          Never returns <code>null</code>.
   */
  public List listFixedTests() {
    final List fixedTests = new LinkedList();
    for (final Iterator i = currentReport.getTests().iterator(); i.hasNext(); ) {
      final TestReportTest currentTest = (TestReportTest) i.next();
      if (!currentTest.wasSuccessful()) {
        // Test is broken
        continue;
      }

      final TestReportTest lastTest = lastReport.getTest(currentTest.getName());
      if (lastTest == null) {
        // New test
        continue;
      }

      if (!lastTest.wasSuccessful()) {
        fixedTests.add(currentTest);
      }
    }

    return fixedTests;
  }

  /**
   * Lists tests that broke.
   *
   * @return list of tests ({@link TestReportTest} instances).
   *          Never returns <code>null</code>.
   */
  public List listBrokenTests() {
    final List brokenTests = new LinkedList();
    for (final Iterator i = lastReport.getTests().iterator(); i.hasNext(); ) {
      final TestReportTest lastTest = (TestReportTest) i.next();
      if (!lastTest.wasSuccessful()) {
        // Test is broken
        continue;
      }

      final TestReportTest currentTest =
          currentReport.getTest(lastTest.getName());
      if (currentTest == null) {
        // Test has been removed
        continue;
      }

      if (!currentTest.wasSuccessful()) {
        brokenTests.add(currentTest);
      }
    }

    return brokenTests;
  }

  /**
   * Lists failing tests that still fail but differently.
   *
   * @return list of tests ({@link TestReportTest} instances).
   *          Never returns <code>null</code>.
   */
  public List listDifferentlyFailingTests() {
    final List differentlyFailingTests = new LinkedList();
    for (final Iterator i = currentReport.getTests().iterator(); i.hasNext(); ) {
      final TestReportTest currentTest = (TestReportTest) i.next();
      if (currentTest.wasSuccessful()) {
        // Test doesn't fail
        continue;
      }

      final TestReportTest lastTest = lastReport.getTest(currentTest.getName());
      if (lastTest == null) {
        // New test
        continue;
      }

      if (lastTest.wasSuccessful()) {
        // Current test is broken
        continue;
      }

      if ((!stackTraceEquals(currentTest.getErrorStackTrace(),
          lastTest.getErrorStackTrace()))
          || (!stackTraceEquals(currentTest.getFailureStackTrace(),
          lastTest.getFailureStackTrace()))) {

        differentlyFailingTests.add(currentTest);
      }
    }

    return differentlyFailingTests;
  }

  /**
   * Checks whether two stack traces are equal.
   *
   * @param trace1 first stack trace.
   * @param trace2 second stack trace.
   *
   * @return <code>true</code> if and only if two stacktraces are equal;
   *         <code>false</code> otherwise.
   */
  public static boolean stackTraceEquals(String trace1, String trace2) {
    if ((trace1 == null) && (trace2 == null)) {
      return true;
    }

    if (((trace1 == null) && (trace2 != null))
        || ((trace1 != null) && (trace2 == null))) {
      return false;
    }

    // Now compare only exception type and message
    BufferedReader reader1 = null;
    BufferedReader reader2 = null;
    try {
      reader1 = new BufferedReader(new StringReader(trace1));
      final String firstLine1 = reader1.readLine();
      reader2 = new BufferedReader(new StringReader(trace2));
      final String firstLine2 = reader2.readLine();

      return (firstLine1.equals(firstLine2));
    } catch (IOException e) {
      return false;
    } finally {
      if (reader1 != null) {
        try {
          reader1.close();
        } catch (IOException e) {}
        reader1 = null;
      }

      if (reader2 != null) {
        try {
          reader2.close();
        } catch (IOException e) {}
        reader2 = null;
      }
    }
  }

  public static final void main(String params[]) throws Exception {
    final File lastReportFile = new File(params[0]);
    final File currentReportFile = new File(params[1]);
    final TestReport lastReport = new TestReport(lastReportFile);
    final TestReport currentReport = new TestReport(currentReportFile);

    final TestReportDiff diff = new TestReportDiff(lastReport, currentReport);

    showReportInfo("LAST REPORT   :", lastReport);
    showReportInfo("CURRENT REPORT:", currentReport);

    final List newTests = diff.listNewTests();
    dumpNewTests(newTests);
    final List removedTests = diff.listMissingTests();
    dumpTests("REMOVED", removedTests, true);
    final List fixedTests = diff.listFixedTests();
    dumpTests("FIXED", fixedTests, false);
    final List brokenTests = diff.listBrokenTests();
    dumpBrokenTests(brokenTests);
    final List differentlyFailingTests = diff.listDifferentlyFailingTests();
    dumpDifferentlyFailingTests(differentlyFailingTests, lastReport);
    if ((newTests.size() > 0) || (removedTests.size() > 0)
        || (fixedTests.size() > 0) || (brokenTests.size() > 0)
        || (differentlyFailingTests.size() > 0)) {

      System.out.println();
      System.out.println("CHANGES FOUND!");
    }
  }

  /**
   * Lists test one per line to System.out.
   *
   * @param title title to append to the list of tests.
   * @param tests tests.
   * @param statusShown whether to show status (PASS/FAIL) for each test.
   */
  private static void dumpTests(String title, List tests, boolean statusShown) {
    if (tests.size() == 0) {
      return;
    }

    System.out.println(title + ": " + tests.size());
    for (final Iterator i = tests.iterator(); i.hasNext(); ) {
      final TestReportTest test = (TestReportTest) i.next();
      if (statusShown) {
        System.out.println("* " + test);
      } else {
        System.out.println("* " + test.getPath());
      }
    }
  }

  /**
   * Dumps new tests.
   *
   * @param tests tests.
   */
  private static void dumpNewTests(List tests) {

    if (tests.size() == 0) {
      return;
    }

    System.out.println("NEW: " + tests.size());
    for (final Iterator i = tests.iterator(); i.hasNext(); ) {
      final TestReportTest currentTest = (TestReportTest) i.next();
      System.out.println("* " + currentTest);
      if (currentTest.getErrorStackTrace() != null) {
        System.out.println(currentTest.getErrorStackTrace());
        System.out.println();
      } else if (currentTest.getFailureStackTrace() != null) {
        System.out.println(currentTest.getFailureStackTrace());
        System.out.println();
      }
    }
  }

  /**
   * Dumps broken tests.
   *
   * @param tests tests.
   */
  private static void dumpBrokenTests(List tests) {

    if (tests.size() == 0) {
      return;
    }

    System.out.println("BROKEN: " + tests.size());
    for (final Iterator i = tests.iterator(); i.hasNext(); ) {
      final TestReportTest currentTest = (TestReportTest) i.next();
      System.out.println("* " + currentTest.getPath());
      if (currentTest.getErrorStackTrace() != null) {
        System.out.println(currentTest.getErrorStackTrace());
      } else if (currentTest.getFailureStackTrace() != null) {
        System.out.println(currentTest.getFailureStackTrace());
      }

      System.out.println();
    }
  }

  /**
   * Dumps differently failing tests with differences shown.
   *
   * @param tests tests
   * @param lastReport last report
   */
  private static void dumpDifferentlyFailingTests(List tests,
      TestReport lastReport) {

    if (tests.size() == 0) {
      return;
    }

    System.out.println("FAILING DIFFERENTLY: " + tests.size());
    for (final Iterator i = tests.iterator(); i.hasNext(); ) {
      final TestReportTest currentTest = (TestReportTest) i.next();
      System.out.println("* " + currentTest.getPath());
      if (currentTest.getErrorStackTrace() != null) {
        System.out.println(currentTest.getErrorStackTrace());
      } else if (currentTest.getFailureStackTrace() != null) {
        System.out.println(currentTest.getFailureStackTrace());
      }

      System.out.println();
      System.out.println("Previously:");
      final TestReportTest lastTest = lastReport.getTest(currentTest.getName());
      if (lastTest.getErrorStackTrace() != null) {
        System.out.println(lastTest.getErrorStackTrace());
      } else if (lastTest.getFailureStackTrace() != null) {
        System.out.println(lastTest.getFailureStackTrace());
      }
      System.out.println();
    }
  }

  /**
   * Shows brief information about tests in report.
   *
   * @param title title
   * @param report report
   */
  private static void showReportInfo(String title, TestReport report) {
    final int testCount = report.getTests().size();
    final int passedCount = report.listPassedTests().size();
    System.out.println(title + " " + testCount + " tests."
        + " " + passedCount + " pass."
        + " " + (testCount - passedCount) + " fail.");
  }
}
