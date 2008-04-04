/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.metrics;



import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.test.Utils;

import org.apache.log4j.Category;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Distance from the Main Sequence metric.

 * <ul>

 *   <li><code>I</code> - Instability.

 *   </li>

 *   <li><code>A</code> - Abstractness.

 *   </li>

 *   <li><code>D</code> - Distance.</li>

 * </ul>

 * <p>

 * The perpendicular distance of a package from the idealized line A + I = 1.

 * This metric is an indicator of the package's balance between abstractness

 * and stability.

 * <p>

 * A package squarely on the main sequence is optimally balanced with respect

 * to its abstractness and stability. Ideal packages are either completely

 * abstract and stable (x=0, y=1) or completely concrete and instable (x=1,

 * y=0).

 * <p>

 * <code>D = A + I - 1</code>

 * <p>

 * The range for this metric is 0 to 1, with D=0 indicating a package that is

 * coincident with the main sequence and D=1 indicating a package that is as

 * far from the main sequence as possible.

 * </p>

 */

public class DistanceMetric {

  /** Hidden constructor. */

  private DistanceMetric() {

  }

  /**
   * Calculates Distance metric for a package.

   *

   * @param pkg package.

   *

   * @return Distance metric for the package.

   */

  public static double calculate(BinPackage pkg) {

    final double instability = InstabilityMetric.calculate(pkg);

    if (instability == Double.NaN) {

      return 0; // shortcut

    }

    final double abstractness = AbstractnessMetric.calculate(pkg);

    if (abstractness == Double.NaN) {

      return 0; // shortcut

    }

    return calculate(instability, abstractness);

  }

  /**
   * Calculates Distance metric for a package.

   *

   * @param instability Instability metric for the package.

   * @param abstractness Abstractness metric for the package.

   *

   * @return Distance metric for the package.

   */

  public static double calculate(double instability, double abstractness) {

    if (instability == Double.NaN || abstractness == Double.NaN) {

      return 0; // shortcut

    }

    return Math.abs(instability + abstractness - 1);

  }

  /** Test driver for {@link DistanceMetric}. */

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

      suite.setName("Distance metric tests");

      return suite;

    }

    protected void setUp() throws Exception {

      project =

          Utils.createTestRbProject(

          Utils.getTestProjects().getProject("Stability Metrics"));

      project.getProjectLoader().build();

    }

    protected void tearDown() {

      project = null;

    }

    /**
     * Tests I for default package.

     */

    public void testDefault() {

      cat.info("Testing I for default package");

      assertDistance("", Math.abs(4d / (4 + 4)

          + AbstractnessMetric.calculate(getPackage("")) - 1d));

      cat.info("SUCCESS");

    }

    /**
     * Tests I for package <code>a</code>.

     */

    public void testA() {

      cat.info("Testing I for package a");

      assertDistance("a", Math.abs(5d / (4 + 5)

          + AbstractnessMetric.calculate(getPackage("a")) - 1d));

      cat.info("SUCCESS");

    }

    /**
     * Tests I for package <code>b</code>.

     */

    public void testB() {

      cat.info("Testing I for package b");

      assertDistance("b", Math.abs(0d

          + AbstractnessMetric.calculate(getPackage("b")) - 1d));

      cat.info("SUCCESS");

    }

    /**
     * Tests I for package <code>c</code>.

     */

    public void testC() {

      cat.info("Testing I for package c");

      assertDistance("c", Math.abs(0d

          + AbstractnessMetric.calculate(getPackage("c")) - 1d));

      cat.info("SUCCESS");

    }

    /**
     * Gets package from test project.

     *

     * @param name package name.

     *

     * @return package or <code>null</code> if package cannot be found.

     */

    private BinPackage getPackage(String name) {

      return project.getPackageForName(name);

    }

    /**
     * Asserts that Distance metric for the package is as expected.

     *

     * @param packageName name of the package to check.

     * @param expectedDistance expected distance.

     */

    private void assertDistance(String packageName,

        double expectedDistance) {

      final BinPackage pkg = getPackage(packageName);

      final double distance = DistanceMetric.calculate(pkg);

      assertEquals("Distance for package \"" + packageName + "\"",

          expectedDistance,

          distance,

          0.0001);

    }

  }

}
