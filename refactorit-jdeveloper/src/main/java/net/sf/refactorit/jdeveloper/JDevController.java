/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jdeveloper;


import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.commonIDE.ActionRepository;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.MenuBuilder;
import net.sf.refactorit.jdeveloper.projectoptions.JDevProjectOptions;
import net.sf.refactorit.jdeveloper.projectoptions.ProjectConfiguration;
import net.sf.refactorit.jdeveloper.vfs.JDevClassPath;
import net.sf.refactorit.jdeveloper.vfs.JDevJavadocPath;
import net.sf.refactorit.jdeveloper.vfs.JDevSourcePath;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.vfs.ClassPath;
import net.sf.refactorit.vfs.Source;

import oracle.ide.Ide;
import oracle.ide.IdeAction;
import oracle.ide.IdeConstants;
import oracle.ide.addin.Context;
import oracle.jdeveloper.model.JProject;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *
 *
 * @author Tonis Vaga
 */
public class JDevController extends IDEController {
  JDevController() {
    super();
    setInstance(this);
  }

  private ActionRepository repository;

//  public static JProject cachedJProject;
//  public static Project refactorItProject;
//  public static String cachePath;
//
//  private static ClassPath classpath;

  Context lastEventContext;

  public Context getLastEventContext() {
    return lastEventContext;
  }

  public ActionRepository getActionRepository() {
    if (repository == null) {
      repository = new JDevActionRepository();
    }
    return repository;
  }

  public Object getActiveProjectFromIDE() {
    if (Ide.isStarting()) {
      return null;
    }

    return Ide.getActiveProject();
  }

  /**
   * Creates new project from ideProject
   *
   * @param ideProject object returned by {@link #getActiveProjectFromIDE() }
   */
  public Project createNewProjectFromIdeProject(Object ideProject) {
    JProject jdevIdeProject = (JProject) ideProject;

    ClassPath classpath = new JDevClassPath(jdevIdeProject);

    Project newProject = new Project(jdevIdeProject.getLongLabel(),
        new JDevSourcePath(jdevIdeProject), classpath,
        new JDevJavadocPath(jdevIdeProject));

    //newProject.setWriter(new PrintWriter(new RefactorItWriter()));

    newProject.setCachePath(getCachePathForActiveProject(ideProject));
    newProject.setOptions(JDevProjectOptions.getInstance());

    ProjectConfiguration.getActiveInstance().setCachePath(
        (String) getCachePathForActiveProject(ideProject));

    jdevIdeProject.markDirty(true);

    return newProject;
  }

  protected Object getCachePathForActiveProject(Object ideProject) {
    if (Ide.isStarting()) {
      return null;
    }

    //cachePath = (String)cachedJProject.getProjectSettings().getCommonData().get( PROP_CACHEPATH );
    String cachePath = ProjectConfiguration.getActiveInstance().getCachePath();
    if (cachePath == null) {
      cachePath = generateNewCacheFileName(Ide.getUserHomeDirectory());
    }

    return cachePath;
  }

  public boolean saveAllFiles() {
    boolean wasSaved = true;
    // Request the save action from IDE and call the supervisor controller
    // to handle the requested action. I.e. save all files before compiling
    // and before parsing the source files.
    IdeAction saveAction = IdeAction.find(IdeConstants.SAVE_ALL_CMD_ID);
    saveAction.actionPerformed(new ActionEvent(
        AbstractionUtils.getMainWindow(),
        ActionEvent.ACTION_PERFORMED, ""));

    return wasSaved;
  }

  public RefactorItContext createProjectContext() {
    return new JDevContext(getActiveProject());
  }

  public int getPlatform() {
    return JDEV;
  }

  public MenuBuilder createMenuBuilder(
      String name, char mnemonic, String icon, boolean submenu) {
    return new JDevMenuBuilder(name, mnemonic, icon, submenu);
  }

//  public Object getBackInfo() {
//
//    if ( lastEventContext == null ) {
//      return null;
//    }
//    Object result=null;
//
//    if ( lastEventContext.getView() instanceof CodeEditor ) {
//      CodeEditor editor=(CodeEditor) lastEventContext.getView();
//      if ( editor == null ) {
//        return null;
//      }
//      int line=editor.getCaretPosition();
  //  result=new BackAction.BackInfo();
//
//    }
//  }

  void setLastEventContext(Context context) {
    lastEventContext = context;
  }

  /*
   * @see net.sf.refactorit.commonIDE.IDEController#getIdeInfo()
   */
  public void getIdeInfo() {
    setIdeName("JDeveloper");
    boolean error = false;
    String version = System.getProperty("product.version");

    if (version == null) {
      error = true;
    }

    if (!error) {
      Pattern p = Pattern.compile("([[0-9]+\\.]+)\\.([0-9]+)");
      Matcher m = p.matcher(version);
      if (m.find()) {
        setIdeVersion(m.group(1));
        setIdeBuild(m.group(2));
      } else {
        error = true;
      }
    }

    if (error) {
      AppRegistry.getLogger(this.getClass()).error(
          "Can't determine JDeveloper version");
    }
  }

  public void addIgnoredSources(Project pr, Source[] sourcePaths) {
    ProjectConfiguration projectConfig = ProjectConfiguration.getActiveInstance();

    String ignoredSourcepathStr =
      projectConfig.get(ProjectConfiguration.PROP_IGNORED_SOURCEPATH);

    StringBuffer result = new StringBuffer();
    if (ignoredSourcepathStr != null) {
      result.append(ignoredSourcepathStr);
    }

    for (int i = 0; i < sourcePaths.length; i++) {
      if (result.length() > 0) {
        result.append(File.pathSeparator);
      }

      result.append(sourcePaths[i].getAbsolutePath());
    }

    projectConfig.set(ProjectConfiguration.PROP_IGNORED_SOURCEPATH, result.toString());
  }

  public String getLowMemoryWarning(int recommendedInMBs) {
  	String binDir = Ide.getBinDirectory();
  	if (binDir != null) {
  		File confFile = new File(binDir, "jdev.conf");
  		if (confFile.exists()) {
  			return "Specify e.g. " +
				"<pre>" +
				"AddVMOption     -Xmx" + recommendedInMBs +"M\n" +
				"</pre>" +
				"in " + confFile +
				" to allow JDeveloper access more memory, and restart the IDE.";
  		}
  	}

  	return super.getLowMemoryWarning(recommendedInMBs);
  }
}
