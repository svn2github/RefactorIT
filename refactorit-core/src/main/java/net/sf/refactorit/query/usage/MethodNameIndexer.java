/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.usage;

import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.expressions.BinStringConcatenationExpression;
import net.sf.refactorit.query.usage.filters.BinMethodSearchFilter;


public final class MethodNameIndexer extends MethodIndexer {

  private boolean renameInJavadocs = false;

  public MethodNameIndexer(final ManagingIndexer supervisor,
      final BinMethod target,
      final boolean subtypes, final boolean supertypes,
      final boolean renameInJavadocs) {

    super(supervisor, target,
        new BinMethodSearchFilter(
        true, true, supertypes, subtypes, true, false, false, false, false));
    /*System.err.println("Target: " + target.getClass().getName() + "@"
     + Integer.toHexString(target.hashCode()) + " - " + target.getQualifiedName()
                          + " - " + target.getOwner().getQualifiedName());*/
    setSearchForNames(true);

    this.renameInJavadocs = renameInJavadocs;
  }

  public final void visit(final CompilationUnit source) {
    super.visit(source);
    if (renameInJavadocs) {
      source.visit(getSupervisor());
    }
  }

  /** overrides method {@link MethodIndexer#visit(BinMethod)} */
  public final void visit(final BinMethod method) {
    // usual MethodIndexer of course doesn't find self
    if (method == getTarget()) {
      addInvocation(method);
    } else {
      super.visit(method);
    }
  }

  public final void visit(final BinStringConcatenationExpression expression) {
    // there is no name usage in string concatenation
  }
}
