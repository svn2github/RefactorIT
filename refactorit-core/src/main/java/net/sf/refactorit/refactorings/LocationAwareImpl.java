/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings;

import net.sf.refactorit.classmodel.AbstractLocationAware;
import net.sf.refactorit.classmodel.CompilationUnit;


/**
 * Basic implementation of {@link net.sf.refactorit.classmodel.LocationAware LocationAware}
 * for composition ant testing purposes.<br>
 * Used also for moving expression sequences.
 *
 * @author Anton Safonov
 */
public class LocationAwareImpl extends AbstractLocationAware {

  private CompilationUnit compilationUnit = null;
  private int startLine;
  private final int endLine;
  private int startColumn;
  private final int endColumn;

  public LocationAwareImpl(CompilationUnit compilationUnit, int startLine, int startColumn,
      int endLine, int endColumn) {
    this.compilationUnit = compilationUnit;
    this.startLine = startLine;
    this.startColumn = startColumn;
    this.endLine = endLine;
    this.endColumn = endColumn;
  }

  public final CompilationUnit getCompilationUnit() {
    return compilationUnit;
  }

  public final int getStartLine() {
    return startLine;
  }
  
  public final void setStartLine(int startLine) {
    this.startLine = startLine;
  }

  public final int getEndLine() {
    return endLine;
  }

  public final int getStartColumn() {
    return startColumn;
  }
  
  public final void setStartColumn(int startColumn) {
    this.startColumn = startColumn;
  }

  public final int getEndColumn() {
    return endColumn;
  }

  public final String toString() {
    String name = this.getClass().getName();
    return name.substring(name.lastIndexOf('.') + 1) + ": "
        + getCompilationUnit() + ", "
        + getStartLine() + ":" + getStartColumn() + " - "
        + getEndLine() + ":" + getEndColumn() + ", "
        + Integer.toHexString(hashCode());
  }
}
