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
import net.sf.refactorit.classmodel.statements.BinReturnStatement;

public class BinReturnStatementFormatter extends BinItemFormatter {
  
  private BinReturnStatement statement;
  
  public BinReturnStatementFormatter(BinReturnStatement returnStatement) {
    this.statement = returnStatement;
  }
  
  public String print() {
    StringBuffer buffer = new StringBuffer();
    if(statement.getRootAst() == null) {
      buffer.append("return");
      BinExpression expression = statement.getReturnExpression();
      if(expression != null) {
        buffer.append(' ').append(expression.getFormatter().print());
      }
    } else {
      buffer.append(statement.getText());
    }
    buffer.append(';');
    return buffer.toString();
  }

}
