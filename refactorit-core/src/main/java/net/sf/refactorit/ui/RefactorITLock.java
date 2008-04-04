/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui;

import net.sf.refactorit.common.util.Assert;


/**
 *  <p>Description: Class for RIT actions synchronization. Needs refactoring and common solutions.
 *  There were
 *  problems with JB8.0 synchronization<p>
 *  <code>synchronize(object)</code> didn't work.
 *
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Aqris Software AS</p>
 * @author Tonis Vaga
 * @version 1.0
 */

public final class RefactorITLock {
  private static Object instance = new Object();

  public static Object getInstance() {
    return instance;
  }

  private static int count = 0;
  public static synchronized boolean lock() {
    Assert.must(count >= 0 && count < 2);
    if (count == 0) {
      ++count;
//      DebugInfo.trace("RIT lock obtained");
      return true;
    } else {
//      DebugInfo.trace("RIT lock not obtained");
      return false;
    }
  }

  public static boolean isLocked() {
    return count > 0;
  }

  public static synchronized void unlock() {
    if (count > 0) {
//      DebugInfo.trace("RIT lock released");
      --count;
    } else {
//      DebugInfo.trace("calling unlock when lock wasn't set");
    }
  }
}
