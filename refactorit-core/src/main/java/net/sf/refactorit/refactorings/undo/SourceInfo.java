/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.undo;


import net.sf.refactorit.common.exception.SystemException;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.exception.ErrorCodes;
import net.sf.refactorit.utils.FileUtil;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourcePath;
import net.sf.refactorit.vfs.SourceUtil;

import java.io.Serializable;


public class SourceInfo implements Serializable {

  /** absolute path to corresponding root source */
  private String rootPath;

  /** path from rootSource to this source */
  private String relativePath;
  private char separatorChar;
  private boolean directory = false;

  public SourceInfo(final Source source) {
    String[] paths = extractPathsFrom(source);
    this.rootPath = paths[0];
    this.separatorChar = source.getSeparatorChar();
    this.relativePath = paths[1];
    this.directory = source.isDirectory();
  }

  public SourceInfo(String rootPath, String relativePath, char separatorChar) {
    this.rootPath = rootPath;
    this.separatorChar = separatorChar;
    this.relativePath = relativePath;
  }

  public SourceInfo(final Source dir, String newName) {
    String[] paths = extractPathsFrom(dir);
    rootPath = paths[0];
    separatorChar = dir.getSeparatorChar();
    relativePath = paths[1] + dir.getSeparatorChar() + newName;
  }

  public SourceInfo(String rootPath, String relativePath, char separatorChar,
      boolean directory) {
    this(rootPath, relativePath, separatorChar);
    this.directory = directory;
  }

  public SourceInfo(final Source dir, String newName, boolean directory) {
    this(dir, newName);
    this.directory = directory;
  }

  /**
   * @param src
   * @return result array where result[0] is source root path and result[1] is src relative path
   */
  private static String[] extractPathsFrom(Source src) {
    SourcePath sourcePath = IDEController.getInstance().getActiveProject().getPaths().getSourcePath();

    // FIXME: here every source backup means callings sourcepath
    // getRootSources which can cause performance problems
    Source rootSrc = SourceUtil.findRootSourceFor(
        sourcePath.getRootSources(), src);

    if (rootSrc == null) {
      throw new SystemException(ErrorCodes.INTERNAL_ERROR,
          "Couldn't find root source for " + src);
    }

    String result[] = {
      rootSrc.getAbsolutePath(),
      FileUtil.getRelativePathFrom(rootSrc, src),
    };

    return result;
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof SourceInfo)) {
      return false;
    }

    SourceInfo info = (SourceInfo) obj;

    return relativePath.equals(info.relativePath)
        && rootPath.equals(info.rootPath);
  }

  public String toString() {
    return rootPath + separatorChar + relativePath;
  }

  public boolean isDirectory() {
      return this.directory;
  }

  public String getRelativePath() {
    return this.relativePath;
  }

  public String getRootPath() {
    return this.rootPath;
  }

  public char getSeparatorChar() {
    return this.separatorChar;
  }
}
