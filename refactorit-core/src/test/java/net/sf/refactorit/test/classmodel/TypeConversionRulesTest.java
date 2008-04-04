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
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.TypeConversionRules;
import net.sf.refactorit.test.Utils;

import org.apache.log4j.Category;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test driver for {@link net.sf.refactorit.classmodel.TypeConversionRules}.
 */
public class TypeConversionRulesTest extends TestCase {

  /** Logger instance. */
  private static final Category cat =
      Category.getInstance(TypeConversionRulesTest.class.getName());

  public TypeConversionRulesTest(String name) {
    super(name);
  }

  public static Test suite() throws Exception {
    final TestSuite suite = new TestSuite(TypeConversionRulesTest.class);
    suite.setName("TypeConversionRules tests");
    return suite;
  }

  /**
   * Tests whether isIdentityConversion method works.
   */
  public void testIsIdentityConversion() {
    cat.info("Testing whether isIdentityConversion method works");

    Project project = Utils.createFakeProject();

    assertTrue("byte -> byte",
        TypeConversionRules.isIdentityConversion(
        BinPrimitiveType.BYTE_REF,
        BinPrimitiveType.BYTE_REF
        ));

    assertTrue("boolean -> boolean",
        TypeConversionRules.isIdentityConversion(
        BinPrimitiveType.BOOLEAN_REF,
        BinPrimitiveType.BOOLEAN_REF
        ));

    assertTrue("double -> double",
        TypeConversionRules.isIdentityConversion(
        BinPrimitiveType.DOUBLE_REF,
        BinPrimitiveType.DOUBLE_REF));

    assertTrue("char -> int not allowed",
        !TypeConversionRules.isIdentityConversion(
        BinPrimitiveType.CHAR_REF,
        BinPrimitiveType.INT_REF
        ));

    assertTrue("String -> String",
        TypeConversionRules.isIdentityConversion(
        project.getTypeRefForName("java.lang.String"),
        project.getTypeRefForName("java.lang.String")
        ));

    assertTrue("String -> Object not allowed",
        !TypeConversionRules.isIdentityConversion(
        project.getTypeRefForName("java.lang.String"),
        project.getObjectRef()
        ));

    assertTrue("abc.Object -> def.Object not allowed",
        !TypeConversionRules.isIdentityConversion(
        Utils.createClass("abc.Object").getTypeRef(),
        Utils.createClass("def.Object").getTypeRef()
        ));

    assertTrue("abc.Object -> Object not allowed",
        !TypeConversionRules.isIdentityConversion(
        Utils.createClass("abc.Object").getTypeRef(),
        Utils.createClass("Object").getTypeRef()
        ));

    assertTrue("null -> Object not allowed",
        !TypeConversionRules.isIdentityConversion(null, project.getObjectRef()));

    cat.info("SUCCESS");
  }

  /**
   * Tests whether isWideningPrimitiveConversion method works.
   */
  public void testIsWideningPrimitiveConversion() {
    cat.info("Testing whether isWideningPrimitiveConversion method"
        + " works");

    Project project = Utils.createFakeProject();

    assertTrue("byte -> short",
        TypeConversionRules.isWideningPrimitiveConversion(
        BinPrimitiveType.BYTE_REF,
        BinPrimitiveType.SHORT_REF
        ));

    assertTrue("byte -> int",
        TypeConversionRules.isWideningPrimitiveConversion(
        BinPrimitiveType.BYTE_REF,
        BinPrimitiveType.INT_REF
        ));

    assertTrue("byte -> long",
        TypeConversionRules.isWideningPrimitiveConversion(
        BinPrimitiveType.BYTE_REF,
        BinPrimitiveType.LONG_REF
        ));

    assertTrue("byte -> float",
        TypeConversionRules.isWideningPrimitiveConversion(
        BinPrimitiveType.BYTE_REF,
        BinPrimitiveType.FLOAT_REF
        ));

    assertTrue("byte -> double",
        TypeConversionRules.isWideningPrimitiveConversion(
        BinPrimitiveType.BYTE_REF,
        BinPrimitiveType.DOUBLE_REF
        ));

    assertTrue("short -> int",
        TypeConversionRules.isWideningPrimitiveConversion(
        BinPrimitiveType.SHORT_REF,
        BinPrimitiveType.INT_REF
        ));

    assertTrue("short -> long",
        TypeConversionRules.isWideningPrimitiveConversion(
        BinPrimitiveType.SHORT_REF,
        BinPrimitiveType.LONG_REF
        ));

    assertTrue("short -> float",
        TypeConversionRules.isWideningPrimitiveConversion(
        BinPrimitiveType.SHORT_REF,
        BinPrimitiveType.FLOAT_REF
        ));

    assertTrue("short -> double",
        TypeConversionRules.isWideningPrimitiveConversion(
        BinPrimitiveType.SHORT_REF,
        BinPrimitiveType.DOUBLE_REF
        ));

    assertTrue("char -> int",
        TypeConversionRules.isWideningPrimitiveConversion(
        BinPrimitiveType.CHAR_REF,
        BinPrimitiveType.INT_REF
        ));

    assertTrue("char -> long",
        TypeConversionRules.isWideningPrimitiveConversion(
        BinPrimitiveType.CHAR_REF,
        BinPrimitiveType.LONG_REF
        ));

    assertTrue("char -> float",
        TypeConversionRules.isWideningPrimitiveConversion(
        BinPrimitiveType.CHAR_REF,
        BinPrimitiveType.FLOAT_REF
        ));

    assertTrue("char -> double",
        TypeConversionRules.isWideningPrimitiveConversion(
        BinPrimitiveType.CHAR_REF,
        BinPrimitiveType.DOUBLE_REF
        ));

    assertTrue("int -> long",
        TypeConversionRules.isWideningPrimitiveConversion(
        BinPrimitiveType.INT_REF,
        BinPrimitiveType.LONG_REF
        ));

    assertTrue("int -> float",
        TypeConversionRules.isWideningPrimitiveConversion(
        BinPrimitiveType.INT_REF,
        BinPrimitiveType.FLOAT_REF
        ));

    assertTrue("int -> double",
        TypeConversionRules.isWideningPrimitiveConversion(
        BinPrimitiveType.INT_REF,
        BinPrimitiveType.DOUBLE_REF
        ));

    assertTrue("long -> float",
        TypeConversionRules.isWideningPrimitiveConversion(
        BinPrimitiveType.LONG_REF,
        BinPrimitiveType.FLOAT_REF
        ));

    assertTrue("long -> double",
        TypeConversionRules.isWideningPrimitiveConversion(
        BinPrimitiveType.LONG_REF,
        BinPrimitiveType.DOUBLE_REF
        ));

    assertTrue("float -> double",
        TypeConversionRules.isWideningPrimitiveConversion(
        BinPrimitiveType.FLOAT_REF,
        BinPrimitiveType.DOUBLE_REF
        ));

    assertTrue("double -> float not allowed",
        !TypeConversionRules.isWideningPrimitiveConversion(
        BinPrimitiveType.DOUBLE_REF,
        BinPrimitiveType.FLOAT_REF
        ));

    assertTrue("String -> String not allowed",
        !TypeConversionRules.isWideningPrimitiveConversion(
        project.getTypeRefForName("java.lang.String"),
        project.getTypeRefForName("java.lang.String")
        ));

    assertTrue("null -> Object not allowed",
        !TypeConversionRules.isIdentityConversion(
        null,
        project.getObjectRef()
        ));

    cat.info("SUCCESS");
  }

  /**
   * Tests whether isNarrowingPrimitiveConversion method works.
   */
  public void testIsNarrowingPrimitiveConversion() {
    cat.info("Testing whether isNarrowingPrimitiveConversion method"
        + " works");

    Project project = Utils.createFakeProject();

    assertTrue("byte -> char",
        TypeConversionRules.isNarrowingPrimitiveConversion(
        BinPrimitiveType.BYTE_REF,
        BinPrimitiveType.CHAR_REF
        ));

    assertTrue("short -> byte",
        TypeConversionRules.isNarrowingPrimitiveConversion(
        BinPrimitiveType.SHORT_REF,
        BinPrimitiveType.BYTE_REF
        ));

    assertTrue("short -> char",
        TypeConversionRules.isNarrowingPrimitiveConversion(
        BinPrimitiveType.SHORT_REF,
        BinPrimitiveType.CHAR_REF
        ));

    assertTrue("char -> byte",
        TypeConversionRules.isNarrowingPrimitiveConversion(
        BinPrimitiveType.CHAR_REF,
        BinPrimitiveType.BYTE_REF
        ));

    assertTrue("char -> short",
        TypeConversionRules.isNarrowingPrimitiveConversion(
        BinPrimitiveType.CHAR_REF,
        BinPrimitiveType.SHORT_REF
        ));

    assertTrue("int -> byte",
        TypeConversionRules.isNarrowingPrimitiveConversion(
        BinPrimitiveType.INT_REF,
        BinPrimitiveType.BYTE_REF
        ));

    assertTrue("int -> short",
        TypeConversionRules.isNarrowingPrimitiveConversion(
        BinPrimitiveType.INT_REF,
        BinPrimitiveType.SHORT_REF
        ));

    assertTrue("int -> char",
        TypeConversionRules.isNarrowingPrimitiveConversion(
        BinPrimitiveType.INT_REF,
        BinPrimitiveType.CHAR_REF
        ));

    assertTrue("long -> byte",
        TypeConversionRules.isNarrowingPrimitiveConversion(
        BinPrimitiveType.LONG_REF,
        BinPrimitiveType.BYTE_REF
        ));

    assertTrue("long -> short",
        TypeConversionRules.isNarrowingPrimitiveConversion(
        BinPrimitiveType.LONG_REF,
        BinPrimitiveType.SHORT_REF
        ));

    assertTrue("long -> char",
        TypeConversionRules.isNarrowingPrimitiveConversion(
        BinPrimitiveType.LONG_REF,
        BinPrimitiveType.CHAR_REF
        ));

    assertTrue("long -> int",
        TypeConversionRules.isNarrowingPrimitiveConversion(
        BinPrimitiveType.LONG_REF,
        BinPrimitiveType.INT_REF
        ));

    assertTrue("float -> byte",
        TypeConversionRules.isNarrowingPrimitiveConversion(
        BinPrimitiveType.FLOAT_REF,
        BinPrimitiveType.BYTE_REF
        ));

    assertTrue("float -> short",
        TypeConversionRules.isNarrowingPrimitiveConversion(
        BinPrimitiveType.FLOAT_REF,
        BinPrimitiveType.SHORT_REF
        ));

    assertTrue("float -> char",
        TypeConversionRules.isNarrowingPrimitiveConversion(
        BinPrimitiveType.FLOAT_REF,
        BinPrimitiveType.CHAR_REF
        ));

    assertTrue("float -> int",
        TypeConversionRules.isNarrowingPrimitiveConversion(
        BinPrimitiveType.FLOAT_REF,
        BinPrimitiveType.INT_REF
        ));

    assertTrue("float -> long",
        TypeConversionRules.isNarrowingPrimitiveConversion(
        BinPrimitiveType.FLOAT_REF,
        BinPrimitiveType.LONG_REF
        ));

    assertTrue("double -> byte",
        TypeConversionRules.isNarrowingPrimitiveConversion(
        BinPrimitiveType.DOUBLE_REF,
        BinPrimitiveType.BYTE_REF
        ));

    assertTrue("double -> short",
        TypeConversionRules.isNarrowingPrimitiveConversion(
        BinPrimitiveType.DOUBLE_REF,
        BinPrimitiveType.SHORT_REF
        ));

    assertTrue("double -> char",
        TypeConversionRules.isNarrowingPrimitiveConversion(
        BinPrimitiveType.DOUBLE_REF,
        BinPrimitiveType.CHAR_REF
        ));

    assertTrue("double -> int",
        TypeConversionRules.isNarrowingPrimitiveConversion(
        BinPrimitiveType.DOUBLE_REF,
        BinPrimitiveType.INT_REF
        ));

    assertTrue("double -> long",
        TypeConversionRules.isNarrowingPrimitiveConversion(
        BinPrimitiveType.DOUBLE_REF,
        BinPrimitiveType.LONG_REF
        ));

    assertTrue("double -> float",
        TypeConversionRules.isNarrowingPrimitiveConversion(
        BinPrimitiveType.DOUBLE_REF,
        BinPrimitiveType.FLOAT_REF
        ));

    assertTrue("float -> double not allowed",
        !TypeConversionRules.isNarrowingPrimitiveConversion(
        BinPrimitiveType.FLOAT_REF,
        BinPrimitiveType.DOUBLE_REF
        ));

    assertTrue("String -> String not allowed",
        !TypeConversionRules.isNarrowingPrimitiveConversion(
        project.getTypeRefForName("java.lang.String"),
        project.getTypeRefForName("java.lang.String")
        ));

    assertTrue("null -> Object not allowed",
        !TypeConversionRules.isIdentityConversion(
        null,
        project.getObjectRef()
        ));

    cat.info("SUCCESS");
  }

  /**
   * Tests whether isWideningReferenceConversion works.
   */
  public void testIsWideningReferenceConversion() {
    cat.info("Testing whether isWideningReferenceConversion works");

    Project project = Utils.createFakeProject();

    assertTrue("String -> Object",
        TypeConversionRules.isWideningReferenceConversion(
        Utils.createClass("java.lang.String").getTypeRef(),
        project.getObjectRef()));

    {
      final BinCIType a = Utils.createClass("A");
      final BinCIType b = Utils.createClass("B", a);
      assertTrue("Subclass -> Class",
          TypeConversionRules.isWideningReferenceConversion(
          b.getTypeRef(),
          a.getTypeRef()
          ));
    }

    {
      final BinCIType a = Utils.createClass("A");
      final BinCIType b = Utils.createClass("B", a);
      assertTrue("Subclass -> Class (transitive)",
          TypeConversionRules.isWideningReferenceConversion(
          Utils.createClass("C", b).getTypeRef(),
          a.getTypeRef()
          ));
    }

    {
      final BinCIType interfaceA = Utils.createInterface("A");
      final BinCIType interfaceB = Utils.createInterface("B");
      interfaceB.getTypeRef().setSuperclass(
          interfaceA.getTypeRef());
      assertTrue("Subinterface -> Interface",
          TypeConversionRules.isWideningReferenceConversion(
          interfaceB.getTypeRef(),
          interfaceA.getTypeRef()
          ));
    }

    {
      final BinCIType interfaceA = Utils.createInterface("A");
      final BinCIType interfaceB = Utils.createInterface("B");
      final BinCIType interfaceC = Utils.createInterface("C");
      interfaceB.getTypeRef().setSuperclass(interfaceA.getTypeRef());
      interfaceC.getTypeRef().setSuperclass(interfaceB.getTypeRef());
      assertTrue("Subinterface -> Interface (transitive)",
          TypeConversionRules.isWideningReferenceConversion(
          interfaceC.getTypeRef(),
          interfaceA.getTypeRef()
          ));
    }

    {
      final BinTypeRef interfaceARef =
          Utils.createInterface("A").getTypeRef();
      final BinTypeRef interfaceBRef =
          Utils.createInterface("B").getTypeRef();
      final BinCIType classTmp =
          Utils.createClass(
          "Tmp");
      classTmp.getTypeRef().setInterfaces(
          new BinTypeRef[] {interfaceARef, interfaceBRef});
      assertTrue("Class -> Interface A",
          TypeConversionRules.isWideningReferenceConversion(
          classTmp.getTypeRef(),
          interfaceARef
          ));

      assertTrue("Class -> Interface B",
          TypeConversionRules.isWideningReferenceConversion(
          classTmp.getTypeRef(),
          interfaceBRef
          ));
    }

    {
      final BinTypeRef interfaceARef =
          Utils.createInterface("A").getTypeRef();
      final BinCIType interfaceB = Utils.createInterface("B");
      interfaceB.getTypeRef().setSuperclass(interfaceARef);
      final BinCIType interfaceC = Utils.createInterface("C");
      interfaceC.getTypeRef().setSuperclass(interfaceB.getTypeRef());
      final BinCIType classTmp =
          Utils.createClass(
          "Tmp");
      classTmp.getTypeRef().setInterfaces(
          new BinTypeRef[] {interfaceC.getTypeRef()});
      assertTrue("Class -> Interface (transitive)",
          TypeConversionRules.isWideningReferenceConversion(
          classTmp.getTypeRef(),
          interfaceARef
          ));
    }

    assertTrue("null -> Object",
        TypeConversionRules.isWideningReferenceConversion(
        null,
        project.getObjectRef()
        ));

    assertTrue(
        "byte[] -> java.lang.Cloneable",
        TypeConversionRules.isWideningReferenceConversion(
        Utils.createArrayTypeRef(BinPrimitiveType.BYTE_REF, 1),
        project.getTypeRefForName("java.lang.Cloneable")
        ));

    assertTrue(
        "SomeType[][] -> java.lang.Cloneable",
        TypeConversionRules.isWideningReferenceConversion(
        Utils.createArrayTypeRef(
        Utils.createClass("SomeType").getTypeRef(),
        2),
        project.getTypeRefForName("java.lang.Cloneable")
        ));

    assertTrue(
        "short[][] -> java.io.Serializable",
        TypeConversionRules.isWideningReferenceConversion(
        Utils.createArrayTypeRef(BinPrimitiveType.SHORT_REF, 2),
        project.getTypeRefForName("java.io.Serializable")
        ));

    assertTrue(
        "SomeType[] -> java.io.Serializable",
        TypeConversionRules.isWideningReferenceConversion(
        Utils.createArrayTypeRef(
        Utils.createClass("SomeType").getTypeRef(),
        1),
        project.getTypeRefForName("java.io.Serializable")
        ));

    {
      final BinTypeRef interfaceARef =
          Utils.createInterface("A").getTypeRef();
      final BinCIType interfaceB = Utils.createInterface("B");
      interfaceB.getTypeRef().setSuperclass(interfaceARef);
      assertTrue("B[] -> A[]",
          TypeConversionRules.isWideningReferenceConversion(
          Utils.createArrayTypeRef(interfaceB.getTypeRef(), 1),
          Utils.createArrayTypeRef(interfaceARef, 1))
          );
    }

    {
      final BinTypeRef classARef = Utils.createClass("A").getTypeRef();
      final BinTypeRef classBRef =
          Utils.createClass("B", classARef.getBinCIType()).getTypeRef();
      assertTrue("B[][] -> A[][]",
          TypeConversionRules.isWideningReferenceConversion(
          Utils.createArrayTypeRef(classBRef, 2),
          Utils.createArrayTypeRef(classARef, 2))
          );
    }

    {
      final BinTypeRef classARef = Utils.createClass("A").getTypeRef();
      assertTrue("A[][] -> Object[]",
          TypeConversionRules.isWideningReferenceConversion(
          Utils.createArrayTypeRef(classARef, 2),
          Utils.createArrayTypeRef(project.getObjectRef(), 1))
          );
    }

    {
      assertTrue("long[][] -> Object[]",
          TypeConversionRules.isWideningReferenceConversion(
          Utils.createArrayTypeRef(BinPrimitiveType.LONG_REF, 2),
          Utils.createArrayTypeRef(project.getObjectRef(), 1))
          );
    }

    assertTrue("byte[] -> int[] not allowed",
        !TypeConversionRules.isWideningReferenceConversion(
        Utils.createArrayTypeRef(BinPrimitiveType.BYTE_REF, 1),
        Utils.createArrayTypeRef(BinPrimitiveType.INT_REF, 1))
        );

    assertTrue(
        "SomeType -> java.lang.Cloneable not allowed",
        !TypeConversionRules.isWideningReferenceConversion(
        Utils.createClass("SomeType").getTypeRef(),
        project.getTypeRefForName("java.lang.Cloneable")
        ));

    assertTrue("float -> double not allowed",
        !TypeConversionRules.isNarrowingPrimitiveConversion(
        BinPrimitiveType.FLOAT_REF,
        BinPrimitiveType.DOUBLE_REF
        ));

    assertTrue("String -> String not allowed",
        !TypeConversionRules.isNarrowingPrimitiveConversion(
        Utils.createClass("java.lang.String").getTypeRef(),
        Utils.createClass("java.lang.String").getTypeRef()
        ));

    cat.info("SUCCESS");
  }

  /**
   * Tests whether isNarrowingReferenceConversion works.
   */
  public void testIsNarrowingReferenceConversion() {
    cat.info("Testing whether isNarrowingReferenceConversion works");

    Project project = Utils.createFakeProject();

    assertTrue("Object -> String",
        TypeConversionRules.isNarrowingReferenceConversion(
        project.getObjectRef(),
        Utils.createClass("java.lang.String").getTypeRef()
        ));

    {
      final BinCIType a = Utils.createClass("A");
      final BinCIType b = Utils.createClass("B", a);
      assertTrue("Class -> Subclass",
          TypeConversionRules.isNarrowingReferenceConversion(
          a.getTypeRef(),
          b.getTypeRef()
          ));
    }

    {
      final BinCIType a = Utils.createClass("A");
      final BinCIType b = Utils.createClass("B", a);
      assertTrue("Class -> Subclass (transitive)",
          TypeConversionRules.isNarrowingReferenceConversion(
          a.getTypeRef(),
          Utils.createClass("C", b).getTypeRef()
          ));
    }

    {
      final BinCIType interfaceA = Utils.createInterface("A");
      final BinCIType interfaceB = Utils.createInterface("B");
      interfaceB.getTypeRef().setSuperclass(
          interfaceA.getTypeRef());
      assertTrue("Interface -> Subinterface not allowed",
          !TypeConversionRules.isNarrowingReferenceConversion(
          interfaceA.getTypeRef(),
          interfaceB.getTypeRef()
          ));
    }

    assertTrue("SomeClass -> SomeInterface",
        TypeConversionRules.isNarrowingReferenceConversion(
        Utils.createClass("SomeClass").getTypeRef(),
        Utils.createInterface("SomeInteface").getTypeRef()
        ));

    {
      final BinCIType interfaceA = Utils.createInterface("A");
      final BinCIType interfaceB = Utils.createInterface("B");
      final BinCIType interfaceC = Utils.createInterface("C");
      interfaceB.getTypeRef().setSuperclass(interfaceA.getTypeRef());
      interfaceC.getTypeRef().setSuperclass(interfaceB.getTypeRef());
      assertTrue("Interface -> Subinterface (transitive) not allowed",
          !TypeConversionRules.isNarrowingReferenceConversion(
          interfaceA.getTypeRef(),
          interfaceC.getTypeRef()
          ));
    }

    assertTrue("Object -> SomeInterface",
        TypeConversionRules.isNarrowingReferenceConversion(
        project.getObjectRef(),
        Utils.createInterface("SomeInterface").getTypeRef())
        );

    {
      final BinCIType finalClass = Utils.createClass("FinalClass");
      finalClass.setModifiers(BinModifier.FINAL);
      assertTrue("FinalClass -> SomeInterface not allowed.",
          !TypeConversionRules.isNarrowingReferenceConversion(
          finalClass.getTypeRef(),
          Utils.createInterface("SomeInterface").getTypeRef())
          );
    }

    {
      final BinTypeRef interfaceARef =
          Utils.createInterface("A").getTypeRef();
      final BinTypeRef interfaceBRef =
          Utils.createInterface("B").getTypeRef();
      final BinCIType classTmp = Utils.createClass("Tmp");
      classTmp.getTypeRef().setInterfaces(
          new BinTypeRef[] {interfaceARef, interfaceBRef});
      assertTrue("Class -> Interface A not allowed",
          !TypeConversionRules.isNarrowingReferenceConversion(
          classTmp.getTypeRef(),
          interfaceARef
          ));

      assertTrue("Class -> Interface B not allowed",
          !TypeConversionRules.isNarrowingReferenceConversion(
          classTmp.getTypeRef(),
          interfaceBRef
          ));
    }

    assertTrue("null -> Object not allowed",
        !TypeConversionRules.isNarrowingReferenceConversion(
        null,
        project.getObjectRef()
        ));

    assertTrue("Object -> byte[]",
        TypeConversionRules.isNarrowingReferenceConversion(
        project.getObjectRef(),
        Utils.createArrayTypeRef(
        BinPrimitiveType.BYTE_REF, 1)
        ));

    assertTrue("Object -> SomeType[][]",
        TypeConversionRules.isNarrowingReferenceConversion(
        project.getObjectRef(),
        Utils.createArrayTypeRef(
        Utils.createClass("SomeType").getTypeRef(),
        2)
        ));

    assertTrue("SomeInterface -> SomeNonFinalType",
        TypeConversionRules.isNarrowingReferenceConversion(
        Utils.createInterface("SomeInterface").getTypeRef(),
        Utils.createClass("SomeNonFinalType").getTypeRef()
        ));

    {
      final BinCIType finalType = Utils.createClass("FinalType");
      finalType.setModifiers(BinModifier.FINAL);
      assertTrue("SomeInterface -> FinalType not allowed",
          !TypeConversionRules.isNarrowingReferenceConversion(
          Utils.createInterface("SomeInterface").getTypeRef(),
          finalType.getTypeRef()
          ));
    }
    {
      final BinTypeRef interfaceARef =
          Utils.createInterface("A").getTypeRef();
      final BinCIType finalClass = Utils.createClass("FinalType");
      finalClass.setModifiers(BinModifier.FINAL);
      finalClass.getTypeRef().setInterfaces(new BinTypeRef[] {interfaceARef});
      assertTrue(
          "A -> FinalClassThatImplementsA",
          TypeConversionRules.isNarrowingReferenceConversion(
          interfaceARef,
          finalClass.getTypeRef()
          ));
    }

    // TODO: Test
    // From any interface type J to any interface type K, provided that J is not
    // a subinterface of K and there is no method name m such that J and K both
    // contain a method named m with the same signature but different return
    // types.

    {
      final BinTypeRef interfaceARef =
          Utils.createInterface("InterfaceA").getTypeRef();
      final BinCIType interfaceB = Utils.createInterface("InterfaceB");
      interfaceB.getTypeRef().setSuperclass(interfaceARef);
      assertTrue("InterfaceA[] -> InterfaceB[] not allowed",
          !TypeConversionRules.isNarrowingReferenceConversion(
          Utils.createArrayTypeRef(interfaceARef, 1),
          Utils.createArrayTypeRef(interfaceB.getTypeRef(), 1)
          ));
    }

    {
      final BinCIType a = Utils.createClass("A");
      final BinCIType b = Utils.createClass("B", a);
      assertTrue("A[][] -> B[][]",
          TypeConversionRules.isNarrowingReferenceConversion(
          Utils.createArrayTypeRef(a.getTypeRef(), 2),
          Utils.createArrayTypeRef(b.getTypeRef(), 2)
          ));
    }

    assertTrue("int[] -> byte[] not allowed",
        !TypeConversionRules.isNarrowingReferenceConversion(
        Utils.createArrayTypeRef(BinPrimitiveType.INT_REF, 1),
        Utils.createArrayTypeRef(BinPrimitiveType.BYTE_REF, 1)
        ));

    cat.info("SUCCESS");
  }

  /**
   * Tests whether isStringConversion works.
   */
  public void testIsStringConversion() {
    cat.info("Testing whether isStringConversion works");

    Project project = Utils.createFakeProject();

    assertTrue("Object -> String",
        TypeConversionRules.isStringConversion(
        project.getObjectRef(),
        Utils.createClass("java.lang.String").getTypeRef()
        ));

    assertTrue("byte -> String",
        TypeConversionRules.isStringConversion(
        BinPrimitiveType.BYTE_REF,
        Utils.createClass("java.lang.String").getTypeRef()
        ));

    assertTrue("Type[] -> String",
        TypeConversionRules.isStringConversion(
        Utils.createArrayTypeRef(
        Utils.createClass("Type").getTypeRef(),
        1),
        Utils.createClass("java.lang.String").getTypeRef()
        ));

    assertTrue("null -> String",
        TypeConversionRules.isStringConversion(
        null,
        Utils.createClass("java.lang.String").getTypeRef()
        ));

    assertTrue("null -> Test not allowed",
        !TypeConversionRules.isStringConversion(
        null,
        Utils.createClass("Test").getTypeRef()
        ));

    cat.info("SUCCESS");
  }
  
  public void testIsBoxingConversion() {
    cat.info("Testing whether isBoxingConversion method works");

    Utils.createFakeProject();

    assertFalse("cannot convert from null", 
        TypeConversionRules.isBoxingConversion(
        null, 
        Utils.createClass("java.lang.Boolean").getTypeRef()
        ));
    
    assertFalse("cannot convert to null", 
        TypeConversionRules.isBoxingConversion(
        BinPrimitiveType.CHAR_REF,
        null        
        ));
    
    assertFalse("cannot boxing from not primitive type", 
        TypeConversionRules.isBoxingConversion(
        Utils.createClass("java.lang.Short").getTypeRef(),
        Utils.createClass("java.lang.Long").getTypeRef()     
        ));
    
    assertFalse("cannot boxing to not reference type", 
        TypeConversionRules.isBoxingConversion(
        BinPrimitiveType.SHORT_REF,
        BinPrimitiveType.LONG_REF       
        ));    
    
    assertTrue("boolean -> Boolean",
        TypeConversionRules.isBoxingConversion(
        BinPrimitiveType.BOOLEAN_REF,
        Utils.createClass("java.lang.Boolean").getTypeRef()
        ));
    
    assertTrue("byte -> Byte",
        TypeConversionRules.isBoxingConversion(
        BinPrimitiveType.BYTE_REF,
        Utils.createClass("java.lang.Byte").getTypeRef()
        ));

    assertTrue("char -> Character",
        TypeConversionRules.isBoxingConversion(
        BinPrimitiveType.CHAR_REF,
        Utils.createClass("java.lang.Character").getTypeRef()
        ));
    
    assertTrue("short -> Short",
        TypeConversionRules.isBoxingConversion(
        BinPrimitiveType.SHORT_REF,
        Utils.createClass("java.lang.Short").getTypeRef()
        ));
    
    assertTrue("int -> Integer",
        TypeConversionRules.isBoxingConversion(
        BinPrimitiveType.INT_REF,
        Utils.createClass("java.lang.Integer").getTypeRef()
        ));
    
    assertTrue("long -> Long",
        TypeConversionRules.isBoxingConversion(
        BinPrimitiveType.LONG_REF,
        Utils.createClass("java.lang.Long").getTypeRef()
        ));
    
    assertTrue("float -> Float",
        TypeConversionRules.isBoxingConversion(
        BinPrimitiveType.FLOAT_REF,
        Utils.createClass("java.lang.Float").getTypeRef()
        ));    
    
    assertTrue("double -> Double",
        TypeConversionRules.isBoxingConversion(
        BinPrimitiveType.DOUBLE_REF,
        Utils.createClass("java.lang.Double").getTypeRef()
        ));    
    
    cat.info("SUCCESS");
  }

  public void testIsUnboxingConversion() {
    cat.info("Testing whether isUnboxingConversion method works");

    Utils.createFakeProject();
    
    assertFalse("cannot convert to null", 
		TypeConversionRules.isUnboxingConversion(
		BinPrimitiveType.CHAR_REF,
		null        
		));
	
	assertFalse("cannot unboxing from not reference type", 
		TypeConversionRules.isUnboxingConversion(
		BinPrimitiveType.SHORT_REF,
		BinPrimitiveType.LONG_REF     
		  ));
	
	assertFalse("cannot unboxing to not primitive type", 
		TypeConversionRules.isUnboxingConversion(
		Utils.createClass("java.lang.Short").getTypeRef(),
		Utils.createClass("java.lang.Long").getTypeRef()       
		));    	
	
	assertTrue("Boolean -> boolean",
		TypeConversionRules.isUnboxingConversion(
		Utils.createClass("java.lang.Boolean").getTypeRef(),
		BinPrimitiveType.BOOLEAN_REF
		));
	
	assertTrue("Byte -> byte",
	    TypeConversionRules.isUnboxingConversion(
	    Utils.createClass("java.lang.Byte").getTypeRef(),
		BinPrimitiveType.BYTE_REF
	    ));
	
	assertTrue("Character -> char",
		TypeConversionRules.isUnboxingConversion(
		Utils.createClass("java.lang.Character").getTypeRef(),
		BinPrimitiveType.CHAR_REF
		));
	
	assertTrue("Short -> short",
		TypeConversionRules.isUnboxingConversion(
		Utils.createClass("java.lang.Short").getTypeRef(),
		BinPrimitiveType.SHORT_REF		
		));
	
	assertTrue("Integer -> int",
		TypeConversionRules.isUnboxingConversion(
	    Utils.createClass("java.lang.Integer").getTypeRef(),
	    BinPrimitiveType.INT_REF
		));
	
	assertTrue("Long -> long",
		TypeConversionRules.isUnboxingConversion(
	    Utils.createClass("java.lang.Long").getTypeRef(),
	    BinPrimitiveType.LONG_REF
		));
	
	assertTrue("Float -> float",
		TypeConversionRules.isUnboxingConversion(
	    Utils.createClass("java.lang.Float").getTypeRef(),
	    BinPrimitiveType.FLOAT_REF
		));    
	
	assertTrue("Double -> double",
		TypeConversionRules.isUnboxingConversion(
	    Utils.createClass("java.lang.Double").getTypeRef(),
	    BinPrimitiveType.DOUBLE_REF
		));    
        
    cat.info("SUCCESS");
  }
  
  public void testIsBoxingConversion_of_boolean_WithSubtyping1() {
    cat.info("Testing whether isBoxingConversion with subtyping");
    Project project = Utils.createFakeProject();

	assertTrue("boolean -> Object",
		TypeConversionRules.isBoxingConversion(
	    BinPrimitiveType.BOOLEAN_REF,
	    project.getObjectRef()
		));   
    cat.info("SUCCESS");
  }
  
  public void testIsBoxingConversion_of_boolean_WithSubtyping2() {
    cat.info("Testing whether isBoxingConversion with subtyping");
	assertTrue("boolean -> Serializable",
		TypeConversionRules.isBoxingConversion(
		BinPrimitiveType.BOOLEAN_REF,
		Utils.createClass("java.io.Serializable").getTypeRef()
	    ));   
    cat.info("SUCCESS");
  }
  
  public void testIsBoxingConversion_of_boolean_WithSubtyping3() {
    cat.info("Testing whether isBoxingConversion with subtyping");
	assertFalse("boolean -> Number",
		TypeConversionRules.isBoxingConversion(
		BinPrimitiveType.BOOLEAN_REF,
		Utils.createClass("java.lang.Number").getTypeRef()
		));
    cat.info("SUCCESS");
  }
  
  public void testIsBoxingConversion_of_boolean_WithSubtyping4() {
    cat.info("Testing whether isBoxingConversion with subtyping");
	assertFalse("boolean not -> Comparable",
		TypeConversionRules.isBoxingConversion(
	    BinPrimitiveType.BOOLEAN_REF,
	    Utils.createClass("java.lang.Comparable").getTypeRef()
		));
    cat.info("SUCCESS");
  }
    
  public void testIsBoxingConversion_of_char_WithSubtyping1() {
    cat.info("Testing whether isBoxingConversion with subtyping");
    Project project = Utils.createFakeProject();

	assertTrue("char -> Object",
		TypeConversionRules.isBoxingConversion(
		BinPrimitiveType.CHAR_REF,
		project.getObjectRef()
		));
    cat.info("SUCCESS");
  }
  
  public void testIsBoxingConversion_of_char_WithSubtyping2() {
    cat.info("Testing whether isBoxingConversion with subtyping");
	assertTrue("char -> Serializable",
		TypeConversionRules.isBoxingConversion(
		BinPrimitiveType.CHAR_REF,
	    Utils.createClass("java.io.Serializable").getTypeRef()
		));
    cat.info("SUCCESS");
  }
  
  public void testIsBoxingConversion_of_char_WithSubtyping3() {
    cat.info("Testing whether isBoxingConversion with subtyping");
	assertTrue("char -> Comparable",
		TypeConversionRules.isBoxingConversion(
		BinPrimitiveType.CHAR_REF,
	    Utils.createClass("java.lang.Comparable").getTypeRef()
		));
    cat.info("SUCCESS");
  }
  
  public void testIsBoxingConversion_of_char_WithSubtyping4() {
    cat.info("Testing whether isBoxingConversion with subtyping");
	assertFalse("char not -> Number",
		TypeConversionRules.isBoxingConversion(
		BinPrimitiveType.CHAR_REF,
	    Utils.createClass("java.lang.Number").getTypeRef()
		));
    cat.info("SUCCESS");
  }
  
  public void testIsBoxingConversion_of_byte_WithSubtyping1() {
    cat.info("Testing whether isBoxingConversion with subtyping");
    Project project = Utils.createFakeProject();

	//byte
	assertTrue("byte -> Object",
		TypeConversionRules.isBoxingConversion(
		BinPrimitiveType.BYTE_REF,
	  project.getObjectRef()
		));
    cat.info("SUCCESS");
  }
  
  public void testIsBoxingConversion_of_byte_WithSubtyping2() {
    cat.info("Testing whether isBoxingConversion with subtyping");
	assertTrue("byte -> Serializable",
		TypeConversionRules.isBoxingConversion(
		BinPrimitiveType.BYTE_REF,
	    Utils.createClass("java.io.Serializable").getTypeRef()
		));
    cat.info("SUCCESS");
  }
  
  public void testIsBoxingConversion_of_byte_WithSubtyping3() {
    cat.info("Testing whether isBoxingConversion with subtyping");
	assertTrue("byte -> Number",
		TypeConversionRules.isBoxingConversion(
		BinPrimitiveType.BYTE_REF,
	    Utils.createClass("java.lang.Number").getTypeRef()
		));
    cat.info("SUCCESS");
  }
  
  public void testIsBoxingConversion_of_byte_WithSubtyping4() {
    cat.info("Testing whether isBoxingConversion with subtyping");
	assertTrue("byte -> Comparable",
		TypeConversionRules.isBoxingConversion(
		BinPrimitiveType.BYTE_REF,
	    Utils.createClass("java.lang.Comparable").getTypeRef()
		));
    cat.info("SUCCESS");
  }

  // ======= tests for short type
  public void testIsBoxingConversion_of_short_WithSubtyping1() {
    cat.info("Testing whether isBoxingConversion with subtyping");
    Project project = Utils.createFakeProject();

	assertTrue("short -> Object",
		TypeConversionRules.isBoxingConversion(
		BinPrimitiveType.SHORT_REF,
	  project.getObjectRef()
		));
    cat.info("SUCCESS");
  }
  
  public void testIsBoxingConversion_of_short_WithSubtyping2() {
    cat.info("Testing whether isBoxingConversion with subtyping");
	assertTrue("short -> Serializable",
		TypeConversionRules.isBoxingConversion(
		BinPrimitiveType.SHORT_REF,
	    Utils.createClass("java.io.Serializable").getTypeRef()
		));
    cat.info("SUCCESS");
  }
  
  public void testIsBoxingConversion_of_short_WithSubtyping3() {
    cat.info("Testing whether isBoxingConversion with subtyping");
	assertTrue("short -> Number",
		TypeConversionRules.isBoxingConversion(
		BinPrimitiveType.SHORT_REF,
	    Utils.createClass("java.lang.Number").getTypeRef()
		));
    cat.info("SUCCESS");
  }
  
  public void testIsBoxingConversion_of_short_WithSubtyping4() {
    cat.info("Testing whether isBoxingConversion with subtyping");
	assertTrue("short -> Comparable",
		TypeConversionRules.isBoxingConversion(
		BinPrimitiveType.SHORT_REF,
	    Utils.createClass("java.lang.Comparable").getTypeRef()
		));
    cat.info("SUCCESS");
  }  
  
  // ======= tests for int type
  public void testIsBoxingConversion_of_int_WithSubtyping1() {
    cat.info("Testing whether isBoxingConversion with subtyping");
    Project project = Utils.createFakeProject();

	assertTrue("int -> Object",
		TypeConversionRules.isBoxingConversion(
		BinPrimitiveType.INT_REF,
	  project.getObjectRef()
		));
    cat.info("SUCCESS");
  }
  
  public void testIsBoxingConversion_of_int_WithSubtyping2() {
    cat.info("Testing whether isBoxingConversion with subtyping");
	assertTrue("int -> Serializable",
		TypeConversionRules.isBoxingConversion(
		BinPrimitiveType.INT_REF,
	    Utils.createClass("java.io.Serializable").getTypeRef()
		));
    cat.info("SUCCESS");
  }
  
  public void testIsBoxingConversion_of_int_WithSubtyping3() {
    cat.info("Testing whether isBoxingConversion with subtyping");
	assertTrue("int -> Number",
		TypeConversionRules.isBoxingConversion(
		BinPrimitiveType.INT_REF,
	    Utils.createClass("java.lang.Number").getTypeRef()
		));
    cat.info("SUCCESS");
  }
  
  public void testIsBoxingConversion_of_int_WithSubtyping4() {
    cat.info("Testing whether isBoxingConversion with subtyping");
	assertTrue("int -> Comparable",
		TypeConversionRules.isBoxingConversion(
		BinPrimitiveType.INT_REF,
	    Utils.createClass("java.lang.Comparable").getTypeRef()
		));
    cat.info("SUCCESS");
  }    
 
  // ======= tests for long type
  public void testIsBoxingConversion_of_long_WithSubtyping1() {
    cat.info("Testing whether isBoxingConversion with subtyping");
    Project project = Utils.createFakeProject();
	assertTrue("long -> Object",
		TypeConversionRules.isBoxingConversion(
		BinPrimitiveType.LONG_REF,
	  project.getObjectRef()
		));
    cat.info("SUCCESS");
  }
  
  public void testIsBoxingConversion_of_long_WithSubtyping2() {
    cat.info("Testing whether isBoxingConversion with subtyping");
	assertTrue("long -> Serializable",
		TypeConversionRules.isBoxingConversion(
		BinPrimitiveType.LONG_REF,
	    Utils.createClass("java.io.Serializable").getTypeRef()
		));
    cat.info("SUCCESS");
  }
  
  public void testIsBoxingConversion_of_long_WithSubtyping3() {
    cat.info("Testing whether isBoxingConversion with subtyping");
	assertTrue("long -> Number",
		TypeConversionRules.isBoxingConversion(
		BinPrimitiveType.LONG_REF,
	    Utils.createClass("java.lang.Number").getTypeRef()
		));
    cat.info("SUCCESS");
  }
  
  public void testIsBoxingConversion_of_long_WithSubtyping4() {
    cat.info("Testing whether isBoxingConversion with subtyping");
	assertTrue("long -> Comparable",
		TypeConversionRules.isBoxingConversion(
		BinPrimitiveType.LONG_REF,
	    Utils.createClass("java.lang.Comparable").getTypeRef()
		));
    cat.info("SUCCESS");
  }    
  
  // ======= tests for float type
  public void testIsBoxingConversion_of_float_WithSubtyping1() {
    cat.info("Testing whether isBoxingConversion with subtyping");
    Project project = Utils.createFakeProject();
	assertTrue("float -> Object",
		TypeConversionRules.isBoxingConversion(
		BinPrimitiveType.FLOAT_REF,
	  project.getObjectRef()
		));
    cat.info("SUCCESS");
  }
  
  public void testIsBoxingConversion_of_float_WithSubtyping2() {
    cat.info("Testing whether isBoxingConversion with subtyping");
	assertTrue("float -> Serializable",
		TypeConversionRules.isBoxingConversion(
		BinPrimitiveType.FLOAT_REF,
	    Utils.createClass("java.io.Serializable").getTypeRef()
		));
    cat.info("SUCCESS");
  }
  
  public void testIsBoxingConversion_of_float_WithSubtyping3() {
    cat.info("Testing whether isBoxingConversion with subtyping");
	assertTrue("float -> Number",
		TypeConversionRules.isBoxingConversion(
		BinPrimitiveType.FLOAT_REF,
	    Utils.createClass("java.lang.Number").getTypeRef()
		));
    cat.info("SUCCESS");
  }
  
  public void testIsBoxingConversion_of_float_WithSubtyping4() {
    cat.info("Testing whether isBoxingConversion with subtyping");
	assertTrue("float -> Comparable",
		TypeConversionRules.isBoxingConversion(
		BinPrimitiveType.FLOAT_REF,
	    Utils.createClass("java.lang.Comparable").getTypeRef()
		));
    cat.info("SUCCESS");
  }     
 
  // ======= tests for double type
  public void testIsBoxingConversion_of_double_WithSubtyping1() {
    cat.info("Testing whether isBoxingConversion with subtyping");
    Project project = Utils.createFakeProject();

	assertTrue("double -> Object",
		TypeConversionRules.isBoxingConversion(
		BinPrimitiveType.DOUBLE_REF,
	  project.getObjectRef()
		));
    cat.info("SUCCESS");
  }
  
  public void testIsBoxingConversion_of_double_WithSubtyping2() {
    cat.info("Testing whether isBoxingConversion with subtyping");
	assertTrue("double -> Serializable",
		TypeConversionRules.isBoxingConversion(
		BinPrimitiveType.DOUBLE_REF,
	    Utils.createClass("java.io.Serializable").getTypeRef()
		));
    cat.info("SUCCESS");
  }
  
  public void testIsBoxingConversion_of_double_WithSubtyping3() {
    cat.info("Testing whether isBoxingConversion with subtyping");
	assertTrue("double -> Number",
		TypeConversionRules.isBoxingConversion(
		BinPrimitiveType.DOUBLE_REF,
	    Utils.createClass("java.lang.Number").getTypeRef()
		));
    cat.info("SUCCESS");
  }
  
  public void testIsBoxingConversion_of_double_WithSubtyping4() {
    cat.info("Testing whether isBoxingConversion with subtyping");
	assertTrue("double -> Comparable",
		TypeConversionRules.isBoxingConversion(
		BinPrimitiveType.DOUBLE_REF,
	    Utils.createClass("java.lang.Comparable").getTypeRef()
		));
    cat.info("SUCCESS");
  }    
  
  public void testIsBoxingConversion_WithSubtyping1() {
    cat.info("Testing whether isBoxingConversion with subtyping");
	assertFalse("short -> Byte",
		TypeConversionRules.isBoxingConversion(
		BinPrimitiveType.SHORT_REF,
	    Utils.createClass("java.lang.Byte").getTypeRef()
		));
	cat.info("SUCCESS"); 
  }
  public void testIsBoxingConversion_WithSubtyping2() {
    cat.info("Testing whether isBoxingConversion with subtyping");	
	assertFalse("Short -> byte",
		TypeConversionRules.isBoxingConversion(
		Utils.createClass("java.lang.Short").getTypeRef(),
		BinPrimitiveType.BYTE_REF
		));
	cat.info("SUCCESS"); 
  }
  
  public void testIsBoxingConversion_WithSubtyping3() {
    cat.info("Testing whether isBoxingConversion with subtyping");
	assertFalse("int -> Short",
		TypeConversionRules.isBoxingConversion(
		BinPrimitiveType.INT_REF,
	    Utils.createClass("java.lang.Short").getTypeRef()
		));
	cat.info("SUCCESS");   
  }
  
  public void testIsBoxingConversion_WithSubtyping4() {
    cat.info("Testing whether isBoxingConversion with subtyping");	
	assertFalse("short -> Integer",
		TypeConversionRules.isBoxingConversion(
		Utils.createClass("java.lang.Integer").getTypeRef(),
		BinPrimitiveType.SHORT_REF
		));
	cat.info("SUCCESS");
  }
}
