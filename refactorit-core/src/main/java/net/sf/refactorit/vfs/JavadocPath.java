/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.vfs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


/**
 * @author Anton Safonov
 */
public abstract class JavadocPath {

  public abstract String[] getElements();

  public final String getStringForm() {
    String[] paths = getElements();
    String result = new String();
    for (int i = 0; i < paths.length; i++) {
      if (i > 0) {
        result += File.pathSeparatorChar;
      }
      result += paths[i];
    }

    return result;
  }

  protected final String[] split(final String paths) {
    List result = new ArrayList();

    StringTokenizer tokens = new StringTokenizer(paths, File.pathSeparator);

    while (tokens.hasMoreTokens()) {
      result.add(tokens.nextToken());
    }

    return (String[]) result.toArray(new String[result.size()]);
  }

  public final String toString() {
    return getStringForm();
  }

}
