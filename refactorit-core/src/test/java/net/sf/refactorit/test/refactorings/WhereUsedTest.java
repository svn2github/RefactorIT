/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings;



import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.statements.BinLabeledStatement;
import net.sf.refactorit.classmodel.statements.BinVariableDeclaration;
import net.sf.refactorit.query.AbstractIndexer;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.query.usage.ConstructorIndexer;
import net.sf.refactorit.query.usage.FieldIndexer;
import net.sf.refactorit.query.usage.Finder;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.LabelIndexer;
import net.sf.refactorit.query.usage.LocalVariableIndexer;
import net.sf.refactorit.query.usage.ManagingIndexer;
import net.sf.refactorit.query.usage.MethodIndexer;
import net.sf.refactorit.query.usage.PackageIndexer;
import net.sf.refactorit.query.usage.TypeIndexer;
import net.sf.refactorit.query.usage.filters.BinMethodSearchFilter;
import net.sf.refactorit.query.usage.filters.SimpleFilter;
import net.sf.refactorit.test.Utils;

import org.apache.log4j.Category;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests Where Used refactoring.
 */
public class WhereUsedTest extends TestCase {
  /** Logger instance. */
  private static final Category cat =
      Category.getInstance(WhereUsedTest.class.getName());

  public WhereUsedTest(String name) {
    super(name);
  }

  public static Test suite() throws Exception {
    final TestSuite suite = new TestSuite(WhereUsedTest.class);
    suite.setName("Where Used");

    return suite;
  }

  /**
   * Tests bug #96: Usages in static initializers not found by Where Used.
   */
  public void testBug96() throws Exception {
    cat.info("Testing bug #96");
    final Project project =
        Utils.createTestRbProject(Utils.getTestProjects().getProject("bug #96"));
    project.getProjectLoader().build();

    BinTypeRef testTypeRef = project.getTypeRefForName("Test");

    cat.debug("Finding usages of Test.test static method");
    final BinMethod testMethod = testTypeRef.getBinCIType()
        .getDeclaredMethod("test", BinTypeRef.NO_TYPEREFS);

    ManagingIndexer supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, testMethod,
        new BinMethodSearchFilter(true, true));
    assertUsagesEqual("Usages of Test.test()",
        new int[] {7}
        ,
        supervisor.getInvocationsForProject(project));

    cat.debug("Finding usages of Test.abc static field");
    final BinField abcField
        = project.getTypeRefForName("Test").getBinCIType()
        .getDeclaredField("abc");

    supervisor = new ManagingIndexer();
    new FieldIndexer(supervisor, abcField, true);
    assertUsagesEqual("Usages of Test.abc",
        new int[] {8}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  /**
   * Tests bug 105: Usages in anonymous classes not found by Where Used.
   */
  public void testBug105() throws Exception {
    cat.info("Testing bug #105");
    final Project project =
        Utils.createTestRbProject(Utils.getTestProjects().getProject("bug #105"));
    project.getProjectLoader().build();

    cat.debug("Finding usages of Test.test method");
    BinTypeRef testTypeRef = project.getTypeRefForName("Test");

    final BinMethod testMethod = testTypeRef.getBinCIType()
        .getDeclaredMethod("test", BinTypeRef.NO_TYPEREFS);

    ManagingIndexer supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, testMethod,
        new BinMethodSearchFilter(true, true));
    assertUsagesEqual("Usages of Test.test()",
        new int[] {5, 8}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  /**
   * Tests bug 106: Usage of parent's default constructor not found in
   * constructors
   */
  public void testBug106() throws Exception {
    cat.info("Testing bug #106");
    final Project project =
        Utils.createTestRbProject(Utils.getTestProjects().getProject("bug #106"));
    project.getProjectLoader().build();

    cat.debug("Finding usages of A class constructor");

    BinClass aType = (BinClass) (project.getTypeRefForName("A").getBinType());

    final BinConstructor aConstructor = aType.getDeclaredConstructors()[0];

    ManagingIndexer supervisor = new ManagingIndexer();
    new ConstructorIndexer(supervisor, aConstructor);
    assertUsagesEqual("Usages of A class class constructor",
        new int[] {4, 14, 18, 24}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  /**
   * Tests bug 107: Usage of parent's constructor invoked using super() not
   * found
   */
  public void testBug107() throws Exception {
    cat.info("Testing bug #107");
    final Project project =
        Utils.createTestRbProject(Utils.getTestProjects().getProject("bug #107"));
    project.getProjectLoader().build();

    cat.debug("Finding usages of A class default constructor");

    BinClass aType = (BinClass) (project.getTypeRefForName("A").getBinType());

    final BinConstructor aDefaultConstructor = aType.getDeclaredConstructors()[
        0];

    ManagingIndexer supervisor = new ManagingIndexer();
    new ConstructorIndexer(supervisor, aDefaultConstructor);
    assertUsagesEqual("Usages of A class default constructor",
        new int[] {13}
        ,
        supervisor.getInvocationsForProject(project));

    cat.debug("Finding usages of A class constructor (int a)");

    final BinConstructor aConstructor = aType.getDeclaredConstructors()[1];

    supervisor = new ManagingIndexer();
    new ConstructorIndexer(supervisor, aConstructor);
    assertUsagesEqual("Usages of A class constructor (int a)",
        new int[] {9}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  /**
   * Tests bug 121: Where Used doesn't find usage of type as exception in catch
   */
  public void testBug121() throws Exception {
    cat.info("Testing bug #121");

    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("bug #121"));
    project.getProjectLoader().build();

    cat.debug("Finding usages of class A - sub/super");
    ManagingIndexer supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor,
        project.getTypeRefForName("A").getBinCIType(),
        true, true);
    assertUsagesEqual("Usages of class A - sub/super",
        new int[] {13, 13}
        ,
        supervisor.getInvocationsForProject(project));

    cat.debug("Finding usages of class A - sub");
    supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor,
        project.getTypeRefForName("A").getBinCIType(),
        false, true);
    assertUsagesEqual("Usages of class A - sub",
        new int[] {13, 13}
        ,
        supervisor.getInvocationsForProject(project));

    cat.debug("Finding usages of class A - super");
    supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor,
        project.getTypeRefForName("A").getBinCIType(),
        true, false);
    assertUsagesEqual("Usages of class A - super",
        new int[] {13, 13}
        ,
        supervisor.getInvocationsForProject(project));

    cat.debug("Finding usages of class A");
    supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor,
        project.getTypeRefForName("A").getBinCIType(),
        false, false);
    assertUsagesEqual("Usages of class A",
        new int[] {13, 13}
        ,
        supervisor.getInvocationsForProject(project));

    cat.debug("Finding usages of class B - sub/super");
    supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor,
        project.getTypeRefForName("B").getBinCIType(),
        true, true);
    assertUsagesEqual("Usages of class B - sub/super",
        new int[] {4, 5}
        ,
        supervisor.getInvocationsForProject(project));

    cat.debug("Finding usages of class B");
    supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor,
        project.getTypeRefForName("B").getBinCIType(),
        false, false);
    assertUsagesEqual("Usages of class B",
        new int[] {4, 5}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  /**
   * Tests bug 124: Where Used incorrectly finds Object's methods invoked on
   * interfaces
   */
  public void testBug124() throws Exception {
    cat.info("Testing bug #124");

    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("bug #124/125"));
    project.getProjectLoader().build();

    cat.debug("Finding usages of A.toString method in Test.java");

    BinTypeRef testTypeRef = project.getTypeRefForName("A");

    final BinMethod toStringMethod = testTypeRef.getBinCIType()
        .getDeclaredMethod("toString", BinTypeRef.NO_TYPEREFS);

    ManagingIndexer supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, toStringMethod,
        new BinMethodSearchFilter(true, true));
    supervisor.visit(
        project.getTypeRefForName("Test")
        .getBinCIType().getCompilationUnit());
    assertUsagesEqual("Usages of A.toString in Test.java",
        new int[0],
        supervisor.getInvocations());

    cat.info("SUCCESS");
  }

  /**
   * Tests bug 125: Where Used incorrectly reports usages of super.method for
   * "method"
   */
  public void testBug125() throws Exception {
    cat.info("Testing bug #125");

    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("bug #124/125"));
    project.getProjectLoader().build();

    cat.debug("Finding usages of A.toString method in A.java");
    BinTypeRef testTypeRef = project.getTypeRefForName("A");

    final BinMethod toStringMethod = testTypeRef.getBinCIType()
        .getDeclaredMethod("toString", BinTypeRef.NO_TYPEREFS);

    ManagingIndexer supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, toStringMethod,
        new BinMethodSearchFilter(true, true));
    supervisor.visit(
        project.getTypeRefForName("A")
        .getBinCIType().getCompilationUnit());
    assertUsagesEqual("Usages of A.toString in A.java",
        new int[0],
        supervisor.getInvocations());

    cat.info("SUCCESS");
  }

  public void testBug127() throws Exception {
    cat.info("Testing bug #127");

    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("bug #127"));
    project.getProjectLoader().build();

    cat.debug("Finding usages of Test.test method");

    final BinMethod toStringMethod =
        new BinMethod("toString",
        BinParameter.NO_PARAMS,
        project.getTypeRefForName("java.lang.String"),
        BinModifier.PUBLIC,
        BinMethod.Throws.NO_THROWS
        );
    toStringMethod.setOwner(project.getTypeRefForName("Test"));

    ManagingIndexer supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, toStringMethod,
        new BinMethodSearchFilter(true, true));
    assertUsagesEqual("Usages of Test.toString()",
        new int[] {4, 5, 5, 6, 7, 8, 10, 12}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  /**
   * Tests bug 130: Where Used for subclass doesn't find unoverriden
   * superclass's methods usage
   */
  public void testBug130() throws Exception {
    cat.info("Testing bug #130");

    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("bug #130"));
    project.getProjectLoader().build();

    cat.debug("Finding usages of class B");

    ManagingIndexer supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor,
        project.getTypeRefForName("B").getBinCIType(),
        true, true);
    assertUsagesEqual("Usages of class B",
        new int[] {2, 3}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  /**
   * Tests bug 142: Where Used doesn't treat declarations of variables of
   * a type as usage of the type
   */
  public void testBug142() throws Exception {
    cat.info("Testing bug #142");

    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("bug #142"));
    project.getProjectLoader().build();

    cat.debug("Finding usages of class A");
    ManagingIndexer supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor,
        project.getTypeRefForName("A").getBinCIType(),
        true, true);
    assertUsagesEqual("Usages of class A",
        new int[] {2, 3, 6, 9}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  /**
   * Tests bug 143: extends and implements
   */
  public void testBug143() throws Exception {
    cat.info("Testing bug #143");

    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("bug #143"));
    project.getProjectLoader().build();

    cat.debug("Finding usages of interface A");
    ManagingIndexer supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor,
        project.getTypeRefForName("A").getBinCIType(),
        true, true);
    assertUsagesEqual("Usages of interface A",
        new int[] {1, 5}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  /**
   * Tests bug 151: Where Used incorrectly identifies method as overriden by
   * subclass.
   */
  public void testBug151() throws Exception {
    cat.info("Testing bug #151");

    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("bug #151"));
    project.getProjectLoader().build();

    cat.debug("Finding usages of method A.test(Object)");

    BinTypeRef testTypeRef = project.getTypeRefForName("A");

    final BinMethod method = testTypeRef.getBinCIType()
        .getDeclaredMethod("test", new BinParameter[] {
        new BinParameter("o",
        project.getTypeRefForName("java.lang.Object"),
        0)
    }
        );

    ManagingIndexer supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, method,
        new BinMethodSearchFilter(true, true));
    assertUsagesEqual("Usages of A.test(Object)",
        new int[0],
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  /**
   * Tests bug 156: Where Used doesn't find usages of array fields.
   */
  public void testBug156() throws Exception {
    cat.info("Testing bug #156");

    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("bug #156"));
    project.getProjectLoader().build();

    // String[] tmp
    final BinField field
        = project.getTypeRefForName("Test").getBinCIType()
        .getDeclaredField("tmp");

    ManagingIndexer supervisor = new ManagingIndexer();
    new FieldIndexer(supervisor, field, true);
    assertUsagesEqual("Usages of tmp",
        new int[] {5, 6}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  /**
   * Tests bug: type usage is not found in cast expressions
   */
  public void testTypeCast() throws Exception {
    cat.info("Testing WhereUsed of type cast");

    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("WhereUsed_type_cast"));
    project.getProjectLoader().build();

    cat.debug("Finding usages of class A");
    ManagingIndexer supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor,
        project.getTypeRefForName("A").getBinCIType(),
        true, true);
    assertUsagesEqual("Usages of class A",
        new int[] {3}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  /**
   * Tests functionality of sub/super filters of WhereUsed methods search.
   */
  public void testSubSuper() throws Exception {
    cat.info("Testing WhereUsed sub/super");

    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("WhereUsed_sub_super"));
    project.getProjectLoader().build();

    cat.debug("Finding usages of method A.test()");

    BinTypeRef testTypeRef = project.getTypeRefForName("A");

    BinMethod method = testTypeRef.getBinCIType()
        .getDeclaredMethod("test", BinTypeRef.NO_TYPEREFS);

    ManagingIndexer supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, method,
        new BinMethodSearchFilter(true, true));
    assertUsagesEqual("Usages of A.test() (sub/super)",
        new int[] {8, 3, 17}
        ,
        supervisor.getInvocationsForProject(project));

    supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, method,
        new BinMethodSearchFilter(false, true));
    assertUsagesEqual("Usages of A.test() (sub)",
        new int[] {8, 3, 17}
        ,
        supervisor.getInvocationsForProject(project));

    supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, method,
        new BinMethodSearchFilter(true, false));
    assertUsagesEqual("Usages of A.test() (super)",
        new int[] {3}
        ,
        supervisor.getInvocationsForProject(project));

    supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, method,
        new BinMethodSearchFilter(false, false));
    assertUsagesEqual("Usages of A.test() ()",
        new int[] {3}
        ,
        supervisor.getInvocationsForProject(project));

    cat.debug("Finding usages of method B.test()");
    testTypeRef = project.getTypeRefForName("B");

    method = testTypeRef.getBinCIType()
        .getDeclaredMethod("test", BinTypeRef.NO_TYPEREFS);

    supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, method,
        new BinMethodSearchFilter(true, true));
    assertUsagesEqual("Usages of B.test() (sub/super)",
        new int[] {8, 3, 4, 13}
        ,
        supervisor.getInvocationsForProject(project));

    supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, method,
        new BinMethodSearchFilter(false, true));
    assertUsagesEqual("Usages of B.test() (sub)",
        new int[] {8, 4}
        ,
        supervisor.getInvocationsForProject(project));

    supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, method,
        new BinMethodSearchFilter(true, false));
    assertUsagesEqual("Usages of B.test() (super)",
        new int[] {3, 4, 13}
        ,
        supervisor.getInvocationsForProject(project));

    supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, method,
        new BinMethodSearchFilter(false, false));
    assertUsagesEqual("Usages of B.test() ()",
        new int[] {4}
        ,
        supervisor.getInvocationsForProject(project));

    cat.debug("Finding usages of method Test.test()");

    testTypeRef = project.getTypeRefForName("Test");

    method = testTypeRef.getBinCIType()
        .getDeclaredMethod("test", BinTypeRef.NO_TYPEREFS);

    supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, method,
        new BinMethodSearchFilter(true, true));
    assertUsagesEqual("Usages of Test.test() (sub/super)",
        new int[] {3, 4, 5, 13, 17}
        ,
        supervisor.getInvocationsForProject(project));

    supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, method,
        new BinMethodSearchFilter(false, true));
    assertUsagesEqual("Usages of Test.test() (sub)",
        new int[] {5}
        ,
        supervisor.getInvocationsForProject(project));

    supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, method,
        new BinMethodSearchFilter(true, false));
    assertUsagesEqual("Usages of Test.test() (super)",
        new int[] {3, 4, 5, 13, 17}
        ,
        supervisor.getInvocationsForProject(project));

    supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, method,
        new BinMethodSearchFilter(false, false));
    assertUsagesEqual("Usages of Test.test() ()",
        new int[] {5}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  /**
   * Tests Where Used for parameter.
   */
  public void testParameter() throws Exception {
    cat.info("Testing Where Used for parameter");

    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("WhereUsed_parameter"));
    project.getProjectLoader().build();

    final BinCIType testClass =
        project.getTypeRefForName("Test").getBinCIType();
    assertNotNull("class Test found in project", testClass);
    final BinMethod method =
        testClass.getDeclaredMethod("test", new BinParameter[] {
        new BinParameter("a", BinPrimitiveType.INT_REF, 0)});
    assertNotNull("method test found in Test class", method);

    final BinParameter parameter = method.getParameters()[0];

    final ManagingIndexer supervisor = new ManagingIndexer();
    new LocalVariableIndexer(supervisor, parameter);
    assertUsagesEqual("Usages of parameter a",
        new int[] {7, 15}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  /**
   * Tests Where Used for parameter used in for cycle.
   */
  public void testParameterUsedInFor() throws Exception {
    cat.info("Testing Where Used for parameter used in for cycle");

    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("WhereUsed_parameter"));
    project.getProjectLoader().build();

    final BinCIType testClass =
        project.getTypeRefForName("Test").getBinCIType();
    assertNotNull("class Test found in project", testClass);
    final BinMethod method =
        testClass.getDeclaredMethod("test2", new BinParameter[] {
        new BinParameter("a", BinPrimitiveType.INT_REF, 0)});
    assertNotNull("method test2 found in Test class", method);

    final BinParameter parameter = method.getParameters()[0];

    final ManagingIndexer supervisor = new ManagingIndexer();
    new LocalVariableIndexer(supervisor, parameter);
    assertUsagesEqual("Usages of parameter a",
        new int[] {19, 19, 19, 20, 21, 19, 26}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  /**
   * Tests Where Used for parameter used because method is invoked on it.
   */
  public void testParameterUsedWhenMethodInvoked() throws Exception {
    cat.info("Testing Where Used for parameter used because method is invoked"
        + " on it");

    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("WhereUsed_parameter"));
    project.getProjectLoader().build();

    final BinCIType testClass =
        project.getTypeRefForName("Test").getBinCIType();
    assertNotNull("class Test found in project", testClass);
    final BinMethod method =
        testClass.getDeclaredMethod("test3", new BinParameter[] {
        new BinParameter("s",
        project.getTypeRefForName("java.lang.String"), 0)});
    assertNotNull("method test3 found in Test class", method);

    final BinParameter parameter = method.getParameters()[0];

    final ManagingIndexer supervisor = new ManagingIndexer();
    new LocalVariableIndexer(supervisor, parameter);
    assertUsagesEqual("Usages of parameter s",
        new int[] {30}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  /**
   * Tests Where Used for parameter used because its field is accessed.
   */
  public void testParameterUsedWhenFieldAccessed() throws Exception {
    cat.info("Testing Where Used for parameter used because its field is"
        + " accessed");

    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("WhereUsed_parameter"));
    project.getProjectLoader().build();

    final BinCIType testClass =
        project.getTypeRefForName("Test").getBinCIType();
    assertNotNull("class Test found in project", testClass);
    final BinMethod method =
        testClass.getDeclaredMethod("test4", new BinParameter[] {
        new BinParameter("t", testClass.getTypeRef(), 0)});
    assertNotNull("method test4 found in Test class", method);

    final BinParameter parameter = method.getParameters()[0];

    final ManagingIndexer supervisor = new ManagingIndexer();
    new LocalVariableIndexer(supervisor, parameter);
    assertUsagesEqual("Usages of parameter t",
        new int[] {34}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  /**
   * Tests Where Used for parameter used because its elements are accessed.
   */
  public void testParameterUsedWhenElementsAccessed() throws Exception {
    cat.info("Testing Where Used for parameter used because its elements are"
        + " accessed");

    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("WhereUsed_parameter"));
    project.getProjectLoader().build();

    final BinCIType testClass =
        project.getTypeRefForName("Test").getBinCIType();
    assertNotNull("class Test found in project", testClass);
    final BinMethod method =
        testClass.getDeclaredMethod("test5", new BinParameter[] {
        new BinParameter("arr",
        project.createArrayTypeForType(project.getObjectRef(), 1), 0)});
    assertNotNull("method test5 found in Test class", method);

    final BinParameter parameter = method.getParameters()[0];

    final ManagingIndexer supervisor = new ManagingIndexer();
    new LocalVariableIndexer(supervisor, parameter);
    assertUsagesEqual("Usages of parameter arr",
        new int[] {38, 38, 39}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  /**
   * Tests bug 179a: WhereUsed does not correclty understand owner class's
   * method invocation
   */
  public void testBug179a() throws Exception {
    cat.info("Testing bug #179 (a)");

    final Project project =
        Utils.createTestRbProject(Utils.getTestProjects().getProject("bug179"));
    project.getProjectLoader().build();

    final BinCIType a =
        project.getTypeRefForName("A").getBinCIType();
    assertNotNull("class A found in project", a);
    BinMethod method = a.getDeclaredMethod("test", BinTypeRef.NO_TYPEREFS);
    assertNotNull("method test found in A class", method);

    final ManagingIndexer supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, method,
        new BinMethodSearchFilter(false, false));
    assertUsagesEqual("Usages of A.test",
        new int[] {18}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  /**
   * Tests bug 179: WhereUsed does not correclty understand owner class's
   * method invocation.
   */
  public void testBug179b() throws Exception {
    cat.info("Testing bug #179 (b)");

    final Project project =
        Utils.createTestRbProject(Utils.getTestProjects().getProject("bug179"));
    project.getProjectLoader().build();

    final BinCIType b =
        project.getTypeRefForName("B").getBinCIType();
    assertNotNull("class B found in project", b);
    BinMethod method = b.getDeclaredMethod("test", BinTypeRef.NO_TYPEREFS);
    assertNotNull("method test found in B class", method);

    final ManagingIndexer supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, method,
        new BinMethodSearchFilter(true, true));
    assertUsagesEqual("Usages of B.test",
        new int[] {10, 17}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  /**
   * Tests bug 181: FIXME
   */
  public void testBug181a() throws Exception {
    cat.info("Testing bug #181 (a)");

    final Project project =
        Utils.createTestRbProject(Utils.getTestProjects().getProject("bug181"));
    project.getProjectLoader().build();

    final BinCIType a =
        project.getTypeRefForName("A").getBinCIType();
    assertNotNull("class A found in project", a);
    ManagingIndexer supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor, a, true, true);
    assertUsagesEqual("Usages of A - sub/super",
        new int[] {2, 5, 10, 10}
        ,
        supervisor.getInvocationsForProject(project));

    supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor, a, false, false);
    assertUsagesEqual("Usages of A",
        new int[] {2, 5, 10, 10}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  /**
   * Tests bug 181: FIXME
   */
  public void testBug181b() throws Exception {
    cat.info("Testing bug #181 (b)");

    final Project project =
        Utils.createTestRbProject(Utils.getTestProjects().getProject("bug181"));
    project.getProjectLoader().build();

    final BinCIType b =
        project.getTypeRefForName("B").getBinCIType();
    assertNotNull("class B found in project", b);
    ManagingIndexer supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor, b, true, true);
    assertUsagesEqual("Usages of B - sub/super",
        new int[] {2, 4, 6, 6}
        ,
        supervisor.getInvocationsForProject(project));

    supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor, b, false, false);
    assertUsagesEqual("Usages of B ",
        new int[] {2, 4, 6, 6}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  /**
   * Generic test for WhereUsed of types
   */
  public void testTypeUsageA() throws Exception {
    cat.info("Testing WhereUsed type usage (A)");

    final Project project =
        Utils.createTestRbProject(Utils.getTestProjects()
        .getProject("WhereUsed_type_usage"));
    project.getProjectLoader().build();

    BinCIType type
        = project.getTypeRefForName("A").getBinCIType();
    assertNotNull("class A found in project", type);

    cat.debug("Finding usages of type A - sub/super");
    ManagingIndexer supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor, type, true, true);
    List results = supervisor.getInvocationsForProject(project);

    List usages = new ArrayList(results.size());
    for (int i = 0, max = results.size(); i < max; i++) {
      usages.add(new Integer(((InvocationData) results.get(i)).getLineNumber()));
    }

    assertTrue("Usage of class A in line 4", usages.remove(new Integer(4)));
    assertTrue("Usage of class A in line 9", usages.remove(new Integer(9)));
    assertTrue("Usage of class A in line 48", usages.remove(new Integer(48)));
    assertTrue("Usage of class A in line 51", usages.remove(new Integer(51)));
    assertTrue("Usage of class A in line 58", usages.remove(new Integer(58)));
    assertTrue("Usage of class A in line 58", usages.remove(new Integer(58)));
    assertTrue("Usage of class A in line 204", usages.remove(new Integer(204)));
    assertTrue("Usage of class A in line 205", usages.remove(new Integer(205)));

    if (usages.size() > 0) {
      String extra = "";
      for (int i = 0, max = usages.size(); i < max; i++) {
        if (i > 0) {extra += ", ";
        }
        extra += usages.get(i);
      }
      assertTrue("Extra usages of A found in lines: " + extra, false);
    }

    cat.debug("Finding usages of type A");
    supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor, type, false, false);
    results = supervisor.getInvocationsForProject(project);

    usages = new ArrayList(results.size());
    for (int i = 0, max = results.size(); i < max; i++) {
      usages.add(new Integer(((InvocationData) results.get(i)).getLineNumber()));
    }

    assertTrue("Usage of class A in line 4", usages.remove(new Integer(4)));
    assertTrue("Usage of class A in line 9", usages.remove(new Integer(9)));

// OK
//    assertTrue("Usage of class A in line 37", usages.remove(new Integer(37)));

    assertTrue("Usage of class A in line 48", usages.remove(new Integer(48)));
    assertTrue("Usage of class A in line 51", usages.remove(new Integer(51)));
    assertTrue("Usage of class A in line 58", usages.remove(new Integer(58)));
    assertTrue("Usage of class A in line 58", usages.remove(new Integer(58)));

// OK
//    assertTrue("Usage of class A in line 202", usages.remove(new Integer(202)));

    assertTrue("Usage of class A in line 204", usages.remove(new Integer(204)));
    assertTrue("Usage of class A in line 205", usages.remove(new Integer(205)));

    if (usages.size() > 0) {
      String extra = "";
      for (int i = 0, max = usages.size(); i < max; i++) {
        if (i > 0) {extra += ", ";
        }
        extra += usages.get(i);
      }
      assertTrue("Extra usages of A found in lines: " + extra, false);
    }

    cat.info("SUCCESS");
  }

  /**
   * Generic test for WhereUsed of types
   */
  public void testTypeUsageB() throws Exception {
    cat.info("Testing WhereUsed type usage (B)");

    final Project project =
        Utils.createTestRbProject(Utils.getTestProjects()
        .getProject("WhereUsed_type_usage"));
    project.getProjectLoader().build();

    BinCIType type
        = project.getTypeRefForName("B").getBinCIType();
    assertNotNull("class B found in project", type);

    cat.debug("Finding usages of type B - sub/super");
    ManagingIndexer supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor, type, true, true);
    List results = supervisor.getInvocationsForProject(project);

    List usages = new ArrayList(results.size());
    for (int i = 0, max = results.size(); i < max; i++) {
      usages.add(new Integer(((InvocationData) results.get(i)).getLineNumber()));
    }

    assertTrue("Usage of class B in line 5", usages.remove(new Integer(5)));
    assertTrue("Usage of class B in line 10", usages.remove(new Integer(10)));
    assertTrue("Usage of class B in line 49", usages.remove(new Integer(49)));

    if (usages.size() > 0) {
      String extra = "";
      for (int i = 0, max = usages.size(); i < max; i++) {
        if (i > 0) {extra += ", ";
        }
        extra += usages.get(i);
      }
      assertTrue("Extra usages of B found in lines: " + extra, false);
    }

    cat.debug("Finding usages of type B");
    supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor, type, false, false);
    results = supervisor.getInvocationsForProject(project);

    usages = new ArrayList(results.size());
    for (int i = 0, max = results.size(); i < max; i++) {
      usages.add(new Integer(((InvocationData) results.get(i)).getLineNumber()));
    }

    assertTrue("Usage of class B in line 5", usages.remove(new Integer(5)));
    assertTrue("Usage of class B in line 10", usages.remove(new Integer(10)));
    assertTrue("Usage of class B in line 49", usages.remove(new Integer(49)));

// OK
//    assertTrue("Usage of class B in line 202", usages.remove(new Integer(202)));

    if (usages.size() > 0) {
      String extra = "";
      for (int i = 0, max = usages.size(); i < max; i++) {
        if (i > 0) {extra += ", ";
        }
        extra += usages.get(i);
      }
      assertTrue("Extra usages of B found in lines: " + extra, false);
    }

    cat.info("SUCCESS");
  }

  /**
   * Generic test for WhereUsed of types
   */
  public void testTypeUsageC() throws Exception {
    cat.info("Testing WhereUsed type usage (C)");

    final Project project =
        Utils.createTestRbProject(Utils.getTestProjects()
        .getProject("WhereUsed_type_usage"));
    project.getProjectLoader().build();

    BinCIType type
        = project.getTypeRefForName("C").getBinCIType();
    assertNotNull("class C found in project", type);

    cat.debug("Finding usages of type C - sub/super");
    ManagingIndexer supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor, type, true, true);
    List results = supervisor.getInvocationsForProject(project);

    List usages = new ArrayList(results.size());
    for (int i = 0, max = results.size(); i < max; i++) {
      usages.add(new Integer(((InvocationData) results.get(i)).getLineNumber()));
    }

    assertTrue("Usage of class C in line 64", usages.remove(new Integer(64)));
    assertTrue("Usage of class C in line 65", usages.remove(new Integer(65)));
    assertTrue("Usage of class C in line 66", usages.remove(new Integer(66)));
    assertTrue("Usage of class C in line 72", usages.remove(new Integer(72)));
    assertTrue("Usage of class C in line 208", usages.remove(new Integer(208)));
    assertTrue("Usage of class C in line 209", usages.remove(new Integer(209)));

    if (usages.size() > 0) {
      String extra = "";
      for (int i = 0, max = usages.size(); i < max; i++) {
        if (i > 0) {extra += ", ";
        }
        extra += usages.get(i);
      }
      assertTrue("Extra usages of C found in lines: " + extra, false);
    }

    cat.debug("Finding usages of type C");
    supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor, type, false, false);
    results = supervisor.getInvocationsForProject(project);

    usages = new ArrayList(results.size());
    for (int i = 0, max = results.size(); i < max; i++) {
      usages.add(new Integer(((InvocationData) results.get(i)).getLineNumber()));
    }

    assertTrue("Usage of class C in line 64", usages.remove(new Integer(64)));
    assertTrue("Usage of class C in line 65", usages.remove(new Integer(65)));
    assertTrue("Usage of class C in line 66", usages.remove(new Integer(66)));
    assertTrue("Usage of class C in line 72", usages.remove(new Integer(72)));

// OK
//    assertTrue("Usage of class C in line 202", usages.remove(new Integer(202)));

    assertTrue("Usage of class C in line 208", usages.remove(new Integer(208)));
    assertTrue("Usage of class C in line 209", usages.remove(new Integer(209)));

    if (usages.size() > 0) {
      String extra = "";
      for (int i = 0, max = usages.size(); i < max; i++) {
        if (i > 0) {extra += ", ";
        }
        extra += usages.get(i);
      }
      assertTrue("Extra usages of C found in lines: " + extra, false);
    }

    cat.info("SUCCESS");
  }

  /**
   * Generic test for WhereUsed of types
   */
  public void testTypeUsageD() throws Exception {
    cat.info("Testing WhereUsed type usage (D)");

    final Project project =
        Utils.createTestRbProject(Utils.getTestProjects()
        .getProject("WhereUsed_type_usage"));
    project.getProjectLoader().build();

    BinCIType type
        = project.getTypeRefForName("D").getBinCIType();
    assertNotNull("class D found in project", type);

    cat.debug("Finding usages of type D - sub/super");
    ManagingIndexer supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor, type, true, true);
    List results = supervisor.getInvocationsForProject(project);

    List usages = new ArrayList(results.size());
    for (int i = 0, max = results.size(); i < max; i++) {
      usages.add(new Integer(((InvocationData) results.get(i)).getLineNumber()));
    }

    assertTrue("Usage #1 of class D in line 37", usages.remove(new Integer(37)));
    assertTrue("Usage #2 of class D in line 202", usages.remove(new Integer(202)));

    if (usages.size() > 0) {
      String extra = "";
      for (int i = 0, max = usages.size(); i < max; i++) {
        if (i > 0) {extra += ", ";
        }
        extra += usages.get(i);
      }
      assertTrue("Extra usages of D found in lines: " + extra, false);
    }

    cat.debug("Finding usages of type D");
    supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor, type, false, false);
    results = supervisor.getInvocationsForProject(project);

    usages = new ArrayList(results.size());
    for (int i = 0, max = results.size(); i < max; i++) {
      usages.add(new Integer(((InvocationData) results.get(i)).getLineNumber()));
    }

    // should be no usages

    if (usages.size() > 0) {
      String extra = "";
      for (int i = 0, max = usages.size(); i < max; i++) {
        if (i > 0) {extra += ", ";
        }
        extra += usages.get(i);
      }
      assertTrue("Extra usages of D found in lines: " + extra, false);
    }

    cat.info("SUCCESS");
  }

  /**
   * Generic test for WhereUsed of types
   */
  public void testTypeUsageE() throws Exception {
    cat.info("Testing WhereUsed type usage (E)");

    final Project project =
        Utils.createTestRbProject(Utils.getTestProjects()
        .getProject("WhereUsed_type_usage"));
    project.getProjectLoader().build();

    BinCIType type
        = project.getTypeRefForName("E").getBinCIType();
    assertNotNull("class E found in project", type);

    cat.debug("Finding usages of type E - sub/super");
    ManagingIndexer supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor, type, true, true);
    List results = supervisor.getInvocationsForProject(project);

    List usages = new ArrayList(results.size());
    for (int i = 0, max = results.size(); i < max; i++) {
      usages.add(new Integer(((InvocationData) results.get(i)).getLineNumber()));
    }

    assertTrue("Usage of class E in line 13", usages.remove(new Integer(13)));
    assertTrue("Usage of class E in line 14", usages.remove(new Integer(14)));
    assertTrue("Usage of class E in line 16", usages.remove(new Integer(16)));

// OK
//    assertTrue("Usage of class E in line 23", usages.remove(new Integer(23)));
//    assertTrue("Usage of class E in line 34", usages.remove(new Integer(34)));

    assertTrue("Usage of class E in line 92", usages.remove(new Integer(92)));
    assertTrue("Usage of class E in line 92", usages.remove(new Integer(92)));

    // override
    assertTrue("Usage of class E in line 93", usages.remove(new Integer(93)));

    assertTrue("Usage of class E in line 99", usages.remove(new Integer(99)));
    assertTrue("Usage of class E in line 100", usages.remove(new Integer(100)));
    assertTrue("Usage of class E in line 103", usages.remove(new Integer(103)));
    assertTrue("Usage of class E in line 105", usages.remove(new Integer(105)));
    assertTrue("Usage of class E in line 105", usages.remove(new Integer(105)));
    assertTrue("Usage of class E in line 107", usages.remove(new Integer(107)));
    assertTrue("Usage of class E in line 109", usages.remove(new Integer(109)));
    assertTrue("Usage of class E in line 117", usages.remove(new Integer(117)));
    assertTrue("Usage of class E in line 118", usages.remove(new Integer(118)));
    assertTrue("Usage of class E in line 121", usages.remove(new Integer(121)));
    assertTrue("Usage of class E in line 123", usages.remove(new Integer(123)));
    assertTrue("Usage of class E in line 124", usages.remove(new Integer(124)));
    assertTrue("Usage of class E in line 126", usages.remove(new Integer(126)));
    assertTrue("Usage of class E in line 127", usages.remove(new Integer(127)));
    assertTrue("Usage of class E in line 160", usages.remove(new Integer(160)));
    assertTrue("Usage of class E in line 162", usages.remove(new Integer(162)));
    assertTrue("Usage of class E in line 162", usages.remove(new Integer(162)));
    assertTrue("Usage of class E in line 164", usages.remove(new Integer(164)));
    assertTrue("Usage of class E in line 166", usages.remove(new Integer(166)));
    assertTrue("Usage of class E in line 178", usages.remove(new Integer(178)));
    assertTrue("Usage of class E in line 180", usages.remove(new Integer(180)));
    assertTrue("Usage of class E in line 181", usages.remove(new Integer(181)));
    assertTrue("Usage of class E in line 183", usages.remove(new Integer(183)));
    assertTrue("Usage of class E in line 184", usages.remove(new Integer(184)));

    if (usages.size() > 0) {
      String extra = "";
      for (int i = 0, max = usages.size(); i < max; i++) {
        if (i > 0) {extra += ", ";
        }
        extra += usages.get(i);
      }
      assertTrue("Extra usages of E found in lines: " + extra, false);
    }

    cat.debug("Finding usages of type E");
    supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor, type, false, false);
    results = supervisor.getInvocationsForProject(project);

    usages = new ArrayList(results.size());
    for (int i = 0, max = results.size(); i < max; i++) {
      usages.add(new Integer(((InvocationData) results.get(i)).getLineNumber()));
    }

    assertTrue("Usage of class E in line 13", usages.remove(new Integer(13)));
    assertTrue("Usage of class E in line 14", usages.remove(new Integer(14)));
    assertTrue("Usage of class E in line 16", usages.remove(new Integer(16)));
    assertTrue("Usage of class E in line 92", usages.remove(new Integer(92)));
    assertTrue("Usage of class E in line 92", usages.remove(new Integer(92)));
    assertTrue("Usage of class E in line 99", usages.remove(new Integer(99)));
    assertTrue("Usage of class E in line 100", usages.remove(new Integer(100)));
    assertTrue("Usage of class E in line 103", usages.remove(new Integer(103)));
    assertTrue("Usage of class E in line 105", usages.remove(new Integer(105)));
    assertTrue("Usage of class E in line 105", usages.remove(new Integer(105)));
    assertTrue("Usage of class E in line 107", usages.remove(new Integer(107)));
    assertTrue("Usage of class E in line 109", usages.remove(new Integer(109)));
    assertTrue("Usage of class E in line 117", usages.remove(new Integer(117)));
    assertTrue("Usage of class E in line 118", usages.remove(new Integer(118)));

// OK
//    assertTrue("Usage of class E in line 121", usages.remove(new Integer(121)));
//    assertTrue("Usage of class E in line 123", usages.remove(new Integer(123)));
//    assertTrue("Usage of class E in line 124", usages.remove(new Integer(124)));

    assertTrue("Usage of class E in line 126", usages.remove(new Integer(126)));
    assertTrue("Usage of class E in line 127", usages.remove(new Integer(127)));
    assertTrue("Usage of class E in line 160", usages.remove(new Integer(160)));
    assertTrue("Usage of class E in line 162", usages.remove(new Integer(162)));
    assertTrue("Usage of class E in line 162", usages.remove(new Integer(162)));
    assertTrue("Usage of class E in line 164", usages.remove(new Integer(164)));
    assertTrue("Usage of class E in line 166", usages.remove(new Integer(166)));

// OK
//    assertTrue("Usage of class E in line 178", usages.remove(new Integer(178)));
//    assertTrue("Usage of class E in line 180", usages.remove(new Integer(180)));
//    assertTrue("Usage of class E in line 181", usages.remove(new Integer(181)));
//    assertTrue("Usage of class E in line 183", usages.remove(new Integer(183)));
//    assertTrue("Usage of class E in line 184", usages.remove(new Integer(184)));

// OK
//    assertTrue("Usage of class E in line 202", usages.remove(new Integer(202)));

    if (usages.size() > 0) {
      String extra = "";
      for (int i = 0, max = usages.size(); i < max; i++) {
        if (i > 0) {extra += ", ";
        }
        extra += usages.get(i);
      }
      assertTrue("Extra usages of E found in lines: " + extra, false);
    }

    cat.debug("Finding usages of type E - sub");
    supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor, type, false, true);
    results = supervisor.getInvocationsForProject(project);

    usages = new ArrayList(results.size());
    for (int i = 0, max = results.size(); i < max; i++) {
      usages.add(new Integer(((InvocationData) results.get(i)).getLineNumber()));
    }

    assertTrue("Usage of class E in line 13", usages.remove(new Integer(13)));
    assertTrue("Usage of class E in line 14", usages.remove(new Integer(14)));
    assertTrue("Usage of class E in line 16", usages.remove(new Integer(16)));

// OK
//    assertTrue("Usage of class E in line 23", usages.remove(new Integer(23)));
//    assertTrue("Usage of class E in line 34", usages.remove(new Integer(34)));

    assertTrue("Usage of class E in line 92", usages.remove(new Integer(92)));
    assertTrue("Usage of class E in line 92", usages.remove(new Integer(92)));

    // override
    assertTrue("Usage of class E in line 93", usages.remove(new Integer(93)));

    assertTrue("Usage of class E in line 99", usages.remove(new Integer(99)));
    assertTrue("Usage of class E in line 100", usages.remove(new Integer(100)));
    assertTrue("Usage of class E in line 103", usages.remove(new Integer(103)));
    assertTrue("Usage of class E in line 105", usages.remove(new Integer(105)));
    assertTrue("Usage of class E in line 105", usages.remove(new Integer(105)));
    assertTrue("Usage of class E in line 107", usages.remove(new Integer(107)));
    assertTrue("Usage of class E in line 109", usages.remove(new Integer(109)));
    assertTrue("Usage of class E in line 117", usages.remove(new Integer(117)));
    assertTrue("Usage of class E in line 118", usages.remove(new Integer(118)));
    assertTrue("Usage of class E in line 121", usages.remove(new Integer(121)));
    assertTrue("Usage of class E in line 123", usages.remove(new Integer(123)));
    assertTrue("Usage of class E in line 124", usages.remove(new Integer(124)));
    assertTrue("Usage of class E in line 126", usages.remove(new Integer(126)));
    assertTrue("Usage of class E in line 127", usages.remove(new Integer(127)));
    assertTrue("Usage of class E in line 160", usages.remove(new Integer(160)));
    assertTrue("Usage of class E in line 162", usages.remove(new Integer(162)));
    assertTrue("Usage of class E in line 162", usages.remove(new Integer(162)));
    assertTrue("Usage of class E in line 164", usages.remove(new Integer(164)));
    assertTrue("Usage of class E in line 166", usages.remove(new Integer(166)));
    assertTrue("Usage of class E in line 178", usages.remove(new Integer(178)));
    assertTrue("Usage of class E in line 180", usages.remove(new Integer(180)));
    assertTrue("Usage of class E in line 181", usages.remove(new Integer(181)));
    assertTrue("Usage of class E in line 183", usages.remove(new Integer(183)));
    assertTrue("Usage of class E in line 184", usages.remove(new Integer(184)));

// OK
//    assertTrue("Usage of class E in line 202", usages.remove(new Integer(202)));

    if (usages.size() > 0) {
      String extra = "";
      for (int i = 0, max = usages.size(); i < max; i++) {
        if (i > 0) {extra += ", ";
        }
        extra += usages.get(i);
      }
      assertTrue("Extra usages of E found in lines: " + extra, false);
    }

    cat.debug("Finding usages of type E - super");
    supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor, type, true, false);
    results = supervisor.getInvocationsForProject(project);

    usages = new ArrayList(results.size());
    for (int i = 0, max = results.size(); i < max; i++) {
      usages.add(new Integer(((InvocationData) results.get(i)).getLineNumber()));
    }

    assertTrue("Usage of class E in line 13", usages.remove(new Integer(13)));
    assertTrue("Usage of class E in line 14", usages.remove(new Integer(14)));
    assertTrue("Usage of class E in line 16", usages.remove(new Integer(16)));
    assertTrue("Usage of class E in line 92", usages.remove(new Integer(92)));
    assertTrue("Usage of class E in line 92", usages.remove(new Integer(92)));
    assertTrue("Usage of class E in line 99", usages.remove(new Integer(99)));
    assertTrue("Usage of class E in line 100", usages.remove(new Integer(100)));
    assertTrue("Usage of class E in line 103", usages.remove(new Integer(103)));
    assertTrue("Usage of class E in line 105", usages.remove(new Integer(105)));
    assertTrue("Usage of class E in line 105", usages.remove(new Integer(105)));
    assertTrue("Usage of class E in line 107", usages.remove(new Integer(107)));
    assertTrue("Usage of class E in line 109", usages.remove(new Integer(109)));
    assertTrue("Usage of class E in line 117", usages.remove(new Integer(117)));
    assertTrue("Usage of class E in line 118", usages.remove(new Integer(118)));

// OK
//    assertTrue("Usage of class E in line 121", usages.remove(new Integer(121)));
//    assertTrue("Usage of class E in line 123", usages.remove(new Integer(123)));
//    assertTrue("Usage of class E in line 124", usages.remove(new Integer(124)));

    assertTrue("Usage of class E in line 126", usages.remove(new Integer(126)));
    assertTrue("Usage of class E in line 127", usages.remove(new Integer(127)));
    assertTrue("Usage of class E in line 160", usages.remove(new Integer(160)));
    assertTrue("Usage of class E in line 162", usages.remove(new Integer(162)));
    assertTrue("Usage of class E in line 162", usages.remove(new Integer(162)));
    assertTrue("Usage of class E in line 164", usages.remove(new Integer(164)));
    assertTrue("Usage of class E in line 166", usages.remove(new Integer(166)));

// OK
//    assertTrue("Usage of class E in line 178", usages.remove(new Integer(178)));
//    assertTrue("Usage of class E in line 180", usages.remove(new Integer(180)));
//    assertTrue("Usage of class E in line 181", usages.remove(new Integer(181)));
//    assertTrue("Usage of class E in line 183", usages.remove(new Integer(183)));
//    assertTrue("Usage of class E in line 184", usages.remove(new Integer(184)));

    if (usages.size() > 0) {
      String extra = "";
      for (int i = 0, max = usages.size(); i < max; i++) {
        if (i > 0) {extra += ", ";
        }
        extra += usages.get(i);
      }
      assertTrue("Extra usages of E found in lines: " + extra, false);
    }

    cat.info("SUCCESS");
  }

  /**
   * Generic test for WhereUsed of types
   */
  public void testTypeUsageF() throws Exception {
    cat.info("Testing WhereUsed type usage (F)");

    final Project project =
        Utils.createTestRbProject(Utils.getTestProjects()
        .getProject("WhereUsed_type_usage"));
    project.getProjectLoader().build();

    BinCIType type
        = project.getTypeRefForName("F").getBinCIType();
    assertNotNull("class F found in project", type);

    cat.debug("Finding usages of type F - sub/super");
    ManagingIndexer supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor, type, true, true);
    List results = supervisor.getInvocationsForProject(project);

    List usages = new ArrayList(results.size());
    for (int i = 0, max = results.size(); i < max; i++) {
      usages.add(new Integer(((InvocationData) results.get(i)).getLineNumber()));
    }

    assertTrue("Usage of class F in line 16", usages.remove(new Integer(16)));
    assertTrue("Usage of class F in line 19", usages.remove(new Integer(19)));
    assertTrue("Usage of class F in line 20", usages.remove(new Integer(20)));
    assertTrue("Usage of class F in line 23", usages.remove(new Integer(23)));
    assertTrue("Usage of class F in line 34", usages.remove(new Integer(34)));
    assertTrue("Usage of class F in line 83", usages.remove(new Integer(83)));
    assertTrue("Usage of class F in line 94", usages.remove(new Integer(94)));
    assertTrue("Usage of class F in line 96", usages.remove(new Integer(96)));
    assertTrue("Usage of class F in line 97", usages.remove(new Integer(97)));
    assertTrue("Usage of class F in line 97", usages.remove(new Integer(97)));
    assertTrue("Usage of class F in line 100", usages.remove(new Integer(100)));
    assertTrue("Usage of class F in line 107", usages.remove(new Integer(107)));
    assertTrue("Usage of class F in line 112", usages.remove(new Integer(112)));
    assertTrue("Usage of class F in line 114", usages.remove(new Integer(114)));
    assertTrue("Usage of class F in line 115", usages.remove(new Integer(115)));
    assertTrue("Usage of class F in line 115", usages.remove(new Integer(115)));
    assertTrue("Usage of class F in line 118", usages.remove(new Integer(118)));
    assertTrue("Usage of class F in line 124", usages.remove(new Integer(124)));
    assertTrue("Usage of class F in line 127", usages.remove(new Integer(127)));
    assertTrue("Usage of class F in line 131", usages.remove(new Integer(131)));
    assertTrue("Usage of class F in line 133", usages.remove(new Integer(133)));
    assertTrue("Usage of class F in line 135", usages.remove(new Integer(135)));
    assertTrue("Usage of class F in line 135", usages.remove(new Integer(135)));
    assertTrue("Usage of class F in line 139", usages.remove(new Integer(139)));
    assertTrue("Usage of class F in line 141", usages.remove(new Integer(141)));
    assertTrue("Usage of class F in line 143", usages.remove(new Integer(143)));
    assertTrue("Usage of class F in line 143", usages.remove(new Integer(143)));
    assertTrue("Usage of class F in line 149", usages.remove(new Integer(149)));
    assertTrue("Usage of class F in line 149", usages.remove(new Integer(149)));
    assertTrue("Usage of class F in line 151", usages.remove(new Integer(151)));
    assertTrue("Usage of class F in line 153", usages.remove(new Integer(153)));
    assertTrue("Usage of class F in line 154", usages.remove(new Integer(154)));
    assertTrue("Usage of class F in line 156", usages.remove(new Integer(156)));
    assertTrue("Usage of class F in line 157", usages.remove(new Integer(157)));
    assertTrue("Usage of class F in line 164", usages.remove(new Integer(164)));
    assertTrue("Usage of class F in line 169", usages.remove(new Integer(169)));
    assertTrue("Usage of class F in line 171", usages.remove(new Integer(171)));
    assertTrue("Usage of class F in line 172", usages.remove(new Integer(172)));
    assertTrue("Usage of class F in line 174", usages.remove(new Integer(174)));
    assertTrue("Usage of class F in line 175", usages.remove(new Integer(175)));
    assertTrue("Usage of class F in line 187", usages.remove(new Integer(187)));
    assertTrue("Usage of class F in line 191", usages.remove(new Integer(191)));

    // override
    assertTrue("Usage of class F in line 193", usages.remove(new Integer(193)));

    if (usages.size() > 0) {
      String extra = "";
      for (int i = 0, max = usages.size(); i < max; i++) {
        if (i > 0) {extra += ", ";
        }
        extra += usages.get(i);
      }
      assertTrue("Extra usages of F found in lines: " + extra, false);
    }

    cat.debug("Finding usages of type F");
    supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor, type, false, false);
    results = supervisor.getInvocationsForProject(project);

    usages = new ArrayList(results.size());
    for (int i = 0, max = results.size(); i < max; i++) {
      usages.add(new Integer(((InvocationData) results.get(i)).getLineNumber()));
    }

    assertTrue("Usage of class F in line 19", usages.remove(new Integer(19)));
    assertTrue("Usage of class F in line 20", usages.remove(new Integer(20)));
    assertTrue("Usage of class F in line 23", usages.remove(new Integer(23)));

// OK
//    assertTrue("Usage of class F in line 34", usages.remove(new Integer(34)));

    assertTrue("Usage of class F in line 94", usages.remove(new Integer(94)));
    assertTrue("Usage of class F in line 96", usages.remove(new Integer(96)));
    assertTrue("Usage of class F in line 97", usages.remove(new Integer(97)));
    assertTrue("Usage of class F in line 97", usages.remove(new Integer(97)));
    assertTrue("Usage of class F in line 100", usages.remove(new Integer(100)));
    assertTrue("Usage of class F in line 107", usages.remove(new Integer(107)));
    assertTrue("Usage of class F in line 112", usages.remove(new Integer(112)));
    assertTrue("Usage of class F in line 114", usages.remove(new Integer(114)));
    assertTrue("Usage of class F in line 115", usages.remove(new Integer(115)));
    assertTrue("Usage of class F in line 115", usages.remove(new Integer(115)));
    assertTrue("Usage of class F in line 118", usages.remove(new Integer(118)));
    assertTrue("Usage of class F in line 124", usages.remove(new Integer(124)));
    assertTrue("Usage of class F in line 127", usages.remove(new Integer(127)));
    assertTrue("Usage of class F in line 131", usages.remove(new Integer(131)));
    assertTrue("Usage of class F in line 133", usages.remove(new Integer(133)));
    assertTrue("Usage of class F in line 135", usages.remove(new Integer(135)));
    assertTrue("Usage of class F in line 135", usages.remove(new Integer(135)));
    assertTrue("Usage of class F in line 139", usages.remove(new Integer(139)));
    assertTrue("Usage of class F in line 141", usages.remove(new Integer(141)));
    assertTrue("Usage of class F in line 143", usages.remove(new Integer(143)));
    assertTrue("Usage of class F in line 143", usages.remove(new Integer(143)));
    assertTrue("Usage of class F in line 149", usages.remove(new Integer(149)));
    assertTrue("Usage of class F in line 149", usages.remove(new Integer(149)));

// OK
//    assertTrue("Usage of class F in line 151", usages.remove(new Integer(151)));
//    assertTrue("Usage of class F in line 153", usages.remove(new Integer(153)));
//    assertTrue("Usage of class F in line 154", usages.remove(new Integer(154)));

    assertTrue("Usage of class F in line 156", usages.remove(new Integer(156)));
    assertTrue("Usage of class F in line 157", usages.remove(new Integer(157)));
    assertTrue("Usage of class F in line 164", usages.remove(new Integer(164)));

// OK
//    assertTrue("Usage of class F in line 169", usages.remove(new Integer(169)));
//    assertTrue("Usage of class F in line 171", usages.remove(new Integer(171)));
//    assertTrue("Usage of class F in line 172", usages.remove(new Integer(172)));

    assertTrue("Usage of class F in line 174", usages.remove(new Integer(174)));
    assertTrue("Usage of class F in line 175", usages.remove(new Integer(175)));
    assertTrue("Usage of class F in line 187", usages.remove(new Integer(187)));
    assertTrue("Usage of class F in line 191", usages.remove(new Integer(191)));

// OK
//    assertTrue("Usage of class F in line 202", usages.remove(new Integer(202)));

    if (usages.size() > 0) {
      String extra = "";
      for (int i = 0, max = usages.size(); i < max; i++) {
        if (i > 0) {extra += ", ";
        }
        extra += usages.get(i);
      }
      assertTrue("Extra usages of F found in lines: " + extra, false);
    }

    cat.debug("Finding usages of type F - sub");
    supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor, type, false, true);
    results = supervisor.getInvocationsForProject(project);

    usages = new ArrayList(results.size());
    for (int i = 0, max = results.size(); i < max; i++) {
      usages.add(new Integer(((InvocationData) results.get(i)).getLineNumber()));
    }

// OK
//    assertTrue("Usage of class F in line 16", usages.remove(new Integer(16)));

    assertTrue("Usage of class F in line 19", usages.remove(new Integer(19)));
    assertTrue("Usage of class F in line 20", usages.remove(new Integer(20)));
    assertTrue("Usage of class F in line 23", usages.remove(new Integer(23)));
    assertTrue("Usage of class F in line 34", usages.remove(new Integer(34)));
    assertTrue("Usage of class F in line 94", usages.remove(new Integer(94)));
    assertTrue("Usage of class F in line 96", usages.remove(new Integer(96)));
    assertTrue("Usage of class F in line 97", usages.remove(new Integer(97)));
    assertTrue("Usage of class F in line 97", usages.remove(new Integer(97)));
    assertTrue("Usage of class F in line 100", usages.remove(new Integer(100)));
    assertTrue("Usage of class F in line 107", usages.remove(new Integer(107)));
    assertTrue("Usage of class F in line 112", usages.remove(new Integer(112)));
    assertTrue("Usage of class F in line 114", usages.remove(new Integer(114)));
    assertTrue("Usage of class F in line 115", usages.remove(new Integer(115)));
    assertTrue("Usage of class F in line 115", usages.remove(new Integer(115)));
    assertTrue("Usage of class F in line 118", usages.remove(new Integer(118)));
    assertTrue("Usage of class F in line 124", usages.remove(new Integer(124)));
    assertTrue("Usage of class F in line 127", usages.remove(new Integer(127)));
    assertTrue("Usage of class F in line 131", usages.remove(new Integer(131)));
    assertTrue("Usage of class F in line 133", usages.remove(new Integer(133)));
    assertTrue("Usage of class F in line 135", usages.remove(new Integer(135)));
    assertTrue("Usage of class F in line 135", usages.remove(new Integer(135)));
    assertTrue("Usage of class F in line 139", usages.remove(new Integer(139)));
    assertTrue("Usage of class F in line 141", usages.remove(new Integer(141)));
    assertTrue("Usage of class F in line 143", usages.remove(new Integer(143)));
    assertTrue("Usage of class F in line 143", usages.remove(new Integer(143)));
    assertTrue("Usage of class F in line 149", usages.remove(new Integer(149)));
    assertTrue("Usage of class F in line 149", usages.remove(new Integer(149)));
    assertTrue("Usage of class F in line 151", usages.remove(new Integer(151)));
    assertTrue("Usage of class F in line 153", usages.remove(new Integer(153)));
    assertTrue("Usage of class F in line 154", usages.remove(new Integer(154)));
    assertTrue("Usage of class F in line 156", usages.remove(new Integer(156)));
    assertTrue("Usage of class F in line 157", usages.remove(new Integer(157)));
    assertTrue("Usage of class F in line 164", usages.remove(new Integer(164)));
    assertTrue("Usage of class F in line 169", usages.remove(new Integer(169)));
    assertTrue("Usage of class F in line 171", usages.remove(new Integer(171)));
    assertTrue("Usage of class F in line 172", usages.remove(new Integer(172)));
    assertTrue("Usage of class F in line 174", usages.remove(new Integer(174)));
    assertTrue("Usage of class F in line 175", usages.remove(new Integer(175)));
    assertTrue("Usage of class F in line 187", usages.remove(new Integer(187)));
    assertTrue("Usage of class F in line 191", usages.remove(new Integer(191)));

    // override
    assertTrue("Usage of class F in line 193", usages.remove(new Integer(193)));

// OK
//    assertTrue("Usage of class F in line 202", usages.remove(new Integer(202)));

    if (usages.size() > 0) {
      String extra = "";
      for (int i = 0, max = usages.size(); i < max; i++) {
        if (i > 0) {extra += ", ";
        }
        extra += usages.get(i);
      }
      assertTrue("Extra usages of F found in lines: " + extra, false);
    }

    cat.debug("Finding usages of type F - super");
    supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor, type, true, false);
    results = supervisor.getInvocationsForProject(project);

    usages = new ArrayList(results.size());
    for (int i = 0, max = results.size(); i < max; i++) {
      usages.add(new Integer(((InvocationData) results.get(i)).getLineNumber()));
    }

    assertTrue("Usage of class F in line 16", usages.remove(new Integer(16)));
    assertTrue("Usage of class F in line 19", usages.remove(new Integer(19)));
    assertTrue("Usage of class F in line 20", usages.remove(new Integer(20)));
    assertTrue("Usage of class F in line 23", usages.remove(new Integer(23)));

// OK
//    assertTrue("Usage of class F in line 34", usages.remove(new Integer(34)));

    assertTrue("Usage of class F in line 83", usages.remove(new Integer(83)));
    assertTrue("Usage of class F in line 94", usages.remove(new Integer(94)));
    assertTrue("Usage of class F in line 96", usages.remove(new Integer(96)));
    assertTrue("Usage of class F in line 97", usages.remove(new Integer(97)));
    assertTrue("Usage of class F in line 97", usages.remove(new Integer(97)));
    assertTrue("Usage of class F in line 100", usages.remove(new Integer(100)));
    assertTrue("Usage of class F in line 107", usages.remove(new Integer(107)));
    assertTrue("Usage of class F in line 112", usages.remove(new Integer(112)));
    assertTrue("Usage of class F in line 114", usages.remove(new Integer(114)));
    assertTrue("Usage of class F in line 115", usages.remove(new Integer(115)));
    assertTrue("Usage of class F in line 115", usages.remove(new Integer(115)));
    assertTrue("Usage of class F in line 118", usages.remove(new Integer(118)));
    assertTrue("Usage of class F in line 124", usages.remove(new Integer(124)));
    assertTrue("Usage of class F in line 127", usages.remove(new Integer(127)));
    assertTrue("Usage of class F in line 131", usages.remove(new Integer(131)));
    assertTrue("Usage of class F in line 133", usages.remove(new Integer(133)));
    assertTrue("Usage of class F in line 135", usages.remove(new Integer(135)));
    assertTrue("Usage of class F in line 135", usages.remove(new Integer(135)));
    assertTrue("Usage of class F in line 139", usages.remove(new Integer(139)));
    assertTrue("Usage of class F in line 141", usages.remove(new Integer(141)));
    assertTrue("Usage of class F in line 143", usages.remove(new Integer(143)));
    assertTrue("Usage of class F in line 143", usages.remove(new Integer(143)));
    assertTrue("Usage of class F in line 149", usages.remove(new Integer(149)));
    assertTrue("Usage of class F in line 149", usages.remove(new Integer(149)));

// OK
//    assertTrue("Usage of class F in line 151", usages.remove(new Integer(151)));
//    assertTrue("Usage of class F in line 153", usages.remove(new Integer(153)));
//    assertTrue("Usage of class F in line 154", usages.remove(new Integer(154)));

    assertTrue("Usage of class F in line 156", usages.remove(new Integer(156)));
    assertTrue("Usage of class F in line 157", usages.remove(new Integer(157)));
    assertTrue("Usage of class F in line 164", usages.remove(new Integer(164)));

// OK
//    assertTrue("Usage of class F in line 169", usages.remove(new Integer(169)));
//    assertTrue("Usage of class F in line 171", usages.remove(new Integer(171)));
//    assertTrue("Usage of class F in line 172", usages.remove(new Integer(172)));

    assertTrue("Usage of class F in line 174", usages.remove(new Integer(174)));
    assertTrue("Usage of class F in line 175", usages.remove(new Integer(175)));
    assertTrue("Usage of class F in line 187", usages.remove(new Integer(187)));
    assertTrue("Usage of class F in line 191", usages.remove(new Integer(191)));

    if (usages.size() > 0) {
      String extra = "";
      for (int i = 0, max = usages.size(); i < max; i++) {
        if (i > 0) {extra += ", ";
        }
        extra += usages.get(i);
      }
      assertTrue("Extra usages of F found in lines: " + extra, false);
    }

    cat.info("SUCCESS");
  }

  /**
   * Generic test for WhereUsed of types
   */
  public void testTypeUsageG() throws Exception {
    cat.info("Testing WhereUsed type usage (G)");

    final Project project =
        Utils.createTestRbProject(Utils.getTestProjects()
        .getProject("WhereUsed_type_usage"));
    project.getProjectLoader().build();

    BinCIType type
        = project.getTypeRefForName("G").getBinCIType();
    assertNotNull("class G found in project", type);

    cat.debug("Finding usages of type G - sub/super");
    ManagingIndexer supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor, type, true, true);
    List results = supervisor.getInvocationsForProject(project);

    List usages = new ArrayList(results.size());
    for (int i = 0, max = results.size(); i < max; i++) {
      usages.add(new Integer(((InvocationData) results.get(i)).getLineNumber()));
    }

    assertTrue("Usage of class G in line 21", usages.remove(new Integer(21)));
    assertTrue("Usage of class G in line 28", usages.remove(new Integer(28)));
    assertTrue("Usage of class G in line 29", usages.remove(new Integer(29)));
    assertTrue("Usage of class G in line 31", usages.remove(new Integer(31)));
    assertTrue("Usage of class G in line 32", usages.remove(new Integer(32)));
    assertTrue("Usage of class G in line 34", usages.remove(new Integer(34)));
    assertTrue("Usage of class G in line 40", usages.remove(new Integer(40)));
    assertTrue("Usage of class G in line 109", usages.remove(new Integer(109)));
    assertTrue("Usage of class G in line 130", usages.remove(new Integer(130)));
    assertTrue("Usage of class G in line 131", usages.remove(new Integer(131)));
    assertTrue("Usage of class G in line 133", usages.remove(new Integer(133)));
    assertTrue("Usage of class G in line 133", usages.remove(new Integer(135)));
    assertTrue("Usage of class G in line 151", usages.remove(new Integer(151)));
    assertTrue("Usage of class G in line 153", usages.remove(new Integer(153)));
    assertTrue("Usage of class G in line 154", usages.remove(new Integer(154)));
    assertTrue("Usage of class G in line 154", usages.remove(new Integer(154)));
    assertTrue("Usage of class G in line 157", usages.remove(new Integer(157)));
    assertTrue("Usage of class G in line 166", usages.remove(new Integer(166)));
    assertTrue("Usage of class G in line 172", usages.remove(new Integer(172)));
    assertTrue("Usage of class G in line 175", usages.remove(new Integer(175)));
    assertTrue("Usage of class G in line 181", usages.remove(new Integer(181)));
    assertTrue("Usage of class G in line 184", usages.remove(new Integer(184)));
    assertTrue("Usage of class G in line 188", usages.remove(new Integer(188)));

    if (usages.size() > 0) {
      String extra = "";
      for (int i = 0, max = usages.size(); i < max; i++) {
        if (i > 0) {extra += ", ";
        }
        extra += usages.get(i);
      }
      assertTrue("Extra usages of G found in lines: " + extra, false);
    }

    cat.debug("Finding usages of type G");
    supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor, type, false, false);
    results = supervisor.getInvocationsForProject(project);

    usages = new ArrayList(results.size());
    for (int i = 0, max = results.size(); i < max; i++) {
      usages.add(new Integer(((InvocationData) results.get(i)).getLineNumber()));
    }

    assertTrue("Usage of class G in line 21", usages.remove(new Integer(21)));
    assertTrue("Usage of class G in line 28", usages.remove(new Integer(28)));
    assertTrue("Usage of class G in line 29", usages.remove(new Integer(29)));
    assertTrue("Usage of class G in line 31", usages.remove(new Integer(31)));
    assertTrue("Usage of class G in line 32", usages.remove(new Integer(32)));
    assertTrue("Usage of class G in line 34", usages.remove(new Integer(34)));
    assertTrue("Usage of class G in line 40", usages.remove(new Integer(40)));
    assertTrue("Usage of class G in line 109", usages.remove(new Integer(109)));

// OK
//    assertTrue("Usage of class G in line 131", usages.remove(new Integer(131)));
//    assertTrue("Usage of class G in line 133", usages.remove(new Integer(133)));
//    assertTrue("Usage of class G in line 135", usages.remove(new Integer(135)));

    assertTrue("Usage of class G in line 151", usages.remove(new Integer(151)));
    assertTrue("Usage of class G in line 153", usages.remove(new Integer(153)));
    assertTrue("Usage of class G in line 154", usages.remove(new Integer(154)));
    assertTrue("Usage of class G in line 154", usages.remove(new Integer(154)));
    assertTrue("Usage of class G in line 157", usages.remove(new Integer(157)));
    assertTrue("Usage of class G in line 166", usages.remove(new Integer(166)));
    assertTrue("Usage of class G in line 172", usages.remove(new Integer(172)));
    assertTrue("Usage of class G in line 175", usages.remove(new Integer(175)));
    assertTrue("Usage of class G in line 181", usages.remove(new Integer(181)));
    assertTrue("Usage of class G in line 184", usages.remove(new Integer(184)));
    assertTrue("Usage of class G in line 188", usages.remove(new Integer(188)));

    if (usages.size() > 0) {
      String extra = "";
      for (int i = 0, max = usages.size(); i < max; i++) {
        if (i > 0) {extra += ", ";
        }
        extra += usages.get(i);
      }
      assertTrue("Extra usages of G found in lines: " + extra, false);
    }

    cat.debug("Finding usages of type G - sub");
    supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor, type, false, true);
    results = supervisor.getInvocationsForProject(project);

    usages = new ArrayList(results.size());
    for (int i = 0, max = results.size(); i < max; i++) {
      usages.add(new Integer(((InvocationData) results.get(i)).getLineNumber()));
    }

    assertTrue("Usage of class G in line 21", usages.remove(new Integer(21)));
    assertTrue("Usage of class G in line 28", usages.remove(new Integer(28)));
    assertTrue("Usage of class G in line 29", usages.remove(new Integer(29)));
    assertTrue("Usage of class G in line 31", usages.remove(new Integer(31)));
    assertTrue("Usage of class G in line 32", usages.remove(new Integer(32)));
    assertTrue("Usage of class G in line 34", usages.remove(new Integer(34)));
    assertTrue("Usage of class G in line 40", usages.remove(new Integer(40)));
    assertTrue("Usage of class G in line 109", usages.remove(new Integer(109)));

// OK
//    assertTrue("Usage of class G in line 131", usages.remove(new Integer(131)));
//    assertTrue("Usage of class G in line 133", usages.remove(new Integer(133)));
//    assertTrue("Usage of class G in line 133", usages.remove(new Integer(135)));

    assertTrue("Usage of class G in line 151", usages.remove(new Integer(151)));
    assertTrue("Usage of class G in line 153", usages.remove(new Integer(153)));
    assertTrue("Usage of class G in line 154", usages.remove(new Integer(154)));
    assertTrue("Usage of class G in line 154", usages.remove(new Integer(154)));
    assertTrue("Usage of class G in line 157", usages.remove(new Integer(157)));
    assertTrue("Usage of class G in line 166", usages.remove(new Integer(166)));
    assertTrue("Usage of class G in line 172", usages.remove(new Integer(172)));
    assertTrue("Usage of class G in line 175", usages.remove(new Integer(175)));
    assertTrue("Usage of class G in line 181", usages.remove(new Integer(181)));
    assertTrue("Usage of class G in line 184", usages.remove(new Integer(184)));
    assertTrue("Usage of class G in line 188", usages.remove(new Integer(188)));

// OK
//    assertTrue("Usage of class G in line 202", usages.remove(new Integer(202)));

    if (usages.size() > 0) {
      String extra = "";
      for (int i = 0, max = usages.size(); i < max; i++) {
        if (i > 0) {extra += ", ";
        }
        extra += usages.get(i);
      }
      assertTrue("Extra usages of G found in lines: " + extra, false);
    }

    cat.debug("Finding usages of type G - super");
    supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor, type, true, false);
    results = supervisor.getInvocationsForProject(project);

    usages = new ArrayList(results.size());
    for (int i = 0, max = results.size(); i < max; i++) {
      usages.add(new Integer(((InvocationData) results.get(i)).getLineNumber()));
    }

    assertTrue("Usage of class G in line 21", usages.remove(new Integer(21)));
    assertTrue("Usage of class G in line 28", usages.remove(new Integer(28)));
    assertTrue("Usage of class G in line 29", usages.remove(new Integer(29)));
    assertTrue("Usage of class G in line 31", usages.remove(new Integer(31)));
    assertTrue("Usage of class G in line 32", usages.remove(new Integer(32)));
    assertTrue("Usage of class G in line 34", usages.remove(new Integer(34)));
    assertTrue("Usage of class G in line 40", usages.remove(new Integer(40)));
    assertTrue("Usage of class G in line 109", usages.remove(new Integer(109)));
    assertTrue("Usage of class G in line 130", usages.remove(new Integer(130)));
    assertTrue("Usage of class G in line 131", usages.remove(new Integer(131)));
    assertTrue("Usage of class G in line 133", usages.remove(new Integer(133)));
    assertTrue("Usage of class G in line 133", usages.remove(new Integer(135)));
    assertTrue("Usage of class G in line 151", usages.remove(new Integer(151)));
    assertTrue("Usage of class G in line 153", usages.remove(new Integer(153)));
    assertTrue("Usage of class G in line 154", usages.remove(new Integer(154)));
    assertTrue("Usage of class G in line 154", usages.remove(new Integer(154)));
    assertTrue("Usage of class G in line 157", usages.remove(new Integer(157)));
    assertTrue("Usage of class G in line 166", usages.remove(new Integer(166)));
    assertTrue("Usage of class G in line 172", usages.remove(new Integer(172)));
    assertTrue("Usage of class G in line 175", usages.remove(new Integer(175)));
    assertTrue("Usage of class G in line 181", usages.remove(new Integer(181)));
    assertTrue("Usage of class G in line 184", usages.remove(new Integer(184)));
    assertTrue("Usage of class G in line 188", usages.remove(new Integer(188)));

    if (usages.size() > 0) {
      String extra = "";
      for (int i = 0, max = usages.size(); i < max; i++) {
        if (i > 0) {extra += ", ";
        }
        extra += usages.get(i);
      }
      assertTrue("Extra usages of G found in lines: " + extra, false);
    }

    cat.info("SUCCESS");
  }

  public void testUsageOnCastedNull() throws Exception {
    cat.info("Testing WhereUsed_UsageOnCastedNull");
    final Project project = Utils.createTestRbProject(
        Utils.getTestProjects().getProject("WhereUsed_usage_on_casted_null"));
    project.getProjectLoader().build();

    cat.debug("Finding usages of Test.test2 method");

    BinTypeRef testTypeRef = project.getTypeRefForName("Test");

    BinMethod testMethod = testTypeRef.getBinCIType()
        .getDeclaredMethod("test2", BinTypeRef.NO_TYPEREFS);

    ManagingIndexer supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, testMethod,
        new BinMethodSearchFilter(true, true));
    assertUsagesEqual("Usages of Test.test2() - sub/super",
        new int[] {2, 7}
        ,
        supervisor.getInvocationsForProject(project));

    supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, testMethod,
        new BinMethodSearchFilter(false, false));

    assertUsagesEqual("Usages of Test.test2()",
        new int[0],
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  /**
   * Tests bug #211: Where Used does not find usages of type inside
   * "instanceof" expression
   */
  public void testBug211() throws Exception {
    cat.info("Testing bug #211");
    final Project project = Utils.createTestRbProject(
        Utils.getTestProjects().getProject("bug211"));
    project.getProjectLoader().build();

    cat.debug("Finding usages of class A");

    final BinCIType test = project.getTypeRefForName("Test").getBinCIType();

    final ManagingIndexer supervisor = new ManagingIndexer();
    final BinCIType a = project.getTypeRefForName("A").getBinCIType();
    new TypeIndexer(supervisor, a, true, true);
    supervisor.visit(test.getCompilationUnit());
    assertUsagesEqual("Usages of class a in Test.java - sub/super",
        new int[] {7}
        ,
        supervisor.getInvocations());

    // FIXME: this one for reflection call forName(<typename>);
    /*    assertEquals("Line number of usage #1 of class A in Test.java",
                     new Integer(7),
                     ((InvocationData)results.get(1)).getLineNumber());*/

    cat.info("SUCCESS");
  }

  /**
   * Tests bug #212: Where Used doesn't find type usages in case type is
   * imported in "import" statement
   */
  public void testBug212() throws Exception {
    cat.info("Testing bug #212");
    final Project project = Utils.createTestRbProject(
        Utils.getTestProjects().getProject("bug211"));
    project.getProjectLoader().build();

    cat.debug("Finding usages of class A");

    final BinCIType test = project.getTypeRefForName("Test").getBinCIType();

    final ManagingIndexer supervisor = new ManagingIndexer();
    final BinCIType b = project.getTypeRefForName("a.B").getBinCIType();
    new TypeIndexer(supervisor, b, true, true);
    supervisor.visit(test.getCompilationUnit());
    assertUsagesEqual("Usages of class B in Test.java - sub/super",
        new int[] {1, 12}
        ,
        supervisor.getInvocations());

    cat.info("SUCCESS");
  }

  /**
   * Test bug #213: usage of method in invocation on interface
   */
  public void testBug213() throws Exception {
    cat.info("Testing bug #213");
    final Project project = Utils.createTestRbProject(
        Utils.getTestProjects().getProject("bug213"));
    project.getProjectLoader().build();

    cat.debug("Finding usages of Test.method() method");
    BinTypeRef testTypeRef = project.getTypeRefForName("Test");

    BinMethod testMethod = testTypeRef.getBinCIType()
        .getDeclaredMethod("method", BinTypeRef.NO_TYPEREFS);

    ManagingIndexer supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, testMethod,
        new BinMethodSearchFilter(true, true));
    assertUsagesEqual("Usages of Test.method - sub/super",
        new int[] {2, 7, 10, 13}
        ,
        supervisor.getInvocationsForProject(project));

    supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, testMethod,
        new BinMethodSearchFilter(false, false));
    assertUsagesEqual("Usages of Test.method",
        new int[0],
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  /**
   * Tests bug 256: Usages of constants from implemented interface.
   */
  public void testBug256() throws Exception {
    cat.info("Testing bug #256");
    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("bug256"));
    project.getProjectLoader().build();

    final BinTypeRef superType
        = project.getTypeRefForName("MyInterface");
    final BinField testField = superType.getBinCIType().getDeclaredField("a");

    final BinFieldInvocationExpression expr = new BinFieldInvocationExpression(
        testField, null, project.getTypeRefForName("MyClass"), null);

    ManagingIndexer supervisor = new ManagingIndexer();
    new FieldIndexer(supervisor, expr, false, false);
    assertUsagesEqual("Usages of MyClass - a",
        new int[] {7}
        ,
        supervisor.getInvocationsForProject(project));

    supervisor = new ManagingIndexer();
    new FieldIndexer(supervisor, expr, true, true);
    assertUsagesEqual("Usages of MyClass - a (sub/super)",
        new int[] {7, 13}
        ,
        supervisor.getInvocationsForProject(project));
    cat.info("SUCCESS");
  }

  /**
   * Tests bug 257: incorrectly finds usages of field coming from superclass.
   */
  public void testBug257() throws Exception {
    cat.info("Testing bug #257");
    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("bug257"));
    project.getProjectLoader().build();

    final BinTypeRef typeRef = project.getTypeRefForName("A");
    final BinField testField
        = typeRef.getBinCIType().getDeclaredField("b");

    final BinFieldInvocationExpression expr = new BinFieldInvocationExpression(
        testField, null, project.getTypeRefForName("B"), null);

    ManagingIndexer supervisor = new ManagingIndexer();
    new FieldIndexer(supervisor, expr, false, false);
    assertUsagesEqual("Usages of B - b",
        new int[] {10}
        ,
        supervisor.getInvocationsForProject(project));

    supervisor = new ManagingIndexer();
    new FieldIndexer(supervisor, expr, true, true);
    assertUsagesEqual("Usages of B - b",
        new int[] {4, 10}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  /**
   * Tests bug 258: incorrectly finds usages of static methods of parent inside
   * child.
   */
  public void testBug258() throws Exception {
    cat.info("Testing bug #258");
    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("bug258"));
    project.getProjectLoader().build();

    BinTypeRef testTypeRef = project.getTypeRefForName("A");

    BinMethod testMethod = testTypeRef.getBinCIType()
        .getDeclaredMethod("m", BinTypeRef.NO_TYPEREFS);

    // FIXME: it is a bit dangerous to create synthetic constructions here
    final BinMethodInvocationExpression expr
        = new BinMethodInvocationExpression(
        testMethod, null, null, project.getTypeRefForName("B"), null);

    ManagingIndexer supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, expr.getMethod(),
        expr.getInvokedOn().getBinCIType(),
        new BinMethodSearchFilter(false, false));
    assertUsagesEqual("Usages of B.m()",
        new int[] {3, 11}
        ,
        supervisor.getInvocationsForProject(project));

    supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, expr.getMethod(),
        expr.getInvokedOn().getBinCIType(),
        new BinMethodSearchFilter(true, true));
    assertUsagesEqual("Usages of B.m() (sub/super)",
        new int[] {3, 11}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  /**
   * Tests bug 259: incorrectly finds usages of unoverriden method of parent
   * used in child.
   */
  public void testBug259() throws Exception {
    cat.info("Testing bug #259");
    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("bug259"));
    project.getProjectLoader().build();

    BinTypeRef testTypeRef = project.getTypeRefForName("A");

    BinMethod testMethod = testTypeRef.getBinCIType()
        .getDeclaredMethod("m", BinTypeRef.NO_TYPEREFS);

    // FIXME: it is a bit dangerous to create synthetic constructions here
    final BinMethodInvocationExpression expr
        = new BinMethodInvocationExpression(
        testMethod, null, null, project.getTypeRefForName("B"), null);

    ManagingIndexer supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, expr.getMethod(),
        expr.getInvokedOn().getBinCIType(),
        new BinMethodSearchFilter(false, false));
    assertUsagesEqual("Usages of B.m()",
        new int[] {11}
        ,
        supervisor.getInvocationsForProject(project));

    supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, expr.getMethod(),
        expr.getInvokedOn().getBinCIType(),
        new BinMethodSearchFilter(true, true));
    assertUsagesEqual("Usages of B.m() (sub/super)",
        new int[] {3, 11}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  /**
   * Test for bug 262: fails to find usages of methods inherited from
   * super-super-interface or class
   */
  public void testBug262() throws Exception {
    cat.info("Testing bug #262");
    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("bug262"));
    project.getProjectLoader().build();

    BinCIType type
        = project.getTypeRefForName("B").getBinCIType();
    assertNotNull("class B found in project", type);

    cat.debug("Finding usages of type B - sub/super");
    ManagingIndexer supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor, type, true, true);
    List results = supervisor.getInvocationsForProject(project);

    List usages = new ArrayList(results.size());
    for (int i = 0, max = results.size(); i < max; i++) {
      usages.add(new Integer(((InvocationData) results.get(i)).getLineNumber()));
    }

    assertTrue("Usage of class B in line 14", usages.remove(new Integer(14)));
    assertTrue("Usage of class B in line 17", usages.remove(new Integer(17)));

    if (usages.size() > 0) {
      String extra = "";
      for (int i = 0, max = usages.size(); i < max; i++) {
        if (i > 0) {extra += ", ";
        }
        extra += usages.get(i);
      }
      assertTrue("Extra usages of B found in lines: " + extra, false);
    }

    cat.debug("Finding usages of type B");
    supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor, type, false, false);
    results = supervisor.getInvocationsForProject(project);

    usages = new ArrayList(results.size());
    for (int i = 0, max = results.size(); i < max; i++) {
      usages.add(new Integer(((InvocationData) results.get(i)).getLineNumber()));
    }

    assertTrue("Usage of class B in line 14", usages.remove(new Integer(14)));
    assertTrue("Usage of class B in line 17", usages.remove(new Integer(17)));

    if (usages.size() > 0) {
      String extra = "";
      for (int i = 0, max = usages.size(); i < max; i++) {
        if (i > 0) {extra += ", ";
        }
        extra += usages.get(i);
      }
      assertTrue("Extra usages of B found in lines: " + extra, false);
    }

    cat.info("SUCCESS");
  }

  /**
   * Test for owner class's method used in inner class.
   */
  public void testOuterMethodInInner() throws Exception {
    cat.info("Testing for owner class's method used in inner class");
    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("WU/owner_method_in_inner"));
    project.getProjectLoader().build();

    final BinCIType test =
        project.getTypeRefForName("Test").getBinCIType();
    assertNotNull("Class Test found in project", test);

    final BinMethod doSomething =
        test.getAccessibleMethods("doSomething", test)[0];
    cat.debug("Finding usages of method Test.doSomething in Test.Inner");
    ManagingIndexer supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, doSomething,
        new BinMethodSearchFilter(true, true));
    supervisor.visit(test.getDeclaredType("Inner").getBinCIType());
    assertUsagesEqual("Usages of Test.doSomething in Test.Inner",
        new int[] {9, 10, 11}
        ,
        supervisor.getInvocations());

    final BinMethod doSomething2 =
        test.getAccessibleMethods("doSomething2", test)[0];
    cat.debug("Finding usages of method Test.doSomething2 in Test.Inner");
    supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, doSomething2,
        new BinMethodSearchFilter(true, true));
    supervisor.visit(test.getDeclaredType("Inner").getBinCIType());
    assertUsagesEqual("Usages of Test.doSomething2 in Test.Inner",
        new int[] {14, 15}
        ,
        supervisor.getInvocations());

    cat.info("SUCCESS");
  }

  /**
   * Test for owner class's field used in inner class.
   */
  public void testOuterFieldInInner() throws Exception {
    cat.info("Testing for owner class's field used in inner class");
    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("WU/owner_field_in_inner"));
    project.getProjectLoader().build();

    final BinCIType test =
        project.getTypeRefForName("Test").getBinCIType();
    assertNotNull("Class Test found in project", test);

    final BinField a = test.getAccessibleField("a", test);

    cat.debug("Finding usages of field Test.a");
    ManagingIndexer supervisor = new ManagingIndexer();
    new FieldIndexer(supervisor, a, true);
    assertUsagesEqual("Usages of Test.a in Test.Inner (sub)",
        new int[] {11, 17, 23, 24, 25, 35}
        ,
        supervisor.getInvocationsForProject(project));

    final BinField b = test.getAccessibleField("b", test);

    cat.debug("Finding usages of field Test.b in Test.Inner");
    supervisor = new ManagingIndexer();
    new FieldIndexer(supervisor, b, true);
    supervisor.visit(test.getDeclaredType("Inner").getBinCIType());
    assertUsagesEqual("Usages of Test.b in Test.Inner (sub)",
        new int[] {27, 28}
        ,
        supervisor.getInvocations());

    cat.info("SUCCESS");
  }

  /**
   * Test Where Used for A.field.
   */
  public void testFieldOfA() throws Exception {
    cat.info("Testing fields of A.field");
    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("WU/field"));
    project.getProjectLoader().build();

    final BinCIType a =
        project.getTypeRefForName("A").getBinCIType();
    assertNotNull("Class A found in project", a);

    final BinField aField = a.getAccessibleField("field", a);

    cat.debug("Finding usages of field A.field");
    ManagingIndexer supervisor = new ManagingIndexer();
    new FieldIndexer(supervisor, aField, false, a.getTypeRef(), false);
    assertUsagesEqual("Usages of A.field (false/false)",
        new int[] {6, 10, 11, 12, 19, 20, 21, 28, 33, 50, 69, 81}
        ,
        supervisor.getInvocationsForProject(project));

    cat.debug("Finding usages of field A.field");
    supervisor = new ManagingIndexer();
    new FieldIndexer(supervisor, aField, false, a.getTypeRef(), true);
    assertUsagesEqual("Usages of A.field (false/true)",
        new int[] {6, 10, 11, 12, 19, 20, 21, 28, 33, 50, 69, 81}
        ,
        supervisor.getInvocationsForProject(project));

    cat.debug("Finding usages of field A.field");
    supervisor = new ManagingIndexer();
    new FieldIndexer(supervisor, aField, true, a.getTypeRef(), false);
    assertUsagesEqual("Usages of A.field (true/false)",
        new int[] {6, 10, 11, 12, 18, 19, 20, 21, 27, 28, 33, 34,
        41, 42, 46, 47, 48, 49, 50, 60, 67, 68, 69, 79,
        80, 81}
        ,
        supervisor.getInvocationsForProject(project));

    cat.debug("Finding usages of field A.field");
    supervisor = new ManagingIndexer();
    new FieldIndexer(supervisor, aField, true, a.getTypeRef(), true);
    assertUsagesEqual("Usages of A.field (true/true)",
        new int[] {6, 10, 11, 12, 18, 19, 20, 21, 27, 28, 33, 34,
        41, 42, 46, 47, 48, 49, 50, 60, 67, 68, 69, 79,
        80, 81}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  /**
   * Test Where Used for A.field invoked on B.
   */
  public void testFieldOfB() throws Exception {
    cat.info("Testing fields of A.field invoked on B");
    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("WU/field"));
    project.getProjectLoader().build();

    final BinCIType a =
        project.getTypeRefForName("A").getBinCIType();
    assertNotNull("Class A found in project", a);

    final BinCIType b =
        project.getTypeRefForName("B").getBinCIType();
    assertNotNull("Class B found in project", b);

    final BinField aField = a.getAccessibleField("field", a);

    cat.debug("Finding usages of field B.field");
    ManagingIndexer supervisor = new ManagingIndexer();
    new FieldIndexer(supervisor, aField, false, b.getTypeRef(), false);
    assertUsagesEqual("Usages of B.field (false/false)",
        new int[] {18, 27, 34, 42, 48, 49, 68}
        ,
        supervisor.getInvocationsForProject(project));

    cat.debug("Finding usages of field B.field");
    supervisor = new ManagingIndexer();
    new FieldIndexer(supervisor, aField, true, b.getTypeRef(), false);
    assertUsagesEqual("Usages of B.field (true/false)",
        new int[] {18, 27, 34, 41, 42, 46, 47, 48, 49, 60, 67, 68,
        79, 80}
        ,
        supervisor.getInvocationsForProject(project));

    cat.debug("Finding usages of field B.field");
    supervisor = new ManagingIndexer();
    new FieldIndexer(supervisor, aField, false, b.getTypeRef(), true);
    assertUsagesEqual("Usages of B.field (false/true)",
        new int[] {18, 27, 34, 42, 48, 49, 68,
        6, 10, 11, 12, 19, 20, 21, 28, 33, 50, 69, 81}
        ,
        supervisor.getInvocationsForProject(project));

    cat.debug("Finding usages of field B.field");
    supervisor = new ManagingIndexer();
    new FieldIndexer(supervisor, aField, true, b.getTypeRef(), true);
    assertUsagesEqual("Usages of B.field (true/true)",
        new int[] {6, 10, 11, 12, 18, 19, 20, 21, 27, 28, 33, 34,
        41, 42, 46, 47, 48, 49, 50, 60, 67, 68, 69, 79,
        80, 81}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  /**
   * Test Where Used for A.field invoked on C.
   */
  public void testFieldOfC() throws Exception {
    cat.info("Testing fields of A.field invoked on C");
    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("WU/field"));
    project.getProjectLoader().build();

    final BinCIType a =
        project.getTypeRefForName("A").getBinCIType();
    assertNotNull("Class A found in project", a);

    final BinCIType c =
        project.getTypeRefForName("C").getBinCIType();
    assertNotNull("Class C found in project", c);

    final BinField aField = a.getAccessibleField("field", a);

    cat.debug("Finding usages of field C.field");
    ManagingIndexer supervisor = new ManagingIndexer();
    new FieldIndexer(supervisor, aField, false, c.getTypeRef(), false);
    assertUsagesEqual("Usages of C.field (false/false)",
        new int[] {41, 47, 46, 60, 67, 79, 80}
        ,
        supervisor.getInvocationsForProject(project));

    cat.debug("Finding usages of field C.field");
    supervisor = new ManagingIndexer();
    new FieldIndexer(supervisor, aField, false, c.getTypeRef(), true);
    assertUsagesEqual("Usages of C.field (false/true)",
        new int[] {41, 47, 46, 60, 67, 79, 80,
        18, 27, 34, 42, 48, 49, 68,
        6, 10, 11, 12, 19, 20, 21, 28, 33, 50, 69, 81}
        ,
        supervisor.getInvocationsForProject(project));

    cat.debug("Finding usages of field C.field");
    supervisor = new ManagingIndexer();
    new FieldIndexer(supervisor, aField, true, c.getTypeRef(), false);
    assertUsagesEqual("Usages of C.field (true/false)",
        new int[] {41, 47, 46, 60, 67, 79, 80}
        ,
        supervisor.getInvocationsForProject(project));

    cat.debug("Finding usages of field C.field");
    supervisor = new ManagingIndexer();
    new FieldIndexer(supervisor, aField, true, c.getTypeRef(), true);
    assertUsagesEqual("Usages of C.field (true/true)",
        new int[] {6, 10, 11, 12, 18, 19, 20, 21, 27, 28, 33, 34,
        41, 42, 46, 47, 48, 49, 50, 60, 67, 68, 69, 79,
        80, 81}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  public void testSubpackages() throws Exception {
    cat.info("Testing usages of upper_package without child packages");

    final Project project = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit("package test;import upperpackage.*; import upperpackage.subpackage.*; class A{}",
        "A.java", "test"),
        new Utils.TempCompilationUnit("package upperpackage;class B{}", "B.java",
        "upperpackage"),
        new Utils.TempCompilationUnit("package upperpackage.subpackage;class C{}",
        "C.java", "upperpackage.subpackage")
    });
    BinPackage upperPackage = project.getPackageForName("upperpackage");

    ManagingIndexer supervisor = new ManagingIndexer();

    // Exclude subpackages
    new PackageIndexer(supervisor, upperPackage, true, true, false);
    assertUsagesEqual("Usages of upper_package",
        new int[] {1, 1}
        ,
        supervisor.getInvocationsForProject(project));

    // Include subpackages
    supervisor = new ManagingIndexer();
    new PackageIndexer(supervisor, upperPackage, true, true, true);
    assertUsagesEqual("Usages of upper_package",
        new int[] {1, 1, 1, 1}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCESS");
  }

  public void testSubClassesWithMethods() throws Exception {
    cat.info("Testing usages of overriden methods");

    final Project project = Utils.createTestRbProjectFromString(
        "public class X {\n" +
        "  public void method() {}\n" +
        "}\n " +
        "class XSub extends X { \n" +
        "  public void method() {} \n" +
        "} \n" +
        "class User {{\n" +
        "  new XSub().method();\n" +
        "}} \n",
        "X.java",
        null
        );

    assertTrue("No bugs in the project",
        !(project.getProjectLoader().getErrorCollector()).getUserFriendlyErrors().hasNext());

    BinClass x = (BinClass) project.getTypeRefForName("X").getBinType();
    BinMethod method = x.getDeclaredMethod("method", BinTypeRef.NO_TYPEREFS);

    ManagingIndexer supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, method,
        new BinMethodSearchFilter(true, true));

    assertUsagesEqual("Usages of overriden method",
        new int[] {5}
        , // we test implementation usage here
        supervisor.getInvocationsForProject(project));

    x = (BinClass) project.getTypeRefForName("XSub").getBinType();
    method = x.getDeclaredMethod("method", BinTypeRef.NO_TYPEREFS);

    supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, method,
        new BinMethodSearchFilter(true, true));

    assertUsagesEqual("Usages of sub method",
        new int[] {2, 8}
        , // we test implementation usage here
        supervisor.getInvocationsForProject(project));

    cat.info("SUCESS");
  }

  public void testBug1770() throws Exception {
    Project project = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
        "package a;\n\n" +
        "public class X {\n" +
        "  Object o = new I();  \n" + // parsing error -- I is an interface
        "                       \n" +
        "  public interface I{} \n" +
        "}",
        "X.java", null)});

    BinCIType type = project.getTypeRefForName("a.X").getBinCIType();
    ManagingIndexer supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor, type, false, false);

    assertUsagesEqual("Should cause parsing error, NOT a general exception",
        new int[0], supervisor.getInvocationsForProject(project));

    assertTrue("The interface initiation must be an parsing error",
        (project.getProjectLoader().getErrorCollector()).hasUserFriendlyErrors());
  }

  /**
   * Test Where Used for D.field.
   */
  public void testFieldOfD() throws Exception {
    cat.info("Testing fields of D.field");
    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("WU/field"));
    project.getProjectLoader().build();

    final BinCIType d =
        project.getTypeRefForName("D").getBinCIType();
    assertNotNull("Class D found in project", d);

    final BinField dField = d.getAccessibleField("field", d);

    cat.debug("Finding usages of field D.field");
    ManagingIndexer supervisor = new ManagingIndexer();
    new FieldIndexer(supervisor, dField, false, d.getTypeRef(), false);
    assertUsagesEqual("Usages of D.field (false/false)",
        new int[] {59, 65, 66, 78}
        ,
        supervisor.getInvocationsForProject(project));

    cat.debug("Finding usages of field D.field");
    supervisor = new ManagingIndexer();
    new FieldIndexer(supervisor, dField, false, d.getTypeRef(), true);
    assertUsagesEqual("Usages of D.field (false/true)",
        new int[] {59, 65, 66, 78}
        ,
        supervisor.getInvocationsForProject(project));

    cat.debug("Finding usages of field D.field");
    supervisor = new ManagingIndexer();
    new FieldIndexer(supervisor, dField, true, d.getTypeRef(), false);
    assertUsagesEqual("Usages of D.field (true/false)",
        new int[] {59, 65, 66, 78}
        ,
        supervisor.getInvocationsForProject(project));

    cat.debug("Finding usages of field D.field");
    supervisor = new ManagingIndexer();
    new FieldIndexer(supervisor, dField, true, d.getTypeRef(), true);
    assertUsagesEqual("Usages of D.field (true/true)",
        new int[] {59, 65, 66, 78}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  /**
   * Test Where Used for D.Inner2.field.
   */
  public void testFieldOfDInner2() throws Exception {
    cat.info("Testing fields of D.Inner2.field");
    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("WU/field"));
    project.getProjectLoader().build();

    final BinCIType d =
        project.getTypeRefForName("D").getBinCIType();
    assertNotNull("Class D found in project", d);
    final BinCIType inner2 = d.getDeclaredType("Inner2").getBinCIType();
    assertNotNull("Class D.Inner2 found in project", inner2);

    final BinField field = inner2.getAccessibleField("field", inner2);

    cat.debug("Finding usages of field D.Inner2.field");
    ManagingIndexer supervisor = new ManagingIndexer();
    new FieldIndexer(supervisor, field, false, inner2.getTypeRef(), false);
    assertUsagesEqual("Usages of D.Inner2.field (false/false)",
        new int[] {76, 77}
        ,
        supervisor.getInvocationsForProject(project));

    cat.debug("Finding usages of field D.Inner2.field");
    supervisor = new ManagingIndexer();
    new FieldIndexer(supervisor, field, false, inner2.getTypeRef(), true);
    assertUsagesEqual("Usages of D.Inner2.field (false/true)",
        new int[] {76, 77}
        ,
        supervisor.getInvocationsForProject(project));

    cat.debug("Finding usages of field D.Inner2.field");
    supervisor = new ManagingIndexer();
    new FieldIndexer(supervisor, field, true, inner2.getTypeRef(), false);
    assertUsagesEqual("Usages of D.Inner2.field (true/false)",
        new int[] {76, 77}
        ,
        supervisor.getInvocationsForProject(project));

    cat.debug("Finding usages of field D.Inner2.field");
    supervisor = new ManagingIndexer();
    new FieldIndexer(supervisor, field, true, inner2.getTypeRef(), true);
    assertUsagesEqual("Usages of D.Inner2.field (true/true)",
        new int[] {76, 77}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  public void testMultiSelection1() throws Exception {
    Finder.clearInvocationMap();

    cat.info("Testing WhereUsed of multi selection");

    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("WhereUsed_multi_selection"));
    project.getProjectLoader().build();

    cat.debug("Finding usages of field tmp1");

    BinCIType binCI = project.getTypeRefForName("test1").
        getBinCIType();

    Object[] arrObj = new Object[2];
    arrObj[0] = binCI.getAccessibleField("tmp1", binCI);
    arrObj[1] = binCI.getAccessibleField("tmp2", binCI);

    assertUsagesEqual("Usages of fields: tmp1, tmp2",
        new int[] {7, 8, 16}
        ,
        Finder.getInvocations(project, arrObj, new SimpleFilter(true, false, false)));

    cat.info("SUCCESS");
  }

  /**
   * Asserts that usages found are as expected.
   *
   * @param expectedLines line numbers of expected usages.
   * @param actualUsages usages found ({@link InvocationData} instances).
   */
  private void assertUsagesEqual(String message,
      int[] expectedLines,
      List actualUsages) {
    final List actual = new ArrayList();
    for (final Iterator i = actualUsages.iterator(); i.hasNext(); ) {
      final InvocationData usage = (InvocationData) i.next();
      actual.add(new Integer(usage.getLineNumber()));
    }

    final List expected = new ArrayList();
    for (int i = 0, len = expectedLines.length; i < len; i++) {
      expected.add(new Integer(expectedLines[i]));
    }

    Collections.sort(actual);
    Collections.sort(expected);

    assertEquals(message, expected, actual);
  }

  /**
   * Test for usage of package formed by binary class.
   */
  public void testBug1686() throws Exception {
    cat.info("Testing for usage of package formed by binary class");
    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("WU/bug1686"));
    project.getProjectLoader().build();
    new AbstractIndexer().visit(project);

    final BinPackage z = project.getPackageForName("z");
    assertNotNull("Package z found in project", z);

    cat.debug("Finding usages of package z");
    ManagingIndexer supervisor = new ManagingIndexer();
    new PackageIndexer(supervisor, z, false, false);
    assertUsagesEqual("Usages of z",
        new int[] {3, 8, 8}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  public void testInvocation_in_super_private() throws Exception {
    cat.info("Testing of invocation in private method of super");
    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("WU/invocation_in_super_private"));
    project.getProjectLoader().build();

    final BinTypeRef typeRef = project.getTypeRefForName("Class2");
    final BinMethod method = ((BinClass) typeRef.getBinType()).
        getDeclaredMethods()[0];
    assertNotNull("Method f1 found in project", method);

    cat.debug("Finding usages of method f1");
    ManagingIndexer supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, method,
        new BinMethodSearchFilter(true, false, true, true, true, true, true, false, false));
    assertUsagesEqual("Usages of f1",
        new int[] {6, 7}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  public void testTest1() throws Exception {
    cat.info(
        "Testing of invocation of interface method in private method of sub");
    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("WU/test1"));
    project.getProjectLoader().build();

    final BinTypeRef typeRef = project.getTypeRefForName("Class1");
    final BinMethod method = ((BinClass) typeRef.getBinType()).
        getDeclaredMethods()[0];
    assertNotNull("Method f1 found in project", method);

    cat.debug("Finding usages of method f1");
    ManagingIndexer supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, method, new BinMethodSearchFilter(true, false, true, true, true, true, false, false, false));
    assertUsagesEqual("Usages of f1",
        new int[] {6, 7}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  public void testTest2() throws Exception {
    cat.info(
        "Testing of not invocation of implementation method in private method of sub");
    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("WU/test2"));
    project.getProjectLoader().build();

    final BinTypeRef typeRef = project.getTypeRefForName("Class1");
    final BinMethod method = ((BinClass) typeRef.getBinType()).
        getDeclaredMethods()[0];
    assertNotNull("Method f1 found in project", method);

    cat.debug("Finding usages of method f1");
    ManagingIndexer supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, method, new BinMethodSearchFilter(true, false, true, true, true, true, true, false, false));
    assertUsagesEqual("Usages of f1",
        new int[] {6}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  public void testBug1941() throws Exception {
    cat.info("Testing for bug 1941");
    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("WU/bug1941"));
    project.getProjectLoader().build();

    final BinTypeRef type = project.getTypeRefForName("Test");
    assertNotNull("Type Test found in project", type);

    BinMethod method = type.getBinCIType().getDeclaredMethod(
        "method", new BinTypeRef[] {project.objectRef});

    cat.debug("Finding usages of method 'method(Object)'");
    ManagingIndexer supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, method,
        new BinMethodSearchFilter(true, true));
    assertUsagesEqual("Usages of method(Object)",
        new int[0],
        supervisor.getInvocationsForProject(project));

    BinTypeRef objectArray = project.getTypeRefForName("[Ljava.lang.Object;");
    assertNotNull("Type Object[] found in project", objectArray);

    method = type.getBinCIType().getDeclaredMethod(
        "method", new BinTypeRef[] {objectArray});
    cat.debug("Finding usages of method 'method(Object[])'");
    supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, method,
        new BinMethodSearchFilter(true, true));
    assertUsagesEqual("Usages of method(Object[])",
        new int[] {5}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  public void testInterfaceMethodUsage() throws Exception {
    Project project = Utils.createTestRbProjectFromString(
        new Utils.TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
        "public class X implements Y {\n" +
        "  {  new X().method(); }\n" + // here Y.method used as interface only
        "  public void method() {}\n" +
        "}\n" +
        "interface Y {\n" +
        "  void method();\n" +
        "}\n",
        "X.java", null)});

    BinCIType type = project.getTypeRefForName("Y").getBinCIType();
    BinMethod method = type.getDeclaredMethod("method", BinTypeRef.NO_TYPEREFS);

    BinMethodSearchFilter filter = new BinMethodSearchFilter(
        true, false, true, true, true, false, false, false, false);
    ManagingIndexer supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, method, filter);

    assertUsagesEqual("Usages of Y.method",
        new int[] {2}
        ,
        supervisor.getInvocationsForProject(project));

    filter = new BinMethodSearchFilter(
        true, false, true, true, true, false, true, false, false);
    supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, method, filter);

    assertUsagesEqual("Usages of Y.method",
        new int[0],
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  public void testNotOverridenMethodUsage() throws Exception {
    Project project = Utils.createTestRbProjectFromString(
        new Utils.TempCompilationUnit[] {
        new Utils.TempCompilationUnit(
        "public class X {\n" +
        "  public void method() {}\n" +
        "}\n" +
        "class Y extends X {\n" +
        "  { new Y().method(); }\n" +
        "}\n",
        "X.java", null)});

    BinCIType type = project.getTypeRefForName("X").getBinCIType();
    BinMethod method = type.getDeclaredMethod("method", BinTypeRef.NO_TYPEREFS);

    BinMethodSearchFilter filter = new BinMethodSearchFilter(
        true, false, true, true, true, false, false, false, false);
    ManagingIndexer supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, method, filter);

    assertUsagesEqual("Usages of Y.method",
        new int[] {5}
        ,
        supervisor.getInvocationsForProject(project));

    filter = new BinMethodSearchFilter(
        true, false, true, true, true, false, true, false, false);
    supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, method, filter);

    assertUsagesEqual("Usages of Y.method",
        new int[] {5}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  public void testBug1957() throws Exception {
    cat.info("Testing for bug 1957");
    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("WU/bug1957"));
    project.getProjectLoader().build();

    final BinTypeRef type = project.getTypeRefForName("Test");
    assertNotNull("Type Test found in project", type);

    BinMethod method = type.getBinCIType().getDeclaredMethod(
        "method", BinTypeRef.NO_TYPEREFS);
    BinLocalVariable var
        = (BinLocalVariable) ((BinVariableDeclaration) method
        .getBody().getStatements()[0]).getVariables()[0];

    cat.debug("Finding usages of variable 'field'");
    ManagingIndexer supervisor = new ManagingIndexer();
    new LocalVariableIndexer(supervisor, var);
    assertUsagesEqual("Usages of variable 'field'",
        new int[0],
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  public void testAnotherBranch() throws Exception {
    cat.info("Testing of invocation in another branch of inheritance");
    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("WU/another_branch"));
    project.getProjectLoader().build();

    final BinTypeRef typeRef = project.getTypeRefForName("FirstBranch");
    final BinMethod method
        = ((BinClass) typeRef.getBinType()).getDeclaredMethod("method",
        BinParameter.NO_PARAMS);
    assertNotNull("Method 'method' found in project", method);

    cat.debug("Finding usages of method 'method'");
    ManagingIndexer supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, method,
        new BinMethodSearchFilter(true, true, true, true, true, false, false, false, false));
    assertUsagesEqual("Usages of method (super/sub)",
        new int[] {3, 4, 5, 6, 8}
        ,
        supervisor.getInvocationsForProject(project));

    supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, method,
        new BinMethodSearchFilter(true, true, true, false, true, false, false, false, false));
    assertUsagesEqual("Usages of method (super)",
        new int[] {3, 4, 6}
        ,
        supervisor.getInvocationsForProject(project));

    supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, method,
        new BinMethodSearchFilter(true, true, false, false, true, false, false, false, false));
    assertUsagesEqual("Usages of method",
        new int[] {4}
        ,
        supervisor.getInvocationsForProject(project));

    supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, method,
        new BinMethodSearchFilter(true, true, true, true, true, false, true, false, false));
    assertUsagesEqual("Usages of method (super/sub/impl)",
        new int[] {3, 4, 6}
        ,
        supervisor.getInvocationsForProject(project));

    final BinMethod anotherMethod
        = ((BinClass) typeRef.getBinType()).getDeclaredMethod("anotherMethod",
        BinParameter.NO_PARAMS);
    assertNotNull("Method 'anotherMethod' found in project", anotherMethod);

    supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, anotherMethod,
        new BinMethodSearchFilter(true, true, true, true, true, false, false, false, false));
    assertUsagesEqual("Usages of anotherMethod (super/sub)",
        new int[0],
        supervisor.getInvocationsForProject(project));

    final BinMethod thirdMethod
        = ((BinClass) typeRef.getBinType()).getDeclaredMethod("thirdMethod",
        BinParameter.NO_PARAMS);
    assertNotNull("Method 'thirdMethod' found in project", thirdMethod);

    supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, thirdMethod,
        new BinMethodSearchFilter(true, true, true, true, true, false, false, false, false));
    assertUsagesEqual("Usages of thirdMethod",
        new int[0],
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  public void testMethodInvocations() throws Exception {
    cat.info("Testing of method invocation all possible casses");
    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("WU/method_invocations"));
    project.getProjectLoader().build();

    final BinTypeRef typeRef = project.getTypeRefForName("Main");
    final BinMethod method
        = ((BinClass) typeRef.getBinType()).getDeclaredMethod("method",
        BinParameter.NO_PARAMS);
    assertNotNull("Method 'method' found in project", method);

    cat.debug("Finding usages of method 'method'");

    ManagingIndexer supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, method,
        new BinMethodSearchFilter(true, true, true, true, true, false, false, false, false));
    assertUsagesEqual("Usages of method (super/sub/inh)",
        new int[] {2, 3, 5, 6, 7, 8, 9, 10, 16, 17, 23, 24, 30, 31, 36, 37, 38,
        43, 44, 45}
        ,
        supervisor.getInvocationsForProject(project));

    supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, method,
        new BinMethodSearchFilter(true, true, true, true, true, false, true, false, false));
    assertUsagesEqual("Usages of method (super/sub/impl)",
        new int[] {2, 3, 5, 6, 7, 8, 16, 23, 30, 31, 36, 38}
        ,
        supervisor.getInvocationsForProject(project));

    supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, method,
        new BinMethodSearchFilter(true, true, true, false, true, false, false, false, false));
    assertUsagesEqual("Usages of method (super/inh)",
        new int[] {2, 3, 5, 6, 7, 16, 17, 23, 24, 31, 38, 45}
        ,
        supervisor.getInvocationsForProject(project));

    supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, method,
        new BinMethodSearchFilter(true, true, true, false, true, false, true, false, false));
    assertUsagesEqual("Usages of method (super/impl)",
        new int[] {2, 3, 5, 6, 7, 16, 23, 31, 38}
        ,
        supervisor.getInvocationsForProject(project));

    supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, method,
        new BinMethodSearchFilter(true, true, false, true, true, false, false, false, false));
    assertUsagesEqual("Usages of method (sub/inh)",
        new int[] {7, 8, 9, 23, 30, 31, 36, 37, 38}
        ,
        supervisor.getInvocationsForProject(project));

    supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, method,
        new BinMethodSearchFilter(true, true, false, true, true, false, true, false, false));
    assertUsagesEqual("Usages of method (sub/impl)",
        new int[] {7, 8, 23, 30, 31, 36, 38}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }

  public void testWildcardsBug() throws Exception {
    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("WhereUsed_wildcards"));
    project.getProjectLoader().build();

    cat.debug("Finding usages of class MyPrimitiveType");
    ManagingIndexer supervisor = new ManagingIndexer();
    new TypeIndexer(supervisor,
        project.getTypeRefForName("WhereUsedWildcards.MyPrimitiveType").getBinCIType(),
        true, true);
    assertUsagesEqual("Usages of class MyPrimitiveType",
        new int[] {6, 9, 16, 17, 26, 30, 30, 34, 34}
        ,
        supervisor.getInvocationsForProject(project));

    cat.info("SUCCESS");
  }
  
  public void testLabels() throws Exception {
    cat.info("Testing of invocation in private method of super");
    final Project project =
        Utils.createTestRbProject(
        Utils.getTestProjects().getProject("WhereUsed_labels"));
    project.getProjectLoader().build();
    
    cat.debug("Finding usages of class MyPrimitiveType");
    ManagingIndexer supervisor = new ManagingIndexer();
    BinTypeRef typeRef = project.getTypeRefForName("WhereUsed.labels.A");
    
    BinLabeledStatement statement = findLabeledStatementForName(typeRef.getBinCIType(),"loop1");
    new LabelIndexer(supervisor, statement);
    
    assertUsagesEqual("Usages of loop1 label",
        new int[] {10,21,23},
        supervisor.getInvocationsForProject(project));
    
    cat.info("SUCCESS");
  }
  
  // for rough tests only
  private BinLabeledStatement findLabeledStatementForName(BinItemVisitable target, final String name) {
    LabelNameIndexer visitor = new LabelNameIndexer(name); 
    target.accept(visitor);
    return (BinLabeledStatement)visitor.getResults().get(0);
  }
  
  private class LabelNameIndexer extends BinItemVisitor {
    private List found = new ArrayList();
    private String name;
    public LabelNameIndexer(String name) {
      this.name = name;
    }
    
    public void visit(BinLabeledStatement statement) {
      if(name.equals(statement.getLabelIdentifierName())) {
        found.add(statement);
      }
      super.visit(statement);
    }
    
    public List getResults() {
      return found;
    }
  }
}
