/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.vcs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import net.sf.refactorit.common.util.ReflectionUtil;
import net.sf.refactorit.netbeans.common.RefactorItActions;

import org.apache.log4j.Logger;
import org.netbeans.modules.vcscore.VcsFileSystem;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;


/**
 * @author risto
 */
public class FileSystemProperties {
  private static final Logger log = Logger.getLogger(FileSystemProperties.class);

  public static final boolean isVcsFileSystem(final FileSystem fileSystem) {
    if(fileSystem instanceof VcsFileSystem) {
      return true;
    }

    if(isDelegatingFilesystem(fileSystem)) {
      List delegateFilesystems = getDelegateFilesystems(fileSystem);

      // FIXME: We expect delegateFilesystems.size() to be either 0 or 1. Could it be more? If so, how to handle that?
      if(delegateFilesystems.size() > 0) {
        return isVcsFileSystem((FileSystem)delegateFilesystems.get(0));
      }
    }

    return false;
  }

  private static List getDelegateFilesystems(final FileSystem fileSystem) {
    List delegateFilesystems = new ArrayList();
    Enumeration allFs = Repository.getDefault().getFileSystems();
    while(allFs.hasMoreElements()) {
      FileSystem filesystemInRepository = (FileSystem) allFs.nextElement();

      if(delegatesTo(filesystemInRepository, fileSystem)) {
        delegateFilesystems.add(filesystemInRepository);
      }
    }

    return delegateFilesystems;
  }

  private static boolean delegatesTo(final FileSystem filesystem, final FileSystem delegateFilesystem) {
    if((filesystem.equals(delegateFilesystem))) {
      return false;
    }

    Boolean result = (Boolean) ReflectionUtil.invokeMethod(
        delegateFilesystem, "correspondsTo", FileSystem.class, filesystem);
    return result.booleanValue();
  }

  private static boolean isDelegatingFilesystem(final FileSystem fileSystem) {
    return ReflectionUtil.hasMethod(fileSystem, "correspondsTo", FileSystem.class);
  }

  public static final boolean isVssFileSystem(final FileSystem fileSystem) {
    return getCommandNames(fileSystem).contains("CREATE");
  }

  /**
   * @return never returns null
   */
  static List getCommandNames(final FileSystem fileSystem) {
    List result = null;
    if (isVcsFileSystem(fileSystem)) {
      try {
        Object[] names = (Object[]) ReflectionUtil.invokeMethod(
            fileSystem, "getCommandNames");
        result = new ArrayList(Arrays.asList(names));
      } catch (Exception e) {
        log.warn(e.getMessage(), e);
        result = new ArrayList(30);
      }

      if (result != null && result.contains("CREATE")) {
        // our own VSS commands
        result.add("RENAME");
        result.add("SHARE");
      }
    }

    if (result == null) {
      result = Collections.EMPTY_LIST;
    }

    return result;
  }
  
  public static boolean isVssFileSystem(FileObject fileObject) {
    return isVssFileSystem(VcsRunner.getFileSystem(fileObject));
  }
  
  static boolean inVcs(final FileObject fileObject, boolean shouldRunEvenWhenVcsIsDisabled) {
    if (!Options.vcsEnabled() && (!shouldRunEvenWhenVcsIsDisabled)) {
      return false;
    }

    if (VcsRunner.getFileSystem(fileObject) == null) {
      ErrorDialog.error("REFACTORIT: FileSystem is null for " + fileObject);
      return false;
    }

    if (!isVcsFileSystem(VcsRunner.getFileSystem(fileObject))) {
      return false;
    }
    
    return true;
  }
}
