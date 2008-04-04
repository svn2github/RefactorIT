/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.util;

// netbeans classes
import org.openide.filesystems.FileObject;


/**
 * Used to search for specific file or collect information for files.
 * Used in collaboration with PathUtil.traverse(FileObjectTraverseListener) method.
 *
 * @author "Jaanek Oja" <jaanek@refactorit.com>
 */
public class BaseFileObjectTraverseListener implements
    FileObjectTraverseListener {

  /**
   * This method is called for every file found in directory.
   * It is called by PathUtil.traverse(..) algorithms.
   */
  public int foundFile(FileObject file) {
    return CONTINUE_PROCESSING;
  }

  public int enterDirectory(FileObject file) {
    return CONTINUE_PROCESSING;
  }

  public int exitDirectory(FileObject file) {
    return CONTINUE_PROCESSING;
  }
}
