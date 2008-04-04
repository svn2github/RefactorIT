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
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.expressions.BinAssignmentExpression;
import net.sf.refactorit.classmodel.expressions.BinConditionalExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.module.TreeRefactorItContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 *
 *
 * @author Villu Ruusmann
 */
public class SelfAssignmentRule extends AuditRule {
  public static final String NAME = "self_assignment";

  /* Cache */
  private List buffer = new ArrayList();

  public void visit(BinAssignmentExpression expression) {
    // Must be '=' operator
    if (expression.getAssignmentType() == JavaTokenTypes.ASSIGN) {
      BinExpression left = expression.getLeftExpression();
      BinExpression right = expression.getRightExpression();

      buffer.clear();

      // Self-assignment on a local variable
      if (left instanceof BinVariableUseExpression) {
        BinVariable variable = ((BinVariableUseExpression) left).getVariable();

        extractVariableUses(variable, right, buffer);

        for (int i = 0; i < buffer.size(); i++) {
          addViolation(new SelfAssignmentOnVariable(expression, variable));
        }
      } else if (left instanceof BinFieldInvocationExpression) {
        // Self-assignment on a field
        BinField field = ((BinFieldInvocationExpression) left).getField();

        extractFieldInvocations(field, right, buffer);

        for (int i = 0; i < buffer.size(); i++) {
          if (!sameField((BinFieldInvocationExpression) left,
              (BinFieldInvocationExpression) buffer.get(i))) {
            continue;
          }

          addViolation(new SelfAssignmentOnField(expression, field));
        }
      }
    }

    super.visit(expression);
  }

  private void extractVariableUses(BinVariable variable, BinExpression expr,
      List buffer) {
    if (expr instanceof BinVariableUseExpression) {
      BinVariableUseExpression use = (BinVariableUseExpression) expr;

      if (use.getVariable() == variable) {
        buffer.add(use);
      }
    }

    if (expr instanceof BinConditionalExpression) {
      BinConditionalExpression cndExpr = (BinConditionalExpression) expr;

      // Descend
      extractVariableUses(variable, cndExpr.getTrueExpression(), buffer);
      extractVariableUses(variable, cndExpr.getFalseExpression(), buffer);
    }
  }

  private void extractFieldInvocations(BinField field, BinExpression expr,
      List buffer) {
    if (expr instanceof BinFieldInvocationExpression) {
      BinFieldInvocationExpression invocation = (BinFieldInvocationExpression)
          expr;

      if (invocation.getField() == field) {
        buffer.add(invocation);
      }
    }

    if (expr instanceof BinConditionalExpression) {
      BinConditionalExpression cndExpr = (BinConditionalExpression) expr;

      // Descend
      extractFieldInvocations(field, cndExpr.getTrueExpression(), buffer);
      extractFieldInvocations(field, cndExpr.getFalseExpression(), buffer);
    }
  }

  private static boolean sameField(
      BinFieldInvocationExpression left, BinFieldInvocationExpression right) {
    if (left.invokedViaThisReference() || left.invokedViaSuperReference()) {
      return right.invokedViaThisReference() || right.invokedViaSuperReference();
    }

    BinItem leftChild = left.getExpression();
    BinItem rightChild = right.getExpression();

    if (leftChild instanceof BinVariableUseExpression &&
        rightChild instanceof BinVariableUseExpression) {
      BinVariable leftVar = ((BinVariableUseExpression) leftChild).getVariable();
      BinVariable rightVar = ((BinVariableUseExpression) rightChild).
          getVariable();

      // Variable instances must be equal
      return (leftVar == rightVar);
    }

    return false;
  }
}


class SelfAssignment extends AwkwardExpression {
  SelfAssignment(BinAssignmentExpression expression, String message) {
    super(expression, message, "refact.audit.self_assignments");
  }

  public BinMember getSpecificOwnerMember() {
    return getSourceConstruct().getEnclosingStatement().getParentMember();
  }
  
  public List getCorrectiveActions() {
    return Collections.singletonList(RemoveSelfAssignment.instance);
  }
}


class SelfAssignmentOnField extends SelfAssignment {
  SelfAssignmentOnField(BinAssignmentExpression expression, BinField field) {
    super(expression, "Self-assignment on a field " +
        field.getName() + " - most likely a bug");
  }
}


class SelfAssignmentOnVariable extends SelfAssignment {
  SelfAssignmentOnVariable(BinAssignmentExpression expression,
      BinVariable variable) {
    super(expression, "Self-assignment on a local variable " +
        variable.getName() + " - most likely a bug!");
  }
}


class RemoveSelfAssignment extends MultiTargetCorrectiveAction {
  static final RemoveSelfAssignment instance = new RemoveSelfAssignment();

  public String getKey() {
    return "refactorit.audit.action.self_assignment.remove";
  }

  public String getName() {
    return "Remove self assignment";
  }

  public String getMultiTargetName() {
    return "Remove self assignments";
  }

  protected Set process(TreeRefactorItContext context, final TransformationManager manager, RuleViolation violation) {
    if (!(violation instanceof SelfAssignment)) {
      return Collections.EMPTY_SET; // foreign violation - do nothing
    }

    CompilationUnit compilationUnit = violation.getCompilationUnit();

    StringEraser eraser = new StringEraser(
        compilationUnit, violation.getAst().getParent(), true);
    eraser.setRemoveLinesContainingOnlyComments(true);
    manager.add(eraser);

    return Collections.singleton(compilationUnit);
  }
}
