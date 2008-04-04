/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.parser;

public interface ErrorListener {

  /** NOTE: Sometimes line nr may simply point uninformatively to the beginning
   * of file (ANTLR's problem?)
   */
  void onError(String message, String fileName, int line, int column);

  boolean hadErrors();

}
