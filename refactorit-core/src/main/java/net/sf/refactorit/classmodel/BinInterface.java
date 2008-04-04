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
 * Defines interface type
 */
public class BinInterface extends BinCIType {

  public BinInterface(BinPackage aPackage, String name,
      BinMethod[] b_methods, BinField[] b_fields,
      BinFieldDeclaration[] b_fieldDeclarations,
      BinTypeRef[] b_inners,
      BinTypeRef b_owner, int b_modifiers, Project project) {
    super(aPackage, name, b_methods, b_fields, b_fieldDeclarations, b_inners,
        b_owner,
        b_modifiers, project);
  }

  public final void accept(net.sf.refactorit.query.BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public final boolean isClass() {
    return false;
  }

  public final boolean isInterface() {
    return true;
  }

  public final boolean isEnum() {
    return false;
  }

  public boolean isAnnotation() {
    return false;
  }

  
  public String getMemberType() {
    return memberType;
  }

  private static final String memberType = "interface";

}
