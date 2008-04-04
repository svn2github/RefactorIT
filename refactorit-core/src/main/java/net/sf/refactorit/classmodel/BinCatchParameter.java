/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel;

import net.sf.refactorit.classmodel.references.BinCatchParameterReference;
import net.sf.refactorit.classmodel.references.BinItemReference;
import net.sf.refactorit.classmodel.statements.BinTryStatement;


public final class BinCatchParameter extends BinParameter {

  public BinCatchParameter(String name, BinTypeRef typeRef,
      int modifiers) {
    super(name, typeRef, modifiers);
  }

  public void accept(net.sf.refactorit.query.BinItemVisitor visitor) {
    visitor.visit(this);
  }

  // FIXME getParent should work now!
  public void setCatchClause(BinTryStatement.CatchClause catchClause) {
    this.catchClause = catchClause;
  }

  public BinTryStatement.CatchClause getCatchClause() {
    return this.catchClause;
  }

  public String getMemberType() {
    return memberType;
  }

  /** Overrides */
  public BinMethod getMethod() {
    return null;
  }

  public BinItemReference createReference() {
    return new BinCatchParameterReference(this);
  }

  private static final String memberType = "catch parameter";

  private BinTryStatement.CatchClause catchClause;
}
