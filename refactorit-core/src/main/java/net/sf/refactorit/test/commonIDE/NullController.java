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
import net.sf.refactorit.commonIDE.ActionRepository;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.MenuBuilder;
import net.sf.refactorit.commonIDE.WorkspaceManager;
import net.sf.refactorit.test.TestProject;
import net.sf.refactorit.test.refactorings.NullContext;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.vfs.Source;

import java.awt.Frame;


/**
 * @author Anton Safonov
 */
public class NullController extends IDEController {
  public NullController() {
    super();
    setInstance(this);
  }

  public ActionRepository getActionRepository() {
    return null;
  }

  public Object getCachePathForActiveProject(Object ideProject) {
    return null;
  }


  public Frame getIDEMainWindow() {
    return null;
  }

//  public boolean isProjectChangedInIDE() {
//    return false;
//  }

  public Object getActiveProjectFromIDE() {

    return ((NullWorkspace)getWorkspace()).getLastIdeProject();
  }

  public void addIdeProject(TestProject project) {
    ((NullWorkspace)IDEController.getInstance().getWorkspace()).addIdeProject(project);
  }

  public int getPlatform() {
    return TEST;
  }

  public RefactorItContext createProjectContext() {
    return new NullContext(getActiveProject());
  }

  public MenuBuilder createMenuBuilder(String name, char mnemonic, String icon,
      boolean submenu) {
    return null;
  }

  /* (non-Javadoc)
   * @see net.sf.refactorit.commonIDE.IDEController#getIdeInfo()
   */
  public void getIdeInfo() {
    setIdeName("test");
  }

  /**
   * @see net.sf.refactorit.commonIDE.IDEController#saveAllFiles()
   */
  public boolean saveAllFiles() {
    return false;
  }

  /**
   * @see net.sf.refactorit.commonIDE.IDEController#createNewProjectFromIdeProject(java.lang.Object)
   */
  protected Project createNewProjectFromIdeProject(Object prj) {
    //return Utils.createNewProjectFrom((TestProject)ideProject);
    TestProject ideProject = (TestProject)prj;
    return new Project(ideProject.getUniqueID(),ideProject.getSourcePath(),ideProject.getClassPath(),ideProject.getJavadocPath());
  }

  /**
   * @see net.sf.refactorit.commonIDE.IDEController#addIgnoredSources(Project, net.sf.refactorit.vfs.Source[])
   */
  public void addIgnoredSources(Project pr, Source[] sourcePaths) {
    throw new UnsupportedOperationException("method not implemented yet");
    //
  }

  /** Overriden to not access UI */
  protected boolean processParsingResult(Project project, ParsingResult result) {
    return !project.getProjectLoader().isParsingCanceledLastTime();
  }

  public WorkspaceManager getWorkspaceManager() {
    return NullWorkspaceManager.getInstance();
  }


}
