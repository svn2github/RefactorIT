/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.usage;

import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinSourceConstruct;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.parser.ASTImpl;


/**
 * @author Anton Safonov
 */
public class LocalVariableIndexer extends TargetIndexer {

  public LocalVariableIndexer(final ManagingIndexer supervisor,
      final BinLocalVariable target) {
    super(supervisor, target, null);
  }

  public final void visit(final BinVariableUseExpression expression) {
    checkVariable(expression.getVariable(), expression.getNameAst(), expression);
  }

  protected final void checkVariable(final BinLocalVariable variable,
      final ASTImpl node,
      final BinSourceConstruct expression) {
    if (getTarget() == variable) {
      getSupervisor().addInvocation(
          variable,
          getSupervisor().getCurrentLocation(),
          node,
          expression
          );
    }
  }
}
