/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.format;

import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.statements.BinLocalVariableDeclaration;

public class BinLocalVariableDeclarationFormatter extends BinItemFormatter {

  private BinLocalVariableDeclaration binVarDeclaration;

  public BinLocalVariableDeclarationFormatter(
          BinLocalVariableDeclaration binVarDeclaration) {
    this.binVarDeclaration = binVarDeclaration;
  }

  public String print() {
    if (binVarDeclaration.getRootAst() != null) {
      return binVarDeclaration.getText();
    } else {
      StringBuffer sb = new StringBuffer();
      BinLocalVariable[] vars = (BinLocalVariable[]) binVarDeclaration
              .getVariables();
      BinLocalVariableFormatter firstVarFormatter = (BinLocalVariableFormatter) vars[0]
              .getFormatter();
      sb.append(FormatSettings.getIndentStringForChildrenOf(firstVarFormatter
              .getNewParent()));
      sb.append(firstVarFormatter.formModifiers());
      sb.append(formatTypeName(vars[0].getTypeRef()));
      sb.append(" ");
      for (int i = 0; i < vars.length; i++) {
        sb.append(vars[i].getFormatter().print());
        BinLocalVariableFormatter varFormatter = (BinLocalVariableFormatter) vars[i]
                .getFormatter();
        sb.append(varFormatter.formBody());
        sb.append(',');
      }
      if (vars.length > 0) {
        sb.setLength(sb.length() - 1);
      }
      sb.append(';');
      sb.append(FormatSettings.LINEBREAK);
      return sb.toString();
    }
  }

}
