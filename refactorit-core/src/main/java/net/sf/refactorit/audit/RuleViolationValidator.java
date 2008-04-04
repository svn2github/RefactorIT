/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.audit;

import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.ui.audit.AuditTreeTableModel;
import net.sf.refactorit.ui.audit.AuditTreeTableNode;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author Arseni Grigorjev
 */
public class RuleViolationValidator {
  
  private AuditTreeTableModel model;
  
  private HashSet violationsToValidate;
  private List valid;

  public RuleViolationValidator(AuditTreeTableModel model) {
    this.model = model;
  }
  
  public RefactoringStatus validate(List violations){
    violationsToValidate = new HashSet(50);
    valid = new ArrayList(50);
    
    fillHash(violations);
    traverseModel();
    
    return reportStatus();
  }
  
  private RefactoringStatus reportStatus(){
    final RefactoringStatus status = new RefactoringStatus();
    
    final int lostViolationsCount = violationsToValidate.size();
    final int validViolationsCount = valid.size();

    boolean hasMessages = true;
    
    String message = "Some sources were changed manually,"
        + " so RefactorIT performed audits rerun action:\n";

    if (validViolationsCount == 0){ // no violations found!
      message += "None of the target violations were ";
    } else if (lostViolationsCount == 1){
      message += "1 target violation was not ";
    } else if (lostViolationsCount > 0){
      message += "" + lostViolationsCount + " target violations were not ";
    } else {
      hasMessages = false;
    }
    
    if (hasMessages){
      message += "found after audits rerun";
      status.addEntry(message, RefactoringStatus.INFO);
    }

    return status;
  }

  private void fillHash(List violations) {
     for (int i = 0, i_max = violations.size(); i < i_max; ++i){
       violationsToValidate.add(violations.get(i));
     }
  }

  private void traverseModel() {
    traverseNode((BinTreeTableNode) model.getRoot());
  }
  
  private void traverseNode(BinTreeTableNode node){
    if (node instanceof AuditTreeTableNode){
      validateWith(((AuditTreeTableNode) node).getRuleViolation());
    } else {
      List children = node.getChildren();
      for (int i = 0; i < children.size(); i++){
        traverseNode((BinTreeTableNode) children.get(i));
      }
    }
  }

  private void validateWith(RuleViolation violationFromModel) {
    if (violationsToValidate.contains(violationFromModel)){
      violationsToValidate.remove(violationFromModel);
      valid.add(violationFromModel);
    }
  }

  public List getValid() {
    if (this.valid == null){
      return Collections.EMPTY_LIST;
    }
    return this.valid;
  }
}
