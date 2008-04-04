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
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.source.SourceParsingException;
import net.sf.refactorit.test.Utils;

import org.apache.log4j.Category;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Depth In Tree (DIT) metric. Gives length from a class to the root of the

 * tree.

 */

public class DitMetric {

  /** Hidden constructor. */

  private DitMetric() {}

  /**
   * Calculates DIT for the class.

   *

   * @param clazz class.

   *

   * @return DIR metric for the class.

   */

  public static int calculate(BinClass clazz) {

    int dit = 0;

    final Set visitedSuperclasses = new HashSet();

    BinTypeRef superclass = clazz.getTypeRef().getSuperclass();

    while (superclass != null) {

      if (!visitedSuperclasses.add(superclass)) {

        // class has already been visited

        // Cyclic inheritance -- cannot calculate metric

        return -1;

      }

      dit++;

      superclass = superclass.getSuperclass();

    }

    return dit;

  }

  /** Test driver for {@link DitMetric}. */

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

      suite.setName("DIT metric tests");

      return suite;

    }

    protected void setUp() throws Exception {

      project =

          Utils.createTestRbProject(Utils.getTestProjects().getProject("DIT"));

      try {

        project.getProjectLoader().build();

      } catch (SourceParsingException e) {

        // expected because of cyclic inheritance

      }

    }

    protected void tearDown() {

      project = null;

    }

    /**
     * Tests DIT for class <code>java.lang.Object</code>.

     */

    public void testObject() {

      cat.info("Testing DIT for class java.lang.Object");

      assertEquals("DIT", 0, getDitForClass("java.lang.Object"));

      cat.info("SUCCESS");

    }

    /**
     * Tests DIT for class A.

     */

    public void testA() {

      cat.info("Testing DIT for class A");

      assertEquals("DIT", 1, getDitForClass("A"));

      cat.info("SUCCESS");

    }

    /**
     * Tests DIT for class B.

     */

    public void testB() {

      cat.info("Testing DIT for class B");

      assertEquals("DIT", 2, getDitForClass("B"));

      cat.info("SUCCESS");

    }

    /**
     * Tests DIT for class C.

     */

    public void testC() {

      cat.info("Testing DIT for class C");

      assertEquals("DIT", 3, getDitForClass("C"));

      cat.info("SUCCESS");

    }

    /**
     * Tests DIT for classes with cyclic inheritance.

     */

    public void testCyclic() {

      cat.info("Testing DIT for classes with cyclic inheritance");

      assertEquals("DIT for D", -1, getDitForClass("D"));

      assertEquals("DIT for E", -1, getDitForClass("E"));

      assertEquals("DIT for F", -1, getDitForClass("F"));

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

    private int getDitForClass(String fqn) {

      final BinClass clazz = getClass(fqn);

      if (clazz == null) {

        throw new IllegalArgumentException("Class " + fqn + " not found");

      }

      return DitMetric.calculate(clazz);

    }

  }

}
