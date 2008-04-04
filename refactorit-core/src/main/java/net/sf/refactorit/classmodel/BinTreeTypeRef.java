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


/**
 * @author Anton Safonov
 */
public class BinTreeTypeRef extends BinSourceTypeRef {
  private BinSpecificTypeRef child = null;

  protected BinTreeTypeRef(final CompilationUnit compilationUnit,
      final ASTImpl node, final BinTypeRef typeRef) {
    super(compilationUnit, node, typeRef);

    /*    if(net.sf.refactorit.common.util.Assert.enabled) {
          if( typeRef != null
              && !typeRef.isPrimitiveType()
              && !typeRef.isArray()
              && node.getType() != JavaTokenTypes.IDENT){

            // Launch ASTFrame - you *just* can't not to notice that :P
            (new rantlr.debug.misc.ASTFrame((typeRef != null ? "@" + typeRef.getQualifiedName() : "") + node.toStringTree(), node)).setVisible(true);
          }
        }*/
  }

  public final BinTypeRef addChild(final BinSpecificTypeRef child) {
    this.child = child;
    return this.child;
  }

  public final BinSpecificTypeRef getChild() {
    return this.child;
  }

  public void traverse(final BinTypeRefVisitor visitor) {
    super.traverse(visitor);

    BinTypeRef achild = getChild();
    if (achild != null) {
      achild.accept(visitor);
    }
  }
}
