/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.vfs.local;

import net.sf.refactorit.vfs.ClassPath;
import net.sf.refactorit.vfs.JavadocPath;


/**
 * @author Anton Safonov
 */
public final class LocalJavadocPath extends JavadocPath {
  private final String[] paths;

  public LocalJavadocPath(String paths) {
    this.paths = split(paths);
  }

  public LocalJavadocPath(ClassPath classPath) {
    // TODO write correctly
    this.paths = split(classPath.getStringForm());
  }

  public final String[] getElements() {
    return this.paths;
  }
}
