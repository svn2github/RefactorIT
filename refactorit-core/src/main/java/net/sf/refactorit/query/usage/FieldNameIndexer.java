/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.usage;

import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;


public final class FieldNameIndexer extends FieldIndexer {

  private boolean renameInJavadocs = false;

  public FieldNameIndexer(final ManagingIndexer supervisor,
      final BinField target,
      final boolean renameInJavadocs) {
    super(supervisor, target, true);

    this.renameInJavadocs = renameInJavadocs;

    setSearchForNames(true);
  }

  public final void visit(final CompilationUnit source) {
    if (renameInJavadocs) {
      source.visit(getSupervisor());
    }
  }

  public final void visit(final BinField field) {
    if (isTargetField(field)) {
      getSupervisor().addInvocation(
          getTarget(),
          getSupervisor().getCurrentLocation(),
          field.getNameAstOrNull());
      getSupervisor().addAffectedMember(field);
    }
  }

  public final void visit(final BinFieldInvocationExpression expression) {
    if (isTargetField(expression.getField())) {
      getSupervisor().addInvocation(
          getTarget(),
          getSupervisor().getCurrentLocation(),
          expression.getNameAst(),
					expression);
    }
  }

  private boolean isTargetField(final BinField field) {
    return field == getTarget();
  }

}
