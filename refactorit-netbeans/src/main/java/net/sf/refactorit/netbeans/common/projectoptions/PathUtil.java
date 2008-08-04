/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.projectoptions;

import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.netbeans.common.RefactorItActions;
import net.sf.refactorit.netbeans.common.VersionSpecific;
import net.sf.refactorit.netbeans.common.util.FileObjectTraverseListener;
import net.sf.refactorit.utils.FileUtil;
import net.sf.refactorit.vfs.Source;

import org.apache.log4j.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystemCapability;
import org.openide.filesystems.Repository;
import org.openide.modules.InstalledFileLocator;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


/**
 * @author  tanel
 */
public class PathUtil {
  private static final Logger log = Logger.getLogger(PathUtil.class);

  private final Repository repository = Repository.getDefault();

  private static PathUtil instance = new PathUtil();

  private PathUtil() {}

  public static PathUtil getInstance() {
    return instance;
  }

  public static void setInstance(PathUtil newInstance) {
    instance = newInstance;
  }

  public PathItemReference[] getClasspath(Object ideProject) {
    Object projectKey = IDEController.getInstance().getWorkspaceManager()
    .getIdeProjectIdentifier(ideProject);
    NBProjectOptions options = NBProjectOptions.getInstance(projectKey);
    if (options.getAutodetectPaths()) {
      return getAutodetectedClasspath(ideProject);
    } else {
      return options.getUserSpecifiedClassPath();
    }
  }

  public PathItemReference[] getSourcepath(Object ideProject) {
    return getSourcepath(ideProject, false);
  }

  public PathItemReference[] getSourcepath(Object ideProject, boolean includeIgnoredListItems) {
    if(ideProject == null) {
      return new PathItemReference[]{};
    }

    Object projectKey = IDEController.getInstance().getWorkspaceManager()
    .getIdeProjectIdentifier(ideProject);
    NBProjectOptions options = NBProjectOptions.getInstance(projectKey);
    PathItemReference[] pathItems = null;
    if (options.getAutodetectPaths()) {
      pathItems = getAutodetectedSourcepath(ideProject, includeIgnoredListItems);
    } else {
      pathItems = options.getUserSpecifiedSourcePath(includeIgnoredListItems);
    }
    return pathItems;
  }

  public PathItemReference[] getIgnoredSourceDirectories(Object ideProject) {
    Object projectKey = IDEController.getInstance().getWorkspaceManager()
    .getIdeProjectIdentifier(ideProject);
    NBProjectOptions options = NBProjectOptions.getInstance(projectKey);
    return options.getUserSpecifiedIgnoredSourcePathDirectories();
  }

  public IgnoreListFilter getIgnoreListFilter(Object ideProject) {
    return new IgnoreListFilter(ideProject);
  }

  public PathItemReference[] getAutodetectedClasspath(Object ideProject) {
    List result = new ArrayList(10);

    CollectionUtil.addAllNew(result, VersionSpecific.getInstance().getBootClasspath(ideProject));
    CollectionUtil.addAllNew(result, VersionSpecific.getInstance().getClasspath(ideProject));

    return (PathItemReference[]) result.toArray(
        new PathItemReference[result.size()]);
  }

  /**
   * For each newly mounted filesystem, asks the user if it should
   * be included in sourcepath.
   * @param ideProject
   */
  public void checkForNewlyMountedFilesystems(Object ideProject) {
    getAutodetectedSourcepath(ideProject, false);
  }

  public PathItemReference[] getAutodetectedSourcepath(Object ideProject,
      boolean includeIgnoredListItems) {
    List sources = new ArrayList(10);

    FileObject[] roots = VersionSpecific.getInstance().getIdeSourcepath(ideProject);
    try {
      for (int i = 0; i < roots.length; i++) {
        PathItemReference item = getValidReferenceOrNull(roots[i]);
        if(item == null) {
          continue;
        }
        Object projectKey = IDEController.getInstance().getWorkspaceManager()
        .getIdeProjectIdentifier(ideProject);
        if ((!includeIgnoredListItems) &&
            ( ! NBProjectOptions.getInstance(projectKey).getSourcepathFilter().
                includePathItem(item))) {
          // not refactorable found
          continue;
        }

        sources.add(item);
      }
    } catch (FileStateInvalidException e1) {
      log.warn(e1.getMessage(), e1);
    }

    return (PathItemReference[]) sources.toArray(new PathItemReference[sources
        .size()]);
  }

  private PathItemReference getValidReferenceOrNull(FileObject fileObject) throws FileStateInvalidException {
    PathItemReference result = new PathItemReference(fileObject);

    if (fileObject.isValid()
        && fileObject.canWrite()
        && (!fileObject.getFileSystem().isDefault())
        && result.isValid()) {
      return result;
    } else {
      return null;
    }
  }

  private boolean hiddenFilesAllowedForSourcepath() {
    return true;
  }

  public boolean isRefactorable(FileSystem fs) {
    if (!fs.isValid() /*|| fs.isHidden()*/ || fs.isReadOnly() || fs.isDefault() ||
        (!fs.getCapability().capableOf(FileSystemCapability.COMPILE)) ||
        (fs.isHidden() && (!hiddenFilesAllowedForSourcepath()))) {
      return false;
    }

    return true;
  }

  public boolean sourceOnSourcepath(Object ideProject, FileObject fileObject) {
    if (fileObjectOnSourcepath(ideProject, fileObject)) {
      return true;
    }

    try {
      return fileOnSourcepath(ideProject, fileObject);
    } catch (Exception e) {
      return false;
    }
  }

  private boolean fileObjectOnSourcepath(Object ideProject, FileObject fileObject) {
    return
        PathItemReference.inListOrParentInList(fileObject, getSourcepath(ideProject)) &&
        ( ! PathItemReference.inListOrParentInList(fileObject, getIgnoredSourceDirectories(ideProject)));
  }

  private boolean fileOnSourcepath(Object ideProject, FileObject fileObject) throws
      FileStateInvalidException {
    File file = FileObjectUtil.getFileOrNull(fileObject);
    if (file == null) {
      return false;
    }

    return PathItemReference.inListOrParentInList(file, getSourcepath(ideProject)) &&
        ( ! PathItemReference.inListOrParentInList(file, getIgnoredSourceDirectories(ideProject)));
  }

  public Enumeration getFileSystems() {
    return repository.getFileSystems();
  }

  public static class IgnoreListFilter {
    private Source[] ignoredSources;

    IgnoreListFilter(Object ideProject) {
      List ignoredSourcesList = new ArrayList();

      PathItemReference[] ignoreReference = PathUtil.getInstance().getIgnoredSourceDirectories(ideProject);
      for (int i = 0; i < ignoreReference.length; i++) {
        if (ignoreReference[i].isValid()) {
          ignoredSourcesList.add(ignoreReference[i].getSource());
        }
      }

      ignoredSources = (Source[]) ignoredSourcesList.toArray(Source.NO_SOURCES);
    }

    /** Does not check children/parents/etc */
    public boolean inIgnoreList(Source source) {
      for (int i = 0; i < ignoredSources.length; i++) {
        if (ignoredSources[i].equals(source)) {
          return true;
        }
      }

      return false;
    }
  }


  /**
   * Traverses the PathItemReference object entirely. Goes through all directories under the
   * object if they exists and calls appropriate methods on listener object.
   *
   * @param file The FileObject object to traverse.
   * @param listener The listener object on what the methods are called by the algorithm
   * to notify about the events (found file, entering/exiting directory).
   * @return The event (STOP_PROCESSING, CONTINUE_PROCESSING, ...) that finished the traverse.
   */
  public int traverseFileObject(FileObject file,
      FileObjectTraverseListener listener) {
    // if the file denotes the file then call listener foundFile(..) method,
    // otherwise call listener exter/exit Directory methods and traverse the directory.
    if (file.isData()) {
      int fileTraversingStatus = listener.foundFile(file);
      return fileTraversingStatus;
    } else {
      int fileTraversingStatus = listener.enterDirectory(file);
      if (fileTraversingStatus
          != FileObjectTraverseListener.CONTINUE_PROCESSING) {
        return fileTraversingStatus;
      }
      FileObject[] files = file.getChildren();
      for (int i = 0; i < files.length; i++) {
        fileTraversingStatus = traverseFileObject(files[i], listener);
        if (fileTraversingStatus == FileObjectTraverseListener.STOP_PROCESSING) {
          return fileTraversingStatus;
        }
      }
      fileTraversingStatus = listener.exitDirectory(file);
      return fileTraversingStatus;
    }
  }

  public String getNbLog() {
    return
        RefactorItActions.isNetBeansFour() ?
        FileUtil.useSystemPS(System.getProperty("netbeans.user") + "/var/log/messages.log") :
        FileUtil.useSystemPS(System.getProperty("netbeans.user") + "/system/ide.log");
  }

  public String getNbConfigFile() {
    File result;

    if (RefactorItActions.isNetBeansFour()) {
  	  result = new File(new File(getNbInstallFolder(), "etc"), "netbeans.conf");
  	} else {
  		result = new File(new File(getNbInstallFolder(), "bin"), "ide.cfg");
  	}

    return result.getAbsolutePath();
  }

  public String getNbInstallFolder() {
    File ritHomeDir = InstalledFileLocator.getDefault().
        locate("refactorit/modules", null, false);

    if (ritHomeDir == null) {
      return null;
    }

    // to find where refactorit/modules is placed
    File modulesParentDir = ritHomeDir.getParentFile().getParentFile();

    if (RefactorItActions.isNetBeansFour()) {
      modulesParentDir = modulesParentDir.getParentFile();
      if (modulesParentDir == null) {
        return null;
      }
    }

    return modulesParentDir.getAbsolutePath();
  }
}
