/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel.statements;

import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.parser.ASTImpl;


public final class BinAssertStatement extends BinStatement {
  final BinExpression testExpression;
  final BinExpression messageExpression;

  public BinAssertStatement(BinExpression testExpression,
      BinExpression messageExpression, ASTImpl rootAst) {
    super(rootAst);
    this.testExpression = testExpression;
    this.messageExpression = messageExpression;
  }

  public final BinExpression getTestExpression() {
    return testExpression;
  }

  public final BinExpression getMessageExpression() {
    return messageExpression;
  }

  public final void accept(net.sf.refactorit.query.BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public final void defaultTraverse(net.sf.refactorit.query.BinItemVisitor visitor) {
    testExpression.accept(visitor);
    if (messageExpression != null) {
      messageExpression.accept(visitor);
    }
  }
}
