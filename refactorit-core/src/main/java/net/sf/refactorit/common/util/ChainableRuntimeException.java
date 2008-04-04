/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.common.util;

import java.io.PrintStream;
import java.io.PrintWriter;


/**
 * Run-time exception allowing nested exception or cause to be specified.
 */
public final class ChainableRuntimeException extends RuntimeException {

  /** Nested exception. */
  private final Throwable cause;

  /**
   * Constructs <code>ChainableRuntimeException</code> without detail message.
   */
  public ChainableRuntimeException() {
    this.cause = null;
  }

  /**
   * Constructs <code>ChainableRuntimeException</code> with the specified
   * detail message.
   *
   * @param msg the detail message.
   */
  public ChainableRuntimeException(String msg) {
    super(msg);

    this.cause = null;
  }

  /**
   * Constructs a new <code>ChainableRuntimeException</code>
   * with the specified detail message and cause.
   *
   * @param msg the detail message.
   * @param cause the cause (which is saved for later retrieval by the
   *        {@link #getCause getCause} method).
   *        (A <code>null</code> value is permitted, and indicates that the
   *        cause is nonexistent or unknown.)
   */
  public ChainableRuntimeException(String msg, Throwable cause) {
    super(msg);

    this.cause = cause;
  }

  /**
   * Constructs a new <code>ChainableRuntimeException</code>
   * with the specified cause and a detail message of cause.
   *
   * @param msg the detail message.
   * @param cause the cause (which is saved for later retrieval by the
   *        {@link #getCause getCause} method).
   *        (A <code>null</code> value is permitted, and indicates that the
   *        cause is nonexistent or unknown.)
   */
  public ChainableRuntimeException(Throwable cause) {
    super((cause != null) ? cause.toString() : null);

    this.cause = cause;
  }

  /**
   * Prints the composite message and the embedded stack trace to the specified
   * stream.
   *
   * @param stream the print stream
   */
  public final void printStackTrace(PrintStream stream) {
    synchronized (stream) {
      super.printStackTrace(stream);
      if (getCause() != null) {
        stream.print("Caused by: ");
        getCause().printStackTrace(stream);
      }
    }
  }

  /**
   * Prints the composite message to <code>System.out</code>.
   */
  public final void printStackTrace() {
    printStackTrace(System.out);
  }

  /**
   * Prints the composite message and the embedded stack trace to the
   * specified print writer.
   *
   * @param writer the print writer
   */
  public final void printStackTrace(PrintWriter writer) {
    synchronized (writer) {
      super.printStackTrace(writer);
      if (getCause() != null) {
        writer.print("Caused by: ");
        getCause().printStackTrace(writer);
      }
    }
  }

  /**
   * Returns the cause of this throwable or <code>null</code> if the cause is
   * nonexistent or unknown.
   * (The cause is the throwable that caused this throwable to get thrown.)
   *
   * @return nested exception or <code>null</code> if no nested exception
   * specified.
   */
  public final Throwable getCause() {
    return cause;
  }
}
