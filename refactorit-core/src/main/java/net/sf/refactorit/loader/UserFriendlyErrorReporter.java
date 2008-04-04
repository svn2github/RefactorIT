/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.loader;


import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.source.UserFriendlyError;

import java.util.Iterator;


public final class UserFriendlyErrorReporter implements net.sf.refactorit.parser.
    ErrorListener {
  private final Project project;
  private final CompilationUnit compilationUnit;
  private boolean hadErrors = false;

  public UserFriendlyErrorReporter(Project project, CompilationUnit compilationUnit) {
    this.project = project;
    this.compilationUnit = compilationUnit;
  }

  public boolean hadErrors() {
    return hadErrors;
  }

  public void onError(String message, String fileName, int line, int column) {
    // NOTE: Sometimes line nr may simply point uninformatively to the beginning of file (ANTLR's problem?)
    hadErrors = true;
    (this.project.getProjectLoader().getErrorCollector()).addUserFriendlyError(new UserFriendlyError(
        message,
        getCompilationUnitForName(fileName),
        line,
        column
        ));
  }

  // FIXME: duplicate in Project
  /** NOTE: If unable to find, returns the compilationUnit given to constructor */
  private CompilationUnit getCompilationUnitForName(String name) {
    for (Iterator i = this.project.getCompilationUnits().iterator(); i.hasNext(); ) {
      CompilationUnit aCompilationUnit = (CompilationUnit) i.next();
      if (aCompilationUnit.getSource().getRelativePath().equals(name)) {
        return aCompilationUnit;
      }
    }

    return this.compilationUnit;
  }
}
