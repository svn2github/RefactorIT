/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel.expressions;

import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.source.format.BinItemFormatter;
import net.sf.refactorit.source.format.BinLocalVariableUseExpressionFormatter;


/**
 * Actually should be BinLocalVariableUseExpression, should be renamed,
 * but don't want to loose version history.
 */
public class BinVariableUseExpression extends BinExpression {
  public BinVariableUseExpression(final BinLocalVariable variable,
      final ASTImpl rootAst) {
    super(rootAst);

    this.variable = variable;
  }

  public final BinTypeRef getReturnType() {
    return variable.getTypeRef();
  }

  public final BinLocalVariable getVariable() {
    return variable;
  }

  public final void accept(final BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public final void defaultTraverse(final BinItemVisitor visitor) {
  }

  public final void clean() {
    variable = null;
    super.clean();
  }

  public final ASTImpl getNameAst() {
    return getRootAst();
  }

  public final ASTImpl getClickableNode() {
    return getNameAst();
  }

  public final boolean isSame(final BinItem other) {
    if (!(other instanceof BinVariableUseExpression)) {
      return false;
    }

    return this.variable.isSame(((BinVariableUseExpression) other).variable);
  }

  public final BinItemFormatter getFormatter() {
    return new BinLocalVariableUseExpressionFormatter(this);
  }

  private BinLocalVariable variable;
}
