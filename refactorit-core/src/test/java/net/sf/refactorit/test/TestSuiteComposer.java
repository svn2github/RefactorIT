/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.extensions.TestDecorator;
import junit.extensions.TestSetup;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestListener;
import junit.framework.TestResult;
import junit.framework.TestSuite;


/**
 * Composes RefactorIT test suite.
 */
public class TestSuiteComposer {
 
  /** Hidden constructor. */
  private TestSuiteComposer() {}

  /**
   * Composes test suite by calling <code>static Test suite()</code> method
   * on the class specified in system property
   * <code>junit.rootsuite.class</code>.
   *
   * @return test suite.
   */
  public static Test suite() throws Exception {
    try {
      // Cleanup or create tmp drectory if separate dir is configured
      final String tmp = System.getProperty("java.io.tmpdir");
      if (tmp != null && tmp.length() > 0) {
        File file = new File(tmp);
        if (file.exists()) {
          delete(file);
        }

        file.mkdir();
      }

      Utils.setUpTestingEnvironment();

      // disable not needed logging stuff
      PropertyConfigurator.configure("../src/test/reportLog4j.properties");

      final String rootSuiteClassName
          = System.getProperty("junit.rootsuite.class");

      // Get root suite class, invoke static Test suite() on it.
      // Make returned test suite root test suite.
      final Class rootSuiteClass = Class.forName(rootSuiteClassName);

      // static Test suite()
      final Method suiteMethod
          = rootSuiteClass.getMethod("suite", new Class[0]);

      return new MainTestSetup((Test) suiteMethod.invoke(null, new Object[0]));
    } catch (Throwable e) {
      e.printStackTrace(System.err);
      return null;
    }
  }

  private static void delete(File file) {
    if (file.isDirectory()) {
      File[] children = file.listFiles();
      for (int i = 0; i < children.length; i++) {
        delete(children[i]);
      }
    }

    file.delete();
  }
}

/**
 * 
 * MainTestSetup
 *  Note: if you rename this class or move other package~
 *   you must update also reportLog4j.properties file! 
 * 
 */
class MainTestSetup extends TestSetup implements TestListener {
  private static Category cat =
      Category.getInstance(MainTestSetup.class);

  private ReportingTestListener reportWriter;

  private long timeBefore;

  public MainTestSetup(Test test) {
    super(test);
  }

  protected void setUp() {
    System.out.println("Starting testing with " + countTestCases()
        + " test cases.");

    cat.info("Starting testing...");
    timeBefore = System.currentTimeMillis();
  }

  protected void tearDown() {
    cat.info("Testing completed in "
        + (System.currentTimeMillis() - timeBefore) + " ms");

    cat.info("Creating test report");
    try {
      reportWriter.writeReport(getTest(), new File("test_report.xml"));
      cat.info("Report created");
    } catch (IOException e) {
      cat.error("Failed to create report", e);
    }

    Category.shutdown();
  }

  public void run(TestResult result) {
    result.addListener(this);
    reportWriter = new ReportingTestListener();
    result.addListener(reportWriter);
    super.run(result);
  }

  /** TestListener interface implementation */
  public void addError(Test test, Throwable t) {
    cat.info("Test " + test + " generated an error", t);
  }

  public void addFailure(Test test, AssertionFailedError t) {
    cat.info("Test " + test + "failed", t);
  }

  public void startTest(Test test) {}

  public void endTest(Test test) {
    long total = Runtime.getRuntime().totalMemory();
    if (total > 0) {
      total = total / 1024 / 1024;
    }
    long free = Runtime.getRuntime().freeMemory();
    if (free > 0) {
      free = free / 1024 / 1024;
    }
    cat.info("Ended: " + test + " - total: " + total + "MB, free: " + free
        + "MB");
  }
  /** END TestListener interface implementation */
}


class ReportingTestListener implements TestListener {
  /** Maps test (Test) to error for that test if any (Throwable). */
  private final Map errors = new HashMap();

  /** Maps test (Test) to error for that test if any (AssertionFailedError). */
  private final Map failures = new HashMap();

  /** Set or tests (Test instances) run. */
  private final Set testsRun = new HashSet();

  public void addError(Test test, Throwable error) {
    // Record first error
    if (errors.get(test) == null) {
      errors.put(test, error);
    }
  }

  public void addFailure(Test test, AssertionFailedError failure) {
    // Record first failure
    if (failures.get(test) == null) {
      failures.put(test, failure);
    }
  }

  public void startTest(Test test) {}

  public void endTest(Test test) {
    testsRun.add(test);
  }

  /**
   * Writes report about test run to the file.
   *
   * @param suite test suite that was run.
   * @param file file.
   *
   * @throws IOException if I/O error occurs.
   */
  void writeReport(Test suite, File file) throws IOException {
    PrintWriter out = null;
    try {
      final String encoding = "UTF-8";
      out =
          new PrintWriter(
          new BufferedWriter(
          new OutputStreamWriter(new FileOutputStream(file), encoding)));
      out.println("<?xml version=\"1.0\" encoding=\"" + encoding + "\""
          + " standalone=\"yes\"?>");
      out.println();
      writeTestStatus(suite, out);
    } finally {
      if (out != null) {
        try {
          out.flush();
        } finally {
          out.close();
          out = null;
        }
      }
    }
  }

  /**
   * Writes information about the test/test suite to the specified output.
   *
   * @param test test or test suite.
   * @param out output to write to.
   *
   * @throws IOException if I/O error occurs.
   */
  private void writeTestStatus(Test test, PrintWriter out) throws IOException {
    // Skip test decorators if any
    Test currentTest = test;
    while (currentTest instanceof TestDecorator) {
      currentTest = ((TestDecorator) currentTest).getTest();
    }

    if (test instanceof TestSuite) {
      // Test suite
      final TestSuite suite = (TestSuite) test;
      out.println("<testsuite name=\"" + makeAttributeValue(suite.getName())
          + "\">");
      for (final Enumeration e = suite.tests(); e.hasMoreElements(); ) {
        final Test childTest = (Test) e.nextElement();
        writeTestStatus(childTest, out);
      }
      out.println("</testsuite>");
    } else {
      // Simple test
      final Throwable error = (Throwable) errors.get(test);
      final Throwable failure = (Throwable) failures.get(test);
      if ((error == null) && (failure == null)) {
        // PASSED
        out.println("<test name=\"" + makeAttributeValue(test.toString())
            + "\"/>");
      } else {
        // FAILED
        out.println("<test name=\"" + makeAttributeValue(test.toString())
            + "\">");

        if (error != null) {
          out.println("<error>");
          final StringWriter stackTrace = new StringWriter();
          error.printStackTrace(new PrintWriter(stackTrace));
          out.println(
              replaceNewlinesWithBr(
              makeTextNode(stackTrace.getBuffer().toString())));
          out.println("</error>");
        }

        if (failure != null) {
          out.println("<failure>");
          final StringWriter stackTrace = new StringWriter();
          failure.printStackTrace(new PrintWriter(stackTrace));
          out.println(
              replaceNewlinesWithBr(
              makeTextNode(stackTrace.getBuffer().toString())));
          out.println("</failure>");
        }

        out.println("</test>");
      }
    }
  }

  /**
   * Escapes text so that it is possible to include it as text node of an
   * XML tag.
   *
   * @param text text.
   *
   * @return text node value.
   */
  private static String makeTextNode(String text) {
    // Need to escape &, < and > characters.
    final char[] textChars = text.toCharArray();
    final int textCharsLength = textChars.length;
    final StringBuffer escapedText = new StringBuffer(textCharsLength * 2);
    for (int i = 0; i < textCharsLength; i++) {
      final char c = textChars[i];
      if (c == '&') {
        escapedText.append("&amp;");
      } else if (c == '<') {
        escapedText.append("&lt;");
      } else if (c == '>') {
        escapedText.append("&gt;");
      } else if ((c == 0x09) || (c == 0x0A) || (c == 0x0D)) {
        escapedText.append(c);
      } else if ((c < 0x20) || (c == 0xFFFE) || (c == 0xFFFF)) {
        // Invalid character:
        // Extensible Markup Language (XML) 1.0 (Second Edition)
        // W3C Recommendation 6 October 2000

        // Replace invalid characters with '?'
        escapedText.append('?');
      } else {
        escapedText.append(c);
      }
    }
    return escapedText.toString();
  }

  /**
   * Replaces newline characters in the text with &lt;br/&gt; tag.
   *
   * @return text with &lt;br/&gt; instead of newlines.
   */
  private static String replaceNewlinesWithBr(String text) {
    if (text.indexOf('\n') == -1) {
      return text; // Nothing to replace
    }

    final char[] textChars = text.toCharArray();
    final int textCharsLength = textChars.length;
    final StringBuffer escapedText = new StringBuffer(textCharsLength * 2);
    for (int i = 0; i < textCharsLength; i++) {
      final char c = textChars[i];
      if (c == '\r') {
        // Skip
      } else if (c == '\n') {
        escapedText.append("<br/>\n");
      } else {
        escapedText.append(c);
      }
    }
    return escapedText.toString();
  }

  /**
   * Escapes text so that it is possible to include it as value of an attribute
   * of XML tag.
   *
   * @param text text.
   *
   * @return text node value.
   */
  private static String makeAttributeValue(String text) {
    if (text == null) {
      return text;
    }

    if ((text.indexOf('&') == -1) && (text.indexOf('<') == -1)
        && (text.indexOf('>') == -1) && (text.indexOf('"') == -1)) {
      // Nothing to escape.
      return text;
    }

    // Need to escape &, <, > and " characters.
    final char[] textChars = text.toCharArray();
    final int textCharsLength = textChars.length;
    final StringBuffer escapedText = new StringBuffer(textCharsLength * 2);
    for (int i = 0; i < textCharsLength; i++) {
      final char c = textChars[i];
      if (c == '&') {
        escapedText.append("&amp;");
      } else if (c == '<') {
        escapedText.append("&lt;");
      } else if (c == '>') {
        escapedText.append("&gt;");
      } else if (c == '"') {
        escapedText.append("&quot;");
      } else {
        escapedText.append(c);
      }
    }
    return escapedText.toString();
  }
}
