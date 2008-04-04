/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.classfile;

import java.util.ArrayList;


public final class ClassUtil {

  public static String getNameForDescriptor(String descriptor) {
    if (descriptor.charAt(0) == OBJECT_IDENT) {
      if (!descriptor.endsWith(";")) {
        throw new IllegalArgumentException("descriptor format invalid:'"
            + descriptor + "'");
      }
      return descriptor.substring(1, descriptor.length() - 1);
    }

    //it wasn't object, so it must be one chat identifing primitive type
    if (descriptor.length() != 1) {
      throw new IllegalArgumentException("descriptor format invalid:'"
          + descriptor + "'");
    }
    char typeIdent = descriptor.charAt(0);
    for (int q = 0; q < typeCodes.length; q++) {
      if (typeCodes[q] == typeIdent) {
        return primitiveNames[q];
      }
    }
    //we didn't find any matching char for descriptor
    throw new IllegalArgumentException("descriptor format invalid:'"
        + descriptor + "'");
  }

  public static ArrayList splitDescriptors(String descriptor)
      throws ClassFormatException {
    ArrayList retVal = null;
    int pos = 0;
    while (pos < descriptor.length()) {
      int startPos = pos;
      char curChar = descriptor.charAt(pos);
      while (curChar == '[') {
        pos++;
        curChar = descriptor.charAt(pos);
      } //count dimension identifiers in

      if (curChar == ClassUtil.OBJECT_IDENT) {
        int descrEndI = descriptor.indexOf(";", pos);
        if (descrEndI == -1) {
          throw new ClassFormatException("invalid descriptor sequence '"
              + descriptor + "' at pos:" + pos);
        }
        String curDescr = descriptor.substring(startPos, descrEndI + 1); //";" also in
        if (retVal == null) {
          retVal = new ArrayList(3);
        }
        retVal.add(curDescr);
        pos = descrEndI + 1;
        continue;
      } else {
        boolean found = false;
        for (int q = 0; q < ClassUtil.typeCodes.length; q++) {
          if (ClassUtil.typeCodes[q] == curChar) {
            String curDesrc = descriptor.substring(startPos, pos + 1);
            if (retVal == null) {
              retVal = new ArrayList(3);
            }
            retVal.add(curDesrc);
            pos++; //we have found descriptor
            found = true;
            break;
          }
        }
        if (!found) {
          //we didn't find correct ident char
          throw new ClassFormatException("invalid descriptor sequence '"
              + descriptor + "' at pos:" + pos);
        }
      }
    }

    return retVal;
  }

  public static final char BYTE_IDENT = 'B';
  public static final char CHAR_IDENT = 'C';
  public static final char DOUBLE_IDENT = 'D';
  public static final char FLOAT_IDENT = 'F';
  public static final char INT_IDENT = 'I';
  public static final char LONG_IDENT = 'J';
  public static final char SHORT_IDENT = 'S';
  public static final char BOOLEAN_IDENT = 'Z';
  public static final char VOID_IDENT = 'V';

  public static final char OBJECT_IDENT = 'L';

  public static final char[] typeCodes = {BYTE_IDENT,
      CHAR_IDENT,
      DOUBLE_IDENT,
      FLOAT_IDENT,
      INT_IDENT,
      LONG_IDENT,
      SHORT_IDENT,
      BOOLEAN_IDENT,
      VOID_IDENT
  };
  public static final String[] primitiveNames = {"byte",
      "char",
      "double",
      "float",
      "int",
      "long",
      "short",
      "boolean",
      "void"
  };
}
