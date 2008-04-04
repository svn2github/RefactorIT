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
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.netbeans.common.FileNotFoundReason;
import net.sf.refactorit.netbeans.common.RefactorItActions;
import net.sf.refactorit.netbeans.common.projectoptions.NBProjectOptions;
import net.sf.refactorit.netbeans.common.projectoptions.PathItemReference;
import net.sf.refactorit.netbeans.common.vfs.NBSource;
import net.sf.refactorit.vfs.SourcePath;

import org.openide.filesystems.Repository;

import java.beans.PropertyVetoException;
import java.io.IOException;

/**
 * @author risto
 */
public class FileNotFoundReasonGeneralTest extends NbTestCase {
  private NBSource root;
  
  public void mySetUp() throws IOException, PropertyVetoException {
    root = getRoot();
  }
  
  public void testEmptyPackage() {
    NBSource emptyFolder = (NBSource) root.mkdir("emtpyFolder");
    
    assertEquals(FileNotFoundReason.EMPTY_PACKAGE, FileNotFoundReason.getFor(emptyFolder.getFileObject()));
  }
  
  public void testEmptyPackage_withEmptySubPackages() {
    NBSource emptyFolder = (NBSource) root.mkdir("emtpyFolder");
    emptyFolder.mkdir("emptySubfolder");
    
    assertEquals(FileNotFoundReason.EMPTY_PACKAGE, FileNotFoundReason.getFor(emptyFolder.getFileObject()));
  }
  
  public void testIgnoredOnSourcepathList_filesystemRoot() throws IOException {
    NBSource source = (NBSource) root.createNewFile("X.java");
    source.setContent("public class X{}".getBytes());
    Object ideProject = IDEController.getInstance().getActiveProjectFromIDE();
    Object projectKey = IDEController.getInstance().getWorkspaceManager()
        .getIdeProjectIdentifier(ideProject);
    NBProjectOptions.getInstance(projectKey).setUserSpecifiedIgnoredSourcePathDirectories(
        new PathItemReference[] {new PathItemReference(root.getFileObject())});
    
    assertEquals(FileNotFoundReason.ELEMENT_IGNORED, 
        FileNotFoundReason.getFor(source.getFileObject()));
    assertEquals(FileNotFoundReason.ELEMENT_IGNORED, 
        FileNotFoundReason.getFor(root.getFileObject()));
  }
  
  public void testIgnoredOnSourcepathList_parentFolder() throws IOException, InterruptedException {
    NBSource folder = (NBSource) root.mkdir("com");
    NBSource source = (NBSource) folder.createNewFile("X.java");
    source.setContent("public class X{}".getBytes());
    
    Object ideProject = IDEController.getInstance().getActiveProjectFromIDE();
    Object projectKey = IDEController.getInstance().getWorkspaceManager()
        .getIdeProjectIdentifier(ideProject);
    NBProjectOptions.getInstance(projectKey).setUserSpecifiedIgnoredSourcePathDirectories(
        new PathItemReference[] {new PathItemReference(folder.getFileObject())});
    
    assertEquals(FileNotFoundReason.DEFAULT_REASON_PACKAGE_OR_EMPTY_PACKAGE,
        FileNotFoundReason.getFor(root.getFileObject()));
    assertEquals(FileNotFoundReason.ELEMENT_IGNORED, 
        FileNotFoundReason.getFor(folder.getFileObject()));
    assertEquals(FileNotFoundReason.ELEMENT_IGNORED, 
        FileNotFoundReason.getFor(source.getFileObject()));
  }
  
  public void testNotOnManualPath() throws IOException {
    NBSource source = (NBSource) root.createNewFile("X.java");
    source.setContent("public class X{}".getBytes());
    
    Object ideProject = IDEController.getInstance().getActiveProjectFromIDE();
    Object projectKey = IDEController.getInstance().getWorkspaceManager()
        .getIdeProjectIdentifier(ideProject);
    NBProjectOptions.getInstance(projectKey).setAutodetectPaths(false);
    NBProjectOptions.getInstance(projectKey).setUserSpecifiedSourcePath(
        new PathItemReference[0]);
    
    assertEquals(FileNotFoundReason.ELEMENT_NOT_ON_MANUAL_PATH, 
        FileNotFoundReason.getFor(source.getFileObject()));
  }
  
  public void testUnknownReason() throws IOException {
    NBSource source = (NBSource) root.createNewFile("UnknownReason.java");
    source.setContent("public class UnknownReason{}".getBytes());
    
    assertEquals(FileNotFoundReason.DEFAULT_REASON_PACKAGE_OR_EMPTY_PACKAGE,
        FileNotFoundReason.getFor(root.getFileObject()));
    assertEquals(FileNotFoundReason.DEFAULT_REASON_FILE,
        FileNotFoundReason.getFor(source.getFileObject()));
  }
  
  public void testUnknownReason_localSource() throws IOException {
    NBSource source = (NBSource) root.createNewFile("UnknownReasonLocal.java");
    source.setContent("public class UnknownReasonLocal{}".getBytes());
    
    Object ideProject = IDEController.getInstance().getActiveProjectFromIDE();
    Object projectKey = IDEController.getInstance().getWorkspaceManager()
        .getIdeProjectIdentifier(ideProject);
    NBProjectOptions.getInstance(projectKey).setAutodetectPaths(false);
    NBProjectOptions.getInstance(projectKey).setUserSpecifiedSourcePath(
        new PathItemReference[] {new PathItemReference(root.getFileOrNull())});
    
    assertEquals(FileNotFoundReason.DEFAULT_REASON_FILE,
        FileNotFoundReason.getFor(source.getFileObject()));
  }
  
  public void testNonJavaFile() {
    NBSource source = (NBSource) root.createNewFile("somefile");
    
    assertEquals(FileNotFoundReason.NOT_JAVA_FILE,
        FileNotFoundReason.getFor(source.getFileObject()));
  }
  
  public void testEmptyJavaFile() {
    NBSource source = (NBSource) root.createNewFile("X.java");
    
    assertEquals(FileNotFoundReason.EMPTY_JAVA_FILE,
        FileNotFoundReason.getFor(source.getFileObject()));
  }
  
  // TODO: To be finished
  /*public void testUnparseableJavaFile() throws IOException {
    NBSource source = (NBSource) root.createNewFile("Unparseable.java");
    source.setContent("Some unparseable gibberish".getBytes());
    
    IDEController.getInstance().ensureProject();
    
    assertEquals(FileNotFoundReason.PARSE_ERROR,
        FileNotFoundReason.getFor(source.getFileObject()));
  }*/
  /* else if(onSourcepath(fileObject) && (!parsed(fileObject))) {
  return PARSE_ERROR;
}
  private static boolean parsed(FileObject fileObject) {
    NBSource source = NBSource.getSource(fileObject);
    File file = FileObjectUtil.getFileOrNull(fileObject);
    
    List units = IDEController.getInstance().getActiveProject().getCompilationUnits();
    for (Iterator i = units.iterator(); i.hasNext();) {
      CompilationUnit unit = (CompilationUnit) i.next();
      if(unit.getSource().equals(source) || unit.getSource().getFileOrNull().equals(file)) {
        return true;
      }
    }

    return false;
  }

  private static boolean onSourcepath(FileObject fileObject) {
    try {
      return onPath(fileObject, PathUtil.getInstance().getSourcepath());
    } catch (FileStateInvalidException e) {
      AppRegistry.getExceptionLogger().error(e, FileNotFoundReason.class);
      throw new SystemException(ErrorCodes.INTERNAL_ERROR,e);
    }
  }*/

  
  public void testSystemFileSystem() {
    assertEquals(FileNotFoundReason.SYSTEM_FILESYSTEM,
        FileNotFoundReason.getFor(Repository.getDefault().getDefaultFileSystem().getRoot()));
  }
}
