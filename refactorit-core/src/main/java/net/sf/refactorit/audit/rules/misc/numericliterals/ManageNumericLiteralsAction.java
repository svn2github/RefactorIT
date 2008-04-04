/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.misc.numericliterals;


import net.sf.refactorit.audit.Audit;
import net.sf.refactorit.audit.AuditRule;
import net.sf.refactorit.audit.CorrectiveAction;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.query.DelegatingVisitor;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.audit.numericliterals.NumLitDialog;
import net.sf.refactorit.ui.module.TreeRefactorItContext;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class ManageNumericLiteralsAction extends CorrectiveAction {
  public static final ManageNumericLiteralsAction instance
      = new ManageNumericLiteralsAction();

  public String getKey() {
    return "refactorit.audit.action.create_constants";
  }

  public String getName() {
    return "Manage constants";
  }
	
	public boolean isMultiTargetsSupported() {
		return false;
	}

  public Set run(TreeRefactorItContext context, List violations) {
    RuleViolation violation = (RuleViolation) violations.get(0);
    if (!(violation instanceof NumericLiteral)) {
      return Collections.EMPTY_SET; // foreign violation - do nothing
    }
    NumericLiteral numViolation = (NumericLiteral) violation;

    AuditRule rule =(new Audit(NumericLiteralsRule.class)).createAuditingRule();
    rule.init();
    rule.setConfiguration(numViolation.getConfiguration());
    ((NumericLiteralsRule) rule).setLiteralToFind(numViolation.getLiteral());
    ((NumericLiteralsRule) rule).setTraverseInnerClasses(false);
    rule.clearViolations();
    DelegatingVisitor supervisor = new DelegatingVisitor();
    supervisor.registerDelegate(rule);
    
    BinMember owner = numViolation.getOwnerMember();
    if(!(owner instanceof BinCIType)) {
      owner = owner.getParentType();
    }
    owner.accept(supervisor);

    final List revisitedViolations = rule.getViolations();
    NumLitDialog dialog = new NumLitDialog(context, revisitedViolations);
    dialog.setLiteral(numViolation.getLiteral());
    dialog.show();

    if (dialog.isButtonOKPressed()) {
      TransformationManager manager = new TransformationManager(null);
      for (Iterator it = revisitedViolations.iterator(); it.hasNext(); ) {
        NumericLiteral curViolation = (NumericLiteral) it.next();
        if (curViolation.hasFix()) {
          manager.add(curViolation.getFix().getTransformationList());
        }
      }

      manager.performTransformations();
      clearFixes(revisitedViolations);
      return Collections.singleton(numViolation.getCompilationUnit());
    }

    clearFixes(revisitedViolations);
    return Collections.EMPTY_SET;
  }

  private void clearFixes(final List revisitedViolations) {
    for (Iterator it = revisitedViolations.iterator(); it.hasNext(); ){
      ((NumericLiteral) it.next()).clearFix();
    }
  }
}
