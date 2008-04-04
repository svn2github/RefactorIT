/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.format;

import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.LocationAware;


public class BinLocalVariableFormatter extends BinVariableFormatter {
  public BinLocalVariableFormatter(BinLocalVariable var) {
    super(var);
  }
  
  public String print() {
    return var.getName();
  }

  public String formFooter() {
    if (var instanceof BinParameter) {
      return "";
    }

    return ";" + FormatSettings.LINEBREAK;
  }

  protected String formModifiers() {
    return "";
  }

  protected LocationAware getNewParent() {
    LocationAware parent = (LocationAware)this.var.getParent();
    // hack to skip synthetic constructs
    while (parent!=null && parent.getStartLine() == -1) {
      parent = (LocationAware) ((BinItem) parent).getParent();
    }

    return parent;
  }
}
