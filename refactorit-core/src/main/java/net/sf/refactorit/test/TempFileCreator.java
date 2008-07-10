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
import net.sf.refactorit.common.util.WildcardPattern;
import net.sf.refactorit.vfs.AbstractSourcePath;
import net.sf.refactorit.vfs.FileChangeMonitor;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourcePath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public abstract class TempFileCreator {
  public static class TestSourcePath extends AbstractSourcePath {
    public List getNonJavaSources(WildcardPattern[] patterns) {
      List sources = getAllSources();
      List result = new ArrayList();

      for (int i = 0; i < sources.size(); ++i) {
        Source element = (Source) sources.get(i);

        for (int j = 0; j < patterns.length; j++) {
          if (patterns[j].matches(element.getName())) {
            result.add(element);
            break;
          }
        }
      }

      return result;
    }

    private final Source root;

    public TestSourcePath(Source root) {
      this.root = root;
    }

    public Source[] getRootSources() {
      return new Source[] {root};
    }

    public List getIgnoredSources() {
      return Collections.EMPTY_LIST;
    }

    public List getAllSources() {
      ArrayList result = new ArrayList();

      iterateDirectory(root, result, null);

      return result;
    }

    public FileChangeMonitor getFileChangeMonitor() {
      return null;
    }
  }

  private static TempFileCreator instance = new LocalTempFileCreator();

  public static final String tempDirPrefix = "RefactorIT";
  public static final String tempDirSuffix = "";

  public static final String tempFilePrefix = "mutable";
  public static final String tempFileSuffix = ".java";

  public static TempFileCreator getInstance() {
    return TempFileCreator.instance;
  }

  public static void setInstance(final TempFileCreator instance) {
    TempFileCreator.instance = instance;
  }

  public abstract Source createRootDirectory() throws SystemException;

  public abstract Source createRootFile() throws SystemException;

  public abstract SourcePath createSourcePath(Source root) throws SystemException;

  public static class TempNameGenerator {
    private static long count = System.currentTimeMillis();

    public static String createDir() {
      return tempDirPrefix + (++count) + tempDirSuffix;
    }

    public static String createFile() {
      return tempFilePrefix + (++count) + tempFileSuffix;
    }
  }
}
