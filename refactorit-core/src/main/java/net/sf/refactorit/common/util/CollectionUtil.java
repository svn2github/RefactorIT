/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.common.util;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;


/**
 * Collection of static methods to use {@link java.util.List Lists} more
 * effectively.
 *
 * @author Anton Safonov
 * @author Risto
 */
public final class CollectionUtil {
  public static final Set EMPTY_SET = new EmptySet();
  public static final Map EMPTY_MAP = new HashMap(0);
  public static final ArrayList EMPTY_ARRAY_LIST = new ArrayList(0) {
   public void add(int i1, Object obj2) {
     throw new UnsupportedOperationException("this is unmodifiable list");
   }

   public boolean add(Object obj) {
     throw new UnsupportedOperationException("this is unmodifiable list");
   }

   public Object set(int i1, Object obj2) {
     throw new UnsupportedOperationException("this is unmodifiable list");
   }

   public boolean addAll(int i1, Collection collection2) {
     throw new UnsupportedOperationException("this is unmodifiable list");
   }

   public boolean addAll(Collection collection) {
     throw new UnsupportedOperationException("this is unmodifiable list");
   }
  };

  private static final class EmptySet extends AbstractSet {
    private static final Iterator EMPTY_ITERATOR = new EmptyIterator();

    private static final class EmptyIterator implements Iterator {
      public final boolean hasNext() {
        return false;
      }

      public final Object next() {
        throw new NoSuchElementException();
      }

      public final void remove() {
        throw new UnsupportedOperationException();
      }
    };

    public final Iterator iterator() {
      return EMPTY_ITERATOR;
    }

    public final int size() {
      return 0;
    }

    public final boolean contains(Object obj) {
      return false;
    }
  }


  /**
   * Adds an <code>object</code> to the given <code>list</code> when it is
   * still missing.
   *
   * @param list where to add
   * @param object what to add
   * @return the same resulting list as given
   */
  public static final List addNew(List list, Object object) {
    if (!list.contains(object)) {
      list.add(object);
    }

    return list;
  }

  /**
   * Adds an <code>object</code> to the given <code>list</code> when it is
   * still missing.
   *
   * @param list where to add
   * @param object what to add
   * @param comparator used in determinig if the list contains the object
   * @return the same resulting list as given
   */
  public static final List addNew(List list, Object object,
      Comparator comparator) {
    if (!contains(list, object, comparator)) {
      list.add(object);
    }

    return list;
  }

  public static final HashMap addNew(HashMap map, Object key, Object value) {
    if (!map.containsKey(key)) {
      map.put(key, value);
    }

    return map;
  }

  /**
   * Uses given comparator to compare all list items to the given object to see
   * if there are some equal objects in the list.
   *
   * @return  true if there is at least one equal object, false otherwise
   */
  public static final boolean contains(List list, Object object,
      Comparator comparator) {
    for (int i = 0; i < list.size(); i++) {
      if (comparator.compare(list.get(i), object) == 0) {
        return true;
      }
    }

    return false;
  }

  /**
   * Adds all objects which are still missing from the <code>from</code> list
   * to the given <code>to</code> list.
   *
   * @param to list where to add
   * @param from list from which to get content
   * @return <code>to</code> list
   */
  public static final List addAllNew(List to, List from) {
    for (int i = 0, max = from.size(); i < max; i++) {
      final Object object = from.get(i);
      if (!to.contains(object)) {
        to.add(object);
      }
    }

    return to;
  }

  public static final void removeDuplicates(List list) {
    List duplicatesToRemove = new ArrayList();

    for (int i = 0; i < list.size(); i++) {
      final Object first = list.get(i);

      for (int k = i + 1; k < list.size(); k++) {
        if (i != k) {
          final Object second = list.get(k);

          if (first.equals(second)) {
            CollectionUtil.addNew(duplicatesToRemove, second);
          }
        }
      }
    }

    for (int i = 0; i < duplicatesToRemove.size(); i++) {
      final Object duplicate = duplicatesToRemove.get(i);
      list.remove(duplicate);
    }
  }

  public static final void removeDuplicates(List list, Comparator comparator) {
    List duplicatesToRemove = new ArrayList();

    for (int i = 0; i < list.size(); i++) {
      final Object first = list.get(i);

      for (int k = i + 1; k < list.size(); k++) {
        if (i != k) {
          final Object second = list.get(k);

          if (comparator.compare(first, second) == 0) {
            duplicatesToRemove.add(second);
//            CollectionUtil.addNew(duplicatesToRemove, second, comparator);
          }
        }
      }
    }

    list.removeAll(duplicatesToRemove);

//    for (int i = 0; i < duplicatesToRemove.size(); i++) {
//      final Object duplicate = duplicatesToRemove.get(i);
//      list.remove(duplicate);
//    }
  }

  public static boolean containsSome(List l, List items) {
    for (int i = 0; i < items.size(); i++) {
      if (l.contains(items.get(i))) {
        return true;
      }
    }

    return false;
  }

  public static final ArrayList singletonArrayList(Object o) {
    final ArrayList result = new ArrayList(1);
    result.add(o);
    return result;
  }

  public static final List toList(Iterator it) {
    final ArrayList result = new ArrayList();
    if (it == null) {
      return result;
    }
    while (it.hasNext()) {
      result.add(it.next());
    }

    return result;
  }

  public static List toList(Enumeration e) {
    final ArrayList result = new ArrayList();
    if (e == null) {
      return result;
    }
    while (e.hasMoreElements()) {
      result.add(e.nextElement());
    }
    return result;
  }

  public static List toMutableList(Object[] array) {
    final ArrayList result = new ArrayList(array.length);
    for (int i = 0; i < array.length; i++){
      result.add(array[i]);
    }
    return result;
  }

  public static void addAll(Collection collection, Object[] array) {
    for (int i = 0, max = array.length; i < max; i++) {
      collection.add(array[i]);
    }
  }

  public static void addAllNew(List list, Object[] array) {
    for (int i = 0, max = array.length; i < max; i++) {
      addNew(list, array[i]);
    }
  }

  /**
   * @return two element list
   */
  public static List toList(Object obj1, Object obj2) {
    ArrayList list=new ArrayList(2);
    list.add(obj1);
    list.add(obj2);
    return list;
  }

  public static void removeAll(List list, Object[] elements) {
    if ( elements == null ) {
      return;
    }
    for (int i = 0; i < elements.length; i++) {
      list.remove(elements[i]);
    }
  }

  public static void removeNulls(Collection c) {
    for(Iterator i = c.iterator(); i.hasNext(); ) {
      if(i.next() == null) {
        i.remove();
      }
    }
  }

  public static Map toMap(Object[] keys, Object[] values) {
    if (keys == null || values == null) {
      return EMPTY_MAP;
    }

    return toMap(new HashMap(keys.length), keys, values);
  }

  public static Map toMap(Map map, Object[] keys, Object[] values) {
    if (keys == null || values == null) {
      return map;
    }
    for (int i = 0, max = Math.min(keys.length, values.length); i < max; i++) {
      map.put(keys[i], values[i]);
    }

    return map;
  }

  public static Properties clone(Properties input) {
    Properties result = new Properties();
    result.putAll(input);
    return result;
  }

  public static List intersection(List list1, List list2) {
    List result = new ArrayList(list1.size() + list2.size());
    Object item;
    for (int i = 0, max_i = list1.size(); i < max_i; i++){
      item = list1.get(i);
      if (list2.contains(item)){
        result.add(item);
      }
    }

    return result;
  }
}
