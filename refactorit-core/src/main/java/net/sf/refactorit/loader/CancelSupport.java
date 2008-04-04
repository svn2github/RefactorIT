/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.loader;

/**
 *
 *
 * @author Tonis Vaga
 */
public final class CancelSupport {
  /**
   * Checks if interrupted flag is set for current thread
   * and stops thread if it is.
   *
   * @throws {@link CanceledException}
   */
  public static final void checkThreadInterrupted() {
    // check and clear status
    if (Thread.interrupted()) {
//    try {
      // System.out.println("stopping thread with thread.stop()");
      // FIXME: <code>Thread.stop()</code> is deprecated.
      // Thread.currentThread().stop();
      throw new CanceledException("RIT interrupted");
//    } catch(Exception e) {
//      DebugInfo.traceException(e);
//    }
    }
  }
}
