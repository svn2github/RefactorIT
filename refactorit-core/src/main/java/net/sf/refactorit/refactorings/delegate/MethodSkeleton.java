/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.delegate;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinModifierBuffer;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinMethod.Throws;
import net.sf.refactorit.refactorings.NameUtil;


/**
 * @author Tonis Vaga
 */
public class MethodSkeleton {
//  public Throws []throwSt;

  MethodBodySkeleton body;

//  private String methodName;
//
//  public BinTypeRef[] argumentTypes;
//  public String[] argumentNames;

  protected BinMethod method;

//  private BinTypeRef returnTypeRef;
//  private BinTypeRef owner;

//  private int modifiers;

  public BinTypeRef getOwner() {
    return method.getOwner();
  }

  public boolean equals(Object obj) {
    return method.sameSignature(((MethodSkeleton) obj).getMethod());
  }

  public int hashCode() {
    return method.getName().hashCode();
  }

  public boolean isConstructor() {
    return method instanceof BinConstructor;
  }

  public BinMethod getMethod() {

    return method;
  }

  public String getMethodName() {
    return method.getName();
  }

//  public void setMethodName(final String methodName) {
//    this.methodName = methodName;
//  }

//  public BinTypeRef getBaseClass() {
//    return this.baseClass;
//  }
//
//  public void setBaseClass(final BinTypeRef baseClass) {
//    this.baseClass = baseClass;
//  }

//  public String getReturnType() {
//    return this.returnType;
//  }
//
//  public void setReturnType(final String returnType) {
//    this.returnType = returnType;
//  }

//  public int getVisibility() {
//    return this.visibility;
//  }
//
//  public void setVisibility(final int visibility) {
//    this.visibility = visibility;
//  }

//  public BinTypeRef getInvokedIn() {
//    return this.invokedIn;
//  }

//  public boolean isStaticMethod() {
//    return BinModifier.hasFlag(modifiers,BinModifier.STATIC);
//  }

//  public void setStaticMethod(final boolean staticMethod) {
//    this.staticMethod = staticMethod;
//  }

  public int getModifiers() {
    return method.getModifiers();
  }

  public BinParameter[] getParameters() {
//    BinParameter []result=new BinParameter[argumentTypes.length];
//
//    for (int j = 0; j < argumentTypes.length; j++) {
//        result[j] = new BinParameter(argumentNames[j], argumentTypes[j], 0);
//    }
//    return result;
    return method.getParameters();

  }

  public BinTypeRef getReturnTypeRef() {
    return method.getReturnType();
  }

  public Throws[] getThrows() {
    return method.getThrows();
  }

  public MethodBodySkeleton getBody() {
    return body;
  }

  public MethodSkeleton(BinCIType owner, BinMethod method,
      MethodBodySkeleton methodBodyContext) {

    this.method = method.cloneSkeleton();

    //new BinMethod(method.getName(),method.getParameters(),method.getReturnType(),method.getModifiers(),method.getThrows());

    this.method.setOwner(owner.getTypeRef());
    this.method.setParent(owner);

    setBody(methodBodyContext);

    validateContext();

  }

  /**
   * @param method
   */
  protected void validateContext() {
    if (body != null && !body.isEmpty()) {
      int modifiers = method.getModifiers();
      BinModifierBuffer buff = new BinModifierBuffer(modifiers);
      buff.clearFlags(BinModifier.ABSTRACT);
      buff.clearFlags(BinModifier.NATIVE);
      method.setModifiers(buff.getModifiers());
    }

    // make sure param names are correct and unique

    BinParameter[] parameters = method.getParameters();
    for (int i = 0; i < parameters.length; i++) {
      String name2 = parameters[i].getName();
      if (name2 == null || name2 == "") {

        String result = NameUtil.extractConvenientVariableNameForType(
            parameters[
            i].getTypeRef());

        String str = result;

        if (parameters.length > 1) {
          str = str + (i + 1);
        }

        parameters[i].setName(str);

//          parameters[i].setName("param"+(i+1));
      }

    }
  }

  public String toString() {
    return method.toString();
  }

  public void setBody(final MethodBodySkeleton body) {
    this.body = body;
    validateContext();
  }

  public void setIsStatic(boolean b) {
    int modifiers = method.getModifiers();

    if (b) {
      modifiers = BinModifier.setFlags(modifiers, BinModifier.STATIC);
    } else {
      modifiers = BinModifier.clearFlags(modifiers, BinModifier.STATIC);
    }
    method.setModifiers(modifiers);
  }

  public void setOwner(BinTypeRef owner) {
    method.setOwner(owner);
  }

  /**
   * Create skeleton with empty body
   * @param binClass
   * @param binMethod
   */
  public MethodSkeleton(BinClass binClass, BinMethod binMethod) {
    this(binClass, binMethod, null);
  }
}
