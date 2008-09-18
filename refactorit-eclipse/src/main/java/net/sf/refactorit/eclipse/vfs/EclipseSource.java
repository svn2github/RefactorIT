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
 * @author Tõnis Vaga
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

  public boolean exists() {
    return resource.exists();
  }

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

  public String getName() {
    return resource.getName();
  }

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

  public boolean canWrite() {
    return !resource.isReadOnly();
  }

  public boolean startEdit() {
    return canWrite();
  }

  public long lastModified() {
    return resource.getLocalTimeStamp();
  }

  // FIXME sometimes doesn't work, like in project rebuild tests
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

  public long length() {
    if (!isFile()) {
      return 0;
    }

    // FIXME: find better way to do it
    return getFileOrNull().length();
  }

  public boolean isFile() {
    return resource instanceof IFile;
  }

  public boolean isDirectory() {
    return resource instanceof IFolder || resource instanceof IProject;
  }

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

  public OutputStream getOutputStream() {
    return new EclipseOuputStream();
  }

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

  private boolean isDirEmpty() {
    Source children[] = getChildren();

    for (int i = 0; i < children.length; i++) {
      if (children[i].exists()) {
        return false;
      }
    }

    return true;
  }

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

  public File getFileOrNull() {
    return resource.getLocation().toFile();
  }

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

  public void renameFormFileIfExists(String oldName, String newName) {
  }

  public void moveFormFileIfExists(Source destination) {
  }

  public boolean shouldSupportVcsInFilesystem() {
    return false;
  }

  public boolean inVcs() {
    return false;
  }

  public char getSeparatorChar() {
    return '/';
  }

  private static final Object getIdentifier(final IResource resource) {
    return resource.getFullPath().toString();
  }
}
