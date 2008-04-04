/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.classmodel.statements;

import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.parser.ASTImpl;


public final class BinCITypesDefStatement extends BinStatement {

  public BinCITypesDefStatement(BinTypeRef typeRef, ASTImpl node) {
    super(node);
    this.typeRef = typeRef;
  }

  public final BinTypeRef getTypeRef() {
    return typeRef;
  }

  public final void accept(net.sf.refactorit.query.BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public final void defaultTraverse(net.sf.refactorit.query.BinItemVisitor visitor) {
    typeRef.getBinCIType().accept(visitor);
  }

  /** This is intended for debug only */
  public final void clean() {
    if (typeRef != null) { // just to be sure
      typeRef.cleanUp();
    }
    typeRef = null;
    super.clean();
  }

  private BinTypeRef typeRef;
}
