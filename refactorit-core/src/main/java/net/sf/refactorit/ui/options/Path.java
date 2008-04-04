/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.options;


/**
 * Type safe wrapper for sourcepath/classpath string.
 *
 * @author Igor Malinin
 */
public class Path {
  
  public String path;
  
  public Path( String path ) {
    this.path = path==null?"":path;
  }
  
  public String toString() {
    return path;
  }
}
