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
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinTypeRefManager;
import net.sf.refactorit.classmodel.BinTypeRefVisitor;
import net.sf.refactorit.classmodel.statements.BinCITypesDefStatement;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.loader.MethodBodyLoader;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.source.UserFriendlyError;
import net.sf.refactorit.source.format.BinItemFormatter;
import net.sf.refactorit.source.format.BinNewExpressionFormatter;



public final class BinNewExpression extends BinExpression
    implements BinTypeRefManager, MethodOrConstructorInvocationExpression {

  public BinNewExpression(BinTypeRef typeRef,
      BinExpressionList expressionList,
      BinExpression[] dimensionExpressions,
      BinArrayInitExpression arrayInitExpression,
      BinCITypesDefStatement typeDef,
      ASTImpl rootAst) {
    super(rootAst);
    if (Assert.enabled && typeRef == null) {
      Assert.must(false, "Can't create NewExpression without typeRef: " + rootAst);
    }
    this.typeRef = typeRef;
    this.expressionList = expressionList;
    this.dimensionExpressions = dimensionExpressions;
    this.arrayInitExpression = arrayInitExpression;
    this.typeDefStatement = typeDef;
  }

  public final BinTypeRef getReturnType() {
    return this.typeRef;
  }

  public final BinTypeRef getTypeRef() {
    return this.typeRef;
  }

  public final void setLeftExpression(BinExpression leftExpression) {
    this.leftExpression = leftExpression;
  }

  public final BinCITypesDefStatement getTypeDefStatement() {
    return typeDefStatement;
  }

  public final BinExpression getLeftExpression() {
    return leftExpression;
  }

  public final BinExpressionList getExpressionList() {
    return expressionList;
  }

  public final BinExpression[] getDimensionExpressions() {
    return dimensionExpressions;
  }

  public final BinExpression getArrayInitExpression() {
    return arrayInitExpression;
  }

  public final BinConstructor getConstructor() {
    if (this.constructor == null
        && !getTypeRef().isArray()
        && getTypeRef().isReferenceType()
        && (getTypeRef().getBinCIType().isClass() ||
        getTypeRef().getBinCIType().isEnum())) {

      if (this.expressionList == null) {
        if (!getTypeRef().isArray()) {
          AppRegistry.getLogger(getClass()).debug(
              "Missing expression list for non-array new expression: " + this);
        }
        this.expressionList = BinExpressionList.NO_EXPRESSIONLIST;
      }

      BinClass type = (BinClass) this.typeRef.getBinType();
      this.constructor = type.getAccessibleConstructor(
          type, this.expressionList.getExpressionTypes());

      if (this.constructor == null) {
        (getParentMember().getProject().getProjectLoader().getErrorCollector()).addNonCriticalUserFriendlyError(new UserFriendlyError("Wrong constructor call: "
                + typeRef.getQualifiedName() + "("
                + MethodBodyLoader.displayableListOfReturnTypes(expressionList) + ")",
                getParentMember().getCompilationUnit(), getRootAst()));
      }
    }

    return this.constructor;
  }

  public final void accept(net.sf.refactorit.query.BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public final void defaultTraverse(net.sf.refactorit.query.BinItemVisitor visitor) {
    if (leftExpression != null) {
      leftExpression.accept(visitor);
    }
    if (expressionList != null) {
      expressionList.accept(visitor);
    }
    if (arrayInitExpression != null) {
      arrayInitExpression.accept(visitor);
    }
    if (dimensionExpressions != null) {
      for (int i = 0; i < dimensionExpressions.length; ++i) {
        dimensionExpressions[i].accept(visitor);
      }
    }
    if (typeDefStatement != null) {
      typeDefStatement.accept(visitor);
    }
  }

  public final void clean() {
    if (leftExpression != null) {
      leftExpression.clean();
      leftExpression = null;
    }
    if (expressionList != null) {
      expressionList.clean();
      expressionList = null;
    }
    if (arrayInitExpression != null) {
      arrayInitExpression.clean();
      arrayInitExpression = null;
    }
    if (dimensionExpressions != null) {
      for (int i = 0; i < dimensionExpressions.length; ++i) {
        dimensionExpressions[i].clean();
      }
      dimensionExpressions = null;
    }
    if (typeDefStatement != null) {
      typeDefStatement.clean();
      typeDefStatement = null;
    }
    super.clean();
  }

  public void setTypeArguments(BinTypeRef[] typeArguments) {
    this.typeArguments = typeArguments;
  }

  public BinTypeRef[] getTypeArguments() {
    return this.typeArguments;
  }

  public final void accept(BinTypeRefVisitor visitor) {
    if (this.typeRef != null) {
      if (visitor.isIncludeNewExpressions()) {
        this.typeRef.accept(visitor);
      } else {
        this.typeRef.traverse(visitor); // skipping itself
      }
    }

    if (typeArguments != null) {
      for (int i = 0, max = typeArguments.length; i < max; i++) {
        typeArguments[i].accept(visitor);
      }
    }
  }

  public final ASTImpl getClickableNode() {
    return this.typeRef.getNode();
  }

  public final boolean isSame(BinItem other) {
    if (!(other instanceof BinNewExpression)) {
      return false;
    }
    final BinNewExpression expr = (BinNewExpression) other;

    if (isBothNullOrSame(this.getTypeRef(), expr.getTypeRef())
        && isBothNullOrSame(this.leftExpression, expr.leftExpression)
        && isBothNullOrSame(this.expressionList, expr.expressionList)
        && isBothNullOrSame(this.arrayInitExpression, expr.arrayInitExpression)
        && isBothNullOrSame(this.typeDefStatement, expr.typeDefStatement)) {
      if (this.dimensionExpressions == null && expr.dimensionExpressions == null) {
        return true;
      } else if (this.dimensionExpressions != null
          && expr.dimensionExpressions != null) {
        if (this.dimensionExpressions.length
            == expr.dimensionExpressions.length) {
          for (int i = 0; i < this.dimensionExpressions.length; i++) {
            if (!this.dimensionExpressions[i].isSame(
                expr.dimensionExpressions[i])) {
              return false;
            }
          }
        }

        return true;
      }
    }

    return false;
  }

  public final BinMethod getMethod() {
    return getConstructor();
  }

  public final BinItemFormatter getFormatter() {
    return new BinNewExpressionFormatter(this);
  }

  private BinTypeRef typeRef;
  private BinExpressionList expressionList;
  private BinExpression[] dimensionExpressions;
  private BinExpression arrayInitExpression;
  private BinCITypesDefStatement typeDefStatement;

  /** like new Outer().new Inner() - new Outer() is left expression of new Inner()*/
  private BinExpression leftExpression;

  private BinConstructor constructor = null;

  private BinTypeRef[] typeArguments;
}
