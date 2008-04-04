/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.vfs;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * @author Anton Safonov
 */
public class CompoundClassPath implements ClassPath {
  private ClassPath[] paths;

  public CompoundClassPath(ClassPath[] paths) {
    this.paths = paths;
  }

  public ClassPath[] getPaths() {
    return this.paths;
  }

  public boolean contains(String aClass) {
    for (int i = 0, max = paths.length; i < max; i++) {
      if (paths[i].contains(aClass)) {
        return true;
      }
    }
    return false;
  }

  public boolean delete(String cls) {
    for (int i = 0, max = paths.length; i < max; i++) {
      if (paths[i].delete(cls)) {
        return true;
      }
    }
    return false;
  }

  public boolean exists(String cls) {
    for (int i = 0, max = paths.length; i < max; i++) {
      if (paths[i].exists(cls)) {
        return true;
      }
    }
    return false;
  }

  public ClassPathElement[] getAutodetectedElements() {
    ArrayList result = new ArrayList();
    for (int i = 0, max = paths.length; i < max; i++) {
      result.addAll(Arrays.asList(paths[i].getAutodetectedElements()));
    }
    return (ClassPathElement[]) result.toArray(new ClassPathElement[result.size()]);
  }

  public InputStream getInputStream(String cls) {
    for (int i = 0, max = paths.length; i < max; i++) {
      InputStream in = paths[i].getInputStream(cls);
      if (in != null) {
        return in;
      }
    }
    return null;
  }

  public String getStringForm() {
    StringBuffer buf = new StringBuffer();
    for (int i = 0, max = paths.length; i < max; i++) {
      buf.append(paths[i].getStringForm()).append(File.pathSeparatorChar);
    }
    return buf.toString();
  }

  public boolean isAnythingChanged() {
    boolean changed = false;
    for (int i = 0, max = paths.length; i < max; i++) {
      if (paths[i].isAnythingChanged()) {
        changed = true;
      }
    }
    return changed;
  }

  public long lastModified(String cls) {
    for (int i = 0, max = paths.length; i < max; i++) {
      long lastModif = paths[i].lastModified(cls);
      if (lastModif > 0) {
        return lastModif;
      }
    }
    return 0;
  }

  public long length(String cls) {
    for (int i = 0, max = paths.length; i < max; i++) {
      long len = paths[i].length(cls);
      if (len > 0) {
        return len;
      }
    }
    return 0;
  }

  public void release() {
    for (int i = 0, max = paths.length; i < max; i++) {
      paths[i].release();
    }
  }
}
