/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jdeveloper.vfs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.ChainableRuntimeException;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.jdeveloper.RefactorItController;
import net.sf.refactorit.utils.FileUtil;
import net.sf.refactorit.vfs.AbstractSource;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourceMap;
import net.sf.refactorit.vfs.SourcePath;

import oracle.ide.addin.Context;
import oracle.ide.model.DeployableTextNode;
import oracle.ide.model.Node;
import oracle.ide.model.NodeFactory;
import oracle.ide.model.Project;
import oracle.ide.net.URLFactory;
import oracle.jdeveloper.model.JavaSourceNode;
import oracle.jdeveloper.model.JspSourceNode;


/**
 * @author Tanel
 * @author Anton Safonov
 */
public class JDevSourceDir extends AbstractSource {
  private final File file;
  private final String packageName;
  private final Object identifier;

  /** Creates new JDevSourceDir */
  private JDevSourceDir(File dir, String packageName) {
    this.file = dir;
    this.packageName = packageName;
    this.identifier = getIdentifier(dir);
  }

  public static final JDevSourceDir getSource(File dir, String packageName) {
    Source result = SourceMap.getSource(getIdentifier(dir));
    if (result == null || !(result instanceof JDevSourceDir)) {
      result = new JDevSourceDir(dir, packageName);
      SourceMap.addSource(result);
    }

    return (JDevSourceDir) result;
  }

  public boolean exists() {
    return this.file != null && this.file.exists();
  }

  public void invalidateCaches() {
    super.invalidateCaches();
  }

  /**
   * Try to create new Source directory. Returns null if can't create one. Only
   * for directories!!!
   *
   * @return created directory Source if succeed; null otherwise
   * @param name
   *          name of file to create
   */
  public Source mkdir(String name) {
    // FIXME: implement this
    throw new RuntimeException("Not implemented method invoked!");
  }

  /**
   * Only for files!!! Dont't forget to close it!
   *
   * @return InputStream to write contents of the file
   */
  public OutputStream getOutputStream() throws IOException {
    throw new RuntimeException("This method cannot be applied to directory!");
  }

  /**
   * Parent directory.
   *
   * @return parent Source directory
   */
  public Source getParent() {
    File parentDir = file.getParentFile();
    if (parentDir != null) {
      String parentPackage = "";
      if (packageName.length() > 0) {
        int lastDotPos = packageName.lastIndexOf('.');
        if (lastDotPos != -1) {
          parentPackage = packageName.substring(0, lastDotPos);
        }

        return JDevSourceDir.getSource(file.getParentFile(), parentPackage);
      }
    }

    return null;
  }

  /**
   * Tests if this file is writable.
   *
   * @return true if this file is writable
   */
  public boolean canWrite() {
    return file.canWrite();
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

  public Source[] getChildren() {
    Source[] retList = getChildrenAsFromIDE();
    return retList;
  }

  public Source[] getChildren(Source.SourceFilter sourceFilter) {
    Source[] children= getChildrenAsFromIDE();
    return filterChildren(children, sourceFilter);
  }

  private static Source[] filterChildren(Source[] children,
          Source.SourceFilter sourceFilter) {
    Source[] filteredChildren = new Source[children.length];
    int k = 0;
    for (int i = 0; i < children.length; i++) {
      if (sourceFilter.accept(children[i])) {
        filteredChildren[k] = children[i];
        k++;
      }
    }
    children = new Source[k];
    System.arraycopy(filteredChildren, 0, children, 0, k);
    return children;
  }

  private Source[] getChildrenAsFromIDE() {
    //Context context =
    // RefactorItController.getJDevInstance().getLastEventContext();

    SourcePath sourcePath = IDEController.getInstance().getActiveProject().getPaths().getSourcePath();
    ArrayList result = new java.util.ArrayList(200);
    Node node;
    try {
      node = NodeFactory.find(URLFactory.newDirURL(file));
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }

    //Node [] nodes =
    // oracle.jdeveloper.runner.Source.getProjectFileList(jproject);

    if (node == null) {
      return getChildrenAsFromFS();
    }

    Iterator it = node.getChildren();

    JDevSource jdevSource;
    if(it!=null)
    while (it.hasNext()) {
      Node nodei = (Node) it.next();
      jdevSource = null;
      if ((JavaSourceNode.class).isAssignableFrom(nodei.getClass())) {
        JavaSourceNode javaSource = (JavaSourceNode) nodei;
        jdevSource = JDevSource.getSource(javaSource);
      } else if ((JspSourceNode.class).isAssignableFrom(nodei.getClass())) {
        JspSourceNode jspSource = (JspSourceNode) nodei;
        jdevSource = JDevSource.getSource(jspSource);
      } else if ((DeployableTextNode.class).isAssignableFrom(nodei.getClass())) {
        DeployableTextNode deployableTextNode = (DeployableTextNode) nodei;
        jdevSource = JDevSource.getSource(deployableTextNode);
      }
      if (jdevSource != null
              && !sourcePath.isIgnoredPath(jdevSource.getAbsolutePath())) {
        result.add(jdevSource);
      }
    }
    /*
     * for (int i = 0; i < nodes.length; i++) { if
     * ((JavaSourceNode.class).isAssignableFrom(nodes[i].getClass())) {
     * JavaSourceNode javaSource = (JavaSourceNode) nodes[i]; URL url =
     * javaSource.getURL(); fileName=URLDecoder.decode(url.getFile()); File
     * currentFileParent = new File(fileName).getParentFile(); if
     * (currentFileParent.equals(this.file)) {
     * result.add(JDevSource.getSource(javaSource)); } } else if (
     * (JspSourceNode.class).isAssignableFrom(nodes[i].getClass())) {
     * JspSourceNode jspSource = (JspSourceNode) nodes[i]; URL url =
     * jspSource.getURL(); fileName=URLDecoder.decode(url.getFile()); File
     * currentFileParent = new File(fileName).getParentFile(); if
     * (currentFileParent.equals(this.file)) { // FIXME: JDev JspSourceNode has
     * getPackage method, // but it needs JProject, we are not using it now!
     * [tonis]
     * //result.add(JDevSource.getSource(jspSource,jspSource.getPackage(jproject)));
     * result.add(JDevSource.getSource(jspSource)); }
     *  } }
     */
    // add folders
    File[] allFiles = file.listFiles();
    if (allFiles != null) {
      for (int i = 0; i < allFiles.length; i++) {
        File child = allFiles[i];
        if (!child.isDirectory()
            || sourcePath.isIgnoredPath(child.getAbsolutePath())) {
          continue;
        }
        addChildDir(result, child);
      }
    }

    Collections.sort(result);

    return (Source[]) result.toArray(new Source[result.size()]);
  }

  private Source[] getChildrenAsFromFS() {
    ArrayList result = new java.util.ArrayList(200);
    File[] allFiles = file.listFiles();

    SourcePath sourcePath = IDEController.getInstance().getActiveProject().getPaths().getSourcePath();

    if (allFiles != null) {
      for (int i = 0; i < allFiles.length; i++) {
        File child = allFiles[i];
        if (child.isDirectory()) {
          if (sourcePath.isIgnoredPath(child.getAbsolutePath())) {
            continue;
          }
          addChildDir(result, child);
          continue;
        }
        if (!((JDevSourcePath) sourcePath).isValidSource(child.getAbsolutePath())
            || sourcePath.isIgnoredPath(child.getAbsolutePath())) {
          continue;
        }
        addChildSource(result, child);
      }
    }

    Collections.sort(result);
    return (Source[]) result.toArray(new Source[result.size()]);
  }

  /**
   * Properly makes a <code>JDevSourceDir</code> out of the given child
   * <code>File</code> and adds it to the result list.
   *
   * Is used in <code>getChildren</code> when it's clear that the given child
   * <code>File</code> is a directory
   *
   * @param result
   * @param child
   */
  private void addChildDir(List result, File child) {
    String childPackage = packageName;
    if (childPackage.length() > 0) {
      childPackage += '.';
    }
    childPackage += child.getName();
    result.add(JDevSourceDir.getSource(child, childPackage));
  }

  /**
   * Makes a <code>JavaSourceNode</code>,<code>JspSourceNode</code> or
   * <code>DeployableTextNode</code> out of the given child <code>File</code>,
   * then creates a JDevSource out it and adds the latter to the given result
   * <code>List</code>.
   *
   * @param result
   * @param child
   */
  private void addChildSource(List result, File child) {
    URL  childUrl = URLFactory.newFileURL(child);

    DeployableTextNode childAsDnode;
    try {
      if (child.getName().endsWith("java")) {
        childAsDnode = (DeployableTextNode) NodeFactory.findOrCreate(
                JavaSourceNode.class, childUrl);
      } else if (child.getName().endsWith("jsp")) {
        childAsDnode = (DeployableTextNode) NodeFactory.findOrCreate(
                JspSourceNode.class, childUrl);
      } else {
        System.err
                .println("while adding a file ("
                        + child.getName()
                        + ") from manually specified path couldn't"
                        + " couldn't recognize it's extension and initialized it as a DeployableTextNode");
        childAsDnode = (DeployableTextNode) NodeFactory.findOrCreate(
                DeployableTextNode.class, childUrl);
        ;
      }
      childAsDnode.setURL(childUrl);
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }

    JDevSource childAsSource = JDevSource.getSource(childAsDnode, packageName);
    result.add(childAsSource);
  }

  private static final Object getIdentifier(final File file) {
    return file.getAbsolutePath();
  }

  /**
   * this should return something that identifies the source and is fast to
   * operate with when getting hashcode or testing equals
   */
  public final Object getIdentifier() {
    return this.identifier;
  }

  /**
   * Only for files!!! Dont't forget to close it!
   *
   * @return InputStream to read contents of the file
   */
  public InputStream getInputStream() throws IOException {
    throw new RuntimeException("This method cannot be applied to directory!");
  }

  public byte[] getContent() throws IOException {
    throw new RuntimeException("This method cannot be applied to directory!");
  }

  /**
   * Name of the file/directory.
   *
   * @return name of the file/directory
   */
  public String getName() {
    return file.getName();
  }

  /**
   * Try to create new Source directory/directories. Path components are
   * delimited by slash '/'. Returns null if can't create one. Only for
   * directories!!!
   *
   * @return created directory Source if succeed; null otherwise
   * @param path
   *          path to created directory
   */
  public Source mkdirs(String path) {
    if (path.length() == 0) {
      return this;
    }

    File newDir = new File(file, path);

    newDir.mkdirs();

    if (newDir.exists()) {
      String newPackageName = packageName;
      if (packageName.length() > 0) {
        newPackageName += '.';
      }
      newPackageName += StringUtil.replace(StringUtil.replace(path, '/', '.'),
              '\\', '.');
      return JDevSourceDir.getSource(newDir, newPackageName);
    }

    return null;
  }

  /**
   * Try to create new Source file. Returns null if can't create one. Only for
   * directories!!!
   *
   * @return created file Source if succeed; null otherwise
   * @param name
   *          name of file to create
   */
  public Source createNewFile(String name) throws IOException {
    File dst = new File(file, name);

    if (dst.createNewFile()) {

      DeployableTextNode node = null;
      Context context = RefactorItController.getJDevInstance()
              .getLastEventContext();

      Project project = context.getProject();
      boolean modified = false;

      if (FileUtil.isJavaFile(name)) {
        try {
          node = (DeployableTextNode) NodeFactory.findOrCreate(
                  JavaSourceNode.class, URLFactory.newFileURL(dst));
          modified = true;
        } catch (Exception e) {
          throw new IOException("Couldn't create JavaSourceNode from file: "
                  + e);
        }
      } else if (FileUtil.isJspFile(name)) {
        try {
          node = (DeployableTextNode) NodeFactory.findOrCreate(
                  JspSourceNode.class, URLFactory.newFileURL(dst));
          modified = true;
        } catch (Exception e) {
          throw new IOException("Couldn't create JspSourceNode from file: " + e);
        }
      }
      if (modified) {
        try {
          project.add(node, true);
          project.markDirty(true);
          project.save(true);
        } catch (IOException e) {
          throw new ChainableRuntimeException("Project couldn't be saved: " + e);
        }
        return JDevSource.getSource(node, packageName);
      }
    }

    return null;
  }

  /**
   * Tests if this is the directory
   *
   * @return true if this Source object represents a directory
   */
  public boolean isDirectory() {
    return true;
  }

  /**
   * Set time of last modification
   *
   * @param time
   *          last modified time
   */
  public boolean setLastModified(long time) {
    return file.setLastModified(time);
  }

  /**
   * Time of last modification
   *
   * @return last modified time
   */
  public long lastModified() {
    return file.lastModified();
  }

  /**
   * Only for files!!!
   *
   * @return lenght of the file
   */
  public long length() {
    return file.length();
  }

  /**
   * Child Source. Returns null if does not exists.
   *  // FIXME: for path with slashes slow because uses getChildren
   *
   * @param path
   *          relative name of source
   * @return child Source
   */
  public Source getChild(String path) {
    path = normalize(path);
    if (path.indexOf(RIT_SEPARATOR_CHAR) == 0) {
      path = path.substring(1, path.length());
    }

    StringTokenizer st = new StringTokenizer(path, RIT_SEPARATOR);
    Source src = this;

    while (st.hasMoreTokens()) {
      String name = st.nextToken();
      if (name.length() == 0) {
        continue;
      }

      Source newSrc = null;

      Source[] children = src.getChildren();
      for (int i = 0; i < children.length && src != null; i++) {
        if (children[i].getName().equals(name)) {
          newSrc = children[i];
          break;
        }
      }

      src = newSrc;
    }

    return src;
  }

  /**
   * Try to delete this file/directory.
   */
  public boolean delete() {
    String[] children = file.list();
    if (children != null && children.length > 0) {
      return false; // not empty
    }


    return file.delete();
  }

  /**
   * Tests if this is the file
   *
   * @return true if this Source object represents a file
   */
  public boolean isFile() {
    return false;
  }

  /**
   * Path from the root of filesystem this file belongs to. Path components are
   * delimited by slash '/'. Root of the file system is "". Example:
   * "com/package/SourceFile.java"
   *
   * @return absolute path (relative to filesystem root)
   */
  public String getAbsolutePath() {
    return file.getAbsolutePath();
  }

  /**
   * Path from the root of filesystem this file belongs to. Path components are
   * delimited by slash '/'. Root of the file system is "". Example:
   * "com/package/SourceFile.java"
   *
   * @return absolute path (relative to filesystem root)
   */
  //  public String getRelativePath() {
  //    // FIXME: wrong when SourcePath starts in the middle of the package!!!
  //    //System.out.println("JDevSourceDir: relative path = " +
  // StringUtil.replace(packageName, '.', '/'));
  //    return StringUtil.replace(packageName, '.', '/');
  //  }
  /**
   * Try to rename Source. Returns null if can't rename.
   *
   * @return renamed file/directory Source if succeed; null otherwise
   *
   * @param dir
   *          target directory
   * @param name
   *          new name
   */
  public Source renameTo(Source dir, String name) {
    File destination
        = new File(dir.getAbsolutePath() + getSeparatorChar() + name);

    if (file.renameTo(destination)) {

      if (Assert.enabled) {
        if (file.getAbsolutePath().equals(destination.getAbsolutePath())) {
          new Exception("Files have the same name after rename: "
                  + destination.getAbsolutePath()).printStackTrace(System.err);
        }
      }
      return JDevSourceDir.getSource(destination, packageName); // is this ok
                                                                // that
                                                                // packageName
                                                                // is the same?
    } else {
      return null;
    }
  }

  public String getPackageName() {
    return this.packageName;
  }

  public void renameFormFileIfExists(String oldName, String newName) {
  }

  public void moveFormFileIfExists(Source destination) {
  }

  public File getFileOrNull() {
    return file;
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
