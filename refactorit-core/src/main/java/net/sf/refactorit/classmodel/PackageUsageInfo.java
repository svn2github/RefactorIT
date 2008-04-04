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


public class PackageUsageInfo {
  /** needed to be able to restore ASTImpl from int index */
  private CompilationUnit compilationUnit;
  private int node = -1;

  private BinPackage binPackage = null;

  /* Indicates if the last component of this package is an identifier node.
   * It's <CODE>false</CODE> only for package-definitions (Example: <PRE>package test;</PRE>),
   * otherwise (import statements and fully qualified type names) it is <CODE>true</CODE>
   */
//  private boolean identifier = true;

  public PackageUsageInfo(final ASTImpl node,
      final BinPackage binPackage, final boolean identifier,
      final CompilationUnit compilationUnit) {
    setNode(node);
    this.compilationUnit = compilationUnit;
    this.binPackage = binPackage;
  }

  public final BinPackage getBinPackage() {
    return this.binPackage;
  }

  public ASTImpl getNode() {
    return compilationUnit.getSource().getASTByIndex(node);
  }

  public void setNode(final ASTImpl node) {
    this.node = ASTUtil.indexFor(node);
  }

  public final CompilationUnit getCompilationUnit() {
    return this.compilationUnit;
  }

  public final void clean() {
    this.compilationUnit = null;
    this.binPackage = null;
  }

  public final String toString() {
    String name = this.getClass().getName();
    name = name.substring(name.lastIndexOf('.') + 1) + ": ";

    if (getBinPackage() != null) {
      name += getBinPackage().toString();
    } else {
      name += "null";
    }

    return name;
  }
}
