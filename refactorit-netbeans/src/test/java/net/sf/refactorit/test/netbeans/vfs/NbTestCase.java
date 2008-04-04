/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.netbeans.vfs;

import java.io.File;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.FileCopier;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.netbeans.common.NBContext;
import net.sf.refactorit.netbeans.common.RefactorItActions;
import net.sf.refactorit.netbeans.common.projectoptions.NBProjectOptions;
import net.sf.refactorit.netbeans.common.projectoptions.PathItemReference;
import net.sf.refactorit.netbeans.common.projectoptions.PathUtil;
import net.sf.refactorit.netbeans.common.vfs.NBSource;
import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.test.LocalTempFileCreator;
import net.sf.refactorit.transformations.SimpleSourceHolder;
import net.sf.refactorit.utils.FileUtil;
import net.sf.refactorit.vfs.Source;

import org.openide.windows.TopComponent;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.OutputStreamWriter;

import junit.framework.TestCase;

/**
 * @author risto
 */
public abstract class NbTestCase extends TestCase {
  
  private AutomaticTestfileDeleter testileDeleter; 
  
  private Object oldOptions;
  
  public final void setUp() throws Exception {
    testileDeleter = new AutomaticTestfileDeleter();
    testileDeleter.startListening();
    Object ideProject = IDEController.getInstance().getActiveProjectFromIDE();
    Object projectKey = IDEController.getInstance().getWorkspaceManager()
        .getIdeProjectIdentifier(ideProject);
    oldOptions = NBProjectOptions.getInstance(projectKey).getRestorableDump();
    NBProjectOptions.getInstance(projectKey).setAutodetectPaths(true);
    
    mySetUp();
  }

  public final void tearDown() throws Exception {
    Object ideProject = IDEController.getInstance().getActiveProjectFromIDE();
    Object projectKey = IDEController.getInstance().getWorkspaceManager()
        .getIdeProjectIdentifier(ideProject);
    NBProjectOptions.getInstance(projectKey).setAllFromRestorableDump(oldOptions);
    
    testileDeleter.stopListening();
    testileDeleter.deleteCreatedFiles();
    
    myTearDown();
  }

  protected void mySetUp() throws Exception {
  }

  protected void myTearDown() throws Exception  {
  }
  
  // Util methods

  public static NBSource getRoot() throws IOException, PropertyVetoException {
    if(RefactorItActions.isNetBeansFour()) {
      Project p = IDEController.getInstance().getActiveProject();
      return (NBSource) p.getPaths().getSourcePath().getRootSources()[0];
    } else {
      return NBSource.getSource(TestFileCreator.mountNewTempFilesystem().getRoot());
    }
  }
  
  public static Source createNewFile(final String fileName, final String fileContents, 
      final String relativePath, final Source root) throws IOException {

    Source folder = root.mkdirs(relativePath);
    Source result = folder.createNewFile(fileName);
    FileCopier.writeStringToWriter(fileContents,
        new OutputStreamWriter(result.getOutputStream()));

    return result;
  }
  
  public static NBSource createSampleSource() throws IOException, PropertyVetoException {
    return (NBSource) createNewFile(
        "X.java", "package p; public class X {}", "p", getRoot());
  }
  
  public static void setIgnoredOnSourcepath(final NBSource folder) {
    Object ideProject = IDEController.getInstance().getActiveProjectFromIDE();
    Object projectKey = IDEController.getInstance().getWorkspaceManager()
        .getIdeProjectIdentifier(ideProject);
    NBProjectOptions.getInstance(projectKey).setUserSpecifiedIgnoredSourcePathDirectories(
        new PathItemReference[] {new PathItemReference(folder.getFileObject())});
  }
  
  public static void setIgnoredOnSourcepath(final File folder) {
    Object ideProject = IDEController.getInstance().getActiveProjectFromIDE();
    Object projectKey = IDEController.getInstance().getWorkspaceManager()
        .getIdeProjectIdentifier(ideProject);
    NBProjectOptions.getInstance(projectKey).setUserSpecifiedIgnoredSourcePathDirectories(
        new PathItemReference[] {new PathItemReference(folder)});
  }

  public static void showInEditor(final Source source, final int line, final int column) {
    NBContext context = (NBContext) IDEController.getInstance().createProjectContext();
    SourceHolder sourceHolder = new SimpleSourceHolder(
        source, IDEController.getInstance().getActiveProject());
    context.show(sourceHolder, line, column, false);
  }
  
  public static void setSourcepathRoot(NBSource root) {
    disablePathsAutodetection();
    Object ideProject = IDEController.getInstance().getActiveProjectFromIDE();
    Object projectKey = IDEController.getInstance().getWorkspaceManager()
        .getIdeProjectIdentifier(ideProject);
    NBProjectOptions.getInstance(projectKey).setUserSpecifiedSourcePath(new PathItemReference[] {
        new PathItemReference(root.getFileObject())});
  }
  
  public static void setSourcepathRoot(File root) {
    disablePathsAutodetection();
    Object ideProject = IDEController.getInstance().getActiveProjectFromIDE();
    Object projectKey = IDEController.getInstance().getWorkspaceManager()
        .getIdeProjectIdentifier(ideProject);
    NBProjectOptions.getInstance(projectKey).setUserSpecifiedSourcePath(new PathItemReference[] {
        new PathItemReference(root)});
  }

  protected static void disablePathsAutodetection() {
    Object ideProject = IDEController.getInstance().getActiveProjectFromIDE();
    Object projectKey = IDEController.getInstance().getWorkspaceManager()
        .getIdeProjectIdentifier(ideProject);
    if( NBProjectOptions.getInstance(projectKey).getAutodetectPaths()) {
      NBProjectOptions.getInstance(projectKey).setAutodetectPaths(false);
      
      // Let's provide some sample (sensible) values for when the caller forgets
      // to set some of these later on.
      ideProject = IDEController.getInstance().getActiveProjectFromIDE();
      projectKey = IDEController.getInstance().getWorkspaceManager()
          .getIdeProjectIdentifier(ideProject);
      NBProjectOptions.getInstance(projectKey).setUserSpecifiedSourcePath(
          PathUtil.getInstance().getAutodetectedSourcepath(ideProject, true));
      NBProjectOptions.getInstance(projectKey).setUserSpecifiedClassPath(
          PathUtil.getInstance().getAutodetectedClasspath(ideProject));
    }
  }
}
