/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.common.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class BidirectionalMap {
  HashMap mapKeyToValue;
  HashMap mapValueToKey;
  
  public BidirectionalMap() {
    mapKeyToValue = new HashMap();
    mapValueToKey = new HashMap();
  }
  
  public BidirectionalMap(int initialCapacity) {
    mapKeyToValue = new HashMap(initialCapacity);
    mapValueToKey = new HashMap(initialCapacity);
  }
  
  public BidirectionalMap(int initialCapacity, float loadFactor) {
    mapKeyToValue = new HashMap(initialCapacity, loadFactor);
    mapValueToKey = new HashMap(initialCapacity, loadFactor);
  }
  
  public void put(Object key, Object value) {
    // determine what key and values to delete
    Object keyToDelete = mapValueToKey.get(value);
    Object valueToDelete = mapKeyToValue.get(key);
    // delete keys and values
    mapKeyToValue.remove(keyToDelete);
    mapValueToKey.remove(valueToDelete);
    // update with new keys and values
    mapKeyToValue.put(key, value);
    mapValueToKey.put(value, key);
  }
  
  public void putAll(BidirectionalMap from){
    mapKeyToValue.putAll(from.getKeyToValueMap());
    mapValueToKey.putAll(from.getValueToKeyMap());
  }
  
  public Map getKeyToValueMap(){
    return mapKeyToValue;
  }
  
  public Map getValueToKeyMap(){
    return mapValueToKey;
  }
  
  public Object getValueByKey(Object key) {
    return mapKeyToValue.get(key);
  }
  
  public Object getKeyByValue(Object value) {
    return mapValueToKey.get(value);
  }
  
  public void removeByKey(Object key) {
   Object value = mapKeyToValue.get(key);
   mapKeyToValue.remove(key);
   mapValueToKey.remove(value);
  }
  
  public void removeByValue(Object value) {
    Object key = mapValueToKey.get(value);
    mapKeyToValue.remove(key);
    mapValueToKey.remove(value); 
  }

  public Set getKeySet(){
    return mapKeyToValue.keySet();
  }
  
  public Set getValueSet(){
    return mapValueToKey.keySet();
  }
  
  public Set getKeySetCopy() {
    return ((BidirectionalMap)clone()).mapKeyToValue.keySet();
  }
  
  public Set getValueSetCopy() {
    return ((BidirectionalMap)clone()).mapValueToKey.keySet();
  }
  
  public int size() {
    return mapKeyToValue.size();
  }
  
  public final void clear() {
    mapKeyToValue.clear();
    mapValueToKey.clear();
  }
  
  public String toString(){
    return "BidirectionalMap(" + hashCode() + ") " + mapValueToKey.toString();
  }

  public Object clone() {
    BidirectionalMap map = new BidirectionalMap(size(), 1f);
    map.mapKeyToValue.putAll(this.mapKeyToValue);
    map.mapValueToKey.putAll(this.mapValueToKey);
    return map;
  }
}
