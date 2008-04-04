/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.loader;


import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;

import java.util.HashMap;
import java.util.List;


public final class FastCompilationUnitForNameFinder
    implements ProjectChangedListener {
  private final Project project;
  private HashMap cache = null;
  private boolean rebuildInProgress = false;

  public FastCompilationUnitForNameFinder(Project project) {
    this.project = project;
    project.getProjectLoader().addProjectChangedListener(this);
  }

  public CompilationUnit getCompilationUnitForName(final String relativePath) {
    if (this.rebuildInProgress) {
      return findLiveResult(relativePath);
    }

    ensureCache();

    return (CompilationUnit) this.cache.get(relativePath);
  }

  private CompilationUnit findLiveResult(final String relativePath) {
System.err.println("relativePath: " + relativePath);
    final List allCompilationUnits = this.project.getCompilationUnits();
    for (int i = 0, max = allCompilationUnits.size(); i < max; i++) {
      final CompilationUnit compilationUnit
          = (CompilationUnit) allCompilationUnits.get(i);
      if (compilationUnit.getSource().getRelativePath().equals(relativePath)) {
        return compilationUnit;
      }
    }
    return null;
  }

  private void ensureCache() {
    if (cache == null) {
      cache = new HashMap();
      final List allCompilationUnits = this.project.getCompilationUnits();
      for (int i = 0, max = allCompilationUnits.size(); i < max; i++) {
        final CompilationUnit compilationUnit
            = (CompilationUnit) allCompilationUnits.get(i);
        this.cache.put(
            compilationUnit.getSource().getRelativePath(), compilationUnit);
      }
    }
  }

  public void rebuildStarted(final Project project) {
    if (this.project == project) {
      rebuildInProgress = true;
    }
  }

  public void rebuildPerformed(final Project project) {
    if (this.project == project) {
      rebuildInProgress = false;
      cache = null;
    }
  }
}
