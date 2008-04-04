/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Allows accessing list of tests in test report.
 */
public class TestReport {

  /** Tests ({@link TestReportTest} instances). */
  private final List tests;

  /** Maps test name to test ({@link TestReportTest} instance). */
  private final Map nameToTest = new HashMap();

  /**
   * Loads test report from file.
   *
   * @param file file.
   * @throws Exception
   */
  public TestReport(File file) throws Exception {
    final DocumentBuilderFactory builderFactory =
        DocumentBuilderFactory.newInstance();

    final DocumentBuilder builder = builderFactory.newDocumentBuilder();
    final Document doc = builder.parse(file);

    this.tests = parseTestsuite(doc.getDocumentElement(), null);
    for (final Iterator i = tests.iterator();
        i.hasNext(); ) {
      final TestReportTest test = (TestReportTest) i.next();
      if ((test.getName() == null) || (test.getName().length() == 0)) {
        continue; // Don't add this test to name -> test map.
      }

      nameToTest.put(test.getName(), test);
    }
  }

  /**
   * Parses <code>testsuite</code> element.
   *
   * @param testsuiteElement <code>testsuite</code> element.
   * @param parentSuitePath path of test suite containing this one.
   *        <code>null</code> if this test suite is root test suite.
   *
   * @return list of tests ({@link TestReportTest} instances).
   *         Never returns <code>null</code>.
   */
  private static List parseTestsuite(Element testsuiteElement,
      String parentSuitePath) {

    final String name = testsuiteElement.getAttribute("name");
    final String path =
        (parentSuitePath == null) ? name : parentSuitePath + "/" + name;

    final NodeList children = testsuiteElement.getChildNodes();
    final List tests = new LinkedList();
    if (children != null) {
      for (int i = 0, max = children.getLength(); i < max; i++) {
        final Node child = children.item(i);
        if (child.getNodeType() != Node.ELEMENT_NODE) {
          continue; // Not an element
        }
        final Element childElement = (Element) child;
        if ("testsuite".equals(childElement.getTagName())) {
          // testsuite element encountered
          tests.addAll(parseTestsuite((Element) child, path));
        } else if ("test".equals(childElement.getTagName())) {
          // test element encountered
          tests.add(parseTest((Element) child, path));
        }
      }
    }

    return tests;
  }

  /**
   * Parses <code>test</code> element.
   *
   * @param testElement <code>test</code> element.
   * @param parentSuitePath path of test suite containing this test.
   *
   * @return test. Never returns <code>null</code>.
   */
  private static TestReportTest parseTest(Element testElement,
      String parentSuitePath) {

    final Element errorElement = getFirstChild(testElement, "error");
    final Element failureElement = getFirstChild(testElement, "failure");

    final String errorStackTrace =
        ((errorElement == null) ? null : parseStackTrace(errorElement));
    final String failureStackTrace =
        ((failureElement == null) ? null : parseStackTrace(failureElement));

    final String name = testElement.getAttribute("name");
    final String path = parentSuitePath + "/" + name;
    return new TestReportTest(name,
        path,
        errorStackTrace,
        failureStackTrace);
  }

  /**
   * Parses stack trace (<code>error</code> or <code>failure</code>) element.
   *
   * @param stackTraceElement <code>error</code> or <code>failure</code>
   *                          element.
   * @return stack trace.
   */
  private static String parseStackTrace(Element stackTraceElement) {
    final NodeList children = stackTraceElement.getChildNodes();
    if (children != null) {
      final StringBuffer stackTrace = new StringBuffer();
//      final String lineSeparator = System.getProperty("line.separator");
      for (int i = 0, max = children.getLength(); i < max; i++) {
        final Node child = children.item(i);
        if (child.getNodeType() != Node.ELEMENT_NODE) {
          // Not an element => convert to text
          if (child.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
            final String childNodeName = child.getNodeName();
            if ("lt".equals(childNodeName)) {
              stackTrace.append('<');
            } else if ("gt".equals(childNodeName)) {
              stackTrace.append('>');
            } else if ("amp".equals(childNodeName)) {
              stackTrace.append('&');
            } else if ("quot".equals(childNodeName)) {
              stackTrace.append('\"');
            } else if ("apos".equals(childNodeName)) {
              stackTrace.append('\'');
            } else {
              throw new IllegalArgumentException("Unknown entity reference: "
                  + childNodeName);
            }
          } else {
            stackTrace.append(child.getNodeValue());
          }
          continue;
        }

        final Element childElement = (Element) child;
        if ("br".equals(childElement.getTagName())) {
          // br element encountered
          // stackTrace.append(lineSeparator);
        } else {
          // convert all other elements to text
          stackTrace.append(childElement.getNodeValue());
        }
      }

      return trimLinefeeds(stackTrace.toString());
    } else {
      // No child nodes
      return null;
    }
  }

  /**
   * Gets first child of the specified element which has specified name.
   *
   * @param element element.
   * @param name name.
   *
   * @return child or <code>null</code> if not found.
   */
  private static Element getFirstChild(Element element, String name) {
    final NodeList children = element.getElementsByTagName(name);
    if ((children == null) || (children.getLength() == 0)) {
      return null;
    }

    return (Element) children.item(0);
  }

  /**
   * Trims linefeeds from the beginning and end of the text.
   *
   * @param text text
   *
   * @return text without linefeeds at the beginning and at the end.
   */
  private static String trimLinefeeds(String text) {
    String result = text;
    int i = 0;
    while ((i < result.length())) {
      final char c = result.charAt(i);
      if ((c != '\r') && (c != '\n')) {
        break;
      }
      i++;
    }

    // i points to first non-linefeed character from the beginning of result
    if (i >= result.length()) {
      return "";
    }

    if (i > 0) {
      result = result.substring(i);
    }

    i = result.length() - 1;
    while (i >= 0) {
      final char c = result.charAt(i);
      if ((c != '\r') && (c != '\n')) {
        break;
      }
      i--;
    }

    // i points to first non-linefeed character from the end of result
    if (i <= 0) {
      return "";
    }

    if (i < (result.length() - 1)) {
      result = result.substring(0, i + 1);
    }

    return result;
  }

  /**
   * Gets list of tests.
   *
   * @return tests ({@link TestReportTest} instances). Never returns
   *         <code>null</code>.
   */
  public List getTests() {
    return tests;
  }

  /**
   * Gets test.
   *
   * @param name test name.
   *
   * @return test or <code>null</code> if not found.
   */
  public TestReportTest getTest(String name) {
    return (TestReportTest) nameToTest.get(name);
  }

  /**
   * Lists tests that passed.
   *
   * @return list of tests ({@link TestReportTest} instances).
   *          Never returns <code>null</code>.
   */
  public List listPassedTests() {
    final List passedTests = new LinkedList();
    for (final Iterator i = getTests().iterator(); i.hasNext(); ) {
      final TestReportTest test = (TestReportTest) i.next();
      if (test.wasSuccessful()) {
        passedTests.add(test);
      }
    }

    return passedTests;
  }
}
