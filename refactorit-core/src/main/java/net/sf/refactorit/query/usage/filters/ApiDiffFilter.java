/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.usage.filters;

import java.util.HashMap;


public final class ApiDiffFilter {
  private final HashMap instances = new HashMap();

  public ApiDiffFilter() {
  }

  private void createInstance(final Object type) {
    if (!instances.containsKey(type)) {
      instances.put(type, new FilterData(type));
    }
  }

  private FilterData getInstance(final Object type) {
    if (!instances.containsKey(type)) {
      createInstance(type);
    }
    return (FilterData) instances.get(type);
  }

  public final boolean getAccess(final Object type, final String access) {
    final FilterData filter = getInstance(type);
    return filter.getAccessFromHash(access);
  }

  public final void setAccess(final Object key, final Object value,
      final Object type) {
    final FilterData filter = getInstance(type);
    filter.putAccessToHash(key, value);
  }

  private static final class FilterData {
    private final Object type;
    private final HashMap accessHash = new HashMap();

    private FilterData(final Object type) {
      this.type = type;
    }

    final boolean getAccessFromHash(final String access) {
      if (!accessHash.containsKey(access)) {
        putAccessToHash(access, new Boolean(true));
      }

      return ((Boolean) accessHash.get(access)).booleanValue();
    }

    public final void putAccessToHash(final Object key, final Object value) {
//      if(accessHash.containsKey(key)) {
//        accessHash.remove(key);
//      }

      accessHash.put(key, value);
    }
  }
}

/*
 public class ApiDiffFilter {
  public static String[] accessStrings;
  private static HashMap instances = new HashMap();
  private HashMap accessHash = new HashMap();
  private String name;

  public static boolean isInstances() {
    return !instances.isEmpty();
  }

  public static ApiDiffFilter getInstance(String name) {
  if(instances.containsKey(name)) {
   return (ApiDiffFilter)instances.get(name);
  }
    else {
      return null;
    }
 }

  public static ApiDiffFilter createInstance(String name) {
  if(!instances.containsKey(name)) {
   instances.put(name, new ApiDiffFilter(name));
  }
    return (ApiDiffFilter)instances.get(name);
 }

  public static boolean skip(String type, String access) {
    ApiDiffFilter filter = getInstance(type);
    if(filter == null) {
      return false;
    }
    else {
      return !filter.checkAccess(access);
    }
  }

  public String getName() {
  return name;
 }

 public boolean checkAccess(String access) {
    if(!accessHash.containsKey(access)) {
      return false;
    }

    return ((Boolean)accessHash.get(access)).booleanValue();
 }

  public void addAccessHash(Object key, Object value) {
    accessHash.put(key, value);
  }

 private ApiDiffFilter() { }
 private ApiDiffFilter(String name) {
  this.name = name;
 }
 }
 */
