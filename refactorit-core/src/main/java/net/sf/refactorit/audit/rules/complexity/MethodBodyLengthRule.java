/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.complexity;


import net.sf.refactorit.audit.AuditRule;
import net.sf.refactorit.audit.AwkwardMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.statements.BinStatementList;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.utils.AuditProfileUtils;
import net.sf.refactorit.utils.GetterSetterUtils;

import org.w3c.dom.Element;


/**
* Finds methods that are: shorter than some fixed minimal size and longer than
* fixed maximum length.
*
* @author Arseni Grigorjev
*/
public class MethodBodyLengthRule extends AuditRule {
  public static final String NAME = "method_body_length";
  
  private int minLen = 0;
  private int maxLen = 9999999;
  private boolean allowMin = true;
  private boolean allowMax = true;
  private boolean skipGetters = true;
  
  private StatementsCounter counter = new StatementsCounter();
  
  public void init() {
    final Element configuration = getConfiguration();
    this.minLen = AuditProfileUtils.getIntOption(configuration, "options",
        "min_value", minLen);
    this.maxLen = AuditProfileUtils.getIntOption(configuration, "options",
        "max_value", maxLen);
    this.skipGetters = AuditProfileUtils.getBooleanOption(configuration,
        "options", "skip_getters", true);
    this.allowMin = AuditProfileUtils.getBooleanOption(configuration, "options",
        "allow_min_value", true);
    this.allowMax = AuditProfileUtils.getBooleanOption(configuration, "options",
        "allow_max_value", true);
    super.init();
  }
  
  
  public void visit (BinMethod method){
    if (!method.hasBody()) {
      return; // super.visit will be useless anyway
    }

    // for counting statements runs a counter that is based on visitor pattern
    method.getBody().accept(counter);
        
    // if is shorter
    if (this.allowMin && counter.codeLength < minLen){

      // check conditions for skipping this violation
      if (!this.skipGetters || !(GetterSetterUtils.isGetterMethod(method, true)
          || GetterSetterUtils.isSetterMethod(method, true)) ){
        addViolation(new MethodBodyLength(method, "less than "+minLen
          +" statements: "+counter.codeLength));
      }
      
    // if is longer
    } else if (this.allowMax && counter.codeLength > maxLen){

      addViolation(new MethodBodyLength(method, "more than "+maxLen
          +" statements: "+counter.codeLength));
    }
    
    counter.reset();
    super.visit(method);
  }

  class StatementsCounter extends BinItemVisitor {
    protected int codeLength = 0;
    
    StatementsCounter() {
      
    }

    void reset() {
      this.codeLength = 0;
    }
    
    public void visit(BinStatementList statement) {
      this.codeLength += statement.getStatements().length;
      super.visit(statement);
    }
    
    public void visit(BinMethod method){
      this.codeLength--;
      super.visit(method);
    }

    /*
     * if anyone will ever want to count "catch(...) {", "finally {" and "else{"
     * as a statement in this audit - uncomment following part of code
     */
    /*
     public void visit(BinIfThenElseStatement statement) {
      // count 'else {...' as a line
      if (statement.getFalseList() != null){
        this.codeLength++;
      }
      super.visit(statement);
    }

    public void visit(BinSwitchStatement statement) {
      // count each 'case ...:' as a line
      this.codeLength += statement.getCaseGroupList().length;
      super.visit(statement);
    }

    public void visit(BinTryStatement statement){
      // count each 'catch(...) {...' as a line
      this.codeLength += statement.getCatches().length;
      if (statement.getFinally() != null){
        this.codeLength++;
      }
      super.visit(statement);
    }*/
  }
}

class MethodBodyLength extends AwkwardMember {
  public MethodBodyLength(BinMethod method, String addMsg) {
      super(method, "Method body has " + addMsg, "refact.audit.method_body_length");
  }
}
