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
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.expressions.BinAssignmentExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinIncDecExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;


/**
 *
 *
 * @author Villu Ruusmann
 */
public class ParameterAssignmentRule extends AuditRule {
  public static final String NAME = "parameter_assignment";

  public void visit(BinIncDecExpression expression) {
    checkAssignmentTarget(expression.getExpression());

    super.visit(expression);
  }

  public void visit(BinAssignmentExpression expression) {
    checkAssignmentTarget(expression.getLeftExpression());

    super.visit(expression);
  }

  private void checkAssignmentTarget(BinExpression expression) {
    if (expression instanceof BinVariableUseExpression) {
      BinVariable variable = ((BinVariableUseExpression) expression).
          getVariable();

      if (variable instanceof BinParameter) {
        BinParameter parameter = (BinParameter) variable;

        addViolation(new ParameterAssignment(expression, parameter));
      }
    }
  }
}


class ParameterAssignment extends AwkwardExpression {
  ParameterAssignment(BinExpression expression, BinParameter parameter) {
    super(expression, "Parameter " + parameter.getName() 
        + " is reassigned", "refact.audit.parameter_assignment");
  }

  public BinMember getSpecificOwnerMember() {
    return getSourceConstruct().getEnclosingStatement().getParentMember();
  }
}
