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
import net.sf.refactorit.common.util.FileReadWriteUtil;
import net.sf.refactorit.vfs.AbstractSource;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourceMap;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;


/**
 * Zip/jar file, i.e. a single java source inside archive.
 *
 * @author Anton Safonov
 */
public class ZipEntrySource extends AbstractSource {
  final static Logger log = AppRegistry.getLogger(ZipEntrySource.class);

  private final ZipSource parent;
  private final ZipEntry file;

  private final Object identifier;

  private long lastModified = -1;
  private long length = -1;


  /**
   * Creates new ZipEntrySource
   */
  private ZipEntrySource(ZipSource parent, ZipEntry file) {
    this.parent = parent;
    this.file = file;
    this.identifier = getIdentifier(file);
    if (LocalSource.debug) {
      AppRegistry.getLogger(ZipEntrySource.class).debug("new ZipEntrySource: " + this);
    }
  }

  public static final ZipEntrySource getSource(ZipSource parent, ZipEntry file) {
    if (LocalSource.debug) {
      log.debug("getSource for: " + file + " " + Integer.toHexString(file.hashCode()));
    }
    Source result = SourceMap.getSource(getIdentifier(file));
    if (result == null || !(result instanceof ZipEntrySource)) {
      result = new ZipEntrySource(parent, file);
      SourceMap.addSource(result);
    }

    return (ZipEntrySource) result;
  }

  public final boolean exists() {
    return true; //file != null && file.exists();
  }

  public final byte[] getContent() throws IOException {
    byte[] preloadBuffer = null;

    InputStream inputStream = null;
    try {
      inputStream = getInputStream();
      preloadBuffer = FileReadWriteUtil.read(inputStream, (int) length());
    } catch (IOException e) {
      preloadBuffer = new byte[0];
      throw e;
    } finally {
      if (inputStream != null) {
        inputStream.close();
      }
      if (preloadBuffer == null) {
        preloadBuffer = new byte[0];
      }
    }

    return preloadBuffer;
  }

  /**
   * Parent directory.
   *
   * @return parent Source directory
   */
  public Source getParent() {
    return parent;
  }

  /**
   * @param path relative name of child Source
   * @return child Source, <code>null</code> when such child doesn't exist
   */
  public final Source getChild(String path) {
    return null;
  }

  /**
   * Name of the file/directory.
   *
   * @return name of the file/directory
   */
  public String getName() {
    return file.getName();
  }

  private static final Object getIdentifier(final ZipEntry entry) {
    if (entry != null) {
      return entry.getName();
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
    return file.getName(); // FIXME: hmm
  }

  /**
   * Tests if this file is writable.
   *
   * @return true if this file is writable
   */
  public final boolean canWrite() {
    return false;
  }

  /**
   * Makes file editable if possible. Usable with files from VisualSourceSafe
   * and when files are checked out as read-only.
   *
   * @return true if succeeded to make file writable
   */
  public final boolean startEdit() {
    return canWrite();
  }

  /**
   * Time of last modification
   *
   * @return last modified time
   */
  public final long lastModified() {
    if (this.lastModified < 0) {
      this.lastModified = file.getTime();
    }

    return this.lastModified;
  }

  /**
   * Try to set time of last modification
   *
   * @param time last modified time
   */
  public final boolean setLastModified(long time) {
    return false;
  }

  /**
   * Only for files!!!
   *
   * @return length of the file
   */
  public final long length() {
    if (this.length < 0) {
      this.length = file.getSize();
    }

    return this.length;
  }

  /**
   * Tests if this is the file
   *
   * @return true if this Source object represents a file
   */
  public final boolean isFile() {
    return !isDirectory();
  }

  /**
   * Tests if this is the directory
   *
   * @return true if this Source object represents a directory
   */
  public final boolean isDirectory() {
    return file.isDirectory();
  }

  /**
   * Only for files!!! Dont't forget to close it!
   *
   * @return InputStream to read contents of the file
   */
  public final InputStream getInputStream() throws IOException {
    return this.parent.getZip().getInputStream(file);
  }

  /**
   * Only for files!!! Dont't forget to close it!
   *
   * @return InputStream to write contents of the file
   */
  public final OutputStream getOutputStream() throws IOException {
    return null;
  }

  /**
   * Try to delete this file/directory
   */
  public final boolean delete() {
    return false;
  }

  /**
   * List of childrens (subdirectories and files).
   * Only for directories!!! If this is file returns null
   *
   * @return Source childrens (directories and files)
   */

  public final Source[] getChildren() {
    return Source.NO_SOURCES;
  }

  /**
   * List of childrens (subdirectories and files).
   * Only for directories!!! If this is file returns null
   *
   * @return Source childrens (directories and files)
   */

  public final Source[] getChildren(Source.SourceFilter sourceFilter) {
    return Source.NO_SOURCES;
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
    return null;
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
