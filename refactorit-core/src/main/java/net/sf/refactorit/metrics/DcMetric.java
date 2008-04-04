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
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.test.Utils;

import org.apache.log4j.Category;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Density of Comments (DC) metric. <code>DC = CLOC / LOC</code>.
 *
 * <h3>Example</h3>
 * <code><pre>
 *void test() {
 *  // Hi!
 *  System.out.println("Hi!");
 *
 *  // This one throws exception
 *  throw new RuntimeException(
 *      "Hi!");
 *}
 * </pre></code>
 *
 * <code>LOC = 6</code>, <code>CLOC = 2</code>, <code>DC = 0.3(3)</code>.
 */
public class DcMetric {
  /** Hidden constructor. */
  private DcMetric() {}

  public static double calculate(BinPackage pkg) {
    return calculate(LocMetric.calculate(pkg), ClocMetric.calculate(pkg));
  }

  public static double calculate(BinMember member) {
    return calculate(LocMetric.calculate(member), ClocMetric.calculate(member));
  }

  /**
   * Calculates DC for the member.
   *
   * @param member member.
   *
   * @return DC of the <code>member</code>.
   */
  public static double calculate(int loc, int cloc) {
    if (loc == 0) {
      return 0d;
    }

    if ((loc == -1) || (cloc == -1)) {
      return Double.NaN;
    }

    return ((double) cloc) / loc;
  }

  /** Test driver for {@link DcMetric}. */
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
      suite.setName("DC metric tests");
      return suite;
    }

    protected void setUp() throws Exception {
      project =
          Utils.createTestRbProject(Utils.getTestProjects().getProject("LOC"));
      project.getProjectLoader().build();
    }

    protected void tearDown() {
      project = null;
    }

    /**
     * Tests DC for Test constructor.
     */
    public void testTestConstructor() {
      cat.info("Testing DC for Test constructor");
      final BinClass test = (BinClass) getType("Test");
      assertEquals(
          "DC",
          0d,
          DcMetric.calculate(test.getDeclaredConstructors()[0]),
          0.001);
      cat.info("SUCCESS");
    }

    /**
     * Tests DC for Test second constructor.
     */
    public void testTestConstructor2() {
      cat.info("Testing DC for Test second constructor");
      final BinClass test = (BinClass) getType("Test");
      assertEquals(
          "LOC",
          0d,
          DcMetric.calculate(test.getDeclaredConstructors()[1]),
          0.001);
      cat.info("SUCCESS");
    }

    /**
     * Tests DC for method Test.a.
     */
    public void testTestA() {
      cat.info("Testing DC for method Test.a");
      assertEquals("DC", 0d, getDcForMethod("a"), 0.001);
      cat.info("SUCCESS");
    }

    /**
     * Tests DC for method Test.b.
     */
    public void testTestB() {
      cat.info("Testing DC for method Test.b");
      assertEquals(
          "DC",
          Double.doubleToLongBits(0d),
          Double.doubleToLongBits(getDcForMethod("b")));
      cat.info("SUCCESS");
    }

    /**
     * Tests DC for method Test.c.
     */
    public void testTestC() {
      cat.info("Testing DC for method Test.c");
      assertEquals(
          "DC",
          Double.doubleToLongBits(0d),
          Double.doubleToLongBits(getDcForMethod("c")));
      cat.info("SUCCESS");
    }

    /**
     * Tests DC for method Test.d.
     */
    public void testTestD() {
      cat.info("Testing DC for method Test.d");
      assertEquals("DC", 0d, getDcForMethod("d"), 0.001);
      cat.info("SUCCESS");
    }

    /**
     * Tests DC for method Test.e.
     */
    public void testTestE() {
      cat.info("Testing DC for method Test.e");
      assertEquals("DC", 0d, getDcForMethod("e"), 0.001);
      cat.info("SUCCESS");
    }

    /**
     * Tests DC for method Test.f.
     */
    public void testTestF() {
      cat.info("Testing DC for method Test.f");
      assertEquals("DC", 0.5, getDcForMethod("f"), 0.001);
      cat.info("SUCCESS");
    }

    /**
     * Tests DC for method Test.g.
     */
    public void testTestG() {
      cat.info("Testing DC for method Test.g");
      assertEquals("DC", 0.5, getDcForMethod("g"), 0.001);
      cat.info("SUCCESS");
    }

    /**
     * Tests DC for method Test.h.
     */
    public void testTestH() {
      cat.info("Testing DC for method Test.h");
      assertEquals("DC", 0.5, getDcForMethod("h"), 0.001);
      cat.info("SUCCESS");
    }

    /**
     * Tests DC for method Test.i.
     */
    public void testTestI() {
      cat.info("Testing DC for method Test.i");
      assertEquals(
          "DC",
          Double.doubleToLongBits(0d),
          Double.doubleToLongBits(getDcForMethod("i")));
      cat.info("SUCCESS");
    }

    /**
     * Tests DC for method Test.j.
     */
    public void testTestJ() {
      cat.info("Testing DC for method Test.j");
      assertEquals("DC", 0.3, getDcForMethod("j"), 0.001);
      cat.info("SUCCESS");
    }

    /**
     * Gets type for FQN from test project.
     *
     * @param fqn type's FQN.
     *
     * @return type or <code>null</code> if type cannot be found.
     */
    private BinCIType getType(String fqn) {
      return (project.getTypeRefForName(fqn)).getBinCIType();
    }

//    /**
//     * Gets DC metric for type from test project.
//     *
//     * @param fqn type's FQN.
//     *
//     * @return DC metric.
//     */
//    private double getDcForType(String fqn) {
//      final BinCIType type = getType(fqn);
//      if (type == null) {
//        throw new IllegalArgumentException("Type " + fqn + " not found");
//      }
//
//      return DcMetric.calculate(type);
//    }

    /**
     * Gets DC metric for method from Test class of test project.
     *
     * @param name name of the method.
     *
     * @return DC metric.
     */
    private double getDcForMethod(String name) {
      final BinCIType type = getType("Test");
      if (type == null) {
        throw new IllegalArgumentException("Type Test not found");
      }

      final BinMethod method
          = type.getDeclaredMethod(name, BinTypeRef.NO_TYPEREFS);

      return DcMetric.calculate(method);
    }
  }
}
