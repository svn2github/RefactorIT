/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.format;

import net.sf.refactorit.classmodel.BinParameter;


public class BinParameterFormatter extends BinItemFormatter {
  BinParameter param;
  public BinParameterFormatter(BinParameter param) {
    this.param = param;
  }
  
  public String print() {
    int modifier = param.getModifiers();
    BinModifierFormatter modifierFormatter = new BinModifierFormatter(modifier);
    modifierFormatter.needsPostfix(true);
    String modifiers = modifierFormatter.print();
    return modifiers + formatTypeName(param.getTypeRef()) + " " + param.getName();
  }

}
