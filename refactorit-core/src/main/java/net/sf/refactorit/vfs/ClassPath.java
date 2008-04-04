/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.vfs;

import java.io.InputStream;


/**
 * Representation of class path. Provides binary stream for
 * a given full qualified class name.
 *
 * @author  Igor Malinin
 */
public interface ClassPath {

  /**
   * Deletes corresponding class file. Required on Rename, Move Class.
   *
   * @param cls  class name in the form com/package/Class$Inner.class
   * @return true if the file was successfully deleted
   */
  boolean delete(String cls);

  /**
   * Depending on options checks if any jar file date has changed
   * or any class in the classpath has changed (later one is much slower).
   * @return true if something has changed and project should be rebuilt
   */
  boolean isAnythingChanged();

  /**
   * Time of last modification
   * Returns 0 if unknown.
   *
   * @param cls  class name in the form com/package/Class$Inner.class
   * @return  last modified time
   */
  long lastModified(String cls);

  /**
   * Returns size of binary representation of a class for a given full
   * qualified class name. Returns 0 if class does not exists.
   * Names of packages and classes are delimited by slashes, inner
   * classes are delimited from containing classes by dollar sign.
   *
   * @param cls  class name in the form com/package/Class$Inner.class
   * @return  length of binary class representation
   */
  long length(String cls);

  boolean exists(String cls);

  /**
   * Provides binary stream for a given full qualified class name.
   * Names of packages and classes are delimited by slashes, inner
   * classes are delimited from containing classes by dollar sign.
   *
   * @param cls  class name in the form com/package/Class$Inner.class
   * @return  binary stream of class data
   **/
  InputStream getInputStream(String cls);

  /**
   * Releases any used resources and unlocks any files locked.
   **/
  void release();

  /**
   * This is used to check if classpath has changed in project
   */
  String getStringForm();

  /**
   * Example: "java/lang/Object.class"
   * @return true if classpath contains aClass.
   */
  public boolean contains(String aClass);

  /**
   * Returns entries from IDE. Used for displaying in RIT path settings dialog
   * Implementators should override it, by default returns all elements
   */
  public ClassPathElement[] getAutodetectedElements();

  /**
   * Interface representing individual class in classpath.
   **/
  interface Entry {
    long lastModified();

    long length();

    InputStream getInputStream();

    boolean delete();

    boolean exists();
  }
}
