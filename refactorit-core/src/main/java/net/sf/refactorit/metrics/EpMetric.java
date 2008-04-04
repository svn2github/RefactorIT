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
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.query.usage.Finder;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.filters.BinPackageSearchFilter;
import net.sf.refactorit.query.usage.filters.SearchFilter;
import net.sf.refactorit.test.Utils;

import org.apache.log4j.Category;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 *
 * @author  Ulli
 */
public class EpMetric {
  /** Hidden constructor. */
  private EpMetric() {
  }

  static Set getUsedClasses(BinPackage pkg) {
    SearchFilter filter =
        new BinPackageSearchFilter(true, false, false, false, false, false,
        false, false, false, false);
    Project pr = pkg.getProject();
    List invocations = Finder.getInvocations(pr, pkg, filter);

    final Set usedClasses = new HashSet();

    for (Iterator i = invocations.iterator(); i.hasNext(); ) {
      final InvocationData invocation = (InvocationData) i.next();

      CompilationUnit source = invocation.getCompilationUnit();
      BinPackage wherePkg = source.getPackage();

      BinTypeRef type = invocation.getWhatType();
      if (type == null || type.isPrimitiveType()) {
        continue;
      }

      if (type.getBinType().getOwner() != null) {
        type = type.getBinType().getTopLevelEnclosingType().getTypeRef();
      }

      if (pkg.isIdentical(wherePkg) || !pkg.isIdentical(type.getPackage())) {
        continue;
      }

      usedClasses.add(type);
    }

    return usedClasses;
  }

  static Set getToplevelClasses(BinPackage pkg) {
    final Set topLevels = new HashSet();

    for (Iterator i = pkg.getAllTypes(); i.hasNext(); ) {
      BinCIType type = ((BinTypeRef) i.next()).getBinCIType();

      if (type.getOwner() != null) {
        type = type.getTopLevelEnclosingType();
      }

      topLevels.add(type);
    }

    return topLevels;
  }

  public static double calculate(BinPackage pkg) {
    double nrTypes = EpMetric.getToplevelClasses(pkg).size();

    if (nrTypes == 0) {
      return Double.NaN;
    }

    return ((double) Math.round(1000 * (
        EpMetric.getUsedClasses(pkg).size() / nrTypes))) / 1000;
  }

  /** Test driver for {@link EPMetric}. */
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
      suite.setName("EP metric tests");

      return suite;
    }

    protected void setUp() throws Exception {
      project =
          Utils.createTestRbProject(Utils.getTestProjectsDirectory()
          .getAbsolutePath() +
          "/Metrics/EP");
      project.getProjectLoader().build();
    }

    protected void tearDown() {
      project = null;
    }

    /**
     * Tests EP for default package.
     */
    public void testDefault() {
      cat.info("Testing Ep for default package");
      assertEP("", 0);
      cat.info("SUCCESS");
    }

    /**
     * Tests Ep for package a.
     */
    public void testPackageA() {
      cat.info("Testing Ep for package a");
      assertEP("a", ((double) Math.round(1000 * (1d / 2))) / 1000);
      cat.info("SUCCESS");
    }

    /**
     * Tests Ep for package b.
     */
    public void testPackageB() {
      cat.info("Testing Ep for package b");
      assertEP("b", 0);
      cat.info("SUCCESS");
    }

    /**
     * Tests Ep for package c.
     */
    public void testPackageC() {
      cat.info("Testing Ep for package c");
      assertEP("c", ((double) Math.round(1000 * (2d / 2))) / 1000);
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

    private void assertEP(String packageName, double expectedEp) {
      final BinPackage pkg = getPackage(packageName);
      double ep = EpMetric.calculate(pkg);

      assertEquals("EP for package \"" + packageName + "\"", expectedEp,
          ep, 0.001);
    }
  }
}
