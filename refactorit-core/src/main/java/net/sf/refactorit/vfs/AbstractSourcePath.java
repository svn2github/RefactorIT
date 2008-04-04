/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.vfs;


import net.sf.refactorit.common.util.WildcardPattern;
import net.sf.refactorit.vfs.local.LocalFileChangeMonitor;

import java.io.File;
import java.util.Collection;


public abstract class AbstractSourcePath implements SourcePath {

  public Source[] getPossibleRootSources() {
    return Source.NO_SOURCES;
  }
  private String[] extensions = new String[] {".jsp"};

  public void setValidExtensions(String[] extensions) {
    this.extensions = extensions;
  }

  public Source[] getAutodetectedElements() {
    return null;
  }

  public final String[] getValidExtensions() {
    return extensions;
  }

  /**
   * Checked via file extension, <code>.java</code> is ok, and then those in
   * <code>extensions</code> array are also ok.
   */
  public final boolean isValidSource(String sourceName) {
    String name = sourceName.toLowerCase();

    // accept .java files by default
    // as there is many java sources, it give quick return.
    if (name.endsWith(".java")) {
      return true;
    }

    // check additional extensions
    if (extensions != null) {
      for (int i = 0; i < extensions.length; i++) {
        if (name.endsWith(extensions[i])) {
          return true;
        }
      }
    }

    return false;
  }

  public final String toString() {
    Source[] dirs = getRootSources();
    StringBuffer buf = new StringBuffer(120);
    for (int i = 0; i < dirs.length; i++) {
      if (i > 0) {
        buf.append(File.pathSeparatorChar);
      }
      buf.append(dirs[i].getDisplayPath());
    }
    return buf.toString();
  }

  public static boolean fileAcceptedByPattern(
      String file, WildcardPattern[] patterns
      ) {
    if (patterns == null) {
      return false;
    }

    for (int i = 0; i < patterns.length; i++) {
      if (patterns[i].matches(file)) {
        return true;
      }
    }

    return false;
  }

  /**
   * In AbstractSourcePath defined as a method stub always returning false;
   */
  public boolean isIgnoredPath(String absolutePath){
    return false;
  }

  /**
   *  Iterates direcotry and adds all sources to result matching filter
   * @param parent
   * @param result
   * @param filter
   */
  protected final void iterateDirectory(Source parent, Collection result, Source.SourceFilter filter) {
    if (parent == null) {
      return;
    }
    if (parent.isFile()) {
      return;
    }
    Source[] list = parent.getChildren();

    // fix for bug #75 from public bugzilla
    if (list == null) {
      return;
    }
    for (int i = 0; i < list.length; ++i) {
      Source curSource = list[i];

      if (filter != null && !filter.accept(curSource)) {
        continue;
      }

      if (curSource.isFile()) {
        if ( isValidSource(curSource.getName()) ) {
          result.add(curSource);
               }
      } else {
          iterateDirectory(curSource, result, filter);
      }
    }

  }

  protected FileChangeMonitor fileChangeMonitor;

  public FileChangeMonitor getFileChangeMonitor() {
    if (fileChangeMonitor == null) {
      fileChangeMonitor = new LocalFileChangeMonitor(getAllSources()) {
        protected void collectSources(Collection result) {
          result.addAll(getAllSources());
        }
      };
    }

    return fileChangeMonitor;
  }

}
