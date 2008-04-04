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
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;


/**
 *
 *
 * @author Arseni Grigorjev
 */
public class ServiceEnumUsagesRule extends AuditRule {
  public static final String NAME = "service_enum";
  
  public void visit(BinCIType type){
    if (type.isEnum()){
      addViolation(new EnumUsage(type));
    }
  }
  
  public void visit(BinFieldInvocationExpression expr){
    if (expr.getInvokedOn().getBinCIType().isEnum()){
      addViolation(new EnumInvocation(expr));
    }
  }
}

class EnumUsage extends AwkwardMember {
  EnumUsage(BinMember variable) {
    super(variable, "[service] enum usage", null);
  }
}

class EnumInvocation extends AwkwardSourceConstruct {
  EnumInvocation(BinFieldInvocationExpression expr) {
    super(expr, "[service] enum usage", null);
  }
}
