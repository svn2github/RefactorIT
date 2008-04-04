/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.common.util;

import java.util.EmptyStackException;

/**
 * @author Anton Safonov
 */
public final class FastStack {
  private Object[] data;
  private int size = 0;

  public FastStack() {
    this(10);
  }

  public FastStack(final int capacity) {
    data = new Object[capacity];
  }

  public boolean isEmpty() {
    return size == 0;
  }

  public boolean empty() {
    return size == 0;
  }

  public int size() {
    return size;
  }

  public void clear() {
    this.data = new Object[this.size]; // drop old content to help GC
    this.size = 0;
  }

  public Object push(final Object item) {
    ensureCapacity(size + 1);
    data[size++] = item;
    return item;
  }

  public void ensureCapacity(final int minCapacity) {
    final int oldCapacity = data.length;
    if (minCapacity > oldCapacity) {
      Object oldData[] = data;
      int newCapacity = (oldCapacity * 3) / 2 + 1;
      if (newCapacity < minCapacity) {
        newCapacity = minCapacity;
      }
      data = new Object[newCapacity];
      System.arraycopy(oldData, 0, data, 0, size);
    }
  }

  public Object pop() {
    --size;
    final Object obj = data[size];
    data[size] = null;

    return obj;
  }

  public Object peek() {
    if (size == 0) {
      throw new EmptyStackException();
    }
    return data[size - 1];
  }

  public Object get(final int index) {
    return data[index];
  }

  public int search(final Object elem) {
    if (elem == null) {
      for (int i = size; --i >= 0; ) {
        if (data[i] == null) {
          return i;
        }
      }
    } else {
      for (int i = size; --i >= 0; ) {
        if (elem.equals(data[i])) {
          return i;
        }
      }
    }

    return -1;
  }
}
