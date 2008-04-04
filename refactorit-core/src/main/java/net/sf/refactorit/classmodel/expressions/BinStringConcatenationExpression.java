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
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.query.BinItemVisitor;



/**
 * String concatenation expression.
 *
 * <p>
 * <h3>JLS 15.18 Additive Operators</h3>
 * If the type of either operand of a &#43; operator is <code>String</code>,
 * then the operation is string concatenation.
 * </p>
 *
 * <p>
 * <h3> JLS 15.18.1 String Concatenation Operator &#43;</h3>
 * If only one operand expression is of type <code>String</code>,
 * then string conversion is performed on the other operand to produce a string
 * at run time. The result is a reference to a newly created <code>String</code>
 * object that is the concatenation of the two operand strings.
 * The characters of the left-hand operand precede the characters of the
 * right-hand operand in the newly created string.
 * </p>
 *
 * <p>
 * <h3>Example</h3>
 * <code><pre>
 *Object tmp = null;
 *"Hello" &#43; tmp &#43; (13 &#43; 15)
 *
 *BinStringConcatenationExpression
 *&#43; BinStringConcatenationExpression
 *   - BinLiteralExpression "Hello"
 *   - BinFieldInvocationExpression tmp, local
 *&#43; BinArithmeticalExpression
 *   - BinLiteralExpression 13
 *   - BinLiteralExpression 15
 * </pre></code>
 * </p>
 */
public final class BinStringConcatenationExpression extends BinExpression {
  public BinStringConcatenationExpression(BinExpression leftExpression,
      BinExpression rightExpression,
      ASTImpl rootAst) {
    super(rootAst);
    this.leftExpression = leftExpression;
    this.rightExpression = rightExpression;
  }

  /**
   * Always returns <code>java.lang.String</code>.
   */
  public final BinTypeRef getReturnType() {
    // TODO: This can be optimized since it should always return
    //       java.lang.String.
    BinTypeRef returnType = null;
    final BinTypeRef leftReturnType = leftExpression.getReturnType();
    if ((leftReturnType != null) && (leftReturnType.isString())) {
      returnType = leftReturnType;
    }

    if (returnType == null) {
      returnType = rightExpression.getReturnType();
    }

    // FIXME: Does it always return java.lang.String?
    if (Assert.enabled) {
      Assert.must(returnType.isString(),
          "BinStringConcatenationExpression getReturnType must be java.lang.String");
    }

    return returnType;
  }

  public final BinExpression getLeftExpression() {
    return leftExpression;
  }

  public final BinExpression getRightExpression() {
    return rightExpression;
  }

  public final void accept(BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public final void defaultTraverse(BinItemVisitor visitor) {
    leftExpression.accept(visitor);
    rightExpression.accept(visitor);
  }

  public final void clean() {
    leftExpression.clean();
    leftExpression = null;
    rightExpression.clean();
    rightExpression = null;
    super.clean();
  }

  public final boolean isSame(BinItem other) {
    if (!(other instanceof BinStringConcatenationExpression)) {
      return false;
    }
    final BinStringConcatenationExpression expr
        = (BinStringConcatenationExpression) other;
    return this.leftExpression.isSame(expr.leftExpression)
        && this.rightExpression.isSame(expr.rightExpression);
  }

  private BinExpression leftExpression;
  private BinExpression rightExpression;

}
