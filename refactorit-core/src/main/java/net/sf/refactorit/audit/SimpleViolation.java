/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit;


import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.parser.ASTImpl;

import java.util.Collections;
import java.util.List;

public class SimpleViolation extends RuleViolation {
  private ASTImpl ast;
  private String message;
  private String typeAcronym;
  private String helpId;
 
  public SimpleViolation(BinTypeRef binCITypeRef, ASTImpl ast, String message,
    String helpId) {
    super(binCITypeRef);
    this.ast = ast;
    this.message = message;
    this.helpId = helpId;
  }

  public BinMember getSpecificOwnerMember() {
    return null;
  }

  public List getCorrectiveActions() {
    return Collections.EMPTY_LIST;
  }

  public ASTImpl getAst() {
    return ast;
  }

  public String getMessage() {
    return message;
  }

  public String getTypeShortName() {
    return typeAcronym;
  }

  public String getHelpTopicId() {
    return helpId;
  }
  
  public void setAuditRule(AuditRule rule) {
    super.setAuditRule(rule);
    typeAcronym = rule.getKey();
  }
  
  public void setTypeAcronym(String typeAcronym){
    this.typeAcronym = typeAcronym;
  }
}
