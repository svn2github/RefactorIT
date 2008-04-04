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

import java.util.Iterator;


/**
 * Defines resolver for binary(compiled) classes.
 */
final class ClassTypeResolver extends Resolver {
  ClassTypeResolver(BinTypeRef aType) {
    super(aType);
  }

  public BinTypeRef resolve(String name) throws SourceParsingException {
    if (this.type == null) {
      return null;
    }

    BinTypeRef result = this.type.getBinCIType().getDeclaredType(name);
    if (result == null) {
      Iterator supers = this.type.getAllSupertypes().iterator();
      while (supers.hasNext()) {
        result = ((BinTypeRef) supers.next()).getBinCIType()
            .getDeclaredType(name);
        if (result != null) {
          break;
        }
      }
    }

    return result;

//    Assert.must(
//     false,
//     "Illegal call to compiled class resolver's resolve, "
//       + type.getQualifiedName());
//    return null;
  }

  public void resolveSuperTypes() throws SourceParsingException {}
}
