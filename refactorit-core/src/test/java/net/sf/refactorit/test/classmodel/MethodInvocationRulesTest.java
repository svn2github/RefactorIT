/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.classmodel;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariableArityParameter;
import net.sf.refactorit.classmodel.MethodInvocationRules;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.parser.FastJavaLexer;
import net.sf.refactorit.test.Utils;

import org.apache.log4j.Category;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/** Test driver for {@link MethodInvocationRules}. */
public final class MethodInvocationRulesTest extends TestCase {

  private static final Category cat =
      Category.getInstance(MethodInvocationRulesTest.class.getName());

  public MethodInvocationRulesTest(final String name) {
    super(name);
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite(MethodInvocationRulesTest.class);
    suite.setName("MethodInvocationRules tests");
    return suite;
  }

  /**
   * Test whether getInvokedType properly fails if invoked method cannot be
   * found.
   */
  public final void testGetInvokedNoMethodFound() {
    cat.info("Test whether getInvokedType properly fails if invoked method"
        + " cannot be found");

    // No such method at all
    assertNull("no type for test() #1",
        MethodInvocationRules.getTypeForDotlessInvocation(Utils.createClass("A"), "test"));

    {
      // Method is private in superclass
      final BinCIType a = Utils.createClass("A");
      final BinCIType b = Utils.createClass("B", a);
      final BinMethod test =
          new BinMethod("test",
          BinParameter.NO_PARAMS,
          a.getTypeRef(),
          BinModifier.PRIVATE,
          BinMethod.Throws.NO_THROWS);
      a.addDeclaredMethod(test);

      assertNull("no type for test() #2",
          MethodInvocationRules.getTypeForDotlessInvocation(b, "test"));
    }

    {
      final BinCIType a = Utils.createClass("abc.A");
      final BinCIType b = Utils.createClass("def.B", a);
      final BinCIType c = Utils.createClass("ghi.C", b);
      final BinMethod test =
          new BinMethod("test",
          BinParameter.NO_PARAMS,
          a.getTypeRef(),
          BinModifier.PACKAGE_PRIVATE,
          BinMethod.Throws.NO_THROWS);
      a.addDeclaredMethod(test);

      assertNull("no type for test() #3", MethodInvocationRules.getTypeForDotlessInvocation(c,
          "test"));
    }

    cat.info("SUCCESS");
  }

  /**
   * Tests whether getInvokedType works when method declared in the
   * type is invoked.
   * */
  public final void testGetInvokedTypeSameType() {
    cat.info("Testing whether getInvokedType works when method declared in"
        + " the type is invoked");

    final BinCIType a = Utils.createClass("A");
    final BinMethod test =
        new BinMethod("test",
        BinParameter.NO_PARAMS,
        a.getTypeRef(),
        0,
        BinMethod.Throws.NO_THROWS);
    a.addDeclaredMethod(test);

    assertEquals("invoked type for test()",
        a,
        MethodInvocationRules.getTypeForDotlessInvocation(a, "test"));

    cat.info("SUCCESS");
  }

  /**
   * Tests whether getInvokedType works when method is declared in superclass.
   */
  public final void testGetInvokedTypeSuperclass() {
    cat.info(
        "Testing whether getInvokedType works when method is declared in"
        + " superclass");

    final BinCIType a = Utils.createClass("abc.A");
    final BinCIType b = Utils.createClass("def.B", a);
    final BinCIType c = Utils.createClass("ghi.C", b);
    final BinMethod test =
        new BinMethod("test",
        BinParameter.NO_PARAMS,
        a.getTypeRef(),
        BinModifier.PROTECTED,
        BinMethod.Throws.NO_THROWS);
    a.addDeclaredMethod(test);

    assertTrue("test method accessible from ghi.C",
        test.isAccessible(test.getOwner().getBinCIType(), c));

    assertEquals("type for test()",
        c,
        MethodInvocationRules.getTypeForDotlessInvocation(c, "test"));

    cat.info("SUCCESS");
  }

  /**
   * Tests whether getInvokedType works when method is declared in
   * superinterface.
   */
  public final void testGetInvokedTypeSuperinterface() {
    cat.info(
        "Testing whether getInvokedType works when method is declared in"
        + " superinterface");

    Utils.createFakeProject();

    final BinCIType a = Utils.createClass("A");
    a.setModifiers(BinModifier.ABSTRACT);

    final BinCIType supI = Utils.createInterface("SupI");

    final BinCIType i = Utils.createInterface("I");

    i.getTypeRef().setInterfaces(new BinTypeRef[] {supI.getTypeRef()});
    a.getTypeRef().setInterfaces(new BinTypeRef[] {i.getTypeRef()});

    final BinMethod test =
        new BinMethod("test",
        BinParameter.NO_PARAMS,
        i.getTypeRef(),
        0,
        BinMethod.Throws.NO_THROWS);
    supI.addDeclaredMethod(test);

    /*      assertEquals("i.getAllAccessibleMethods().length",
              1,
              i.getMethods().length);

          assertEquals("a.getAllAccessibleMethods().length",
              1,
              a.getMethods().length);*/

    assertEquals("type for test()",
        a,
        MethodInvocationRules.getTypeForDotlessInvocation(a, "test"));

    cat.info("SUCCESS");
  }

  /**
   * Tests whether getInvokedType works when method is declared in
   * enclosing outer class.
   */
  public final void testGetInvokedTypeOuter() {
    cat.info(
        "Testing whether getInvokedType works when method is declared in"
        + " enclosing outer class");

//       class TopLevel{
//         void test() {}
//
//         class A {
//           class B {
//
//           }
//         }
//       }
    final BinCIType topLevel = Utils.createClass("TopLevel");
    final BinCIType a = Utils.createClass("A");
    a.setOwner(topLevel.getTypeRef());
    final BinCIType b = Utils.createClass("B");
    b.setOwner(a.getTypeRef());

    final BinMethod test =
        new BinMethod("test",
        BinParameter.NO_PARAMS,
        topLevel.getTypeRef(),
        0,
        BinMethod.Throws.NO_THROWS);
    topLevel.addDeclaredMethod(test);

    assertEquals("type for test() from within B",
        topLevel,
        MethodInvocationRules.getTypeForDotlessInvocation(b, "test"));

    cat.info("SUCCESS");
  }

  /**
   * Tests whether top-level public type is accessible.
   */
  public final void testIsTypeAccessibleTopLevelPublic() {
    cat.info("Testing whether top-level public type is accessible");
    final BinCIType a = Utils.createClass("A");
    a.setModifiers(BinModifier.PUBLIC);

    assertTrue("public top-level class accessible",
        a.isAccessible(Utils.createClass("B")));

    final BinCIType i = Utils.createInterface("I");
    i.setModifiers(BinModifier.PUBLIC);

    assertTrue("public top-level interface accessible",
        i.isAccessible(Utils.createClass("B")));

    cat.info("SUCCESS");
  }

  /**
   * Tests whether top-level non-public type is accessible.
   */
  public final void testIsTypeAccessibleTopLevelNonPublic() {
    cat.info("Testing whether top-level non-public type is accessible");

    {
      final BinCIType a = Utils.createClass("abc.A");
      a.setModifiers(BinModifier.PACKAGE_PRIVATE);

      assertTrue("protected top-level class accessible inside package",
          a.isAccessible(Utils.createClass("abc.B")));

      assertTrue("protected top-level class not accessible outside package",
          !a.isAccessible(Utils.createClass("B")));
    }

    {
      final BinCIType i = Utils.createInterface("I");
      i.setModifiers(BinModifier.PROTECTED);

      final BinCIType b = Utils.createClass("B");
      final BinCIType innerOfB = Utils.createClass("InnerOfB");
      innerOfB.setOwner(b.getTypeRef());
      assertTrue("default-access top-level interface accessible inside"
          + " package",
          i.isAccessible(innerOfB));

      assertTrue("default-access top-level interface not accessible outside"
          + " package",
          !i.isAccessible(Utils.createClass("abc.B")));
    }

    cat.info("SUCCESS");
  }

  /**
   * Tests whether inner type is accessible.
   */
  public final void testIsTypeAccessibleInnerType() {
    cat.info("Testing whether inner type is accessible");

    final BinCIType a = Utils.createClass("A");
    a.setModifiers(BinModifier.PACKAGE_PRIVATE);
    final BinCIType innerOfA = Utils.createClass("InnerOfA");
    innerOfA.setModifiers(BinModifier.PUBLIC);
    innerOfA.setOwner(a.getTypeRef());

    assertTrue("public inner of default-access type accessible from same"
        + " package",
        innerOfA.isAccessible(Utils.createClass("B")));

    innerOfA.setModifiers(BinModifier.PRIVATE);
    assertTrue(
        "private inner of default-access type not accessible from same"
        + " package",
        !innerOfA.isAccessible(Utils.createClass("B")));

    assertTrue("private inner of type accessible from the type",
        innerOfA.isAccessible(a));

    innerOfA.setModifiers(BinModifier.PACKAGE_PRIVATE);
    assertTrue("default-access inner of default-access type accessible from "
        + " same package",
        innerOfA.isAccessible(Utils.createClass("B")));

    cat.info("SUCCESS");
  }

  /**
   * Tests isMemberAccessible for superclass's protected member.
   */
  public final void testIsMemberAccessibleProtectedInSuperclass() {
    cat.info("Testing isMemberAccessible for superclass's protected member");

    final BinCIType a = Utils.createClass("abc.A");
    final BinCIType b = Utils.createClass("def.B", a);
    final BinCIType c = Utils.createClass("ghi.C", b);
    final BinMethod test =
        new BinMethod("test",
        BinParameter.NO_PARAMS,
        a.getTypeRef(),
        BinModifier.PROTECTED,
        BinMethod.Throws.NO_THROWS);
    a.addDeclaredMethod(test);

    assertTrue("A is public", a.isPublic());

    assertTrue("A is accessible from C", a.isAccessible(c));

    assertTrue("test is protected", test.isProtected());

    assertTrue("C is derived from A",
        c.getTypeRef().isDerivedFrom(a.getTypeRef()));

    assertTrue("A.test accessible from C",
        test.isAccessible(test.getOwner().getBinCIType(), c));

    cat.info("SUCCESS");
  }

  /**
   * Tests isMemberAccessible for superclass's default-access member in same
   * package.
   */
  public final void testIsMemberAccessibleDefaultSamePackage() {
    cat.info("Testing isMemberAccessible for superclass's default-access"
        + " member in same package");

    final BinCIType a = Utils.createClass("A");
    final BinCIType b = Utils.createClass("B", a);

    final BinField fieldOfA =
        new BinField("test", BinPrimitiveType.LONG_REF, BinModifier.PACKAGE_PRIVATE, false);
    fieldOfA.setOwner(a.getTypeRef());
    a.setDeclaredFields(new BinField[] {fieldOfA});

    assertTrue("A.test accessible from B",
        fieldOfA.isAccessible(fieldOfA.getOwner().getBinCIType(), b));

    cat.info("SUCCESS");
  }

  /**
   * Tests isMemberAccessible for superclass's default-access member in a
   * different package.
   */
  public final void testIsMemberAccessibleDefaultDifferentPackage() {
    cat.info("Testing isMemberAccessible for superclass's default-access"
        + " member in a different package");

    final BinCIType a = Utils.createClass("abc.A");
    final BinCIType b = Utils.createClass("abc.def.B", a);

    final BinField fieldOfA =
        new BinField("test", BinPrimitiveType.LONG_REF, BinModifier.PACKAGE_PRIVATE, false);
    fieldOfA.setOwner(a.getTypeRef());
    a.setDeclaredFields(new BinField[] {fieldOfA});

    assertTrue("A.test not accessible from B",
        !fieldOfA.isAccessible(fieldOfA.getOwner().getBinCIType(), b));

    cat.info("SUCCESS");
  }

  /**
   * Tests isMemberAccessible for superclass's private member.
   */
  public final void testIsMemberAccessiblePrivateInSuperclass() {
    cat.info("Testing isMemberAccessible for superclass's private member");

    final BinCIType a = Utils.createClass("A");
    final BinCIType b = Utils.createClass("B", a);
    final BinCIType c = Utils.createClass("C", b);

    final BinField fieldOfA =
        new BinField("test", BinPrimitiveType.LONG_REF, BinModifier.PRIVATE, false);
    fieldOfA.setOwner(a.getTypeRef());
    a.setDeclaredFields(new BinField[] {fieldOfA});

    assertTrue("A.test not accessible from C",
        !fieldOfA.isAccessible(fieldOfA.getOwner().getBinCIType(), c));

    cat.info("SUCCESS");
  }

  /**
   * Tests isMemberAccessible for private member from owner class.
   */
  public final void testIsMemberAccessiblePrivateInSameClass() {
    cat.info("Testing isMemberAccessible for private member from owner"
        + " class");

    final BinCIType a = Utils.createClass("A");

    final BinField fieldOfA =
        new BinField("test", BinPrimitiveType.LONG_REF, BinModifier.PRIVATE, false);
    fieldOfA.setOwner(a.getTypeRef());
    a.setDeclaredFields(new BinField[] {fieldOfA});

    assertTrue("A.test not accessible from A",
        fieldOfA.isAccessible(fieldOfA.getOwner().getBinCIType(), a));

    cat.info("SUCCESS");
  }

  /**
   * Test for bug #293: Bug in determining method accessibility
   */
  public final void testBug293() {
    cat.info("Testing bug #293");

    final BinCIType packagePrivate = Utils.createClass("a.PackagePrivate");
    packagePrivate.setModifiers(BinModifier.PACKAGE_PRIVATE);

    final BinMethod accessibleMethod =
        new BinMethod("accessibleMethod",
        BinParameter.NO_PARAMS,
        BinPrimitiveType.VOID_REF,
        BinModifier.PUBLIC,
        BinMethod.Throws.NO_THROWS);
    packagePrivate.addDeclaredMethod(accessibleMethod);

    final BinCIType a = Utils.createClass("a.A", packagePrivate);
    a.setModifiers(BinModifier.PUBLIC);

    final BinCIType userB = Utils.createClass("b.User");

    assertTrue("A.accessibleMethod is accessible from b.User",
        accessibleMethod.isAccessible(a, userB));

    assertEquals("package of packagePrivate",
        "a", packagePrivate.getPackage().getQualifiedName());

    assertEquals("package of userB",
        "b", userB.getPackage().getQualifiedName());

    assertTrue(
        "package of packagePrivate is not identical to package of b.User",
        !packagePrivate.getPackage().isIdentical(userB.getPackage()));

    assertTrue("packagePrivate is not public",
        !packagePrivate.isPublic());

    assertTrue("packagePrivate is not accessible from b.User",
        !packagePrivate.isAccessible(userB));

    assertTrue(
        "packagePrivate.accessibleMethod is not accessible from b.User",
        !accessibleMethod.isAccessible(packagePrivate, userB));

    final BinCIType userA = Utils.createClass("a.User");

    assertTrue("packagePrivate.accessibleMethod is accessible from a.User",
        accessibleMethod.isAccessible(packagePrivate, userA));

    cat.info("SUCCESS");
  }

  /**
   * Tests isMemberAccessible for protected method in the same package through
   * type of other package.
   */
  public final void testIsMemberAccessibleProtectedInOtherClass() {
    cat.info("Testing isMemberAccessible for protected method in the same package through type of other package");

    final BinCIType a = Utils.createClass("abc.A");
    final BinCIType b = Utils.createClass("def.B", a);
    final BinCIType c = Utils.createClass("abc.C");
    final BinMethod test =
        new BinMethod("test",
        BinParameter.NO_PARAMS,
        a.getTypeRef(),
        BinModifier.PROTECTED,
        BinMethod.Throws.NO_THROWS);
    a.addDeclaredMethod(test);

    assertTrue("A is public", a.isPublic());

    assertTrue("A is accessible from C", a.isAccessible(c));

    assertTrue("test is protected", test.isProtected());

    assertTrue("B is derived from A",
        b.getTypeRef().isDerivedFrom(a.getTypeRef()));

    assertTrue("B.test accessible from C",
        test.isAccessible(b, c));

    cat.info("SUCCESS");
  }

  /**
   * Tests isMemberAccessible for protected method in the other package
   * through type of the same package.
   */
  public final void testIsNotMemberAccessibleProtectedInOtherClass() {
    cat.info("Testing isMemberAccessible for protected method in the other package through type of the same package");

    final BinCIType a = Utils.createClass("def.A");
    final BinCIType b = Utils.createClass("abc.B", a);
    final BinCIType c = Utils.createClass("abc.C");
    final BinMethod test =
        new BinMethod("test",
        BinParameter.NO_PARAMS,
        a.getTypeRef(),
        BinModifier.PROTECTED,
        BinMethod.Throws.NO_THROWS);
    a.addDeclaredMethod(test);

    assertTrue("A is public", a.isPublic());

    assertTrue("A is accessible from C", a.isAccessible(c));

    assertTrue("test is protected", test.isProtected());

    assertTrue("B is derived from A",
        b.getTypeRef().isDerivedFrom(a.getTypeRef()));

    assertTrue("B.test is not accessible from C",
        !test.isAccessible(b, c));

    cat.info("SUCCESS");
  }

  public final void testFindSuitableMethodWhenBoxing1() {
    cat.info("Testing findSuitableMethod with boxing supported on A{test(int x)} and B->A{test(integer x)} classes");

    Utils.createFakeProject();

    // reference for integer class
    BinTypeRef integerRef = Utils.createClass("java.lang.Integer").getTypeRef();

    // creating A class
    final BinCIType a = Utils.createClass("abc.A");
    BinParameter[] intParam = {
        new BinParameter(
            "x",
            BinPrimitiveType.INT_REF,
        	BinModifier.NONE)
        };
    final BinMethod intMethod = new BinMethod("test", intParam, BinPrimitiveType.VOID_REF,
        BinModifier.PROTECTED, BinMethod.Throws.NO_THROWS);
    a.addDeclaredMethod(intMethod);

    // creating B class what inherits A class
    final BinCIType b = Utils.createClass("abc.B", a);

    BinParameter[] integerParam = {
        new BinParameter(
            "x",
            integerRef,
            BinModifier.NONE)
        };
    final BinMethod integerMethod = new BinMethod("test", integerParam, BinPrimitiveType.VOID_REF,
        BinModifier.PROTECTED, BinMethod.Throws.NO_THROWS);
    b.addDeclaredMethod(integerMethod);

    // testing...


    BinMethod[] methods = { intMethod, integerMethod };
    BinTypeRef[] intParamTypes = { BinPrimitiveType.INT_REF };
    BinTypeRef[] integerParamTypes = { integerRef };
    BinTypeRef[] booleanParamTypes = { BinPrimitiveType.BOOLEAN_REF };


    assertEquals("Method shall be with Integer parameter",
        integerMethod,
        MethodInvocationRules.findSuitableMethod(methods, integerParamTypes));

    assertEquals("Method shall be with int parameter",
        intMethod,
        MethodInvocationRules.findSuitableMethod(methods, intParamTypes));

    assertEquals("Method shall be null (not found)",
        null,
        MethodInvocationRules.findSuitableMethod(methods, booleanParamTypes));

    cat.info("SUCCESS");
  }

  public final void testFindSuitableMethodWhenBoxing2() {
    cat.info("Testing findSuitableMethod, what shall return false (no inherit support in Java 5 boxing)");

    Utils.createFakeProject();

    // creating A class
    final BinCIType a = Utils.createClass("abc.A");
    BinParameter[] shortParam = {
        new BinParameter(
            "x",
            BinPrimitiveType.SHORT_REF,
        	BinModifier.NONE)
        };
    final BinMethod shortMethod = new BinMethod("test", shortParam, BinPrimitiveType.VOID_REF,
        BinModifier.PROTECTED, BinMethod.Throws.NO_THROWS);

    BinParameter[] intParam = {
        new BinParameter(
            "x",
            BinPrimitiveType.INT_REF,
        	BinModifier.NONE)
        };
    final BinMethod intMethod = new BinMethod("test", intParam, BinPrimitiveType.VOID_REF,
        BinModifier.PROTECTED, BinMethod.Throws.NO_THROWS);

    a.addDeclaredMethod(shortMethod);
    a.addDeclaredMethod(intMethod);

    // reference for integer class
    BinTypeRef longRef = Utils.createClass("java.lang.Long").getTypeRef();
    BinTypeRef objectRef = Utils.createClass("java.lang.Object").getTypeRef();

    BinMethod[] methods = { shortMethod, intMethod };
    BinTypeRef[] intParamTypes = { BinPrimitiveType.INT_REF };
    BinTypeRef[] shortParamTypes = { BinPrimitiveType.SHORT_REF };
    BinTypeRef[]longParamTypes = { longRef };
    BinTypeRef[] objectParamTypes = { objectRef };


    assertEquals("Method shall be with Int parameter",
        intMethod,
        MethodInvocationRules.findSuitableMethod(methods, intParamTypes));

    assertEquals("Method shall be with Short parameter",
        shortMethod,
        MethodInvocationRules.findSuitableMethod(methods, shortParamTypes));

    assertEquals("Method shall be null (not found)",
        null,
        MethodInvocationRules.findSuitableMethod(methods, longParamTypes));

    assertEquals("Method shall be null (not found)",
        null,
        MethodInvocationRules.findSuitableMethod(methods, objectParamTypes));

    cat.info("SUCCESS");
  }

  public void testVariableArityInvocations() {
    int oldJvmMode = Project.getDefaultOptions().getJvmMode();
    Project.getDefaultOptions().setJvmMode(FastJavaLexer.JVM_50);

    try {
      cat.info("Testing variable arity invocations");
      Project project = Utils.createFakeProject();
      final BinCIType a = Utils.createClass("abc.A");
      BinParameter firstParam = new BinParameter("x", BinPrimitiveType.INT_REF,
          BinModifier.NONE);
      BinParameter secondParam = new BinVariableArityParameter("y",
          project.createArrayTypeForType(BinPrimitiveType.INT_REF, 1),
          BinModifier.NONE);
      BinParameter[] params = {firstParam, secondParam};

      final BinMethod method1 = new BinMethod("test", params,
          BinPrimitiveType.VOID_REF,
          BinModifier.PROTECTED, BinMethod.Throws.NO_THROWS);

      BinParameter firstParam2 = new BinParameter("x",
          BinPrimitiveType.FLOAT_REF, BinModifier.NONE);
      BinParameter[] params2 = {firstParam2};
      final BinMethod method2 = new BinMethod("test", params2,
          BinPrimitiveType.VOID_REF,
          BinModifier.PROTECTED, BinMethod.Throws.NO_THROWS);

      final BinMethod method3 = new BinMethod("test2", params2,
          BinPrimitiveType.VOID_REF,
          BinModifier.PROTECTED, BinMethod.Throws.NO_THROWS);

      a.addDeclaredMethod(method1);
      a.addDeclaredMethod(method2);
      a.addDeclaredMethod(method3);

      BinMethod[] methods = new BinMethod[] {method1, method2, method3};

      BinTypeRef[] zeroIntTypes = {};
      BinTypeRef[] oneIntType = {BinPrimitiveType.INT_REF};
      BinTypeRef[] twoIntTypes = {BinPrimitiveType.INT_REF,
          BinPrimitiveType.INT_REF};
      BinTypeRef[] threeIntTypes = {BinPrimitiveType.INT_REF,
          BinPrimitiveType.INT_REF, BinPrimitiveType.INT_REF};
      BinTypeRef[] twoIntOneFloatTypes = {BinPrimitiveType.INT_REF,
          BinPrimitiveType.INT_REF, BinPrimitiveType.FLOAT_REF};

      assertEquals("Invocation test() shall return null",
          null,
          MethodInvocationRules.findSuitableMethod(methods, zeroIntTypes));

      assertEquals("Invocation test(intVar); shall return method test(float x)",
          method2,
          MethodInvocationRules.findSuitableMethod(methods, oneIntType));

      assertEquals(
          "Invocation test(intVar1, intVar2); shall return test(int x, int...y)",
          method1,
          MethodInvocationRules.findSuitableMethod(methods, twoIntTypes));

      assertEquals(
          "Invocation test(intVar1, intVar2, intVar3); shall return test(int x, int...y)",
          method1,
          MethodInvocationRules.findSuitableMethod(methods, threeIntTypes));
      cat.info("SUCCESS");

      assertEquals("Invocation test(intVar1, intVar2, floatVar3); shall return null(int x, int...y)",
          null,
          MethodInvocationRules.findSuitableMethod(methods, twoIntOneFloatTypes));
      cat.info("SUCCESS");
    } finally {
      Project.getDefaultOptions().setJvmMode(oldJvmMode);
    }
  }

  public void testVariableArityInvocationsWithBoxing() {
    int oldJvmMode = Project.getDefaultOptions().getJvmMode();
    Project.getDefaultOptions().setJvmMode(FastJavaLexer.JVM_50);

    try {
      cat.info("Testing variable arity invocations with boxing enabled");
      Project project = Utils.createFakeProject();
      final BinCIType a = Utils.createClass("abc.A");
      final BinTypeRef integerRef = Utils.createClass("java.lang.Integer").
          getTypeRef();

      BinParameter firstParam = new BinParameter("x", BinPrimitiveType.BOOLEAN_REF,
          BinModifier.NONE);
      BinParameter secondParam = new BinVariableArityParameter("y",
          project.createArrayTypeForType(integerRef, 1), BinModifier.NONE);
      BinParameter thirdParam = new BinVariableArityParameter("y",
          project.createArrayTypeForType(BinPrimitiveType.BOOLEAN_REF, 1),
          BinModifier.NONE);
      BinParameter[] params1 = {firstParam, secondParam};
      BinParameter[] params2 = {firstParam, thirdParam};

      // creating protected void test(bool x, Integer... y);
      final BinMethod method1 = new BinMethod("test", params1,
          BinPrimitiveType.VOID_REF,
          BinModifier.PROTECTED, BinMethod.Throws.NO_THROWS);

    //  creating protected void test(bool x, boolean... y);
      final BinMethod method2 = new BinMethod("test", params2,
          BinPrimitiveType.VOID_REF,
          BinModifier.PROTECTED, BinMethod.Throws.NO_THROWS);

      a.addDeclaredMethod(method1);
      a.addDeclaredMethod(method2);
      BinMethod[] methods = new BinMethod[] {method1, method2};

      BinTypeRef[] oneBoolType = {BinPrimitiveType.BOOLEAN_REF}; // invocation for (bool)
      BinTypeRef[] oneBoolOneIntegerTypes = {BinPrimitiveType.BOOLEAN_REF,
          integerRef}; // invocation for (bool, Integer)
      BinTypeRef[] oneBoolOneIntOneIntegerTypes = {BinPrimitiveType.BOOLEAN_REF,
          BinPrimitiveType.INT_REF, integerRef}; // invocation for(bool, int, Integer)
      BinTypeRef[] oneBoolTwoIntTypes = {BinPrimitiveType.BOOLEAN_REF,
          BinPrimitiveType.INT_REF, BinPrimitiveType.INT_REF}; // invocation for (bool, int, int)
      BinTypeRef[] twoBoolTypes = {BinPrimitiveType.BOOLEAN_REF, // invocation for (bool, bool)
          BinPrimitiveType.BOOLEAN_REF};

      assertEquals(
          "Invocation for (bool); shall return null because it is ambiguous",
          null, // is ambiguous
          MethodInvocationRules.findSuitableMethod(methods, oneBoolType));

      assertEquals(
          "Invocation for (bool, Integer); shall return test(bool x, Integer... y)",
          method1,
          MethodInvocationRules.findSuitableMethod(methods, oneBoolOneIntegerTypes));

      assertEquals(
          "Invocation for (bool, int, int); shall return test(bool x, Integer... y)",
          method1,
          MethodInvocationRules.findSuitableMethod(methods, oneBoolTwoIntTypes));

      assertEquals(
          "Invocation for (bool, int, Integer); shall return test(bool x, Integer... y)",
          method1,
          MethodInvocationRules.findSuitableMethod(methods,
          oneBoolOneIntOneIntegerTypes));

      assertEquals(
          "Invocation for (bool, bool); shall return test(bool x, boolean... y)",
          method2,
          MethodInvocationRules.findSuitableMethod(methods, twoBoolTypes));

      cat.info("SUCCESS");
    } finally {
      Project.getDefaultOptions().setJvmMode(oldJvmMode);
    }
  }

  public void testVariableArityInvocationsWithBoxingAndSubtyping() {
    int oldJvmMode = Project.getDefaultOptions().getJvmMode();
    Project.getDefaultOptions().setJvmMode(FastJavaLexer.JVM_50);

    try {
      cat.info(
          "Testing variable arity invocations with boxing enabled and using subtyping");
      Project project = Utils.createFakeProject();
      final BinCIType a = Utils.createClass("abc.A");
      final BinTypeRef objectRef = Utils.createClass("java.lang.Object").getTypeRef();
      final BinTypeRef integerRef = Utils.createClass("java.lang.Integer").
          getTypeRef();
      final BinTypeRef booleanRef = Utils.createClass("java.lang.Boolean").
          getTypeRef();

      BinParameter firstParam = new BinParameter("x", BinPrimitiveType.INT_REF,
          BinModifier.NONE);
      BinParameter secondParam = new BinVariableArityParameter("y",
          project.createArrayTypeForType(objectRef, 1), BinModifier.NONE);
      BinParameter thirdParam = new BinParameter("y", BinPrimitiveType.INT_REF,
          BinModifier.NONE);
      BinParameter[] params1 = {firstParam, secondParam};
      BinParameter[] params2 = {firstParam, thirdParam};
      // creating method test(int x, Object... t)
      final BinMethod method1 = new BinMethod("test", params1,
          BinPrimitiveType.VOID_REF,
          BinModifier.PROTECTED, BinMethod.Throws.NO_THROWS);
      // creating method test(int x, int y )
      final BinMethod method2 = new BinMethod("test", params2,
          BinPrimitiveType.VOID_REF,
          BinModifier.PROTECTED, BinMethod.Throws.NO_THROWS);

      a.addDeclaredMethod(method1);
      a.addDeclaredMethod(method2);
      BinMethod[] methods = new BinMethod[] {method1, method2};

      BinTypeRef[] oneIntType = {BinPrimitiveType.INT_REF}; // invocation for (int)
      BinTypeRef[] oneIntOneIntegerTypes = {BinPrimitiveType.INT_REF,
          integerRef}; // invocation for (int, Integer)
      BinTypeRef[] oneIntTwoIntegerTypes = {BinPrimitiveType.INT_REF,
          integerRef, integerRef}; // invocation for(int, integer, integer)
      BinTypeRef[] oneIntOneIntegerOneBooleanTypes = {BinPrimitiveType.INT_REF,
          integerRef, booleanRef}; // invocation for (int, integer, boolean)
      BinTypeRef[] threeIntTypes = {BinPrimitiveType.INT_REF,
          BinPrimitiveType.INT_REF, BinPrimitiveType.INT_REF}; // invocation for (int, int, int)

      assertEquals("Invocation for (int); shall return test(int x, Object... y)",
          method1,
          MethodInvocationRules.findSuitableMethod(methods, oneIntType));

      assertEquals("Invocation for (int, Integer); shall return test(int x, int y)",
          method2,
          MethodInvocationRules.findSuitableMethod(methods, oneIntOneIntegerTypes));

      assertEquals(
          "Invocation for (int, int, int); shall return test(int x, Object... y)",
          method1,
          MethodInvocationRules.findSuitableMethod(methods, threeIntTypes));

      assertEquals(
          "Invocation for (int, Integer, Integer); shall return test(int x, Object... y)",
          method1,
          MethodInvocationRules.findSuitableMethod(methods, oneIntTwoIntegerTypes));

      assertEquals(
          "Invocation for (int, Integer, Boolean); shall return test(int x, Object... y)",
          method1,
          MethodInvocationRules.findSuitableMethod(methods,
          oneIntOneIntegerOneBooleanTypes));

      cat.info("SUCCESS");
    } finally {
      Project.getDefaultOptions().setJvmMode(oldJvmMode);
    }
  }

  public void testVariableArityMoreSpecificResolving() {
    int oldJvmMode = Project.getDefaultOptions().getJvmMode();
    Project.getDefaultOptions().setJvmMode(FastJavaLexer.JVM_50);

    try {
      cat.info("Testing resolving of more specific variable arity methods");
      Project project = Utils.createFakeProject();
      final BinCIType a = Utils.createClass("abc.A");

      BinParameter firstParam1 = new BinParameter("x", BinPrimitiveType.INT_REF,
          BinModifier.NONE);
      BinParameter firstParam2 = new BinVariableArityParameter("y",
          project.createArrayTypeForType(BinPrimitiveType.INT_REF, 1),
          BinModifier.NONE);
      BinParameter secondParam1 = new BinParameter("x", BinPrimitiveType.INT_REF,
          BinModifier.NONE);
      BinParameter secondParam2 = new BinParameter("y", BinPrimitiveType.INT_REF,
          BinModifier.NONE);
      BinParameter secondParam3 = new BinVariableArityParameter("z",
          project.createArrayTypeForType(BinPrimitiveType.INT_REF, 1),
          BinModifier.NONE);
      BinParameter[] params1 = {firstParam1, firstParam2};
      BinParameter[] params2 = {secondParam1, secondParam2, secondParam3};
      // creating method test(int x, int.. y)
      final BinMethod method1 = new BinMethod("test", params1,
          BinPrimitiveType.VOID_REF,
          BinModifier.PROTECTED, BinMethod.Throws.NO_THROWS);

      //  creating method test(int x, int y, int.. z)
      final BinMethod method2 = new BinMethod("test", params2,
          BinPrimitiveType.VOID_REF,
          BinModifier.PROTECTED, BinMethod.Throws.NO_THROWS);

      a.addDeclaredMethod(method1);
      a.addDeclaredMethod(method2);
      BinMethod[] methods = new BinMethod[] {method1, method2};

      BinTypeRef[] threeIntTypes = {BinPrimitiveType.INT_REF,
          BinPrimitiveType.INT_REF, BinPrimitiveType.INT_REF}; // invocation for (int, int, int)

      assertEquals(
          "Invocation for (int, int, int); shall return test(int x, int... y)",
          method2,
          MethodInvocationRules.findSuitableMethod(methods, threeIntTypes));

      cat.info("SUCCESS");
    } finally {
      Project.getDefaultOptions().setJvmMode(oldJvmMode);
    }
  }
}
