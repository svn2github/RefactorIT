/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.inlinevariable;

import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinNewExpression;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.ASTUtil;
import net.sf.refactorit.parser.JavaTokenTypes;


/**
 * @author  RISTO A
 */
class InlinedExpression {
  private ASTImpl usage;
  private BinVariable var;
  private String exprNodeText;

  public InlinedExpression(BinVariable var, ASTImpl usage, String exprNodeText) {
    this.usage = usage;
    this.var = var;
    this.exprNodeText = exprNodeText;
  }

  public String getStringForm() {
    if (requiresParentheses()) {
      return "(" + exprNodeText + ")";
    } else {
      return exprNodeText;
    }
  }

  private boolean requiresParentheses() {
    return valueRequiresParentheses() && usageRequiresParentheses();
  }

  private boolean valueRequiresParentheses() {
    ASTImpl ast = var.getExprNode();
    if (ast == null) {
      return false;

    } else {
      boolean multipartExpr = ASTUtil.getAllChildren(ast).size() > 1;
      return multipartExpr &&
          (!(var.getExpression() instanceof BinNewExpression)) &&
          (!(var.getExpression() instanceof BinMethodInvocationExpression));
    }
  }

  private boolean usageRequiresParentheses() {
    return usage.getParent().getType() != JavaTokenTypes.EXPR &&
        usage.getParent().getType() != JavaTokenTypes.LT &&
        usage.getParent().getType() != JavaTokenTypes.LE &&
        usage.getParent().getType() != JavaTokenTypes.GT &&
        usage.getParent().getType() != JavaTokenTypes.LE;
  }

}
