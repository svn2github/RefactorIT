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
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinLiteralExpression;
import net.sf.refactorit.classmodel.expressions.BinLogicalExpression;
import net.sf.refactorit.parser.JavaTokenTypes;


/**
 *
 *
 * @author Villu Ruusmann
 */
public class BooleanLiteralComparisonRule extends AuditRule {
  public static final String NAME = "boolean_comparison";
  
  public void visit(BinLogicalExpression expression) {
    // Must be '==' operator
    if (expression.getAssigmentType() == JavaTokenTypes.EQUAL) {
      if (containsBooleanLiteral(expression.getLeftExpression()) ||
          containsBooleanLiteral(expression.getRightExpression())) {
        addViolation(new BooleanLiteralComparison(expression));
      }
    }

    super.visit(expression);
  }

  private static boolean containsBooleanLiteral(BinExpression expression) {
    if (expression instanceof BinLiteralExpression) {
      BinLiteralExpression literal = (BinLiteralExpression) expression;

      return (literal.isTrue() || literal.isFalse());
    }

    return false;
  }
}


class BooleanLiteralComparison extends AwkwardExpression {

  BooleanLiteralComparison(BinExpression expression) {
    super(expression, "Comparison with boolean literal", "refact.audit.boolean_comparison");
  }

  public BinMember getSpecificOwnerMember() {
    return getSourceConstruct().getEnclosingStatement().getParentMember();
  }
}
