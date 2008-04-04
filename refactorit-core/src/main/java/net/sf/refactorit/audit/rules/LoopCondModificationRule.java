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
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.expressions.BinAssignmentExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinIncDecExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.classmodel.statements.BinForStatement;
import net.sf.refactorit.classmodel.statements.BinStatement;
import net.sf.refactorit.classmodel.statements.BinWhileStatement;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.query.BinItemVisitor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class LoopCondModificationRule extends AuditRule {
  public static final String NAME = "loop_cond_modification";

  private MultiValueMap loopStopVars = new MultiValueMap();

  private MultiValueMap variableAssignments = new MultiValueMap();

  public void visit(BinForStatement s) {
    handleLoopCondition(s);
    super.visit(s);
  }

  public void visit(BinWhileStatement s) {
    handleLoopCondition(s);
    super.visit(s);
  }

  public void visit(BinIncDecExpression expression) {
    handleExpression(expression.getExpression());
    super.visit(expression);
  }

  public void visit(BinAssignmentExpression expression) {
    handleExpression(expression.getLeftExpression());
    super.visit(expression);
  }

  public void postProcess() {
    for (Iterator it = loopStopVars.keySet().iterator(); it.hasNext();) {
      BinStatement loopStatement = (BinStatement) it.next();
      List loopVariables = loopStopVars.get(loopStatement);
      for (int i = 0; i < loopVariables.size(); i++) {
        BinVariable var = (BinVariable) loopVariables.get(i);
        List varUsages = variableAssignments.get(var);
        if (varUsages != null) {
          for (int k = 0; k < varUsages.size(); k++) {
            BinExpression usageExpr = (BinExpression) varUsages.get(k);
            if (loopStatement instanceof BinForStatement) {
              BinForStatement forStatement = (BinForStatement) loopStatement;
              if (forStatement.getStatementList().getScope().contains(usageExpr.getScope())) {
                addViolation(new LoopConditionalsModification(usageExpr,
                    "super for"));
              }
            } else if (loopStatement instanceof BinWhileStatement) {
              BinWhileStatement whileStatement = (BinWhileStatement) loopStatement;
              if (whileStatement.getStatementList().getScope().contains(usageExpr.getScope()) &&
                  !isUsageHasUpperLoop(usageExpr, loopStatement)) {
                addViolation(new LoopConditionalsModification(usageExpr,
                    "super while"));
              }
            }
          }
        }
      }
    }
    loopStopVars.clear();
    variableAssignments.clear();
  }

  private void handleLoopCondition(BinStatement s) {
    BinExpression exprCondition = null;
    if (s instanceof BinForStatement) {
      exprCondition = ((BinForStatement) s).getCondition();
    } else if (s instanceof BinWhileStatement) {
      exprCondition = ((BinWhileStatement) s).getCondition();
    }

    if (exprCondition != null) {
      // collect variables, used in loop stop condition
      UsedVariableCollectVisitor varUseVisitor = new UsedVariableCollectVisitor();
      exprCondition.accept(varUseVisitor);
      List list = varUseVisitor.getVarUsageList();
      loopStopVars.putAll(s, list);
    }
  }

  private void handleExpression(BinExpression expr) {
    BinVariable var = null;
    if (expr instanceof BinVariableUseExpression) {
      var = ((BinVariableUseExpression) expr).getVariable();
    } else if (expr instanceof BinFieldInvocationExpression) {
      var = ((BinFieldInvocationExpression) expr).getField();
    }
    if (var != null && var.getTypeRef().isPrimitiveType()) {
      variableAssignments.put(var, expr);
    }
  }

  private boolean isUsageHasUpperLoop(BinExpression varUseExpression,
      BinStatement loopStatement) {
    if (loopStatement instanceof BinForStatement
        || loopStatement instanceof BinWhileStatement) {
      BinItemVisitable parent = varUseExpression.getParent();
      while (parent != null && !(parent instanceof BinMember)) {
        if (parent instanceof BinForStatement
            || parent instanceof BinWhileStatement) {
          return loopStatement.equals(parent);
        }
        parent = parent.getParent();
      }
    }
    return false;
  }

  class UsedVariableCollectVisitor extends BinItemVisitor {
    private List binVariableList = new ArrayList();

    public void visit(BinVariableUseExpression x) {
      BinVariable var = x.getVariable();
      if (var.getTypeRef().isPrimitiveType() 
          && !binVariableList.contains(var)) {
        binVariableList.add(var);
      }
      super.visit(x);
    }

    public void visit(BinFieldInvocationExpression x) {
      BinVariable var = x.getField();
      if (var.getTypeRef().isPrimitiveType() && 
          !binVariableList.contains(var)) {
        binVariableList.add(var);
      }
      super.visit(x);
    }

    public List getVarUsageList() {
      return binVariableList;
    }
  }
}

class LoopConditionalsModification extends AwkwardExpression {
  public LoopConditionalsModification(BinExpression expr, String loopType) {
    super(expr, "Expression modifies variable used in " + loopType
        + "-loop condition", "refact.audit.loop_cond_modification");
  }
}
