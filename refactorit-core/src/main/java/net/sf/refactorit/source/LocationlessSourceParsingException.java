/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source;

import net.sf.refactorit.classmodel.CompilationUnit;


/**
 * This is a version of SourceParsingException for which
 * the throwing code cannot figure out where the exact location of the error
 * is in a particular file and therefore it cannot throw a proper user friendly
 * error.
 */
public final class LocationlessSourceParsingException extends Exception {
  private final String description;
  private final CompilationUnit compilationUnit;
  public final Exception e;

  public LocationlessSourceParsingException(String description,
      CompilationUnit compilationUnit) {
    super(description);

    this.description = description;
    this.compilationUnit = compilationUnit;
    this.e = new Exception("thrown at");
  }

  public CompilationUnit getCompilationUnit() {
    return this.compilationUnit;
  }

  public String toString() {
    return this.description;
  }
}
