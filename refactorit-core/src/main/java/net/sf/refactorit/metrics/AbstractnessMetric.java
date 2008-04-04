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
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.test.Utils;

import org.apache.log4j.Category;

import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Abstractness metric (A). Ratio of abstract classes/interfaces in a package.

 */

public class AbstractnessMetric {

  /** Hidden constructor. */

  private AbstractnessMetric() {}

  /**
   * Calculates Abstractness metric for a package.

   *

   * @param pkg package.

   *

   * @return Abstractness metric for the package or <code>NaN</code> if

   *         metric cannot be calculated.

   */

  public static double calculate(BinPackage pkg) {

    int typeCount = 0;

    int abstractTypeCount = 0;

    for (final Iterator i = pkg.getAllTypes(); i.hasNext(); ) {

      final BinCIType type = ((BinTypeRef) i.next()).getBinCIType();
      ++typeCount;

      if ((type.isInterface()) || (type.isAbstract())) {

        ++abstractTypeCount;

      }

    }

    return ((double) abstractTypeCount) / ((double) typeCount);

  }

  /** Test driver for {@link AbstractnessMetric}. */

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

      suite.setName("Abstractness metric tests");

      return suite;

    }

    protected void setUp() throws Exception {

      project =

          Utils.createTestRbProject(

          Utils.getTestProjects().getProject("Abstractness Metric"));

      project.getProjectLoader().build();

    }

    protected void tearDown() {

      project = null;

    }

    /**
     * Tests A for default package.

     */

    public void testDefault() {

      cat.info("Testing A for default package");

      assertAbstractness("", 3d / 4);

      cat.info("SUCCESS");

    }

    /**
     * Tests A for package <code>a</code>.

     */

    public void testA() {

      cat.info("Testing A for package a");

      assertAbstractness("a", 2d / 4);

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
     * Asserts that Abstractness metric for the package is as expected.

     *

     * @param packageName name of the package to check.

     * @param expectedAbstractness expected abstractness.

     */

    private void assertAbstractness(String packageName,

        double expectedAbstractness) {

      final BinPackage pkg = getPackage(packageName);

      final double abstractness = AbstractnessMetric.calculate(pkg);

      assertEquals("Abstractness for package \"" + packageName + "\"",

          expectedAbstractness,

          abstractness,

          0.001);

    }

  }

}
