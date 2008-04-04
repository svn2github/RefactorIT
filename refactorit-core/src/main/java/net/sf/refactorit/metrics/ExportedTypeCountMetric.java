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
import net.sf.refactorit.query.AbstractIndexer;
import net.sf.refactorit.test.Utils;

import org.apache.log4j.Category;

import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Number of Exported Types (NOTe) metric.
 */
public class ExportedTypeCountMetric {

  /** Hidden constructor. */

  private ExportedTypeCountMetric() {
  }

  /**
   * Calculates metric for a package.

   *

   * @param pkg package.

   *

   * @return metric for the package or <code>-1</code> if

   *         metric cannot be calculated.

   */

  public static int calculate(BinPackage pkg) {

    int typeCount = 0;

    for (final Iterator i = pkg.getAllTypes(); i.hasNext(); ) {

      final BinCIType type = ((BinTypeRef) i.next()).getBinCIType();

      // Scan only top-level types

      if (type.isInnerType()) {

        continue;

      }

      final int typeCountForType = calculate(type);

      if (typeCountForType != -1) {

        typeCount += typeCountForType;

      }

    }

    return typeCount;

  }

  /**
   * Calculates metric for a type.

   *

   * @param type type.

   *

   * @return metric for the type or <code>-1</code> if

   *         metric cannot be calculated.

   */

  public static int calculate(BinCIType type) {

    // Top-level type can be exported if it is public

    // Type can be exported if it is public or private and his owner can be

    // exported.

    // Local and anonymous types cannot be exported.

    return new AbstractIndexer(true) {

      private int typeCount = 0;

      public int countTypes(BinCIType typeToCount) {

        visit(typeToCount);

        return typeCount;

      }

      public void visit(BinCIType someType) {

        if ((!someType.isAnonymous()) && (!someType.isLocal())

            && ((someType.isPublic()) || (someType.isProtected()))) {

          typeCount++;

          super.visit(someType);

        } else {

          // Don't descend deeper, since this type and its subtypes

          // cannot be exported

        }

      }

    }


    .countTypes(type);

  }

  /** Test driver for {@link ExportedTypeCountMetric}. */

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

      suite.setName("NOTe metric tests");

      return suite;

    }

    protected void setUp() throws Exception {

      project =

          Utils.createTestRbProject(

          Utils.getTestProjects().getProject("NOT Metric"));

      project.getProjectLoader().build();

    }

    protected void tearDown() {

      project = null;

    }

    /**
     * Tests NOT for default package.

     */

    public void testDefault() {

      cat.info("Testing NOTe for default package");

      assertTypeCount("", 9);

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
     * Asserts that NOTc metric for the package is as expected.

     *

     * @param packageName name of the package to check.

     * @param expectedCount expected type count.

     */

    private void assertTypeCount(String packageName,

        int expectedCount) {

      final BinPackage pkg = getPackage(packageName);

      final int actualCount = ExportedTypeCountMetric.calculate(pkg);

      assertEquals("NOTe for package \"" + packageName + "\"",

          expectedCount,

          actualCount);

    }

  }

}
