/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.memory;

import java.text.Format;
import java.text.MessageFormat;


public class MemoryTrack {
  public static final boolean enabled = true;
  public static final boolean isMemoryDebugMode = false;
  public static boolean runFinalization = true;

  private static long lastMemory = 0;

  private static final Format format = new MessageFormat("POINT: {0}\n  " +
      "Memory used = {1,number,##,###} Diff = {2,number,##,###}\n");

  private static void forceGC() {
    try {
      System.runFinalization();
      System.gc();
      Thread.sleep(3000);
      System.runFinalization();
      System.gc();
      Thread.sleep(2000);
      System.runFinalization();
      System.gc();
      Thread.sleep(1000);
    } catch (Exception e) {
      // ignore
    }
  }

  public static void makeMeasurment(String pointName) {
    if (!enabled) {
      return;
    }
    if (runFinalization) {
      forceGC();
    }
    Runtime r = Runtime.getRuntime();
    long memory = (r.totalMemory() - r.freeMemory());
    long difference = (memory - lastMemory);
    lastMemory = memory;

    System.err.print(
        format.format(
        new Object[] {pointName, new Long(memory), new Long(difference)}
        ));
  }
}
