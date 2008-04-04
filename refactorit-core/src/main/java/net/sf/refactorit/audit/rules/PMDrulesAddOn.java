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
import net.sf.refactorit.audit.SimpleViolation;
import net.sf.refactorit.audit.pmd.InterfaceToPMD;
import net.sf.refactorit.audit.pmd.ReportContainer;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.ASTUtil;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleSet;

import java.util.List;

/**
 * This audit, is interface for RefactorIT to PMD. Run initialize() to get
 * available rulesets from PMD (they will be stored in allRuleSet field).
 * Add any PMD rules to ruleSetToCheck set, that You want to run on Your 
 * project.
 *
 * @author Kirill Buhhalko
 */
public class PMDrulesAddOn extends AuditRule{
  public static final String NAME = "PMD_";

  // RuleSetToCheck - rules which will be appy when audit start
  private RuleSet ruleSetToCheck;
  
  public PMDrulesAddOn(){
    ruleSetToCheck = new RuleSet();
    setName("PMD rules");
    setPriority(Priority.NORMAL);
  }

  public void visit(CompilationUnit sf) {

    //- file, to check, must be a full path to file
    //ex.: "/home/user/project/SomeClass.java"
    String fileToCheck="";

    InterfaceToPMD pmd = new InterfaceToPMD();

    fileToCheck = sf.getSource().getAbsolutePath();

    // processing... file
    pmd.processFile(fileToCheck, ruleSetToCheck);

    List results = pmd.getResults();

    for(int i = 0, max_i = results.size(); i < max_i; i++) {
      ReportContainer rc = (ReportContainer) results.get(i);

      SimpleViolation sv = new PMDRuleViolation(sf, getASTImpl(sf,rc.getErrorLine()), rc.getErrorDescription());
      addViolation(sv);
      sv.setTypeAcronym(rc.getType());
    }
    super.visit(sf);
  }

  public class PMDRuleViolation extends SimpleViolation{

    public PMDRuleViolation(CompilationUnit compilationUnit, ASTImpl astImpl,
        String message) {
      super(compilationUnit.getMainType(), astImpl, message, "refact.audit.pmd");
    }
  }

  private static ASTImpl getASTImpl(CompilationUnit compilationUnit, int line) {
    int size;
    size = ASTUtil.getForLine(compilationUnit.getSource().getFirstNode(),
                              line).size();

    if (size == 0) {
      // return the place first possible node // need fix
      return  compilationUnit.getSource().getFirstNode();
    }
    else {
      // return needed place by line
      return (ASTImpl) ASTUtil.getForLine(
          compilationUnit.getSource().getFirstNode(), line).get(0);
    }
  }


  public boolean hasRulesToCheck() {
    return ruleSetToCheck.size() > 0;
  }
  
  public void addRuleToCheck(Rule rule){
    ruleSetToCheck.addRule(rule);
  }
}
