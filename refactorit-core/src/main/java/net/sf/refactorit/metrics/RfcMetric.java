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
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.query.dependency.DependenciesIndexer;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.ManagingIndexer;
import net.sf.refactorit.test.Utils;

import org.apache.log4j.Category;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Response For Class (RFC) metric. Number of distinct methods that are invoked
 * inside a class.
 */
public class RfcMetric {
  /** Hidden constructor. */
  private RfcMetric() {}

  /**
   * Calculates RFC metric for a class.
   *
   * @param clazz class.
   *
   * @return RFC metric for the class.
   */
  public static int calculate(BinClass clazz) {
    final Set dependencies = getDependencies(clazz);
    return dependencies.size();
  }

  /**
   * Gets list of all methods and constructors class depends on.
   *
   * @param clazz class.
   *
   * @return methods ({@link BinMethod}) and constructors
   *         ({@link BinConstructor}) instances.
   *         Never returns <code>null</code>.
   */
  static Set getDependencies(BinClass clazz) {
    // Gather all dependencies of this package
    final ManagingIndexer supervisor = new ManagingIndexer();
    new DependenciesIndexer(supervisor, clazz) {
      protected boolean isItemInsideTarget(BinItem item) {
        return false; // Interested in all dependencies, self ones also
      }
    };

    supervisor.visit(clazz);

    // All dependencies
    final List invocations = supervisor.getInvocations();

    // Types depended upon
    final Set dependencies = new HashSet();
    for (int i = 0, max = invocations.size(); i < max; i++) {
      final InvocationData invocation = (InvocationData) invocations.get(i);
      final BinMember member = (BinMember) invocation.getWhat();
      if (member == null) {
        if (Assert.enabled) {
          Assert.must(false, "Invoked member == null: " + invocation);
        }
        continue; // Skip this
      }

      // Only add dependencies on methods and constructors
      // NOTE: BinConstructor is not a method, at least not ever
      if (member instanceof BinMethod || member instanceof BinConstructor) {
        dependencies.add(member);
      }
    }

    return dependencies;
  }

  /** Test driver for {@link RfcMetric}. */
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
      suite.setName("RFC metric tests");
      return suite;
    }

    protected void setUp() throws Exception {
      project =
          Utils.createTestRbProject(
          Utils.getTestProjects().getProject("RFC Metric"));
      project.getProjectLoader().build();
    }

    protected void tearDown() {
      project = null;
    }

    /**
     * Tests RFC for class Test1.
     */
    public void testTest1() {
      cat.info("Testing RFC for class Test1");
      assertRfc("Test1", 1);
      cat.info("SUCCESS");
    }

    /**
     * Tests RFC for class Test2.
     */
    public void testTest2() {
      cat.info("Testing RFC for class Test2");
      assertRfc("Test2", 3);
      cat.info("SUCCESS");
    }

    /**
     * Tests RFC for class Test3.
     */
    public void testTest3() {
      cat.info("Testing RFC for class Test3");
      assertRfc("Test3", 2);
      cat.info("SUCCESS");
    }

    /**
     * Tests RFC for class Test4.
     */
    public void testTest4() {
      cat.info("Testing RFC for class Test4");
      assertRfc("Test4", 4);
      cat.info("SUCCESS");
    }

    /**
     * Tests RFC for class Test5.
     */
    public void testTest5() {
      cat.info("Testing RFC for class Test5");
      assertRfc("Test5", 2);
      cat.info("SUCCESS");
    }

    /**
     * Tests RFC for class Test6.
     */
    public void testTest6() {
      cat.info("Testing RFC for class Test6");
      assertRfc("Test6", 2);
      cat.info("SUCCESS");
    }

    /**
     * Tests RFC for class Test7.
     */
    public void testTest7() {
      cat.info("Testing RFC for class Test7");
      assertRfc("Test7", 4);
      cat.info("SUCCESS");
    }

    /**
     * Gets class from test project.
     *
     * @param fqn FQN of the class.
     *
     * @return class or <code>null</code> if class cannot be found.
     */
    private BinClass getClass(String fqn) {
      return (BinClass) project.getTypeRefForName(fqn).getBinType();
    }

    /**
     * Asserts that RFC metric for the class is as expected.
     *
     * @param fqn FQN of the class to check.
     * @param expectedRfc expected RFC.
     */
    private void assertRfc(String fqn, int expectedRfc) {
      final BinClass clazz = getClass(fqn);
      if (clazz == null) {
        throw new IllegalArgumentException(
            "Class \"" + fqn + "\" not found" + " in test projet");
      }

      final int rfc = RfcMetric.calculate(clazz);
      if (expectedRfc != rfc) {
        fail("RFC for class \"" + fqn + "\" expected:<" + expectedRfc + ">" +
            " but was:<" + rfc + ">" + System.getProperty("line.separator") +
            "Dependencies: " + getDependencies(clazz));
      }
    }
  }
}
