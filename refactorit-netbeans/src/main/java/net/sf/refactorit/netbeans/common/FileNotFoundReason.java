/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common;


import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.netbeans.common.projectoptions.FileObjectUtil;
import net.sf.refactorit.netbeans.common.projectoptions.NBProjectOptions;
import net.sf.refactorit.netbeans.common.projectoptions.PathItemReference;
import net.sf.refactorit.netbeans.common.projectoptions.PathUtil;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.utils.RefactorItConstants;

import org.apache.log4j.Logger;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.Repository;

import java.util.Arrays;


/**
 * Sample output: "Selected item is disabled via the \"Ingnored On Sourcepath\" list...".
 *
 * Could be (but I'm not sure) that when a file is checked out via CVSFileSystem
 * (or smth) then the user still uses the file under RefactorIT via RefactorIT's own
 * LocalSourcePath. This combination is not supported here (very likely a default
 * explanation will be returned).
 *
 * @author  Risto
 */
public class FileNotFoundReason {
  private static final Logger log = Logger.getLogger(FileNotFoundReason.class);

  private static final String PROJECT_OPTIONS_COMMAND =
    "RefactorIT -> Help And Settings -> Project Options";

  public static final String ELEMENT_IGNORED =
      "Ignored per configuration (see the \"Ignored On Sourcepath\" list under " +
      PROJECT_OPTIONS_COMMAND + ").";
  public static final String WRONG_FS_CAPABILITIES =
      "Wrong filesystem capabilities " +
      "(required: 'Use in Compiler'='true', 'Read-only'='false'). " +
      "You can change them in the 'Capabilities' tab of filesystem properties. " +
      "Alternatively, you could change RefactorIT sourcepath under " + PROJECT_OPTIONS_COMMAND + ".";
  public static final String SYSTEM_FILESYSTEM =
      "System filesystem is not refactorable";
  public static final String ELEMENT_NOT_ON_MANUAL_PATH =
      "Not on RefactorIT sourcepath (see " + PROJECT_OPTIONS_COMMAND + ").";
  public static final String ELEMENT_NOT_ON_AUTODETECTED_PATH =
      "Not on sourcepath (see " + PROJECT_OPTIONS_COMMAND + ").";
  public static final String EMPTY_PACKAGE =
      "Empty package";
  public static final String NOT_JAVA_FILE =
      "Not a Java file";

  public static final String DEFAULT_REASON_PACKAGE_OR_EMPTY_PACKAGE =
      "Cannot access: it's either not on RefactorIT sourcepath or an empty package. " +
      "(Sourcepath can be configured under " + PROJECT_OPTIONS_COMMAND + ".)";
  public static final String DEFAULT_REASON_FILE =
      "Cannot access: it's is not on RefactorIT sourcepath. " +
      "(Sourcepath can be configured under " + PROJECT_OPTIONS_COMMAND + ".)";

  public static final String EMPTY_JAVA_FILE = "Empty file";

  public static final String PARSE_ERROR = "Error compiling the source";
  public static final String CURSOR_MISPLACED_IN_SOURCE = "Cursor misplaced for this action";

  public static String getFor(ElementInfo[] elements) {
    return getFirstNonDefaultReason(elements);
  }

  private static String getFirstNonDefaultReason(ElementInfo[] elements) {
    String result = getFor(elements[0].getFileObject());

    for (int i = 1; i < elements.length; i++) {
      String explanation = getFor(elements[i].getFileObject());
      if (defaultReason(result) && (!defaultReason(explanation))) {
        return explanation;
      }
    }

    return result;
  }

  private static boolean defaultReason(String reason) {
    return (reason == DEFAULT_REASON_FILE || reason == DEFAULT_REASON_PACKAGE_OR_EMPTY_PACKAGE);
  }

  public static String getFor(FileObject fileObject) {
    try {
      if(fileObject.getFileSystem().equals(Repository.getDefault().getDefaultFileSystem())) {
        return SYSTEM_FILESYSTEM;
      }

      Object ideProject = IDEController.getInstance().getActiveProjectFromIDE();
      Object projectKey = IDEController.getInstance().getWorkspaceManager()
      .getIdeProjectIdentifier(ideProject);

      if(RefactorItActions.isNetBeansFour()) {
        if (NBProjectOptions.getInstance(projectKey).getAutodetectPaths() &&
            (!onAutodetectedPath(fileObject))) {
          return ELEMENT_NOT_ON_AUTODETECTED_PATH;
        }
      } else {
        if (NBProjectOptions.getInstance(projectKey).getAutodetectPaths() &&
            (!isFilesystemAutodetected(fileObject))) {
          return WRONG_FS_CAPABILITIES;
        }
      }

      if ((!NBProjectOptions.getInstance(projectKey).getAutodetectPaths()) &&
          (!onSpecifiedPath(fileObject))
          ) {
        return ELEMENT_NOT_ON_MANUAL_PATH;
      }

      if (inIgnoreListAssumingItIsOnPath(fileObject)) {
        return ELEMENT_IGNORED;
      }
    } catch (FileStateInvalidException e) {
      log.warn("REFACTORIT EXCEPTION -- PLEASE REPORT", e);
    }

    if (fileObject.isFolder()) {
      if(childrenAreOnlyFoldersRecursive(fileObject)) {
        return EMPTY_PACKAGE;
      } else {
        return DEFAULT_REASON_PACKAGE_OR_EMPTY_PACKAGE;
      }
    } else {
      if( ! supportedFileExtension(fileObject.getExt())) {
        return NOT_JAVA_FILE;
      } else if( fileObject.getSize() == 0) {
        return EMPTY_JAVA_FILE;
      }
    }

    return DEFAULT_REASON_FILE;
  }

  private static boolean supportedFileExtension(String ext) {
    return "java".equals(ext);
  }

  private static boolean childrenAreOnlyFoldersRecursive(FileObject fileObject) {
    FileObject[] children = fileObject.getChildren();

    for (int i = 0; i < children.length; i++) {
      FileObject child = children[i];
      if(child.isData()) {
        return false;
      } else {
        if( ! childrenAreOnlyFoldersRecursive(child)) {
          return false;
        }
      }
    }

    return true;
  }

  private static boolean inIgnoreListAssumingItIsOnPath(FileObject fileObject) throws
      FileStateInvalidException {
    Object ideProject = IDEController.getInstance().getActiveProjectFromIDE();
    Object projectKey = IDEController.getInstance().getWorkspaceManager()
        .getIdeProjectIdentifier(ideProject);
    if (NBProjectOptions.getInstance(ideProject).getAutodetectPaths()) {
      return fileObjectInIgnoredList(fileObject);
    } else {
      return
          (fileInIgnoredList(fileObject) || (!fileOnPath(fileObject, NBProjectOptions.getInstance(projectKey).getUserSpecifiedSourcePath(true)))) &&
          (fileObjectInIgnoredList(fileObject) || (!fileObjectOnPath(fileObject, NBProjectOptions.getInstance(projectKey).getUserSpecifiedSourcePath(true))));
    }
  }


  private static boolean onAutodetectedPath(FileObject fileObject) {
    Object ideProject = IDEController.getInstance().getActiveProjectFromIDE();
    return fileObjectOnPath(fileObject, PathUtil.getInstance()
        .getAutodetectedSourcepath(ideProject, true));
  }

  private static boolean onSpecifiedPath(FileObject fileObject) throws
      FileStateInvalidException {
    Object ideProject = IDEController.getInstance().getActiveProjectFromIDE();
    return onPath(fileObject, NBProjectOptions.getInstance(ideProject).getUserSpecifiedSourcePath(true));
  }

  private static boolean onPath(FileObject fileObject, PathItemReference[] path) throws FileStateInvalidException {
    return fileOnPath(fileObject, path) || fileObjectOnPath(fileObject, path);
  }

  private static boolean fileOnPath(FileObject fileObject, PathItemReference[] path) throws
      FileStateInvalidException {
    return PathItemReference.inListOrParentInList(
        FileObjectUtil.getFileOrNull(fileObject), path);
  }

  private static boolean fileObjectOnPath(FileObject fileObject, PathItemReference[] path) {
    return PathItemReference.inListOrParentInList(fileObject, path);
  }

  private static boolean fileInIgnoredList(FileObject fileObject) throws
      FileStateInvalidException {
    Object ideProject = IDEController.getInstance().getActiveProjectFromIDE();
    return PathItemReference.inListOrParentInList(
        FileObjectUtil.getFileOrNull(fileObject),
        PathUtil.getInstance().getIgnoredSourceDirectories(ideProject));
  }

  private static boolean fileObjectInIgnoredList(FileObject fileObject) {
    Object ideProject = IDEController.getInstance().getActiveProjectFromIDE();
    return PathItemReference.inListOrParentInList(
        fileObject,
        PathUtil.getInstance().getIgnoredSourceDirectories(ideProject));
  }

  private static boolean isFilesystemAutodetected(FileObject fileObject) throws
      org.openide.filesystems.FileStateInvalidException {
    return PathUtil.getInstance().isRefactorable(fileObject.getFileSystem());
  }

  public static void showMessageDialogOnWhyBinItemNotFound(
      IdeWindowContext context, ElementInfo[] elements, boolean inSourceEditor
  ) {
    Object ideProject = IDEController.getInstance().getActiveProjectFromIDE();
    if (inSourceEditor &&
        PathUtil.getInstance().sourceOnSourcepath(ideProject, elements[0].getFileObject())) {

      // Sometimes getting these messages under S1S4.1
      // while all seems to be ok with parsing
      if (RefactorItConstants.debugInfo) {
        final String message = "DebugInfo: RefactorIT click debug - elements:"
            + Arrays.asList(elements);
        log.debug(message, new Throwable());
      }

      // If the cursor was placed right but there was a source code or sourcepath problem,
      // on some type that is refferred to in the sourcepath then we would
      // have a critical parsing error and we wouln't have gotten this far. So if we get here, the only
      // reason could be that the cursor is not placed on something meaningful.
      // (For instance, after semicolon after an import statement -- nothing there).
      DialogManager.getInstance().showWarning(context,
          "warning.action.unit.error", CURSOR_MISPLACED_IN_SOURCE);
      return;
    }

    DialogManager.getInstance().showWarning(context,
        "warning.action.unit.error", FileNotFoundReason.getFor(elements));
    return;
  }
}
