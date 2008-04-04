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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author Anton Safonov
 */
public class CompoundSourcePath implements SourcePath {
  private SourcePath[] paths;

  public CompoundSourcePath(SourcePath[] paths) {
    this.paths = paths;
  }

  public SourcePath[] getPaths() {
    return this.paths;
  }

  public List getAllSources() {
    ArrayList result = new ArrayList();
    for (int i = 0, max = paths.length; i < max; i++) {
      result.addAll(paths[i].getAllSources());
    }
    return result;
  }

  public Source[] getAutodetectedElements() {
    ArrayList result = new ArrayList();
    for (int i = 0, max = paths.length; i < max; i++) {
      result.addAll(Arrays.asList(paths[i].getAutodetectedElements()));
    }
    return (Source[]) result.toArray(new Source[result.size()]);
  }

  public FileChangeMonitor getFileChangeMonitor() {
    return paths[0].getFileChangeMonitor();
  }

  public List getNonJavaSources(WildcardPattern[] patterns) {
    ArrayList result = new ArrayList();
    for (int i = 0, max = paths.length; i < max; i++) {
      result.addAll(paths[i].getNonJavaSources(patterns));
    }
    return result;
  }

  public Source[] getPossibleRootSources() {
    ArrayList result = new ArrayList();
    for (int i = 0, max = paths.length; i < max; i++) {
      result.addAll(Arrays.asList(paths[i].getPossibleRootSources()));
    }
    return (Source[]) result.toArray(new Source[result.size()]);
  }

  public Source[] getRootSources() {
    ArrayList result = new ArrayList();

    for (int i = 0, max = paths.length; i < max; i++) {
      result.addAll(Arrays.asList(paths[i].getRootSources()));
    }
    return (Source[]) result.toArray(new Source[result.size()]);
  }

  public List getIgnoredSources() {
    List result = new ArrayList();

    for (int i = 0, max = paths.length; i < max; i++) {
      result.addAll(paths[i].getIgnoredSources());
    }
    return result;
  }

  public boolean isIgnoredPath(String absolutePath) {
    for (int i = 0, max = paths.length; i < max; i++) {
      if (paths[i].isIgnoredPath(absolutePath)) {
        return true;
      }
    }
    return false;
  }
}
