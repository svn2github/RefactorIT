/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.movetype;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMember;


// TODO: useless class - refactor and remove
public class MoveTypeUtil {

  public static BinCIType getType(final BinMember member) {
    if (member instanceof BinCIType) {
      return (BinCIType) member;
    } else {
      return member.getOwner().getBinCIType();
    }
  }
}
