/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.classmodel.statements;

import net.sf.refactorit.parser.ASTImpl;


public final class BinBreakStatement extends BinStatement {

  public BinBreakStatement(String identifier, boolean isBreakStatement,
      BinStatement breakTarget, ASTImpl rootAst) {
    super(rootAst);
    this.identifier = identifier;
    this.breakTarget = breakTarget;
    this.isBreakStatement = isBreakStatement;
  }

  public final String getIdentifier() {
    return identifier;
  }

  public final boolean isBreakStatement() {
    return isBreakStatement;
  }

  public final BinStatement getBreakTarget() {
    return breakTarget;
  }

  public final void accept(net.sf.refactorit.query.BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public final void defaultTraverse(net.sf.refactorit.query.BinItemVisitor visitor) {

  }

  public final String toString() {
    return super.toString() + " Breaks: " + breakTarget;
  }

  private final String identifier;
  private final boolean isBreakStatement;
  private final BinStatement breakTarget;
}
