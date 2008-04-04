/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.format;

import net.sf.refactorit.classmodel.expressions.BinUnaryExpression;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.parser.JavaTokenTypes;

public class BinUnaryExpressionFormatter extends BinItemFormatter {

  public BinUnaryExpression expression;
  
  public BinUnaryExpressionFormatter(BinUnaryExpression unaryExpression) {
    expression = unaryExpression;
  }
  
  public String print() {
    StringBuffer buffer = new StringBuffer();
    int type = expression.getType();
    switch(type) {
      case JavaTokenTypes.LNOT:
        buffer.append('!');
        break;
      case JavaTokenTypes.BNOT:
        buffer.append('~');
        break;
      case JavaTokenTypes.UNARY_MINUS:
        buffer.append('-');
        break;
      case JavaTokenTypes.UNARY_PLUS:
        buffer.append('+');
        break;
      default:
        Assert.must(false, "Unknown unary epxpression, type = " + type);
    }
    
    buffer.append(expression.getExpression().getFormatter().print());
    
    return buffer.toString();
  }

}
