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
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.source.format.BinEmptyExpressionFormatter;
import net.sf.refactorit.source.format.BinItemFormatter;


public final class BinEmptyExpression extends BinExpression {

  /**
   * When typing Object o = new Object[5][][];
   * then last 2 '[]''s are empty expressions
   **/
  public BinEmptyExpression() {
    super(null);
  }

  // FIXME: maybe need better type here.
  public final BinTypeRef getReturnType() {
    return null;
  }

  public final void accept(net.sf.refactorit.query.BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public final void defaultTraverse(net.sf.refactorit.query.BinItemVisitor visitor) {

  }

  public final boolean isSame(BinItem other) {
    if (!(other instanceof BinEmptyExpression)) {
      return false;
    }
    return true;
  }
  
  public final BinItemFormatter getFormatter() {
    return new BinEmptyExpressionFormatter(this);
  }

}
