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
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinLiteralExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.source.format.BinFormatter;
import net.sf.refactorit.utils.AuditProfileUtils;

import java.util.List;

/**
 * Object.equals() comparing variables of different types.
 *
 * @author  Arseni Grigorjev
 */
public class EqualsOnDiffTypesRule extends AuditRule {
  public static final String NAME = "equals_on_diff_types";

  private boolean skipSameBranch = true;

  public void init() {
    skipSameBranch = AuditProfileUtils.getBooleanOption(getConfiguration(),
        "skip", "same_branch", skipSameBranch);
    super.init();
    if (isTestRun()) {
      skipSameBranch = false;
    }
  }

  private List getHierarchy(BinTypeRef ref) {
    List hierarchy = ref.getTypeRef().getAllSubclasses();
    hierarchy.addAll(ref.getTypeRef().getAllSupertypes());
    hierarchy.add(ref);

    return hierarchy;
  }

  private boolean isOfTheSameBranch(BinTypeRef invRef, BinTypeRef argRef) {

    return (invRef.isDerivedFrom(argRef) || argRef.isDerivedFrom(invRef));
    /*List invHierarchy = getHierarchy(invRef);
     List argHierarchy = getHierarchy(argRef);

     for(Iterator it = invHierarchy.iterator(); it.hasNext();){
     if(argHierarchy.contains(it.next())){
     return true;
     }
     }
     return false;*/
  }


  // FIXME: this is really wrong code - there could be several supertypes!!!
  // will this find correct supertype in any case, or should implement recursive call
  private BinTypeRef getTypeReference(BinTypeRef typeRef){
    while (typeRef != null && typeRef.getBinCIType() != null && typeRef.getBinCIType().isTypeParameter()) {
      BinTypeRef[] arr = typeRef.getSupertypes();
      typeRef = arr[0];
      for (int k=0; k < arr.length; k++) {
        if (!arr[k].getBinCIType().isTypeParameter()) {
          typeRef = arr[k];
        }
      }
    }
    return typeRef;
  }

  public void visit(BinMethodInvocationExpression expression) {
    BinMethod method = expression.getMethod();
    /*
     * optimization fix: quickly check the name, if equals -- then perform more
     * detailed check, that it is Object.equals() with needed params (MIGHTLY
     * decreases processing time!)
     */
    if (!method.getName().equals("equals")) {
      super.visit(expression);
      return;
    }

    // get BinMethod for java.lang.Object.equals(java.lang.Object)
    BinMethod objectEquals = method.getProject().getObjectRef().getBinCIType()
        .getAccessibleMethods("equals", method.getOwner().getBinCIType())[0];
    // get top methods for the method
    List topMethods = method.getTopMethods();

    // check, if it is Object.equals() we are dealing with
    if (topMethods.contains(objectEquals) || objectEquals == method) {
      // only one param, so take elem 0
      BinExpression argExpression = (expression.getExpressionList()
          .getExpressions())[0];

      BinType invType = expression.getInvokedOn().getBinType();

      // if Object.equals(null); consider all ok
      if (argExpression instanceof BinLiteralExpression
          && ((BinLiteralExpression) argExpression).getLiteral().equals(
              BinLiteralExpression.NULL)) {
        super.visit(expression);
        return;
      }

      BinType argType = argExpression.getReturnType().getBinType();

      // case types are different
      if (argType != invType) {
        BinTypeRef invRef = getTypeReference(invType.getTypeRef());
        BinTypeRef argRef = getTypeReference(argType.getTypeRef());



        if (!invRef.equals(argRef)) {
          if (!isOfTheSameBranch(invRef, argRef)) {
            addViolation(new EqualsOnDiffTypes(expression, BinFormatter
                .format(argRef), BinFormatter.format(invRef)));
          } else if(!skipSameBranch){
            addViolation(new EqualsOnDiffTypesSameBranch(expression,
                BinFormatter.format(argRef), BinFormatter.format(invRef)));
          }
        }
      }
    }
    super.visit(expression);
  }
}

class EqualsOnDiffTypes extends AwkwardExpression {

  public EqualsOnDiffTypes(BinExpression expression, String argType,
      String invType) {
    super(expression, "equals() compares variables of different types ('"
        + invType + "' and '" + argType + "')", "refact.audit.equals_on_diff_types");
  }

  public BinMember getSpecificOwnerMember() {
    return getSourceConstruct().getEnclosingStatement().getParentMember();
  }
}

class EqualsOnDiffTypesSameBranch extends AwkwardExpression {
  public EqualsOnDiffTypesSameBranch(BinExpression expression, String argType,
      String invType) {
    super(expression,
        "equals() compares variables of different types which are from the"
            + " same inheritance branch ('" + invType + "' and '" + argType
            + "')", "refact.audit.equals_on_diff_types");
  }

  public BinMember getSpecificOwnerMember() {
    return getSourceConstruct().getEnclosingStatement().getParentMember();
  }
}
