/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.common.util;


public final class Assert {
  public static final boolean enabled = false;

  public static void must(boolean flag) {
    if (enabled) {
      must(flag, "");
    }
  }

  public static void must(boolean flag, String message) {
    if (!flag) {
      throw new AssertionException(message);
    }
  }

  public static final void must(boolean flag, String message, Object node) {
    if (!flag) {
      message += " [Node: " + node + "]";
      throw new AssertionException(message);
    }
  }
}
