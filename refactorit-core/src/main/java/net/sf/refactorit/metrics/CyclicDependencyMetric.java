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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Cyclic Dependencies (CYC) metric. The number of times a package is repeated
 * in the dependency graph
 *
 * @author Daniel Wilken Damm
 */
public class CyclicDependencyMetric {

  /** Hidden constructor. */
  private CyclicDependencyMetric() {}

  /**
   * Calculates CYC for the package by expanding the package's dependency map
   * to estimate how many cycles in which the package is involved.
   *
   * @param pkg BinPackage.
   *
   * @return Number of cyclic dependencies for the package.
   */
  public static int calculate(MetricsModel model, BinPackage pkg) {
    Map cycMap = model.getPackageDepsMap(pkg.getProject());

    ArrayList expandList = new ArrayList();
    HashSet pkgsExpanded = new HashSet();

    int cyc = 0; // Number of cyclic dependencies
    int i = 0;

    expandList.add(pkg);

    while (i < expandList.size()) {
      BinPackage pkgDistinct = (BinPackage) expandList.get(i);

      if (!pkgsExpanded.contains(pkgDistinct)) {
        pkgsExpanded.add(pkgDistinct);
        Set pkgDep = (Set) cycMap.get(pkgDistinct); // Get package's dependencies

        if (pkgDep != null) {
          // Here was something about bug 2264
          expandList.addAll(pkgDep); // Expand distinct package
        }
      }

      if (pkg.equals(expandList.get(i))) { // Same package?
        cyc++; // We have a cyclic dependency
      }

      i++;
    }

    return cyc - 1;
  }

  /** Test driver for {@link CyclicDependencyMetric}. */
  public static class TestDriver extends TestCase {

    /** Logger instance. */
    private static final Category cat
        = Category.getInstance(TestDriver.class.getName());

    /** Test project. */
    private Project project;

    public TestDriver(String name) {
      super(name);
    }

    public static Test suite() {
      final TestSuite suite = new TestSuite(TestDriver.class);
      suite.setName("CYC metric tests");
      return suite;
    }

    protected void setUp() throws Exception {
      project = Utils.createTestRbProject(
          Utils.getTestProjectsDirectory().getAbsolutePath() + "/Metrics/CYC");
      project.getProjectLoader().build();
    }

    protected void tearDown() {
      project = null;
    }

    /**
     * Tests correct dependencies for package a
     *
     * A: {B, C} => 1
     * B: {A, C} => 2
     * C: {B}    => 2
     *
     */
    public void testDepA() {

      cat.info("Testing dependencies for package a");

      final BinPackage pkgA = project.getPackageForName("a");
      final BinPackage pkgB = project.getPackageForName("b");
      final BinPackage pkgC = project.getPackageForName("c");

      Map testMap = new MetricsModel(null, null).getPackageDepsMap(project);
      Set pkgADep = (Set) testMap.get(pkgA); // Get package's dependencies

      Set correctDep = new HashSet();
      correctDep.add(pkgB);
      correctDep.add(pkgC);

      assertEquals("Testing dependencies for package a", pkgADep, correctDep);

      cat.info("SUCCESS");
    }

    /**
     * Tests correct dependencies for package b
     *
     * A: {B, C} => 1
     * B: {A, C} => 2
     * C: {B}    => 2
     *
     */
    public void testDepB() {

      cat.info("Testing dependencies for package b");

      final BinPackage pkgA = project.getPackageForName("a");
      final BinPackage pkgB = project.getPackageForName("b");
      final BinPackage pkgC = project.getPackageForName("c");

      Map testMap = new MetricsModel(null, null).getPackageDepsMap(project);
      Set pkgBDep = (Set) testMap.get(pkgB); // Get package's dependencies

      Set correctDep = new HashSet();
      correctDep.add(pkgA);
      correctDep.add(pkgC);

      assertEquals("Testing dependencies for package b", pkgBDep, correctDep);

      cat.info("SUCCESS");
    }

    /**
     * Tests correct dependencies for package a
     *
     * A: {B, C} => 1
     * B: {A, C} => 2
     * C: {B}    => 2
     *
     */
    public void testDepC() {

      cat.info("Testing dependencies for package c");

      final BinPackage pkgB = project.getPackageForName("b");
      final BinPackage pkgC = project.getPackageForName("c");

      Map testMap = new MetricsModel(null, null).getPackageDepsMap(project);
      Set pkgCDep = (Set) testMap.get(pkgC); // Get package's dependencies

      Set correctDep = new HashSet();
      correctDep.add(pkgB);

      assertEquals("Testing dependencies for package c", pkgCDep, correctDep);

      cat.info("SUCCESS");
    }

    /**
     * Tests CYC metric for package a
     *
     * A: {B, C} => {A B C A C B} => 1
     * B: {A, C} => {B A C B C B} => 2
     * C: {B}    => {C B A C B C} => 2
     *
     */
    public void testA() {
      cat.info("Testing CYC for package a");
      assertEquals("CYC", 1, getCycForPkg("a"));
      cat.info("SUCCESS");
    }

    /**
     * Tests CYC metric for package b
     *
     * A: {B, C} => {A B C A C B} => 1
     * B: {A, C} => {B A C B C B} => 2
     * C: {B}    => {C B A C B C} => 2
     *
     */
    public void testB() {
      cat.info("Testing CYC for package b");
      assertEquals("CYC", 2, getCycForPkg("b"));
      cat.info("SUCCESS");
    }

    /**
     * Tests CYC metric for package c
     *
     * A: {B, C} => {A B C A C B} => 1
     * B: {A, C} => {B A C B C B} => 2
     * C: {B}    => {C B A C B C} => 2
     *
     */
    public void testC() {
      cat.info("Testing CYC for package c");
      assertEquals("CYC", 2, getCycForPkg("c"));
      cat.info("SUCCESS");
    }

    /**
     * Gets CYC metric for a package from test project.
     *
     * @param fqn package's Fully Qualified Name.
     *
     * @return CYC metric.
     */
    private int getCycForPkg(String fqn) {
      final BinPackage pkg = project.getPackageForName(fqn);

      if (pkg == null) {
        throw new IllegalArgumentException("Package " + fqn + " not found");
      }

      return calculate(new MetricsModel(null, null), pkg);
    }
  }
}
