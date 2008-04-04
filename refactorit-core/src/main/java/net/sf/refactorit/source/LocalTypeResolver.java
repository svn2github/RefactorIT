/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source;


import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.common.util.Assert;

import java.util.HashMap;
import java.util.Map;


/**
 * Defines class for resolving name in the given context.
 */
final class LocalTypeResolver extends Resolver {
  // FIXME: globalOwnerTypeRef is really an outer type ref? (in case local is within local?)

  LocalTypeResolver(BinTypeRef aTypeRef,
      BinTypeRef globalOwnerTypeRef,
      BinTypeRef[] localDefinedTypes) {
    super(aTypeRef);
    //System.err.println("To " + aTypeRef.getQualifiedName() + " OWNER " + globalOwnerTypeRef);
    this.globalOwnerTypeRef = globalOwnerTypeRef;

    if (Assert.enabled && globalOwnerTypeRef.equals(aTypeRef)) {
      Assert.must(false,
          "Tried to pass itself as globalOwner - " + aTypeRef);
    }

    localDefinedTypesMap = new HashMap();

    for (int i = 0; i < localDefinedTypes.length; i++) {
      localDefinedTypesMap.put(localDefinedTypes[i].getQualifiedName(),
          localDefinedTypes[i]);
    }
  }

  /**
   * Gets extra location information. Used to provide more detailed error
   * messages.
   *
   * @return extra location information or <code>null</code> no information
   * available.
   */
  String getLocationInfo() {
    return globalOwnerTypeRef.getQualifiedName();
  }

  //============================================================================
  /**
   * Resolves type name in given context, also used for resolving interfaces.
   *
   * @see #getInnerTypeRefForName
   */
  public BinTypeRef resolve(String name) throws SourceParsingException,
      LocationlessSourceParsingException {

    // try to resolve name as inner
    BinTypeRef retVal = getAllInnerForName(name);
    if (retVal != null) {
      return retVal;
    }

    // try to resolve as local Type
    retVal = getTypeRefForNameInLocal(name);
    if (retVal != null) {
      return retVal;
    }

    // try to resolve name as global type
    return globalOwnerTypeRef.getResolver().resolve(name);
  }

  /**
   * Resolves superclass for given context.
   *
   * @see #getOwnerInnerForName
   */
  BinTypeRef resolveAsSuperclass(String name) throws SourceParsingException,
      LocationlessSourceParsingException {
//System.err.println("resolveAsSuperclass: " + name);
    // try to resolve name as inner
    BinTypeRef retVal = getOwnerInnerForName(name);

    // try to resolve as local Type
    if (retVal == null) {
//System.err.println("111");
      retVal = getTypeRefForNameInLocal(name);
    }

    // try to resolve name as global type
    if (retVal == null) {
//System.err.println("222");
      retVal = globalOwnerTypeRef.getResolver().resolve(name);
    }

//System.err.println("retval: " + retVal);
    return retVal;
  }

  //==========================================================================

  private BinTypeRef getTypeRefForNameInLocal(String name) throws
      SourceParsingException {
//System.err.println("localDefinedTypesMap: " + localDefinedTypesMap);
    BinTypeRef retVal = (BinTypeRef) localDefinedTypesMap.get(name);
//System.err.println("name: " + name + " - " + retVal);
    if (retVal != null) {
      return retVal;
    }

//    int pos = name.indexOf('.');
//
//    String tmpName;
//    if(pos == -1) {
//      tmpName = name;
//    } else {
//      tmpName = name.substring(0, pos);
//    }

    retVal = (BinTypeRef) localDefinedTypesMap.get(name);
    if (retVal == null) {
      return null;
    }

    return retVal.getResolver().getAllInnerForName(name);
  }

//  private static FastStack typesInProgress = new FastStack();

  private final BinTypeRef globalOwnerTypeRef;
  private final Map localDefinedTypesMap;
}
