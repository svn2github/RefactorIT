/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.vfs;

import net.sf.refactorit.ui.javadoc.TypeInfoJavadoc;

import java.io.PrintWriter;


/**
 * @author Anton Safonov
 */
public final class Paths {
  private SourcePath sourcePath;
  private ClassPath classPath;
  private JavadocPath javadocPath;

  /** either source or class path were changed */
  private boolean classPathChanged = true;
  private boolean sourcePathChanged = true;

  private String lastClasspathString;

  public Paths(final SourcePath sourcePath, final ClassPath classPath,
      final JavadocPath javadocPath) {
    this.sourcePath = sourcePath;
    this.classPath = classPath;
    this.javadocPath = javadocPath;
  }

  public SourcePath getSourcePath() {
    return sourcePath;
  }

  public void setSourcePath(SourcePath sourcePath) {
    // FIXME? may be it's better/safer invalidate cache here and call this clear in Project constructor
    // FIXME: it clears cache for all projects!!!!!!!!!!!!!!!!!!!
    SourceMap.clear();
    this.sourcePath = sourcePath;
    this.sourcePathChanged = true;
  }

  public ClassPath getClassPath() {
    return classPath;
  }

  public void setClassPath(ClassPath classPath) {
    this.classPath = classPath;
    this.classPathChanged = true;
  }

  public JavadocPath getJavadocPath() {
    return javadocPath;
  }

  public void setJavadocPath(JavadocPath javadocPath) {
    // FIXME: it will clear cache for all projects
    TypeInfoJavadoc.clearCache();

    this.javadocPath = javadocPath;
  }

  public boolean hasClassPathChanged() {
    return classPathChanged;
  }

  public void setClassPathChanged(boolean classPathHasChanged) {
    this.classPathChanged = classPathHasChanged;
  }

  public boolean hasSourcePathChanged() {
    return sourcePathChanged;
  }

  public void setSourcePathChanged(boolean sourcePathHasChanged) {
    this.sourcePathChanged = sourcePathHasChanged;
  }

  /**
   * Checks whether the classpath for this project has changed.
   *
   * It does this currently by comparing the classpath strings. The current
   * string for last string.
   */
  public void checkClasspathForChanges() {
    final String curClasspathString = getClassPath().getStringForm();

    if (!curClasspathString.equals(this.lastClasspathString)) {
      setClassPathChanged(true);
    }

    this.lastClasspathString = curClasspathString;

    // updates caches also as a side effect, so can't be skipped
    if (getClassPath().isAnythingChanged()) {
      setClassPathChanged(true);
    }
  }

  private void printSourcePath(PrintWriter debugOutputFile) {
    debugOutputFile.println("SourcePath:");

    final Source[] rootSources = getSourcePath().getRootSources();
    for (int i = 0; i < rootSources.length; i++) {
      debugOutputFile.println("\"" + rootSources[i].getAbsolutePath() + "\"");
    }

    debugOutputFile.println();
  }

  private void printClassPath(PrintWriter debugOutputFile) {
    debugOutputFile.println();
    debugOutputFile.println("ClassPath: ");
    debugOutputFile.println("\"" + getClassPath().getStringForm() + "\"");
    debugOutputFile.println();
  }

  public void printSourcePathAndClassPath(PrintWriter debugOutputFile) {
    debugOutputFile.println();
    printSourcePath(debugOutputFile);
    printClassPath(debugOutputFile);
    debugOutputFile.println();
  }
}
