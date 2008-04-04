/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.format;

import net.sf.refactorit.classmodel.expressions.BinLiteralExpression;

public class BinLiteralExpressionFormatter extends BinItemFormatter {

  private BinLiteralExpression expression;
  
  public BinLiteralExpressionFormatter(BinLiteralExpression literalExpression) {
    this.expression = literalExpression;
  }

  public String print() {
    return expression.getLiteral();
  }


}
