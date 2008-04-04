/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test;

import net.sf.refactorit.vfs.ClassPath;
import net.sf.refactorit.vfs.JavadocPath;
import net.sf.refactorit.vfs.SourcePath;


/**
 * TestProject
 *
 * @author <a href="mailto:tonis.vaga@aqris.com>Tonis Vaga</a>
 * @version $Revision: 1.8 $ $Date: 2004/12/30 12:16:30 $
 */
public class TestProject {

  private String name;
  private SourcePath sourcePath;
  private ClassPath classPath;
  private JavadocPath javadocPath;

  public TestProject(String name, SourcePath sourcePath, ClassPath classPath, JavadocPath javadocPath) {
//    this.name = name; // it's not unique sometimes and fools the workspace
    this.name = Integer.toHexString(this.hashCode());
    this.sourcePath=sourcePath;
    this.classPath=classPath;
    this.javadocPath=javadocPath;
  }

  public SourcePath getSourcePath() {
    return this.sourcePath;
  }

  public ClassPath getClassPath() {
    return this.classPath;
  }

  public JavadocPath getJavadocPath() {
    return this.javadocPath;
  }

  public String getUniqueID() {
    return this.name;
  }
}
