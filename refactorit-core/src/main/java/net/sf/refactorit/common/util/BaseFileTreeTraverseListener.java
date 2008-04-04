/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.common.util;

// java classes
import java.io.File;
import java.io.FileFilter;


/**
 * Used to search for specific file or collect information for files.
 * Used in collaboration with FileUtil.traverse(FileTreeTraverseListener) method.
 *
 * @author "Jaanek Oja" <jaanek@refactorit.com>
 */
public class BaseFileTreeTraverseListener implements FileTreeTraverseListener {

  /**
   * This method is called for every file found in directory.
   * It is called by FileUtil.traverse(..) algorithms.
   */
  public int foundFile(File file) {
    return CONTINUE_PROCESSING;
  }

  public int enterDirectory(File file) {
    return CONTINUE_PROCESSING;
  }

  public final int exitDirectory(File file) {
    return CONTINUE_PROCESSING;
  }

  /**
   * It is called after every call to enterDirectory() by FileUtil.traverse()
   * algorithm. So inside enterDirectory() method it can be changed.
   */
  public final FileFilter getFileFilter() {
    return null;
  }
}
