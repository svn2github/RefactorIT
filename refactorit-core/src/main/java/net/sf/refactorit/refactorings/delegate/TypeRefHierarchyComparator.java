/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.delegate;


import net.sf.refactorit.classmodel.BinTypeRef;

import java.util.Comparator;


/**
 * For comparing BinTypeRef-s by place in type hierarchy, subclasses are lesser than superclasses
 *
 * @author Tonis Vaga
 */
public class TypeRefHierarchyComparator implements Comparator {
  public TypeRefHierarchyComparator() {
  }

  public int compare(Object obj1, Object obj2) {
    BinTypeRef ref1 = (BinTypeRef) obj1;
    BinTypeRef ref2 = (BinTypeRef) obj2;

    if (ref1 == null) {
      return 1;
    }

    if (ref2 == null) {
      return -1;
    }

    if (ref1.equals(ref2)) {
      return 0;
    }

    if (ref1.isDerivedFrom(ref2)) {
      return -1;
    }

    if (ref2.isDerivedFrom(ref1)) {
      return 1;
    }

    BinTypeRef objectRef = ref1.getProject().getObjectRef();

    if (ref1.equals(objectRef)) {
      return 1;
    }

    if (ref2.equals(objectRef)) {
      return -1;
    }
    return 0;
  }
}
