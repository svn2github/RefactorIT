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
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinLiteralExpression;
import net.sf.refactorit.classmodel.expressions.BinMemberInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;


/**
 *
 *
 * @author Villu Ruusmann
 */
public class NonStaticReferenceRule extends AuditRule {
  public static final String NAME = "nonstatic_reference";

  public void visit(BinFieldInvocationExpression expression) {
    analyze(expression, expression.getField());
    super.visit(expression);
  }

  public void visit(BinMethodInvocationExpression expression) {
    analyze(expression, expression.getMethod());
    super.visit(expression);
  }

  private final void violate(final BinExpression expr, final BinMember member){
    if (member instanceof BinField){
      addViolation(new NonStaticFieldAccess(expr, member));
    } else {
      addViolation(new NonStaticMethodAccess(expr, member));
    }
  }
  
  private final void analyze(final BinMemberInvocationExpression expression,
      final BinMember member){
    if (member.isStatic()) {
      BinExpression left = expression.getExpression();

      if (left instanceof BinLiteralExpression) {
        BinLiteralExpression literal = (BinLiteralExpression) left;

        if (literal.isThis() || literal.isSuper()) {
          violate(expression, member);
        }
      } else if (left instanceof BinVariableUseExpression) {
        violate(left, member);
      } else if (left instanceof BinMemberInvocationExpression) {
        violate(left, member);
      }
    }
  }
}


class NonStaticFieldAccess extends AwkwardExpression {
  NonStaticFieldAccess(BinExpression expression, BinMember field) {
    super(expression, "Static field " + field.getName() 
        + " invoked via object instance", "refact.audit.static_access_via_instance");
  }

  public BinMember getSpecificOwnerMember() {
    return getSourceConstruct().getEnclosingStatement().getParentMember();
  }
}


class NonStaticMethodAccess extends AwkwardExpression {
  NonStaticMethodAccess(BinExpression expression, BinMember method) {
    super(expression, "Static method " + method.getName() 
        + " invoked via object instance", "refact.audit.static_access_via_instance");
  }

  public BinMember getSpecificOwnerMember() {
    return getSourceConstruct().getEnclosingStatement().getParentMember();
  }
}
