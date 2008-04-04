/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jbuilder;

import com.borland.jbuilder.info.JBuilderInfo;
import com.borland.jbuilder.node.JBProject;
import com.borland.primetime.ide.Browser;
import com.borland.primetime.properties.PropertyManager;
import com.borland.primetime.util.VetoException;

import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.commonIDE.ActionRepository;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.LoadingProperties;
import net.sf.refactorit.commonIDE.MenuBuilder;
import net.sf.refactorit.commonIDE.WorkspaceManager;
import net.sf.refactorit.jbuilder.vfs.JBClassPath;
import net.sf.refactorit.jbuilder.vfs.JBJavadocPath;
import net.sf.refactorit.jbuilder.vfs.JBSource;
import net.sf.refactorit.jbuilder.vfs.JBSourcePath;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.vfs.ClassPath;
import net.sf.refactorit.vfs.Source;



public class JBController extends IDEController {
  private ActionRepository repository;

  /*
   * @see net.sf.refactorit.commonIDE.IDEController#getActionRepository()
   */
  public ActionRepository getActionRepository() {
    if (repository == null) {
      repository = new JBActionRepository();
    }
    return repository;
  }

  public JBController() {
    super();
    IDEController.setInstance(this);
  }


  public int getPlatform() {
    return JBUILDER;
  }

  public boolean isProjectChangedInIDE() {
    return getIDEProject() != (JBProject) getActiveProjectFromIDE();
  }

  public Object getActiveProjectFromIDE() {
    try {
      return Browser.getActiveBrowser().getActiveProject();
    } catch (NullPointerException e) {
      return null;
    }
  }

  public static Object[] getProjectsFromIDE() {
    return Browser.getActiveBrowser().getProjectView().getOpenProjects();
  }

  public void beforeEnsureProject() {
    JavaFileNodeRe.informAboutProductivityModeIfNeeded();
  }

  protected Project createNewProjectFromIdeProject(Object ideProject) {
    if ( ! ( ideProject instanceof JBProject) ) {
      return null;
    }
    JBProject jbProject=(JBProject) ideProject;
    Project newProject;
    ClassPath classpath = new JBClassPath(jbProject);

    JBSourcePath jbSourcePath = new JBSourcePath(jbProject);

    newProject = new Project(jbProject.getLongDisplayName(),
        jbSourcePath, classpath, new JBJavadocPath(jbProject));

//    jbSourcePath.setProject(newProject);

    //newProject.setWriter(new PrintWriter(new RefactorItWriter()));

    newProject.setCachePath(getCachePathForActiveProject(ideProject));

    newProject.setOptions( JBProjectOptions.getInstance());

    return newProject;
  }

  protected Object getCachePathForActiveProject(Object ideProject) {
      final JBProject newJBProject = (JBProject) ideProject;

      String cachePath = RefactorItPropGroup.PROP_CACHEPATH.getValue(newJBProject);
      if (cachePath == null) {
        String projectHome = PropertyManager.getSettingsRootUrl().getFileObject().
        getAbsolutePath();
          cachePath = generateNewCacheFileName(projectHome);
          setCachePathForProject(newJBProject, cachePath);
      }

      return cachePath;
  }

  private void setCachePathForProject(final JBProject newProject, String path) {
      RefactorItPropGroup.PROP_CACHEPATH.setValue(newProject, path);
  }

  protected void releaseResources(Project project) {
    super.releaseResources(project);
    Browser.getActiveBrowser().dispatchEvent(new WindowEvent(
        Browser.getActiveBrowser(), WindowEvent.WINDOW_ACTIVATED));
  }

  public RefactorItContext createProjectContext() {
    return new JBContext(getActiveProject(), Browser.getActiveBrowser());
  }

  public boolean saveAllFiles() {
      Browser browser = Browser.getActiveBrowser();
      try {
          browser.doSaveAll(false);
      } catch (VetoException ignore) {
          return false;
      }

      return true;
  }

  /*
   * @see net.sf.refactorit.commonIDE.IDEController#createMenuBuilder(java.lang.String, char, java.lang.String, boolean)
   */
  public MenuBuilder createMenuBuilder(
          String name, char mnemonic, String icon, boolean submenu
  ) {
      return new JBMenuBuilder(name, mnemonic, icon);
  }

  /*
   * @see net.sf.refactorit.commonIDE.IDEController#getIdeInfo()
   */
  public void getIdeInfo() {
      setIdeName("JBuilder");
      try {
          Method getBuildNumber
          = com.borland.jbuilder.info.JBuilderInfo.class.getMethod(
                  "getBuildNumber", new Class[0]);
          String build
          = (String) getBuildNumber.invoke(com.borland.jbuilder.info.
                  JBuilderInfo.class, new String[0]);
          setIdeBuild(build);
      } catch (Exception e) {
        AppRegistry.getExceptionLogger().error(
            e, "Unable to determine JBuilder build", this);
      }

  }

  public void setIdeVersion(byte v1, byte v2){

      /**
       JB5    - 4:2
       JB6    - 4:3
       JB7    - 4:4
       JB8    - 4:5
       JB9    - 4:6
       JBX    - 4:7
       JB2005 - 4:8
       */

      if(v1 == 4){
          if(v2 >= 2 && v2 <= 6){
              setIdeVersion(String.valueOf(v2+3));
          } else if(v2 == 7){
              setIdeVersion("X");
          } else if(v2 == 8){
              setIdeVersion("2005");
          }
      }

      // If we were unable to determine the version then we get full IDE name
      // with version
      if(getIdeVersion() == null || getIdeVersion().length() == 0){
          setIdeVersion(JBuilderInfo.getDescription());
      }

  }


  public void addIgnoredSources(Project pr, Source[] newIgnoredPaths) {
    List ignoredPaths = new ArrayList(RefactorItPropGroup.getIgnoredSourcePath());
    for (int i = 0; i < newIgnoredPaths.length; i++) {
      JBSource src = (JBSource) newIgnoredPaths[i];
      ignoredPaths.add(src.getAbsolutePath());
    }
    RefactorItPropGroup.setIgnoredSourcePath(ignoredPaths);
  }

  public String getLowMemoryWarning(int recommendedInMBs) {
  	String root = System.getProperty("jbuilder.home");
  	if (root != null) {
  		File confFile = new File(new File(root, "bin"), "jbuilder.config");
  		if (confFile.exists()) {
  			return "Specify e.g. " +
				"<pre>" +
				"vmparam -Xmx" + recommendedInMBs +"M\n" +
				"</pre>" +
				"in " + confFile + " to allow JBuilder access more memory, and restart the IDE.";
  		}
  	}
  	return super.getLowMemoryWarning(recommendedInMBs);
  }

  public WorkspaceManager getWorkspaceManager() {
    return JBWorkspaceManager.getInstance();
  }

  protected boolean checkParsingPreconditions(Project pr) {
    LoadingProperties properties = super.getLoadingProperties();
    if (!checkClassPathSanity(pr.getPaths().getClassPath(), properties.showDialogsIfNeeded)) {
      AppRegistry.getLogger(this.getClass()).debug("classpath checking failed");
      return false;
    }

    if (!checkSourcePathSanity(pr.getPaths().getSourcePath(), properties.showDialogsIfNeeded)) {
      AppRegistry.getLogger(this.getClass()).debug("sourcepath checking failed");
      return false;
    }

    return true;
  }
}
