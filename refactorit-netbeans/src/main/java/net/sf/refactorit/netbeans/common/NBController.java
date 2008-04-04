/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common;


import javax.swing.SwingUtilities;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.exception.SystemException;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.commonIDE.ActionRepository;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.LoadingProperties;
import net.sf.refactorit.commonIDE.MenuBuilder;
import net.sf.refactorit.commonIDE.SourcesModificationOperation;
import net.sf.refactorit.commonIDE.WorkspaceManager;
import net.sf.refactorit.exception.ErrorCodes;
import net.sf.refactorit.loader.ASTTreeCache;
import net.sf.refactorit.loader.ProjectChangedListener;
import net.sf.refactorit.netbeans.common.projectoptions.NBProjectOptions;
import net.sf.refactorit.netbeans.common.projectoptions.PathItemReference;
import net.sf.refactorit.netbeans.common.projectoptions.PathUtil;
import net.sf.refactorit.netbeans.common.projectoptions.ProjectsManager;
import net.sf.refactorit.netbeans.common.projectoptions.ui.SettingsDialog;
import net.sf.refactorit.netbeans.common.standalone.ErrorManager;
import net.sf.refactorit.netbeans.common.standalone.IdeVersion;
import net.sf.refactorit.netbeans.common.vfs.NBClassPath;
import net.sf.refactorit.netbeans.common.vfs.NBJavadocPath;
import net.sf.refactorit.netbeans.common.vfs.NBSource;
import net.sf.refactorit.netbeans.common.vfs.NBSourcePath;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.utils.RefactorItConstants;
import net.sf.refactorit.utils.XMLSerializer;
import net.sf.refactorit.vfs.Source;

import org.apache.log4j.Logger;

import org.openide.LifecycleManager;
import org.openide.actions.SaveAllAction;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;


/**
 *
 *
 * @author Tonis Vaga
 * @author Oleg Golovachov
 */
public class NBController extends IDEController {
  private static final Logger log = Logger.getLogger(NBController.class);

  private ProjectId cachedProjectNode;

  private ActionRepository repository;

  //inited in RefactorItActions
  private static NBControllerVersionState versionStateObj=null;

  /**called from RefactorItActions*/
  public static void setVersionState(NBControllerVersionState versionStateObj) {
    NBController.versionStateObj=versionStateObj;
  }

  public static NBControllerVersionState getVersionState() {
    return versionStateObj;
  }

  public ActionRepository getActionRepository() {
    if (repository == null) {
      repository = new NBActionRepository();
    }

    return repository;
  }

  public NBController() {
    super();
    IDEController.setInstance(this);
  }

  public int getPlatform() {
    return NETBEANS;
  }

  protected static boolean serializeCacheOldImpl(Project project) {
    if (project == null) {
      return true;
    }

    FileLock lock = null;
    FileObject loadedCachePath = (FileObject) project.getCachePath();
    Assert.must(loadedCachePath != null);

    try {
      lock = loadedCachePath.lock();
      ASTTreeCache.writeCache(project.getProjectLoader().getAstTreeCache(), loadedCachePath
          .getOutputStream(lock));
      return true;
    } catch (IOException e) {
      net.sf.refactorit.common.util.AppRegistry.getExceptionLogger().error(e,
          NBController.class);
      return false;
    } finally {
      if (lock != null) {
        lock.releaseLock();
      }
    }
  }

  /**
   * @return true if project saved last time with setActiveProject is not same
   *         with IDE current project.
   */
  public boolean isProjectChangedInIDE() {
    ProjectId activeProjectNode = VersionSpecific.getInstance().getProjectId();
    if(activeProjectNode == null) {
      return false;
    }
    return !activeProjectNode.equals(cachedProjectNode);
  }

  public ASTTreeCache readCache(final Object cachePath) {
    removeOldFormatCacheFileIfExists(); // No converting from old format --
                                        // safer & simpler
    final FileObject loadedCachePath = (FileObject) cachePath;
    try {
      return ASTTreeCache.readCache(loadedCachePath.getInputStream(),
          (int) loadedCachePath.getSize());
    } catch (FileNotFoundException e) {
      log.warn(e.getMessage(), e);
      return null;
    }
  }

  /**
   * Introduced in RefactorIT version 1.2.2 RCs. Perhaps can be removed a long
   * time after?
   */
  private static void removeOldFormatCacheFileIfExists() {
    String path = RefactorItOptions.getDefault().getRefactoryCachePath();
    if (path != null && new File(path).exists()) {
      try {
        File oldCache = new File(path);
        File folder = oldCache.getParentFile();
        File refactoritConfFolder = folder.getParentFile();

        oldCache.delete();
        folder.delete();
        refactoritConfFolder.delete();
      } catch (Exception ignore) {
      }

      RefactorItOptions.getDefault().setRefactoryCachePath(null);
    }
  }

  /**
   *
   * @return true if succeeded
   */
  public boolean saveAllFiles() {
    SaveAllAction save = (SaveAllAction) SystemAction.get(SaveAllAction.class);
    if (save.isEnabled()) {
      save.performAction();
    } else {
      LifecycleManager.getDefault().saveAll();
    }
    return true;
  }

  static void showSettingsDialog(Object ideProject) {
    IDEController instance = getInstance();

    if ( ideProject == null) {
      RitDialog.showMessageDialog(
          instance.createProjectContext(),
          "No active project, or selection is ambiguous.\n" +
          "Please select a project first!",
          "No active project", JOptionPane.INFORMATION_MESSAGE);
      return;
    }
    Object projectKey = IDEController.getInstance().getWorkspaceManager()
        .getIdeProjectIdentifier(ideProject);
    NBProjectOptions options= NBProjectOptions.getInstance(projectKey);
    Project project = instance.getWorkspace().getProject(ideProject);
//    Project project = instance.getActiveProject();
    if (SettingsDialog.showAndEditOptions(instance.createProjectContext(), ideProject, options)) {
      if (project != null) {
        // SourcePath don't store their actual source path, rather they work it
        // out
        // on the fly, however if we don't set a new source path then the
        // changes
        // won't get acted upon.

        project.getPaths().setSourcePath(new NBSourcePath(ideProject));
        project.getPaths().setJavadocPath(new NBJavadocPath(ideProject));

        // HACK: We need to call "clean rebuild" on project, otherwise
        // classpath will have some weird "class not found" bugs after
        // modification.
        // FIXME: We can't show progress dialog for both of the ensureProject()
        // calls because then we'd also see messages like "empty sourcepath" twice.
        if ("true".equals(
            GlobalOptions.getOption("rebuild.project.options.change", "true"))) {
          instance.ensureProject(new LoadingProperties(true, false, false));
          instance.ensureProject(new LoadingProperties(false, true, true));
        }
      }
    }
  }

  /////////////////////////////////////////////////
  /// IDEController implementation functions
  /////////////////////////////////////////////////

  public boolean serializeProjectCache(Project prj, boolean showDialogs) {

    if (RefactorItConstants.debugInfo) {
      if (prj != null) {
        FileObject loadedCachePath = (FileObject) prj.getCachePath();

        try {
          loadedCachePath.getURL();
        } catch (FileStateInvalidException ex) {
          log.warn(ex);
        }
      }
    }

    return serializeCacheOldImpl(prj);
  }

  public void deserializeCache(boolean showDialog) {
    if (RefactorItConstants.debugInfo) {
      FileObject loadedCachePath = (FileObject) getActiveProject()
          .getCachePath();

      Assert.must(loadedCachePath != null);

      try {
        loadedCachePath.getURL();
      } catch (FileStateInvalidException ex) {
        log.warn(ex);
      }
    }

    super.deserializeCache(showDialog);
  }

  protected Object getCachePathForActiveProject(Object ideProject) {
    return ProjectsManager.getCacheFile();
  }

  protected void beforeEnsureProject() {
    RefactorItActions.importFormatterEngineDefaults(); // it will ask once only
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



  static void ensureControllerInit() {
    if (getInstance() == null) {
      new NBController();
      //      IDEController.createFor(IDEController.NETBEANS);
    }
  }

  protected Project createNewProjectFromIdeProject(Object ideProject) {
    // FIXME: fix to depend on ide project!!

    Project newProject;
    // checking wether we need to save old ASTTreeCache
    //      serializeOldProject();
    NBSourcePath sourcePath = new NBSourcePath(ideProject);
    NBClassPath classpath = new NBClassPath(ideProject);
    NBJavadocPath javadocPath = new NBJavadocPath(ideProject);
    newProject = new Project(
        getWorkspaceManager().getIdeProjectIdentifier(ideProject).toString(),
        sourcePath, classpath, javadocPath);



    // saves old active project stuff
    if (this.activeProject != null) {
      // here we are moving all old project listeners to the new one
      Vector listeners = this.activeProject.getProjectLoader()
          .cloneProjectChangedListeners();
      for (int i = 0; i < listeners.size(); i++) {
        newProject.getProjectLoader().addProjectChangedListener((ProjectChangedListener) listeners.get(i));
      }
    }

    newProject.setCachePath(getCachePathForActiveProject(ideProject));

    Object projectKey = IDEController.getInstance().getWorkspaceManager()
    .getIdeProjectIdentifier(ideProject);
    newProject.setOptions(NBProjectOptions.getInstance(projectKey));
    return newProject;
  }

  public void setActiveProject(Project project) {
    //project.setWriter(IOProvider.getDefault().getIO("RefactorIT", true).getOut());
    super.setActiveProject(project);
    cachedProjectNode = VersionSpecific.getInstance().getProjectId();
  }

  public RefactorItContext createProjectContext() {
    return new NBContext(getActiveProject());
  }

  /*
   * @see net.sf.refactorit.commonIDE.IDEController#createMenuBuilder(java.lang.String,
   *      char, java.lang.String, boolean)
   */
  public MenuBuilder createMenuBuilder(String name, char mnemonic, String icon,
      boolean submenu) {
    return new NBMenuBuilder(name, mnemonic, icon);
  }

  /*
   * @see net.sf.refactorit.commonIDE.IDEController#getIdeInfo()
   */
  public void getIdeInfo() {
    String currentVersion;
    try {
      currentVersion = NbBundle.getBundle("org.netbeans.core.Bundle").getString(
          "currentVersion");
    } catch (Exception e) {
      log.warn("Got an exception when asking for currentVersion", e);
      return;
    }

    String buildNumber = System.getProperty("netbeans.buildnumber");

    IdeVersion result = new IdeVersion(currentVersion, buildNumber);

    if( ! result.isValid()) {
      log.warn("Could not detect IDE version");
      return;
    }

    setIdeBuild(buildNumber);
    setIdeName(result.name);
    setIdeVersion(result.version);
  }

  public XMLSerializer getXMLSerializer() {
  	return NBSerializer.instance;
  }

  public boolean isExitIdePossible() {
  	return true;
  }

  public void exitIde() {
  	LifecycleManager.getDefault().exit();
  }

  // FIXME: for NB >= 3.5 and use org.openide.kib.xerces/1 module
  static class NBSerializer extends XMLSerializer {
    static final NBSerializer instance = new NBSerializer();

    private NBSerializer() {}

    public void serialize(Document document,
        OutputStream out) throws IOException {
      serialize(document, out, "UTF-8");
    }

    public void serialize(Document document, OutputStream out,
        String enc) throws IOException {
      XMLUtil.write(document, out, enc);
    }
  }

  public Object getActiveProjectFromIDE() {
    return versionStateObj.getActiveProjectFromIDE();
  }

  public void addIgnoredSources(Project pr, Source[] sourcePaths) {
  Object projectKey = IDEController.getInstance().getWorkspaceManager()
        .getProjectIdentifier(pr);

    List ignoredPaths = new ArrayList(Arrays.asList(NBProjectOptions.getInstance(projectKey).getUserSpecifiedIgnoredSourcePathDirectories()));
    for (int i = 0; i < sourcePaths.length; i++) {
      NBSource source = (NBSource) sourcePaths[i];
      PathItemReference ref = new PathItemReference(source.getFileObject());
      ignoredPaths.add(ref);
    }
    NBProjectOptions.getInstance(projectKey).setUserSpecifiedIgnoredSourcePathDirectories(
        (PathItemReference[])ignoredPaths.toArray(new PathItemReference[0]));
  }

  public String getLowMemoryWarning(int recommendedInMBs) {
    String confFile = PathUtil.getInstance().getNbConfigFile();

    if (confFile != null && new File(confFile).exists()) {
      if(RefactorItActions.isNetBeansFour()) {
        return "Specify e.g. " +
            "<pre>" +
            "netbeans_default_options=\"-J-Xms32m -J-Xmx" + recommendedInMBs + "m -J-Xverify:none\"\n" +
            "</pre>" +
            "in " + confFile + " to allow the IDE access more memory, and restart the IDE.";
      } else {
        return "Specify e.g. " +
          "<pre>" +
          "-J-Xms32m -J-Xmx" + recommendedInMBs + "m\n" +
          "</pre>" +
          "in " + confFile + " to allow the IDE access more memory, and restart the IDE.";
      }
    }

  	return super.getLowMemoryWarning(recommendedInMBs);
  }

  public void run(final SourcesModificationOperation op) {
    if(SwingUtilities.isEventDispatchThread()) {
      // Let's just run it directly, no RequestProcessor here. Reason: we don't want to block
      // the Swing thread while we're waiting for the completion of the operation (task.waitFinished()
      // could cause deadlocks if the operation itself waited for something to be ran in the Swing thread;
      // this fixes RIM-503).
      op.run();
    } else {
      final Exception exception[] = new Exception[] {null};

      RequestProcessor.Task task = RequestProcessor.getDefault().post(new Runnable() {
        public void run() {
          versionStateObj.beginTransaction();
          try {
            op.run();
          } catch(Exception e) {
            exception[0] = e;
          } finally {
            versionStateObj.endTransaction();
          }
        }
      } );

      if(exception[0] != null) {
        throw new RuntimeException(exception[0]);
      }

      task.waitFinished();
    }

    if(op.getException() != null) {
      AppRegistry.getExceptionLogger().error(op.getException(), VersionSpecific.class);
      throw new SystemException(ErrorCodes.INTERNAL_ERROR, op.getException());
    }
  }

  public WorkspaceManager getWorkspaceManager() {
    return NBWorkspaceManager.getInstance();
  }

  public void showAndLogInternalError(Throwable ex) {
    ErrorManager.showAndLogInternalError(ex);
  }
}
