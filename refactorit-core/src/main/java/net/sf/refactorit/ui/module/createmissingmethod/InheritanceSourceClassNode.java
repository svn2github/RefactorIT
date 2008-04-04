/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.createmissingmethod;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.ui.tree.InheritanceNode;
import net.sf.refactorit.ui.tree.UITreeNode;

import java.util.List;


/**
 * Used to create a tree of a class node and all it's superclasses that are
 * on the sourcepath.
 */
class InheritanceSourceClassNode extends InheritanceNode {
  protected InheritanceSourceClassNode(UITreeNode parent, BinCIType bin) {
    super(parent, bin);
  }

  /**
   * Initializes the list with the direct subTypes of this.bin object.
   *
   * It is overridden function in its base class.
   * This method is called by the method in base class.
   *
   * @param list to be filled with direct subTypes of this.bin object.
   * @see InheritanceNode#getMembers()
   */
  protected void initInheritance(List list) {
    BinTypeRef superClass = bin.getTypeRef().getSuperclass();
    if ((superClass != null) && (superClass.getBinCIType().isFromCompilationUnit())) {
      list.add(new InheritanceSourceClassNode(this, superClass.getBinCIType()));
    }
  }
}
