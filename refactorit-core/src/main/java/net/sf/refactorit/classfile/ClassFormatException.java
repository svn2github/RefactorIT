/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classfile;

import java.io.PrintWriter;
import java.io.StringWriter;


public final class ClassFormatException extends Exception {
  public Throwable throwable;

  public ClassFormatException(Throwable t) {
    super(makeStackTrace(t));
    throwable = t;
  }

  private static String makeStackTrace(Throwable t) {
    StringWriter sw = new StringWriter();
    t.printStackTrace(new PrintWriter(sw));
    return sw.toString();
  }

  public ClassFormatException(String message) {
    super(message);
  }
}
