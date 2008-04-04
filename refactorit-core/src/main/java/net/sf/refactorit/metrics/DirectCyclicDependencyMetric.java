/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.metrics;


import net.sf.refactorit.classmodel.BinArrayType;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.query.dependency.DependenciesIndexer;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.ManagingIndexer;
import net.sf.refactorit.query.usage.PackageIndexer;
import net.sf.refactorit.test.Utils;

import org.apache.log4j.Category;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Direct Cyclic Dependencies (CYC) metric.
 *
 * For package <code>DCYC</code> is the number of other packages which use the
 * package and are at the same time used by the package.
 *
 * For type (not implemented) it is the number of other types that use the type
 * and are in use by the type.
 *
 * For method (not implemented) it is the number of other methods that use the
 * method and are in use by the method.
 *
 * @author Unknown
 * @author Daniel Wilken Damm (renaming to DCYC and refining tests)
 */
public class DirectCyclicDependencyMetric {

  /** Hidden constructor. */
  private DirectCyclicDependencyMetric() {}

  /**
   * Gets all methods which are used by the method and at the same time depend
   * on the method (excluding the method itself).
   *
   * @param method method.
   *
   * @return methods
   * ({@link net.sf.refactorit.classmodel.BinMethod} instances).
   * Never returns <code>null</code>.
   */
  /*
     public static Set getCyclicallyDependingMethods(BinMethod method) {
    final ManagingIndexer supervisor = new ManagingIndexer();
    final MethodIndexer indexer =
      new MethodIndexer(supervisor,
                        method,
                        false, // don't include invocations via subclasses
                        false // don't include invocations via superclasses
                        );
     }
   **/

  /**
   * Gets all methods which are used by the package and at the same time depend
   * on the package (excluding the package itself).
   *
   * @param pkg package.
   *
   * @return packages ({@link BinPackage} instances). Never returns
   *         <code>null</code>.
   */
  public static Set getCyclicallyDependingPackages(BinPackage pkg) {
    final Set typesDependedUpon = DependenciesIndexer.getDependencies(pkg);
    if (typesDependedUpon.size() == 0) {
      return Collections.EMPTY_SET;
    }

    final ManagingIndexer supervisor = new ManagingIndexer();
    new PackageIndexer(supervisor,
        pkg,
        false, // don't include invocations via subclasses
        false // don't include invocations via superclasses
        );

    // Iterate over all external types package depends upon and find those
    // which depend on the package.
    final Set packagesDependedUpon = new HashSet();
    for (final Iterator i = typesDependedUpon.iterator(); i.hasNext(); ) {
      final BinCIType typeDependedUpon = (BinCIType) i.next();
      packagesDependedUpon.add(typeDependedUpon.getPackage());
    }

    for (final Iterator i = packagesDependedUpon.iterator(); i.hasNext(); ) {
      final BinPackage packageDependedUpon = (BinPackage) i.next();
      //System.err.println("before getAllTypes: " + packagesDependedUpon);
      for (final Iterator j = packageDependedUpon.getAllTypes(); j.hasNext(); ) {
        final BinCIType type = ((BinTypeRef) j.next()).getBinCIType();
        //System.err.println("got: " + type);
        supervisor.visit(type);
      }
      //System.err.println("after getAllTypes");
    }

    Set cyclicallyDependingPackages = Collections.EMPTY_SET;
    for (final Iterator i = supervisor.getInvocations().iterator();
        i.hasNext(); ) {

      final InvocationData invocation = (InvocationData) i.next();
      Object location = invocation.getWhere();
      if (location == null) {
        continue; // Skip this
      }

      // Type this member belongs to
      final BinCIType type;
      if (location instanceof BinTypeRef) {
        location = ((BinTypeRef) location).getBinCIType();
      }

      if (location instanceof BinCIType) {
        // Type invoked
        type = (BinCIType) location;
      } else if (location instanceof BinArrayType) {
        // Invocation of array type
        final BinTypeRef typeRef = ((BinArrayType) location).getArrayType();
        if (typeRef.isPrimitiveType()) {
          // Array with primitive type as element
          continue; // Skip it
        }
        type = typeRef.getBinCIType();
      } else if (location instanceof BinMember) {
        type = ((BinMember) location).getOwner().getBinCIType();
      } else {
        // FIXME: What are we skipping here?
        continue; // Skip
      }

      if (cyclicallyDependingPackages == Collections.EMPTY_SET) {
        cyclicallyDependingPackages = new HashSet();
      }
      cyclicallyDependingPackages.add(type.getPackage());
    }

    return cyclicallyDependingPackages;
  }

  //  /**
   //   * Gets list of all types inside the package that types outside of the package
   //   * depend on.
   //   *
   //   * @param pkg package.
   //   *
   //   * @return types ({@link BinCIType} instances).
   //   *         Never returns <code>null</code>.
   //   */
  //  private static Set getReferencedTypes(BinPackage pkg) {
  //    // Gather all dependants of this package
  //    final ManagingIndexer supervisor = new ManagingIndexer();
  //    new PackageIndexer(supervisor,
  //        pkg,
  //        false, // don't include invocations via subclasses
  //        false // don't include invocations via superclasses
  //    );
  //
  //    // All dependants
  //    supervisor.visit(pkg.getProject());
  //    final List invocations = supervisor.getInvocations();
  //
  //    // Types depended upon
  //    final Set dependants = new HashSet();
  //    for (final Iterator i = invocations.iterator(); i.hasNext();) {
  //      final InvocationData invocation = (InvocationData) i.next();
  //      Object location = invocation.getWhere();
  //      if (location == null) {
  //        continue; // Skip this
  //      }
  //
  //      // Type this member belongs to
  //      final BinCIType type;
  //      if (location instanceof BinTypeRef) {
  //        location = ((BinTypeRef) location).getBinCIType();
  //      }
  //
  //      if (location instanceof BinCIType) {
  //        // Type invoked
  //        type = (BinCIType) location;
  //      } else if (location instanceof BinArrayType) {
  //        // Invocation of array type
  //        final BinTypeRef typeRef = ((BinArrayType) location).getArrayType();
  //        if (typeRef.isPrimitiveType()) {
  //          // Array with primitive type as element
  //          continue; // Skip it
  //        }
  //        type = typeRef.getBinCIType();
  //      } else if (location instanceof BinMember) {
  //        type = ((BinMember) location).getOwner().getBinCIType();
  //      } else {
  //        // FIXME: What are we skipping here?
  //        continue; // Skip
  //      }
  //
  //      if (pkg.isIdentical(type.getPackage())) {
  //        // Don't add dependant from this package
  //        continue; // Skip
  //      }
  //
  //      // Add type and its owners if any
  //      BinTypeRef currentTypeRef = type.getTypeRef();
  //      do {
  //        final BinCIType currentType = currentTypeRef.getBinCIType();
  //        dependants.add(currentType);
  //        currentTypeRef = currentType.getOwner();
  //      } while (currentTypeRef != null);
  //    }
  //
  //    return dependants;
  //  }

  /**
   * Calculates Direct Cyclic Dependencies metric for a package.
   *
   * @param pkg package.
   *
   * @return Cyclic Dependencies metric for the package.
   */
  public static int calculate(BinPackage pkg) {
    final Set cyclicallyDependingPackages = getCyclicallyDependingPackages(pkg);
    return cyclicallyDependingPackages.size();
  }

  /** Test driver for {@link DirectCyclicDependencyMetric}. */
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
      suite.setName("DCYC metric tests");
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
     * Tests DCYC for package a.
     */
    public void testPackageA() {
      cat.info("Testing DCYC for package a");
      assertCyclicDependenciesForPackage(
          "a",
          new String[] {"b"});
      cat.info("SUCCESS");
    }

    /**
     * Tests DCYC for package b.
     */
    public void testPackageB() {
      cat.info("Testing DCYC for package b");
      assertCyclicDependenciesForPackage(
          "b",
          new String[] {"a", "c"});
      cat.info("SUCCESS");
    }

    /**
     * Tests DCYC for package c.
     */
    public void testPackageC() {
      cat.info("Testing DCYC for package c");
      assertCyclicDependenciesForPackage(
          "c",
          new String[] {"b"});
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
     * Asserts that only expected direct cyclically depending packages are
     * reported for a package.
     *
     * @param packageName name of the package to check dependants of.
     * @param expectedNames names of expected packages.
     */
    private void assertCyclicDependenciesForPackage(
        String packageName,
        String[] expectedNames) {

      final BinPackage pkg = getPackage(packageName);
      final Set expected = new HashSet();
      for (int i = 0, len = expectedNames.length; i < len; i++) {
        final String expectedName = expectedNames[i];
        final BinPackage expectedPackage = getPackage(expectedName);
        if (expectedPackage == null) {
          throw new IllegalArgumentException("Cannot find package \""
              + expectedName + "\"");
        }

        expected.add(expectedPackage);
      }

      final Set actual = getCyclicallyDependingPackages(pkg);
      final Set missing = new HashSet(expected);
      missing.removeAll(actual);
      final Set extra = new HashSet(actual);
      extra.removeAll(expected);
      if ((missing.size() != 0) || (extra.size() != 0)) {
        final StringBuffer message = new StringBuffer();
        if (missing.size() > 0) {
          message.append("Missing: ").append(missing);
        }
        if (extra.size() > 0) {
          if (message.length() > 0) {
            message.append(", ");
          }
          message.append("Extra: ").append(extra);
        }

        fail("Invalid direct cyclic dependencies for package \"" + packageName
            + "\": " + message);
      }

      assertEquals("DCYC for package \"" + packageName + "\"",
          expected.size(),
          DirectCyclicDependencyMetric.calculate(pkg));
    }
  }
}
