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

import java.util.List;


/**
 * "The Modularization Quality (MQ) of an Module Dependency Graph (MDG) that is
 * partitioned into k clusters is the difference between the average
 * inter- and intra-connectivity of the k clusters.
 * The intra-connectivity Ai of cluster i consisting of Ni nodes
 * and Mi intra-edges is the fraction of Mi over the maximum number of
 * intra-edges of i (i.e. Ni^2).
 * The inter-connectivity Eij between two distinct
 * clusters i and j consisting of Ni and Nj nodes, respectively, and with Eij
 * inter-edges is the fraction of Eij over the maximum number of inter-edges
 * between i and j (i.e. 2NiNj)."
 * Mancoridis S. (1999) Bunch - A Clustering Tool for the Recovery and
 * Maintenance of Software System Structures
 *
 * @author Anton Safonov
 */
public class ModularizationQualityMetric {

  /** Hidden constructor. */
  private ModularizationQualityMetric() {}

  /**
   * @param pkgs packages.
   * @return ModularizationQuality metric for a set of packages.
   */
  public static double calculate(MetricsModel model, List pkgs) {
    double sumA = 0;
    double sumE = 0;
    int packageNum = pkgs.size();

    for (int i = 0; i < packageNum; i++) {
      BinPackage pkg1 = (BinPackage) pkgs.get(i);
//System.err.println("pkg1: " + pkg1);
      double ai = ConnectivityMetric.calculateIntraConnectivity(model, pkg1);
//System.err.println("ai: " + ai);
      sumA += ai;

      for (int j = i + 1; j < packageNum; j++) {
        BinPackage pkg2 = (BinPackage) pkgs.get(j);

//        if (pkg1 != pkg2) {
          double eij = ConnectivityMetric.calculateInterConnectivity(
              model, pkg1, pkg2);
//System.err.println("eij: " + eij);
          sumE += eij;
//        }
      }
    }

    sumA *= 1000;
    sumE *= 1000;

    if (packageNum <= 1) {
      return sumA;
    }

    return (sumA / ((double) packageNum))
        - (sumE * 4 / ((double) packageNum * (packageNum - 1)));
  }

//  /** Test driver for {@link IntraConnectivityMetric}. */
//  public static class TestDriver extends TestCase {
//    /** Logger instance. */
//    private static final Category cat =
//        Category.getInstance(TestDriver.class.getName());
//
//    /** Test project. */
//    private Project project;
//
//    public TestDriver(String name) {
//      super(name);
//    }
//
//    public static Test suite() {
//      final TestSuite suite = new TestSuite(TestDriver.class);
//      suite.setName("Ai metric tests");
//      return suite;
//    }
//
//    protected void setUp() throws Exception {
//      project =
//          Utils.createTestRbProject(
//          Utils.getTestProjects().getProject("Stability Metrics"));
//      project.getProjectLoader().build();
//    }
//
//    protected void tearDown() {
//      project = null;
//    }
//
//    /**
//     * Tests Ca for default package.
//     */
//    public void testDefault() {
//      cat.info("Testing Ca for default package");
//      assertDependants(
//          "",
//          Arrays.asList(
//          new String[] {"a.Test", "a.Test2", "a.Test3", "a.Test4"}));
//      cat.info("SUCCESS");
//    }
//
//    /**
//     * Tests Ca for package <code>a</code>.
//     */
//    public void testA() {
//      cat.info("Testing Ca for package a");
//      assertDependants(
//          "a",
//          Arrays.asList(new String[] {"A", "C", "D", "E"}));
//      cat.info("SUCCESS");
//    }
//
//    /**
//     * Tests Ca for package <code>b</code>.
//     */
//    public void testB() {
//      cat.info("Testing Ca for package b");
//      assertDependants("b", Collections.EMPTY_LIST);
//      cat.info("SUCCESS");
//    }
//
//    /**
//     * Tests Ca for package <code>c</code>.
//     */
//    public void testC() {
//      cat.info("Testing Ca for package c");
//      assertDependants("c", Collections.EMPTY_LIST);
//      cat.info("SUCCESS");
//    }
//
//    /**
//     * Gets package from test project.
//     *
//     * @param name package name.
//     *
//     * @return package or <code>null</code> if package cannot be found.
//     */
//    private BinPackage getPackage(String name) {
//      return project.getPackageForName(name);
//    }
//
//    /**
//     * Asserts that only expected dependants are reported for a package.
//     *
//     * @param packageName name of the package to check dependants of.
//     * @param expectedDependantsFqns expected dependants
//     *        (FQN's of types <code>String</code> instances).
//     */
//    private void assertDependants(String packageName,
//        Collection expectedDependantsFqns) {
//      final BinPackage pkg = getPackage(packageName);
//      final Set expectedDependants = new HashSet();
//      for (final Iterator i = expectedDependantsFqns.iterator();
//          i.hasNext(); ) {
//        final String expectedDependantFqn = (String) i.next();
//        final BinTypeRef expectedDependantRef =
//            project.getTypeRefForName(expectedDependantFqn);
//        if (expectedDependantRef == null) {
//          throw new IllegalArgumentException("Expected type "
//              + expectedDependantFqn + " not found in project");
//        }
//        if (expectedDependantRef.isPrimitiveType()) {
//          continue;
//        }
//
//        final BinCIType expectedDependant = expectedDependantRef.getBinCIType();
//        expectedDependants.add(expectedDependant);
//      }
//
//      final Set dependants = DependenciesIndexer.getReferencedTypes(pkg);
//      final Set missingDependants = new HashSet(expectedDependants);
//      missingDependants.removeAll(dependants);
//      final Set extraDependants = new HashSet(dependants);
//      extraDependants.removeAll(expectedDependants);
//      if ((missingDependants.size() != 0)
//          || (extraDependants.size() != 0)) {
//        final StringBuffer message = new StringBuffer();
//        if (missingDependants.size() > 0) {
//          message.append("Missing: ").append(missingDependants);
//        }
//        if (extraDependants.size() > 0) {
//          if (message.length() > 0) {
//            message.append(", ");
//          }
//          message.append("Extra: ").append(extraDependants);
//        }
//
//        fail("Invalid dependants for package \"" + packageName
//            + "\": " + message);
//      }
//
//      assertEquals("Ca for package \"" + packageName + "\"",
//          expectedDependants.size(),
//          AfferentCouplingMetric.calculate(pkg));
//    }
//  }
}
