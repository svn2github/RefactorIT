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
 * Instability metric (Robert C. Martin).

 * <p>

 * <ul>

 *   <li><code>C<sub>a</sub></code> - Afferent Coupling (incoming dependencies).

 *   </li>

 *   <li><code>C<sub>e</sub></code> - Efferent Coupling (outgoing dependencies).

 *   </li>

 *   <li><code>I</code> - instability.</li>

 * </ul>

 *

 * <code>I = C<sub>e</sub> / (C<sub>a</sub> + C<sub>e</sub>)</code>

 * </p>

 */

public class InstabilityMetric {

  /** Hidden constructor. */

  private InstabilityMetric() {}

  /**
   * Calculates Instability metric for a package.

   *

   * @param pkg package.

   *

   * @return Instability metric for the package.

   */

  public static double calculate(BinPackage pkg) {

    final int efferentCoupling = EfferentCouplingMetric.calculate(pkg);

    if (efferentCoupling == 0) {

      return 0; // shortcut

    }

    final int afferentCoupling = AfferentCouplingMetric.calculate(pkg);

    return calculate(efferentCoupling, afferentCoupling);

  }

  /**
   * Calculates Instability metric for a package.

   *

   * @param efferentCoupling Efferent Coupling metric for the package.

   * @param afferentCoupling Afferent Coupling metric for the package.

   *

   * @return Instability metric for the package.

   */

  public static double calculate(int efferentCoupling, int afferentCoupling) {

    if (efferentCoupling == 0) {

      return 0; // shortcut

    }

    return ((double) (efferentCoupling))

        / (afferentCoupling + efferentCoupling);

  }

  /** Test driver for {@link InstabilityMetric}. */

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

      suite.setName("Instability metric tests");

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

      assertInstability("", 4d / (4 + 4));

      cat.info("SUCCESS");

    }

    /**
     * Tests I for package <code>a</code>.

     */

    public void testA() {

      cat.info("Testing I for package a");

      assertInstability("a", 5d / (4 + 5));

      cat.info("SUCCESS");

    }

    /**
     * Tests I for package <code>b</code>.

     */

    public void testB() {

      cat.info("Testing I for package b");

      assertInstability("b", 0d);

      cat.info("SUCCESS");

    }

    /**
     * Tests I for package <code>c</code>.

     */

    public void testC() {

      cat.info("Testing I for package c");

      assertInstability("c", 0);

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
     * Asserts that Instability metric for the package is as expected.

     *

     * @param packageName name of the package to check.

     * @param expectedInstability expected instability.

     */

    private void assertInstability(String packageName,

        double expectedInstability) {

      final BinPackage pkg = getPackage(packageName);

      final double instability = InstabilityMetric.calculate(pkg);

      assertEquals("Instability for package \"" + packageName + "\"",

          expectedInstability,

          instability,

          0.001);

    }

  }

}
