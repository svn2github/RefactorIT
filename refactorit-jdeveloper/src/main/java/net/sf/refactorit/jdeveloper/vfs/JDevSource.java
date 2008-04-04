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
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.ChainableRuntimeException;
import net.sf.refactorit.common.util.FileReadWriteUtil;
import net.sf.refactorit.jdeveloper.RefactorItController;
import net.sf.refactorit.vfs.AbstractSource;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourceMap;

import oracle.ide.addin.Context;
import oracle.ide.addin.UpdateMessage;
import oracle.ide.cmd.RenameMessage;
import oracle.ide.cmd.buffer.EditProcessor;
import oracle.ide.model.DeployableTextNode;
import oracle.ide.model.Project;
import oracle.ide.model.TextNode;
import oracle.ide.net.URLFactory;
import oracle.javatools.buffer.TextBuffer;
import oracle.jdeveloper.model.JavaSourceNode;



/**
 * @author Tanel
 * @author Anton Safonov
 */
public class JDevSource extends AbstractSource {
  private final DeployableTextNode sourceNode;
  private final File compilationUnit;
  private final String packageName;
  private final Object identifier;

//  private long length = -1;
//  private long lastModified = -1;

  private JDevSource(DeployableTextNode sourceNode) {
    // FIXME it could be a bug here - what if file is still the same,
    // but package declaration was edited inside?
    this(sourceNode, sourceNode instanceof JavaSourceNode ?
        ((JavaSourceNode) sourceNode).getPackage() : "");
  }

  private JDevSource(DeployableTextNode sourceNode, String packageName) {
    this.sourceNode = sourceNode;
    this.packageName = packageName;
    this.compilationUnit = new File(URLDecoder.decode(sourceNode.getURL().getFile()));
    this.identifier = getIdentifier(sourceNode);
  }

  public static final JDevSource getSource(
      DeployableTextNode sourceNode, String packageName
      ) {
    Source result = SourceMap.getSource(getIdentifier(sourceNode));
    if (result == null || !(result instanceof JDevSource)) {
      result = new JDevSource(sourceNode, packageName);
      SourceMap.addSource(result);
    }

    return (JDevSource) result;
  }

  public static final JDevSource getSource(DeployableTextNode sourceNode) {
    Source result = SourceMap.getSource(getIdentifier(sourceNode));
    if (result == null || !(result instanceof JDevSource)) {
      result = new JDevSource(sourceNode);
      SourceMap.addSource(result);
    }

    return (JDevSource) result;
  }

  public boolean exists() {
    return this.compilationUnit != null && this.compilationUnit.exists();
  }

  /**
   * Parent directory.
   *
   * @return  parent Source directory
   */
  public Source getParent() {
    return JDevSourceDir.getSource(compilationUnit.getParentFile(), packageName);
  }

  private static final Object getIdentifier(final DeployableTextNode node) {
    return URLDecoder.decode(node.getURL().getFile());
  }

  /**
   * this should return something that identifies the source and is fast to
   * operate with when getting hashcode or testing equals
   */
  public Object getIdentifier() {
    return this.identifier;
  }

  /**
   * Child Source.
   * Returns null if does not exists.
   *
   * @param path  relative name of source
   * @return  child Source
   */
  public Source getChild(String path) {
    return null;
  }

  /**
   * Name of the file/directory.
   *
   * @return  name of the file/directory
   */
  public String getName() {
    return sourceNode.getShortLabel();
  }

  /**
   * Path from the root of filesystem this file belongs to.
   * Path components are delimited by slash '/'. Root of
   * the file system is "". Example: "com/package/CompilationUnit.java"
   *
   * @return  absolute path (relative to filesystem root)
   */
  public String getAbsolutePath() {
    return compilationUnit.getAbsolutePath();
  }

  /**
   * Path from the root of filesystem this file belongs to.
   * Path components are delimited by slash '/'. Root of
   * the file system is "". Example: "com/package/CompilationUnit.java"
   *
   * @return  absolute path (relative to filesystem root)
   */
//  public String getRelativePath() {
//    // FIXME: wrong when SourcePath starts in the middle of the package!!!
//    String relativePath;
//    if (packageName.length() > 0) {
//      String packagePath = StringUtil.replace(packageName, '.', '/');
//      relativePath = packagePath + '/' + getName();
//    } else {
//      relativePath = getName();
//    }
//    return relativePath;
//  }

  /**
   * Tests if this file is writable.
   *
   * @return  true if this file is writable
   */
  public boolean canWrite() {
    boolean result = !sourceNode.isReadOnly();
    if (!result) {
      try {
        sourceNode.save(true);
      } catch (Exception e) {
        e.printStackTrace();
      }
      result = !sourceNode.isReadOnly();
    }

    if (!result) {
      sourceNode.releaseTextBuffer();
      result = !sourceNode.isReadOnly();
    }

    return result;
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
//    if (this.lastModified < 0) {
//      this.lastModified = sourceNode.getTimestamp();
//    }
//    return this.lastModified;
    return sourceNode.getTimestamp();
  }

  /**
   * Set time of last modification
   *
   * @param time  last modified time
   */
  public boolean setLastModified(long time) {
    invalidateCaches();
    // FIXME: it would be better if we could set the lastModified time to
    // sourceNode. Don't know how JDev reacts to this...
    return compilationUnit.setLastModified(time);
  }

  /**
   * Only for files!!!
   *
   * @return  length of the file
   */
  public long length() {
//    if (this.length < 0) {
    TextBuffer textBuffer = sourceNode.acquireTextBuffer();
    int bufferLength = textBuffer.getLength();
    sourceNode.releaseTextBuffer();
//      this.length = bufferLength;
//    }
//    return this.length;
    return bufferLength;
  }

  /**
   * Tests if this is the file
   *
   * @return  true if this Source object represents a file
   */
  public boolean isFile() {
    return true;
  }

  /**
   * Tests if this is the directory
   *
   * @return  true if this Source object represents a directory
   */
  public boolean isDirectory() {
    return false;
  }

  /**
   * Only for files!!! Dont't forget to close it!
   *
   * @return  InputStream to read contents of the file
   */
  public InputStream getInputStream() throws IOException {
    return sourceNode.getInputStream();
  }

  /**
   * Only for files!!! Dont't forget to close it!
   *
   * @return  InputStream to write contents of the file
   */
  public OutputStream getOutputStream() throws IOException {
    return new TextBufferOutputStream(sourceNode);
  }

  /**
   * Try to delete this file/directory.
   */
  public boolean delete() {
    invalidateCaches();

    if (compilationUnit.delete()) {
      Context context = RefactorItController.getJDevInstance().
          getLastEventContext();
      Project project = context.getProject();
      project.remove(sourceNode, true);
      UpdateMessage.fireChildRemoved(project, sourceNode);
      UpdateMessage.fireObjectClosed(sourceNode);
      project.markDirty(true);
      try {
        project.save(true);
      } catch (IOException e) {
        throw new ChainableRuntimeException("Project couldn't be saved: " + e);
      }
      return true;
    } else {
      return false;
    }
  }

  /**
   * List of childrens (subdirectories and files).
   * Only for directories!!!
   *
   * @return  Source childrens (directories and files)
   */
  public Source[] getChildren() {
    // FIXME: implement this
    new RuntimeException("Not implemented method invoked!").printStackTrace();
    return new JDevSource[0];
  }

  /**
   * List of children (subdirectories and files).
   * Only for directories!!!
   *
   * @return  Source childrens (directories and files)
   */
  public Source[] getChildren(Source.SourceFilter sourceFilter) {
    // FIXME: implement this
    new RuntimeException("Not implemented method invoked!").printStackTrace();
    return new JDevSource[0];
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
    // FIXME: implement this
    throw new RuntimeException("Not implemented method invoked!");
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
  public Source mkdirs(String path) {
    // FIXME: implement this
    throw new RuntimeException("Not implemented method invoked!");
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
    // FIXME: implement this
    throw new RuntimeException("Not implemented method invoked!");
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
    File destination = new File(dir.getAbsolutePath() + File.separatorChar
        + name);

    if (compilationUnit.renameTo(destination)) {

      try {
        String fileName = sourceNode.getURL().getFile();
        fileName = URLDecoder.decode(fileName);

        renameInJDevSystem(URLFactory.newFileURL(destination));

        if (Assert.enabled) {
          if (compilationUnit.getAbsolutePath().equals(destination.getAbsolutePath())) {
            new Exception("Files have the same name after rename: "
                + destination.getAbsolutePath()).printStackTrace(System.err);
          }
        }

        JDevSource newSource = JDevSource
            .getSource(sourceNode, destination.getAbsolutePath());

        newSource.setASTTree(this.getASTTree());

        return newSource;
      } catch (Exception e) {
        return null;
      } finally {
        invalidateCaches();
      }
    } else {
      System.err.println("Renaming source file failed!");
      return null;
    }
  }

  private void renameInJDevSystem(URL newURL) {
    URL oldURL = sourceNode.getURL();
    Context context = RefactorItController.getJDevInstance().getLastEventContext();
    Project project = context.getProject();
    if (isMove(oldURL, newURL)) {
      UpdateMessage.fireChildRemoved(project, sourceNode);
    }

    sourceNode.setURL(newURL);

    RenameMessage.fireObjectRenamed(sourceNode, oldURL, context);

    project.markDirty(true);
    try {
      project.save(true);
    } catch (IOException e) {
      System.err.println("Project couldn't be saved: " + e);
      e.printStackTrace();
    }
    if (isMove(oldURL, newURL)) {
      UpdateMessage.fireChildAdded(project, sourceNode);
    }
  }

  private boolean isMove(URL oldURL, URL newURL) {
    return (!new File(URLDecoder.decode(oldURL.getFile())).getParent().equals(new
        File(URLDecoder.decode(newURL.getFile())).getParent()));
  }

  public void invalidateCaches() {
    super.invalidateCaches();
//    this.lastModified = -1;
//    this.length = -1;
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

  public TextNode getNode() {
    return this.sourceNode;
  }

  /**
   * OutputStream used internally for letting RefactorIT write to the source
   * file. This class buffers the contents and writes them to the file when
   * the stream is closed.
   */
  class TextBufferOutputStream extends OutputStream {
    List bytesBuffer = new ArrayList();
    TextNode node;

    public TextBufferOutputStream(TextNode node) {
      this.node = node;
    }

    public void write(int b) throws IOException {
      byte by = (byte) (b & 0xFF);
      bytesBuffer.add(new Byte(by));
    }

    byte[] getByteArray(List byteList) {
      byte[] result = new byte[byteList.size()];
      for (int i = 0; i < byteList.size(); i++) {
        result[i] = ((Byte) byteList.get(i)).byteValue();
      }

      return result;
    }

    public void close() throws IOException {
      EditProcessor.doReplaceAll(new String(getByteArray(bytesBuffer)).toCharArray(), node, false, "Refactoring", null );
      node.markDirty(true);
      node.save(true);
    }
  }


  public void renameFormFileIfExists(String oldName, String newName) {}

  public void moveFormFileIfExists(Source destination) {}

  public File getFileOrNull() {
    throw new UnsupportedOperationException();
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
