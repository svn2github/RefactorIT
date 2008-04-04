/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.format;

import net.sf.refactorit.classmodel.expressions.BinArithmeticalExpression;

public class BinArithmeticalExpressionFormatter extends BinItemFormatter {

  private BinArithmeticalExpression expression;
  
  public BinArithmeticalExpressionFormatter(
      BinArithmeticalExpression arithmeticalExpression) {
    this.expression = arithmeticalExpression;
  }
  
  public String print() {
    StringBuffer buffer = new StringBuffer();
    
    buffer.append(expression.getLeftExpression().getFormatter().print());

    String operationType = expression.getRootAst().getText();

    if(FormatSettings.isSpaceAroudBinaryOperator()) {
      buffer.append(' ').append(operationType).append(' ');
    } else {
      buffer.append(operationType);
    }
    
    buffer.append(expression.getRightExpression().getFormatter().print());
    
    return buffer.toString();
  }

}
