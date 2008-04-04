/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.classmodel;

import net.sf.refactorit.classmodel.expressions.BinConstructorInvocationExpression;
import net.sf.refactorit.classmodel.references.BinItemReference;
import net.sf.refactorit.classmodel.references.BinMethodOrConstructorReference;
import net.sf.refactorit.classmodel.statements.BinExpressionStatement;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.ASTUtil;


// FIXME: according to JLS constructor is a member, but not a method!!!
// Fix with care, too much code relies on a fact it is instanceof BinMethod

/**
 * Contains information about class constructor
 */
public final class BinConstructor extends BinMethod {

  public static final BinConstructor[] NO_CONSTRUCTORS = new BinConstructor[0];

  public BinConstructor(
      BinParameter[] c_params, int c_modifiers, Throws[] c_exceptions) {
    // FIXME: fix return type
    this(null, c_params, c_modifiers, c_exceptions, false);
  }

  public BinConstructor(BinTypeRef returnType, BinParameter[] c_params,
      int c_modifiers, Throws[] c_exceptions, boolean synthetic) {
    super(null, c_params, returnType, c_modifiers, c_exceptions);
    setSynthetic(synthetic);
  }

  public BinMethod cloneSkeleton() {
    return new BinConstructor(this.getReturnType(), this.getParameters(),
        this.getModifiers(),
        this.getThrows(), true);
  }

  public static BinConstructor createByPrototype(BinTypeRef returnType,
      BinParameter[] c_params, int c_modifiers, Throws[] c_exceptions,
      boolean synthetic, BinTypeRef forType) {

    BinConstructor cnstr = forType.getProject().getProjectLoader()
        .getPrototypeManager().findConstructor(forType, c_params);
    if (cnstr == null) {
      cnstr = new BinConstructor(returnType, c_params, c_modifiers,
          c_exceptions, synthetic);
      cnstr.setOwner(forType);
    } else {
      cnstr.reInit(returnType, c_modifiers, c_exceptions, synthetic, c_params,
          forType);
    }

    return cnstr;
  }

  protected void reInit(BinTypeRef returnType, int modifiers, Throws[] throwses,
      boolean synthetic, BinParameter[] params, BinTypeRef owner) {
    setSynthetic(synthetic);
    super.reInit(returnType, modifiers, throwses, params, owner);
  }

  public String getName() {
    String name = super.getName();
    if (name == null) {
      BinTypeRef ownerType = getOwner();
      if (ownerType != null) {
        name = ownerType.getName();
        setName(name);
      }
    }

    return name;
  }

  public void setName(final String name) {
    super.setName(name);
  }

  public BinTypeRef getReturnType() {
    return getOwner(); // JAVA5: must check if super.getReturnType() is not null and return it?
  }

  public ASTImpl getNameAstOrNull() {
    // the if here is a guard for synthetically generated constructors
    if (this.nameAst == -1) {
      this.nameAst = ASTUtil.indexFor(
          ((ASTImpl) getOffsetNode().getFirstChild().getNextSibling()));
    }

    return getCompilationUnit().getSource().getASTByIndex(this.nameAst);
  }

  public void accept(net.sf.refactorit.query.BinItemVisitor visitor) {
    if (visitor.isSkipSynthetic() && this.isSynthetic()) {
      return;
    }
    visitor.visit(this);
  }

  public final void accept(BinTypeRefVisitor visitor) {
  }

  public String getMemberType() {
    return memberType;
  }

  public static String getStaticMemberType() {
    return memberType;
  }

  public boolean hasExplicitConstructorInvocation() {
    try {
      BinExpressionStatement firstStatement
          = (BinExpressionStatement) getBody().getStatements()[0];

      return firstStatement.getExpression() instanceof
          BinConstructorInvocationExpression;
    } catch (Exception e) {
      // If no first statement (or if it is not BinExpressionStatement, etc)
      return false;
    }
  }

  public boolean hasExplicitConstructorInvocationWithThisKeword() {
    try {
      BinExpressionStatement firstStatement
          = (BinExpressionStatement) getBody().getStatements()[0];

      return ((BinConstructorInvocationExpression) firstStatement.getExpression()).
          getConstructor().getOwner().equals(getOwner());
    } catch (Exception e) {
      // If no first statement (or if it is not BinExpressionStatement, etc)
      return false;
    }
  }

  public BinItemReference createReference() {
    return new BinMethodOrConstructorReference(this);
  }

  private static final String memberType = "constructor";
}
