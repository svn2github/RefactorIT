/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.common.util;

/**
 * A very special ArrayList for storing shorts.
 * Optimized for very specific use - look at the implementation before using
 * Ment for Single threaded usage
 */
public final class ShortArrayList implements java.io.Serializable {
  int count = 0;
  short[] data;

  public ShortArrayList(final int initialsize) {
    data = new short[initialsize];
  }

  public final void add(final short addable) {
    if (count >= data.length) {
      final short newdata[] = new short[count << 1];
      System.arraycopy(data, 0, newdata, 0, count);
      data = newdata;
    }
    data[count++] = addable;
  }

  public final short get(final int index) {
    return data[index];
  }

  public final void set(final int index, final short datum) {
    data[index] = datum;
  }

  public final int size() {
    return this.count;
  }

  public final short[] toShortArray() {
    final short newdata[] = new short[count];
    System.arraycopy(data, 0, newdata, 0, count);
    return newdata;
  }
}
