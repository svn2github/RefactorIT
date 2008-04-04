/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.usage;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.expressions.BinConstructorInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.query.usage.filters.BinClassSearchFilter;


/**
 * Searches for usages of the name of the given {@link BinCIType}.
 *
 * @author Anton Safonov
 */
public final class TypeNameIndexer extends TypeIndexer {

  private boolean renameInJavadocs = false;

  public TypeNameIndexer(final ManagingIndexer supervisor,
      final BinCIType target,
      final boolean renameInJavadocs) {
    this(supervisor, target);
    this.renameInJavadocs = renameInJavadocs;
  }

  public TypeNameIndexer(final ManagingIndexer supervisor,
      final BinCIType target) {
    super(supervisor, target, new BinClassSearchFilter(true, true));
    setSearchForNames(true);

    typeRefVisitor.setCheckTypeSelfDeclaration(true);
    typeRefVisitor.setIncludeNewExpressions(true);
  }

  public final void visit(final CompilationUnit source) {
    super.visit(source);
    if (renameInJavadocs) {
      source.visit(getSupervisor());
    }
  }

  public final void visit(final BinConstructor constructor) {
    // name of the constructor matches class name
    if (constructor.getOwner().equals(getTypeRef())
        && !constructor.isSynthetic()) {
      getSupervisor().addInvocation(
          getTarget(),
          constructor,
          constructor.getNameAstOrNull());
    }
  }

  /** Override - don't need to check for method invocations on instance */
  public final void visit(final BinMethodInvocationExpression x) {
    // type name usage in reflection
    /*    if ("java.lang.Class".equals(invokedOn.getQualifiedName())) {
          if ("forName".equals(method.getName())) {
            BinExpression[] exprs = x.getExpressionList().getExpressions();
            for (int i = 0; i < exprs.length; i++) {
     System.err.println("forName: " + exprs[i].getReturnType().getQualifiedName());
            }
          }
        }*/

    // type arguments: String in case - A.<String>method("aaa");
    typeRefVisitor.init(null, x, null);
    x.accept(typeRefVisitor);
  }

  public void visit(final BinConstructorInvocationExpression x) {
    // type arguments: String in case - <String> this("aaa");
    typeRefVisitor.init(null, x, null);
    x.accept(typeRefVisitor);
  }

  /** Override - we don't need to scan for member invocations on instance
   * since there is no class name occured
   */
  protected final void registerMemberDelegates() {
  }

}
