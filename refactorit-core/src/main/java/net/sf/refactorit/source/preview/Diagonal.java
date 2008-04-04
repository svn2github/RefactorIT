/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.preview;

public class Diagonal {

  public int x1 = -1, y1 = -1;
  public int x2 = -1, y2 = -1;
  private boolean empty =  true;

  public String toString() {

    return y1 + ":" + x1 + " - " + y2 + ":" + x2;
  }

  private int cost = 0;
  public int cost() {
    if (cost==0) {
      cost += 2*(x2 - x1);
    }
    return cost;
  }

  public int costX() {

    return x2-x1+1;
  }

  public int costY() {

    return y2-y1+1;
  }

  public void setPoint(int y, int x) {
    empty = false;

    if (x1 == -1 || y1 == -1) {
      x1 = x;
      y1 = y;
      x2 = x;
      y2 = y;
    }
    else {
      x2 = x;
      y2 = y;
    }
  }

  public boolean isEmpty() {
    return this.empty;
  }

  public void setEmpty(final boolean empty) {
    this.empty = empty;
  }


}
