/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel;


import net.sf.refactorit.classmodel.references.BinItemReference;
import net.sf.refactorit.classmodel.references.BinSourceConstructReference;
import net.sf.refactorit.classmodel.statements.BinStatement;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.ASTUtil;
import net.sf.refactorit.source.edit.CompoundASTImpl;

import java.util.HashMap;


/**
 * Base class for all source constructs like expressions and statements.
 */
public abstract class BinSourceConstruct extends AbstractLocationAware implements
    SourceConstruct {

  private static final HashMap compoundsCache = new HashMap(1024);

  //private ASTImpl rootAst;
  private int rootAst = -1;

  /**
   * really should not be called - just for where used
   */
  public BinSourceConstruct() {
  }

  public BinSourceConstruct(final int rootAst) {
    this.rootAst = rootAst;
  }

  public BinSourceConstruct(final ASTImpl rootAst) {
    setRootAst(rootAst);
  }

  protected void setRootAst(final ASTImpl rootAst) {
    this.rootAst = ASTUtil.indexFor(rootAst);
  }

  public ASTImpl getRootAst() {
    final CompilationUnit src = getCompilationUnit();
    if (src != null) {
      return src.getSource().getASTByIndex(rootAst);
    }
    return null;
  }

  /**
   * Use this call, when in sourceMethodBodyLoader
   */
  public final ASTImpl getRootAst(final CompilationUnit src) {
    return src.getSource().getASTByIndex(rootAst);
  }

  public final BinTypeRef getOwner() {
    BinMember parentMember = getParentMember();
    if (parentMember != null) {
      if (parentMember instanceof BinCIType) {
        return ((BinCIType) parentMember).getTypeRef();
      } else {
        return parentMember.getOwner();
      }
    } else {
      return null;
    }
  }

  //******************* BASE LOCATION AWARE SUPPORT ********************/

  public CompilationUnit getCompilationUnit() {
    BinMember parentMember = getParentMember();

    return parentMember == null ? null : parentMember.getCompilationUnit();
  }

  public final int getStartLine() {
    final ASTImpl realAst = getCompoundAst();
    if (realAst == null) {
      return -1; // FIXME: this a right thing to do?
    }
    return realAst.getStartLine();
  }

  public final int getStartColumn() {
    final ASTImpl realAst = getCompoundAst();
    if (realAst == null) {
      return -1; // FIXME: this a right thing to do?
    }
    return realAst.getStartColumn();
  }

  public final int getEndLine() {
    final ASTImpl realAst = getCompoundAst();
    if (realAst == null) {
      return -1; // FIXME: this a right thing to do?
    }
    return realAst.getEndLine();
  }

  public final int getEndColumn() {
    final ASTImpl realAst = getCompoundAst();
    if (realAst == null) {
      return -1; // FIXME: this a right thing to do?
    }
    return realAst.getEndColumn();
  }

  public String toString() {
    final String name = this.getClass().getName();
    return name.substring(name.lastIndexOf('.') + 1) + ": "
        + getCompilationUnit() + ", "
        + getStartLine() + ":" + getStartColumn() + " - "
        + getEndLine() + ":" + getEndColumn() + ", "
        + Integer.toHexString(hashCode());
  }

  public void clean() {
    rootAst = -1;
  }

  public final BinStatement getEnclosingStatement() {
    BinItemVisitable scope = this;
    while (scope != null && !(scope instanceof BinStatement)) {
      // the first non-expression must be statement, right?
      // NOTE: there are BinLocalVariables sometimes also, but this doesn't bother us
      scope = scope.getParent();
    }

    return (BinStatement) scope;
  }

  public final ASTImpl getCompoundAst() {
    ASTImpl ast = (ASTImpl) BinSourceConstruct.compoundsCache.get(this);
    if (ast == null) {
      ast = getRootAst();
      if (ast != null) {
        ast = new CompoundASTImpl(ast);
        BinSourceConstruct.compoundsCache.put(this, ast);
      }
    }

    return ast;
  }

  public static final void clearCompoundsCache() {
    BinSourceConstruct.compoundsCache.clear();
  }

  public BinItemReference createReference() {
    return new BinSourceConstructReference(this);
  }
}
