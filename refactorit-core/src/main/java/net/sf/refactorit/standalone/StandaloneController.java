/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.standalone;

import net.sf.refactorit.Version;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.commonIDE.ActionRepository;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.MenuBuilder;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.standalone.projectoptions.StandaloneProjectOptions;
import net.sf.refactorit.ui.errors.ErrorsTab;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.projectoptions.ProjectOptions;
import net.sf.refactorit.ui.projectoptions.ProjectSettingsListener;
import net.sf.refactorit.ui.tree.PackageTreeModel;
import net.sf.refactorit.utils.SwingUtil;
import net.sf.refactorit.vfs.SourcePath;
import net.sf.refactorit.vfs.local.LocalClassPath;
import net.sf.refactorit.vfs.local.LocalJavadocPath;
import net.sf.refactorit.vfs.local.LocalSourcePath;

import java.io.File;
import java.io.IOException;


public class StandaloneController extends IDEController {
  private final JBrowserPanel browser;
  private final JRefactorItFrame frame;

  private ActionRepository actRep;

  public RefactorItProject activeProjectFromIDE;
  private boolean projectChangedInIDE = true;

  public ActionRepository getActionRepository() {
    if (actRep == null) {
      actRep = new StandaloneActionRepository();
    }

    return actRep;
  }

  public StandaloneController() {
    setInstance(this);

    browser = new JBrowserPanel();
    frame = new JRefactorItFrame();
  }

  public boolean saveAllFiles() {
    return true;
  }

  public static final JRefactorItFrame getMainWindow() {
    return ((StandaloneController) getInstance()).frame;
  }


  public Object getCachePathForActiveProject(Object ideProject) {
    return getActiveProject().getCachePath();
  }

  public boolean isProjectChangedInIDE() {
    return projectChangedInIDE;
  }

  public Object getActiveProjectFromIDE() {
    return activeProjectFromIDE;
  }

  public void setActiveProject(Project project) {
    super.setActiveProject(project);
    projectChangedInIDE = false; // finished update
  }

  public JBrowserPanel getBrowser() {
    return this.browser;
  }

  protected boolean checkParsingPreconditions(Project pr) {
    ErrorsTab.remove();

//    getBrowser().getTree().rebuild(PackageTreeModel.empty);

    getBrowser().getStatus().setStatus(
        JRefactorItFrame.resLocalizedStrings.getString("status.parsing"));

    return super.checkClassPathSanity(getActiveProject().getPaths().getClassPath(), true);
  }

  protected boolean processParsingResult(final Project project, ParsingResult result) {
    SwingUtil.invokeLater(new Runnable() {
      public void run() {
        if (!project.getProjectLoader().isLoadingCompleted()) {
          getBrowser().getTree().rebuild(PackageTreeModel.EMPTY);
          getBrowser().getStatus().setStatus("Loading cancelled");
        } else {
          getBrowser().setProject(project);
          getBrowser().rebuildTree();
          getBrowser().getTree().smartExpand();
          getBrowser().getStatus().setStatus(
              JRefactorItFrame.resLocalizedStrings.getString("status.ready"));
        }

        getBrowser().reload();
      }
    });

    return super.processParsingResult(project, result);
  }

  public int getPlatform() {
    return STANDALONE;
  }

  public RefactorItContext createProjectContext() {
    return new BrowserContext(getCachedActiveProject(), getBrowser());
  }

  /*
   * @see net.sf.refactorit.commonIDE.IDEController#createMenuBuilder(java.lang.String, char, java.lang.String, boolean)
   */
  public MenuBuilder createMenuBuilder(
      String name, char mnemonic, String icon, boolean submenu
  ) {
    return new StandaloneMenuBuilder(name, mnemonic, submenu);
  }

  /*
   * @see net.sf.refactorit.commonIDE.IDEController#getIdeInfo()
   */
  public void getIdeInfo() {
    setIdeName("Standalone");
    setIdeVersion(Version.getVersion());
    setIdeBuild(Version.getBuildId());
  }

  /*
   * @see net.sf.refactorit.commonIDE.IDEController#createNewProjectFromIdeProject(java.lang.Object)
   */
  protected Project createNewProjectFromIdeProject(Object ideProject) {
    RefactorItProject ritProject = (RefactorItProject) ideProject;

    StandaloneProjectOptions options = ritProject.options;

    SourcePath srcPath = createSourcePath(options);
    LocalClassPath clsPath = createClassPath(options);

    LocalJavadocPath jdocPath = new LocalJavadocPath(
        options.getJavadocPath().toString());

    Project result = new Project(
        ritProject.getName(), srcPath, clsPath, jdocPath);
    result.setCachePath(getCachePathFor(ritProject));
    result.setOptions(options);

    return result;
  }

  SourcePath createSourcePath(ProjectOptions projectOptions) {
    return new LocalSourcePath(projectOptions.getSourcePath(),
        projectOptions.getIgnoredSourcePath());
  }

  LocalClassPath createClassPath(ProjectOptions projectOptions) {
    return new LocalClassPath(projectOptions.getClassPath().toString());
  }

  private String getCachePathFor(RefactorItProject ritProject) {
    // project names can be same for different files but hashcode should be
    // unique enough for a path
    return GlobalOptions.configDir + File.separatorChar + "cache"
        + File.separatorChar + ritProject.getName()
        + ritProject.getFile().hashCode() + ".cache";
  }

  /**
   * creates and sets active ide project
   * @param projectFile
   * @throws IOException
   */
  public RefactorItProject createIDEProject(File projectFile) throws IOException {
    setIdeProject(projectFile);

    Project project = getActiveProject();

    project.addProjectSettingsListener(new ProjectSettingsListener() {
      public void settingsChanged(ProjectOptions newOptions) {
        RefactorItProject rPrj = activeProjectFromIDE;

        rPrj.setChanged(true);

        // fixme: LocalClassPath etc should use project settings instead creating here
        Project aPrj = getActiveProject();
        aPrj.getPaths().setSourcePath(createSourcePath(newOptions));
        aPrj.getPaths().setClassPath(createClassPath(newOptions));
      }
    });

    return activeProjectFromIDE;
  }

  public String getLowMemoryWarning(int recommendedInMBs) {
  	String modulesDir = System.getProperty("refactorit.modules");
  	if (modulesDir != null) {
  		File confFile = new File((new File(modulesDir)).getParent(), "RefactorIT.lax");

  		if (confFile.exists()) {
  			return "Specify e.g. " +
				"<pre>" +
				"lax.nl.java.option.java.heap.size.max=" +
				(recommendedInMBs * 1024 * 1024) + "\n" +
				"</pre>" +
				"in " + confFile +
				" to allow RefactorIT access more memory, and restart the program.";
  		}
  	}

  	return super.getLowMemoryWarning(recommendedInMBs);
  }

  public void setIdeProject(File project) throws IOException {
    activeProjectFromIDE = new RefactorItProject(project);
    projectChangedInIDE = true;
  }
}
