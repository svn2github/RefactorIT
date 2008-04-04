/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.vfs;

import org.openide.filesystems.FileObject;

import java.io.File;
import java.io.IOException;

/**
 * @author Juri Reinsalu
 */

public interface NBSourceVersionState {

  String getAbsolutePath(NBSource source);

  String getRelativePath(NBSource nbSource);

  FileObject renameInNbFilesystems(NBSource source, NBSource destinationDir,
          String name, String ext) throws IOException;

  FileObject getFileObjectForPath(String localPath);

  /**
   * @param localFile
   * @return
   */
  FileObject getFileObjectForFile(File localFile);

}
