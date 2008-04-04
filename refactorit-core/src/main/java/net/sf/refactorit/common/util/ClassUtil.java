/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.common.util;


import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Tõnis Vaga
 * @author Anton Safonov
 */
public final class ClassUtil {

  public static final Class[] getClassesArray(final Object[] array) {
    final ArrayList result = new ArrayList(array.length);
    for (int i = 0; i < array.length; i++) {
      if (array[i] != null) {
        result.add(array[i].getClass());
      } else {
        AppRegistry.getLogger(ClassUtil.class).warn(
            "skipped a class in getClassesArray: " + Arrays.asList(array));
      }
    }

    return (Class[]) result.toArray(new Class[result.size()]);
  }

  public static final Class[] getClassesArray(final Object object) {
    if (object instanceof Object[]) {
      return getClassesArray((Object[]) object);
    } else {
      return new Class[] {object.getClass()};
    }
  }

  public static final String getShortClassName(final Object object) {
    String name = object.getClass().getName();
    if (name.lastIndexOf('$') >= 0) {
      name = name.substring(name.lastIndexOf('$') + 1);
    } else if (name.lastIndexOf('.') >= 0) {
      name = name.substring(name.lastIndexOf('.') + 1);
    }

    return name;
  }
}
