/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.options;

/** @author Anton Safonov */
public final class JavadocPath extends Path {
  public static final JavadocPath EMPTY = new JavadocPath();

  private JavadocPath() {
    super("");
  }

  public JavadocPath(String path) {
    super(path);
  }
}
