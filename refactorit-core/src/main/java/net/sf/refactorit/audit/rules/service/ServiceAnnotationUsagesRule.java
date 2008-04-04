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
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMember;


/**
 *
 *
 * @author Arseni Grigorjev
 */
public class ServiceAnnotationUsagesRule extends AuditRule {
  public static final String NAME = "service_annotations";
  
  public void visit(BinCIType type){
    if (type.isAnnotation()){
      addViolation(new AnnotationUsage(type));
    }
  }
}

class AnnotationUsage extends AwkwardMember {
  AnnotationUsage(BinMember variable) {
    super(variable, "[service] annotation usage", null);
  }
}
