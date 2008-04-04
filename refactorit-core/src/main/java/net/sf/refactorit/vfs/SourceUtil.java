/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.vfs;




/**
 * SourceUtil
 *
 * @author <a href="mailto:tonis.vaga@aqris.com>Tõnis Vaga</a>
 * @version $Revision: 1.5 $ $Date: 2005/09/19 14:40:49 $
 */
public final class SourceUtil {
  /**
   * @param rootSources
   * @param source
   * @return project root source under this source is or null if not found
   */
  public static Source findRootSourceFor(Source[] rootSources, Source source) {
    char separator = Source.RIT_SEPARATOR_CHAR;
    String absolutePath = AbstractSource.normalize(source.getAbsolutePath());

    for (int i = 0; i < rootSources.length; i++) {

      String rootPath =
          AbstractSource.normalize(rootSources[i].getAbsolutePath());

      if (absolutePath.indexOf(rootPath) == 0
          && (absolutePath.length() == rootPath.length()
          || absolutePath.charAt(rootPath.length()) == separator)) {

        return rootSources[i];
      }

      // Hack.
      // Happens in Netbeans, when the rootPath may be deeper, than the actual sourcePath.
      if(rootPath.indexOf(absolutePath) == 0
          && (absolutePath.length() == rootPath.length()
          || rootPath.charAt(absolutePath.length()) == separator)) {

        return source;
      }
    }

    return null;
  }

  /**
   * @param rootSources
   * @param absolutePath
   * @return rootSource under which element with absolutePath is or null
   */
  public static Source findRootSource(Source[] rootSources, String absolutePath) {
    Source[] roots = rootSources;
    absolutePath = AbstractSource.normalize(absolutePath);
    char separator = Source.RIT_SEPARATOR_CHAR;

    for (int i = 0; i < roots.length; i++) {
      String rootSrc = AbstractSource.normalize(roots[i].getAbsolutePath());

      if ((absolutePath + separator).indexOf(rootSrc + separator) == 0) {
        return roots[i];
      }
    }

    return null;
  }

  /**
   * Finds corresponding child source from parents
   *
   * @param roots
   * @param absolutePath
   * @return null if not found
   */
  public static Source findSource(Source[] roots, String absolutePath) {
    Source root = findRootSource(roots, absolutePath);

    if (root == null) {
      return null;
    }

    if (AbstractSource.normalize(absolutePath).equals
        (AbstractSource.normalize(root.getAbsolutePath()))) {
      return root;
    }

    return findSource(root.getChildren(), absolutePath);
  }
}
