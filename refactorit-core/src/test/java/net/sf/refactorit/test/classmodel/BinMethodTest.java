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
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariableArityParameter;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.FastJavaLexer;
import net.sf.refactorit.query.ItemByNameFinder;
import net.sf.refactorit.test.Utils;

import org.apache.log4j.Category;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test driver for {@link BinMethod}.
 */
public class BinMethodTest extends TestCase {

  /** Logger instance. */
  private static final Category cat =
      Category.getInstance(BinMethodTest.class.getName());

  /** Creates new BinMethodTest */
  public BinMethodTest(String name) {
    super(name);
  }

  public static TestSuite suite() {
    final TestSuite suite = new TestSuite(BinMethodTest.class);
    suite.setName("BinMethod tests");
    suite.addTest(GetBodyASTTest.suite());
    return suite;
  }

  /**
   * Tests {@link net.sf.refactorit.classmodel.BinMethod#findOverrides} when no
   * explicit superclass is there.
   */
  public void testOverridesNoSuper() {
    cat.info("Testing overrides when no explicit superclass is there");

    final BinCIType test = Utils.createClass("Test");

    assertNotNull("Tests has a superclass",
        test.getTypeRef().getSuperclass());

    assertEquals("Tests supeclass is Object",
        "java.lang.Object",
        test.getTypeRef().getSuperclass().getQualifiedName()
        );

    final BinMethod method =
        new BinMethod("method",
        BinParameter.NO_PARAMS,
        BinPrimitiveType.VOID_REF,
        0,
        BinMethod.Throws.NO_THROWS);
    test.addDeclaredMethod(method);

    assertEquals("No overriden method", 0, method.findOverrides().size());

    cat.info("SUCCESS");
  }

  /**
   * Tests {@link net.sf.refactorit.classmodel.BinMethod#findOverrides} when
   * method is static.
   */
  public void testOverridesStatic() {
    cat.info("Testing overrides when method is static");

    final BinCIType a = Utils.createClass("A");
    final BinCIType b = Utils.createClass("B", a);

    final BinMethod methodOfA =
        new BinMethod("method",
        BinParameter.NO_PARAMS,
        BinPrimitiveType.VOID_REF,
        BinModifier.STATIC,
        BinMethod.Throws.NO_THROWS);
    a.addDeclaredMethod(methodOfA);

    final BinMethod methodOfB =
        new BinMethod("method",
        BinParameter.NO_PARAMS,
        BinPrimitiveType.VOID_REF,
        BinModifier.STATIC,
        BinMethod.Throws.NO_THROWS);
    b.addDeclaredMethod(methodOfB);

    assertEquals("No overriden method", 0, methodOfB.findOverrides().size());

    cat.info("SUCCESS");
  }

  /**
   * Tests {@link net.sf.refactorit.classmodel.BinMethod#findOverrides} when
   * superclass doesn't have a method with such a name.
   */
  public void testOverridesDifferentName() {
    cat.info(
        "Testing overrides when superclass doesn't have a method with such"
        + " a name");

    final BinCIType a = Utils.createClass("A");
    final BinCIType b = Utils.createClass("B", a);

    final BinMethod methodOfA =
        new BinMethod("methodAbc",
        BinParameter.NO_PARAMS,
        BinPrimitiveType.VOID_REF,
        0,
        BinMethod.Throws.NO_THROWS);
    a.addDeclaredMethod(methodOfA);

    final BinMethod methodOfB =
        new BinMethod("method",
        BinParameter.NO_PARAMS,
        BinPrimitiveType.VOID_REF,
        0,
        BinMethod.Throws.NO_THROWS);
    b.addDeclaredMethod(methodOfB);

    assertEquals("No overriden method", 0, methodOfB.findOverrides().size());

    cat.info("SUCCESS");
  }

  /**
   * Tests {@link net.sf.refactorit.classmodel.BinMethod#findOverrides}.
   */
  public void testOverrides() {
    cat.info("Testing overrides");

    final BinCIType a = Utils.createClass("A");
    final BinCIType b = Utils.createClass("B", a);

    final BinMethod methodOfA =
        new BinMethod("method",
        new BinParameter[] {
        new BinParameter("i", BinPrimitiveType.INT_REF, 0)
    }
        ,
        BinPrimitiveType.VOID_REF,
        0,
        BinMethod.Throws.NO_THROWS);
    a.addDeclaredMethod(methodOfA);

    final BinMethod otherMethodOfA =
        new BinMethod("method",
        new BinParameter[] {
        new BinParameter("i", BinPrimitiveType.BYTE_REF, 0)
    }
        ,
        BinPrimitiveType.VOID_REF,
        0,
        BinMethod.Throws.NO_THROWS);
    a.addDeclaredMethod(otherMethodOfA);

    final BinMethod methodOfB =
        new BinMethod("method",
        new BinParameter[] {
        new BinParameter("i", BinPrimitiveType.INT_REF, 0)
    }
        ,
        BinPrimitiveType.VOID_REF,
        0,
        BinMethod.Throws.NO_THROWS);
    b.addDeclaredMethod(methodOfB);

    final BinMethod otherMethodOfB =
        new BinMethod("method",
        new BinParameter[] {
        new BinParameter("x", BinPrimitiveType.BYTE_REF, 0)
    }
        ,
        BinPrimitiveType.VOID_REF,
        0,
        BinMethod.Throws.NO_THROWS);
    b.addDeclaredMethod(otherMethodOfB);

    assertEquals("B.method(int) overrides A.method(int)",
        methodOfA,
        methodOfB.findOverrides().get(0));

    assertEquals("B.method(byte) overrides A.method(byte)",
        otherMethodOfA,
        otherMethodOfB.findOverrides().get(0));

    cat.info("SUCCESS");
  }

  /**
   * Tests {@link net.sf.refactorit.classmodel.BinMethod#findOverrides} with
   * deep hierarchy.
   */
  public void testOverridesDeepHierarchy() {
    cat.info("Testing overrides with deep hierarchy");

    final BinCIType a = Utils.createClass("A");
    final BinCIType b = Utils.createClass("B", a);
    final BinCIType c = Utils.createClass("C", b);

    final BinMethod methodOfA =
        new BinMethod("method",
        new BinParameter[] {
        new BinParameter("i", BinPrimitiveType.INT_REF, 0)
    }
        ,
        BinPrimitiveType.VOID_REF,
        0,
        BinMethod.Throws.NO_THROWS);
    a.addDeclaredMethod(methodOfA);

    final BinMethod otherMethodOfA =
        new BinMethod("method",
        new BinParameter[] {
        new BinParameter("i", BinPrimitiveType.BYTE_REF, 0)
    }
        ,
        BinPrimitiveType.VOID_REF,
        0,
        BinMethod.Throws.NO_THROWS);
    a.addDeclaredMethod(otherMethodOfA);

    final BinMethod methodOfB =
        new BinMethod("method",
        new BinParameter[] {
        new BinParameter("i", BinPrimitiveType.INT_REF, 0)
    }
        ,
        BinPrimitiveType.VOID_REF,
        0,
        BinMethod.Throws.NO_THROWS);
    b.addDeclaredMethod(methodOfB);

    final BinMethod methodOfC =
        new BinMethod("method",
        new BinParameter[] {
        new BinParameter("i", BinPrimitiveType.INT_REF, 0)
    }
        ,
        BinPrimitiveType.VOID_REF,
        0,
        BinMethod.Throws.NO_THROWS);
    c.addDeclaredMethod(methodOfC);

    final BinMethod otherMethodOfC =
        new BinMethod("method",
        new BinParameter[] {
        new BinParameter("i", BinPrimitiveType.BYTE_REF, 0)
    }
        ,
        BinPrimitiveType.VOID_REF,
        0,
        BinMethod.Throws.NO_THROWS);
    c.addDeclaredMethod(otherMethodOfC);

    assertEquals("C.method(int) overrides B.method(int)",
        methodOfB,
        methodOfC.findOverrides().get(0));

    assertEquals("C.method(byte) overrides A.method(byte)",
        otherMethodOfA,
        otherMethodOfC.findOverrides().get(0));

    cat.info("SUCCESS");
  }

  /**
   * Tests {@link net.sf.refactorit.classmodel.BinMethod#findOverrides} with
   * private method with same signature in superclass.
   */
  public void testOverridesPrivate() {
    cat.info("Testing overrides with private method with same signature in"
        + " superclass");

    final BinCIType a = Utils.createClass("A");
    final BinCIType b = Utils.createClass("B", a);

    final BinMethod methodOfA =
        new BinMethod("method",
        BinParameter.NO_PARAMS,
        BinPrimitiveType.VOID_REF,
        BinModifier.PRIVATE,
        BinMethod.Throws.NO_THROWS);
    a.addDeclaredMethod(methodOfA);

    final BinMethod methodOfB =
        new BinMethod("method",
        BinParameter.NO_PARAMS,
        BinPrimitiveType.VOID_REF,
        0,
        BinMethod.Throws.NO_THROWS);
    b.addDeclaredMethod(methodOfB);

    assertEquals("B.method doesn't override A.method",
        0, methodOfB.findOverrides().size());

    cat.info("SUCCESS");
  }

  /**
   * Tests {@link net.sf.refactorit.classmodel.BinMethod#findOverrides} with
   * default method with same signature in superclass which is in the same
   * package.
   */
  public void testOverridesDefaultSamePackage() {
    cat.info("Testing overrides with private method with same signature in"
        + " superclass which is in the same package");

    final BinCIType a = Utils.createClass("A");
    final BinCIType b = Utils.createClass("B", a);

    final BinMethod methodOfA =
        new BinMethod("method",
        BinParameter.NO_PARAMS,
        BinPrimitiveType.VOID_REF,
        BinModifier.PACKAGE_PRIVATE,
        BinMethod.Throws.NO_THROWS);
    a.addDeclaredMethod(methodOfA);

    final BinMethod methodOfB =
        new BinMethod("method",
        BinParameter.NO_PARAMS,
        BinPrimitiveType.VOID_REF,
        0,
        BinMethod.Throws.NO_THROWS);
    b.addDeclaredMethod(methodOfB);

    assertEquals("B.method overrides A.method",
        methodOfA,
        methodOfB.findOverrides().get(0));

    cat.info("SUCCESS");
  }

  /**
   * Tests {@link net.sf.refactorit.classmodel.BinMethod#findOverrides} with
   * default method with same signature in superclass which is in a different
   * package.
   */
  public void testOverridesDefaultOtherPackage() {
    cat.info("Testing overrides with private method with same signature in"
        + " superclass which is in a different package");

    final BinCIType a = Utils.createClass("abc.A");
    final BinCIType b = Utils.createClass("B", a);

    final BinMethod methodOfA =
        new BinMethod("method",
        BinParameter.NO_PARAMS,
        BinPrimitiveType.VOID_REF,
        BinModifier.PACKAGE_PRIVATE,
        BinMethod.Throws.NO_THROWS);
    a.addDeclaredMethod(methodOfA);

    final BinMethod methodOfB =
        new BinMethod("method",
        BinParameter.NO_PARAMS,
        BinPrimitiveType.VOID_REF,
        0,
        BinMethod.Throws.NO_THROWS);
    b.addDeclaredMethod(methodOfB);

    assertEquals("B.method doesn't override A.method",
        0, methodOfB.findOverrides().size());

    cat.info("SUCCESS");
  }

  /**
   * Tests {@link net.sf.refactorit.classmodel.BinMethod#findOverrides} when
   * superclass has a method with same name and parameter types but with
   * different return type.
   */
  public void testOverridesDifferentReturnType() {
    cat.info("Testing overrides when superclass superclass has a method with"
        + " same name and parameter types but with different signature");

    final BinCIType a = Utils.createClass("A");
    final BinCIType b = Utils.createClass("B", a);

    final BinMethod methodOfA =
        new BinMethod("method",
        BinParameter.NO_PARAMS,
        BinPrimitiveType.VOID_REF,
        0,
        BinMethod.Throws.NO_THROWS);
    a.addDeclaredMethod(methodOfA);

    final BinMethod methodOfB =
        new BinMethod("method",
        BinParameter.NO_PARAMS,
        BinPrimitiveType.INT_REF,
        0,
        BinMethod.Throws.NO_THROWS);
    b.addDeclaredMethod(methodOfB);

    assertEquals("B.method overrides A.method",
        methodOfA,
        methodOfB.findOverrides().get(0));

    cat.info("SUCCESS");
  }

  /**
   * Tests {@link net.sf.refactorit.classmodel.BinMethod#findOverrides} when
   * superclass has a method with same name and return type but with different
   * parameter types.
   */
  public void testOverridesDifferentParameterTypes() {
    cat.info(
        "Testing overrides when superclass has a method with same name and"
        + " return type but with different parameter types");

    final BinCIType a = Utils.createClass("A");
    final BinCIType b = Utils.createClass("B", a);

    final BinMethod methodOfA =
        new BinMethod("method",
        new BinParameter[] {
        new BinParameter("a", a.getTypeRef(), 0)
    }
        ,
        BinPrimitiveType.VOID_REF,
        0,
        BinMethod.Throws.NO_THROWS);
    a.addDeclaredMethod(methodOfA);

    final BinMethod methodOfB =
        new BinMethod("method",
        new BinParameter[] {
        new BinParameter("b", b.getTypeRef(), 0)
    }
        ,
        BinPrimitiveType.VOID_REF,
        0,
        BinMethod.Throws.NO_THROWS);
    b.addDeclaredMethod(methodOfB);

    assertEquals("No overriden method", 0, methodOfB.findOverrides().size());

    cat.info("SUCCESS");
  }

  /**
   * Tests {@link net.sf.refactorit.classmodel.BinMethod#findOverrides} when
   * superclass has a method with same name and return type but with different
   * number of parameters.
   */
  public void testOverridesDifferentNumberOfParameters() {
    cat.info(
        "Testing overrides when superclass has a method with same name and"
        + " return type but with different number of parameters");

    final BinCIType a = Utils.createClass("A");
    final BinCIType b = Utils.createClass("B", a);

    {
      final BinMethod methodOfA =
          new BinMethod("method",
          new BinParameter[] {
          new BinParameter("a", a.getTypeRef(), 0)
      }
          ,
          BinPrimitiveType.VOID_REF,
          0,
          BinMethod.Throws.NO_THROWS);
      a.addDeclaredMethod(methodOfA);

      final BinMethod methodOfB =
          new BinMethod("method",
          new BinParameter[] {
          new BinParameter("a", a.getTypeRef(), 0),
          new BinParameter("i", BinPrimitiveType.INT_REF, 0)
      }
          ,
          BinPrimitiveType.VOID_REF,
          0,
          BinMethod.Throws.NO_THROWS);
      b.addDeclaredMethod(methodOfB);

      assertEquals("No overriden method for B.method",
          0, methodOfB.findOverrides().size());
    }

    {
      final BinMethod otherMethodOfA =
          new BinMethod("otherMethod",
          new BinParameter[] {
          new BinParameter("a", a.getTypeRef(), 0),
          new BinParameter("i", BinPrimitiveType.INT_REF, 0)
      }
          ,
          BinPrimitiveType.VOID_REF,
          0,
          BinMethod.Throws.NO_THROWS);
      a.addDeclaredMethod(otherMethodOfA);

      final BinMethod otherMethodOfB =
          new BinMethod("otherMethod",
          new BinParameter[] {
          new BinParameter("a", a.getTypeRef(), 0)
      }
          ,
          BinPrimitiveType.VOID_REF,
          0,
          BinMethod.Throws.NO_THROWS);
      b.addDeclaredMethod(otherMethodOfB);

      assertEquals("No overriden method for B.otherMethod",
          0, otherMethodOfB.findOverrides().size());
    }

    cat.info("SUCCESS");
  }

  /**
   * Tests {@link net.sf.refactorit.classmodel.BinMethod#findOverrides} using
   * method with same signature as in implemented interface.
   */
  public void testOverridesInterfaceMethod() {
    cat.info("Testing overrides with method with same signature as in"
        + " implemented interface");

    final BinCIType a = Utils.createInterface("A");
    final BinCIType b = Utils.createClass("B");
    b.getTypeRef().setInterfaces(new BinTypeRef[] {a.getTypeRef()});

    final BinMethod methodOfA =
        new BinMethod("method",
        BinParameter.NO_PARAMS,
        BinPrimitiveType.VOID_REF,
        BinModifier.PACKAGE_PRIVATE | BinModifier.ABSTRACT,
        BinMethod.Throws.NO_THROWS);
    a.addDeclaredMethod(methodOfA);

    final BinMethod methodOfB =
        new BinMethod("method",
        BinParameter.NO_PARAMS,
        BinPrimitiveType.VOID_REF,
        0,
        BinMethod.Throws.NO_THROWS);
    b.addDeclaredMethod(methodOfB);

    assertEquals("B.method overrides A.method",
        methodOfA,
        methodOfB.findOverrides().get(0));

    cat.info("SUCCESS");
  }

  /**
   * Tests {@link net.sf.refactorit.classmodel.BinMethod#findOverrides} using
   * abstract method with same signature as in implemented interface.
   */
  public void testOverridesAbstractInterfaceMethod() {
    cat.info("Testing overrides with abstract method having same signature as"
        + " in implemented interface");

    final BinCIType a = Utils.createInterface("A");
    final BinCIType b = Utils.createClass("B");
    b.getTypeRef().setInterfaces(new BinTypeRef[] {a.getTypeRef()});
    b.setModifiers(BinModifier.PUBLIC | BinModifier.ABSTRACT);

    final BinMethod methodOfA =
        new BinMethod("method",
        BinParameter.NO_PARAMS,
        BinPrimitiveType.VOID_REF,
        BinModifier.PUBLIC | BinModifier.ABSTRACT,
        BinMethod.Throws.NO_THROWS);
    a.addDeclaredMethod(methodOfA);

    final BinMethod methodOfB =
        new BinMethod("method",
        BinParameter.NO_PARAMS,
        BinPrimitiveType.VOID_REF,
        BinModifier.PUBLIC | BinModifier.ABSTRACT,
        BinMethod.Throws.NO_THROWS);
    b.addDeclaredMethod(methodOfB);

    assertEquals("B.method overrides A.method",
        methodOfA,
        methodOfB.findOverrides().get(0));

    cat.info("SUCCESS");
  }

  /**
   * Tests {@link net.sf.refactorit.classmodel.BinMethod#getTopMethods}.
   */
  public void testGetTopMethodsBasic() {
    cat.info("Testing getTopMethods() basic functionality");

    final BinCIType x = Utils.createInterface("X");
    final BinCIType a = Utils.createClass("A");
    a.getTypeRef().setInterfaces(new BinTypeRef[] {x.getTypeRef()});
    final BinCIType b = Utils.createClass("B", a);
    final BinCIType c = Utils.createClass("C", b);

    {
      final BinMethod methodOfX =
          new BinMethod("method",
          BinParameter.NO_PARAMS,
          BinPrimitiveType.VOID_REF,
          0,
          BinMethod.Throws.NO_THROWS);
      x.addDeclaredMethod(methodOfX);

      final BinMethod methodOfA =
          new BinMethod("method",
          BinParameter.NO_PARAMS,
          BinPrimitiveType.VOID_REF,
          0,
          BinMethod.Throws.NO_THROWS);
      a.addDeclaredMethod(methodOfA);

      final BinMethod methodOfC =
          new BinMethod("method",
          BinParameter.NO_PARAMS,
          BinPrimitiveType.VOID_REF,
          0,
          BinMethod.Throws.NO_THROWS);
      c.addDeclaredMethod(methodOfC);

      List list = methodOfC.getTopMethods();
//      for (int i = 0, max = list.size(); i < max; i++) {
//        System.err.println("Method: "+((BinMethod)list.get(i)).getQualifiedName());
//      }

      assertEquals("Wrong N of top methods for C.method", 1, list.size());
      assertEquals("Wrong top method for C.method", "X.method",
          ((BinMethod) list.get(0)).getQualifiedName());
    }

    cat.info("SUCCESS");
  }

  /**
   * Tests {@link net.sf.refactorit.classmodel.BinMethod#getTopMethods}
   * when method has the same top method received in several branches
   * of implementation - the list should not contain duplicates.
   */
  public void testGetTopMethodsDuplicates() {
    cat.info("Testing getTopMethods() duplicate top");

    final BinCIType x = Utils.createInterface("X");
    final BinCIType a = Utils.createClass("A");
    a.getTypeRef().setInterfaces(new BinTypeRef[] {x.getTypeRef()});
    final BinCIType b = Utils.createClass("B", a);
    b.getTypeRef().setInterfaces(new BinTypeRef[] {x.getTypeRef()});

    {
      final BinMethod methodOfX =
          new BinMethod("method",
          BinParameter.NO_PARAMS,
          BinPrimitiveType.VOID_REF,
          0,
          BinMethod.Throws.NO_THROWS);
      x.addDeclaredMethod(methodOfX);

      final BinMethod methodOfA =
          new BinMethod("method",
          BinParameter.NO_PARAMS,
          BinPrimitiveType.VOID_REF,
          0,
          BinMethod.Throws.NO_THROWS);
      a.addDeclaredMethod(methodOfA);

      final BinMethod methodOfB =
          new BinMethod("method",
          BinParameter.NO_PARAMS,
          BinPrimitiveType.VOID_REF,
          0,
          BinMethod.Throws.NO_THROWS);
      b.addDeclaredMethod(methodOfB);

      List list = methodOfB.getTopMethods();
//      for (int i = 0, max = list.size(); i < max; i++) {
//        System.err.println("Method: "+((BinMethod)list.get(i)).getQualifiedName());
//      }

      assertEquals("Wrong N of top methods for B.method", 1, list.size());
      assertEquals("Wrong top method for B.method", "X.method",
          ((BinMethod) list.get(0)).getQualifiedName());
    }

    cat.info("SUCCESS");
  }

  /**
   * Tests {@link net.sf.refactorit.classmodel.BinMethod#getTopMethods}
   */
  public void testGetTopMethodsComplex() {
    cat.info("Testing getTopMethods() complex inheritance");

    final BinCIType x = Utils.createInterface("X");
    final BinCIType a = Utils.createClass("A");
    a.getTypeRef().setInterfaces(new BinTypeRef[] {x.getTypeRef()});
    final BinCIType b = Utils.createClass("B", a);
    final BinCIType c = Utils.createClass("C", b);
    c.getTypeRef().setInterfaces(new BinTypeRef[] {x.getTypeRef()});
    final BinCIType d = Utils.createClass("D", c);

    {
      final BinMethod methodOfX =
          new BinMethod("method",
          BinParameter.NO_PARAMS,
          BinPrimitiveType.VOID_REF,
          0,
          BinMethod.Throws.NO_THROWS);
      x.addDeclaredMethod(methodOfX);

      final BinMethod methodOfA =
          new BinMethod("method",
          BinParameter.NO_PARAMS,
          BinPrimitiveType.VOID_REF,
          0,
          BinMethod.Throws.NO_THROWS);
      a.addDeclaredMethod(methodOfA);

      final BinMethod methodOfB =
          new BinMethod("method",
          BinParameter.NO_PARAMS,
          BinPrimitiveType.VOID_REF,
          0,
          BinMethod.Throws.NO_THROWS);
      b.addDeclaredMethod(methodOfB);

      final BinMethod methodOfD =
          new BinMethod("method",
          BinParameter.NO_PARAMS,
          BinPrimitiveType.VOID_REF,
          0,
          BinMethod.Throws.NO_THROWS);
      d.addDeclaredMethod(methodOfD);

      List list = methodOfD.getTopMethods();
//      for (int i = 0, max = list.size(); i < max; i++) {
//        System.err.println("Method: "+((BinMethod)list.get(i)).getQualifiedName());
//      }

      assertEquals("Wrong N of top methods for D.method", 1, list.size());
      assertEquals("Wrong top method for D.method", "X.method",
          ((BinMethod) list.get(0)).getQualifiedName());
    }

    cat.info("SUCCESS");
  }

  /**
   * Tests {@link net.sf.refactorit.classmodel.BinMethod#getTopMethods}
   */
  public void testGetTopMethodsNoTop() {
    cat.info("Testing getTopMethods() no top method");

    final BinCIType a = Utils.createClass("A");
    final BinCIType b = Utils.createClass("B", a);

    assertNotNull(a);
    assertNotNull(a.getProject());
    assertNotNull(a.getProject().getObjectRef());
    assertNotNull(BinPrimitiveType.VOID_REF);

    final BinMethod methodOfA =
        new BinMethod("method",
        new BinParameter[] {
        new BinParameter("a", a.getProject().getObjectRef(), 0)}
        ,
        BinPrimitiveType.VOID_REF,
        0,
        BinMethod.Throws.NO_THROWS);
    a.addDeclaredMethod(methodOfA);

    assertNotNull(b.getProject().getTypeRefForName("java.lang.String"));

    final BinMethod methodOfB =
        new BinMethod("method",
        new BinParameter[] {
        new BinParameter("a",
        b.getProject().getTypeRefForName("java.lang.String"), 0)}
        ,
        BinPrimitiveType.VOID_REF,
        0,
        BinMethod.Throws.NO_THROWS);
    b.addDeclaredMethod(methodOfB);
    // NOTE: overloading occurs, not overriding!
    // So, B appears to have to different methods "method" accessible

    List list = methodOfB.getTopMethods();
//      for (int i = 0, max = list.size(); i < max; i++) {
//        System.err.println("Method: "+((BinMethod)list.get(i)).getQualifiedName());
//      }

    assertEquals("Wrong N of top methods for B.method", 0, list.size());

    cat.info("SUCCESS");
  }

  public void testIsVariableArity() {
    cat.info("Testing BinMethod::isVariableArity() method");
    Project project = Utils.createFakeProject();
    BinParameter variableArityParam = new BinVariableArityParameter("i",
        project.createArrayTypeForType(BinPrimitiveType.INT_REF, 1), BinModifier.NONE);
    BinParameter fixedArityParam = new BinParameter("i", BinPrimitiveType.INT_REF, BinModifier.NONE);

    final BinMethod method1 =
      new BinMethod("method1",
      new BinParameter[] {variableArityParam},
      BinPrimitiveType.VOID_REF,
      0,
      BinMethod.Throws.NO_THROWS);

    final BinMethod method2 =
      new BinMethod("method2",
      new BinParameter[] {},
      BinPrimitiveType.VOID_REF,
      0,
      BinMethod.Throws.NO_THROWS);

    final BinMethod method3 =
      new BinMethod("method3",
      new BinParameter[] {fixedArityParam, fixedArityParam, fixedArityParam, variableArityParam},
      BinPrimitiveType.VOID_REF,
      0,
      BinMethod.Throws.NO_THROWS);

    final BinMethod method4 =
      new BinMethod("method4",
      new BinParameter[] {fixedArityParam, fixedArityParam, fixedArityParam},
      BinPrimitiveType.VOID_REF,
      0,
      BinMethod.Throws.NO_THROWS);

    assertTrue("Method1 shall be variably arity.", method1.isVariableArity());
    assertFalse("Method2 shall be fixed arity.", method2.isVariableArity());
    assertTrue("Method3 shall be variably arity.", method3.isVariableArity());
    assertFalse("Method4 shall be fixed arity.", method4.isVariableArity());
    cat.info("SUCCESS");
  }

  /**
   * Test driver for {@link BinMethod#getBodyAST}.
   */
  public static class GetBodyASTTest extends TestCase {
    /** Logger instance. */
    private static final Category cat =
        Category.getInstance(GetBodyASTTest.class);

    private Project project;

    public GetBodyASTTest(String name) {
      super(name);
    }

    public static Test suite() {
      final TestSuite suite = new TestSuite(GetBodyASTTest.class);
      suite.setName("getBodyAST");
      return suite;
    }

    protected void setUp() throws Exception {
      project =
          Utils.createTestRbProject(
          Utils.getTestProjects().getProject("LocationAware"));
      project.getProjectLoader().build();
    }

    protected void tearDown() {
      project = null;
    }

    /**
     * Tests getBodyAST on Test.main.
     */
    public void testGetBodyASTTestMain() throws Exception {
      cat.info("Testing getBodyAST on Test.main");

      final BinCIType test =
          project.getTypeRefForName("Test").getBinCIType();
      // Test.main
      final ASTImpl bodyAst = test.getDeclaredMethods()[0].getBodyAST();
      assertTrue("getBodyAST() != null", bodyAst != null);
      assertEquals("Body start line", 11, bodyAst.getStartLine());
      assertEquals("Body start column", 50, bodyAst.getStartColumn());
      assertEquals("Body end line", 25, bodyAst.getEndLine());
      assertEquals("Body end column", 4, bodyAst.getEndColumn());

      cat.info("SUCCESS");
    }

    /**
     * Tests getBodyAST on abstract Test3.main.
     */
    public void testGetBodyASTTest3Main() throws Exception {
      cat.info("Testing getBodyAST abstract Test3.main");

      final BinCIType test3 =
          project.getTypeRefForName("Test3")
          .getBinCIType();
      final ASTImpl bodyAst = test3.getDeclaredMethods()[0].getBodyAST();
      assertNull("getBodyAST()", bodyAst);

      cat.info("SUCCESS");
    }

    /**
     * Tests getBodyAST on Point constructor.
     */
    public void testGetBodyASTPointConstructor() throws Exception {
      cat.info("Testing getBodyAST on Point constructor");

      final BinClass test =
          (BinClass)
          project.getTypeRefForName("Point").getBinCIType();
      // Point.Point()
      final ASTImpl bodyAst =
          test.getDeclaredConstructors()[0].getBodyAST();
      assertTrue("getBodyAST() != null", bodyAst != null);
      assertEquals("Body start line", 58, bodyAst.getStartLine());
      assertEquals("Body start column", 23, bodyAst.getStartColumn());
      assertEquals("Body end line", 58, bodyAst.getEndLine());
      assertEquals("Body end column", 50, bodyAst.getEndColumn());

      cat.info("SUCCESS");
    }

    /**
     * Tests getBodyAST on Test4.b.
     */
    public void testGetBodyASTTest4B() throws Exception {
      cat.info("Testing getBodyAST on Point constructor");

      final BinClass test =
          (BinClass)
          project.getTypeRefForName("Test4").getBinCIType();
      // Test4.b()
      final ASTImpl bodyAst
          = test.getDeclaredMethod("b", BinTypeRef.NO_TYPEREFS).getBodyAST();
      assertNull("getBodyAST()", bodyAst);

      cat.info("SUCCESS");
    }
  }
  
  // ------ Generics overrides tests -----
  public void testFindOverrides() throws Exception {
    int oldJvmMode = Project.getDefaultOptions().getJvmMode();
    Project.getDefaultOptions().setJvmMode(FastJavaLexer.JVM_50);
    try {
      Project project = Utils.createTestRbProjectWithManyFiles(new String[] {
          "Comparable.java",
          "public interface Comparable<T> {\n" +
          "  public int compareTo(T o);\n" +
          "}",
          "Map.java",
          "public interface Map<K, V> {\n" +
          "  public void put(K k, V v);\n" +
          "}",
          "Test.java",
          "public abstract class Test implements Comparable {\n" +
          "  public int compareTo(Object o) {return -1;}\n" +
          "  {\n" +
          "    ((Map) null).put(new Object(), new Object());\n" +
          "  }\n" +
          "}",
      });
      BinTypeRef testRef = project.getTypeRefForName("Test");
      assertNotNull(testRef);

      BinMethod method = ItemByNameFinder.findBinMethod(
          testRef.getBinCIType(), "compareTo", new String[] {"Object"});
      assertNotNull(method);

      assertEquals("overrides", 1, method.findOverrides().size());


      BinTypeRef compRef = project.getTypeRefForName("Comparable");
      assertNotNull(compRef);

      BinMethod method1 = ItemByNameFinder.findBinMethod(
          compRef.getBinCIType(), "compareTo", new String[] {"T"});
      assertNotNull(method1);

      assertEquals("overriden", 1, compRef.getBinCIType().getSubMethods(method1).size());
    } finally {
      Project.getDefaultOptions().setJvmMode(oldJvmMode);
    }
  }
  
  public void testGetSubMethods() throws Exception {
    int oldJvmMode = Project.getDefaultOptions().getJvmMode();
    Project.getDefaultOptions().setJvmMode(FastJavaLexer.JVM_50);
    try {
      Project project = Utils.createTestRbProjectWithManyFiles(new String[] {
          "Comparable.java",
          "public interface Comparable<T> {\n" +
          "  public int compareTo(T o);\n" +
          "}",
          "Map.java",
          "public interface Map<K, V> {\n" +
          "  public void put(K k, V v);\n" +
          "}",
          "Test.java",
          "public abstract class Test implements Comparable {\n" +
          "  public int compareTo(Object o) {return -1;}\n" +
          "  {\n" +
          "    ((Map) null).put(new Object(), new Object());\n" +
          "  }\n" +
          "}",
      });
      BinTypeRef comparableRef = project.getTypeRefForName("Comparable");
      assertNotNull(comparableRef);

      BinMethod method = comparableRef.getBinCIType().getDeclaredMethods()[0];
      assertNotNull(method);

      assertEquals("submethods", 1, method.getOwner().getBinCIType()
          .getSubMethods(method).size());

    } finally {
      Project.getDefaultOptions().setJvmMode(oldJvmMode);
    }
  }
}
