/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel;



public final class MissingBinClass extends BinClass {
  private MissingBinClass(String name, BinPackage _package, Project project) {
    super(_package, name, BinMethod.NO_METHODS,
        BinField.NO_FIELDS,
        null,
        BinConstructor.NO_CONSTRUCTORS,
        BinInitializer.NO_INITIALIZERS,
        BinTypeRef.NO_TYPEREFS,
        null,
        BinModifier.PUBLIC,
        project);
  }

  public static BinClass createMissingBinClass(String qName, Project project) {
    BinPackage _package = null;
    String name = null;
    int dotNdx = qName.lastIndexOf('.');
    if (dotNdx > 0) {
      name = qName.substring(dotNdx + 1);
      _package = project.createPackageForName(qName.substring(0, dotNdx), false);
    } else {
      _package = project.createPackageForName("", false);
      name = qName;
    }

    return new MissingBinClass(name, _package, project);
  }
}
