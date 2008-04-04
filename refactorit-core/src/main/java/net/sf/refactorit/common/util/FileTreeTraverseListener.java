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
public interface FileTreeTraverseListener {
  // Return code to notify to skip the processing the current File.
  int STOP_CURRENT_FILE_PROCESSING = -1;
  int STOP_PROCESSING = -2;
  int CONTINUE_PROCESSING = 0;

  /**
   * This method is called for every file found in directory.
   * It is called by FileUtil.traverse(..) algorithms.
   */
  int foundFile(File file);

  int enterDirectory(File directory);

  int exitDirectory(File file);

  /**
   * It is called after every call to enterDirectory() by FileUtil.traverse()
   * algorithm. So inside enterDirectory() method it can be changed.
   */
  FileFilter getFileFilter();
}
