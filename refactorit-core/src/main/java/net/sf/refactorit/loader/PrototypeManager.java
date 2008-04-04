/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.loader;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinTypeRef;

import java.util.HashMap;


public final class PrototypeManager {
  private final HashMap prototypeMap = new HashMap(10);

  final void clear() {
    prototypeMap.clear();
  }

  public final BinField findField(final BinTypeRef forRef, final String name) {
    final PrototypeSet set = (PrototypeSet) prototypeMap.get(forRef);
    if (set == null) {
      return null;
    }

    return set.findField(name);
  }

  public final BinMethod findMethod(final BinTypeRef forRef,
      final String name, final BinParameter[] params) {
    final PrototypeSet set = (PrototypeSet) prototypeMap.get(forRef);
    if (set == null) {
      return null;
    }

    return set.findMethod(name, params);
  }

  public final BinConstructor findConstructor(final BinTypeRef forRef,
      final BinParameter[] params) {
    final PrototypeSet set = (PrototypeSet) prototypeMap.get(forRef);
    if (set == null) {
      return null;
    }

    return set.findConstructor(params);
  }

  final void registerPrototype(final BinTypeRef forRef) {
    final BinCIType aType = forRef.getBinCIType();

    final PrototypeSet prot = new PrototypeSet(aType);
    prototypeMap.put(forRef, prot);
  }

}
