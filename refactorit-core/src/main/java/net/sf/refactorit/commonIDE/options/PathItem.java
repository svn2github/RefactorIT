/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.commonIDE.options;

import net.sf.refactorit.vfs.ClassPathElement;
import net.sf.refactorit.vfs.Source;

import java.io.File;



public class PathItem {
  public String path;

  public PathItem(String localPath) {
    this.path = localPath;
  }
  public PathItem(File file) {
    path = file.getAbsolutePath();
  }
  public PathItem(Source src) {
    path = src.getAbsolutePath();
  }
  
  public PathItem(ClassPathElement el) {
    path = el.getAbsolutePath();
  }

  public String getAbsolutePath() {
    return path;
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof PathItem)) {
      return false;
    }
    PathItem item = (PathItem) obj;
    return this.path.equals(item.path);
  }

  public int hashCode() {
    return path.hashCode();
  }

  public String toString() {
    return path;
  }
}
