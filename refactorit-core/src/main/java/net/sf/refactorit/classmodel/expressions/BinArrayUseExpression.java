/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.classmodel.expressions;

import net.sf.refactorit.classmodel.BinArrayType;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.parser.ASTImpl;



public final class BinArrayUseExpression extends BinExpression {
  public BinArrayUseExpression(BinExpression arrayExpression,
      BinExpression dimensionExpression, ASTImpl rootAst) {
    super(rootAst);
    if (Assert.enabled && arrayExpression == null) {
      Assert.must(false, "Array expression missing");
    }
    if (Assert.enabled && dimensionExpression == null) {
      Assert.must(false, "Dimension expression missing");
    }
    // ArrayUseExpression means: arrayExpression[dimExpr], arrayExpression return type must be BinArrayType as you can't index if you dont have array.
    if (Assert.enabled
        && !(arrayExpression.getReturnType().getBinType() instanceof
        BinArrayType)) {
      Assert.must(false, "illegal array use");
    }

    this.arrayExpression = arrayExpression;
    this.dimensionExpression = dimensionExpression;
  }

  public final BinTypeRef getReturnType() {
    // return ((blaah[a])[b])[c]

    BinTypeRef typeRef = arrayExpression.getReturnType();
    try {
      BinArrayType arrayType = (BinArrayType) typeRef.getBinType();
      int dimension = arrayType.getDimensions();
      BinTypeRef bottomType = arrayType.getArrayType();
      if (dimension == 1) {
        return bottomType;
      }
      return arrayType.getProject().createArrayTypeForType(bottomType,
          dimension - 1);
    } catch (Exception e) {
System.err.println("arrayExpression: " + typeRef);
      return typeRef;
    }
  }

  public final BinExpression getArrayExpression() {
    return arrayExpression;
  }

  public final BinExpression getDimensionExpression() {
    return dimensionExpression;
  }

  private BinExpression arrayExpression;
  private BinExpression dimensionExpression;

  public final void defaultTraverse(net.sf.refactorit.query.BinItemVisitor visitor) {
    arrayExpression.accept(visitor);
    dimensionExpression.accept(visitor);
  }

  public final void clean() {
    arrayExpression.clean();
    arrayExpression = null;
    dimensionExpression.clean();
    dimensionExpression = null;
    super.clean();
  }

  public final void accept(net.sf.refactorit.query.BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public final ASTImpl getClickableNode() {
    return (ASTImpl) getRootAst().getFirstChild();
  }

  public final boolean isSame(final BinItem other) {
    if (!(other instanceof BinArrayUseExpression)) {
      return false;
    }
    final BinArrayUseExpression expr = (BinArrayUseExpression) other;
    return this.arrayExpression.isSame(expr.arrayExpression)
        && this.dimensionExpression.isSame(expr.dimensionExpression);
  }
}
