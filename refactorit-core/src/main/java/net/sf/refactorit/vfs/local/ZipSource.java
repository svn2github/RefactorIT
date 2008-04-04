/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.vfs.local;


import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.vfs.AbstractSource;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourceMap;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * Zip/jar archive with many sources.
 *
 * @author Anton Safonov
 */
public class ZipSource extends AbstractSource {
  final static Logger log = AppRegistry.getLogger(ZipSource.class);

  private static final Filter filter = new Filter();

  private final File file;
  private ZipFile zip;

  private boolean bad = false;

  private final Object identifier;

  private long lastModified = -1;
  private long length = -1;


  /**
   * For testing
   */
  public static boolean fakeReadOnly = false;

  private static final class Filter implements FileFilter {
    public Filter() {}

    public final boolean accept(File f) {
      return (f.isDirectory() || f.isFile());
      // ??? f.getName().endsWith(".java")
    }
  }


  /**
   * Creates new ZipSource
   */
  private ZipSource(File file) {
    this.file = file;
    this.identifier = getIdentifier(file);
    if (LocalSource.debug) {
      AppRegistry.getLogger(ZipSource.class).debug("new ZipSource: " + this);
    }
  }

  public static final ZipSource getSource(File file) {
    if (LocalSource.debug) {
      log.debug("getSource for: " + file + " " + Integer.toHexString(file.hashCode()));
    }
    Source result = SourceMap.getSource(getIdentifier(file));
    if (result == null || !(result instanceof ZipSource)) {
      result = new ZipSource(file);
      SourceMap.addSource(result);
    }

    return (ZipSource) result;
  }

  public ZipFile getZip() {
    if (this.zip == null && !this.bad) {
      try {
      this.zip = new ZipFile(this.file);
      } catch (Exception e) {
        this.bad = true;
      }
    }
    return this.zip;
  }

  public final boolean exists() {
    return this.file != null;
  }

  public final byte[] getContent() throws IOException {
    throw new UnsupportedOperationException(
        "Zip file itself shouldn't be tried to read: " + this);
  }

  final void collectSources(ArrayList result,
      LocalSourcePath.SourceFilter fileFilter) {
    ZipFile zip = getZip();

    Enumeration entries = zip.entries();
    while (entries.hasMoreElements()) {
      ZipEntry entry = (ZipEntry) entries.nextElement();
//System.err.println("Entry: " + entry + " - " + entry.getClass() + " - " + entry.getName());
      // TODO: filter ignore dirs
      if (!entry.isDirectory()) {
        // FIXME: filtering may really mistake, since entry name contains the path as well
        if (fileFilter.accept(entry.getName(), false)) {
          result.add(ZipEntrySource.getSource(this, entry));
        }
      }
    }

//    final File file = this.getFileOrNull();
//    if (file != null && fileFilter.acceptPath(file.getAbsolutePath())) {
//      if (file.isDirectory()) {
//        iterateDirectory(file, this, result, fileFilter);
//      } else if (file.isFile()) {
//        if (fileFilter.accept(file.getName(), false)) {
//          result.add(LocalSource.getSource(file));
//        } else {
//          // FIXME: What if it is a JAR file?
//          //        IDEA, for example, supports sources in JARs.
//        }
//      } else {
//        // FIXME: What if it is neither existing file nor directory?
//      }
//    }
  }

  /**
   * Parent directory.
   *
   * @return parent Source directory
   */
  public Source getParent() {
    return null;
  }

  /**
   * @param path relative name of child Source
   * @return child Source, <code>null</code> when such child doesn't exist
   */
  public final Source getChild(String path) {
    File f = file;
    ZipSource src = this;

    path = normalize(path);
    StringTokenizer st = new StringTokenizer(path, RIT_SEPARATOR);
    while (st.hasMoreTokens()) {
      String name = st.nextToken();

      f = new File(f, name);

      if (!f.exists()) {
        return null;
      }

//      src = ZipEntrySource.getSource(src, null); // TODO
    }

    return src;
  }

  /**
   * Name of the file/directory.
   *
   * @return name of the file/directory
   */
  public String getName() {
    return file.getName();
  }

  private static final Object getIdentifier(final File file) {
    if (file != null) {
      return file.getAbsolutePath();
    } else {
      return null;
    }
  }

  public Object getIdentifier() {
    return this.identifier;
  }

  /**
   * Low-level OS path for this source.
   *
   * @return absolute path in sense of OS
   */
  public String getAbsolutePath() {
    return file.getAbsolutePath();
  }

  /**
   * Tests if this file is writable.
   *
   * @return true if this file is writable
   */
  public final boolean canWrite() {
    if (fakeReadOnly) {
      return false;
    }

    return file.canWrite();
  }

  /**
   * Makes file editable if possible. Usable with files from VisualSourceSafe
   * and when files are checked out as read-only.
   *
   * @return true if succeeded to make file writable
   */
  public final boolean startEdit() {
    // TODO implement Vcs support for standalone also?
    return canWrite();
  }

  /**
   * Time of last modification
   *
   * @return last modified time
   */
  public final long lastModified() {
    if (this.lastModified < 0) {
      this.lastModified = file.lastModified();
    }

    return this.lastModified;
//    return file.lastModified();
  }

  /**
   * Try to set time of last modification
   *
   * @param time last modified time
   */
  public final boolean setLastModified(long time) {
    invalidateCaches();
    boolean success = file.setLastModified(time);
    if (!success) {
      if (Assert.enabled) {
        new Exception("Failed to set lastModified for: " + this)
            .printStackTrace(System.err);
      }
    }
    if (LocalSource.debug) {
      log.debug("setLastModified for: " + this
          + " " + Integer.toHexString(super.hashCode()) + " = " +
          new Date(lastModified()));
    }
    return success;
  }

  /**
   * Only for files!!!
   *
   * @return length of the file
   */
  public final long length() {
    if (this.length < 0) {
      this.length = file.length();
    }

    return this.length;
//    return file.length();
  }

  /**
   * Tests if this is the file
   *
   * @return true if this Source object represents a file
   */
  public final boolean isFile() {
    return false;//file.isFile();
  }

  /**
   * Tests if this is the directory
   *
   * @return true if this Source object represents a directory
   */
  public final boolean isDirectory() {
    return true;//file.isDirectory();
  }

  /**
   * Only for files!!! Dont't forget to close it!
   *
   * @return InputStream to read contents of the file
   */
  public final InputStream getInputStream() throws IOException {
    throw new UnsupportedOperationException(
        "Zip file itself shouldn't be tried to read: " + this);
    //return new FileInputStream(file);
  }

  /**
   * Only for files!!! Dont't forget to close it!
   *
   * @return InputStream to write contents of the file
   */
  public final OutputStream getOutputStream() throws IOException {
    throw new UnsupportedOperationException(
        "Zip file itself shouldn't be tried to write: " + this);
//    return new FileOutputStream(file);
  }

  /**
   * Try to delete this file/directory
   */
  public final boolean delete() {
    invalidateCaches();

    Source[] children = getChildren();

    if (children != null && children.length > 0) {
      return false;
    } // not empty

    return file.delete();
  }

  /**
   * List of childrens (subdirectories and files).
   * Only for directories!!! If this is file returns null
   *
   * @return Source childrens (directories and files)
   */

  public final Source[] getChildren() {
    ZipFile zip = getZip();
    Enumeration entries = zip.entries();

    while (entries.hasMoreElements()) {
      Object entry = entries.nextElement();
      System.err.println("entry: " + entry + " - " + entry.getClass());
    }


//    File[] files = file.listFiles(filter);
//    if (files == null) {
//      return Source.NO_SOURCES;
//    }
//
//    int len = files.length;
//    Source[] childrens = new Source[len];
//
//    for (int i = 0; i < len; i++) {
//// TODO
////      childrens[i] = ZipSource.getSource(this, files[i]);
//    }
//    Arrays.sort(childrens);
//    return childrens;
    return Source.NO_SOURCES;
  }

  /**
   * List of childrens (subdirectories and files).
   * Only for directories!!! If this is file returns null
   *
   * @return Source childrens (directories and files)
   */

  public final Source[] getChildren(Source.SourceFilter sourceFilter) {

    File[] files = file.listFiles(filter);
    if (files == null) {
      return Source.NO_SOURCES;
    }

    int len = files.length;
    List children = new ArrayList(len);

    for (int i = 0; i < len; i++) {
// TODO
//      Source childSource = ZipSource.getSource(this, files[i]);
//      if (sourceFilter.accept(childSource)) {
//        children.add(childSource);
//      }
    }
    Collections.sort(children);
    return (Source[]) children.toArray(new Source[children.size()]);
  }

  /**
   * Try to create new Source directory.
   * Returns null if can't create one.
   * Only for directories!!!
   *
   * @param name name of file to create
   * @return created directory Source if succeed; null otherwise
   */
  public final Source mkdir(String name) {
    return null;
  }

  /**
   * Try to create new Source file.
   * Returns null if can't create one.
   * Only for directories!!!
   *
   * @param name name of file to create
   * @return created file Source if succeed; null otherwise
   */
  public final Source createNewFile(String name) throws IOException {
    return null;
  }

  /**
   * Try to rename Source.
   * Returns null if can't rename.
   *
   * @param dir  target directory
   * @param name new name
   * @return renamed file/directory Source if succeed; null otherwise
   */
  public final Source renameTo(Source dir, String name) {
    return null;
  }

  public final File getFileOrNull() {
    return file;
  }

  public final void invalidateCaches() {
    super.invalidateCaches();
    if (LocalSource.debug) {
      log.debug("invalidateCaches for: " + this);
    }
    this.lastModified = -1;
    this.length = -1;
  }

  public final void renameFormFileIfExists(String oldName, String newName) {
  }

  public final void moveFormFileIfExists(Source destination) {
  }

  public final char getSeparatorChar() {
    return File.separatorChar;
  }

  public final boolean shouldSupportVcsInFilesystem() {
    return false;
  }

  public final boolean inVcs() {
    return false;
  }
}
