/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.vfs;


import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.vfs.AbstractClassPathElement;
import net.sf.refactorit.vfs.ClassPath;

import org.apache.log4j.Logger;

import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.UserQuestionException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;



/**
 *
 * @author  Tanel Alumae
 */
public class NBFileObjectClassPathElement extends AbstractClassPathElement {
  private static final Logger log = Logger.getLogger(NBFileObjectClassPathElement.class);

  private FileObject fileObject;

  public NBFileObjectClassPathElement(FileObject fileObject) {
    this.fileObject = fileObject;
  }

  public ClassPath.Entry getEntry(String cls) {
    if (cls.equals("")) {
      return new Entry(fileObject);
    } else {
      try {
        FileSystem fileSystem = fileObject.getFileSystem();
        String name = fileObject.getNameExt();
        String entryName;
        if ((name == null) || (name.equals(""))) {
          entryName = cls;
        } else {
          entryName = name + "/" + cls;
        }
        FileObject fo = fileSystem.findResource(entryName);
        if (fo != null && fo.isData()) {
          return new Entry(fo);
        } else {
          return null;
        }
      } catch (FileStateInvalidException e) {
        log.warn(e.getMessage(), e);
        return null;
      }

    }
  }

  public void release() {}

  public FileObject getFileObject() {
    return fileObject;
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
    return getAbsolutePath();
  }

  /* (non-Javadoc)
   * @see net.sf.refactorit.vfs.ClassPathElement#getName()
   */
  public String getAbsolutePath() {
    try {
      URL url = fileObject.getURL();
      String str = url.toExternalForm();
      return str;
    } catch (FileStateInvalidException e) {
      AppRegistry.getExceptionLogger().error(e,
          "Can't get url for fileoject: " + fileObject, this);
      return fileObject.toString();
    }/*
    try {
      // TODO: remove this: return fileObject.getFileSystem().getSystemName() + " -> " + fileObject.getPackageNameExt('/', '.');

      String resourceName = null;
      String fileSystemName = null;

      try {
        resourceName = org.netbeans.api.java.classpath.ClassPath
            .getClassPath(fileObject,
            org.netbeans.api.java.classpath.ClassPath.COMPILE)
            .getResourceName(fileObject, File.separatorChar, true);
      } catch (NullPointerException e) {
        AppRegistry.getExceptionLogger().error(e,
            "Can't get resource name for: " + fileObject, this);
      }
      try {
        fileSystemName = fileObject.getFileSystem().getSystemName();
      } catch (FileStateInvalidException e) {
//        return "invalid filesystem -> " + fileObject.getNameExt();
        AppRegistry.getExceptionLogger().error(e,
            "Can't get filesystem name for: " + fileObject, this);
      } catch (NullPointerException e) {
        AppRegistry.getExceptionLogger().error(e,
            "Can't get filesystem name for: " + fileObject, this);
      }

      String path;
      if (fileSystemName == null && resourceName == null) {
        AppRegistry.getLogger(
            this.getClass()).info("Using simple path for: " + fileObject);
        path = fileObject.toString(); // getPath() or getNameExt() are also null usually in such case
      } else if (fileSystemName == null) {
        path = resourceName;
      } else if (resourceName == null) {
        path = fileSystemName;
      } else {
        path = fileSystemName + " -> " + resourceName;
      }

      return path;
    } catch (NullPointerException e) {
      AppRegistry.getExceptionLogger().error(e,
          "Failed to get path for: " + fileObject, this);
      try {
        return fileObject.toString(); // getPath() or getNameExt() are also null usually in such case
      } catch (Exception e1) {
        return "unknown classpath entry";
      }
    }*/
  }
}
