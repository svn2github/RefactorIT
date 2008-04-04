/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel;

import net.sf.refactorit.vfs.Source;


/**
 * @author Tonis Vaga
 */
public final class JspCompilationUnit extends CompilationUnit {

  public JspCompilationUnit(final Source aSource, final Project project) {
    super(aSource, project);
    //DebugInfo.trace("created JspCompilationUnit "+aSource.getName());
  }
}
