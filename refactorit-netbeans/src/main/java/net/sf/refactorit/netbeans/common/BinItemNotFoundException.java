/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common;

/** Thrown when a BinItem could not be located for some given NetBeans Element. */
public class BinItemNotFoundException extends Exception {
  public BinItemNotFoundException() {
    super();
  }
  
  public BinItemNotFoundException(String message) {
    super(message);
  }
}
