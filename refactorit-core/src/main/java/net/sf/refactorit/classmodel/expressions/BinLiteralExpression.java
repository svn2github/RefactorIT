/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.classmodel.expressions;

import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.ASTUtil;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.source.format.BinItemFormatter;
import net.sf.refactorit.source.format.BinLiteralExpressionFormatter;


public final class BinLiteralExpression extends BinExpression {

  public BinLiteralExpression(String literal, BinTypeRef literalTypeRef,
      ASTImpl rootAst) {
    super(rootAst);
    this.literal = literal;
    this.literalTypeRef = literalTypeRef;
  }

  /** NOTE: typeRef differs from expression.getTypeRef() for
   *  e.g. Test.super.method(); */
  public BinLiteralExpression(String literal, BinExpression expression,
      BinTypeRef typeRef, ASTImpl rootAst) {
    this(literal, typeRef, rootAst);
    this.expression = expression;
  }

  public BinTypeRef getReturnType() {
    return literalTypeRef;
  }

  public String getLiteral() {
    return literal;
  }

  public BinExpression getExpression() {
    return this.expression;
  }

  public ASTImpl getClickableNode() {
    if (this.expression != null) {
      return this.expression.getClickableNode();
    }

    return getRootAst();
  }

  public void accept(BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public void defaultTraverse(BinItemVisitor visitor) {
    if (expression != null) {
      expression.accept(visitor);
    }
  }

  public void clean() {
    if (expression != null) {
      expression.clean();
      expression = null;
    }
    literalTypeRef = null;
    super.clean();
  }

  public boolean isNull() {
    return NULL.equals(literal);
  }

  public boolean isSuper() {
    return SUPER.equals(literal);
  }

  public boolean isThis() {
    return THIS.equals(literal);
  }

  public boolean isTrue() {
    return TRUE.equals(literal);
  }

  public boolean isFalse() {
    return FALSE.equals(literal);
  }

  public boolean isClass() {
    return CLASS.equals(literal);
  }

  public boolean isSame(final BinItem other) {
    if (!(other instanceof BinLiteralExpression)) {
      return false;
    }
    final BinLiteralExpression expr = (BinLiteralExpression) other;
    return this.literal.equals(expr.literal)
        && isBothNullOrSame(this.literalTypeRef, expr.literalTypeRef)
        && isBothNullOrSame(this.expression, expr.expression);
  }

  public ASTImpl getNameAst() {
    if (this.nameAst == -1) {
      this.nameAst = ASTUtil.indexFor(this.getRootAst());
    }
    return getCompilationUnit().getSource().getASTByIndex(this.nameAst);
  }

  public void setNameAst(final ASTImpl nameAst) {
    this.nameAst = ASTUtil.indexFor(nameAst);
  }

  public BinItemFormatter getFormatter() {
    return new BinLiteralExpressionFormatter(this);
  }

  private int nameAst = -1;

  private final String literal;
  private BinTypeRef literalTypeRef = null;
  private BinExpression expression = null;

  // constants
  public static final String NULL = "null";
  public static final String SUPER = "super";
  public static final String THIS = "this";
  public static final String TRUE = "true";
  public static final String FALSE = "false";
  public static final String CLASS = "class";
}
