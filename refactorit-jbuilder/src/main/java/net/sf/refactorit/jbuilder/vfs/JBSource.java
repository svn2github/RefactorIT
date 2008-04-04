/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jbuilder.vfs;


import com.borland.primetime.ide.Browser;
import com.borland.primetime.node.FileNode;
import com.borland.primetime.node.Node;
import com.borland.primetime.node.Project;
import com.borland.primetime.vfs.Buffer;
import com.borland.primetime.vfs.FileFilesystem;
import com.borland.primetime.vfs.Filesystem;
import com.borland.primetime.vfs.Url;
import com.borland.primetime.vfs.VFS;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Arrays;

import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.ChainableRuntimeException;
import net.sf.refactorit.common.util.FileReadWriteUtil;
import net.sf.refactorit.ui.RuntimePlatform;
import net.sf.refactorit.utils.RefactorItConstants;
import net.sf.refactorit.utils.SwingUtil;
import net.sf.refactorit.vfs.AbstractSource;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourceMap;


/**
 * Implementation of Source VFS for JBuilder.
 *
 * @author  Igor Malinin
 */
public class JBSource extends AbstractSource {
  // dummy buffer
  private static byte[] buf;

  private final Url url;

  private final Object identifier;
  private JBSource parent;

  /** Creates new JBSource */
  private JBSource(Url url) {
    this(null, url);
  }

  /** Creates new JBSource */
  private JBSource(JBSource parent, Url url) {
    this.url = url;
    this.identifier = getIdentifier(url);
    setParent(parent);
  }

  public static final JBSource getSource(Url url) {
    Source result = SourceMap.getSource(getIdentifier(url));
    if (result == null || !(result instanceof JBSource)) {
      result = new JBSource(url);
      SourceMap.addSource(result);
    }

    return (JBSource) result;
  }

  public static final JBSource getSource(File file) {
    Url url = new Url(file);
    return getSource(url);
  }

  public static final JBSource getSource(JBSource parent, Url url) {
    Source result = SourceMap.getSource(getIdentifier(url));
    if (result == null || !(result instanceof JBSource)) {
      result = new JBSource(parent, url);
      SourceMap.addSource(result);
    }

    if (parent != null && ((JBSource) result).getParent() == null) {
      ((JBSource) result).setParent(parent);
    }

    return (JBSource) result;
  }

  public boolean exists() {
    return VFS.exists(url);
  }

  private static final Object getIdentifier(final Url url) {
    return url.getFullName();
  }

  public final Object getIdentifier() {
    return this.identifier;
  }

  public byte[] getContent() throws IOException {
    byte[] buffer = null;
    InputStream inputStream = null;
    try {
      inputStream = getInputStream();
      buffer = FileReadWriteUtil.read(inputStream, (int) length());
    } catch (IOException e) {
      buffer = new byte[0];
    } finally {
      if (inputStream != null) {
        inputStream.close();
      }
      if (buffer == null) {
        buffer = new byte[0];
      }
    }

    return buffer;
  }

  /**
   * Parent directory
   *
   * @return  parent Source directory
   */
  public Source getParent() {
    return parent;
  }

  private void setParent(JBSource parent) {
    this.parent = parent;
  }

  /**
   * Child Source.
   * Returns null if does not exists.
   *
   * @param path relative name of child Source
   * @return  child Source
   */
  public Source getChild(String path) {
    Url dst = url;
    Filesystem fs = dst.getFilesystem();
    JBSource src = this;

    path = normalize(path);
    StringTokenizer st = new StringTokenizer(path, RIT_SEPARATOR);
    while (st.hasMoreTokens()) {
      String name = st.nextToken();

      dst = fs.getChild(dst, name);
      if (!VFS.exists(dst)) {
        return null;
      }

      src = JBSource.getSource(src, dst);
    }

    return src;
  }

  /**
   * Name of the file/directory
   *
   * @return  name of the file/directory
   */
  public String getName() {
    return url.getName();
  }

  /**
   * Low-level OS path for this source.
   *
   * @return absolute path in sense of OS
   */
  public String getAbsolutePath() {
    return url.getFullName();
  }

  /**
   * Tests if this file is writable.
   *
   * @return  true if this file is writable
   */
  public boolean canWrite() {
    return!VFS.isReadOnly(url);
  }

  /**
   * Makes file editable if possible. Usable with files from VisualSourceSafe
   * and when files are checked out as read-only.
   *
   * @return true if succeeded to make file writable
   */
  public boolean startEdit() {
    return canWrite(); // TODO implement
  }

  /**
   * Time of last modification
   *
   * @return  last modified time
   */
  public long lastModified() {
    return (new File(url.getFile())).lastModified();
    //return VFS.getLastModified(url);
  }

  /**
   * Set time of last modification
   *
   * @param time  last modified time
   */
  public boolean setLastModified(long time) {
    invalidateCaches();
    return url.getFilesystem().setLastModified(url, time);
  }

  /**
   * Only for files!!!
   *
   * @return  length of the file
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
      return FileReadWriteUtil.length(VFS.getInputStream(url));
    } catch (IOException e) {
      AppRegistry.getExceptionLogger()
          .error(e, "Can't get length of: " + this, this);
      return 0;
    }
  }

  /**
   * Tests if this is the file
   *
   * @return  true if this Source object represents a file
   */
  public boolean isFile() {
    return (!VFS.isDirectory(url) && VFS.exists(url));
  }

  /**
   * Tests if this is the directory
   *
   * @return  true if this Source object represents a directory
   */
  public boolean isDirectory() {
    return VFS.isDirectory(url);
  }

  /**
   * Only for files!!! Dont't forget to close it!
   *
   * @return  InputStream to read contents of the file
   */
  public InputStream getInputStream() throws IOException {
//    return VFS.getInputStream( url );
    InputStream in = null;
//    try {
    in = url.getFilesystem().getInputStream(url);
//    } catch (Exception e) {
//      try {
//        saveCaches();
//      } catch (IOException ignore) {
//      }
//
//      in = VFS.getInputStream(url);
//    }
    return in;
  }

  /**
   * Only for files!!! Dont't forget to close it!
   *
   * @return OutputStream to write contents of the file
   */
  public OutputStream getOutputStream() throws IOException {
    // NOTE: don't use VFS output stream - it uses Buffer and doesn't work under JBX
    //return VFS.getOutputStream( url );

    try {
      saveBuffers();
    } catch (IOException ignore) {
    }

    OutputStream outputStream = null;
    try {
      outputStream = url.getFilesystem().getOutputStream(url);
    } catch (IOException ex) {
      ex.printStackTrace();

//      outputStream = VFS.getOutputStream(url);
    }

//System.err.println("outputStream: " + outputStream);
    return outputStream;
  }

  public static void saveBuffers() throws IOException {
    Buffer abuffer[] = Buffer.getModifiedBuffers();
    for (int i = 0; i < abuffer.length; i++) {
      Buffer buffer = abuffer[i];
      buffer.save();
    }
  }

  public static void updateBuffers() throws IOException {
    Buffer.updateModifiedBuffers();
  }

  /**
   * Try to delete this file/directory.
   */
  public boolean delete() {
    invalidateCaches();
    Source[] children = getChildren();
    if (children != null && children.length > 0) { // not empty - can't delete
      return false;
    }


    try {
      saveBuffers();
      closeOpenChildren(url);
      VFS.delete(url);
      deleteFromJBProject();

      return true;
    } catch (IOException ignore) {}

    return false;
  }

  private void deleteFromJBProject() {
    Node[] nodes = findNodes(url);
    for (int i = 0; i < nodes.length; i++) {
      // Under JB7 the node seems to be null (because the url itself is deleted already
      // when this method is called), but under older versions it is not.
      try {
        nodes[i].fireNodeRenamed();
      } catch (Exception e) {}
      try {
        nodes[i].setParent(null);
      } catch (Exception e) {}
    }
  }

  public Source[] getChildren() {
    Url[] urls = VFS.getChildren(url, Filesystem.TYPE_BOTH);
    if (urls == null) {
      return Source.NO_SOURCES;
    }

    int len = urls.length;
    Source[] children = new Source[len];

    for (int i = 0; i < len; i++) {
      children[i] = JBSource.getSource(this, urls[i]);
    }

    Arrays.sort(children);

    return children;
  }

  public Source[] getChildren(Source.SourceFilter sourceFilter) {
    Url[] urls = VFS.getChildren(url, Filesystem.TYPE_BOTH);
    if (urls == null) {
      return Source.NO_SOURCES;
    }

    int len = urls.length;
    List children = new ArrayList(len);
    for (int i = 0; i < len; i++) {
      Source source = JBSource.getSource(this, urls[i]);
      if (sourceFilter.accept(source)) {
        children.add(source);
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
  public Source mkdir(String name) {
    Filesystem fs = url.getFilesystem();
    if (fs instanceof FileFilesystem) {
      Url dst = fs.getChild(url, name);
      File f = dst.getFileObject();
      if (f == null) {
        return null;
      }

      if (f.exists() && f.isDirectory()) {
        return JBSource.getSource(this, dst);
      }

      if (f.mkdir()) {
        refresh();
        return JBSource.getSource(this, dst);
      }
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
  public Source createNewFile(String name) throws IOException {
    Filesystem fs = url.getFilesystem();
    if (fs instanceof FileFilesystem) {
      Url dst = fs.getChild(url, name);
      File f = dst.getFileObject();

      if (f.createNewFile()) {
        addToJBProject(dst);

        return JBSource.getSource(this, dst);
      }
    }

    return null;
  }

  private void addToJBProject(Url childUrl) {
    Browser.getActiveBrowser().getActiveProject().refresh();

    Node[] parentNodes = findNodes(childUrl.getParent());
    for (int i = 0; i < parentNodes.length; i++) {
      Project pr = getBrowser(parentNodes[i]).getActiveProject();
      pr.fireChildrenChanged(parentNodes[i]);

      // FIXME Commented out to fix bug #1620, but seems that single added source disappear then.
      /*
      Node[] childNodes = findNodes(childUrl);
      if (childNodes.length == 0) {
        Node childNode = pr.getNode(childUrl);
        childNode.setParent(pr);
      }*/
    }
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
  public Source renameTo(Source dir, String name) {
    invalidateCaches();
    JBSource dst = (JBSource) dir;
    try {
      Url udir = dst.url;
      final Url udst = udir.getFilesystem().getChild(udir, name);

      Runnable runnable = new Runnable() {
        public void run() {
          try {
            Thread.sleep(RuntimePlatform.getFileDatePrecision()); // Fixes RIM-396
            if (VFS.isDirectory(url)) {
              renameDir(udst);
            } else {
              renameFile(udst);
            }
            Thread.sleep(RuntimePlatform.getFileDatePrecision()); // Fixes RIM-396
          } catch (Exception ex) {
            throw new ChainableRuntimeException(ex);
          }
        }
      };

      SwingUtil.invokeInEdtUnderNetBeansAndJB(runnable);

      if (Assert.enabled) {
        if (url.getFullName().equals(udst.getFullName())) {
          new Exception("Files have the same name after rename: "
              + udst.getFullName()).printStackTrace(System.err);
        }
      }
      JBSource newSource = JBSource.getSource(dst, udst);
      newSource.setASTTree(this.getASTTree());
      return newSource;
    } catch (Exception e) {
      e.printStackTrace(RuntimePlatform.console);
    }

    return null;
  }

  void renameDir(Url udst) throws IOException {
    VFS.rename(url, udst);
    refresh();
  }

  void renameFile(Url dst) throws IOException {
    Node[] nodes = Browser.getAllOpenNodes();
    for (int i = 0; i < nodes.length; i++) {
      Node node = nodes[i];
      if (node instanceof FileNode) {
        FileNode fn = (FileNode) node;
        if (fn.getUrl().equals(url)) {
          try {
            fn.rename(dst);
            return;
          } catch (Exception e) {
            if (RefactorItConstants.debugInfo) {
              AppRegistry.getExceptionLogger().debug(e,this.getClass());
            }

            throw new IOException(e.getMessage());
          }
        }
      }
    }

    VFS.rename(url, dst);
    refresh();
  }

  void refresh() {
    try {
      saveBuffers(); // just in case
    } catch (IOException ignore) {}
    try {
      updateBuffers(); // just in case
    } catch (IOException ignore) {}

    Browser.getActiveBrowser().getActiveProject().refresh();

    Node[] nodes = Browser.getAllOpenNodes();
    for (int i = 0; i < nodes.length; i++) {
      Node node = nodes[i];
      node.check();
      if (node instanceof FileNode) {
        try {
          ((FileNode) node).findBuffer().updateContent();
        } catch (NullPointerException e) {
          // sometimes there is no buffer
        }
      }
//      getBrowser(node).getActiveProject().fireNodeChanged(node);
    }
  }

  private static Browser getBrowser(Node node) {
    Browser[] browsers = Browser.getBrowsers();

    Project project = node.getProject();
    for (int j = 0; j < browsers.length; j++) {
      Browser b = browsers[j];
      Project p = b.getActiveUserProject();
      if (project == p) {
        return b;
      }
    }

    return null;
  }

  private static void closeOpenChildren(Url url) {
    Node[] openChildren = findOpenChildNodes(url);

    for (int i = 0; i < openChildren.length; i++) {
      try {
        getBrowser(openChildren[i]).closeNode(openChildren[i]);
      } catch (Exception e) {}
    }
  }

  private Node[] findNodes(Url url) {
    List nodes = new ArrayList(1);

    Browser[] browsers = Browser.getBrowsers();
    for (int i = 0; i < browsers.length; i++) {
      Project project = browsers[i].getActiveProject();
      Node node = project.findNode(url);
      if (node != null) {
        nodes.add(node);
        continue;
      }
      node = project.findNode(VFS.getCanonicalUrl(url));
      if (node != null) {
        nodes.add(node);
        continue;
      }

      project = browsers[i].getActiveUserProject();
      if (node != null) {
        nodes.add(node);
        continue;
      }
      node = project.findNode(VFS.getCanonicalUrl(url));
      if (node != null) {
        nodes.add(node);
        continue;
      }
    }

    return (Node[]) nodes.toArray(new Node[nodes.size()]);
  }

  private static FileNode[] findOpenNodes(Url url) {
    List openNodes = new ArrayList(1);

    Node[] nodes = Browser.getAllOpenNodes();
    for (int i = 0; i < nodes.length; i++) {
      if (nodes[i] instanceof FileNode) {
        FileNode fn = (FileNode) (nodes[i]);
        if (fn.getUrl().equals(url)) {
          openNodes.add(fn);
        }
      }
    }

    return (FileNode[]) openNodes.toArray(new FileNode[openNodes.size()]);
  }

  private static FileNode[] findOpenChildNodes(Url url) {
    List openNodes = new ArrayList(1);

    Node[] nodes = Browser.getAllOpenNodes();
    for (int i = 0; i < nodes.length; i++) {
      if (nodes[i] instanceof FileNode) {
        FileNode fn = (FileNode) (nodes[i]);
        if (fn.getUrl().isChild(url)) {
          openNodes.add(fn);
        }
      }
    }

    return (FileNode[]) openNodes.toArray(new FileNode[openNodes.size()]);
  }

  public Url getUrl() {
    return url;
  }

  public void invalidateCaches() {
    super.invalidateCaches();
  }

  public void renameFormFileIfExists(String oldName, String newName) {}

  public void moveFormFileIfExists(Source destination) {}

  public File getFileOrNull() {
    return null;
  }

  public char getSeparatorChar() {
    return File.separatorChar;
  }

  public boolean shouldSupportVcsInFilesystem() {
    return false;
  }

  public boolean inVcs() {
    return false;
  }

}
