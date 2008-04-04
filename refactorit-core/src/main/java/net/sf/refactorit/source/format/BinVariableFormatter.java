/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.format;

import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.LocationAware;


/**
 *
 * @author  RISTO A
 */
public abstract class BinVariableFormatter extends BinItemFormatter {
  protected BinVariable var;

  public BinVariableFormatter(BinVariable var) {
    this.var = var;
  }

  public String formHeader() {
    return FormatSettings.getIndentStringForChildrenOf(getNewParent()) +
        formModifiers() + formatTypeName(var.getTypeRef()) + " " + var.getName();
  }

  public String formBody() {
    if (var.getExpression() == null) {
      return "";
    }
    StringBuffer sb=new StringBuffer();
    sb.append(new BinAssigmentExpressionFormatter().print());
    sb.append(var.getExpression().getFormatter().print());
    return sb.toString();
  }

  protected abstract String formModifiers();

  protected abstract LocationAware getNewParent();
}
