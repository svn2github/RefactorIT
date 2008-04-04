/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel;


import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.parser.FastJavaLexer;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;


/**
 * Utility class encapsulating JLS type conversion rules. See JLS Chapter 5 for
 * more details.
 */
public final class TypeConversionRules {

  private static final HashMap unboxing = new HashMap(8, 1f);
  private static final HashMap boxing = new HashMap(8, 1f);

  private static final class ConversionsContainer {
    private final HashSet set;
    private String mainType;

    private ConversionsContainer(Collection set) {
      this.set = new HashSet(set);
    }

    public ConversionsContainer(String str, Collection set) {
      this(set);
      this.set.add(str);
      this.mainType = str;
    }

    public ConversionsContainer(String[] names) {
      this(Arrays.asList(names));
    }

    public String getMainType() {
      return this.mainType;
    }

    public int hashCode() {
      return super.hashCode();
    }

    public boolean equals(Object obj) {
      return set.contains(obj);
    }
  }

  private static final HashSet booleanBoxingConversions = new HashSet(2, 1f);
  private static final HashSet charBoxingConversions = new HashSet(3, 1f);
  private static final HashSet numberBoxingConversions = new HashSet(5, 1f);

  private static final HashMap widening = new HashMap(6, 1f);

  static {
    booleanBoxingConversions.add("java.lang.Object");
    booleanBoxingConversions.add("java.io.Serializable");

    charBoxingConversions.add("java.lang.Object");
    charBoxingConversions.add("java.io.Serializable");
    charBoxingConversions.add("java.lang.Comparable");

    numberBoxingConversions.add("java.lang.Object");
    numberBoxingConversions.add("java.io.Serializable");
    numberBoxingConversions.add("java.lang.Comparable");
    numberBoxingConversions.add("java.lang.Number");

    unboxing.put("java.lang.Boolean", "boolean");
    unboxing.put("java.lang.Byte", "byte");
    unboxing.put("java.lang.Character", "char");
    unboxing.put("java.lang.Short", "short");
    unboxing.put("java.lang.Integer", "int");
    unboxing.put("java.lang.Long", "long");
    unboxing.put("java.lang.Float", "float");
    unboxing.put("java.lang.Double", "double");

    boxing.put("boolean", new ConversionsContainer("java.lang.Boolean", booleanBoxingConversions));
    boxing.put("char", new ConversionsContainer("java.lang.Character", charBoxingConversions));
    boxing.put("byte", new ConversionsContainer("java.lang.Byte", numberBoxingConversions));
    boxing.put("short", new ConversionsContainer("java.lang.Short", numberBoxingConversions));
    boxing.put("int", new ConversionsContainer("java.lang.Integer", numberBoxingConversions));
    boxing.put("long", new ConversionsContainer("java.lang.Long", numberBoxingConversions));
    boxing.put("float", new ConversionsContainer("java.lang.Float", numberBoxingConversions));
    boxing.put("double", new ConversionsContainer("java.lang.Double", numberBoxingConversions));

    widening.put("byte", new ConversionsContainer(new String[] {"short", "int", "long", "float", "double"}));
    widening.put("char", new ConversionsContainer(new String[] {"int", "long", "float", "double"}));
    widening.put("short", new ConversionsContainer(new String[] {"int", "long", "float", "double"}));
    widening.put("int", new ConversionsContainer(new String[] {"long", "float", "double"}));
    widening.put("long", new ConversionsContainer(new String[] {"float", "double"}));
    widening.put("float", new ConversionsContainer(new String[] {"double"}));
  }

  /** Hidden constructor. */
  private TypeConversionRules() {}

  public static boolean isSuitablePrimitiveConversion(final BinTypeRef from,
      final BinTypeRef to) {
    return (from.isPrimitiveType() && to.isPrimitiveType() 
        && (isWideningPrimitiveConversion(from, to) || 
            isNarrowingPrimitiveConversion(from, to)));
  }
  
  /**
   * Checks whether identity conversion from the specified type is allowed to
   * specified type. See JLS 5.1.1 for more details.
   *
   * @param from
   *          type to convert. <code>null</code> means <code>null</code> is
   *          to be converted.
   * @param to
   *          desired type.
   *
   * @return <code>true</code> if and only if conversion is allowed,
   *         <code>false</code> otherwise.
   */
  public static boolean isIdentityConversion(final BinTypeRef from,
      final BinTypeRef to) {
    //JLS: A conversion from a type to that same type is permitted for any type.
    if (from == null) {
      return false; // Cannot convert null.
    }

    return from == to || from.equals(to);
  }

  /**
   * Checks whether widening primitive conversion from specified type is allowed
   * to specified type. See JLS 5.1.2 for more details.
   *
   * @param from
   *          type to convert. <code>null</code> means <code>null</code> is
   *          to be converted.
   * @param to
   *          desired type.
   *
   * @return <code>true</code> if and only if conversion is allowed,
   *         <code>false</code> otherwise.
   */
  public static boolean isWideningPrimitiveConversion(final BinTypeRef from,
      final BinTypeRef to) {

    // JLS:
    // * byte to short, int, long, float, or double
    // * short to int, long, float, or double
    // * char to int, long, float, or double
    // * int to long, float, or double
    // * long to float or double
    // * float to double

    if (from == null) {
      return false; // Cannot convert null.
    }

    if (from.isReferenceType() || to.isReferenceType()) {
      return false; // Conversion allowed only between primitive types
    }

    final Object toCheck = widening.get(from.getQualifiedName());
    return toCheck != null && toCheck.equals(to.getQualifiedName());
  }

  /**
   * Checks whether narrowing primitive conversion from specified type is
   * allowed to specified type. See JLS 5.1.3 for more details.
   *
   * @param from
   *          type to convert. <code>null</code> means <code>null</code> is
   *          to be converted.
   * @param to
   *          desired type.
   *
   * @return <code>true</code> if and only if conversion is allowed,
   *         <code>false</code> otherwise.
   */
  public static boolean isNarrowingPrimitiveConversion(final BinTypeRef from,
      final BinTypeRef to) {

    // JLS:
    // * byte to char
    // * short to byte or char
    // * char to byte or short
    // * int to byte, short, or char
    // * long to byte, short, char, or int
    // * float to byte, short, char, int, or long
    // * double to byte, short, char, int, long, or float

    if (from == null) {
      return false; // Cannot convert null.
    }

    if (from.isReferenceType() || to.isReferenceType()) {
      return false; // Conversion allowed only between primitive types
    }

    final String fromFqn = from.getQualifiedName();
    final String toFqn = to.getQualifiedName();

    if ("byte".equals(fromFqn)) {
      return inList(toFqn,
          new String[] {"char"});
    } else if ("short".equals(fromFqn)) {
      return inList(toFqn,
          new String[] {"byte", "char"});
    } else if ("char".equals(fromFqn)) {
      return inList(toFqn,
          new String[] {"byte", "short"});
    } else if ("int".equals(fromFqn)) {
      return inList(toFqn,
          new String[] {"byte", "short", "char"});
    } else if ("long".equals(fromFqn)) {
      return inList(toFqn,
          new String[] {"byte", "short", "char", "int"});
    } else if ("float".equals(fromFqn)) {
      return inList(toFqn,
          new String[] {"byte", "short", "char", "int", "long"});
    } else if ("double".equals(fromFqn)) {
      return inList(
          toFqn,
          new String[] {"byte", "short", "char", "int", "long", "float"});
    } else {
      return false; // Not within the 23 allowed conversions.
    }
  }

  /**
   * Checks whether widening reference conversion from specified type is allowed
   * to specified type. See JLS 5.1.4 for more details.
   *
   * @param from
   *          type to convert. <code>null</code> means <code>null</code> is
   *          to be converted.
   * @param to
   *          desired type.
   *
   * @return <code>true</code> if and only if conversion is allowed,
   *         <code>false</code> otherwise.
   */
  public static boolean isWideningReferenceConversion(final BinTypeRef from,
      final BinTypeRef to) {

    // JLS:
    // * From any class type S to any class type T, provided that S is a
    //   subclass of T. (An important special case is that there is a widening
    //   conversion to the class type Object from any other class type.)
    // * From any class type S to any interface type K, provided that S
    //   implements K.
    // * From the null type to any class type, interface type, or array type.
    // * From any interface type J to any interface type K, provided that J is a
    //   subinterface of K.
    // * From any interface type to type Object.
    // * From any array type to type Object.
    // * From any array type to type Cloneable.
    // * From any array type to type java.io.Serializable
    // * From any array type SC[] to any array type TC[], provided that SC and
    //   TC are reference types and there is a widening conversion from SC to
    //   TC.

    if (Assert.enabled) {
      Assert.must(to != null, "\"to\" shouldn't be null");
    } else {
      // FIXME report to user
      if (to == null) {
        new Exception("\"to\" shouldn't be null").printStackTrace();
        return false;
      }
    }

//System.err.println("from: " + from + ", to: " + to);
    if (from == null) {
      return to.isReferenceType(); // Allowed only to non-primitive types.
    }

    if (from.isPrimitiveType() || to.isPrimitiveType()) {
      return false; // Conversion allowed only between non-primitive types
    }

    final String toFqn = to.getQualifiedName();

    // B -> Object
    // B[] -> Object
    if (Project.OBJECT.equals(toFqn)
        || (to.getBinType().isTypeParameter() &&
        to.getProject().getObjectRef().equals(to.getSuperclass()))) {
      return true; // Conversion to java.lang.Object allowed
    }

    // JAVA5: quick hack of type parameters
    if (isDerivedFromAll(from, to)) {
      return true;
    }

    if (from.isDerivedFrom(to)) {
      return true; // Conversion from subclass to its superclass.
      // Conversion from class to interface it implements
      // Conversion from subinterface to its superinterface.
    }

    if (from.isArray()) {
      if (("java.lang.Cloneable".equals(toFqn))
          || ("java.io.Serializable".equals(toFqn))) {

        return true;
      }

      // B[] -> A[]
      // B[][] -> A[][]...
      if (to.isArray()) {
        final BinTypeRef fromElementType
            = ((BinArrayType) from.getBinType()).getArrayType();
        final BinTypeRef toElementType
            = ((BinArrayType) to.getBinType()).getArrayType();

        if (fromElementType.isReferenceType()
            && toElementType.isReferenceType()
            && ((BinArrayType) from.getBinType()).getDimensions()
            == ((BinArrayType) to.getBinType()).getDimensions()) {
          // .. are reference types
          if (isWideningReferenceConversion(fromElementType, toElementType)) {
            // ... there is a widening conversion
            return true;
          }
        }

        // B[][] -> Object[]
        if (((BinArrayType) from.getBinType()).getDimensions() >
            ((BinArrayType) to.getBinType()).getDimensions()
            && toElementType.isReferenceType()
            && (toElementType.equals(toElementType.getProject().getObjectRef())

        // JAVA5: one more hack :(
            || (toElementType.getBinType().isTypeParameter()
            && toElementType.getInterfaces().length == 0
            && toElementType.getProject().getObjectRef().equals(
            toElementType.getSuperclass())))) {
          return true;
        }
      }
    }

    return false;
  }

  public static boolean isDerivedFromAll(
      final BinTypeRef from, final BinTypeRef to) {

    if (to.getBinType().isTypeParameter() || to.getBinType().isWildcard()) {
      if (isDerivedFromAll(from, to.getSupertypes())) {
        return true;
      }
    } else if (from.getBinType().isTypeParameter() || from.getBinType().isWildcard()) {
      if (to.getBinType().isTypeParameter() || to.getBinType().isWildcard()) {
        if (isDerivedFromAll(from.getSupertypes(), to.getSupertypes())) {
          return true;
        }
      } else {
        if (isDerivedFromAll(from.getSupertypes(), to)) {
          return true;
        }
      }
    }
    return false;
  }

  // JAVA5: this is a quick intuitive hack and haven't checked JLS yet
  public static boolean isDerivedFromAll(
      final BinTypeRef from, final BinTypeRef[] to) {
    for (int i = 0, max = to.length; i < max; i++) {
      if (!isMethodInvocationConversion(from, to[i])) {
        return false;
      }
    }

    return true;
  }

  // JAVA5: this is a quick intuitive hack and haven't checked JLS yet
  public static boolean isDerivedFromAll(final BinTypeRef[] from, final BinTypeRef to) {
    for (int i = 0, max = from.length; i < max; i++) {
      if (!isMethodInvocationConversion(from[i], to)) {
        return false;
      }
    }

    return true;
  }

  // JAVA5: this is a quick intuitive hack and haven't checked JLS yet
  public static boolean isDerivedFromAll(
      final BinTypeRef[] from, final BinTypeRef[] to) {
    int ok = 0;
    outer: for (int i = 0, max = from.length; i < max; i++) {
      for (int k = 0, maxK = to.length; k < maxK; k++) {
        if (isMethodInvocationConversion(from[i], to[k])) {
          ++ok;
          continue outer;
        }
      }
    }

    return ok >= from.length;
  }

  /**
   * Checks whether narrowing reference conversion from specified type is
   * allowed to specified type. See JLS 5.1.5 for more details.
   *
   * @param from
   *          type to convert. <code>null</code> means <code>null</code> is
   *          to be converted.
   * @param to
   *          desired type.
   *
   * @return <code>true</code> if and only if conversion is allowed,
   *         <code>false</code> otherwise.
   */
  public static boolean isNarrowingReferenceConversion(final BinTypeRef from,
      final BinTypeRef to) {
    // JLS:
    // * From any class type S to any class type T, provided that S is a
    //   superclass of T. (An important special case is that there is a
    //   narrowing conversion from the class type Object to any other class
    //   type.)
    // * From any class type S to any interface type K, provided that S is not
    //   final and does not implement K. (An important special case is that
    //   there is a narrowing conversion from the class type Object to any
    //   interface type.)
    // * From type Object to any array type.
    // * From type Object to any interface type.
    // * From any interface type J to any class type T that is not final.
    // * From any interface type J to any class type T that is final, provided
    //   that T implements J.
    // * From any interface type J to any interface type K, provided that J is
    //   not a subinterface of K and there is no method name m such that J and
    //   K both contain a method named m with the same signature but different
    //   return types.
    // * From any array type SC[] to any array type TC[], provided that SC and
    //   TC are reference types and there is a narrowing conversion from SC to
    //   TC.

    if (from == null) {
      return false; // Cannot convert null
    }

    if ((from.isPrimitiveType()) || (to.isPrimitiveType())) {
      return false; // Conversion allowed only between non-primitive types
    }

    final String fromFqn = from.getQualifiedName();

    if (Project.OBJECT.equals(fromFqn)) {
      return true; // Conversion from java.lang.Object allowed
    }

    final BinCIType fromType = from.getBinCIType();
    final BinCIType toType = to.getBinCIType();
    // JAVA5: what about enums?
    if (fromType.isClass()) {
      if (toType.isClass()) {
        if (to.isDerivedFrom(from)) {
          return true; // Conversion from class to its derived class.
        }
      }

      if (!fromType.isFinal()) {
        if (toType.isInterface()) {
          if (!from.isDerivedFrom(to)) {
            return true; // To is non-final class that doesn't implement the
            // destination interface.
          }
        }
      }
    } else if (fromType.isInterface()) {
      if (toType.isClass()) {
        if (toType.isFinal()) {
          if (to.isDerivedFrom(from)) {
            return true; // Final class that implements the interface
          }
        } else {
          return true; // Interface can be converted to any class type that is
          // not final.
        }
      } else if (toType.isInterface()) {
        // TODO: From any interface type J to any interface type K, provided
        //       that J is not a subinterface of K and there is no method name
        //       m such that J and K both contain a method named m with the same
        //       signature but different return types.
      }
    }

    if (from.isArray()) {
      // A[] -> B[]
      // A[][] -> B[][]...
      if (to.isArray()) {
        final BinTypeRef fromElementType =
            ((BinArrayType) from.getBinType()).getArrayType();
        final BinTypeRef toElementType =
            ((BinArrayType) to.getBinType()).getArrayType();

        if (fromElementType.isReferenceType()
            && toElementType.isReferenceType()) {
          // .. are reference types

          if (isNarrowingReferenceConversion(fromElementType, toElementType)) {
            // ... there is a narrowing conversion

            return true;
          }
        }
      }
    }

    return false;
  }

  /**
   * Checks whether string conversion from specified type is allowed to
   * specified type. See JLS 5.1.6 for more details.
   *
   * @param from
   *          type to convert. <code>null</code> means <code>null</code> is
   *          to be converted.
   * @param to
   *          desired type.
   *
   * @return <code>true</code> if and only if conversion is allowed,
   *         <code>false</code> otherwise.
   */
  public static boolean isStringConversion(final BinTypeRef from,
      final BinTypeRef to) {
    // JLS:
    // There is a string conversion to type String from every other
    // type, including the null type.
    return to.isString();
  }

  /**
   * Checks whether method invocation conversion from specified type is allowed
   * to specified type. See JLS 5.3 for more details.
   *
   * @param from
   *          type to convert. <code>null</code> means <code>null</code> is
   *          to be converted.
   * @param to
   *          desired type.
   * @return <code>true</code> if and only if conversion is allowed,
   *         <code>false</code> otherwise.
   */
  public static boolean isMethodInvocationConversion(final BinTypeRef from,
      final BinTypeRef to) {
    // JLS:
    // Method invocation conversions specifically do not include the implicit
    // narrowing of integer constants which is part of assignment conversion
    // (�5.2). The designers of the Java programming language felt that
    // including these implicit narrowing conversions would add additional
    // complexity to the overloaded method matching resolution process
    // (�15.12.2).

    // this one shouldn't be checked since we anyway check only for identity
    // and widening


    // Method invocation contexts allow the use of an identity conversion
    // (�5.1.1), a widening primitive conversion (�5.1.2), or a widening
    // reference conversion (�5.1.4).
    return /*isIdentityConversion(from, to)
        ||*/ isSubtypingConversion(from, to)
        || isAutoboxingConversion(from, to);
  }

  /**
   * Checks whether conversion from specified type is allowed to specified type
   * in case if subtyping. See JLS3 �4.10 for more details. �4.10.3 is
   * automatically done by �4.10.1 and �4.10.2 support.
   *
   * @param from
   *          type to convert. <code>null</code> means <code>null</code> is
   *          to be converted.
   * @param to
   *          desired type.
   *
   * @return <code>true</code> if and only if conversion is allowed,
   *         <code>false</code> otherwise.
   */
  public static boolean isSubtypingConversion(BinTypeRef from, BinTypeRef to) {
    return isIdentityConversion(from, to)
    || isWideningPrimitiveConversion(from, to)
    || isWideningReferenceConversion(from, to);
  }

  /**
   * Checks whether conversion from specified type is allowed to specified type
   * in case of Autoboxing enabled. See JLS 5.1.7/5.1.8 for more details.
   *
   * @param from
   *          type to convert. <code>null</code> means <code>null</code> is
   *          to be converted.
   * @param to
   *          desired type.
   *
   * @return <code>true</code> if and only if conversion is allowed,
   *         <code>false</code> otherwise.
   */
  public static boolean isAutoboxingConversion(
      final BinTypeRef from, final BinTypeRef to) {
// there is a high probability that mode in options is equal to lexer mode
//    Project project;
//    if (to != null && to.isReferenceType()) {
//      project = Project.getProjectFor(to);
//      if (project == null && from != null && from.isReferenceType()) {
//        project = Project.getProjectFor(from);
//      }
//    } else {
//      project = null;
//    }
//
//    if (project != null) { // null when both are null or primitives
//      int mode = project.getOptions().getJvmMode();
//      switch (mode) {
//        case FastJavaLexer.JVM_13:
//        case FastJavaLexer.JVM_14:
//          return false;
//
//        default:
//          break;
//      }
//    }
    int mode = FastJavaLexer.getActualJvmMode(); // there is a high probability that mode in options is equal to lexer mode
    if (mode != FastJavaLexer.JVM_50) {
      return false;
    }

    return isBoxingConversion(from, to) || isUnboxingConversion(from, to);
  }

  /**
   * Checks whether boxing conversion from specified type is possible to
   * specified type. See JLS3(Java 5) 5.1.7 Boxing Conversions. Boxing
   * conversion converts values of primitive type to corresponding values of
   * reference type
   *
   * @param from
   *          type to convert. Primitive type is allowed and not null
   * @param to
   *          desired type. Reference type is allowed and not null
   *
   * @return <code>true</code> if and only if conversion is allowed,
   *         <code>false</code> otherwise.
   */
  public static boolean isBoxingConversion(final BinTypeRef from,
      BinTypeRef to) {
    // JLS boxing:
    // * boolean -> Boolean
    // * byte -> Byte
    // * char -> Character
    // * short -> Short
    // * int -> Integer
    // * long -> Long
    // * float -> Float
    // * double -> Double

    if (from == null || to == null) {
      return false; // Cannot convert null.
    }

    if(!(from.isPrimitiveType() && to.isReferenceType())) {
      return false; // Boxing conversion allowed only from primitive type to
                    // reference type
    }

    final Object toCheck = boxing.get(from.getQualifiedName());
    if (toCheck == null) {
      return false;
    }

    // JAVA5: another hack
    if (to.getBinType().isTypeParameter() || to.getBinType().isWildcard()) {
      BinTypeRef[] superTypes = to.getSupertypes();
      boolean ok = true;
      for (int i = 0, max = superTypes.length; i < max; i++) {
        BinTypeRef superT = superTypes[i];
        if (!toCheck.equals(superT.getQualifiedName())) {
          ok = false;
          break;
        }
      }
      return ok;
    } else {
      return toCheck.equals(to.getQualifiedName());
    }
  }

  /**
   * Checks whether unboxing conversion from specified type is possible to
   * specified type. See JLS3(Java 5) 5.1.8 Unboxing Conversions. Unboxing
   * conversion converts values of reference type to corresponding values of
   * primitive type.
   *
   * @param from
   *          type to convert. Reference type is allowed
   * @param to
   *          desired type. Primitive type is allowed and not null
   *
   * @return <code>true</code> if and only if conversion is allowed,
   *         <code>false</code> otherwise.
   */
  public static boolean isUnboxingConversion(final BinTypeRef from,
      final BinTypeRef to) {
    // JLS unboxing:
    // * Boolean -> boolean
    // * Byte -> byte
    // * Character -> char
    // * Short -> short
    // * Integer -> int
    // * Long -> long
    // * Float -> float
    // * Double -> double

    if (to == null) {
      return false; // Cannot convert to null.
    }

    // FIXME: check - sometimes from is null for some reason
    if (from == null) {
      return false; // Cannot convert from null.
    }

    if(!(from.isReferenceType() && to.isPrimitiveType())) {
      return false; // Boxing conversion allowed only from reference type to
                    //  primitive type
    }

    final Object toCheck = unboxing.get(from.getQualifiedName());
    
    boolean result = false;
    if(toCheck != null) {
      result = (toCheck.equals(to.getQualifiedName()));
      if(!result) {
        Object o = widening.get(toCheck);
        result = (o != null && o.equals(to.getQualifiedName()));
      }
    }
    return result;
  }


  /**
   * Checks whether specified value is contained in the list.
   *
   * @param value
   *          value.
   * @param list
   *          list.
   *
   * @return <code>true</code> if the value is contained in the list,
   *         <code>false</code> otherwise.
   */
  private static boolean inList(final Object value, final Object[] list) {
    for (int i = 0; i < list.length; i++) {
      final Object element = list[i];
      if (value == null) {
        if (element == null) {
          return true; // Found
        }
      } else {
        if (value.equals(element)) {
          return true; // Found
        }
      }
    }

    return false; // Not in list
  }

  public static String getBoxingObjectByPrimitive(String primitive) {
    ConversionsContainer container = (ConversionsContainer) boxing.get(primitive);
    if (container != null) {
      return container.getMainType();
    }
    return null;
  }

  public static String getUnboxingPrimitiveByType(String typeRef) {
    return (String) unboxing.get(typeRef);
  }

  public static BinTypeRef getUnboxingPrimitiveByType(BinTypeRef ref) {
    String type = getUnboxingPrimitiveByType(ref.getQualifiedName());

    if(type != null) {
      return ref.getProject().getTypeRefForName(type);
    } else {
      return null;
    }
  }

}
