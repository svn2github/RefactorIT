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
 * A very special ArrayList for storing bytes.
 * Optimized for very specific use - look at the implementation before using
 * Ment for Single threaded usage
 */
public final class ByteArrayList implements java.io.Serializable {
  private int count = 0;
  private byte[] data;

  public ByteArrayList(final int initialsize) {
    data = new byte[initialsize];
  }

  public final void add(final byte addable) {
    if (count >= data.length) {
      final byte newdata[] = new byte[count << 1];
      System.arraycopy(data, 0, newdata, 0, count);
      data = newdata;
    }
    data[count++] = addable;
  }

  public final byte get(final int index) {
    return data[index];
  }

  public final void set(final int index, final byte datum) {
    data[index] = datum;
  }

  public final int size() {
    return this.count;
  }

  public final byte[] toByteArray() {
    final byte newdata[] = new byte[count];
    System.arraycopy(data, 0, newdata, 0, count);
    return newdata;
  }
}
