/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.eclipse;

import java.util.Dictionary;
import java.io.File;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.exception.SystemException;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.commonIDE.ActionRepository;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.LoadingProperties;
import net.sf.refactorit.commonIDE.MenuBuilder;
import net.sf.refactorit.commonIDE.SourcesModificationOperation;
import net.sf.refactorit.commonIDE.WorkspaceManager;
import net.sf.refactorit.eclipse.vfs.EclipseClassPath;
import net.sf.refactorit.eclipse.vfs.EclipseJavadocPath;
import net.sf.refactorit.eclipse.vfs.EclipseSourcePath;
import net.sf.refactorit.exception.ErrorCodes;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.projectoptions.ProjectOptions;
import net.sf.refactorit.ui.projectoptions.ProjectSettingsListener;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.eclipse.ui.ide.IDE;
import java.util.Enumeration;
import java.util.Properties;
import java.net.URL;
import java.io.InputStream;


public class EclipseController extends IDEController {
  static final Logger log = AppRegistry.getLogger(EclipseController.class);

  /**
   * @see net.sf.refactorit.commonIDE.IDEController#onProjectChanged(net.sf.refactorit.classmodel.Project)
   */
  protected void onProjectChanged(Project oldProject) {
    super.onProjectChanged(oldProject);

    Project newProject = this.activeProject;

    String projectName = " null";
    if (newProject != null) {
      projectName = newProject.getName();
    }

    log.debug("changed project to " + projectName);
  }


  private ActionRepository actionRep;
  private IWorkspace iWorkspace;

  public EclipseController() {
    super();

    iWorkspace = ResourcesPlugin.getWorkspace();

    setInstance(this);
  }

  public int getPlatform() {
    return ECLIPSE;
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

  public RefactorItContext createProjectContext() {
    return new EclipseContext(getActiveProject());
  }

  /**
   * returns active IProject or null if IProject is not a Java Project
   */
  public Object getActiveProjectFromIDE() {
    final Object[] res = {null};
    PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
      public void run() {
        res[0] = getActiveProjectFromIDE0();
      }
    });
    return res[0];
  }

  Object getActiveProjectFromIDE0() {
    IProject project = null;

    IWorkbench workbench = PlatformUI.getWorkbench();
    IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
    ISelectionService service = window.getSelectionService();

    ISelection selection = service.getSelection();
    if (selection instanceof IStructuredSelection) {
      IStructuredSelection ss = (IStructuredSelection) selection;

      final Object element = ss.getFirstElement();
      if (element instanceof IResource) {
        project = ((IResource) element).getProject();
      } else if (element instanceof IJavaElement) {
        project = ((IJavaElement) element).getJavaProject().getProject();
      } else {
        if (element != null) {
          log.debug("selected element == " + element.getClass());
        }
      }
    }

    if (selection instanceof ITextSelection) {
      IFile file = getFileFromActiveEditor();
      if (file != null) {
        project = file.getProject();
      }
    }

    if (project != null && !JavaCore.create(project).exists()) {
      log.debug(project.getName() + " isn't java project");
      return null;
    }

    return project;
  }


  /*
   * @see net.sf.refactorit.commonIDE.IDEController#getActionRepository()
   */
  public ActionRepository getActionRepository() {
    if (actionRep == null) {
      actionRep = new EclipseActionRepository();
    }

    return actionRep;
  }

  /**
   * Returns absolute file path for using with {@link java.io.File}
   *
   * @see net.sf.refactorit.commonIDE.IDEController#getCachePathForActiveProject(Object)
   */
  protected Object getCachePathForActiveProject(Object ideProject) {
    IProject project = (IProject) getActiveProjectFromIDE();
    if(project == null) {
      // may happen if project active project is not java project
      return null;
    } else {
	    IPath root = project.getWorkingLocation(RitPlugin.getId());

	    String result = root.toOSString() + File.separator + "cache";
	    log.debug("getCachePath returned " + result);

	    return result;
    }
  }

  // TODO: Do we need a MenuBuilder factory at all?
  // MenuBuilders not used anywhere except
  // corresponding IDE integration layer
  public MenuBuilder createMenuBuilder(
      String name, char mnemonic, String icon, boolean submenu
  ) {
    return null;
  }

  /*
   * @see net.sf.refactorit.commonIDE.IDEController#getIdeInfo()
   */
  public void getIdeInfo() {
    setIdeName("Eclipse");
    try {
      setIdeVersion(
          Platform.getBundle("org.eclipse.jdt.ui").getHeaders().get(
          "Bundle-Version").toString());
    } catch (Exception e) {
      AppRegistry.getExceptionLogger().error(e,
          "Couldn't get Eclipse version", this.getClass());
    }
    try {
      URL url = Platform.getBundle("org.eclipse.jdt").getResource("about.mappings");
      Properties prop = new Properties();
      InputStream in = url.openStream();
      try {
        prop.load(in);
      } finally {
        in.close();
      }
      setIdeBuild(prop.getProperty("0"));
    } catch (Exception e) {
      AppRegistry.getExceptionLogger().error(e,
          "Couldn't get Eclipse build", this.getClass());
    }
  }

  /*
   * @see net.sf.refactorit.commonIDE.IDEController#saveAllFiles()
   */
  public boolean saveAllFiles() {
    try {
      final IWorkbench workbench = PlatformUI.getWorkbench();
      final boolean[] res = {false};
      workbench.getDisplay().syncExec(new Runnable() {
        public void run() {
          // TODO Auto-generated method stub
          res[0] = workbench.saveAllEditors(false);
        }
      });
      return res[0];
    } catch (Exception e) {
      AppRegistry.getExceptionLogger().error(e,this);
      return false;
    }
  }

  /*
   * @see net.sf.refactorit.commonIDE.IDEController#createNewProjectFromIdeProject(java.lang.Object)
   */
  protected Project createNewProjectFromIdeProject(Object ideProject) {
    IProject eclipseProject = (IProject) ideProject;

    if ( eclipseProject == null) {
      log.debug("Ide project == null");
      return null;
    }

    Project newProject;

    // TODO: it is fast hack to make closed projects still work
    if(!eclipseProject.isOpen()) {
      return null;
    }


    // Todo: implement javadoc path

    EclipseProjectOptions projectOptions = new EclipseProjectOptions(eclipseProject);

    final EclipseSourcePath srcPath = new EclipseSourcePath(
        eclipseProject,projectOptions);

    final EclipseClassPath classpath = new EclipseClassPath(eclipseProject, projectOptions);

    //classpath.setSettings(projectSettings);

    newProject = new Project(eclipseProject.getName(), srcPath, classpath, new EclipseJavadocPath());

    newProject.addProjectSettingsListener(new ProjectSettingsListener() {
      public void settingsChanged(ProjectOptions newOptions) {
        srcPath.setOptions(newOptions);
        classpath.setOptions(newOptions);
      }
    });

    // todo: implement writer
    //newProject.setWriter();

    newProject.setCachePath(getCachePathForActiveProject(ideProject));

    newProject.setOptions(projectOptions);

    return newProject;
  }

  public static IFile getFileFromActiveEditor() {
    final IFile[] file = {null};

    final IWorkbench workbench = PlatformUI.getWorkbench();
    PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
      public void run() {
        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();

        //IWorkbenchPart activePart = window.getActivePage().getActivePart();
        IEditorPart editor = window.getActivePage().getActiveEditor();

        //        if (activePart instanceof IEditorPart) {
        //          IEditorPart editor = (IEditorPart) activePart;

        if (editor.getEditorInput() instanceof IFileEditorInput) {
          file[0] = ((IFileEditorInput) editor.getEditorInput()).getFile();
        }
        //}
      }
    });

    return file[0];
  }

  /**
   * TODO: currently locks entire workspace for writing, should do more intelligently, for sources and project maybe?
   *
   * @see net.sf.refactorit.commonIDE.IDEController#run(net.sf.refactorit.commonIDE.SourcesModificationOperation)
   */
  public void run(final SourcesModificationOperation op) {
    try {
      iWorkspace.run(new IWorkspaceRunnable() {
        public void run(IProgressMonitor monitor) {
          log.debug("executing sources modification operation");
          op.run();
        }
      }, iWorkspace.getRoot(), IWorkspace.AVOID_UPDATE, null);
    } catch (CoreException e) {
      log.error(e);
      throw new SystemException(ErrorCodes.ECLIPSE_INTERNAL_ERROR, e);
    }
  }

  public WorkspaceManager getWorkspaceManager() {
    return EclipseWorkspaceManager.getInstance();
  }

  public String getLowMemoryWarning(int recommendedInMBs) {
    return "Use the -vmargs -Xmx" +  recommendedInMBs
        + "M JVM option when starting Eclipse to give it more memory.";
  }
}
