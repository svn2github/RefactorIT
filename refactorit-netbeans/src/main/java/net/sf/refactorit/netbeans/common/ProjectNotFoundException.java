/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common;

public class ProjectNotFoundException extends NullPointerException {
  /**
   * Constructs a <code>ProjectNotFoundException</code> with no detail message.
   */
  public ProjectNotFoundException() {
	super();
  }

  /**
   * Constructs a <code>ProjectNotFoundException</code> with the specified 
   * detail message. 
   *
   * @param   s   the detail message.
   */
  public ProjectNotFoundException(String s) {
	super(s);
  }
}
