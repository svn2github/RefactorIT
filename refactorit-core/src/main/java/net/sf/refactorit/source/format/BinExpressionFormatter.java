/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.format;

import net.sf.refactorit.classmodel.expressions.BinExpression;



// FIXME: implement a common base class
public class BinExpressionFormatter extends BinItemFormatter {
  protected BinExpression exp;

  public BinExpressionFormatter(BinExpression exp) {
    this.exp = exp;
  }

  public String print() {
    if (exp.getRootAst() != null) {
      return exp.getCompilationUnit().getContent()
          .substring(exp.getStartPosition(), exp.getEndPosition()).trim();
    } else {
      throw new UnsupportedOperationException("Not yet implemented");
    }
  }

}
