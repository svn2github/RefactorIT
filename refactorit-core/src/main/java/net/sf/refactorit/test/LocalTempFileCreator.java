/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test;

import net.sf.refactorit.common.exception.SystemException;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.FileCopier;
import net.sf.refactorit.exception.ErrorCodes;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourcePath;
import net.sf.refactorit.vfs.local.LocalSource;
import net.sf.refactorit.vfs.local.LocalSourcePath;

import java.io.File;
import java.io.IOException;


public class LocalTempFileCreator extends TempFileCreator {
  public Source createRootFile() throws SystemException {
    try {
      return new LocalSource(
          File.createTempFile(tempFilePrefix, tempFileSuffix));
    } catch (IOException e) {
      AppRegistry.getExceptionLogger().error(e, this);

      throw new SystemException(ErrorCodes.INTERNAL_ERROR, e);
    }
  }

  public Source createRootDirectory() {
    // Create a temporary directory where sources will be copied to
    final File directory = createTempDirectory(tempDirPrefix, tempDirSuffix);

    return new LocalSource(directory);
  }

  public static File createTempDirectory(final String tempDirPrefix,
      final String tempDirSuffix) {
    final File directory;
    try {
      directory = File.createTempFile(tempDirPrefix, tempDirSuffix);
    } catch (IOException e) {
      throw new SystemException(ErrorCodes.INTERNAL_ERROR,e);
    }

    if (!FileCopier.delete(directory)) {
      directory.deleteOnExit();

      throw new SystemException(
          "Failed to delete temporary directory " + directory);
    }

    if (!directory.mkdir()) {
      throw new SystemException(ErrorCodes.INTERNAL_ERROR,
          "Failed to create temporary directory " + directory);
    }

    return directory;
  }

  public SourcePath createSourcePath(Source root) {
    return new LocalSourcePath(root.getAbsolutePath());
  }
}
