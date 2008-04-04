/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.classmodel.expressions;

import net.sf.refactorit.classmodel.BinExpressionList;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinSourceConstruct;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.statements.BinExpressionStatement;
import net.sf.refactorit.classmodel.statements.BinReturnStatement;
import net.sf.refactorit.common.util.ClassUtil;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.query.AbstractIndexer;
import net.sf.refactorit.source.format.BinExpressionFormatter;
import net.sf.refactorit.source.format.BinItemFormatter;



/**
 * Base class for Bin???Expression classes
 */
public abstract class BinExpression extends BinSourceConstruct {

  public BinExpression(ASTImpl rootAst) {
    super(rootAst);
  }

  public abstract BinTypeRef getReturnType();

  public void accept(net.sf.refactorit.query.BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public String toString() {
    return ClassUtil.getShortClassName(this) + ": \""
        + (getReturnType() == null ? "unknown"
        : getReturnType().getQualifiedName()) + "\", "
        + getCompilationUnit() + ", "
        + getStartLine() + ":" + getStartColumn() + " - "
        + getEndLine() + ":" + getEndColumn() + ", "
        + Integer.toHexString(hashCode());
  }

  public final boolean isChangingAnything() {
    final class ChangesAnalyzer extends AbstractIndexer {
      public boolean isChanging = false;

      public final void visit(BinMethodInvocationExpression x) {
        this.isChanging = true;
        // intentionally not doing super.visit(x);
      }

      public final void visit(BinNewExpression x) {
        this.isChanging = true;
        // intentionally not doing super.visit(x);
      }

      public final void visit(BinAssignmentExpression x) {
        this.isChanging = true;
        // intentionally not doing super.visit(x);
      }

      public final void visit(BinIncDecExpression x) {
        this.isChanging = true;
        // intentionally not doing super.visit(x);
      }
    };
    ChangesAnalyzer visitor = new ChangesAnalyzer();
    this.accept(visitor);

    return visitor.isChanging;
  }

  /**
   * This is for ui only to get table to know it's bin position and to highlight.
   */
  public ASTImpl getClickableNode() {
    return this.getRootAst();
  }

  /**
   * @param parent construct
   * @return true if this expression needs brackets around when inserted into
   * given parent
   */
  public final boolean isNeedBrackets(final BinItemVisitable parent) {
    return!(parent instanceof BinExpressionStatement)
        && !(parent instanceof BinExpressionList)
        && !(parent instanceof BinAssignmentExpression)
        && !(parent instanceof BinReturnStatement)
        && !(parent instanceof BinLocalVariable);
  }

  /** Override for every specific expression */
  public BinItemFormatter getFormatter() {
    return new BinExpressionFormatter(this);
  }

}
