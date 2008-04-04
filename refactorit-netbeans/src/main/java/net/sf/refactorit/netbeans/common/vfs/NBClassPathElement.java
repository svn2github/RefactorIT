/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.vfs;


import net.sf.refactorit.netbeans.common.projectoptions.FileObjectUtil;
import net.sf.refactorit.vfs.AbstractClassPathElement;
import net.sf.refactorit.vfs.ClassPath;

import org.apache.log4j.Logger;

import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.UserQuestionException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


/**
 * Local filesystem ClassPath. Do not forget to call close() after using it.
 *
 * @author  Igor Malinin
 */
public class NBClassPathElement extends AbstractClassPathElement {
  private static final Logger log = Logger.getLogger(NBClassPathElement.class);
  
  private FileSystem filesystem;

  /** Creates new DirClassPath */
  public NBClassPathElement(FileSystem fs) {
    filesystem = fs;
  }

  public ClassPath.Entry getEntry(String cls) {
    FileObject fo = filesystem.findResource(cls);
    if (fo != null && fo.isData()) {
      return new Entry(fo);
    }

    return null;
  }

  public void release() {}

  public FileSystem getFileSystem() {
    return filesystem;
  }

  static class Entry implements ClassPath.Entry {
    private FileObject file;

    Entry(FileObject fo) {
      file = fo;
    }

    public boolean exists() {
      return file.isValid() && (file.isData() || file.isFolder());
    }

    public boolean delete() {
      if (!file.isValid()) {
        return true;
      }

      FileLock lock = null;
      try {
        try {
          lock = file.lock();
        } catch (UserQuestionException e) {
          e.confirmed();
        }
        if (lock != null) {
          file.delete(lock);
        }
      } catch (IOException e) {
        log.warn(e.getMessage(), e);
      } finally {
        if (lock != null) {
          lock.releaseLock();
          lock = null;
        }
      }

      return!file.isValid();
    }

    /**
     * Time of last modification.
     * Returns 0 if unknown.
     *
     * @return  last modified time
     */
    public long lastModified() {
      return file.lastModified().getTime();
    }

    /**
     * Returns size of binary representation of a class for a given full
     * qualified class name. Returns 0 if class does not exists.
     * Names of packages and classes are delimited by slashes, inner
     * classes are delimited from containing classes by dollar sign.
     */
    public long length() {
      return file.getSize();
    }

    /**
     * Provides binary stream for a given full qualified class name.
     * Names of packages and classes are delimited by slashes, inner
     * classes are delimited from containing classes by dollar sign.
     * Returns null if nothing found
     */
    public InputStream getInputStream() {
      try {
        return file.getInputStream();
      } catch (IOException e) {
        log.warn(e.getMessage(), e);
      }

      return null;
    }
  }


  public String toString() {
    return filesystem.getDisplayName();
  }

  /* (non-javadoc)
   * @see net.sf.refactorit.vfs.ClassPathElement#getAbsolutePath()
   */
  public String getAbsolutePath() {
      File file = FileObjectUtil.getFileOrNull(filesystem.getRoot());
      if(file == null) {
          return filesystem.getRoot().getPath();
      }
    return file.getAbsolutePath();
  }
}
