/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.format;

import net.sf.refactorit.classmodel.expressions.BinArrayInitExpression;

/**
 * @author Juri Reinsalu
 */
public class BinArrayInitExpressionFormatter extends BinItemFormatter {
  
  private BinArrayInitExpression binArrayInitExpression;

  public BinArrayInitExpressionFormatter(BinArrayInitExpression binArrayInitExpression) {
    this.binArrayInitExpression=binArrayInitExpression;
  }

  public String print() {
    StringBuffer sb=new StringBuffer();
    if(binArrayInitExpression.getExpressions()!=null) {
      for (int i = 0; i < binArrayInitExpression.getExpressions().length; i++) {
        sb.append(binArrayInitExpression.getExpressions()[i].getFormatter().print());
        sb.append(",");
      }
      if(sb.length()>0) {//erase last coma
        sb.setLength(sb.length()-1);
      }
    }
    return sb.toString();
  }


}
