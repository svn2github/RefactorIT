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
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.query.CallTreeIndexer;
import net.sf.refactorit.test.Utils;

import org.apache.log4j.Category;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Lack of Cohesion of Methods (LCOM) metric.
 * A measure for the cohesiveness of a class using the Henderson-Sellers method.
 *
 * @author Daniel Wilken Damm
 */
public class LackOfCohesionMetric {
  /** Hidden constructor. */
  private LackOfCohesionMetric() {
  }

  /**
    * Calculates LCOM using the Henderson-Sellers method for the class.
    *
    * @param type type.
    *
    * @return Lack of Cohesion of Methods metric for the <code>type</code>.
    */
  public static double calculate(BinCIType type) {
    BinField[] fields = type.getDeclaredFields();
    int nNumberOfFields = fields.length; // Fields in type

    BinMethod[] methods = type.getDeclaredMethods();
    int nNumberOfMethods = methods.length; // Methods in type

    if ((nNumberOfFields == 0) || (nNumberOfMethods == 0) ||
       (nNumberOfMethods == 1)) {
      return 0; // Shortcut
    }

    CallTreeIndexer indexer = new CallTreeIndexer();
    type.accept(indexer);

    MultiValueMap invocations = indexer.getInvocationsNet();
    List invokedInMembers = new ArrayList();
    int methodsAccessed;
    int sum = 0;

    for (int i = 0; i < nNumberOfFields; i++) {
      // Methods utilising field
      invokedInMembers = invocations.get(fields[i]);

      methodsAccessed = (invokedInMembers != null)
      ? invokedInMembers.size()
      : 0;

      sum += methodsAccessed;
    }

    double average = (double) sum / (double) nNumberOfFields;
    return Math.abs((average - nNumberOfMethods) / (1 - nNumberOfMethods));
  }

  /** Test driver for {@link LackOfCohesionMetric}. */
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
      suite.setName("LCOM metric tests");

      return suite;
    }

    protected void setUp() throws Exception {
      project = Utils.createTestRbProject(
          Utils.getTestProjectsDirectory().getAbsolutePath() + "/Metrics/LCOM");
      project.getProjectLoader().build();
    }

    protected void tearDown() {
      project = null;
    }

    /**
     * Tests LCOM for class LCOMtest1
     */
    public void testLCOMtest1() {
      cat.info("Testing LCOM for class LCOMtest1");
      assertLcom("LCOM.LCOMtest1", 1.125);
      cat.info("SUCCESS");
    }

    /**
     * Tests LCOM for class LCOMtest1 innerfields
     */
    public void testLCOMtest1Inner() {
      cat.info("Testing LCOM for class LCOMtest1 innerfields");
      assertLcom("LCOM.LCOMtest1$Innerfields", 0.5);
      cat.info("SUCCESS");
    }

    /**
     * Tests LCOM for class LCOMtest2
     */
    public void testLCOMtest2() {
      cat.info("Testing LCOM for class LCOMtest2");
      assertLcom("LCOM.LCOMtest2", 7d / 6d);
      cat.info("SUCCESS");
    }

    /**
     * Tests LCOM for class LCOMtest2 innerfields
     */
    public void testLCOMtest2Inner() {
      cat.info("Testing LCOM for class LCOMtest2 innerfields");
      assertLcom("LCOM.LCOMtest2$Innerfields", 0.0);
      cat.info("SUCCESS");
    }

    /**
     * Tests LCOM for class LCOMtest3
     */
    public void testLCOMtest3() {
      cat.info("Testing LCOM for class LCOMtest3");
      assertLcom("LCOM.LCOMtest3", 1.0);
      cat.info("SUCCESS");
    }

    /**
     * Tests LCOM for class LCOMtest4
     */
    public void testLCOMtest4() {
      cat.info("Testing LCOM for class LCOMtest4");
      assertLcom("LCOM.LCOMtest4", 0.875);
      cat.info("SUCCESS");
    }

    /**
     * Asserts that LCOM metric for the class is as expected.
     *
     * @param fqn Fully Qualified Name of the type to check.
     * @param expectedLCOM expected LCOM value.
     */
    private void assertLcom(String fqn, double expectedLCOM) {
      final BinCIType type = project.getTypeRefForName(fqn).getBinCIType();
      if (type == null) {
        throw new IllegalArgumentException("Class \"" + fqn + "\" not found");
      }

      final double LCOM = LackOfCohesionMetric.calculate(type);
      assertEquals("LCOM for type \"" + fqn + "\"", expectedLCOM, LCOM, 0.001);
    }
  }
}
