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
import java.util.Set;


/**
 * A special MultiValueMap, that is optimized for cases, when there is much more
 * single value leaves and only a few multiValue leaf.<br>
 * BinCIType.accessibleMethodsMap statistics (size of a bucket - amount of such
 * buckets):<br>
 * 1 - 64112, 2 - 4872, 3 - 1729, 4 - 106, 5 - 276, 6 - 159, 7 - 7, 8 - 3,
 * 9 - 107, 10 - 7, 11 - 1, 18 - 83, 51 - 22, 52 - 36, 53 - 26
 *
 * @author Sander M?gi
 * @author Anton Safonov
 */
public final class AdaptiveMultiValueMap {
  private HashMap map = null;
  private int valuesAdded = 0;

  public static final AdaptiveMultiValueMap EMPTY_MAP = new AdaptiveMultiValueMap(0);

  public AdaptiveMultiValueMap() {
    map = new HashMap();
  }

  public AdaptiveMultiValueMap(final int capacity) {
    map = new HashMap(capacity);
  }

  /**
   * Puts this mapping key-object only if it is not yet in the map.
   */
  public final void putNew(final Object key, final Object value) {
    if (!contains(key, value)) {
      put(key, value);
    }
  }

  public final void put(final Object key, final Object value) {
    final Object oldValue = map.get(key);

    if (oldValue != null) {
      final Collection collection;
      if (oldValue instanceof Collection) {
        collection = (Collection) oldValue;
      } else {
        // replace single value with List
        collection = new ArrayList(3);
        collection.add(oldValue);
        map.put(key, collection);
      }

      if (value instanceof Collection) {
        Collection valueCollection = (Collection) value;
        collection.addAll(valueCollection);
        valuesAdded += valueCollection.size();
      } else {
        collection.add(value);
        ++valuesAdded;
      }
    } else {
      map.put(key, value);
      ++valuesAdded;
    }
  }

  public final void remove(final Object key) {
    Object removed = map.remove(key);
    if (removed instanceof Collection) {
      valuesAdded -= ((Collection) removed).size();
    } else if (removed != null) {
      --valuesAdded;
    }
  }

  public final void compact() {
    for (Iterator i = map.values().iterator(); i.hasNext(); ) {
      final Object o = i.next();
      if (o instanceof ArrayList) {
        ((ArrayList) o).trimToSize();
      }
    }
  }

  public final boolean contains(final Object key) {
    return this.map.containsKey(key);
  }

  public final boolean contains(final Object key, final Object value) {
    final Object foundValue = this.map.get(key);
    if (foundValue == null) {
      return false;
    }

    if (value == foundValue) {
      return true;
    }

    if (foundValue instanceof Collection) {
      return ((Collection) foundValue).contains(value);
    }

    return false;
  }

  public final Iterator valuesIterator() {
    return new CollectionIterator(this, map.values());
  }

  /**
   * @return null if no elements
   */
  public final Iterator findIteratorFor(final Object key) {
    final Object obj = map.get(key);
    if (obj == null) {
      return null;
    }
    return new AdaptiveIterator(this, key, obj);
  }

  public final Iterator iteratorFor(final Object key) {
    return new AdaptiveIterator(this, key, map.get(key));
  }

  public final void clear() {
    map.clear();
    valuesAdded = 0;
  }

  public final boolean isEmpty() {
    return map.isEmpty();
  }

  public final Set keySet() {
    return map.keySet();
  }

  /** Didn't want to name it size, since it may not be exact. */
  public final int getValuesAdded() {
    return this.valuesAdded;
  }

  private static final class AdaptiveIterator implements Iterator {
    private int cur, max;
    private Object value;

    /** to support remove, we need to be able to remove key from the map */
    private final AdaptiveMultiValueMap map;
    private final Object key;

    public AdaptiveIterator(final AdaptiveMultiValueMap map, final Object key,
        final Object value) {
      init(value);
      this.map = map;
      this.key = key;
    }

    public void init(final Object value) {
      this.value = value;
      this.cur = 0;
      if (this.value == null) {
        this.max = 0;
      } else if (this.value instanceof List) {
        this.max = ((List)this.value).size();
      } else {
        this.max = 1;
      }
    }

    public final boolean hasNext() {
      return cur < max;
    }

    public final Object next() {
      if (max <= 1) {
        ++cur;
        return value;
      } else {
        return ((List) value).get(cur++);
      }
    }

    public final void remove() {
      if (this.key == null) {
        throw new RuntimeException("key is null");
      } else if (this.cur == 0) {
        throw new RuntimeException("remove() called before next()");
      }

      if (max <= 1) {
        this.map.remove(this.key);
      } else {
        ((List) value).remove(cur - 1);
      }
    }

  }


  private final class CollectionIterator implements Iterator {
    private final Iterator iterator;
    private final AdaptiveIterator elementIterator;

    private CollectionIterator(final AdaptiveMultiValueMap map,
        final Collection collection) {
      this.iterator = collection.iterator();
      elementIterator = new AdaptiveIterator(map, null, null);
    }

    public boolean hasNext() {
      if (!this.elementIterator.hasNext() && this.iterator.hasNext()) {
        this.elementIterator.init(this.iterator.next());
      }

      return this.elementIterator.hasNext();
    }

    public Object next() {
      return this.elementIterator.next();
    }

    public void remove() {
      throw new RuntimeException("Not implemented");
    }

  }

}
