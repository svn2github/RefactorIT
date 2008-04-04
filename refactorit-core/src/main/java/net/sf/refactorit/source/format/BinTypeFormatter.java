/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.format;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.source.SourceCoordinate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


public class BinTypeFormatter extends BinItemFormatter {
  private BinCIType type;

  public BinTypeFormatter(BinCIType type) {
    this.type = type;
  }

  public int getMemberIndent() {
    if (type != null) {

//<FIX> author Aleksei Sosnovski
/* This code seems not to be needed and in fact generates wrong indent
      if (type.isAnonymous()) {
        // FIXME totally wrong technology used here...
        BinItemVisitable parent = type.getParent();
        if (parent != null) {
          parent = parent.getParent();
        }
        if (parent != null) {
          parent = parent.getParent();
        }
        if (parent instanceof BinExpressionList) {
          parent = parent.getParent();
        }
        if (parent instanceof LocationAware) {
          return ((LocationAware) parent).getStartColumn();
        }
      }
*/
//</FIX>

      return getIndent(getDeclaredMembers(type), type.getIndent());
    } else {
      return FormatSettings.getBlockIndent();
    }
  }

  public SourceCoordinate findNewMemberPosition() {
    return type.findNewFieldPosition();
  }

  // General methods to work with BinCIType

  private static List getDeclaredMembers(BinCIType type) {
    List members = new ArrayList();
    members.addAll(Arrays.asList(type.getDeclaredFields()));
    members.addAll(Arrays.asList(type.getDeclaredMethods()));
    if (type.isClass() || type.isEnum()) {
      members.addAll(Arrays.asList(((BinClass) type).getDeclaredConstructors()));
    }

    removeVirtualMethods(members);

    return members;
  }

  private static void removeVirtualMethods(List members) {
    for (Iterator i = members.iterator(); i.hasNext(); ) {
      BinMember member = (BinMember) i.next();
      if (member instanceof BinMethod
          && ((BinMethod) member).isSynthetic()) {
        i.remove();
      }
      if (member instanceof BinConstructor
          && ((BinConstructor) member).isSynthetic()) {
        i.remove();
      }
    }
  }

  /* (non-Javadoc)
   * @see net.sf.refactorit.source.format.BinItemFormatter#print()
   */
  public String print() {
    // TODO Auto-generated method stub
    return null;
  }
}
