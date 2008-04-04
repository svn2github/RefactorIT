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
 * <p>Title: </p>
 * <p>Description: Simple class for interval tracking</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Aqris Software AS</p>
 * @author Tonis Vaga
 * @version 1.0
 */

public final class TimeTracker {

  long start = 0;
  long end = 0;
  public TimeTracker() {
  }

  public final void start() {
    start = System.currentTimeMillis();
  }

  public final void end() {
    end = System.currentTimeMillis();
  }

  public final long difference() {
    return end - start;
  }

  public final String toString() {
    return difference() + " mms";
  }

  public final void trace(String string) {
    System.out.println(string + " took " + difference());
  }
}
