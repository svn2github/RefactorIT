/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source;

import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.parser.ASTImpl;


public final class SourceCoordinate implements Comparable {

  /* The line number. The topmost line is assumed to be 1 */
  private int line = -1;

  /* The number of column. The leftmost column is assumed to be 1 */
  private int column = -1;

  public SourceCoordinate(int line, int column) {

    // Init fields
    setLine(line);
    setColumn(column);
  }

  public String toString() {
    return getClass().getName() + "; line=" + (getLine()) + ", column="
        + (getColumn());
  }

  public int getLine() {
    return this.line;
  }

  public void setLine(int line) {
    this.line = line;
  }

  public int getColumn() {
    return this.column;
  }

  public void setColumn(int column) {
    this.column = column;
  }

  /** Assumes currently in the 1,1 (ASTs) coordinate space; perhaps should be made to always work? */
  public void convertToEditorCoordinateSpace() {
    setColumn(getColumn() - 1);
  }

  public void convertToTextModel() {
    setColumn(getColumn() + 1);
  }

  /**
   * Constructs an instance from the specified ASTImpl.
   */
  public static SourceCoordinate getForAST(final ASTImpl ast) {
    if (ast != null) {
      return new SourceCoordinate(ast.getLine(), ast.getColumn());
    }
    return null;
  }

  public static SourceCoordinate getForStart(ASTImpl ast) {
    return new SourceCoordinate(ast.getStartLine(), ast.getStartColumn());
  }

  public static SourceCoordinate getForEnd(ASTImpl ast) {
    return new SourceCoordinate(ast.getEndLine(), ast.getEndColumn());
  }

  public int compareTo(Object obj) {
    SourceCoordinate another = (SourceCoordinate) obj;
    int res = this.getLine() - another.getLine();
    if (res == 0) {
      res = this.getColumn() - another.getColumn();
    }

    return res;
  }

  public boolean isBefore(SourceCoordinate other) {
    return compareTo(other) < 0;
  }
  
  public boolean isContainedBy(LocationAware la) {
    int startLine = la.getStartLine();
    int endLine = la.getEndLine();
    
    return ((line > startLine ||
        line == startLine && column >= la.getStartColumn() - 1) &&
        (line < endLine ||
        line == endLine && column < la.getEndColumn() - 1));
  }

  public boolean equals(Object other) {
    return compareTo(other) == 0;
  }

}
