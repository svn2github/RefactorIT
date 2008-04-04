/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel.statements;

import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.query.BinItemVisitor;


/** @author  RISTO A */
public final class BinFieldDeclaration extends BinVariableDeclaration {

  public static final BinFieldDeclaration[] NO_FIELDDECLARATIONS
      = new BinFieldDeclaration[0];  

  /** Creates a new instance of BinFieldDeclaration */
  public BinFieldDeclaration(BinField[] fields, ASTImpl rootNode) {
    super(fields, rootNode);
  }

  public final void accept(BinItemVisitor visitor) {
    visitor.visit(this);
  }

  // small hack
  public final void setParent(final BinItemVisitable parent) {
    super.setParent(parent);

    BinVariable[] vars = getVariables();
    for (int i = 0; i < vars.length; i++) {
      vars[i].setParent(this);
    }
  }
  
  public BinMember getParentMember(){
    // HACK: returns owner class as parentMember, but class isn`t actually
    //    a member.
    return ((BinMember) getParent());
  }
}
