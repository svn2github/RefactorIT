/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.format;

import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.LocationAware;


public class BinFieldFormatter extends BinVariableFormatter {
  public BinFieldFormatter(BinField field) {
    super(field);
  }

  public String formFooter() {
    return ";" + getLinebreaksAfterDeclaration();
  }

  private String getLinebreaksAfterDeclaration() {
    if (var.getOwner().getBinCIType().getDeclaredFields().length > 0) {
      return FormatSettings.LINEBREAK;
    }

    return FormatSettings.LINEBREAK + FormatSettings.LINEBREAK;
  }

  protected String formModifiers() {
    BinModifierFormatter formatter = new BinModifierFormatter(var.getModifiers());
    formatter.needsPostfix(true);
    return formatter.print();
  }

  protected LocationAware getNewParent() {
    return var.getOwner().getBinCIType();
  }

  /* (non-Javadoc)
   * @see net.sf.refactorit.source.format.BinItemFormatter#print()
   */
  public String print() {
    // TODO Auto-generated method stub
    return null;
  }
}
