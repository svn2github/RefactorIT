/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.extract;


import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.classmodel.statements.BinTryStatement;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.ui.module.IdeWindowContext;

import java.util.List;


public class RenamingVariableUseAnalyzer extends VariableUseAnalyzer {
  private MultiValueMap usageMap;

  public RenamingVariableUseAnalyzer(
      IdeWindowContext context, BinMember rangeMember, List constructs
  ) {
    super(context, rangeMember, constructs);
  }

  public void initVars() {
    super.initVars();

    this.usageMap = new MultiValueMap();
  }

  public void visit(BinLocalVariable x) {
    if (isInside()) {
      this.usageMap.putAll(x, x.getNameAstOrNull());
    }

    super.visit(x);
  }

  public void visit(BinVariableUseExpression x) {
    if (isInside()) {
      this.usageMap.putAll(x.getVariable(), x.getNameAst());
    }

    super.visit(x);
  }

  public void visit(BinTryStatement.CatchClause x) {
    if (isInside()) {
      this.usageMap.putAll(x.getParameter(), x.getParameter().getNameAstOrNull());
    }

    super.visit(x);
  }

  public MultiValueMap getUsageMap() {
    return this.usageMap;
  }
}
