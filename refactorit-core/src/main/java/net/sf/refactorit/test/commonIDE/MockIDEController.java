/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.commonIDE;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.cli.Arguments;
import net.sf.refactorit.cli.ProjectBuilder;
import net.sf.refactorit.commonIDE.ActionRepository;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.MenuBuilder;
import net.sf.refactorit.commonIDE.WorkspaceManager;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.local.LocalClassPath;
import net.sf.refactorit.vfs.local.LocalSourcePath;

import java.awt.Frame;


/**
 * @author Tonis Vaga
 */
public class MockIDEController extends IDEController {
  public ActionRepository getActionRepository() {
    return null;
  }

  public Object getCachePathForActiveProject(Object ideProject) {
    return generateNewCacheFileName("");
  }

  public Frame getIDEMainWindow() {
    throw new UnsupportedOperationException("should not get called");
  }

  public boolean isProjectChangedInIDE() {
    return false;
  }

  public Object getActiveProjectFromIDE() {
    return null;
  }

  public RefactorItContext createProjectContext() {
    return null;
  }

  /* (non-Javadoc)
   * @see net.sf.refactorit.commonIDE.IDEController#createMenuBuilder(java.lang.String, char, java.lang.String, boolean)
   */
  public MenuBuilder createMenuBuilder(String name, char mnemonic, String icon,
      boolean submenu) {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see net.sf.refactorit.commonIDE.IDEController#getIdeInfo()
   */
  public void getIdeInfo() {
    setIdeName("mock");
  }

  /**
   * @see net.sf.refactorit.commonIDE.IDEController#saveAllFiles()
   */
  public boolean saveAllFiles() {
    return true;
  }

  /**
   * @see net.sf.refactorit.commonIDE.IDEController#createNewProjectFromIdeProject(java.lang.Object)
   */
  protected Project createNewProjectFromIdeProject(Object ideProject) {
    Arguments arg = (Arguments) ideProject;
    return new Project(arg.getUniqueID(), new LocalSourcePath(arg.getSourcepath()),
        new LocalClassPath(ProjectBuilder.getClasspathWithDefaultEntries(
        arg.getClasspath())), null);
  }

  /**
   * @see net.sf.refactorit.commonIDE.IDEController#addIgnoredSources(Project, net.sf.refactorit.vfs.Source[])
   */
  public void addIgnoredSources(Project pr, Source[] sourcePaths) {
    throw new UnsupportedOperationException("method not implemented yet");
  }

  public WorkspaceManager getWorkspaceManager() {
    return MockWorkspaceManager.getInstance();
  }
}
