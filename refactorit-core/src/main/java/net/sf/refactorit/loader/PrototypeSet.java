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
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinTypeRef;


public final class PrototypeSet {

  private final BinField[] fields;
  private final BinMethod[] methods;
  private BinConstructor[] constructors = null;

  public PrototypeSet(final BinCIType aBinType) {
    if (aBinType.isClass() || aBinType.isEnum()) {
      constructors = ((BinClass) aBinType).getDeclaredConstructors();
    }
    fields = aBinType.getDeclaredFields();
    methods = aBinType.getDeclaredMethods();
    aBinType.cleanForPrototype();
  }

  // FIXME: this piece of code is also used in several places (BinMethod.sameSingature for sure)
  // NOTE: it is faster to call it here, than to create an instance of BinMethod
  // NOTE: we already had static BinMethodUtils and it was bad :(
  private static boolean parametersEqual(final BinParameter[] p1,
      final BinParameter[] p2) {
    if (p1.length != p2.length) {
      return false;
    }

    for (int i = 0; i < p1.length; ++i) {
      final BinTypeRef thisParameterType = p1[i].getTypeRef();
      final BinTypeRef otherParameterType = p2[i].getTypeRef();

      if (!(thisParameterType == otherParameterType
          || thisParameterType.equals(otherParameterType))
          // NOTE: this is an important place here -
          // e.g. array types are totally cleared if their base class is being rebuilded,
          // but we still need them to compare with the new parameter types somehow
          && !thisParameterType.getQualifiedName().equals(
          otherParameterType.getQualifiedName())) {
        return false;
      }
    }

    return true;
  }

  final BinConstructor findConstructor(final BinParameter[] params) {
    if (constructors != null) {
      for (int i = 0; i < constructors.length; ++i) {
        final BinConstructor cur = constructors[i];
        if (parametersEqual(cur.getParameters(), params)) {
          return cur;
        }
      }
    }

    return null;
  }

  final BinMethod findMethod(final String forName, final BinParameter[] params) {
    for (int i = 0; i < methods.length; ++i) {
      final BinMethod cur = methods[i];
      if (forName.equals(cur.getName())
          && parametersEqual(params, cur.getParameters())) {
        return cur;
      }
    }

    return null;
  }

  final BinField findField(final String forName) {
    for (int i = 0; i < fields.length; ++i) {
      final BinField cur = fields[i];
      if (forName.equals(cur.getName())) {
        return cur;
      }
    }

    return null;
  }

}
