/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classfile;


import net.sf.refactorit.classmodel.Project;

import java.io.IOException;
import java.io.InputStream;


public final class ClassDataSource extends DataSource {

  public ClassDataSource(Project project, String resource) {
    super(project, resource);
  }

  public final long length() throws IOException {
    return getProject().getPaths().getClassPath().length(getResource());
  }

  public final boolean exists() throws IOException {
    return getProject().getPaths().getClassPath().exists(getResource());
  }

  public final long lastModified() {
    return getProject().getPaths().getClassPath().lastModified(getResource());
  }

  public final InputStream getInputStream() throws IOException {
    return getProject().getPaths().getClassPath().getInputStream(getResource());
  }
}
