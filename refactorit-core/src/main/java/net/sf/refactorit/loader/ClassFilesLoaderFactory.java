/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.loader;

import net.sf.refactorit.classmodel.Project;


/**
 * @author risto
 */
public class ClassFilesLoaderFactory {
  protected ClassFilesLoader createFor(Project p) {
    return new ClassFilesLoader(p);
  }
}
