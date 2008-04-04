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
 * Representation of class path element.
 *
 * @author  Igor Malinin
 */
public interface ClassPathElement {
  /**
   * Returns classpath entry for a given full qualified class name.
   * Returns null if class does not exists in this element.
   *
   * @param cls  class name in the form com/package/Class$Inner.class
   * @return  classpath entry
   */
  ClassPath.Entry getEntry(String cls);

  public String getAbsolutePath();

  /**
   * Releases any used resources and unlocks any files locked.
   **/
  void release();

  boolean existsEntry(String cls);
}
