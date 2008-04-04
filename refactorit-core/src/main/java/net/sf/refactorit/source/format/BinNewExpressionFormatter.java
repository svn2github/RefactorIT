/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.format;

import net.sf.refactorit.classmodel.BinArrayType;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinNewExpression;

/**
 * @author Juri Reinsalu
 */
public class BinNewExpressionFormatter extends BinItemFormatter {

  private BinNewExpression newExpression;

  public BinNewExpressionFormatter(BinNewExpression newExpression) {
    this.newExpression = newExpression;
  }

  public String print() {
    if (newExpression.getRootAst() != null) {
      return newExpression.getCompilationUnit().getContent()
          .substring(newExpression.getStartPosition(), newExpression.getEndPosition()).trim();
    } else {
      StringBuffer sb = new StringBuffer();
      sb.append("new ");
      sb.append(formatTypeName(newExpression.getTypeRef().getNonArrayType()));
      if (!newExpression.getTypeRef().isArray()) {
        sb.append("(");
        //expressions go here;
        sb.append(newExpression.getExpressionList().getFormatter().print());
        sb.append(")");
      } else {
        BinExpression[] dimensionExpressions = newExpression
                .getDimensionExpressions();
        if (dimensionExpressions == null) {
          for (int i = 0; i < ((BinArrayType) newExpression.getTypeRef().getBinType())
                  .getDimensions(); i++) {
            sb.append("[]");
          }
        } else {
        for (int i = 0; i < dimensionExpressions.length; i++) {
            sb.append(dimensionExpressions[i].getFormatter().print());
          }
        }
        if (newExpression.getArrayInitExpression() != null) {
          sb.append(" {");
          sb.append(newExpression.getArrayInitExpression().getFormatter().print());
          sb.append("}");
        }
      }
      return sb.toString();
    }
  }


}
