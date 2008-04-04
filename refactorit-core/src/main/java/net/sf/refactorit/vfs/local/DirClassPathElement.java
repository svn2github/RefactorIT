/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.vfs.local;


import net.sf.refactorit.vfs.AbstractClassPathElement;
import net.sf.refactorit.vfs.ClassPath;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Local filesystem ClassPath. Do not forget to call close() after using it.
 *
 * @author  Igor Malinin
 */
public class DirClassPathElement extends AbstractClassPathElement {
  private final File directory;

  /** Creates new DirClassPath */
  public DirClassPathElement(File dir) {
    directory = dir;
  }

  public final ClassPath.Entry getEntry(String cls) {
    File file = new File(directory, cls);
    if (file.exists() && file.isFile()) {
      return createEntry(file);
    }

    return null;
  }

  /** To be overriden in subclass to provide extra functionality */
  protected ClassPath.Entry createEntry(File file) {
    return new Entry(file);
  }

  public final void release() {}

  public static class Entry implements ClassPath.Entry {
    protected final File file;

    protected Entry(File f) {
      file = f;
    }

    public final boolean exists() {
      return file.exists();
    }

    public boolean delete() {
      return file.delete();
    }

    /**
     * Time of last modification.
     * Returns 0 if unknown.
     *
     * @return  last modified time
     */
    public final long lastModified() {
      return file.lastModified();
    }

    /**
     * Returns size of binary representation of a class for a given full
     * qualified class name. Returns 0 if class does not exists.
     * Names of packages and classes are delimited by slashes, inner
     * classes are delimited from containing classes by dollar sign.
     */
    public final long length() {
      return file.length();
    }

    /**
     * Provides binary stream for a given full qualified class name.
     * Names of packages and classes are delimited by slashes, inner
     * classes are delimited from containing classes by dollar sign.
     * Returns null if nothing found
     */
    public final InputStream getInputStream() {
      try {
        return new FileInputStream(file);
      } catch (IOException e) {
        System.err.println("Something unusual has ocurred:");
        e.printStackTrace(System.err);
      }

      return null;
    }
  }


  public final String toString() {
    return getAbsolutePath();
  }

  public final File getFile() {
    return directory;
  }

  /* (non-Javadoc)
   * @see net.sf.refactorit.vfs.ClassPathElement#getName()
   */
  public final String getAbsolutePath() {
    return directory.getAbsolutePath();
  }
}
