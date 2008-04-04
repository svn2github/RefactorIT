/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.changesignature.analyzer;


import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.refactorings.changesignature.ChangeMethodSignatureRefactoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Aqris AS</p>
 * @author Kirill Buhhalko
 * @version
 */

public class ChangesContainer {
  private HashMap allChangesContainer = new HashMap();

  public ChangesContainer() {
  }

  public ChangeMethodSignatureRefactoring getMethSignChangeForCurrentMethod(
      BinMethod binMethod) {

    Object o;
    o = allChangesContainer.get(binMethod);
    if (o != null) {
      return (ChangeMethodSignatureRefactoring) o;
    } else {
      // check if overrides has MethodSignatureChange
      List list = binMethod.findAllOverridesOverriddenInHierarchy();

      for (int i = 0; i < list.size(); i++) {
        o = allChangesContainer.get(list.get(i));
        if (o != null) {
          return (ChangeMethodSignatureRefactoring) o;
        }
      }

      ChangeMethodSignatureRefactoring ref = new
          ChangeMethodSignatureRefactoring(binMethod);
      ref.setChange(ref.createSingatureChange());
      allChangesContainer.put(binMethod, ref);
      return ref;
    }
  }

  public ChangeMethodSignatureRefactoring[] getAllRefactorings() {
    Set set = allChangesContainer.keySet();
    ArrayList list = new ArrayList(set.size());

    for (Iterator i = set.iterator(); i.hasNext(); ) {
      list.add((ChangeMethodSignatureRefactoring) allChangesContainer.get(i.
          next()));
    }

    return (ChangeMethodSignatureRefactoring[]) list.toArray(new
        ChangeMethodSignatureRefactoring[list.size()]);
  }
}
