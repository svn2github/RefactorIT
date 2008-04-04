/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.classmodel;

import net.sf.refactorit.common.util.MultiValueMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 *
 * @author  Arseni Grigorjev
 */
public class CommonTypeFinder {

  private final Set types;
  private final List tops = new ArrayList(5);
  private final MultiValueMap map;
  private boolean interfacesHigherPriority = false;

  public CommonTypeFinder(final Set types) {
    this.types = types;
    map = new MultiValueMap(types.size() * 2);
    BinTypeRef type;
    for (Iterator it = types.iterator(); it.hasNext(); ) {
      type = (BinTypeRef) it.next();
      findTopFor(type, 0);
    }
  }

  public CommonTypeFinder(BinTypeRef type1, BinTypeRef type2) {
    this(createTypesSet(type1, type2));
  }

  public void setInterfacesHigherPriority(final boolean b){
    this.interfacesHigherPriority = b;
  }
  
  private static Set createTypesSet(BinTypeRef type1, BinTypeRef type2){
    final Set types = new HashSet(2);
    types.add(type1);
    types.add(type2);
    return types;
  }

  public BinTypeRef getCommonType() {
    final TopType top = findMostSuitableTop();
    if (top == null) {
      return null;
    } else {
      return findMostSuitableResult(top.getTypeRef());
    }
  }

  private final void findTopFor(final BinTypeRef type, final int hops) {
    final BinTypeRef[] supertypes = type.getSupertypes();
    if (supertypes.length == 0) {
      registerTop(type, hops);
    } else {
      for (int i = 0; i < supertypes.length; i++) {
        map.put(supertypes[i], type);
        findTopFor(supertypes[i], hops + 1);
      }
    }
  }

  private final void registerTop(final BinTypeRef type, final int hops) {
    TopType top;
    for (final Iterator it = tops.iterator(); it.hasNext(); ) {
      top = (TopType) it.next();
      if (top.getTypeRef().equals(type)) {
        top.hops(hops);
        top.child();
        return;
      }
    }
    top = new TopType(type, hops);
    tops.add(top);
  }

  private TopType findMostSuitableTop() {
    TopType best = null;
    TopType current;
    for (int i = 0, max_i = tops.size(); i < max_i; i++) {
      current = (TopType) tops.get(i);
      if (current.getChildren() == types.size() && (best == null
          || best.getMaxHops() < current.getMaxHops()
          || (best.getMaxHops() == current.getMaxHops()
          && interfacesHigherPriority && best.getTypeRef().equals(
          best.getTypeRef().getProject().getObjectRef())))) {
        best = current;
      }
    }

    return best;
  }

  private BinTypeRef findMostSuitableResult(BinTypeRef currentType) {
    final List subtypes = map.get(currentType);
    if (subtypes == null
        || subtypes.size() != 1
        || types.contains(currentType)) {
      return currentType;
    } else {
      return findMostSuitableResult((BinTypeRef) subtypes.get(0));
    }
  }

// unchecked methods, need to test and uncomment
  public BinTypeRef[] getAllCommonTypes() {
    List tops = findAllSuitableTops();
    Set result = new HashSet(tops.size());

    BinTypeRef commonType;
    for (int i = 0, max_i = tops.size(); i < max_i; i++) {
      commonType = findMostSuitableResult(((TopType) tops.get(i)).getTypeRef());
      if (commonType != null) {
        result.add(commonType);
      }
    }

    return (BinTypeRef[]) result.toArray(new BinTypeRef[result.size()]);
  }

  private List findAllSuitableTops() {
    List result = new ArrayList(tops.size());
    TopType current;
    for (int i = 0, max_i = tops.size(); i < max_i; i++) {
      current = (TopType) tops.get(i);
      if (current.getChildren() == types.size()) {
        result.add(current);
      }
    }
    return result;
  }
}

class TopType {
  private BinTypeRef typeRef;
  private int maxHops;
  private int children;

  public TopType(final BinTypeRef typeRef, int hops) {
    this.typeRef = typeRef;
    maxHops = hops;
    children = 1;
  }

  public void child() {
    ++children;
  }

  public void hops(final int hops) {
    if (hops > maxHops) {
      maxHops = hops;
    }
  }

  public BinTypeRef getTypeRef() {
    return typeRef;
  }

  public int getMaxHops() {
    return maxHops;
  }

  public int getChildren() {
    return children;
  }
}
