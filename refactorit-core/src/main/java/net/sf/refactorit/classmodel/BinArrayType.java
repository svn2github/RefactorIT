/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.classmodel;

import net.sf.refactorit.classfile.ClassUtil;


public final class BinArrayType extends BinClass {

  // FIXME: might be refactored to use BinArrayType in those places?
  public static final class ArrayType {
    public final String type;
    public final int dimensions;

    public ArrayType(final String type, final int dimensions) {
      this.type = type;
      this.dimensions = dimensions;
    }
  }

  // NOTE: when doing isAssignable then we should know that File[] is assignable to Object[]
  /**
   * Constructs an array type. To a great surprise for many - java arrays extend
   * java.lang.Object, implement Cloneable and Serializable and you can call all
   * object methods. Now tell me of course you knew it :)
   *
   */
  public BinArrayType(String qualifiedName, BinTypeRef arrayType,
      int dimensionCount, Project project) {
    super(project.getDefaultPackage(), qualifiedName,
        new BinMethod[] {new BinMethod("clone", BinParameter.NO_PARAMS,
        project.getObjectRef(), BinModifier.PUBLIC,
        BinMethod.Throws.NO_THROWS, true)},
        new BinField[] {new BinField("length", BinPrimitiveType.INT_REF,
        BinModifier.PUBLIC | BinModifier.FINAL, true)},
        null,
        BinConstructor.NO_CONSTRUCTORS,
        BinInitializer.NO_INITIALIZERS,
        BinTypeRef.NO_TYPEREFS, null,
        ARRAY_MODIFIERS, project);

    if (arrayType == null) {
      throw new IllegalArgumentException("arrayType = null");
    }
    this.arrayType = arrayType;
    this.dimensionCount = dimensionCount;
    this.qualifiedName = qualifiedName;
  }

//  public BinPackage getPackage() {
//    return null; // according to VMSpec
//  }

  /**
   * @see BinCIType#getQualifiedName()
   */
  public String getQualifiedName() {

    // DEBUG
    if (this.qualifiedName == null) {
      return super.getName();
    }
    // DEBUG

    return this.qualifiedName;
  }

  public String getName() {
    // FIXME: It is actually called, when there is error message like:
    // could not find method aMethod(Object []);
    // Right now it gives ugly result of internal form - should we have
    // a method getPresentableName?
    //if (Assert.enabled) {
    //  Assert.must(false, "getName() shouldn't be called for arrays");
    //}

    return getQualifiedName();
  }

  public BinTypeRef getArrayType() {
    return arrayType;
  }

  public void setArrayType(BinTypeRef arrayType) {
    this.arrayType = arrayType;
  }

  public int getDimensions() {
    return dimensionCount;
  }

  public String getDimensionString() {
    int dimensionNumber = getDimensions();
    String output = "";
    for (int i = 0; i < dimensionNumber; i++) {
      output = output + "[]";
    }
    return output;
  }

  public String getVariableArityString() {
    int dimensionNumber = getDimensions();
    String output = "";
    if(dimensionNumber < 0) return output;
    for (int i = 0; i < dimensionNumber - 1; i++) {
      output = output + "[]";
    }
    output = output + "...";
    return output;
  }

  public static char getPrimitiveArrayCharForName(String name) {
    char retVal = ClassUtil.OBJECT_IDENT;
    for (int q = 0; q < ClassUtil.primitiveNames.length; q++) {
      if (name.equals(ClassUtil.primitiveNames[q])) {
        retVal = ClassUtil.typeCodes[q];
      }
    }
    return retVal;
  }

  public static final String toInternalForm(final String humanForm) {
    final StringBuffer result = new StringBuffer();

    int pos = humanForm.indexOf('[');
    final String typeName = humanForm.substring(0, pos);

    while (humanForm.indexOf('[', pos) > 0) {
      pos = pos + 2;
      result.append('[');
    }

    final char prim = getPrimitiveArrayCharForName(typeName);
    result.append(prim);
    if (prim == ClassUtil.OBJECT_IDENT) {
      result.append(typeName).append(';');
    }

    return result.toString();
  }

  public boolean isArray() {
    return true;
  }

  public void accept(net.sf.refactorit.query.BinItemVisitor visitor) {
    visitor.visit(this);
  }

  /**
   * Extracts type name, e.g. <code>String</code> from type name that can be
   * an array, e.g. <code>String[][]</code>.
   *
   * @param type type name
   * @return type name without array brackets.
   */
  public static ArrayType extractArrayTypeFromString(final String type) {
    if (type.length() == 0) {
      throw new IllegalArgumentException("Type name empty!");
    }
    int dimensions = 0;
    String arraylessReturnType = type.trim();
    while (arraylessReturnType.trim().endsWith("]")) {
      arraylessReturnType = arraylessReturnType.substring(0,
          arraylessReturnType.length() - 1).trim();
      if (arraylessReturnType.endsWith("[")) {
        arraylessReturnType = arraylessReturnType.substring(0,
            arraylessReturnType.length() - 1).trim();
        dimensions++;
      } else {
        throw new IllegalArgumentException("Array bracket mismatch");
      }
    }
    return new ArrayType(arraylessReturnType, dimensions);
  }

  private BinTypeRef arrayType;
  private int dimensionCount;

  private String qualifiedName;

  // This is what JDK reflecton says
  private static final int ARRAY_MODIFIERS = BinModifier.ABSTRACT |
      BinModifier.FINAL |
      BinModifier.PUBLIC;
}
