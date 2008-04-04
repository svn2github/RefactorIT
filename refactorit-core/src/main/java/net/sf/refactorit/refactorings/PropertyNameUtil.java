/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings;

import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.common.util.StringUtil;

import java.util.ArrayList;
import java.util.List;



public final class PropertyNameUtil {
  private PropertyNameUtil() {}

  public static String getDefaultSetterName(BinField field) {
    return getDefaultSetterName(field.getName());
  }

  public static String[] getDefaultGetterName(BinField field) {
    return getDefaultGetterName(field.getName(),
        field.getTypeRef().getBinType() == BinPrimitiveType.BOOLEAN);
  }

  public static String getDefaultSetterName(String fieldName) {
    return "set" + getPropertyName(fieldName);
  }

  public static String[] getDefaultGetterName(String fieldName,
      boolean fieldTypeIsBoolean) {
    List list = new ArrayList();
    
    // order is vital! Some classes expect first element to be convension strict!
    if (fieldTypeIsBoolean) {
      list.add("is" + getPropertyName(fieldName));
    } 
    list.add("get" + getPropertyName(fieldName));
    return (String[])list.toArray(new String[list.size()]);
  }

  public static String getPropertyName(String fieldName) {
    if (fieldName.startsWith("_")) {
      fieldName = fieldName.substring(1);
    }
    return StringUtil.capitalizeFirstLetter(fieldName);
  }

  public static String getFieldName(final String accessorName) {
    final String[] prefixes = new String[] {"is", "get", "set"};
    for (int i = 0; i < prefixes.length; i++) {
      if (prefixes[i].equals(accessorName)) {
        return "";
      }
      if (accessorName.length() <= prefixes[i].length()) {
        continue;
      }
      if (accessorName.startsWith(prefixes[i])
          && Character.isUpperCase(
          accessorName.charAt(prefixes[i].length()))) {
        return StringUtil.decapitalizeFirstLetter(
            accessorName.substring(prefixes[i].length()));
      }
    }

    return null;
  }
}
