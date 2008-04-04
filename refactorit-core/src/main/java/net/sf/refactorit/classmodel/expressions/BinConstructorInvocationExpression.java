/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel.expressions;


import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinExpressionList;
import net.sf.refactorit.classmodel.BinInterface;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinTypeRefManager;
import net.sf.refactorit.classmodel.BinTypeRefVisitor;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.ASTUtil;
import net.sf.refactorit.source.format.BinFormatter;

import org.apache.log4j.Logger;


/**
 * super(..) and this(..) invocations.
 */
public final class BinConstructorInvocationExpression extends BinExpression
    implements MethodOrConstructorInvocationExpression, BinTypeRefManager {

  /**
   * What to do with synthetic constructor invocation expressions.
   * Currently their rootAst node is the nameAST of declaring class
   */
  public BinConstructorInvocationExpression(BinTypeRef context,
      BinTypeRef typeRef,
      BinExpressionList expressionList,
      boolean isSuper, ASTImpl rootAst) {
    super(rootAst);
    this.expressionList = expressionList;
    this.typeRef = typeRef;
    this.isSuper = isSuper;
    this.context = context;

    // %%% remove this debug code later ???
    {
      if (this.typeRef != null
          && this.typeRef.getBinType() instanceof BinInterface) {
        System.err.println(
            "REFACTORIT: CODE 000 -- PLEASE REPORT WITH THE FOLLOWING STACK TRACE: "
            + this.typeRef + " - " + this.typeRef.getBinType());
        new Throwable().printStackTrace();
      }
    }
  }

  public final BinTypeRef getReturnType() {
    return typeRef;
  }

  public final BinExpressionList getExpressionList() {
    return expressionList;
  }

  public final boolean isSuper() {
    return isSuper;
  }

  public final BinConstructor getConstructor() {
    if (this.constructor == null && !this.typeRef.isPrimitiveType()) {
      if (Assert.enabled) {
        Assert.must(this.typeRef.getBinCIType() instanceof
            BinClass,
            "Querying constructor of non-class type: " + this.typeRef);
      }

      this.constructor = ((BinClass)this.typeRef.getBinType())
          .getAccessibleConstructor(this.context.getBinCIType(),
          expressionList.getExpressionTypes());


      if (Assert.enabled && this.constructor == null) {
        log.error("Couldn't find constructor, type: " + this.typeRef
            + ", context: " + this.context);

        /*        BinConstructor[] cntors = ((BinClass) this.typeRef.getBinType())
                  .getConstructors();
                for (int i = 0; i < cntors.length; i++) {
                  System.err.println("Available cntor: " + cntors[i]);
                }*/
      }
    }

    return this.constructor;
  }

  public final void accept(net.sf.refactorit.query.BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public final void defaultTraverse(net.sf.refactorit.query.BinItemVisitor visitor) {
    expressionList.accept(visitor);

    if (primaryExpression != null) {
      primaryExpression.accept(visitor);
    }
  }

  /**
   * See JLS 8.8.5.1 Explicit Constructor Invocations,
   * Qualified superclass constructor invocations
   *
   * @param primaryExpression "doc" in doc.super();
   */
  public final void setPrimaryExpression(BinExpression primaryExpression) {
    this.primaryExpression = primaryExpression;
  }

  public final String getDetails() {
    return this.context.getName() + " - "
        + BinFormatter.format(getConstructor());
  }

  public final void clean() {
    if (primaryExpression != null) {
      primaryExpression.clean();
      primaryExpression = null;
    }
    expressionList.clean();
    expressionList = null;
    typeRef = null;
    context = null;
    super.clean();
  }

  public final boolean isSame(BinItem other) {
    if (!(other instanceof BinConstructorInvocationExpression)) {
      return false;
    }
    final BinConstructorInvocationExpression expr
        = (BinConstructorInvocationExpression) other;
    return this.isSuper == expr.isSuper
        && isBothNullOrSame(this.typeRef, expr.typeRef)
        && isBothNullOrSame(this.context, expr.context) // isn't it too strict to require same context?
        && isBothNullOrSame(this.primaryExpression, expr.primaryExpression)
        && this.expressionList.isSame(expr.expressionList);
  }

  public final ASTImpl getNameAst() {
    if (this.nameAst == -1) {
      this.nameAst = ASTUtil.indexFor(this.getRootAst());
    }
    return getCompilationUnit().getSource().getASTByIndex(this.nameAst);
  }

  public final void setNameAst(final ASTImpl nameAst) {
    this.nameAst = ASTUtil.indexFor(nameAst);
  }

  private int nameAst = -1;

  public ASTImpl getClickableNode() {
    return getNameAst();
  }

  /** "doc" in doc.super(1,1,1); see bug #210 */
  private BinExpression primaryExpression = null;

  private BinExpressionList expressionList;
  private BinTypeRef typeRef;
  private final boolean isSuper;
  private BinTypeRef context;

  private BinConstructor constructor = null;

  private static final Logger log = AppRegistry.getLogger(BinConstructorInvocationExpression.class);

  public final BinMethod getMethod() {
    return getConstructor();
  }

  /**
   * @return true if anonymous class constructor invocation
   */
  public final boolean isSynthetic() {
    return getExpressionList().getStartLine() < 0;
  }

  public void setTypeArguments(BinTypeRef[] typeArguments) {
    this.typeArguments = typeArguments;
  }

  public BinTypeRef[] getTypeArguments() {
    return this.typeArguments;
  }

  public void accept(BinTypeRefVisitor visitor) {
    if (typeArguments != null) {
      for (int i = 0, max = typeArguments.length; i < max; i++) {
        typeArguments[i].accept(visitor);
      }
    }
  }

  private BinTypeRef[] typeArguments;
}
