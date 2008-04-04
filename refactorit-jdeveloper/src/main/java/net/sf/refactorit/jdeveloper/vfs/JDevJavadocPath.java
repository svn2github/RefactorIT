/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jdeveloper.vfs;


import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.commonIDE.options.Path;
import net.sf.refactorit.commonIDE.options.PathItem;
import net.sf.refactorit.jdeveloper.projectoptions.ProjectConfiguration;
import net.sf.refactorit.vfs.JavadocPath;
import oracle.jdeveloper.library.JLibrary;
import oracle.jdeveloper.library.JLibraryManager;
import oracle.jdeveloper.model.JProject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * @author Anton Safonov
 */
public class JDevJavadocPath extends JavadocPath {
  private String[] paths;

  public JDevJavadocPath(final JProject jproject) {
    final String[] libs = jproject.getActiveConfiguration().getLibraryList();
    final List collected = new ArrayList();
    for (int i = 0; i < libs.length; i++) {
      try {
        final JLibrary lib = JLibraryManager.findLibrary(libs[i]);
        final String[] curDocPaths = lib.getDefaultDocPath().toStrings();
        for (int k = 0; k < curDocPaths.length; k++) {
          CollectionUtil.addNew(collected, curDocPaths[k]);
        }
      } catch (NullPointerException e) {
        // ignore, it probably happens on not-uptodate libs
      }
    }

    final String[] curDocPaths
        = jproject.getActiveConfiguration().getDocPath().toStrings();
    for (int k = 0; k < curDocPaths.length; k++) {
      CollectionUtil.addNew(collected, curDocPaths[k]);
    }

    this.paths = (String[]) collected.toArray(new String[collected.size()]);
  }

  public String[] getElements() {
    ProjectConfiguration projectConfig = ProjectConfiguration.getActiveInstance();
    String javadocStr = projectConfig
        .get(ProjectConfiguration.PROP_JAVADOCPATH);
    List result = CollectionUtil.toMutableList(this.paths);
    List specifiedJavadocs = new Path(javadocStr).toPathItems();
    for(Iterator it = specifiedJavadocs.iterator(); it.hasNext(); ) {
      CollectionUtil.addNew(result, ((PathItem)it.next()).toString());
    }

    return (String[]) result.toArray(new String[result.size()]);
  }
}
