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
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.expressions.BinInstanceofExpression;
import net.sf.refactorit.source.format.BinFormatter;


/**
 *
 *
 * @author Villu Ruusmann
 */
public class RedundantInstanceofRule extends AuditRule {
  public static final String NAME = "redundant_instanceof";

  public void visit(BinInstanceofExpression expression) {
    BinTypeRef leftType = expression.getLeftExpression().getReturnType();
    BinTypeRef rightType = expression.getRightExpression().getReturnType();

    if (leftType != null && rightType != null &&
        leftType.isReferenceType() && rightType.isReferenceType()) {
      if (leftType.isDerivedFrom(rightType)) {
        addViolation(new RedundantInstanceof(expression, leftType, rightType));
      }
    }

    super.visit(expression);
  }
}


class RedundantInstanceof extends AwkwardExpression {
  RedundantInstanceof(
      BinInstanceofExpression expression,
      BinTypeRef leftType, BinTypeRef rightType
      ) {
    super(expression, BinFormatter.formatQualified(leftType) 
        + " matches definitely " + BinFormatter.formatQualified(rightType), "refact.audit.redundant_instanceof");
  }

  public BinMember getSpecificOwnerMember() {
    return getSourceConstruct().getEnclosingStatement().getParentMember();
  }
}
