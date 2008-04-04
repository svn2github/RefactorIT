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
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.query.AbstractIndexer;
import net.sf.refactorit.test.Utils;

import org.apache.log4j.Category;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Number of Fields (NOF) metric. The number of fields in a method.
 *
 * @author Daniel Wilken Damm
 */
public class NumberOfAttributesMetric {
  /** Hidden constructor. */
  private NumberOfAttributesMetric() {
  }

  /**
   * Calculates number of fields metric for the method.
   *
   * @param method method.
   *
   * @return Number of Fields metric for the <code>method</code>.
   */
  public static int calculate(BinCIType cls) {
    final List fields = new ArrayList();

    final AbstractIndexer indexer = new AbstractIndexer() {
        public void visit(final BinField field) {
          fields.add(field);
        }
      };

    indexer.visit(cls);

    return fields.size();
  }


  /** Test driver for {@link NumberOfFieldsMetric}. */
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
      suite.setName("NOA metric tests");

      return suite;
    }

    protected void setUp() throws Exception {
      project = Utils.createTestRbProject(
       Utils.getTestProjectsDirectory().getAbsolutePath() + "/Metrics/NOA");
      project.getProjectLoader().build();
    }

    protected void tearDown() {
      project = null;
    }

    public void testClassTest1() {
      cat.info("Testing NOA for class Test1");
      assertEquals("NOA", 2, getNOA("Test1"));
      cat.info("SUCCESS");
    }

    public void testInterfaceTest2() {
      cat.info("Testing NOA for interface Test2");
      assertEquals("NOA", 2, getNOA("Test2"));
      cat.info("SUCCESS");
    }

    public void testClassTest3() {
      cat.info("Testing NOA for class Test3");
      assertEquals("NOA", 0, getNOA("Test3"));
      cat.info("SUCCESS");
    }

    private int getNOA(String classFqn) {
      BinCIType testType = project.getTypeRefForName(classFqn).getBinCIType();

      return calculate(testType);
    }
  }
}
