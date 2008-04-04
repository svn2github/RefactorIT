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
 * Insert the type's description here.
 * Creation date: (5/23/2001 3:34:22 AM)
 * @author Igor Malinin
 */
public final class SourcePath extends Path {
  public static final SourcePath EMPTY = new SourcePath();
  /**
   * ClassPath constructor comment.
   * @param path java.lang.String
   */
  private SourcePath() {
    super("");
  }

  /**
   * ClassPath constructor comment.
   * @param path java.lang.String
   */
  public SourcePath(String path) {
    super(path);
  }
}
