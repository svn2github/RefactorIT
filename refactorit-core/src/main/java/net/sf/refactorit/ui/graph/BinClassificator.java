/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.graph;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.Project;


/**
 * Returns type of the item usable in switches.<br>
 * Didn't add it into classmodel, since the level of classification may differ
 * depending on the task, e.g. here we doesn't need to differ fields and methods.
 * @author Anton Safonov
 */
public final class BinClassificator {

  public static final int PROJECT = 0;
  public static final int PACKAGE = 1;
  public static final int TYPE = 2;
  public static final int MEMBER = 3;

  /**
   * @used
   */
  private BinClassificator() {
  }

  public static final int getItemType(final BinItemVisitable item,
      final boolean treatInnerAsType) {
    if (item instanceof Project) {
      return PROJECT;
    } else if (item instanceof BinPackage) {
      return PACKAGE;
    } else if (item instanceof BinCIType
        && (treatInnerAsType || !((BinCIType) item).isInnerType()
        || ((BinCIType) item).isLocal())) {
      return TYPE;
    } else {
      return MEMBER;
    }
  }

  public static final BinItemVisitable getParent(final BinItemVisitable bin) {
    if (bin instanceof BinCIType && ((BinCIType) bin).isLocal()) {
      return ((BinCIType) bin).getParentMember();
    }
    if (bin instanceof BinField) {
      // this is because of the feature in the classmodel:
      // BinVariableDeclaration is a parent of field, which is not what we mean here
      return ((BinField) bin).getOwner().getBinCIType();
    }

    return bin.getParent(); // all others should work as expected
  }
}
