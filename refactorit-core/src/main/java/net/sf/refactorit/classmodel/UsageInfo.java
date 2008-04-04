/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel;

import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.ASTUtil;


public abstract class UsageInfo {
  private int node = -1;
  private CompilationUnit compilationUnit; // needed to be able to restore AST from index

  public UsageInfo(final ASTImpl node, final CompilationUnit compilationUnit) {
//    if (Assert.enabled) {
//      if (compilationUnit == null) {
//        Assert.must(false,
//            "Source file = null when creating node " + node);
//      }
//      if (((TreeASTImpl) node).backRef != null
//          && compilationUnit.getSource().getASTTree()
//          != ((TreeASTImpl) node).backRef.ownerTree) {
//        Assert.must(false,
//            "Constructing UsageInfo with wrong source file! :"
//            + compilationUnit.getDisplayPath());
//      }
//    }

    setNode(node);
    this.compilationUnit = compilationUnit;
  }

  public final ASTImpl getNode() {
    return compilationUnit.getSource().getASTByIndex(node);
  }

  /** Note: overriden */
  public final void setNode(final ASTImpl node) {
    this.node = ASTUtil.indexFor(node);
  }

  public final CompilationUnit getCompilationUnit() {
    return this.compilationUnit;
  }

  public final UsageInfo addChild(final UsageInfo child) {
    return child;
  }

  public final UsageInfo getChild() {
    return null;
  }

  public final void clean() {
    this.compilationUnit = null;
  }
}
