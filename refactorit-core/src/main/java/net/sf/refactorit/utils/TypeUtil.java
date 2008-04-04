/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.utils;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.common.util.StringUtil;



public final class TypeUtil {
  // contains static functions for types

  public static String extractShortname(String qualifiedName) {
    int lastDotI = qualifiedName.lastIndexOf('.');
    if (lastDotI == -1) {
      return qualifiedName;
    } else {
      return qualifiedName.substring(lastDotI + 1);
    }
  }

  /**
   * FIXME: BUG: does not understand when name is shaded with method local class
   * @return String with
   * shortest name or empty string if refferedClass == invokedIn
   * better version of this method would also take a scope as a parameter
   */
  public static String getShortestUnderstandableName(BinCIType refferedClass,
      BinCIType invokedIn) {
    String shortestName = null;

    if (invokedIn == refferedClass) {
      shortestName = "";
    } else {
      BinTypeRef resolvesTo = null;
      try {
        resolvesTo = invokedIn.getTypeRef().getResolver().resolve(refferedClass.
            getName());
      } catch (Exception e) {
      }

      if (resolvesTo == null || resolvesTo.getBinType() != refferedClass) {
        BinTypeRef parentRef = refferedClass.getOwner();
        shortestName = refferedClass.getQualifiedName();
        if (parentRef != null) {
          BinTypeRef parentResolves = null;
          try {
            parentResolves = invokedIn.getTypeRef().getResolver().resolve(
                parentRef.getName());
          } catch (Exception e) {
          }
          if (parentResolves != null && parentResolves.equals(parentRef)) {
            shortestName = parentRef.getName() + '.' + refferedClass.getName();
          }
        }
      } else {
        shortestName = refferedClass.getName();
      }
    }

    shortestName = StringUtil.replace(shortestName, "$", ".");

    return shortestName;
  }

  /*
   * new int[3][4][5][6][7][8][9]).getClass().getName() the fully-qualified name is "[[[[[[[I"
   * This function return dimension count(ie number of '[') for fully-qualified name.
   */
  public static int getDimension(String fullyQualifiedName) {
    int retVal = 0;
    if (fullyQualifiedName != null) {
      for (;
          retVal < fullyQualifiedName.length()
          && fullyQualifiedName.charAt(retVal) == '['; retVal++) {
        ;
      }
    }
    return retVal;

  }

}
