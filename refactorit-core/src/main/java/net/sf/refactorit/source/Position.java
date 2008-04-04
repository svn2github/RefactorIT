/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source;

public final class Position implements Comparable {

  /* Starts with 0 */
  private int start = -1;

  private int end = -1;

  public Position(int start, int end) {
    this.start = start;
    this.end = end;
  }

  public String toString() {
    return getClass().getName() + "; start=" + getStart() + ", end=" + getEnd();
  }

  public int getStart() {
    return this.start;
  }

  public int getEnd() {
    return this.end;
  }

  public void setStart(int start) {
    this.start = start;
  }

  public void setEnd(int end) {
    this.end = end;
  }

  public int compareTo(Object o) {
    return -1; // TODO
  }

  public boolean overlaps(final Position other) {
    return this.start < other.start && this.start < other.end
        && this.end >= other.start && this.end <= other.end;
  }

  public boolean contains(final Position inner) {
    return this.start <= inner.start && this.end >= inner.end;
  }

  public boolean merge(Position other) {
    if (this.contains(other)) {
      return true;
    }

    if (this.overlaps(other) || other.overlaps(this)) {
      if (this.start >= other.start && this.start <= other.end) {
        this.start = other.start;
      }
      if (this.end >= other.start && this.end <= other.end) {
        this.end = other.end;
      }

      return true;
    }

    return false;
  }

}
