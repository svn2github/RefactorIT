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
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.query.usage.UnusedVariablesIndexer;
import net.sf.refactorit.test.Utils;

import org.apache.log4j.Category;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;



/**
 * Number of Tramps (NT) metric. The number of parameters in a class' methods
 * which are not referenced by its code.
 *
 * @author Daniel Wilken Damm
 */
public class NumberOfTrampsMetric {
  /** Hidden constructor. */
  private NumberOfTrampsMetric() {
  }

  /**
   * Calculates NT for the method.
   *
   * @param method method.
   *
   * @return Number of Tramps metric for the <code>method</code>.
   */
  public static int calculate(BinMethod method) {
    int NT = 0; // Number of Tramps

    BinLocalVariable[] unusedVars =
     UnusedVariablesIndexer.getUnusedVariablesFor(method);

    for (int i = 0; i < unusedVars.length; i++) {
      if (unusedVars[i] instanceof BinParameter) {
        if ((method != null) && !method.isFinal() && !method.isStatic() &&
              (method.getParameters().length > 0)) {
          NT++;
        }
      }
    }

    return NT;
  }

  /** Test driver for {@link NumberOfTrampsMetric}. */
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
      suite.setName("NT metric tests");

      return suite;
    }

    protected void setUp() throws Exception {
      project = Utils.createTestRbProject(
       Utils.getTestProjectsDirectory().getAbsolutePath() + "/Metrics/NT");
      project.getProjectLoader().build();
    }

    protected void tearDown() {
      project = null;
    }

    /**
     * Tests NT for method a
     */
    public void testMethodA() {
      cat.info("Testing NT for method a");
      assertEquals("NT", 1, getNtForMethod("Test", "a"));
      cat.info("SUCCESS");
    }

    /**
     * Tests NT for method b
     */
    public void testMethodB() {
      cat.info("Testing NT for method b");
      assertEquals("NT", 2, getNtForMethod("Test", "b"));
      cat.info("SUCCESS");
    }

    /**
     * Tests NT for method c
     */
    public void testMethodC() {
      cat.info("Testing NT for method c");
      assertEquals("NT", 0, getNtForMethod("Test", "c"));
      cat.info("SUCCESS");
    }

    /**
     * Tests NT for method d
     */
    public void testMethodD() {
      cat.info("Testing NT for method d");
      assertEquals("NT", 0, getNtForMethod("Test", "d"));
      cat.info("SUCCESS");
    }

    /**
     * Tests NT for method e
     */
    public void testMethodE() {
      cat.info("Testing NT for method e");
      assertEquals("NT", 0, getNtForMethod("Test", "e"));
      cat.info("SUCCESS");
    }

    /**
     * Gets NT metric for a method from test project.
     *
     * @param classFqn Fully Qualified Name of the test class.
     * @param method Name of the test method.
     *
     * @return NT metric.
     */
    private int getNtForMethod(String classFqn, String method) {
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
