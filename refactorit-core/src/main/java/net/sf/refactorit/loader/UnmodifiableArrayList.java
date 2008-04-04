/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.loader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;


/**
 * @author Sander Magi
 * @author Igor Malinin
 */
public final class UnmodifiableArrayList extends ArrayList {
  final List list;

  public UnmodifiableArrayList(List list) {
    super(list);
    this.list = list;
  }

  public boolean equals(Object o) {
    return list.equals(o);
  }

  public int hashCode() {
    return list.hashCode();
  }

  public Object get(int index) {
    return list.get(index);
  }

  public Object set(int index, Object element) {
    throw new UnsupportedOperationException();
  }

  public void add(int index, Object element) {
    throw new UnsupportedOperationException();
  }

  public Object remove(int index) {
    throw new UnsupportedOperationException();
  }

  public int indexOf(Object o) {
    return list.indexOf(o);
  }

  public int lastIndexOf(Object o) {
    return list.lastIndexOf(o);
  }

  public boolean addAll(int index, Collection c) {
    throw new UnsupportedOperationException();
  }

  public ListIterator listIterator() {
    return listIterator(0);
  }

  public void clear() {}

  public ListIterator listIterator(final int index) {
    return new ListIterator() {
      final ListIterator i = list.listIterator(index);

      public boolean hasNext() {
        return i.hasNext();
      }

      public Object next() {
        return i.next();
      }

      public boolean hasPrevious() {
        return i.hasPrevious();
      }

      public Object previous() {
        return i.previous();
      }

      public int nextIndex() {
        return i.nextIndex();
      }

      public int previousIndex() {
        return i.previousIndex();
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }

      public void set(Object o) {
        throw new UnsupportedOperationException();
      }

      public void add(Object o) {
        throw new UnsupportedOperationException();
      }
    };
  }

  public List subList(int fromIndex, int toIndex) {
    return new UnmodifiableArrayList(list.subList(fromIndex, toIndex));
  }
}
