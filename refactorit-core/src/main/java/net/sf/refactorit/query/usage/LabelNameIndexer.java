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
import net.sf.refactorit.classmodel.statements.BinStatement;
import net.sf.refactorit.parser.ASTImpl;


public class LabelNameIndexer extends TargetIndexer {
  public LabelNameIndexer(final ManagingIndexer supervisor, 
      final BinLabeledStatement statement) {
    super(supervisor, statement);
    setSearchForNames(true);
  }
  
  public final void visit(final BinBreakStatement st) {
    if (isTarget(st.getBreakTarget())) {
      getSupervisor().addInvocation(
          getTarget(),
          getSupervisor().getCurrentLocation(),
          (ASTImpl)st.getRootAst().getFirstChild());
    }
  }

  public final void visit(final BinLabeledStatement st) {
    if (isTarget(st)) {
      getSupervisor().addInvocation(
          getTarget(),
          getSupervisor().getCurrentLocation(),
          (ASTImpl)st.getRootAst().getFirstChild());
    }
  }
  
  private boolean isTarget(BinStatement st) {
    return getTarget().equals(st);
  }
}
