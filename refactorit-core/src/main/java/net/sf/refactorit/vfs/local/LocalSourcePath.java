/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.vfs.local;


import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.WildcardPattern;
import net.sf.refactorit.commonIDE.options.Path;
import net.sf.refactorit.commonIDE.options.PathItem;
import net.sf.refactorit.vfs.AbstractSourcePath;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourcePath;
import net.sf.refactorit.vfs.SourcePathFilter;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Local filesystem SourcePath.
 *
 * @author Igor Malinin
 * @author Anton Safonov
 */
public final class LocalSourcePath extends AbstractSourcePath {
  private static final Logger log = AppRegistry.getLogger(LocalSourcePath.class);

  private Path path;

  private Path ignoredPath;

  /** For tests */
  private Source copyPath = null;

  /**
   * Creates new LocalSourcePath for unit tests
   */
  public static SourcePath createTestLocalSource(String sourcePath,
      String copyPath, String ignoredPath) throws IOException {

    LocalSourcePath result = new LocalSourcePath(sourcePath);
    if (copyPath != null) {
      result.copyPath = LocalSource.getSource(new File(copyPath));
    }
    if (ignoredPath != null && ignoredPath.trim().length() > 0) {
      result.setIgnoredPath(new Path(ignoredPath));
    }
    return result;
  }

  /**
   * Creates new LocalSourcePath
   */
  public LocalSourcePath(String path) {
    this(new Path(path), null);
  }

  /**
   * Creates new LocalSourcePath
   */
  public LocalSourcePath(Path path, Path ignoredPath) {
    this.path = path;
    this.ignoredPath = ignoredPath;
  }

  public final Source[] getPossibleRootSources() {
    return getRootSources();
  }

  public final List getAllSources() {
    SourceFilter filter = new IgnoredSourceFilter() {
      public boolean accept(String fileName, boolean dir) {
        boolean result = super.accept(fileName, dir);
        if (!dir) {
          result &= isValidSource(fileName);
        }

        return result;
      }
    };
    return getAllSources(filter);
  }

  public final List getNonJavaSources(final WildcardPattern[] patterns) {
    SourceFilter filter = new IgnoredSourceFilter() {
      public boolean accept(String fileName, boolean dir) {
        if (dir) {
          return super.accept(fileName, dir);
        }

        for (int i = 0, max = patterns.length; i < max; i++) {
          if (patterns[i].matches(fileName)) {
            return true;
          }
        }

        return false;
      }
    };
    return getAllSources(filter);
  }

  public final List getAllSources(SourceFilter filter) {
    Source roots[] = getRootSources();

    ArrayList result = new ArrayList(200);

    for (int i = 0, max = roots.length; i < max; ++i) {
      final Source root = roots[i];
      if (root instanceof LocalSource) {
        ((LocalSource) root).collectSources(result, filter);
      } else if (root instanceof ZipSource) {
        ((ZipSource) root).collectSources(result, filter);
      } else {
        AppRegistry.getLogger(this.getClass()).error(
            "Unhandled root source: " + root);
      }
    }

    final Set uniqueSourcesSet = new HashSet(result);
    final List uniqueSources = new ArrayList(uniqueSourcesSet);

    return uniqueSources;
  }

  public final Source[] getRootSources() {
    PathItem[] items = path.getItems();

    ArrayList result = new ArrayList(items.length);

    for (int i = 0, max = items.length; i < max; i++) {
      File file = new File(items[i].getAbsolutePath());

      if (!file.exists()) {
        log.error("source root " + file.getAbsolutePath() + " doesn't exist");
        continue;
      }

      if (file.getName().endsWith(".zip") || file.getName().endsWith(".jar")) {
        result.add(ZipSource.getSource(file));
      } else {
        result.add(LocalSource.getSource(file));
      }
    }

    return (Source[]) result.toArray(new Source[result.size()]);
  }

  public List getIgnoredSources() {
    if (ignoredPath == null) {
      return Collections.EMPTY_LIST;
    }
    PathItem[] items = ignoredPath.getItems();
    List result = new ArrayList();

    for (int i = 0; i < items.length; i++) {
      result.add(items[i].getAbsolutePath());
    }
    return result;
  }

  public final Source getCopySource() {
    return this.copyPath;
  }

  public final void setIgnoredPath(Path ignoredPath) {
    this.ignoredPath = ignoredPath;
  }

  public static interface SourceFilter {
    boolean accept(String fileName, boolean dir);

    boolean acceptPath(String absolutePath);
  }

  public abstract class IgnoredSourceFilter implements SourceFilter {
    private final SourcePathFilter pathFilter = new SourcePathFilter();

    public boolean accept(String fileName, boolean dir) {
      return !dir || pathFilter.acceptDirectoryByName(fileName);
    }

    public boolean acceptPath(String absolutePath) {
      if (ignoredPath == null) {
        return true;
      }

      PathItem[] items = ignoredPath.getItems();
      for (int i = 0, max = items.length; i < max; i++) {
        if ((absolutePath + File.separator).startsWith(
            items[i].getAbsolutePath() + File.separator)) {
          return false;
        }
      }

      return true;
    }
  }
}
