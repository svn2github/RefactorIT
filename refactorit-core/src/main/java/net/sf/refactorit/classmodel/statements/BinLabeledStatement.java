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


/**
 *		class BinLabeledStatement
 *		Purpose :	Defines class for label statement
 */

public final class BinLabeledStatement extends BinStatement {

  public BinLabeledStatement(String identifier, ASTImpl node) {
    super(node);
    this.identifier = identifier;
  }

  public final String getLabelIdentifierName() {
    return identifier;
  }

  public final BinStatementList getLabelStatementList() {
    return statementList;
  }

  public final void setLabelStatementList(BinStatementList slist) {
    statementList = slist;
  }

  public final void accept(net.sf.refactorit.query.BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public final void defaultTraverse(net.sf.refactorit.query.BinItemVisitor visitor) {
    statementList.accept(visitor);
  }

  public final void clean() {
    statementList.clean();
    statementList = null;
    super.clean();
  }

  private final String identifier;
  private BinStatementList statementList;
}
