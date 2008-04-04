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


public final class BinAssignmentExpression extends BinExpression {
  public BinAssignmentExpression(BinExpression leftExpression,
      BinExpression rightExpression, ASTImpl rootNode) {
    super(rootNode);
    this.leftExpression = leftExpression;
    this.rightExpression = rightExpression;
    this.assignmentType = rootNode.getType();
  }

  public BinTypeRef getReturnType() {
    return leftExpression.getReturnType();
  }

  public BinExpression getLeftExpression() {
    return leftExpression;
  }

  public BinExpression getRightExpression() {
    return rightExpression;
  }

  public boolean leftIsVariable() {
    return leftExpression instanceof BinVariableUseExpression;
  }

  public boolean leftIsField() {
    return leftExpression instanceof BinFieldInvocationExpression;
  }

  public boolean leftIsArray() {
    return leftExpression instanceof BinArrayUseExpression;
  }

  public int getAssignmentType() {
    return assignmentType;
  }

  public void defaultTraverse(net.sf.refactorit.query.BinItemVisitor visitor) {
    rightExpression.accept(visitor);
    leftExpression.accept(visitor);
  }

  public void accept(net.sf.refactorit.query.BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public void clean() {
    rightExpression.clean();
    rightExpression = null;
    leftExpression.clean();
    leftExpression = null;
    super.clean();
  }

  public boolean isSame(BinItem other) {
    if (!(other instanceof BinAssignmentExpression)) {
      return false;
    }
    final BinAssignmentExpression expr = (BinAssignmentExpression) other;
    return this.assignmentType == expr.assignmentType
        && this.leftExpression.isSame(expr.leftExpression)
        && this.rightExpression.isSame(expr.rightExpression);
  }

  private BinExpression leftExpression;
  private BinExpression rightExpression;
  private final int assignmentType;
}
