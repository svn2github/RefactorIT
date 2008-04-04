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
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.query.dependency.DependenciesIndexer;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.ManagingIndexer;
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
public class DipMetric {
  /** Hidden constructor. */
  private DipMetric() {
  }

  public static double calculate(BinCIType type) {
    double nrAbstract = 0;
    final Set dependencies = getDependencies(type);

    for (Iterator i = dependencies.iterator(); i.hasNext(); ) {
      final BinCIType typeDependedOn = ((BinCIType) i.next());

      if (typeDependedOn.isAbstract() || typeDependedOn.isInterface()) {
        nrAbstract++;
      }
    }

    if (dependencies.size() == 0) {
      return Double.NaN;
    }

    return (((double) (Math.round(1000 * (
        nrAbstract / dependencies.size())))) / 1000);
  }

  static Set getDependencies(BinCIType type) {
    final ManagingIndexer supervisor = new ManagingIndexer();
    new DependenciesIndexer(supervisor, type);

    Project project = type.getProject();
    List sourceList = project.getCompilationUnits();

    supervisor.visit(type);

    // All dependencies
    final List invocations = supervisor.getInvocations();

    // Types depended upon
    final Set dependencies = new HashSet();

    for (Iterator i = invocations.iterator(); i.hasNext(); ) {
      final InvocationData invocation = (InvocationData) i.next();
      final BinMember member = (BinMember) invocation.getWhat();

      if (member == null) {
        continue;
      }

      // Type this member belongs to
      final BinCIType memberType;

      if (member instanceof BinCIType) {
        // Type invoked
        memberType = (BinCIType) member;
      } else if (member instanceof BinArrayType) {
        // Invocation of array type
        final BinTypeRef typeRef =
            ((BinArrayType) member).getArrayType();

        if (typeRef.isPrimitiveType()) {
          // Array with primitive type as element
          continue; // Skip it
        }

        memberType = (BinCIType) typeRef.getBinType();
      } else {
        BinTypeRef owner = member.getOwner();
        if(owner == null) {
          // this cannot happen! But REF-2386 has own's will ;( 
          // need to log some more information, if this happens:
          String message = "Unexcepted error occured! The [" + member.toString() 
          + "]'s owner was null. The current type is [" + type.toString() + "]";
          throw new RuntimeException(message);
        }
        memberType = owner.getBinCIType();
      }

      if (sourceList.contains(memberType.getCompilationUnit())) {
        dependencies.add(memberType);
      }
    }

    return dependencies;
  }

  /** Test driver for {@link DipMetric}. */
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
      suite.setName("DIP metric tests");

      return suite;
    }

    protected void setUp() throws Exception {
      project =
          Utils.createTestRbProject(Utils.getTestProjectsDirectory()
          .getAbsolutePath() +
          "/Metrics/DIP");
      project.getProjectLoader().build();
    }

    protected void tearDown() {
      project = null;
    }

    /**
     * Tests Dip for class Test1.
     */
    public void testTest1() {
      cat.info("Testing Dip for class Test1");
      assertDip("Test1", ((double) Math.round(1000 * (2d / 3))) / 1000);
      cat.info("SUCCESS");
    }

    /**
     * Tests Dip for class Test2.
     */
    public void testTest2() {
      cat.info("Testing Dip for class Test2");
      assertDip("Test2", ((double) Math.round(1000 * (1d / 1))) / 1000);
      cat.info("SUCCESS");
    }

    /**
     * Tests Dip for class Test3
     */
    public void testTest3() {
      cat.info("Testing Dip for class Test3");
      assertDip("Test3", ((double) Math.round(1000 * (0d / 1))) / 1000);
      cat.info("SUCCESS");
    }

    /**
     * Tests Dip for class Test4
     */
    public void testTest4() {
      cat.info("Testing Dip for class Test4");
      assertDip("Test4", ((double) Math.round(1000 * (1d / 1))) / 1000);
      cat.info("SUCCESS");
    }

    private BinCIType getClass(String fqn) {
      return (
          project.getTypeRefForName(fqn)
          ).getBinCIType();
    }

    private void assertDip(String className, double expectedDip) {
      final BinCIType type = getClass(className);
      double dip = DipMetric.calculate(type);

      assertEquals("Dip for package \"" + className + "\"", expectedDip,
          dip, 0.001);
    }
  }
}
