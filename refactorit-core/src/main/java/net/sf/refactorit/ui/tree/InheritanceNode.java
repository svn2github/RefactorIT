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

import java.util.ArrayList;
import java.util.List;



/**
 * Type node whose children is superclasses and interfaces.
 *
 * @author Igor Malinin
 */
public class InheritanceNode extends TypeNode implements Comparable {
  private final boolean onlyInterfaces;

  /*
   * InheritanceNode
   */
  public InheritanceNode(UITreeNode parent, BinCIType bin) {
    this(parent, bin, false);
  }

  public InheritanceNode(
      UITreeNode parent, BinCIType bin, boolean onlyInterfaces
      ) {
    super(parent, bin);

    this.onlyInterfaces = onlyInterfaces;
  }

  protected void initInheritance(List list) {
    if (!onlyInterfaces) {
      BinTypeRef ref = bin.getTypeRef().getSuperclass();

      if (ref != null) {
        BinCIType type = ref.getBinCIType();
        if (type != null) {
          list.add(new InheritanceNode(this, type));
        }
      }
    }

    BinTypeRef[] refs = bin.getTypeRef().getInterfaces();
    if (refs != null) {
      // Iterate through interfaces
      int max = (refs != null) ? refs.length : 0;
      for (int pos = 0; pos < max; pos++) {
        BinCIType type = refs[pos].getBinCIType();
        list.add(new InheritanceNode(this, type));
      }
    }
  }

  protected final List getMembers() {
    // Init lazily
    if (members == null) {
      members = new ArrayList(16);

      // Populate list
      initInheritance(members);
    }

    // Return result
    return members;
  }

  public final int compareTo(Object obj) {
    if (!(obj instanceof InheritanceNode)) {
      return -1;
    }

    InheritanceNode node = (InheritanceNode) obj;

    return getBinCIType().getName().compareTo(node.getBinCIType().getName());
  }
}
