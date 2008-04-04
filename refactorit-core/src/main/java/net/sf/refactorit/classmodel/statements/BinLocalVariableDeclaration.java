/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.classmodel.statements;

import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.source.format.BinItemFormatter;
import net.sf.refactorit.source.format.BinLocalVariableDeclarationFormatter;


/**
 *
 * @author  RISTO A
 */
public final class BinLocalVariableDeclaration extends BinVariableDeclaration {
  public BinLocalVariableDeclaration(BinLocalVariable[] vars, ASTImpl node) {
    super(vars, node);
  }

  public final void accept(final net.sf.refactorit.query.BinItemVisitor
      visitor) {
    visitor.visit(this);
  }

  public BinItemFormatter getFormatter() {
    return new BinLocalVariableDeclarationFormatter(this);
  }
}
