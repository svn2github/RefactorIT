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


/**
 * @author Anton Safonov
 */
public class BinSourceTypeRef extends BinGenericTypeRef {
  /** needed to be able to restore ASTImpl from int index */
  private final CompilationUnit compilationUnit;
  private int node = -1;

  protected BinSourceTypeRef(final CompilationUnit compilationUnit,
      final ASTImpl node, final BinTypeRef typeRef) {
    super(typeRef);

//    if (Assert.enabled) {
//      if (typeRef != null
//          && !typeRef.isPrimitiveType()
//          && !typeRef.isArray()
//          && node.getType() != JavaTokenTypes.IDENT) {
//
//        // Launch ASTFrame - you *just* can't not to notice that :P
//        (new rantlr.debug.misc.ASTFrame((typeRef != null ? "@"
//            + typeRef.getQualifiedName() : "") + node.toStringTree(),
//            node)).setVisible(true);
//      }
//    }

    setNode(node);
    this.compilationUnit = compilationUnit;
  }

  public ASTImpl getNode() {
    if (getCompilationUnit() != null && getNodeIndex() >= 0) {
      return getCompilationUnit().getSource().getASTByIndex(getNodeIndex());
    } else {
      return null;
    }
  }

  /** Note: overriden */
  public void setNode(final ASTImpl node) {
    this.node = ASTUtil.indexFor(node);
  }

  public final int getNodeIndex() {
    return this.node;
  }

  public final CompilationUnit getCompilationUnit() {
    return this.compilationUnit;
  }
}
