/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.classmodel.statements;

import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.parser.ASTImpl;


public final class BinEmptyStatement extends BinStatement {

  public BinEmptyStatement(ASTImpl rootAst) {
    super(rootAst);
  }

  public final void accept(net.sf.refactorit.query.BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public final void defaultTraverse(net.sf.refactorit.query.BinItemVisitor visitor) {

  }

  public final boolean isSame(BinItem other) {
    if (!(other instanceof BinEmptyStatement)) {
      return false;
    }

    return true;
  }
}
