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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Useful when you need to map one key to several values, in this case this
 * class stores values in {@link List Lists} in the {@link Map}.
 *
 * @author Anton Safonov
 */
public final class MultiValueMap {

  private Map map = new HashMap();

  public MultiValueMap() {
    map = new HashMap();
  }

  public MultiValueMap(final int initialCapacity) {
    map = new HashMap(initialCapacity, 0.75f);
  }

  public final boolean containsKey(final Object key) {
    return map.containsKey(key);
  }

  public final boolean contains(final Object key, final Object value) {
    final List list = (List) map.get(key);
    if (list != null) {
      return list.contains(value);
    }

    return false;
  }
  
  public final boolean contains(final Object key) {
    for(Iterator it = map.values().iterator(); it.hasNext();) {
      List list = (List) it.next();
      if(list.contains(key)) {
        return true;
      }
    }
    return false;
  }  

  public final void clearKey(final Object key) {
    map.remove(key);
  }

  public final List get(final Object key) {
    return (List) map.get(key);
  }

  public final boolean putNew(final Object key, final Object value) {
    if (!contains(key, value)) {
      return putAll(key, value);
    }

    return false;
  }
  /**
   * If value is collection adds value using map.putAll()
   * Deprecated method, use {@link #putAll(Object, Collection)} instead!!
   * @param key
   * @param value
   */
  public final boolean putAll(final Object key, final Object value) {
    if ( value instanceof Collection ) {
      return putAll(key,(Collection)value);
    }

    return put(key, value);
  }
  /**
   * Adds value to key values list
   * @param key
   * @param value
   */
  public final boolean put(final Object key, final Object value) {
    List list = (List) map.get(key);

    if (list == null) {
      list = new ArrayList(1);
      map.put(key, list);
    }

    if (list.contains(value)) {
      return false;
    }

    list.add(value);

    return true;
  }

  public final boolean putAll(final Object key, final Collection values) {
    boolean result = false;
    List list = (List) map.get(key);
    if (list == null) {
      list = new ArrayList(values);
      if(!list.isEmpty()) {
        result = true;
        map.put(key, list);
      }
    } else {
      /*if (list.containsAll(values)) {
        return false;
      }*/
      result |= list.addAll(values);
    }

    return result;
  }


  public final void putAll(final MultiValueMap other) {
    this.map.putAll(other.map);
  }

  public final boolean remove(final Object key, final Object value) {
    final List list = (List) map.get(key);
    if (list != null) {
      final boolean result = list.remove(value);
      if (list.isEmpty()) {
        map.remove(key);
      }
      return result;
    }

    return false;
  }
  
  public final void removeKeys(final Collection keys) {
    if(keys == null) {
      return;
    }
    for(Iterator it = keys.iterator(); it.hasNext(); ) {
      map.remove(it.next());
    }
  }

  public final boolean removeAll(final MultiValueMap m){
    boolean result = false;
    if (m != null){
      for(Iterator it = m.keySet().iterator();it.hasNext();){
        Object key = it.next();
        List removeList = (List)m.get(key);
        List srcList = (List)map.get(key);
        result |= srcList.removeAll(removeList);
      }
    }
    return result;
  }
  
  public final boolean removeAll(final Object key, Collection values){
    boolean result = false;
    if (key != null && values != null){
      List list = (List)map.get(key);
      if(list != null) {
        result = list.removeAll(values);
        if(list.isEmpty()) {
          map.remove(key);
        }
      }
    }
    return result;
  }

  public final Set keySet() {
    return map.keySet();
  }

  public final Set entrySet() {
    return map.entrySet();
  }

  public final List values() {
    final List results = new ArrayList();

    final Iterator it = map.entrySet().iterator();
    while (it.hasNext()) {
      results.addAll(((List) ((Map.Entry) it.next()).getValue()));
    }

    return results;
  }

  public final void clear() {
    map.clear();
  }

  public final boolean isEmpty() {
    return map.isEmpty();
  }

  /**
   * Frees unneeded memory - use when you have done adding the elements to this Map
   */
  public final void compact() {
    // NOTE: has ArrayList dependency
    for (Iterator i = map.values().iterator(); i.hasNext(); ) {
      final ArrayList aList = (ArrayList) i.next();
      aList.trimToSize();
    }
  }

  public final String toString() {
    String result = "";

    final Iterator keys = keySet().iterator();
    while (keys.hasNext()) {
      final Object key = keys.next();
      final List values = get(key);
      result += "key: " + key + ", values: " + values + "\n";
    }

    return result;
  }

  public int size() {
    return map.size();
  }
}
