/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.undo;

import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.ui.RuntimePlatform;
import net.sf.refactorit.utils.FileUtil;
import net.sf.refactorit.utils.PathElement;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourcePath;
import net.sf.refactorit.vfs.SourceUtil;




/**
 *
 *
 * @author Tonis Vaga
 */
public class UndoUtil {
  static Source getParentSourceForFile(SourcePath sourcePath, String filePath) {
    Source result = null;

    Source parent = findMachingRootPath(sourcePath, filePath);
    // if no such root exist to restore for than

    if (parent == null) {
      RuntimePlatform.console.println(
          "RefactorIT error: matching sourcepath root not found for "
          + filePath);

      return null;
    }

    String relativeFilePath = filePath
        .substring(parent.getAbsolutePath().length() + 1);

    char separatorChar = parent.getSeparatorChar();

    String dirPath = FileUtil
        .extractPathUpToLastSeparator(relativeFilePath, separatorChar);

    result = parent.getChild(dirPath);

    if (result != null) {
      return result;
    }

    AppRegistry.getLogger(UndoUtil.class).debug("[tonisdebug]: parent not found:" + relativeFilePath
    + "parent=" + parent.getAbsolutePath());

    if (separatorChar != Source.RIT_SEPARATOR_CHAR) {
      // mkdirs wants '/' as separator char
      dirPath = dirPath.replace(separatorChar, Source.RIT_SEPARATOR_CHAR);
    }

    return parent.mkdirs(dirPath);
    //    return createDirSource(parent,dirPath);
  }

  static Source findSource(SourcePath sourcePath, String filePath) {
    Source dest = null;
    Source parent = UndoUtil.getParentSourceForFile(sourcePath, filePath);
    if (parent != null) {
      String fileName = FileUtil.extractFileNameFromPath(filePath,
          parent.getSeparatorChar());
      dest = parent.getChild(fileName);
    } else {
      AppRegistry.getLogger(UndoUtil.class).debug("parent not found for " + filePath);
    }

    return dest;
  }

//  /**
//   * @pre  roots[i].findChild(dirPath)==null for every i
//   */
//  private static Source createDirSource(Source[] roots, String dirPathParam) {
//    if ( roots.length == 1 ) {
//      return roots[0].mkdirs(dirPathParam);
//    }
//
//    // start looking for best matching path from tail
//
//    String dirPath;
//    while( (dirPath=FileUtil.extractPathUpToLastSeparator(dirPathParam, File.separatorChar))!=null ) {
//      if (dirPath == null) {
//        return roots[0];
//      }
//    }
//    throw new RuntimeException("not implemented");
//  }

  /**
   * @param sourcePath source path
   * @param filePath file path
   * @return matching root path for file if exist, null otherwise
   */
  static Source findMachingRootPath(SourcePath sourcePath, String filePath) {
    Source[] roots = sourcePath.getRootSources();
    for (int i = 0; i < roots.length; i++) {
//      char separator = roots[i].getSeparatorChar();
//      String dirPath = FileUtil.extractPathUpToLastSeparator(filePath, separator);

      if (filePath.indexOf(roots[i].getAbsolutePath()) == 0) {
        return roots[i];
      }
    }

    return null;
  }
  public static Source findSource(SourcePath sourcePath, SourceInfo srcInf) {
    Source root = SourceUtil.findRootSource(sourcePath.getRootSources(), srcInf.getRootPath());
    if (root == null) {
      return null;
    }

    // Eclipse fall down when  getFile() get emty path as argument like: "";
    if(srcInf.getRelativePath().equals("")) {
      return root;
    }
    return root.getChild(srcInf.getRelativePath());
  }

  public static Source findParent(SourcePath sourcePath, SourceInfo info,
      boolean directory) {
    String relativePath;
    if (!directory) {
      PathElement element = FileUtil
          .extractPathElement(info.getRelativePath(), info.getSeparatorChar());

      if (Assert.enabled) {
        Assert.must(element.file.length() != 0, "wrong argument");
      }
      relativePath = element.dir;
    } else {
      relativePath = info.getRelativePath();

      // remove last separator if needed
      if (relativePath.charAt(relativePath.length() - 1) == info.getSeparatorChar()) {
        relativePath = relativePath.substring(0, relativePath.length() - 1);
      }

      relativePath = FileUtil
          .extractPathUpToLastSeparator(relativePath, info.getSeparatorChar());
    }

    return findSource(sourcePath,
        new SourceInfo(info.getRootPath(), relativePath, info.getSeparatorChar()));
  }
}
