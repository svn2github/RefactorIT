/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.classmodel.statements;

import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.source.format.BinItemFormatter;
import net.sf.refactorit.source.format.BinReturnStatementFormatter;


/**
 *		class BinReturnStatement
 *		Purpose :	Defines class for return expression
 */

public final class BinReturnStatement extends BinStatement {

  public BinReturnStatement(BinExpression expression, ASTImpl rootAst) {
    super(rootAst);
    this.expression = expression;
  }

  public final BinExpression getReturnExpression() {
    return expression;
  }

  public final void accept(net.sf.refactorit.query.BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public final void defaultTraverse(net.sf.refactorit.query.BinItemVisitor visitor) {

    // Check expression's existence
    if (expression != null) {
      expression.accept(visitor);
    }
  }

  public final void clean() {
    if (expression != null) {
      expression.clean();
      expression = null;
    }
    super.clean();
  }

  private BinExpression expression;

  public final BinMethod getMethod() {
    return (BinMethod) getParentMember();
  }
  
  public final BinItemFormatter getFormatter() {
    return new BinReturnStatementFormatter(this);
  }
}
