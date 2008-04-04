/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.common.util;


import net.sf.refactorit.common.exception.SystemException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


public final class ReflectionUtil {

  public static Object newInstance(String className, Class paramClass,
          Object paramValue) {
    return newInstance(className, new Class[] {paramClass},
            new Object[] {paramValue});
  }

  private static Object newInstance(String className, Class[] paramClasses,
          Object[] paramValues) {
    try {
      Class aClass = Class.forName(className);
      Constructor constructor = aClass.getConstructor(paramClasses);
      return constructor.newInstance(paramValues);
    } catch (Exception e) {
      AppRegistry.getExceptionLogger().error(e, ReflectionUtil.class);
      return null;
    }
  }

  public static boolean hasMethod(Object obj, String methodName,
          Class paramclass) {
    try {
      getMethod(obj, methodName, paramclass);
      return true;
    } catch (NoSuchMethodException e) {
      return false;
    }
  }

  /** For now, ignores exceptions (just logs them) */
  public static Object invokeMethod(Object obj, String methodName) {
    return invokeMethod(obj, methodName, new Class[0], new Object[0]);
  }

  /** For now, ignores exceptions (just logs them) */
  public static Object invokeMethod(Object obj, String methodName,
          Class paramClass, Object param) {
    return invokeMethod(obj, methodName, new Class[] {paramClass},
            new Object[] {param});
  }

  private static Object invokeMethod(Object obj, String methodName,
          Class[] paramClasses, Object[] parameters) {
    try {
      return getMethod(obj, methodName, paramClasses).invoke(obj, parameters);
    } catch (Exception e) {
      AppRegistry.getExceptionLogger().error(e, ReflectionUtil.class);
      return null;
    }
  }

  private static Method getMethod(Object obj, String methodName,
          Class paramClass) throws NoSuchMethodException {
    return getMethod(obj, methodName, new Class[] {paramClass});
  }

  private static Method getMethod(final Object obj, final String methodName,
          Class[] paramClasses) throws NoSuchMethodException {
    Class aClass = obj instanceof Class ? (Class) obj : obj.getClass();
    return aClass.getMethod(methodName, paramClasses);
  }

  /** @throw SystemException as a wrapper for all exceptions */
  public static String getField(Class c, String fieldName) {
    try {
      Field f = c.getField(fieldName);
      return (String) f.get(null);
    } catch (Exception e) {
      AppRegistry.getExceptionLogger().error(e, ReflectionUtil.class);
      throw new SystemException("INTERNAL_ERROR", e);
    }
  }
}
