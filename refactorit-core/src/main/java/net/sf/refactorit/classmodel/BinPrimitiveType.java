/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel;

import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.query.BinItemVisitor;


/**
 * Defines class for primitive types as int, double
 */
public final class BinPrimitiveType extends BinType {
  //FIXME: what should really be primitive modifiers?
  public static final int PRIMITIVE_MODIFIERS = 0;

  // Second paramter is priority over other primitive types in widening.
  // (-1 means that this type cannot be wided)

  public static final BinPrimitiveType VOID = new BinPrimitiveType("void", -1);
  public static final BinPrimitiveType BOOLEAN = new BinPrimitiveType("boolean",
      -1);
  public static final BinPrimitiveType BYTE = new BinPrimitiveType("byte", 0);
  public static final BinPrimitiveType CHAR = new BinPrimitiveType("char", 0);
  public static final BinPrimitiveType SHORT = new BinPrimitiveType("short", 0);
  public static final BinPrimitiveType INT = new BinPrimitiveType("int", 0);
  public static final BinPrimitiveType LONG = new BinPrimitiveType("long", 1);
  public static final BinPrimitiveType FLOAT = new BinPrimitiveType("float", 2);
  public static final BinPrimitiveType DOUBLE = new BinPrimitiveType("double",
      3);

  private static final String memberType = "primitive";

  private final int priority;

  public static final BinTypeRef VOID_REF = new BinPrimitiveTypeRef(VOID);
  public static final BinTypeRef BOOLEAN_REF = new BinPrimitiveTypeRef(BOOLEAN);
  public static final BinTypeRef BYTE_REF = new BinPrimitiveTypeRef(BYTE);
  public static final BinTypeRef CHAR_REF = new BinPrimitiveTypeRef(CHAR);
  public static final BinTypeRef SHORT_REF = new BinPrimitiveTypeRef(SHORT);
  public static final BinTypeRef INT_REF = new BinPrimitiveTypeRef(INT);
  public static final BinTypeRef LONG_REF = new BinPrimitiveTypeRef(LONG);
  public static final BinTypeRef FLOAT_REF = new BinPrimitiveTypeRef(FLOAT);
  public static final BinTypeRef DOUBLE_REF = new BinPrimitiveTypeRef(DOUBLE);

  private BinPrimitiveType(String typeName, int priority) {
    super(typeName, PRIMITIVE_MODIFIERS, null);
    this.priority = priority;
  }

  public String getQualifiedName() {
    return getName();
  }

  public int getPriority() {
    return priority;
  }

  public final boolean isPrimitiveType() {
    return true;
  }

  public final boolean isClass() {
    return false;
  }

  public final boolean isInterface() {
    return false;
  }

  public final boolean isEnum() {
    return false;
  }
  
  public boolean isAnnotation() {
    return false;
  }

  public final boolean isAnonymous() {
    return false;
  }

  public final boolean isLocal() {
    return false;
  }

  public boolean isFloatingPointType(){
    return getName().equals(FLOAT.getQualifiedName())
        || getName().equals(DOUBLE.getQualifiedName());
  }

  public boolean isIntegerType(){
    return getName().equals(INT.getQualifiedName())
        || getName().equals(LONG.getQualifiedName())
        || getName().equals(BYTE.getQualifiedName())
        || getName().equals(SHORT.getQualifiedName())
        || getName().equals(CHAR.getQualifiedName());
  }

  public void accept(BinItemVisitor visitor) {
    visitor.visit(this);
  }

  // FIXME: polymorphism is approptiate here
  public String getDefaultValue() {
    if (this == BOOLEAN) {
      return "false";
    }

    if (this == CHAR) {
      return "''";
    }

    if (this == VOID) {
      return "";
    }

    if (this == FLOAT) {
      return "0.f";
    }

    if (this == DOUBLE) {
      return "0.0";
    }

    return "0";
  }

  public final String getMemberType() {
    return memberType;
  }

  // FIXME: polymorphism is approptiate here
  public boolean canConvertFromString(String str) {
    if (this == BOOLEAN) {
      return str.equals("true") || str.equals("false");
    }

    if (this == CHAR && str.length() == 0) {
      return false;
    }

    try {
      if (this == INT) {
        Integer.parseInt(str);
      } else if (this == LONG) {
        Long.parseLong(str);
      } else if (this == SHORT) {
        Short.parseShort(str);
      } else if (this == DOUBLE) {
        Double.parseDouble(str);
      } else if (this == FLOAT) {
        Float.parseFloat(str);
      }
    } catch (NumberFormatException ex) {
      return false;
    }

    return true;
  }

  public CompilationUnit getCompilationUnit() {
    return null;
  }

  public ASTImpl getNameAstOrNull() {
    return null;
  }
}
