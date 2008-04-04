/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.netbeans.vfs;


import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.FileCopier;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.LoadingProperties;
import net.sf.refactorit.netbeans.common.RefactorItActions;
import net.sf.refactorit.netbeans.common.projectoptions.FileObjectUtil;
import net.sf.refactorit.netbeans.common.vfs.NBSource;
import net.sf.refactorit.test.LocalTempFileCreator;
import net.sf.refactorit.utils.FileUtil;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourcePath;

import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.JarFileSystem;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;


import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Seems to be for NB 3.6 only at the moment (4.0 not supported in this class).
 *
 * @author  RISTO A
 */
public class TestFileCreator {
  // FIXME: TempFileCreator has a somewhat similar purpose -- could we merge them or remove duplication?

  public static LocalFileSystem mountNewTempFilesystem() throws IOException,
      PropertyVetoException {
    File root = new LocalTempFileCreator().createRootDirectory().getFileOrNull();
    return mountFilesystem(root);
  }

  private static LocalFileSystem mountFilesystem(final File root) throws
      IOException, PropertyVetoException {
    LocalFileSystem result = new LocalFileSystem();
    result.setRootDirectory(root);

    Repository.getDefault().addFileSystem(result);
    return result;
  }

  public static JarFileSystem mountJarFile(final File jarFile) throws
      IOException,
      PropertyVetoException {
    JarFileSystem jarFs = new JarFileSystem();
    jarFs.setJarFile(jarFile);
    Repository.getDefault().addFileSystem(jarFs);
    return jarFs;
  }

  public static Source createFile(String name, String content) {
    Project p = IDEController.getInstance().getActiveProject();
    FileObject root = ((NBSource) p.getPaths().getSourcePath().getRootSources()[0]).getFileObject();

    return NBSource.getSource(writeTo(root, name, content));
  }

  public static Source createFile(String folderName, String name, String content) throws IOException {
    Project p = IDEController.getInstance().getActiveProject();
    FileObject root = ((NBSource) p.getPaths().getSourcePath().getRootSources()[0]).getFileObject();
    FileObject folder = root.createFolder(folderName);

    return NBSource.getSource(writeTo(folder, name, content));
  }

  public static Source createFile(FileObject folder, final String name, final String content) {
    try {
      return NBSource.getSource(writeTo(folder, name, content));
    } finally {
      IDEController.getInstance().ensureProject(new LoadingProperties(false));
    }
  }

  public static FileObject writeTo(FileObject folder, final String fileName, final String contents) {
    File newFile = new File(FileObjectUtil.getFileOrNull(folder), fileName);
    FileCopier.writeStringToFile(newFile, contents);
    folder.refresh();

    return folder.getFileObject(fileName);
  }

  public static NBSource createSourceInRoot(final LocalFileSystem fs,
      final String filename) {
    FileObject x = writeTo(fs.getRoot(), filename, "class X{}");
    return NBSource.getSource(x);
  }

  public static void writeStringToFileObject(final String contents,
      final FileObject fileObject) throws IOException {

    FileLock l = fileObject.lock();
    try {
      OutputStream out = fileObject.getOutputStream(l);
      out.write(contents.getBytes());
      out.close();
    } finally {
      l.releaseLock();
    }
  }

  public static FileObject getRoot() throws IOException, PropertyVetoException {
    if(RefactorItActions.isNetBeansFour()) {
      Project p = IDEController.getInstance().getActiveProject();
      return ((NBSource) p.getPaths().getSourcePath().getRootSources()[0]).getFileObject();
    } else {
      return TestFileCreator.mountNewTempFilesystem().getRoot();
    }
  }
}
