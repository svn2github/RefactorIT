/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.service;

import net.sf.refactorit.audit.AuditRule;
import net.sf.refactorit.audit.AwkwardMember;
import net.sf.refactorit.audit.AwkwardSourceConstruct;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.expressions.BinConstructorInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;


/**
 *
 *
 * @author Arseni Grigorjev
 */
public class ServiceGenericsUsagesRule extends AuditRule {
  public static final String NAME = "service_generics";
  
  public void visit(BinCIType type){
    if (type.isTypeParameter()){
      addViolation(new TypeParameter(type));
    }
  }
  
  public void visit(BinMethodInvocationExpression expr){
    if (expr.getTypeArguments() != null 
        && expr.getTypeArguments().length > 0){
      addViolation(new GenericsUsageMethod(expr));
    }
  }
  
  public void visit(BinConstructorInvocationExpression expr){
    if (expr.getTypeArguments() != null 
        && expr.getTypeArguments().length > 0){
      addViolation(new GenericsUsageMethod(expr));
    }
  }
  
  public void visit(BinLocalVariable var) {
    if (checkGenerics(var.getTypeRef())){
      addViolation(new TypeArgumentsTypeLevel(var));
    }
  }
  
  public void visit(BinField var) {
    if (checkGenerics(var.getTypeRef())){
      addViolation(new TypeArgumentsTypeLevel(var));
    }
  }
  
  private boolean checkGenerics(BinTypeRef type){
    return type.getTypeArguments() != null
        && type.getTypeArguments().length > 0;
  }
}

class TypeParameter extends AwkwardMember {
  TypeParameter(BinMember variable) {
    super(variable, "[service] generics usage: type parameter", null);
  }
}

class TypeArgumentsTypeLevel extends AwkwardMember {
  TypeArgumentsTypeLevel(BinMember variable) {
    super(variable, "[service] generics usage: type arguments", null);
  }
}

class GenericsUsageMethod extends AwkwardSourceConstruct {
  GenericsUsageMethod(BinExpression expr) {
    super(expr, "[service] generics usage: type arguments", null);
  }
}
