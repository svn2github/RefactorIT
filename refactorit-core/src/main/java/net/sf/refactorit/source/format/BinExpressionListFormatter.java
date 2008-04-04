/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.format;

import net.sf.refactorit.classmodel.BinExpressionList;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinNewExpression;

/**
 * @author Juri Reinsalu
 */
public class BinExpressionListFormatter extends BinItemFormatter {
  private BinExpressionList expressionList;
  public BinExpressionListFormatter(BinExpressionList expressionList){
    this.expressionList=expressionList;
  }

  public String print() {
    if(expressionList.getParent() instanceof BinMethodInvocationExpression ||
            expressionList.getParent() instanceof BinNewExpression) {
      StringBuffer sb=new StringBuffer();
      BinExpression[]args=expressionList.getExpressions();
      for (int i = 0; i < args.length; i++) {
        sb.append(args[i].getFormatter().print());
        sb.append(", ");
      }
      if(sb.length()>0) {
        sb.setLength(sb.length()-2);
      }
      return sb.toString();
    }
    
    return null;
  }

}
