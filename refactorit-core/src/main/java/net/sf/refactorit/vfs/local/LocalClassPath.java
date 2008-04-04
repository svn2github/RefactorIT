/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.vfs.local;


import net.sf.refactorit.vfs.AbstractClassPath;
import net.sf.refactorit.vfs.ClassPathElement;
import net.sf.refactorit.vfs.ZipClassPathElement;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;


/**
 * Local filesystem ClassPath. Do not forget to call release() after using it.
 *
 * @author  Igor Malinin
 */
public class LocalClassPath extends AbstractClassPath {
  private final List classpath;

  public LocalClassPath(String classpath) {
    this(split(classpath));
  }

  public LocalClassPath(List paths) {
    if (paths == null) {
      classpath = Collections.EMPTY_LIST;
    } else {
      classpath = paths;
    }
  }

  public final ClassPathElement[] createElements() {
    if (classpath.size() == 0) {
      return new ClassPathElement[0];
    }

    List list = new ArrayList(classpath.size());

    for (int i = 0; i < classpath.size(); i++) {
      File file = new File((String)classpath.get(i));

      ClassPathElement element;

      element = createClassPathElement(file);

      if ( element != null ) {
        list.add(element);
      }
    }

    return (ClassPathElement[]) list.toArray(new ClassPathElement[list.size()]);
  }

  public void addPath(String path) {
    classpath.add(path);
  }

  /**
   *  Creates local classpath element from file
   * @param file
   * @return classpath element or null if !file.exists()
   */
  public static ClassPathElement createClassPathElement(File file) {
    ClassPathElement element;
    if (!file.exists()) {
      element = null;
    } else if (file.isDirectory()) {
      element = new DirClassPathElement(file);
    } else {
      element = new ZipClassPathElement(file);
    }
    return element;
  }

  private static List split(String classpath) {
    StringTokenizer st = new StringTokenizer(classpath, File.pathSeparator);

    List result = new ArrayList(st.countTokens());

    while (st.hasMoreTokens()) {
      result.add(st.nextToken());
    }

    return result;
  }

  /**
   * Overriden because here we don't actually need to call getElements()
   *
   * @return string representing the classpath
   */
  public final String getStringForm() {
    StringBuffer buf = new StringBuffer(128);

    for (int i = 0; i < classpath.size(); i++) {
      buf.append((String)classpath.get(i)).append(File.pathSeparatorChar);
    }

    return buf.toString();
  }
}
