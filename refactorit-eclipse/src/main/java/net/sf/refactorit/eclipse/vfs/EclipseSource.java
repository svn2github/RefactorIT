/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.eclipse.vfs;

import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.FileCopier;
import net.sf.refactorit.vfs.AbstractSource;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourceMap;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;


/**
 * EclipseSource
 *
 * @author <a href="mailto:tonis.vaga@aqris.com>Tõnis Vaga </a>
 * @version $Revision: 1.53 $ $Date: 2005/12/09 12:02:59 $
 */
public class EclipseSource extends AbstractSource {
  private static final Logger log = AppRegistry.getLogger(EclipseSource.class);

  public static final boolean debug = false;

  /**
   * can be either IFile, IFolder or IProject
   * Note that top level dir is IProject not IFolder
   */
  IResource resource;

  private final Object identifier;

  private EclipseSource(IResource resource) {
    this.resource = resource;
    this.identifier = getIdentifier(resource);

    assertIsValid();

    if (debug) {
      log.debug("creating eclipse source " + getAbsolutePath());
    }
  }

  private void assertIsValid() {
    Assert.must(
        resource instanceof IFolder ||
        resource instanceof IFile ||
        resource instanceof IProject,
        " unsupported resource " + resource.getClass());

    Assert.must(resource.exists(), toString() + " doesn't exist");
  }

  /**
   * Gets an EclipseSource from SourceMap by specified IResource. If there is no
   * entry for specified IResource, then new EclipseSource is created, saved to
   * the SourceMap and returned.
   *
   * @param resource either IFolder or IFile
   * @return EclipseSource
   */
  public static final EclipseSource getSource(IResource resource) {
    Source result = SourceMap.getSource(getIdentifier(resource));
    if (result == null || !(result instanceof EclipseSource)) {
      if (resource != null) {
        result = new EclipseSource(resource);
        SourceMap.addSource(result);
      }
    }

    return (EclipseSource) result;
  }

  public IResource getResource() {
    return resource;
  }

  /*
   * @see net.sf.refactorit.vfs.Source#exists()
   */
  public boolean exists() {
    return resource.exists();
  }

  /*
   * @see net.sf.refactorit.vfs.Source#getParent()
   */
  public Source getParent() {
    IContainer parent = resource.getParent();
    if (!(parent instanceof IFolder || parent instanceof IProject) || !parent.exists()) {
      return null;
    }
    return getSource(parent);
  }

  public Object getIdentifier() {
    return identifier;
  }

  /*
   * @see net.sf.refactorit.vfs.Source#getChild(java.lang.String)
   */
  public Source getChild(String path) {
    if (!isDirectory()) {
      return null;
    }

    if (path.equals("")) {
      AppRegistry.getLogger(this.getClass()).error(
          "Try to get children by empty name \"\" from:" + this.getAbsolutePath());
    }

    IResource childResource = getFile(resource, path);
    if (childResource == null || !childResource.exists()) {
      childResource = getFolder(resource, path);
    }

    if (childResource == null || !childResource.exists()) {
      return null;
    }

    return EclipseSource.getSource(childResource);
  }

  /**
   * @param resource
   * @param path
   * @return file in current directory
   */
  private static IFile getFile(IResource resource, String path) {
    if (resource instanceof IFolder) {
      return ((IFolder) resource).getFile(path);
    }

    if (resource instanceof IProject) {
      return ((IProject) resource).getFile(path);
    }

    return null;
  }

  /*
   * @see net.sf.refactorit.vfs.Source#getName()
   */
  public String getName() {
    return resource.getName();
  }

  /*
   * @see net.sf.refactorit.vfs.Source#getAbsolutePath()
   */
  public String getAbsolutePath() {
    IPath absolutePath = resource.getLocation();
    if (absolutePath == null) {
      // fallback if can't detect absolute path
      absolutePath = resource.getRawLocation();
      log.warn("failed to get location for " + resource);
    }

    if (absolutePath == null) {
      log.warn("absolute path == null for " + resource);
      return getName();
    }

    return absolutePath.toString();
  }

  /*
   * @see net.sf.refactorit.vfs.Source#canWrite()
   */
  public boolean canWrite() {
    return !resource.isReadOnly();
  }

  /*
   * @see net.sf.refactorit.vfs.Source#startEdit()
   */
  public boolean startEdit() {
    return canWrite();
  }

  /*
   * @see net.sf.refactorit.vfs.Source#lastModified()
   */
  public long lastModified() {
    return resource.getLocalTimeStamp();
  }

  /*
   * @see net.sf.refactorit.vfs.Source#setLastModified(long)
   *
   * FIXME sometimes doesn't work, like in project rebuild tests
   */
  public boolean setLastModified(long time) {
    try {
      if (resource.setLocalTimeStamp(time) != time) {
        log.warn("setLastModified failed to set " + time
                  + ", result was " + lastModified());
      }
      return true;
    } catch (CoreException e) {
      AppRegistry.getExceptionLogger().error(e, this);
      return false;
    }
  }

  /*
   * @see net.sf.refactorit.vfs.Source#length()
   */
  public long length() {
    if (!isFile()) {
      return 0;
    }
    // FIXME: find better way to do it
    return getFileOrNull().length();
  }

  /*
   * @see net.sf.refactorit.vfs.Source#isFile()
   */
  public boolean isFile() {
    return resource instanceof IFile;
  }

  /*
   * @see net.sf.refactorit.vfs.Source#isDirectory()
   */
  public boolean isDirectory() {
    return resource instanceof IFolder || resource instanceof IProject;
  }

  /*
   * @see net.sf.refactorit.vfs.Source#getInputStream()
   */
  public InputStream getInputStream() throws IOException {
    if (!isFile()) {
      return null;
    }
    try {
      return ((IFile) resource).getContents(true);
    } catch (CoreException e) {
      AppRegistry.getExceptionLogger().error(e, this);
      throw new IOException(e.getMessage());
    }
  }

  private class EclipseOuputStream extends ByteArrayOutputStream {
    public EclipseOuputStream() {
      super(Source.DEFAULT_BUFFER_SIZE);
    }

    /*
     * @see java.io.OutputStream#close()
     */
    public void close() throws IOException {
      IFile file = (IFile) resource;

      try {
// FIXME: Cursor position is lost and moved to beginning of file
//   when complete replace is performed. If it is applied as diff
//   to the buffer then all is working well...
//
//        ICompilationUnit cu = JavaCore.createCompilationUnitFrom(file);
//        if (cu != null) {
//          try {
//            cu.becomeWorkingCopy(null, null);
//            ICompilationUnit copy = cu;
//            IBuffer buffer = copy.getBuffer();
//            String contents = toString(file.getCharset());
//// DIRTY HACK - unusable because of performance
////            int length = Math.min(buffer.getLength(), contents.length());
////
////            char[] chars = new char[1];
////            for (int i = 0; i < length; i++) {
////              chars[0] = contents.charAt(i);
////              buffer.replace(i, 1, chars);
////            }
////
////            int diff = contents.length() - length;
////            if (diff > 0) {
////              chars = new char[diff];
////              contents.getChars(length, contents.length(), chars, 0);
////              buffer.replace(length, 0, chars);
////            } else {
////              diff = buffer.getLength() - length;
////              if (diff > 0) {
////                buffer.replace(length, diff, "");
////              }
////            }
//            buffer.setContents(contents);
//            copy.commitWorkingCopy(true, null);
//            copy.discardWorkingCopy();
//            return;
//          } catch (UnsupportedEncodingException e) {
//            // Fallback to resource APIs
//          }
//        }

        InputStream in = new ByteArrayInputStream(toByteArray());
        file.setContents(in, true, true, null);
      } catch (CoreException e) {
        AppRegistry.getExceptionLogger().error(e, this);
        throw new IOException(e.getMessage());
      }
    }
  }

  /*
   * @see net.sf.refactorit.vfs.Source#getOutputStream()
   */
  public OutputStream getOutputStream() {
    return new EclipseOuputStream();
  }

  /*
   * @see net.sf.refactorit.vfs.Source#delete()
   */
  public boolean delete() {
    if (isDirectory()) {
      if (!isDirEmpty()) {
        log.warn("tried to delete not empty dir " + this);
        return false;
      }
    }

    try {

      resource.delete(true, null);

      if (log.isDebugEnabled()) {
        log.debug(" deleting " + this);
      }

      SourceMap.removeSource(this);

      return true;
    } catch (CoreException e) {
      AppRegistry.getExceptionLogger().error(e, toString(), this);
      return false;
    }
  }

  /**
   * @returns
   */
  private boolean isDirEmpty() {
    Source children[] = getChildren();

    for (int i = 0; i < children.length; i++) {
      if (children[i].exists()) {
        return false;
      }
    }

    return true;
  }

  /*
   * @see net.sf.refactorit.vfs.Source#getChildren()
   */
  public Source[] getChildren() {
    if (!isDirectory()) {
      return Source.NO_SOURCES;
    }

    IResource children[];
    try {
      children = getFolder().members();
    } catch (CoreException e) {
      AppRegistry.getExceptionLogger().error(e, this);
      return NO_SOURCES;
    }

    Source result[] = new Source[children.length];
    for (int i = 0; i < children.length; i++) {
      Source source = getSource(children[i]);
      result[i] = source;
    }
    Arrays.sort(result);
    return result;
  }

  private IContainer getFolder() {
    return (IContainer) resource;
  }

  /*
   * @see net.sf.refactorit.vfs.Source#mkdir(java.lang.String)
   */
  public Source mkdir(String name) {
    if (!isDirectory()) {
      return null;
    }

    IFolder folder = getFolder(resource, name);

    try {
      if (folder.exists()) {
        log.warn("Can't mkdir, " + folder.getLocation() + " already exists");
        return getSource(folder);
      }

      // TODO: overlook arguments
      folder.create(true, true, null);

      return getSource(folder);
    } catch (CoreException e) {
      AppRegistry.getExceptionLogger().error(e, this);
      return null;
    }
  }

  /**
   *
   * @param resource
   * @param name
   * @return subfolder in current directory
   */
  private static IFolder getFolder(IResource resource, String name) {
    if (resource instanceof IFolder) {
      return ((IFolder) resource).getFolder(name);
    }

    if (resource instanceof IProject) {
      return ((IProject) resource).getFolder(name);
    }

    return null;
  }

  /*
   * @see net.sf.refactorit.vfs.Source#createNewFile(java.lang.String)
   */
  public Source createNewFile(String name) {
    if (!isDirectory()) {
      return null;
    }

    IFile file = getFile(resource, name);

    if (file.exists()) {
      log.error(file + " already exists, will not create");
      return null;
    }


    try {
      file.create(new ByteArrayInputStream(new byte[0]), true, null);

      Assert.must(file.exists());

      if (log.isDebugEnabled()) {
        log.debug("created new file " + getAbsolutePath());
      }
    } catch (CoreException e) {
      AppRegistry.getExceptionLogger().error(e, this);
      return null;
    }

    return getSource(file);
  }

  /*s
   * @see net.sf.refactorit.vfs.Source#renameTo(net.sf.refactorit.vfs.Source,java.lang.String)
   */
  public Source renameTo(Source dir, String name) {
    if (!dir.isDirectory()) {
      log.warn("renameTo called on file " + dir.getAbsolutePath());
      return null;
    }

    IResource targetResource = null;

    Assert.must(dir instanceof EclipseSource);

    Object oldIdentifier = getIdentifier(resource);
    try {
      IResource targetFolder = ((EclipseSource) dir).resource;

      if (isDirectory()) {
        targetResource = getFolder(targetFolder, name);
      } else {
        targetResource = getFile(targetFolder, name);
      }

      if (targetResource.exists()) {
        log.error("Can't rename, target resource "
              + targetResource.getLocation() + " exists");
        return null;
      }

      resource.move(targetResource.getFullPath(), true, null);

      // create new source
      SourceMap.removeSourceWithIdentifier(oldIdentifier);

      EclipseSource newSource = getSource(targetResource);
      newSource.setASTTree(getASTTree());

      return newSource;
    } catch (Exception e) {
      AppRegistry.getExceptionLogger().error(e,
          "From " + toString() + " to " + targetResource, this);
      return null;
    }
  }

  /*
   * @see net.sf.refactorit.vfs.Source#getFileOrNull()
   */
  public File getFileOrNull() {
    return resource.getLocation().toFile();
  }

  /*
   * @see net.sf.refactorit.vfs.Source#getContent()
   */
  public byte[] getContent() throws IOException {
    if (!isFile()) {
      return null;
    }

    ByteArrayOutputStream output = new ByteArrayOutputStream();

    InputStream input = getInputStream();
    try {
      FileCopier.pump(input, output, Source.DEFAULT_BUFFER_SIZE, true);
    } finally {
      input.close();
    }

    return output.toByteArray();
  }

  /*
   * @see net.sf.refactorit.vfs.Source#renameFormFileIfExists(java.lang.String,java.lang.String)
   */
  public void renameFormFileIfExists(String oldName, String newName) {
  }

  /*
   * @see net.sf.refactorit.vfs.Source#moveFormFileIfExists(net.sf.refactorit.vfs.Source)
   */
  public void moveFormFileIfExists(Source destination) {
  }

  /*
   * @see net.sf.refactorit.vfs.Source#shouldSupportVcsInFilesystem()
   */
  public boolean shouldSupportVcsInFilesystem() {
    return false;
  }

  /*
   * @see net.sf.refactorit.vfs.Source#inVcs()
   */
  public boolean inVcs() {
    return false;
  }

  /*
   * @see net.sf.refactorit.vfs.Source#getSeparatorChar()
   */
  public char getSeparatorChar() {
    return '/';
  }

  private static final Object getIdentifier(final IResource resource) {
    String id = resource.getFullPath().toString();

    return id;
  }
}
