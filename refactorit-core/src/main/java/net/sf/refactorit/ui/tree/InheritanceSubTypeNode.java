/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.tree;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinTypeRef;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * Used to create a node representing a type and which provides
 * information for that type. i.e. specifically a subtypes for
 * that type it represents.
 */
class InheritanceSubTypeNode extends InheritanceNode {
  protected InheritanceSubTypeNode(UITreeNode parent, BinCIType bin) {
    super(parent, bin);
  }

  /**
   * Initializes the list with the direct subTypes of this.bin object.
   *
   * It is overridden function in its base class.
   * This method is called by the method in base class.
   * {@link InheritanceNode#getMembers()}
   *
   * @param list to be filled with direct subTypes of this.bin object.
   */
  protected void initInheritance(List list) {
    Iterator subClassesOfBin = bin.getTypeRef().getDirectSubclasses().iterator();
    // iterate through the list of direct subclasses
    // of Bin and add each into the list.
    while (subClassesOfBin.hasNext()) {
      BinTypeRef typeRef = (BinTypeRef) subClassesOfBin.next();
      list.add(new InheritanceSubTypeNode(this, typeRef.getBinCIType()));
    }
    Collections.sort(list);
  }
}
