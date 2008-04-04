/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel.references;

import net.sf.refactorit.classmodel.BinExpressionList;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMemberInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;

/**
 *
 * @author Arseni Grigorjev
 */
public class FakeBinMemberInvocationReference extends BinItemReference {
  private final boolean methodInvocation;
  private final BinItemReference memberReference;
  private final BinItemReference invokedOn;
  
  public FakeBinMemberInvocationReference(
      final BinMemberInvocationExpression expr) {
    methodInvocation = (expr instanceof BinMemberInvocationExpression);
    memberReference = expr.getMember().createReference();
    invokedOn = expr.getInvokedOn().createReference();
  }

  public Object findItem(Project project){
    final BinType fromType = (BinType) invokedOn.restore(project);
    final BinMember member = (BinMember) memberReference.restore(project);

    if (methodInvocation){
      final BinMethod method = (BinMethod) member;
      return new BinMethodInvocationExpression(
        method, null, new BinExpressionList(method.getParameters()),
        fromType.getTypeRef(), null);
    } else {
      return new BinFieldInvocationExpression((BinField) member, null,
          fromType.getTypeRef(), null);
    }
  }
}
