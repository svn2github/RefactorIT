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
import net.sf.refactorit.parser.ASTImpl;


/**
 * Defines enum type
 */
public final class BinEnum extends BinClass {

  public BinEnum(BinPackage aPackage, String name,
      BinMethod[] b_methods, BinField[] b_fields,
      BinFieldDeclaration[] b_fieldDeclarations,
      BinConstructor[] b_constrs, BinInitializer[] b_inits,
      BinTypeRef[] b_inners,
      BinTypeRef b_owner, int b_modifiers, Project project) {
    super(aPackage, name, b_methods, b_fields, b_fieldDeclarations,
        b_constrs, b_inits, b_inners,
        b_owner,
        b_modifiers, project);
  }

  public void accept(net.sf.refactorit.query.BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public final boolean isClass() {
    return false;
  }

  public final boolean isInterface() {
    return false;
  }

  public final boolean isEnum() {
    return true;
  }

  public void generateValueMethods() {
    BinMethod valuesMethod = new BinMethod("values", BinParameter.NO_PARAMS,
        getProject().createArrayTypeForType(getTypeRef(), 1),
        BinModifier.PUBLIC | BinModifier.STATIC | BinModifier.FINAL,
        BinMethod.Throws.NO_THROWS);
    valuesMethod.setSynthetic(true);
    ASTImpl nameAST = getNameAstOrNull();
    valuesMethod.setOffsetNode(nameAST);
    valuesMethod.setNameAst(nameAST);
    addDeclaredMethod(valuesMethod);

    BinMethod valueOfMethod = new BinMethod("valueOf",
        new BinParameter[] {
          new BinParameter("s",
              getProject().findTypeRefForName("java.lang.String"), 0)},
        getTypeRef(), BinModifier.PUBLIC | BinModifier.STATIC,
        BinMethod.Throws.NO_THROWS);
    valueOfMethod.setSynthetic(true);
    valueOfMethod.setOffsetNode(nameAST);
    valueOfMethod.setNameAst(nameAST);
    addDeclaredMethod(valueOfMethod);
  }

  public String getMemberType() {
    return memberType;
  }

  public static String getStaticMemberType() {
    return memberType;
  }

  private static final String memberType = "enum";

}
