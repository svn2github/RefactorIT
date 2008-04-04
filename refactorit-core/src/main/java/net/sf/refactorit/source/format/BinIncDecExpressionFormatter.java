/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.format;

import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinIncDecExpression;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.parser.JavaTokenTypes;

public class BinIncDecExpressionFormatter extends BinItemFormatter {

  private BinIncDecExpression expression;
  
  public BinIncDecExpressionFormatter(BinIncDecExpression incDecExpression) {
    this.expression = incDecExpression;
  }
  
  public String print() {
    StringBuffer buffer = new StringBuffer();
    BinExpression subExpression = expression.getExpression();

    int type = expression.getType();

    String prefix = null;
    String suffix = null;
    switch (type) {
      case JavaTokenTypes.INC:
        prefix = "++";
        break;
      case JavaTokenTypes.POST_INC:
        suffix = "++";
        break;
      case JavaTokenTypes.DEC:
        prefix = "--";
        break;
      case JavaTokenTypes.POST_DEC:
        suffix = "--";
        break;
      default:
        Assert.must(false, "Unknown inc/dec epxpression, type = " + type);
    }
    if(prefix != null) {
      buffer.append(prefix);
    }
    
    if(subExpression != null) {
      buffer.append(subExpression.getFormatter().print());
    }
    
    if(suffix != null) {
      buffer.append(suffix);
    }
    
    return buffer.toString();
  }


}
