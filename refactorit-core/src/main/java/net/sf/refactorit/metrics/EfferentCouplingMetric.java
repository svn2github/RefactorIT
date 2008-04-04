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
 * Ce : Efferent Couplings : The number of classes inside this category that
 * depend upon classes outside this categories.
 */
public class EfferentCouplingMetric {
  /** Hidden constructor. */
  private EfferentCouplingMetric() {}

  /**
   * Calculates Efferent Coupling metric for a package.
   *
   * @param pkg package.
   *
   * @return Efferent Coupling metric for the package.
   */
  public static int calculate(BinPackage pkg) {
    final Set dependencies = DependenciesIndexer.getDependants(pkg);
    return dependencies.size();
  }

  /** Test driver for {@link EfferentCouplingMetric}. */
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
      suite.setName("Ce metric tests");
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
     * Tests Ce for default package.
     */
    public void testDefault() {
      cat.info("Testing Ce for default package");
      assertDependencies(
          "",
          Arrays.asList(
          new String[] {
          "A",
          "C",
          "D",
          "E"
/*          "java.lang.Object",
          "a.Test2",
          "a.Hello",
          "java.lang.System",
          "java.io.PrintStream",
          "java.lang.String",
          "a.Test5",
          "a.Test6",
          "a.Test7",
          "a.Test8",
          "a.Hello$HelloInner",
          "a.Test9",
          "a.Test9$Test10"*/
      }));
      cat.info("SUCCESS");
    }

    /**
     * Tests Ce for package <code>a</code>.
     */
    public void testA() {
      cat.info("Testing Ce for package a");
      assertDependencies(
          "a",
          Arrays.asList(
          new String[] {
          "a.Test",
          "a.Test2",
          "a.Test3",
          "a.Test4",
          "a.Test6"
//          "java.lang.Object",
//          "A",
//          "Base",
//          "B",
//          "java.lang.String"
      }));
      cat.info("SUCCESS");
    }

    /**
     * Tests Ce for package <code>b</code>.
     */
    public void testB() {
      cat.info("Testing Ce for package b");
      assertDependencies(
          "b",
          Arrays.asList(new String[0]/* {"java.lang.Object"}*/));
      cat.info("SUCCESS");
    }

    /**
     * Tests Ce for package <code>c</code>.
     */
    public void testC() {
      cat.info("Testing Ce for package c");
      assertDependencies("c", Collections.EMPTY_LIST);
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
     * Asserts that only expected dependencies are reported for a package.
     *
     * @param packageName name of the package to check dependencies of.
     * @param expectedDependenciesFqns expected dependencies
     *        (FQN's of types <code>String</code> instances).
     */
    private void assertDependencies(
        String packageName,
        Collection expectedDependenciesFqns) {
      final BinPackage pkg = getPackage(packageName);
      final Set expectedDependencies = new HashSet();
      for (final Iterator i = expectedDependenciesFqns.iterator();
          i.hasNext();
          ) {
        final String expectedDependencyFqn = (String) i.next();
        final BinTypeRef expectedDependencyRef =
            project.getTypeRefForName(expectedDependencyFqn);
        if (expectedDependencyRef == null) {
          throw new IllegalArgumentException(
              "Expected type " + expectedDependencyFqn
              + " not found in project");
        }

        final BinCIType expectedDependency = expectedDependencyRef.getBinCIType();
        expectedDependencies.add(expectedDependency);
      }

      final Set dependencies = DependenciesIndexer.getDependants(pkg);
      final Set missingDependencies = new HashSet(expectedDependencies);
      missingDependencies.removeAll(dependencies);
      final Set extraDependencies = new HashSet(dependencies);
      extraDependencies.removeAll(expectedDependencies);
      if ((missingDependencies.size() != 0)
          || (extraDependencies.size() != 0)) {
        final StringBuffer message = new StringBuffer();
        if (missingDependencies.size() > 0) {
          message.append("Missing: ").append(missingDependencies);
        }
        if (extraDependencies.size() > 0) {
          if (message.length() > 0) {
            message.append(", ");
          }
          message.append("Extra: ").append(extraDependencies);
        }

        fail(
            "Invalid dependencies for package \""
            + packageName
            + "\": "
            + message);
      }

      assertEquals(
          "Ce for package \"" + packageName + "\"",
          expectedDependencies.size(),
          EfferentCouplingMetric.calculate(pkg));
    }
  }
}
