/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel;

import net.sf.refactorit.classmodel.statements.BinFieldDeclaration;

/**
 * Defines annotation type
 */
public class BinAnnotation extends BinClass {

  /**
   * @param aPackage
   * @param name
   * @param declaredMethods
   * @param declaredFields
   * @param fieldDeclarations
   * @param inners
   * @param declaringType
   * @param modifiers
   * @param project
   */
  public BinAnnotation(BinPackage aPackage, String name,
      BinMethod[] declaredMethods, BinField[] declaredFields,
      BinFieldDeclaration[] fieldDeclarations, 
      BinConstructor[] declaredConstructors, 
      BinInitializer[] initializers,
      BinTypeRef[] inners,
      BinTypeRef declaringType, int modifiers, Project project) {
    super(aPackage, name, declaredMethods, declaredFields, fieldDeclarations,
        declaredConstructors, initializers, inners, declaringType, modifiers, project);
  }

  public boolean isClass() {
    return false;
  }

  public boolean isInterface() {
    return false;
  }

  public boolean isEnum() {
    return false;
  }
  
  public boolean isAnnotation() {
    return true;
  }

  public String getMemberType() {
    return memberType;
  }
  
  private static final String memberType = "interface";
}
