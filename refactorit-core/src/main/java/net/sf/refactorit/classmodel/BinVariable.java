/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel;

import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.statements.BinVariableDeclaration;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.ASTUtil;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.source.edit.CompoundASTImpl;


/**
 * Superclass for {@link BinField}, {@link BinLocalVariable}.
 */
public abstract class BinVariable extends BinMember
    implements SourceConstruct, BinTypeRefManager {

  public BinVariable(final String name, final BinTypeRef typeRef,
      final int modifiers) {
    super(name, modifiers, null);
    if (Assert.enabled && typeRef == null) {
      Assert.must(false, "Trying to create parameter without type: " + name);
    }
    this.typeRef = typeRef;
  }

  public final BinTypeRef getTypeRef() {
    return this.typeRef;
  }

  protected final void reInit(BinTypeRef typeRef, int modifiers,
      boolean isWithoutExpression, BinTypeRef owner) {

    if (isWithoutExpression) {
      setExpression(null);
    } else {
      expression = null;
    }

    if (Assert.enabled) {
      Assert.must(owner != null, "Owner is null :(");
    }

    setOwner(owner);
    this.typeRef = typeRef;
    setModifiers(modifiers);
  }

  public abstract boolean isLocalVariable();

  /**
   * Gets string represenation of this variable. Follows the contract of
   * {@link java.lang.reflect.Field#toString}.
   *
   * @return string represenation.
   */
  public String toString() {
    final StringBuffer result
        = new StringBuffer(getTypeRef().getQualifiedName());
    result.append(' ');
    result.append(getQualifiedName());

    return result.toString();
  }

  public abstract void accept(net.sf.refactorit.query.BinItemVisitor visitor);

  public final void accept(BinTypeRefVisitor visitor) {
    if (this.typeRef != null) {
      this.typeRef.accept(visitor);
    }
  }

  /** Gives null if the name is implied (which means that the name is
   * not actually present in the source, it's perhaps inherited
   * from some supertype).
   */
  public final ASTImpl getNameAstOrNull() {
    return getCompilationUnit().getSource().getASTByIndex(nameAst);
  }

  public final ASTImpl getTypeAst() {

    if (Assert.enabled) {
      Assert.must(getOffsetNode() != null,
          "offsetNode === null for variable " + getQualifiedName() +
          " parent=" + this.getParent() + ",parentMember=" +
          this.getParentMember());
    }
    ASTImpl typeNode = ASTUtil.getFirstChildOfType(
            getOffsetNode(), JavaTokenTypes.TYPE);
    if (typeNode != null) {
      return (ASTImpl) typeNode.getFirstChild();
    } else {
      return null;
    }
  }

  public final void setNameAst(final ASTImpl nameAst) {
    this.nameAst = ASTUtil.indexFor(nameAst);
  }

  public final ASTImpl getRootAst() {
    return getOffsetNode();
  }

  // FIXME: strange code - for single name it would be enough getNameAst().getStart..()
  public final SourceCoordinate getNameStart() {
    return SourceCoordinate.getForStart(new CompoundASTImpl(getNameAstOrNull()));
  }

  // FIXME: shouldn't here be used getExpression().getRootAst() ?
  // or actually in those places calling this
  public final SourceCoordinate getExpressionStart() {
    return SourceCoordinate.getForStart(new CompoundASTImpl(getExprNode()));
  }

  // FIXME: shouldn't here be used getExpression().getRootAst() ?
  // or actually in those places calling this
  public final SourceCoordinate getExpressionEnd() {
    return SourceCoordinate.getForEnd(new CompoundASTImpl(getExprNode()));
  }

  // FIXME: shouldn't here be used getExpression().getRootAst() ?
  // or actually in those places calling this
  public final ASTImpl getExprNode() {
    return ASTUtil.getAssignedExpression(getOffsetNode());
  }

  // FIXME: all above looks like belonging to the wrong class in classmodel...
  public final String getExprNodeText() {
    if (getExprNode() == null) {
      return null;
    }
    return getCompilationUnit().getSource().getText(new CompoundASTImpl(getExprNode()));
  }

  public final int getTypeNodeEndColumn() {
    return ((BinVariableDeclaration) getWhereDeclared()).getVariables()[0].
        getNameAstOrNull().getStartColumn();
  }

  public final int getTypeNodeEndLine() {
    return ((BinVariableDeclaration) getWhereDeclared()).getVariables()[0].
        getNameAstOrNull().getStartLine();
  }

  public final String getTypeAndModifiersNodeText() {
    return getCompilationUnit().getSource().getText(
        getStartLine(), getStartColumn(), getTypeNodeEndLine(),
        getTypeNodeEndColumn()).trim();
  }

  public final LocationAware getWhereDeclared() {
    return (LocationAware) getParent();
  }

  public final boolean isImplied() {
    return getNameAstOrNull() == null;
  }

  /**
   * Can be also used to set expression to explicitly null
   * This is done for fields from .class file
   * When setExpression is called then ensureExpression will not be called
   */
  public final void setExpression(BinExpression expression) {
    this.expression = expression;
    this.expressionEnsured = true;
  }

  public boolean hasExpression() {
    return expression != null;
  }

  public final BinExpression getExpression() {
    ensureExpression();
    return expression;
  }

  public final void defaultTraverse(net.sf.refactorit.query.BinItemVisitor visitor) {
    BinExpressionList annotations = getAnnotations();
    if (annotations != null) {
      annotations.accept(visitor);
    }
    
    BinExpression expr = getExpression();
    if (expr != null) {
      expr.accept(visitor);
    }
  }

  protected final void cleanForPrototype() {
    this.expressionEnsured = false;
//    if (this.expression != null) {
//      this.expression.clean(); // for debug
//    }
    this.expression = null;
    this.nameAst = -1;
    this.typeRef = null;
    super.cleanForPrototype();
  }

  public final boolean isExpressionEnsured() {
    return this.expressionEnsured;
  }

  public final void setExpressionEnsured(final boolean expressionEnsured) {
    this.expressionEnsured = expressionEnsured;
  }

  protected void ensureExpression() {
  }
  
  /**
   * @param annotationsList
   */
  public void setAnnotations(BinExpressionList annotationsList) {
    this.annotationsList = annotationsList;    
  }
  
  public BinExpressionList getAnnotations() {
    return this.annotationsList;
  }

  private BinExpressionList annotationsList;

  private BinExpression expression;
  private boolean expressionEnsured = false;

  private BinTypeRef typeRef;

  private int nameAst = -1;
}
