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
 * Limited Size Principle (LSP) metric. The number of direct subpackages
 * of a package
 *
 * @author Daniel Wilken Damm
 */
public class LspMetric {

  /** Hidden constructor. */
  private LspMetric() {}

  /**
   * Calculates LSP for the package.
   *
   * @param pkg package.
   *
   * @return LSP metric for the package.
   */
  public static int calculate(BinPackage pkg) {
    return pkg.getDirectSubPackages().size();
  }

  /** Test driver for {@link LspMetric}. */
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
      suite.setName("LSP metric tests");
      return suite;
    }

    protected void setUp() throws Exception {
      project = Utils.createTestRbProject(
          Utils.getTestProjectsDirectory().getAbsolutePath() + "/Metrics/LSP");
      project.getProjectLoader().build();
    }

    protected void tearDown() {
      project = null;
    }

    /**
     * Tests LSP for package A
     */
    public void testA() {
      cat.info("Testing LSP for package A");
      assertEquals("LSP", 3, getLspForPkg("a"));
      cat.info("SUCCESS");
    }

    /**
     * Tests LSP for package B
     */
    public void testB() {
      cat.info("Testing LSP for package B");
      assertEquals("LSP", 0, getLspForPkg("a.b"));
      cat.info("SUCCESS");
    }

    /**
     * Tests LSP for package C
     */
    public void testC() {
      cat.info("Testing LSP for package C");
      assertEquals("LSP", 0, getLspForPkg("a.c"));
      cat.info("SUCCESS");
    }

    /**
     * Tests LSP for package D
     */
    public void testD() {
      cat.info("Testing LSP for package D");
      assertEquals("LSP", 1, getLspForPkg("a.d"));
      cat.info("SUCCESS");
    }

    /**
     * Tests LSP for package E
     */
    public void testE() {
      cat.info("Testing LSP for package E");
      assertEquals("LSP", 0, getLspForPkg("c.e"));
      cat.info("SUCCESS");
    }

    /**
     * Tests LSP for package F
     */
    public void testF() {
      cat.info("Testing LSP for package F");
      assertEquals("LSP", 0, getLspForPkg("a.d.f"));
      cat.info("SUCCESS");
    }

    /**
     * Gets LSP metric for a package from test project.
     *
     * @param fqn package's Fully Qualified Name.
     *
     * @return LSP metric.
     */
    private int getLspForPkg(String fqn) {
      final BinPackage pkg = project.getPackageForName(fqn);
      assertNotNull("package " + fqn, pkg);
      return calculate(pkg);
    }
  }
}
