/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.extractmethod;

import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.source.format.BinFormatter;


/**
 * Node for the Parameters table.
 *
 * @author Vladislv Vislogubov
 */
public class ParametersTableNode {
  private BinVariable var = null;
  private String name = null;
  /**
   * Constructor for ParametersTableNode.
   */
  public ParametersTableNode(BinVariable var) {
    if (var == null) {
      return;
    }

    this.var = var;
    this.name = var.getName();
  }

  public String getParamName() {
    return name;
  }

  public String getParamTypeName() {
    return (var != null) ? BinFormatter.formatNotQualifiedForTypeArgumentsWithAllOwners(var.getTypeRef()).replace('$', '.') : null;
  }

  public String getOriginalParamName() {
    return var.getName();
  }

  public void setParamName(String name) {
    this.name = name;
  }

  public BinVariable getOriginalParam() {
    return this.var;
  }

  public String toString() {
    return getParamTypeName() + "   " + name;
  }
}
