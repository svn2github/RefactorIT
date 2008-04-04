/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.exceptions;

import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.refactorings.extract.ReturnThrowAnalyzer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * @author Oleg Tsernetsov
 */

public class RedundantSearchHelper {

  public static List getRequiredThrows(BinMethod method, List hierarchy) {
    Set requiredThrows = new HashSet();
    // find required exceptions thrown in method hierarchy
    for (Iterator it = hierarchy.iterator(); it.hasNext();) {
      BinMethod tmpMeth = (BinMethod) it.next();
      if (tmpMeth.hasBody() && !tmpMeth.isSynthetic()
          && tmpMeth.getCompilationUnit() != null) {
        ReturnThrowAnalyzer analyzer = new ReturnThrowAnalyzer(IDEController
            .getInstance().createProjectContext(), tmpMeth, Arrays
            .asList(tmpMeth.getBody().getStatements()));
        BinTypeRef[] thrown = analyzer.getThrownExceptions();
        requiredThrows.addAll(Arrays.asList(thrown));
      } else if (//tmpMeth.getOwner().getBinCIType().isInterface() || 
      //tmpMeth.isAbstract() || 
      tmpMeth.getCompilationUnit() == null) {
        addThrowsHierarchically(requiredThrows, getThrownExceptions(tmpMeth));
      }
    }
    List result = new ArrayList(requiredThrows);
    Collections.sort(result, new Comparator(){
      public int compare(Object o1, Object o2) {
        if(o1 == null || o2 == null) {
          return -1;
        }
        return o1.toString().compareTo(o2.toString());
      }
    });
    return result;
  }

  private static void addThrowsHierarchically(Set required, List newThrows) {
    List removed = new ArrayList();
    for (Iterator it = newThrows.iterator(); it.hasNext();) {
      BinTypeRef newRef = (BinTypeRef) it.next();
      for (Iterator iter = required.iterator(); iter.hasNext();) {
        BinTypeRef ref = (BinTypeRef) iter.next();
        if (!ref.equals(newRef) && ref.isDerivedFrom(newRef)) {
          removed.add(newRef);
        } else if (!newRef.equals(ref) && newRef.isDerivedFrom(ref)) {
          removed.add(ref);
        }
      }
    }
    required.addAll(newThrows);
    required.removeAll(removed);
  }

  private static List getThrownExceptions(BinMethod m) {
    List result = new ArrayList();

    BinMethod.Throws[] thrown = m.getThrows();
    for (int i = 0; i < thrown.length; i++)
      result.add(thrown[i].getException());

    return result;
  }

  public static List getThrownTypeList(BinMethod.Throws[] throwses) {
    List result = new ArrayList();
    for (int i = 0; i < throwses.length; i++) {
      result.add(throwses[i].getException());
    }
    return result;
  }

  public static BinMethod.Throws[] getThrowsArray(List thrownTypes) {
    BinMethod.Throws result[] = new BinMethod.Throws[thrownTypes.size()];
    int pos = 0;
    for (int i = 0; i < thrownTypes.size(); i++) {
      result[pos++] = new BinMethod.Throws((BinTypeRef) thrownTypes.get(i));
    }
    return result;
  }
}
