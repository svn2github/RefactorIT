/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel.statements;


import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.query.BinItemVisitor;

import java.util.Arrays;


/** @author  RISTO A */
public class BinVariableDeclaration extends BinStatement {
  private final BinVariable[] variables;

  public BinVariableDeclaration(BinVariable[] variables, ASTImpl node) {
    super(node);

    this.variables = variables;
  }

  public final boolean contains(BinVariable variable) {
    return Arrays.asList(variables).contains(variable);
  }

  public final BinVariable[] getVariables() {
    return variables;
  }

  public final boolean isFirst(BinVariable variable) {
    return variable == variables[0];
  }

  public final boolean isLast(BinVariable variable) {
    return variable == variables[variables.length - 1];
  }

  public final void defaultTraverse(final BinItemVisitor visitor) {
    for (int i = 0; i < variables.length; i++) {
      variables[i].accept(visitor);
    }
  }
}
