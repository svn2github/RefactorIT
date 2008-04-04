/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.metrics;



import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.test.Utils;

import org.apache.log4j.Category;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Number Of Children (NOC) metric. Number of direct subclasses of a class.
 */
public class NocMetric {

  /** Hidden constructor. */
  private NocMetric() {}

  /**
   * Calculates NOC for the class.
   *
   * @param clazz class.
   *
   * @return NOC metric for the class.
   */
  public static int calculate(BinClass clazz) {
    return clazz.getTypeRef().getDirectSubclasses().size();
  }

  /** Test driver for {@link NocMetric}. */
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
      suite.setName("NOC metric tests");
      return suite;
    }

    protected void setUp() throws Exception {
      project =
          Utils.createTestRbProject(Utils.getTestProjects().getProject("NOC"));
      project.getProjectLoader().build();
    }

    protected void tearDown() {
      project = null;
    }

    /**
     * Tests NOC for class A.
     */
    public void testA() {
      cat.info("Testing NOC for class A");
      assertEquals("NOC", 2, getNocForClass("A"));
      cat.info("SUCCESS");
    }

    /**
     * Tests NOC for class B1.
     */
    public void testB1() {
      cat.info("Testing NOC for class B1");
      assertEquals("NOC", 1, getNocForClass("B1"));
      cat.info("SUCCESS");
    }

    /**
     * Tests NOC for class B2.
     */
    public void testB2() {
      cat.info("Testing NOC for class B2");
      assertEquals("NOC", 1, getNocForClass("B2"));
      cat.info("SUCCESS");
    }

    /**
     * Tests NOC for class C.
     */
    public void testC() {
      cat.info("Testing NOC for class C");
      assertEquals("NOC", 0, getNocForClass("C"));
      cat.info("SUCCESS");
    }

    /**
     * Tests NOC for class D.
     */
    public void testD() {
      cat.info("Testing NOC for class D");
      assertEquals("NOC", 0, getNocForClass("D"));
      cat.info("SUCCESS");
    }

    /**
     * Gets type for FQN from test project.
     *
     * @param fqn class's FQN.
     *
     * @return type or <code>null</code> if type cannot be found.
     */
    private BinClass getClass(String fqn) {
      return (BinClass)
          (project.getTypeRefForName(fqn)).getBinCIType();
    }

    /**
     * Gets DIT metric for a class from test project.
     *
     * @param fqn class's FQN.
     *
     * @return DIT metric.
     */
    private int getNocForClass(String fqn) {
      final BinClass clazz = getClass(fqn);
      if (clazz == null) {
        throw new IllegalArgumentException("Class " + fqn + " not found");
      }

      return NocMetric.calculate(clazz);
    }
  }
}
