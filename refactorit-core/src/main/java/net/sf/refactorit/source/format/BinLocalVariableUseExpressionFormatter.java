/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.format;

import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;

public class BinLocalVariableUseExpressionFormatter extends BinItemFormatter {

  BinVariableUseExpression expression;

  public BinLocalVariableUseExpressionFormatter(
      BinVariableUseExpression localVariableUseExpression) {
    this.expression = localVariableUseExpression;
  }
  
  public String print() {
    return this.expression.getVariable().getFormatter().print();
  }

}
