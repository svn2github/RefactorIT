/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jbuilder.vfs;


import com.borland.primetime.vfs.Url;
import com.borland.primetime.vfs.VFS;

import java.io.IOException;
import java.io.InputStream;
import com.borland.primetime.vfs.Filesystem;
import com.borland.primetime.vfs.FileFilesystem;
import java.io.File;

import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.FileReadWriteUtil;
import net.sf.refactorit.vfs.AbstractClassPathElement;
import net.sf.refactorit.vfs.ClassPath;


/**
 * Local filesystem ClassPath. Do not forget to call close() after using it.
 *
 * @author  Igor Malinin
 */
public class JBClassPathElement extends AbstractClassPathElement {

  private Url root;

  /** Creates new DirClassPath */
  public JBClassPathElement(Url url) {
    root = url;
  }

  public ClassPath.Entry getEntry(String cls) {
    Url url = root.getRelativeUrl(cls);
    if (VFS.exists(url) && !VFS.isDirectory(url)) {
      return new Entry(url);
    }

    return null;
  }

  public void release() {}

  static class Entry implements ClassPath.Entry {
    private Url url;

    Entry(Url u) {
      url = u;
    }

    public boolean exists() {
      return VFS.exists(url);
    }

    public boolean delete() {
      try {
        VFS.delete(url);
        return url.getFileObject() == null || !url.getFileObject().exists();
      } catch (IOException e) {
        e.printStackTrace(System.err);
        return false;
      }
    }

    /**
     * Time of last modification.
     * Returns 0 if unknown.
     *
     * @return  last modified time
     */
    public long lastModified() {
      return VFS.getLastModified(url);
    }

    /**
     * Returns size of binary representation of a class for a given full
     * qualified class name. Returns 0 if class does not exists.
     * Names of packages and classes are delimited by slashes, inner
     * classes are delimited from containing classes by dollar sign.
     */
    public long length() {
      Filesystem fs = url.getFilesystem();
      if (fs instanceof FileFilesystem) {
        File f = url.getFileObject();
        if (f != null) {
          return f.length();
        }
      }

      // calculate
      try {
        InputStream in = VFS.getInputStream(url);
        return FileReadWriteUtil.length(in);
      } catch (IOException e) {
        AppRegistry.getExceptionLogger()
            .error(e, "Can't get length of: " + this, this);
        return 0;
      }
    }

    /**
     * Provides binary stream for a given full qualified class name.
     * Names of packages and classes are delimited by slashes, inner
     * classes are delimited from containing classes by dollar sign.
     * Returns null if nothing found
     *
     * @param cls  class name in the form com/package/Class$Inner.class
     */
    public InputStream getInputStream() {
      try {
        return VFS.getInputStream(url);
      } catch (IOException e) {
        System.err.println("Something unusual has ocurred: ");
        e.printStackTrace(System.err);
      }

      return null;
    }
  }


  public String toString() {
    return root.toString();
  }

  public Url getUrl() {
    return root;
  }

  /*
   * @see net.sf.refactorit.vfs.ClassPathElement#getName()
   */
  public String getAbsolutePath() {
    return getUrl().getFullName();
  }
}
