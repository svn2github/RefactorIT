/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.utils;

/**
 * The exception objects of this class is thrown by the functions which are
 * dealing with initializations of objects in this system.
 *
 * Throw this class object every time you intialize some object but cannot
 * finish the initialization or cannot bring some object
 * into working state for some reason.
 *
 * @author  jaanek
 */
public final class InitializationException extends RefactorItException {

  /**
   * Creates a new instance of <code>InitializationException</code>
   * without detail message.
   */
  public InitializationException() {
  }

  /**
   * Constructs an instance of <code>InitializationException</code>
   * with the specified detail message.
   *
   * @param msg the detail message.
   */
  public InitializationException(String msg) {
    super(msg);
  }

  /**
   * Construct an InitializationException with specified detail message
   * and nested Throwable.
   *
   * @param msg The detail message.
   * @param nested the exception or error that caused this exception
   *               to be thrown.
   */
  public InitializationException(String msg, Throwable t) {
    super(msg, t);
  }

}
