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
import net.sf.refactorit.query.dependency.DependenciesIndexer;
import net.sf.refactorit.test.Utils;

import org.apache.log4j.Category;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * http://www.objectmentor.com/resources/articles/oodmetrc.pdf
 * Ca : Afferent Couplings : The number of classes outside this category that
 * depend upon classes within this category.
 */
public class AfferentCouplingMetric {

  /** Hidden constructor. */
  private AfferentCouplingMetric() {}

  /**
   * Calculates Afferent Coupling metric for a package.
   *
   * @param pkg package.
   *
   * @return Afferent Coupling metric for the package.
   */
  public static int calculate(BinPackage pkg) {
    final Set dependants = DependenciesIndexer.getReferencingTypes(pkg);
    return dependants.size();
  }

  /** Test driver for {@link AfferentCouplingMetric}. */
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
      suite.setName("Ca metric tests");
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
     * Tests Ca for default package.
     */
    public void testDefault() {
      cat.info("Testing Ca for default package");
      assertDependants(
          "",
          Arrays.asList(
          new String[] {"a.Test", "a.Test2", "a.Test3", "a.Test4"}));
      cat.info("SUCCESS");
    }

    /**
     * Tests Ca for package <code>a</code>.
     */
    public void testA() {
      cat.info("Testing Ca for package a");
      assertDependants(
          "a",
          Arrays.asList(new String[] {"A", "C", "D", "E"}));
      cat.info("SUCCESS");
    }

    /**
     * Tests Ca for package <code>b</code>.
     */
    public void testB() {
      cat.info("Testing Ca for package b");
      assertDependants("b", Collections.EMPTY_LIST);
      cat.info("SUCCESS");
    }

    /**
     * Tests Ca for package <code>c</code>.
     */
    public void testC() {
      cat.info("Testing Ca for package c");
      assertDependants("c", Collections.EMPTY_LIST);
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
     * Asserts that only expected dependants are reported for a package.
     *
     * @param packageName name of the package to check dependants of.
     * @param expectedDependantsFqns expected dependants
     *        (FQN's of types <code>String</code> instances).
     */
    private void assertDependants(String packageName,
        Collection expectedDependantsFqns) {
      final BinPackage pkg = getPackage(packageName);
      final Set expectedDependants = new HashSet();
      for (final Iterator i = expectedDependantsFqns.iterator();
          i.hasNext(); ) {
        final String expectedDependantFqn = (String) i.next();
        final BinTypeRef expectedDependantRef =
            project.getTypeRefForName(expectedDependantFqn);
        if (expectedDependantRef == null) {
          throw new IllegalArgumentException("Expected type "
              + expectedDependantFqn + " not found in project");
        }
        if (expectedDependantRef.isPrimitiveType()) {
          continue;
        }

        final BinCIType expectedDependant = expectedDependantRef.getBinCIType();
        expectedDependants.add(expectedDependant);
      }

      final Set dependants = DependenciesIndexer.getReferencingTypes(pkg);
      final Set missingDependants = new HashSet(expectedDependants);
      missingDependants.removeAll(dependants);
      final Set extraDependants = new HashSet(dependants);
      extraDependants.removeAll(expectedDependants);
      if ((missingDependants.size() != 0)
          || (extraDependants.size() != 0)) {
        final StringBuffer message = new StringBuffer();
        if (missingDependants.size() > 0) {
          message.append("Missing: ").append(missingDependants);
        }
        if (extraDependants.size() > 0) {
          if (message.length() > 0) {
            message.append(", ");
          }
          message.append("Extra: ").append(extraDependants);
        }

        fail("Invalid dependants for package \"" + packageName
            + "\": " + message);
      }

      assertEquals("Ca for package \"" + packageName + "\"",
          expectedDependants.size(),
          AfferentCouplingMetric.calculate(pkg));
    }
  }
}
