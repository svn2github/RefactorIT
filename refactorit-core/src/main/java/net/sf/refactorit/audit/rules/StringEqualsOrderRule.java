/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules;

import net.sf.refactorit.audit.AuditRule;
import net.sf.refactorit.audit.AwkwardExpression;
import net.sf.refactorit.audit.MultiTargetCorrectiveAction;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.classmodel.BinExpressionList;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.expressions.BinArrayUseExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinIncDecExpression;
import net.sf.refactorit.classmodel.expressions.BinLiteralExpression;
import net.sf.refactorit.classmodel.expressions.BinMemberInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinStringConcatenationExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.module.TreeRefactorItContext;

import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 * @author Oleg Tsernetsov
 */
public class StringEqualsOrderRule extends AuditRule {
  public static final String NAME = "str_equals_order";

  public void visit(BinMethodInvocationExpression expr) {
    BinMethod meth = expr.getMethod();
    BinExpressionList exprList = expr.getExpressionList();
    if ("equals".equals(meth.getName())) {
      BinExpression invExpr = expr.getExpression();
      if (invExpr != null && 
          invExpr.getReturnType().getTypeRef().equals(
          getBinTypeRef("java.lang.String"))
          && isLiteralLikeExpression(exprList.getExpressions()[0])) {
        addViolation(new StringEqualsOrder(expr));
      }
    }
    super.visit(expr);
  }

  private boolean isLiteralLikeExpression(BinExpression expr) {
    if (expr instanceof BinLiteralExpression) {
      return true;
    }

    if (expr instanceof BinStringConcatenationExpression) {
      BinStringConcatenationExpression concat = 
        (BinStringConcatenationExpression) expr;
      return concatTraverse(concat);
    }

    return false;
  }

  private boolean concatTraverse(BinStringConcatenationExpression expr) {
    boolean result = true;
    if (!(expr.getLeftExpression() instanceof BinLiteralExpression)) {
      if (expr.getLeftExpression() instanceof 
          BinStringConcatenationExpression) {
        result &= concatTraverse((BinStringConcatenationExpression) (expr
            .getLeftExpression()));
      } else {
        return false;
      }
    }

    if (!(expr.getRightExpression() instanceof BinLiteralExpression)) {
      if (expr.getRightExpression() instanceof 
          BinStringConcatenationExpression) {
        result &= concatTraverse((BinStringConcatenationExpression) (expr
            .getRightExpression()));
      } else {
        return false;
      }
    }
    return result;
  }
}

class StringEqualsOrder extends AwkwardExpression {
  private int assignmentType;

  public StringEqualsOrder(BinMethodInvocationExpression expr) {
    super(expr, "Unsafe String.equals() method call",
        "refact.audit.str_equals_order");
  }

  public List getCorrectiveActions() {
    return Collections.singletonList(ChangeEqualsOrderAction.instance);
  }
}

class ChangeEqualsOrderAction extends MultiTargetCorrectiveAction {
  public static final ChangeEqualsOrderAction instance = 
    new ChangeEqualsOrderAction();

  public String getKey() {
    return "refactorit.audit.action.str_equals_order.correct";
  }

  public String getName() {
    return "Swap parameter and invocator objects";
  }

  protected Set process(TreeRefactorItContext context,
      TransformationManager manager, RuleViolation violation) {
    if (!(violation instanceof StringEqualsOrder)) {
      return Collections.EMPTY_SET;
    }

    BinMethodInvocationExpression methInv = 
      (BinMethodInvocationExpression) ((AwkwardExpression) violation)
        .getSourceConstruct();

    CompilationUnit compilationUnit = methInv.getCompilationUnit();

    BinExpressionList exprList = methInv.getExpressionList();
    BinExpression paramExpr = (BinExpression) exprList.getExpressions()[0];
    BinExpression invExpr = (BinExpression) methInv.getExpression();

    String newParamExprLine = invExpr.getText();
    if(needBraces(invExpr)) {
      newParamExprLine = "(" + newParamExprLine + ")";
    }
   
    String newInvExprLine = paramExpr.getText();
    if(needBraces(paramExpr)) {
      newInvExprLine = "(" + newInvExprLine + ")";
    }
    newInvExprLine = newInvExprLine+".";
    
    // erase old invocator line
    StringEraser eraser = new StringEraser(compilationUnit, invExpr
        .getStartLine(), invExpr.getStartColumn() - 1, invExpr.getEndLine(),
        invExpr.getEndColumn() - 1, true, false);
    eraser.setRemoveLinesContainingOnlyComments(true);
    manager.add(eraser);

    // erase old parameter text
    eraser = new StringEraser(compilationUnit, paramExpr.getStartLine(),
        paramExpr.getStartColumn() - 1, paramExpr.getEndLine(), paramExpr
            .getEndColumn() - 1, true, false);
    eraser.setRemoveLinesContainingOnlyComments(true);
    manager.add(eraser);

    // insert new invocator line
    StringInserter inserter = new StringInserter(compilationUnit, invExpr
        .getStartLine(), invExpr.getStartColumn(), newInvExprLine);
    manager.add(inserter);

    // insert new parameter line
    inserter = new StringInserter(compilationUnit, paramExpr.getStartLine(),
        paramExpr.getStartColumn(), newParamExprLine);
    manager.add(inserter);

    return Collections.singleton(compilationUnit);
  }

  private static boolean needBraces(BinExpression expr) {
    if (expr instanceof BinVariableUseExpression
        || expr instanceof BinLiteralExpression
        || expr instanceof BinMemberInvocationExpression
        || expr instanceof BinArrayUseExpression
        || expr instanceof BinIncDecExpression) {
      return false;
    }
    return true;
  }
}
