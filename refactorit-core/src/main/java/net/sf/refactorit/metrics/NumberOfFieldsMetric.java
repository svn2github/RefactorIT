/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.metrics;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.statements.BinStatementList;
import net.sf.refactorit.query.AbstractIndexer;
import net.sf.refactorit.test.Utils;

import org.apache.log4j.Category;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Number of Fields in Method (NOF) metric. The number of fields in a method.
 *
 * @author Daniel Wilken Damm
 */
public class NumberOfFieldsMetric {
  /** Hidden constructor. */
  private NumberOfFieldsMetric() {
  }

  /**
   * Calculates number of fields metric for the method.
   *
   * @param method method.
   *
   * @return Number of Fields metric for the <code>method</code>.
   */
  public static int calculate(BinMethod method) {
    final List fields = new ArrayList();
    final BinStatementList body = method.getBody();

    if (body == null) {
      return 0; // Shortcut
    }

    final AbstractIndexer indexer = new AbstractIndexer() {
        public void visit(final BinField field) {
          fields.add(field);
        }
      };

    indexer.visit(body);

    return fields.size();
  }

  /** Test driver for {@link NumberOfFieldsMetric}. */
  public static class TestDriver extends TestCase {
    /** Logger instance. */
    private static final Category cat =
     Category.getInstance(TestDriver.class.getName());

    /** Test project. */
    private Project project;

    public TestDriver(String name) {
      super(name);
    }

    public static Test suite() {
      final TestSuite suite = new TestSuite(TestDriver.class);
      suite.setName("NOF metric tests");

      return suite;
    }

    protected void setUp() throws Exception {
      project = Utils.createTestRbProject(
       Utils.getTestProjectsDirectory().getAbsolutePath() + "/Metrics/NOF");
      project.getProjectLoader().build();
    }

    protected void tearDown() {
      project = null;
    }

    /**
     * Tests NOF for method a
     */
    public void testMethodA() {
      cat.info("Testing NOF for method a");
      assertEquals("NOF", 0, getNofForMethod("Test", "a"));
      cat.info("SUCCESS");
    }

    /**
     * Tests NOF for method b
     */
    public void testMethodB() {
      cat.info("Testing NOF for method b");
      assertEquals("NOF", 1, getNofForMethod("Test", "b"));
      cat.info("SUCCESS");
    }

    /**
     * Tests NOF for method c
     */
    public void testMethodC() {
      cat.info("Testing NOF for method c");
      assertEquals("NOF", 2, getNofForMethod("Test", "c"));
      cat.info("SUCCESS");
    }

    /**
     * Gets NOF metric for a method from test project.
     *
     * @param classFqn Fully Qualified Name of the test class.
     * @param method Name of the test method.
     *
     * @return NOF metric.
     */
    private int getNofForMethod(String classFqn, String method) {
      BinCIType testClass = project.getTypeRefForName(classFqn).getBinCIType();
      BinMethod[] methods = testClass.getDeclaredMethods();
      BinMethod binTestMethod = null;

      for (int i = 0; i < methods.length; i++) {
        if (methods[i].getName().equals(method)) {
          binTestMethod = methods[i];
          break;
        }
      }

      if (binTestMethod == null) {
        throw new IllegalArgumentException(
         "Test method \"" + method + "\" not found" + " in test class");
      }

      return calculate(binTestMethod);
    }
  }
}
