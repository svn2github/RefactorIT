/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.usage;

import net.sf.refactorit.classmodel.statements.BinBreakStatement;
import net.sf.refactorit.classmodel.statements.BinLabeledStatement;
import net.sf.refactorit.parser.ASTImpl;


public class LabelIndexer extends TargetIndexer {
  public LabelIndexer(final ManagingIndexer supervisor,
      final BinLabeledStatement target) {
    super(supervisor, target);
  }

  public void visit(final BinBreakStatement statement) {
    if(getTarget().equals(statement.getBreakTarget())) {
      addInvocation(statement);
    }
  }

  private void addInvocation(final BinBreakStatement statement) {
    getSupervisor().addInvocation(
        getTarget(),
        getSupervisor().getCurrentLocation(),
        (ASTImpl)statement.getRootAst().getFirstChild(),
        statement);
  }
}
