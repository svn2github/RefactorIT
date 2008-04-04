/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.audit;

import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;


/**
 *
 *
 * @author Igor Malinin
 */
public class AuditTreeTableNode extends BinTreeTableNode {
  private final RuleViolation ruleViolation;
  private final AuditTreeTableModel model;

  public AuditTreeTableNode(
      final RuleViolation ruleViolation, final AuditTreeTableModel model
      ) {
    super(ruleViolation.getMessage(), true);

    this.ruleViolation = ruleViolation;
    this.model = model;

    setSourceHolder(ruleViolation.getCompilationUnit());
    setLine(ruleViolation.getLine());
    addAst(ruleViolation.getAst());
    setHidden(ruleViolation.isSkipped());
  }

  public RuleViolation getRuleViolation() {
    return this.ruleViolation;
  }

  /**
   * The idea is that this method will be called every time
   * a node is inserted or removed from the tree. (BTW we are assuming
   * here that it is impossible to change a priority of an existing node.)
   */
  protected void invalidateCacheOfVisibleChildren() {
    super.invalidateCacheOfVisibleChildren();
    if (model != null){
      model.notifyNodesAddedOrRemoved();
    }
  }

  public String getIdentifier() {
    String result = super.getIdentifier();
    if (ruleViolation.isSkipped()) {
      result += "(skipped)";
    }
    return result;
  }
}
