/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test;

import net.sf.refactorit.parser.FastJavaLexer;

import java.util.LinkedList;
import java.util.List;


/**
 * Project metadata.
 */
public class ProjectMetadata {

  /** Project ID. */
  private final String id;

  /** Project name. */
  private final String name;

  /** Copy path. Where the files copy from.*/
  private final String copyPath;

  /** Source paths. (<code>String</code> instances)*/
  private final List sourcePaths;

  /** Class paths. (<code>String</code> instances)*/
  private final List classPaths;

  /** Ignored paths. (<code>String</code> instances)*/
  private final List ignoredPaths;

  /** If project should be tested for errorless loading automatically */
  private final boolean testForLoad;

  /** Java version. */
  private final int jvmMode;

  /** Constructs new metadata. */
  public ProjectMetadata(String id,
      String name,
      String copyPath,
      List sourcePaths,
      List classPaths,
      List ignoredPaths,
      boolean testForLoad) {
    this(id, name, copyPath, sourcePaths, classPaths, ignoredPaths, testForLoad,
        FastJavaLexer.JVM_14);
  }

  /** Constructs new metadata. */
  public ProjectMetadata(String id,
      String name,
      String copyPath,
      List sourcePaths,
      List classPaths,
      List ignoredPaths,
      boolean testForLoad,
      int jvmMode) {
    this.id = id;
    this.name = name;
    this.copyPath = copyPath;
    this.sourcePaths = ((sourcePaths == null) ? new LinkedList() : sourcePaths);
    this.classPaths = ((classPaths == null) ? new LinkedList() : classPaths);
    this.ignoredPaths = ((ignoredPaths == null) ? new LinkedList() : ignoredPaths);
    this.testForLoad = testForLoad;
    this.jvmMode = jvmMode;
  }

  /**
   * Gets project ID.
   *
   * @return ID.
   */
  public String getId() {
    return id;
  }

  /**
   * Gets project name.
   *
   * @return name.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets copy path files to copy from.
   *
   * @return copy path.
   *         May return <code>null</code>.
   */
  public String getCopyPath() {
    return copyPath;
  }

  /**
   * Gets source paths.
   *
   * @return source paths (<code>String</code> instances).
   *         Never returns <code>null</code>.
   */
  public List getSourcePaths() {
    return sourcePaths;
  }

  /**
   * Gets class paths.
   *
   * @return class paths (<code>String</code> instances).
   *         Never returns <code>null</code>.
   */
  public List getClassPaths() {
    return classPaths;
  }

  /**
   * Gets ignored paths.
   *
   * @return ignored paths (<code>String</code> instances).
   *         Never returns <code>null</code>.
   */
  public List getIgnoredPaths() {
    return ignoredPaths;
  }

  /**
   * @return <code>true</code> if project should be tested for loading
   * automatically.
   */
  public boolean isTestForLoad() {
    return testForLoad;
  }

  public int getJvmMode() {
    return jvmMode;
  }

  public String toString() {
    return "\"" + getName() + "\" (id: " + getId() + ")";
  }
}
