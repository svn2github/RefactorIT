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
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.utils.GetterSetterUtils;

import java.util.List;


/**
 *
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Aqris AS</p>
 * @author Kirill Buhhalko
 * @version
 */

public class MethodsCallsNode extends CallNode {

  public MethodsCallsNode(Object bin, MethodsInvocationsMap finder, int type,
      MultiValueMap map) {
    super(bin);
    if (map != null) {
      if (getBin() instanceof BinMethod) {
        map.put(getBin(), this);
      }
    }
    this.finder = finder;
    this.typeD = type;
    this.selected = false;
    this.map = map;
  }

  protected void findChilds(MethodsInvocationsMap finder, final BinMethod method) {
    List called = finder.findAllMethodsAreCalledByThis(method);
    called = sort(called);
    for (int i = 0; called != null && i < called.size(); i++) {
      Object calledMethod = called.get(i);
      if (!alreadyContains((BinMethod) calledMethod)) {
        if (!(GetterSetterUtils.isGetterMethod((BinMethod) calledMethod, true) ||
            GetterSetterUtils.isSetterMethod((BinMethod) calledMethod, true))) {

          MethodsCallsNode thisNode = new MethodsCallsNode(calledMethod, finder,
              CHILD, map);
          addChild(thisNode);
        }
      } else {
        addChild(new MethodsCallsNode("recursive  call ->> "
            + ((BinMethod) calledMethod).getQualifiedName(), null, REC, map));
      }
    }
  }

  protected void findParents(MethodsInvocationsMap finder, final BinMethod method) {
    List called = finder.findAllMethodsWhichCallThis(method);
    called = sort(called);
    for (int i = 0; called != null && i < called.size(); i++) {
      Object caller = called.get(i);
      if (!alreadyContains((BinMethod) caller)) {
        MethodsCallsNode thisNode = new MethodsCallsNode(caller, finder, PARENT,
            map);
        addChild(thisNode);
      } else {
        addChild(new MethodsCallsNode("recursive  call ->> "
            + ((BinMethod) caller).getQualifiedName(), null, REC, map));
      }
    }
  }


}
