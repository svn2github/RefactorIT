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
import net.sf.refactorit.classmodel.BinTypeRefManager;
import net.sf.refactorit.classmodel.BinTypeRefVisitor;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.source.edit.CompoundASTImpl;



public final class BinCastExpression extends BinExpression
    implements BinTypeRefManager {

  public BinCastExpression(final BinExpression expression,
      final BinTypeRef returnType, final ASTImpl rootAst) {
    super(rootAst);
    if (Assert.enabled) {
      Assert.must(returnType != null, "Cast must have typeref!");
    }
    if (Assert.enabled) {
      Assert.must(expression != null, "Cast must have expression!");
    }
    this.expression = expression;
    this.returnType = returnType;
  }

  public BinTypeRef getReturnType() {
    return returnType;
  }

  public BinExpression getExpression() {
    return expression;
  }

  public void accept(net.sf.refactorit.query.BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public void defaultTraverse(net.sf.refactorit.query.BinItemVisitor visitor) {
    expression.accept(visitor);
  }

  public final void accept(BinTypeRefVisitor visitor) {
    if (this.returnType != null) {
      this.returnType.accept(visitor);
    }
  }

  public ASTImpl getClickableNode() {
    return getTypeNode();
  }

  public ASTImpl getTypeNode() {
    try {
      return new CompoundASTImpl(
          (ASTImpl) getRootAst().getFirstChild().getFirstChild());
    } catch (NullPointerException e) {
      if (this.returnType.hasCoordinates()) {
        return this.returnType.getNode();
      } else {
        return getRootAst();
      }
    }
  }

  public void clean() {
    expression.clean();
    expression = null;
    returnType = null;

    super.clean();
  }

  public boolean isSame(final BinItem other) {
    if (!(other instanceof BinCastExpression)) {
      return false;
    }
    final BinCastExpression expr = (BinCastExpression) other;
    return this.getReturnType().equals(expr.getReturnType())
        && this.expression.isSame(expr.expression);
  }

  private BinExpression expression;
  private BinTypeRef returnType;
}
