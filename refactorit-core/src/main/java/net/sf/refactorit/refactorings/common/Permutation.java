/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.common;


import net.sf.refactorit.common.util.Assert;

import java.util.Arrays;


/**
 * @author Tonis Vaga
 */
public class Permutation {

  int perm[];

  public Permutation(int[] is) {
    perm = is;
  }

  public void apply(Permutation permut) {
    Assert.must(permut.size() == this.size());

    for (int i = 0; i < size(); i++) {
      perm[i] = permut.perm[perm[i]];
    }
  }

  public int size() {
    return perm.length;
  }

  public Permutation(int size) {
    perm = new int[size];

    identity();
  }

  /**
   * make identity permutation
   */
  public void identity() {
    for (int i = 0; i < perm.length; i++) {
      perm[i] = i;
    }
  }

  /**
   * add element to the end of this permutation with identity mapping
   */
  public void addElement() {
    int newPerm[] = new int[perm.length + 1];

    System.arraycopy(perm, 0, newPerm, 0, perm.length);

    final int lastIndex = newPerm.length - 1;
    newPerm[lastIndex] = lastIndex;

    perm = newPerm;
  }

  /**
   * Change permutation elements
   * @param from
   * @param to
   */
  public void reorder(int from, int to) {
    //Assert.must( mapFrom < size() &&  mapTo < size() );
    int temp = perm[from];

    perm[from] = to;
    perm[to] = temp;
  }

  /**
   * add element mapping to concrete index
   * @param parIndex
   */
  public void addElement(int parIndex) {
    addElement();
    reorder(size() - 1, parIndex);
  }

  /**
   *  Create new Permutation wherer mapping for element with index is deleted
   *  postcond: this.size() == result.size()+1
   */
  public Permutation deleteElement(int index) {
    int size = size();
    int[] newPerm = new int[size - 1];

    for (int i = 0, j = 0; i < perm.length; i++) {
      if (i == index) {
        continue;
      }
      newPerm[j++] = perm[i];
    }
    return new Permutation(newPerm);
  }

  public boolean equals(Object obj) {
    Permutation permutation = (Permutation) obj;

    if (permutation.size() != size()) {
      return false;
    }

    return this.perm.equals(permutation);
  }

  public boolean isIdentity() {
    return this.equals(new Permutation(size()));
  }

  public int getIndex(int i) {
    return perm[i];
  }

  public void setIndex(int i, int val) {
    Assert.must(val == -1 || val >= 0 && val < size());
    perm[i] = val;
  }

  /**
   * Returns reverse mapping, result.getIndex(i)=i or result.getIndex()== -1
   * if this.getIndex(i)==-1
   *
   */
  public Permutation reverse() {
    int newOrder[] = new int[size()];
    Arrays.fill(newOrder, -1);

    for (int i = 0; i < size(); i++) {
      if (getIndex(i) != -1) {
        newOrder[getIndex(i)] = i;
      }
    }
    return new Permutation(newOrder);
  }

  public String toString() {
    StringBuffer result = new StringBuffer("[");
    for (int i = 0; i < perm.length; i++) {
      result.append(i);
      if (i < perm.length - 1) {
        result.append(", ");
      }
    }
    result.append("]");
    return result.toString();
  }
}
