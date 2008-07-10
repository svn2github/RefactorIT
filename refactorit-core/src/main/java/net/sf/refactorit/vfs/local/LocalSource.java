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
import net.sf.refactorit.common.util.FileReadWriteUtil;
import net.sf.refactorit.refactorings.undo.IUndoableEdit;
import net.sf.refactorit.refactorings.undo.IUndoableTransaction;
import net.sf.refactorit.refactorings.undo.RitUndoManager;
import net.sf.refactorit.vfs.AbstractSource;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourceMap;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;


/**
 * Local filesystem Source.
 *
 * @author  Igor Malinin
 * @author  Anton Safonov
 */
public class LocalSource extends AbstractSource {
  final static Logger log = AppRegistry.getLogger(LocalSource.class);

  static final boolean debug = false;

  /** For testing */
  public static boolean fakeReadOnly = false;

  private static final class Filter implements FileFilter {
    public Filter() {}

    public final boolean accept(File f) {
      return (f.isDirectory() || f.isFile());
      // ??? f.getName().endsWith(".java")
    }
  }


  private static final Filter filter = new Filter();

  private Source parentDir;
  private final File file;
  private final Object identifier;

  /** Creates new LocalSource */
  public LocalSource(File file) {
    this(null, file);
  }

  /** Creates new LocalSource */
  private LocalSource(Source parentDir, File file) {
    this.parentDir = parentDir;
    this.file = file;
    this.identifier = getIdentifier(file);

    if (debug) {
      AppRegistry.getLogger(LocalSource.class)
          .debug("new LocalSource: " + this);
    }
  }

  public static final LocalSource getSource(File file) {
    if (debug) {
      log.debug("getSource for: " + file + " " +
          Integer.toHexString(file.hashCode()));
    }
    
    Source result = SourceMap.getSource(getIdentifier(file));
    if (result == null || !(result instanceof LocalSource)) {
      result = new LocalSource(file);
      SourceMap.addSource(result);
    }
    
    return (LocalSource) result;
  }

  public static final LocalSource getSource(Source parent, File file) {
    if (debug) {
      log.debug("getSource for: " + file + " " +
          Integer.toHexString(file.hashCode()));

    }
    
    Source result = SourceMap.getSource(getIdentifier(file));
    if (result == null || !(result instanceof LocalSource)) {
      result = new LocalSource(parent, file);
      SourceMap.addSource(result);
    }

    if (parent != null && ((LocalSource) result).parentDir == null) {
      ((LocalSource) result).parentDir = parent;
      if (parent == result) {
        throw new IllegalArgumentException("'Parent' must not point to 'file'");
      }
    }

    return (LocalSource) result;
  }

  public final boolean exists() {
    return file != null && file.exists();
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
   * @return  parent Source directory
   */
  public Source getParent() {
    return parentDir;
  }

  /**
   * @param path  relative name of child Source
   * @return child Source, <code>null</code> when such child doesn't exist
   */
  public final Source getChild(String path) {
    File f = file;
    LocalSource src = this;

    path = normalize(path);
    StringTokenizer st = new StringTokenizer(path, RIT_SEPARATOR);
    while (st.hasMoreTokens()) {
      String name = st.nextToken();

      f = new File(f, name);

      if (!f.exists()) {
        return null;
      }

      src = LocalSource.getSource(src, f);
    }

    return src;
  }

  /**
   * Name of the file/directory.
   *
   * @return  name of the file/directory
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
   * @return  true if this file is writable
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
   * @return  last modified time
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
   * @param time  last modified time
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
    if (debug) {
      log.debug("setLastModified for: " + this
          +" " + Integer.toHexString(super.hashCode()) + " = " +
          new Date(lastModified()));
    }
    return success;
  }

  /**
   * Only for files!!!
   *
   * @return  length of the file
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
   * @return  true if this Source object represents a file
   */
  public final boolean isFile() {
    return file.isFile();
  }

  /**
   * Tests if this is the directory
   *
   * @return  true if this Source object represents a directory
   */
  public final boolean isDirectory() {
    return file.isDirectory();
  }

  /**
   * Only for files!!! Dont't forget to close it!
   *
   * @return  InputStream to read contents of the file
   */
  public final InputStream getInputStream() throws IOException {
    return new FileInputStream(file);
  }

  /**
   * Only for files!!! Dont't forget to close it!
   *
   * @return  InputStream to write contents of the file
   */
  public final OutputStream getOutputStream() throws IOException {
    return new FileOutputStream(file);
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
   * @return  Source childrens (directories and files)
   */

  public final Source[] getChildren() {

    File[] files = file.listFiles(filter);
    if (files == null) {
      return Source.NO_SOURCES;
    }

    int len = files.length;
    Source[] childrens = new Source[len];

    for (int i = 0; i < len; i++) {
      childrens[i] = LocalSource.getSource(this, files[i]);
    }
    Arrays.sort(childrens);
    return childrens;
  }

  /**
   * List of childrens (subdirectories and files).
   * Only for directories!!! If this is file returns null
   *
   * @return  Source childrens (directories and files)
   */

  public final Source[] getChildren(Source.SourceFilter sourceFilter) {

    File[] files = file.listFiles(filter);
    if (files == null) {
      return Source.NO_SOURCES;
    }

    int len = files.length;
    List children = new ArrayList(len);

    for (int i = 0; i < len; i++) {
      Source childSource = LocalSource.getSource(this, files[i]);
      if (sourceFilter.accept(childSource)) {
        children.add(childSource);
      }
    }
    Collections.sort(children);
    return (Source[]) children.toArray(new Source[children.size()]);
  }

  /**
   * Try to create new Source directory.
   * Returns null if can't create one.
   * Only for directories!!!
   *
   * @return  created directory Source if succeed; null otherwise
   * @param name  name of file to create
   */
  public final Source mkdir(String name) {
    File dst = new File(file, name);
    if (dst.exists() && dst.isDirectory()) {
      return LocalSource.getSource(this, dst);
    }

    if (dst.mkdir()) {
      return LocalSource.getSource(this, dst);
    }

    return null;
  }

  /**
   * Try to create new Source file.
   * Returns null if can't create one.
   * Only for directories!!!
   *
   * @return  created file Source if succeed; null otherwise
   * @param name  name of file to create
   */
  public final Source createNewFile(String name) throws IOException {
    if (debug) {
      log.debug("LocalSource.createNewFile: " + name);

    }
    File dir = file;
    while (dir != null && !dir.isDirectory()) {
      dir = dir.getParentFile();
    }

    if (dir == null) {
      return null;
    }

    File dst = new File(dir, name);

    if (dst.createNewFile()) {

      return LocalSource.getSource(this, dst);
    }

    return null;
  }

  /**
   * Try to rename Source.
   * Returns null if can't rename.
   *
   * @return  renamed file/directory Source if succeed; null otherwise
   *
   * @param dir  target directory
   * @param name  new name
   */
  public final Source renameTo(Source dir, String name) {
    invalidateCaches();

    LocalSource dst = (LocalSource) dir;

    File f = new File(dst.file, name);
    if (f.exists()) {
      // For UNIX; rename fails under windows automatically if the target file already exists
      return null;
    }

    if (this.file.renameTo(f)) {
      if (Assert.enabled) {
        if (this.file.getAbsolutePath().equals(f.getAbsolutePath())) {
          new Exception("Files have the same name after rename: "
              + f.getAbsolutePath()).printStackTrace(System.err);
        }
      }

      LocalSource newSource = LocalSource.getSource(dst, f);
      newSource.setASTTree(this.getASTTree());
      return newSource;
    }

    return null;
  }

  public final File getFileOrNull() {
    return file;
  }

  public final void invalidateCaches() {
    super.invalidateCaches();
    if (debug) {
      log.debug("invalidateCaches for: " + this);
    }
    this.lastModified = -1;
    this.length = -1;
  }

  public final void renameFormFileIfExists(String oldName, String newName) {
    Source formFile;
    String ext = ".form";

    formFile = getParent().getChild(oldName + ext);

    if (formFile != null) {
      String newFileName = newName + ext;
      if (getParent().getChild(newFileName) == null) {

        IUndoableTransaction undoTransaction = RitUndoManager.
            getCurrentTransaction();

        IUndoableEdit undo = null;
        if (undoTransaction != null) {
          undo = undoTransaction.createRenameFileUndo(formFile, getParent(),
              newFileName);
        }

        Source result = formFile.renameTo(getParent(), newFileName);

        if (undoTransaction != null && result != null) {
          undoTransaction.addEdit(undo);
        }

      }
    }
  }

  public final void moveFormFileIfExists(Source destination) {
    Source formFile;
    String ext = ".form";

    int index = getName().lastIndexOf('.');
    if (index != -1) {
      String formName = getName().substring(0, index) + ext;

      if ((formFile = getParent().getChild(formName)) != null &&
          destination.getChild(formName) == null) {

        IUndoableTransaction undoTransaction = RitUndoManager.
            getCurrentTransaction();

        IUndoableEdit undo = null;
        if (undoTransaction != null) {
          undo = undoTransaction.createRenameFileUndo(
              formFile,
              destination,
              formName);
        }

        Source result = formFile.renameTo(destination, formName);

        if (undoTransaction != null && result != null) {
          undoTransaction.addEdit(undo);
        }

      }
    }
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

  final void collectSources(ArrayList result,
      LocalSourcePath.SourceFilter fileFilter) {
    final File file = this.getFileOrNull();
    if (file != null && fileFilter.acceptPath(file.getAbsolutePath())) {
      if (file.isDirectory()) {
        iterateDirectory(file, this, result, fileFilter);
      } else if (file.isFile()) {
        if (fileFilter.accept(file.getName(), false)) {
          result.add(LocalSource.getSource(file));
        } else {
          // FIXME: What if it is a JAR file?
          //        IDEA, for example, supports sources in JARs.
        }
      } else {
        // FIXME: What if it is neither existing file nor directory?
      }
    }
  }

  private static void iterateDirectory(File aDir, Source parent,
      ArrayList result, LocalSourcePath.SourceFilter fileFilter) {
    // FIXME: use a generic filename filter here
    File[] list = aDir.listFiles();
    if (list == null) {
      return;
    }

    for (int i = 0, max = list.length; i < max; ++i) {
      File curFile = list[i];
      if (curFile != null && fileFilter.acceptPath(curFile.getAbsolutePath())) {
        if (curFile.isFile()) {
          if (fileFilter.accept(curFile.getName(), false)) {
            result.add(LocalSource.getSource(parent, curFile));
          }
        } else {
          if (fileFilter.accept(curFile.getName().toLowerCase(), true)) {
            iterateDirectory(curFile, LocalSource.getSource(parent, curFile),
                result, fileFilter);
          }
        }
      }
    }
  }

  private long lastModified = -1;
  private long length = -1;
}
