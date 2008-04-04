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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 *
 * @author vadim
 */
public final class InheritanceSuperAndSubNode extends InheritanceSubTypeNode {
  private final BinCIType type;
  private final BinCIType superType;

  public InheritanceSuperAndSubNode(
      UITreeNode parent, BinCIType superType, BinCIType type
      ) {
    super(parent, ((superType == null) ? type : superType));

    this.type = type;
    this.superType = superType;
  }

  protected final void initInheritance(List list) {
    if (superType == null) {
      Iterator subClassesOfType = type.getTypeRef().getDirectSubclasses().
          iterator();
      while (subClassesOfType.hasNext()) {
        BinTypeRef subClass = (BinTypeRef) subClassesOfType.next();
        list.add(new InheritanceSuperAndSubNode(this, null,
            subClass.getBinCIType()));
      }
    } else {
      Collection subClassesOfSuper
          = superType.getTypeRef().getDirectSubclasses();

      if (subClassesOfSuper.contains(type.getTypeRef())) {
        list.add(new InheritanceSuperAndSubNode(this, null, type));
      } else {
        Iterator subs = subClassesOfSuper.iterator();
        while (subs.hasNext()) {
          BinTypeRef subClass = (BinTypeRef) subs.next();
          if (subClass.getAllSubclasses().contains(type.getTypeRef())) {
            list.add(new InheritanceSuperAndSubNode(this,
                subClass.getBinCIType(), type));
          }
        }
      }
    }
  }
}
