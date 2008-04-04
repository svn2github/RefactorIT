/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel;

public final class BinVariableArityParameter extends BinParameter {

  public BinVariableArityParameter(String name, BinTypeRef typeRef, int modifiers) {
    super(name, typeRef, modifiers);
  }

  public boolean isVariableArity() {
    return true;
  }

}
