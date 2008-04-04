/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.vfs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import net.sf.refactorit.common.exception.SystemException;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.ChainableRuntimeException;
import net.sf.refactorit.common.util.FileReadWriteUtil;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.netbeans.common.NBContext;
import net.sf.refactorit.netbeans.common.projectoptions.FileObjectUtil;
import net.sf.refactorit.netbeans.common.vcs.FileSystemProperties;
import net.sf.refactorit.netbeans.common.vcs.Options;
import net.sf.refactorit.netbeans.common.vcs.Vcs;
import net.sf.refactorit.netbeans.common.vcs.VcsRunner;
import net.sf.refactorit.refactorings.undo.IUndoableEdit;
import net.sf.refactorit.refactorings.undo.IUndoableTransaction;
import net.sf.refactorit.refactorings.undo.RitUndoManager;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.RuntimePlatform;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.utils.cvsutil.CvsFileStatus;
import net.sf.refactorit.vfs.AbstractSource;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourceMap;

import org.apache.log4j.Logger;
import org.netbeans.modules.vcscore.VcsAttributes;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObject;
import org.openide.util.UserQuestionException;



/**
 * @author Igor Malinin
 * @author Anton Safonov
 * @author Risto
 */
public class NBSource extends AbstractSource {
  private static final Logger log = Logger.getLogger(NBSource.class);

  public static long CVS_DIR_ADD_TIMEOUT = 10000;
  private static final long LOCK_TIMEOUT = 10000;

  //protected NBSource parent;
  protected final FileObject fileObject;
  private final Object identifier;

  private byte[] fakeContent = null;
  private long fakeLastModified = -1;

  /** for testing */
  private boolean fakeRoot = false;

  private static NBSourceVersionState versionState;

  public static final char   SEPARATOR_CHAR   = '/';
  public static final String SEPARATOR_STRING = "" + SEPARATOR_CHAR;

  public static void setVersionState(NBSourceVersionState versionState) {
    NBSource.versionState=versionState;
  }


  /** use the getSource() method for regular use */
  private NBSource(FileObject fo) {
    this.fileObject = fo;
    this.identifier = getIdentifier(this.fileObject);

    fo.addFileChangeListener(new FileChangeAdapter() {
      public void fileRenamed(FileRenameEvent event) {
        SourceMap.removeSource(NBSource.this);
      }

      public void fileDeleted(FileEvent event) {
        SourceMap.removeSource(NBSource.this);
      }
    });
  }


  private static final Object getIdentifier(final FileObject file) {
    try {
      String path;

      path = file.getURL().getFile();
      path = URLDecoder.decode(path);

      // Let's make it comparable independently of mount point
      // ("QB" is a separator over root of Filesystems in NB)
      path = StringUtil.replace(path, "QB", SEPARATOR_STRING);

      return path;
    } catch (FileStateInvalidException e) {
      // Should also work most of the time (but probably not always)
      return new Integer(System.identityHashCode(file));
    }
  }

  public static final NBSource findSource(FileObject fo) {
    return (NBSource) SourceMap.getSource(getIdentifier(fo));
  }

  public static final NBSource getSource(FileObject fo) {
    Source result = SourceMap.getSource(getIdentifier(fo));
    if (result == null || !(result instanceof NBSource)) {
      if (fo != null) {
        result = new NBSource(fo);
        SourceMap.addSource(result);
      }
    }
    return (NBSource) result;
  }

  public static final NBSource getSource(String localPath) {
    FileObject fo=getFileObjectForPath(localPath);
    return getSource(fo);
  }

  public static final NBSource getSource(File file) {
    FileObject fo=getFileObjectForFile(file);
    return getSource(fo);
  }

  private static FileObject getFileObjectForPath(String localPath) {
    return versionState.getFileObjectForPath(localPath);
  }

  private static FileObject getFileObjectForFile(File localFile) {
    return versionState.getFileObjectForFile(localFile);
  }


  /** For testing */
  public void fakeRoot() {
    this.fakeRoot = true;
  }

  public Source getParent() {
    if (fakeRoot || fileObject.getParent() == null) {
      return null;
    } else {
      return getSource(fileObject.getParent());
    }
  }

  public Object getIdentifier() {
    return this.identifier;
  }

  public void setContent(byte[] newContent) throws IOException {
    setContent(getDataObject(), newContent);
  }

  public void pretendContentIs(byte[] content) {
    fakeContent = content;
    fakeLastModified = System.currentTimeMillis();
  }

  public byte[] getFakeContent() {
    return fakeContent;
  }

  public static void setContent(DataObject dataObj,
      byte[] newContent) throws IOException {
    OutputStream out = getOutputStream(dataObj.getPrimaryFile());
    out.write(newContent);
    out.flush();
    out.close();
  }

  public byte[] getContent() throws IOException {
    byte[] preloadBuffer = null;
    InputStream inputStream = null;
    try {
      inputStream = getInputStream();
      preloadBuffer = FileReadWriteUtil.read(inputStream, (int) length());
    } catch (IOException e) {
      preloadBuffer = new byte[0];
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
   * @param path  relative name of child Source
   *
   * @return  child Source or *null* if it does not exist
   */
  public Source getChild(String path) {
    FileObject fo = fileObject;
    NBSource src = this;

    path = normalize(path);
    StringTokenizer st = new StringTokenizer(path, RIT_SEPARATOR);
    while (st.hasMoreTokens()) {
      String name = st.nextToken();

      fo = FileObjectUtil.safeGetFileObject(fo, name);
      if (fo == null || fo.isVirtual()) {
        return null;
      }

      // Workaround for a NOW FIXED bug of NB 4.0 beta 2.
      // See http://www.netbeans.org/issues/show_bug.cgi?id=50326
      if( ! fo.getNameExt().equals(name)) {
        return null;
      }

      src = getSource(fo);
    }

    return src;
  }

  /**
   * Name of the file/directory
   *
   * @return  name of the file/directory
   */
  public String getName() {
    return fileObject.getNameExt();
  }

  public String getAbsolutePath() {
    return versionState.getAbsolutePath(this);
  }

  /**
   * Path from the "sourcepath root" this file belongs to.
   * Path components are delimited by slash '/'.
   * example: com/package/CompilationUnit.java
   */
  public String getRelativePath() {
    return versionState.getRelativePath(this);
  }

  /**
   * Almost the same as {@link #getAbsolutePath absolute path}, but
   * file system systemName looks much better in dialogs.
   * @return  displayable path (relative to filesystem root)
   */
  public String getDisplayPath() {
    if (isRoot()) {
      String systemName;
      try {
        systemName = fileObject.getFileSystem().getDisplayName();
      } catch (FileStateInvalidException e) {
        systemName = "Invalid filesystem";
      }
      String folderName = FileObjectUtil.getResourceName(fileObject);
      if (folderName.length() == 0) {
        return systemName;
      } else {
        return systemName + LINK_SYMBOL + folderName;
      }
    }

    String path = getParent().getAbsolutePath();

    if (path.endsWith("\\") || path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }

    if (path == null || path.length() == 0) {
      return getName();
    } else {
      return path + getSeparatorChar() + getName();
    }
  }

  /**
   * Determines whether the given source element is root of the sourcepath element
   *
   * @return true if given object is "root"
   **/
  public boolean isRoot() {
    return fakeRoot || (getParent() == null);
  }

  /**
   * Tests if this file is writable.
   *
   * @return  true if this file is writable
   */
  public boolean canWrite() {
    return fileObject.canWrite();
  }

  /**
   * Makes file editable if possible. Usable with files from VisualSourceSafe
   * and when files are checked out as read-only.
   *
   * @return true if succeeded to make file writable
   */
  public boolean startEdit() {
    getFromVssIfInvalidFileObject();

    boolean result = canWrite();

    //System.out.println("CanWrite: " + result);
    if (!result) {
      if (Vcs.supportsCheckout(fileObject)
          && FileSystemProperties.isVssFileSystem(fileObject)) {
        Vcs.checkout(fileObject);
      } else if (Vcs.supportsEdit(fileObject)) {
        Vcs.edit(fileObject);
      }
      fileObject.refresh();

      result = canWrite();
    }

    return result;
  }

  private void getFromVssIfInvalidFileObject() {
    if (!fileObject.isValid()) {
      if (FileSystemProperties.isVssFileSystem(fileObject)) {
        Vcs.get(fileObject);
      }
    }
  }

  /**
   * Time of last modification
   *
   * @return  last modified time
   */
  public long lastModified() {
    if(fakeLastModified != -1) {
      return fakeLastModified;
    }

    fileObject.refresh();
    return fileObject.lastModified().getTime();
  }

  /**
   * Set time of last modification
   *
   * @param time  last modified time
   */
  public boolean setLastModified(long time) {
    invalidateCaches();

    return false;
  }

  /**
   * Only for files!!!
   *
   * @return  lenght of the file
   */
  public long length() {
    if (fakeContent != null) {
      return fakeContent.length;
    } else {
      return fileObject.getSize();
    }
  }

  /**
   * Tests if this is the file
   *
   * @return  true if this Source object represents a file
   */
  public boolean isFile() {
    return fileObject.isData();
  }

  /**
   * Tests if this is the directory
   *
   * @return  true if this Source object represents a directory
   */
  public boolean isDirectory() {
    return fileObject.isFolder();
  }

  /**
   * Only for files!!! Dont't forget to close it!
   *
   * @return  InputStream to read contents of the file
   */
  public InputStream getInputStream() throws IOException {
    if (fakeContent != null) {
      return new ByteArrayInputStream(fakeContent);
    }

    return FileObjectUtil.getInputStream(this.fileObject);
  }


  /**
   * Only for files!!! Dont't forget to close it -- otherwise the lock will never get
   * released, etc.
   *
   * @return  OutputStream to get contents of the file
   */
  public OutputStream getOutputStream() throws IOException {
    startEdit(); // ensures not read-only

    return getOutputStream(fileObject);
  }

  static OutputStream getOutputStream(FileObject fileObject) throws
      IOException {

    FileLock lock = getLock(fileObject);

    try {
      NBOutputStream result = new NBOutputStream(fileObject.getOutputStream(lock), lock);

      NBSource nbSource = getSource(fileObject);
      nbSource.fakeContent = null;
      nbSource.fakeLastModified = -1;

      return result;
    } catch (IOException e) {
      if (lock != null) {
        lock.releaseLock();
      }

      throw e;
    }
  }

  private static FileLock getLock(final FileObject fileObject) throws IOException {
    long startTime = System.currentTimeMillis();
    FileLock lock = null;
    Exception exception = null;
    do {
      try {
        lock = fileObject.lock();
        break;
      } catch (UserQuestionException e) {
        try {
          e.confirmed(); // Should we should ask it from user ourselves?
        } catch (IOException ex) {
          AppRegistry.getExceptionLogger().debug(ex,NBSource.class);
        }
      } catch (FileAlreadyLockedException ex) {
        exception = ex;
      }
      try {
        Thread.sleep(100);
      } catch (InterruptedException ex1) {
        break;
      }
    } while (System.currentTimeMillis() - startTime < LOCK_TIMEOUT);

    if (lock == null) {
      String message = "Could not aquire lock in " + LOCK_TIMEOUT + " ms for "+ fileObject.getName();
      AppRegistry.getExceptionLogger().debug(exception, message, NBSource.class);
      throw new SystemException(message, exception);
    }

    return lock;
  }


  /**
   * Try to delete this file/directory.
   */
  public boolean delete() {
    invalidateCaches();

    if (!isEmpty()) {
      return false;
    }


    startEdit(); // ensures not read-only

    try {
      boolean isFolder = isDirectory();
      boolean removed = false;

      if (inVcs()) {
        removed = Vcs.remove(fileObject);

        if (fileObject.isValid() && isFolder
            && FileSystemProperties.isVssFileSystem(this.
            fileObject)) {
          removed = Vcs.remove(fileObject); // to be sure for VSS
        }
      }

      if (!removed) {
        FileLock lock = null;
        try {
          lock = fileObject.lock();
        } catch (UserQuestionException e) {
          e.confirmed(); // Should we ask it from user ourselves?
        }
        if (lock != null) {
          try {
            fileObject.delete(lock);
            removed = true;
          } catch (Exception e) {
            removed = false;
          } finally {
            lock.releaseLock();
          }
        }
      }

      return removed;
    } catch (IOException ignore) {
      return false;
    }
  }

  private boolean isEmpty() {
    Source[] children = getChildren();
    upper:for (int i = 0; i < children.length; i++) {
      final String childName = children[i].getName();
      if (childName.endsWith("~") || childName.equals(".nbattrs")) {

        IUndoableTransaction transaction = RitUndoManager.getCurrentTransaction();
        IUndoableEdit undo = null;

        if (transaction != null) {
          undo = transaction.createDeleteFileUndo(children[i]);
        }

        boolean result = children[i].delete();

        if (transaction != null && result) {
          transaction.addEdit(undo);
        }

      }
      if (childName.equals("vssver.scc")) {
        continue upper; // VSS control file will go on delete
      }
      if (AbstractSource.inVersionControlDirList(childName)) {
        continue upper;
      }
      return false;
    }

    return true;
  }

  /**
   * List of childrens (subdirectories and files).
   * Only for directories!!!
   *
   * @return  Source childrens (directories and files)
   */
  public Source[] getChildren() {
    List sources = new ArrayList(20);
    FileObject[] objects = fileObject.getChildren();
    if (objects == null) {
      log.debug("$$$ fileObject.getChildren() gave null for " + getName());
      return Source.NO_SOURCES;
    }

    int len = objects.length;
    for (int i = 0; i < len; i++) {
      FileObject fo = objects[i];

      if ((fo.isFolder() || fo.isData()) && (!fo.isVirtual())) {
        NBSource source = getSource(fo);
        if (source != null) {
          sources.add(source);
        } else {
          log.debug("$$$ getChildren could not get source for: " + fo.getNameExt());
        }
      } else {
        log.debug("$$$ getChildren saw bad attributes for: " + fo.getNameExt() + ": " +
            fo.isFolder() + ", " + fo.isData() + ", " + fo.isVirtual());
      }
    }

    return (Source[]) sources.toArray(new Source[sources.size()]);
  }

  /**
   * List of childrens (subdirectories and files).
   * Only for directories!!!
   *
   * @return  Source childrens (directories and files)
   */
  public Source[] getChildren(Source.SourceFilter sourceFilter) {
    FileObject[] objects = fileObject.getChildren();
    if (objects == null) {
      return Source.NO_SOURCES;
    }
    List children = new ArrayList(objects.length);
    int len = objects.length;
    for (int i = 0; i < len; i++) {
      FileObject fo = objects[i];

      if ((fo.isFolder() || fo.isData()) && (!fo.isVirtual())) {
        NBSource childSource = getSource(fo);
        if (childSource != null && sourceFilter.accept(childSource)) {
          children.add(childSource);
        }
      }
    }

    return (Source[]) children.toArray(new Source[children.size()]);
  }

  public Source mkdir(String name) {
    return mkdir(name, true);
  }

  /**
   * Try to create new Source directory.
   * Returns null if can't create one.
   * Only for directories!!!
   *
   * @return  created directory Source if succeed; null otherwise
   * @param name  name of file to create
   */
  public Source mkdir(String name, boolean addToVcs) {
    FileObject dst = FileObjectUtil.safeGetFileObject(fileObject, name);
    log.debug("$$$ Will create dir for parent: " + fileObject + ", isValid: " + fileObject.isValid() +
        ", parent exists: " + FileObjectUtil.getFileOrNull(fileObject).exists());

    if (dst == null || (!dst.isFolder())) {
      try {
        dst = fileObject.createFolder(name);
        dst.refresh();
      } catch (IOException ignore) {
        log.debug("REFACTORIT: Failed to create folder: " + name, ignore);
        return null;
      }
    }

    NBSource result = getSource(dst);

    addToVcsIfNeeded(result, addToVcs);

    return result;
  }

  private static void addToVcsIfNeeded(final NBSource src,
      final boolean shouldAddIfPossible) {
    if (shouldAddIfPossible && src.shouldSupportVcsInFilesystem()
        && (!src.inVcs()) && src.getParent().inVcs()) {
      FileObject fo = src.getFileObject();
      fo.getAttribute(VcsAttributes.VCS_STATUS); // this call makes it wroking!!!
      if (Vcs.supportsCreate(fo)) {
        Vcs.create(fo);
        fo.refresh();
        if (FileSystemProperties.isVssFileSystem(fo)) {
          Vcs.getr(fo);
        }
        fo.getAttribute(VcsAttributes.VCS_STATUS); // this call makes it working!!!
      } else {
        if (CvsFileStatus.getInstance().isKnown(src.getParent().getFileOrNull()) &&
            Vcs.addDir(fo)) {

          fo.refresh();
          if (FileSystemProperties.isVssFileSystem(fo)) {
            Vcs.getr(fo);
          }
          fo.getAttribute(VcsAttributes.VCS_STATUS); // this call makes it working!!!

          waitUntilInCvs(src);
        }
      }
    }
  }

  public static void waitUntilInCvs(final NBSource src) {
    long startedWaiting = System.currentTimeMillis();

    do {
      // Sleep for thread safety under Windows: it appears that while we check for
      // availability of Cvs file status, the file can not be read in other threads
      // at the same time under Windows. So, we need to sleep here instead of yielding;
      // still even this might fail sometimes :( There should be a better solution --
      // there should be a way to read the CVS Entries file without locking it for writing.
      try {Thread.sleep(100);
      } catch (Exception e) {throw new ChainableRuntimeException(e);
      }

      if (System.currentTimeMillis() - startedWaiting > CVS_DIR_ADD_TIMEOUT) {
        DialogManager.getInstance().showCustomError(
            IDEController.getInstance().createProjectContext(),
            "Timeout: failed to add to VCS: " + src.getName());
        return;
      }

    } while (!CvsFileStatus.getInstance().isKnown(src.getFileOrNull()));
  }

  /**
   * Try to create new Source directory/directories.
   * Path components are delimited by slash '/'.
   * Returns null if can't create one.
   * Only for directories!!!
   *
   * @return  created directory Source if succeed; null otherwise
   * @param path  path to created directory
   */
  public Source mkdirs(String path, boolean addToVcs) {
    String newPath = StringUtil.replace(path, LINK_SYMBOL, RIT_SEPARATOR);
    newPath = normalize(newPath);

    NBSource src = this;

    StringTokenizer st = new StringTokenizer(newPath, RIT_SEPARATOR);
    while (st.hasMoreTokens()) {
      String name = st.nextToken();
      src = (NBSource) src.mkdir(name, addToVcs);
      if (src == null) {
        break;
      }
    }

    return src;
  }

  public Source mkdirs(String path) {
    return mkdirs(path, true);
  }

  /**
   * Try to create new Source file.
   * Returns null if can't create one.
   * Invoke only on directories!!!
   *
   * @return  created file Source if succeed; null otherwise
   * @param name  name of file to create
   */
  public Source createNewFile(String name) {
    //System.out.println("NBSource.createNewFile: " + name);

    try {
      String ext = null;
      int pos = name.lastIndexOf('.');
      if (pos >= 0) {
        ext = name.substring(pos + 1);
        name = name.substring(0, pos);
      }

      FileObject dir = fileObject;
      while (dir != null && !dir.isFolder()) {
        dir = dir.getParent();
      }

      if (dir == null) {
        return null;
      }

      FileObject dst = dir.createData(name, ext);

      if (dst != null) {
        dst.refresh();
        if (inVcs()) {
          Vcs.add(dst, false);
        }
      }

      return getSource(dst);
    } catch (IOException e) {
      return null;
    }
  }

  // --- renameTo code starts

  /**
   * Try to rename Source.
   * Returns null if can't rename.
   *
   * @return  renamed file/directory Source if succeed; null otherwise
   *
   * @param dir  target directory
   * @param name  new name
   */
  public Source renameTo(final Source dir, final String name) {
    invalidateCaches();

    if (inVcs() && isDirectory()) {
      throw new UnsupportedOperationException("Renaming of folders not supported under CVS -- just create a new folder and delete the old one");
    }

    final Source result[] = new Source[] {null};

    long length = this.length();
    Source parent = this.getParent();
    String myName = getName();

    try {
      result[0] = renameToInCurrentThread(dir, name);
    } catch (Exception e) {
      log.warn("REFACTORIT EXCEPTION, PLEASE REPORT to support@refactorit.com", e);
      return null;
    }
    if (result[0] != null) {
      if (dir.getChild(name) != result[0]) {
        AppRegistry.getLogger(this.getClass()).debug("RENAMETO: getChild failed for " + name
            + ", result is " + dir.getChild(name));
      } else if (!result[0].getName().equals(name)) {
        AppRegistry.getLogger(this.getClass()).debug("RENAMETO :Wrong file name after name, should be "
            + name + ", is " + result[0].getName());
      } else if (result[0].length() != length) {
        AppRegistry.getLogger(this.getClass()).debug("RENAMETO:Wrong file length after rename, was "
            + length + ", is " + result[0].length());
      } else if (parent.getChild(myName) != null
          && parent.getChild(myName).length() != 0) {
        AppRegistry.getLogger(this.getClass()).debug("RENAMETO: Source " + myName +
            " probably not delete during rename cause file length is" +
            this.length());
      }
    }

    return result[0];
  }

  public Source renameToInCurrentThread(Source dir, String name) {
    if (Assert.enabled) {
      AppRegistry.getLogger(this.getClass()).debug("-------------------------------------------------------------------");
      AppRegistry.getLogger(this.getClass()).debug("REFACTORIT: renameTo: " + this +", newdir: " + dir
      + ", newname: " + name);
    }

    NBSource destinationDir = (NBSource) dir;
    String ext = getExtensionIfNotFolder(name);
    name = removeExtension(name);

    FileObject resultFileObject = null;
    try {
      getFromVssIfInvalidFileObject();

      resultFileObject = renameInVersionControl(destinationDir, name, ext);

      if (resultFileObject == null) {
        resultFileObject = renameInNbFilesystems(destinationDir, name, ext);
      }

//    if (Assert.enabled) {
      Assert.must(resultFileObject != null,
          "fileObject is null after rename of: " + this);
//    }

      invalidateCaches();

      return createRenameResultSource(resultFileObject);
    } catch (IOException cause) {
      failRenameGracefully(cause, resultFileObject);
      return null;
    }
  }

  private void failRenameGracefully(IOException cause,
      FileObject resultFileObject) {
    log.warn("REFACTORIT: NBSource.renameTo: ", cause);
    if (resultFileObject != null) {
      FileLock resultFileObjectLock = null;
      try {
        try {
          resultFileObjectLock = resultFileObject.lock();
        } catch (UserQuestionException e) {
          e.confirmed();
        }
      } catch(IOException e) {
        log.warn("Unable to delete a byproduct of rename (file " + resultFileObject + ")", e);
      }

      if (resultFileObjectLock != null) {
        try {
          resultFileObject.delete(resultFileObjectLock);

          RitDialog.showMessageDialog(
              IDEController.getInstance().createProjectContext(),
              "Failed to rename file: " + this.getAbsolutePath() + ".\n"
              + "Possibly file is locked either by CVS or OS.",
              "Error on rename", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
          log.warn(e.getMessage(), e);
        } finally {
          resultFileObjectLock.releaseLock();
        }
      }
    }
  }

  private NBSource createRenameResultSource(FileObject fileObject) {
    if (fileObject == null) {
      log.debug("Returning null for null fileObject after rename");
      return this;
    }

    NBSource newSource = getSource(fileObject);
    if (Assert.enabled) {
      Assert.must(this != newSource,
          "Source is same after rename: " + newSource);
    }
    newSource.setASTTree(this.getASTTree());
    return newSource;
  }

  private FileObject renameInNbFilesystems(NBSource destinationDir,
          String name, String ext) throws IOException {

    String relativeFolderName = getParent().getRelativePath();
    String packageName = StringUtil.replace(
        relativeFolderName, getSeparatorChar() + "", ".");

    String ignoredPropertyName = "org.netbeans.javacore.ignorePackages";

    String oldValue = System.getProperty(ignoredPropertyName);
    System.setProperty(ignoredPropertyName, oldValue + " " + packageName);

    try {
      return versionState.renameInNbFilesystems(this,destinationDir,name,ext);
    } finally {
      if(oldValue == null) {
        Properties all = System.getProperties();
        all.remove(ignoredPropertyName);
        System.setProperties(all);
      } else {
        System.setProperty(ignoredPropertyName, oldValue);
      }
    }
  }

  /**
   * @throws IOException if the file can not be renamed
   * @return null if the file may be possible to rename, but just
   *         not via VCS. On success returns the new fileObject.
   */
  private FileObject renameInVersionControl(
      NBSource destinationDir, String name, String ext) throws IOException {
    FileObject result = null;

    if (inVcs()) {
      if (RuntimePlatform.isWindows() &&
          destinationDir.getChildIgnoreCase(name + "." + ext) != null) {
        throw new IOException("Under Windows CVS, new file name must differ from existing file name in more than case");
      }

      int openedLine = NBContext.openedLine(getDataObject());

      result = Vcs.rename(
          this.fileObject, destinationDir.fileObject, name, ext);
      if(result == null) {
        throw new IOException("Failed to rename in VCS");
      }

      if (this.fileObject.isValid() &&
          FileSystemProperties.isVssFileSystem(this.fileObject)) {
        deleteOldFile();
      }

      NBContext.show(FileObjectUtil.getDataObjectOrNull(result), openedLine, false);
    }

    return result;
  }

  /** Also takes care of error checking & reporting */
  private void deleteOldFile() throws IOException {
    // TODO: VCS support does not support forms yet, but anyway,
    // it would be better to delete a DataObject here not a FileObject

    FileLock lock = null;
    try {
      lock = this.fileObject.lock();
    } catch (UserQuestionException e) {
      e.confirmed(); // TODO Should we ask it from user ourselves?
    }

    if (lock != null) {
      try {
        fileObject.delete(lock);
      } catch (Exception e) {
        log.warn("REFACTORIT EXCEPTION -- PLEASE REPORT to support@refactorit.com", e);
        DialogManager.getInstance().showWarning(
            IDEController.getInstance().createProjectContext(),
            "warning.nbsource.move.cannot.delete.old.file",
            "\"" + fileObject.getNameExt()
            + "\" was copied to its new location, but the old " +
            "copy could not be deleted. Delete it manually.");
      } finally {
        if (lock != null) {
          lock.releaseLock();
        }
      }
    }
  }

  private String removeExtension(String name) {
    if (isFile()) {
      int pos = name.lastIndexOf('.');
      if (pos >= 0) {
        name = name.substring(0, pos);
      }
    }

    return name;
  }

  private String getExtensionIfNotFolder(String name) {
    String ext = null;
    if (isFile()) {
      int pos = name.lastIndexOf('.');
      if (pos >= 0) {
        ext = name.substring(pos + 1);
      }
    }

    return ext;
  }

  public DataObject getDataObject() {
    return FileObjectUtil.getDataObjectOrNull(this.fileObject);
  }

  // -- renameTo code ends

  public FileObject getFileObject() {
    return fileObject;
  }

  public void invalidateCaches() {
    super.invalidateCaches();
  }

  public void renameFormFileIfExists(String oldName, String newName) {
    // Do nothing since NetBeans now takes care of renaming the form file
    // (because we are using DataObject.rename() instead of FileObject.rename() now).
    // Note that LocalFileSystem must still rename/move the form file itself.
  }

  public void moveFormFileIfExists(Source destination) {
    // Do nothing since NetBeans now takes care of renaming the form file
    // (because we are using DataObject.rename() instead of FileObject.rename() now).
    // Note that LocalFileSystem must still rename/move the form file itself.
  }

  public File getFileOrNull() {
    return FileObjectUtil.getFileOrNull(fileObject);
  }

  public char getSeparatorChar() {
    return SEPARATOR_CHAR;
  }

  public boolean exists() {
    return FileObjectUtil.exists(this.fileObject);
  }

  public boolean shouldSupportVcsInFilesystem() {
    return Options.vcsEnabled() &&
        VcsRunner.isVcsFileSystem(this.fileObject);
  }

  public boolean inVcs() {
    return shouldSupportVcsInFilesystem()
        && CvsFileStatus.getInstance().isKnown(getFileOrNull());
  }

//  private void juriTest() throws Exception {
//    int netbeansMajor = System.getProperty("org.openide.version").compareTo(
//            "20040819") > 0 ? 4 : 3;
//    String rootAbsolutePath = "D:/java/RalfTest/src";
//    String packagePartPath = "de/bjig/projectA";
//    String fileName = "X.java";
//    String sourceContent = "package de.bjig.projectA;public class X {}";
//    String testContent = "test content";
//    org.openide.filesystems.Repository repo = null;
//    org.openide.filesystems.FileSystem mainFS = null;
//    org.openide.filesystems.LocalFileSystem fsByPath = null;
//    org.openide.filesystems.FileObject fObject = null;
//    switch (netbeansMajor) {
//      case 3 : {
//        repo = org.openide.filesystems.Repository.getDefault();
//        mainFS = repo.getDefaultFileSystem();
//        java.util.Enumeration all = repo.getFileSystems();
//        while (all.hasMoreElements()) {
//          Object o = all.nextElement();
//          if (o instanceof org.openide.filesystems.LocalFileSystem) {
//            org.openide.filesystems.LocalFileSystem lfs = (org.openide.filesystems.LocalFileSystem) o;
//            if (lfs.getRootDirectory().equals(new File(rootAbsolutePath))) {
//              fsByPath = lfs;
//              break;
//            }
//          }
//        }
//        System.out.println("GOT FILESYSTEM:" + fsByPath.getSystemName());
//        fObject = fsByPath.findResource(packagePartPath + '/' + fileName);
//      }
//      case 4 : {
////        org.netbeans.api.java.classpath.ClassPath clPath = org.netbeans.api.java.classpath.ClassPath
////                .getClassPath(org.openide.filesystems.FileUtil
////                        .toFileObject(new File(rootAbsolutePath)),
////                        org.netbeans.api.java.classpath.ClassPath.SOURCE);
////        fObject = clPath.findResource(packagePartPath + '/' + fileName);
//      }
//    }
//    System.out.println("GOT FILE OBJECT:" + fObject.getName());
//    net.sf.refactorit.netbeans.common.vfs.NBSource nbSrc = net.sf.refactorit.netbeans.common.vfs.NBSource
//            .getSource(fObject);
//    if (nbSrc != null) {
//      System.out.println("CREATED NBSource:" + nbSrc.getName());
//      net.sf.refactorit.vfs.Source srcParent = nbSrc.getParent();
//      System.out.print("test getParent():");
//      if (srcParent == null)
//        System.out.println("FAILED(returned null)");
//      else
//        System.out.println("projectA".equals(srcParent.getName())
//                ? "OK"
//                : ("FAILED(returned:" + srcParent.getName()));
//      System.out.println("test getIdentifier(): " + "returned: "
//              + nbSrc.getIdentifier().toString());
//      System.out.print("test getChild():");
//      System.out.println(nbSrc.equals(srcParent.getChild(fileName))
//              ? "OK"
//              : "FAILED");
//      System.out.print("test getAbsolutePath():");
//      System.out.println(nbSrc.getAbsolutePath());
//      InputStream in = nbSrc.getInputStream();
//      BufferedReader br = new BufferedReader(new InputStreamReader(in));
//      System.out.print("test getInputStream():");
//      String contents = br.readLine();
//      System.out.println(sourceContent.equals(contents)
//              ? "OK"
//              : "FAILED:contents:" + contents);
//      br.close();
//      //getChildren()
//      System.out.println(srcParent.getChildren()[0]);
//      System.out.println(srcParent.getChildren()[1]);
//      System.out.println(srcParent.getChildren()[2]);
//
//      nbSrc.renameTo(srcParent, "Y.java");//check the IDE Explorer
//      nbSrc.renameTo(srcParent, "X.java");//check the IDE Explorer
//      System.out.print("test createNewFile():");
//      net.sf.refactorit.vfs.Source createdSrc = nbSrc.getParent()
//              .createNewFile("Z.java");//check the IDE Explorer
//      if (createdSrc != null && createdSrc.exists())
//        System.out.println("OK");
//      else
//        System.out.println("FAILED");
//      //delete()
//      System.out.print("test delete():");
//      createdSrc.delete();//check the IDE Explorer
//      if (createdSrc != null && !createdSrc.exists())
//        System.out.println("OK");
//      else
//        System.out.println("FAILED");
//      //getContent()
//      System.out.print("test getContent():");
//      if (sourceContent.equals(new String(nbSrc.getContent())))
//        System.out.println("OK");
//      else
//        System.out.println("FAILED");
//      //getDataObject()
//      System.out.print("test getDataObject():");
//      if (nbSrc.getDataObject() != null)
//        System.out.println("OK");
//      else
//        System.out.println("FAILED");
//      //getDisplayPath()
//      System.out.print("test getDisplayPath():");
//      if (nbSrc.getDisplayPath().equalsIgnoreCase(
//              rootAbsolutePath + getSeparatorChar() + packagePartPath
//              + getSeparatorChar() + fileName))
//        System.out.println("OK");
//      else
//        System.out.println("FAILED");
//      //getName()
//      System.out.print("test getName():");
//      if (fileName.equals(nbSrc.getName())
//              && nbSrc.getParent().getName().equals(
//                      packagePartPath.substring(packagePartPath
//                              .lastIndexOf('/') + 1)))
//        System.out.println("OK");
//      else
//        System.out.println("FAILED");
//
//      nbSrc.setContent(testContent.getBytes());
//      //fObject.get('x');
//      org.openide.filesystems.FileLock lock = null;
//      lock = fObject.lock();
//      fObject.rename(lock, "Y", "java");
//      lock.releaseLock();
//      System.out.println(nbSrc.getParent().getName());
//      System.out.println(fObject.isValid());
//      fObject = fObject.move(lock, fObject.getParent().getChildren()[1], "X",
//              "java");
//      fObject = org.openide.filesystems.FileUtil.moveFile(fObject.getParent()
//              .getParent().getChildren()[0].getChildren()[2], fObject
//              .getParent().getParent().getChildren()[2], "X.java");
//      System.out.println(nbSrc.getDataObject());
//    }
//  }
}
