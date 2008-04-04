/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.transformations;


import net.sf.refactorit.vfs.Source;

import java.util.HashMap;
import java.util.Map;


public final class SavedChildren {
  private final Map map = new HashMap();

  public SavedChildren(Source source) {
    saveChildren(source);
  }

  private void saveChildren(Source source) {
    Source[] children = source.getChildren();
    map.put(source, children);

    for (int i = 0; i < children.length; i++) {
      saveChildren(children[i]);
    }
  }

  public Source[] getChildren(Source source) {
    return (Source[]) map.get(source);
  }
}
