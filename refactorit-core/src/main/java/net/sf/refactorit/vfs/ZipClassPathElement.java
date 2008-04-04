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
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * Local filesystem ClassPath. Do not forget to call close() after using it.
 *
 * @author  Igor Malinin
 */
public final class ZipClassPathElement extends AbstractClassPathElement {
  private final File file;
  private ZipFile zip;
  private boolean bad;
  private long lastModified = 0l;

  public ZipClassPathElement(File file) {
    this.file = file;
    isChanged();
  }

  public final ClassPath.Entry getEntry(String cls) {
    ensureZip();

    if (zip == null) {
      return null;
    }

    ZipEntry ent = zip.getEntry(cls);
    if (ent == null) {
      return null;
    }

    return new Entry(zip, ent);
  }

  private void ensureZip() {
    if (zip == null && !bad) {
      try {
        zip = new ZipFile(file);
      } catch (IOException ignore) {
        bad = true;
      }
    }
  }

  public boolean existsEntry(String cls) {
//System.err.println("exists: " + cls);

//    try {
//      ensureZip();
//
//      if (zip == null) {
//        return false;
//      }
//
//      long jzf;
//      try {
//        final Field jzfile = zip.getClass().getField("jzfile");
//        jzfile.setAccessible(true);
//        jzf = jzfile.getLong(zip);
//      } catch (Exception e) {
//        System.err.println("fields: " + Arrays.asList(zip.getClass().getFields()));
//        throw e;
//      }
//
//      Long jzentry;
//
//      Method getEntry;
//      try {
//        getEntry = zip.getClass().getMethod(
//            "getEntry", new Class[] {Long.TYPE, String.class});
//        getEntry.setAccessible(true);
//        jzentry = (Long) getEntry.invoke(zip, new Object[] {
//            new Long(jzf), cls});
//      } catch (Exception e) {
//e.printStackTrace();
//
//        getEntry = zip.getClass().getMethod(
//            "getEntry", new Class[] {Long.TYPE, String.class, Boolean.TYPE});
//        getEntry.setAccessible(true);
//        jzentry = (Long) getEntry.invoke(zip,
//            new Object[] {new Long(jzf), cls, Boolean.FALSE});
//      }
//
//      return jzentry.longValue() > 0;
//    } catch (Exception e) {
//e.printStackTrace();
//      return super.existsEntry(cls);
//    } catch (Error e) {
//e.printStackTrace();
      return super.existsEntry(cls);
//    }
  }

  public final void release() {
    bad = false;

    if (zip != null) {
      try {
        zip.close();
        zip = null;
      } catch (IOException ignore) {}
    }
  }

  public final boolean isChanged() {
    final long oldLastModified = lastModified;
    lastModified = file.lastModified();
    return lastModified != oldLastModified;
  }

  static final class Entry implements ClassPath.Entry {
    private final ZipFile file;
    private final ZipEntry ent;

    Entry(ZipFile f, ZipEntry e) {
      file = f;
      ent = e;
    }

    public final boolean exists() {
      return ent.getSize() > 0;
    }

    public final boolean delete() {
      return false;
    }

    /**
     * Time of last modification.
     * Returns 0 if unknown.
     *
     * @return  last modified time
     */
    public final long lastModified() {
      return ent.getTime();
    }

    /**
     * Returns size of binary representation of a class for a given full
     * qualified class name. Returns 0 if class does not exists.
     * Names of packages and classes are delimited by slashes, inner
     * classes are delimited from containing classes by dollar sign.
     */
    public final long length() {
      return ent.getSize();
    }

    /**
     * Provides binary stream for a given full qualified class name.
     * Names of packages and classes are delimited by slashes, inner
     * classes are delimited from containing classes by dollar sign.
     * Returns null if nothing found
     */
    public final InputStream getInputStream() {
      try {
        return file.getInputStream(ent);
      } catch (IOException e) {
        System.err.println("Something unusual has ocurred:");
        e.printStackTrace(System.err);
      }

      return null;
    }
  }


  public final String toString() {
    return file.toString();
  }

//  public final File getFile() {
//    return file;
//  }

  /* (non-Javadoc)
   * @see net.sf.refactorit.vfs.ClassPathElement#getName()
   */
  public final String getAbsolutePath() {
    return toString();
  }
}
