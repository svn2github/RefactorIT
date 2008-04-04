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
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.ASTUtil;


/**
 * @see net.sf.refactorit.classmodel.Project#addUserFriendlyError(UserFriendlyError)
 * @see net.sf.refactorit.classmodel.Project#getUserFriendlyErrors()
 */
public class UserFriendlyError {
  private final String description;
  private final CompilationUnit compilationUnit;
  private int line;
  private int column;
  private String nodeText;
  public static final String NO_DESCRIPTION_MSG = "no description";

  public static final int UNKNOWN = -1;

  public UserFriendlyError(String description, CompilationUnit compilationUnit, int line,
      int column) {
    if (description == null) {
      description = NO_DESCRIPTION_MSG;
      new Exception("Created UserFriendlyError without description for: "
          + compilationUnit + " " + line + ":" + column).printStackTrace(System.err);
    }

    this.description = description;
    this.compilationUnit = compilationUnit;
    this.line = line;
    this.column = column;
    this.nodeText = "none";

    /* it can be null when we rename or move file in FS with CVS
         if (Assert.enabled) {
     Assert.must(this.compilationUnit != null,
        "CompilationUnit is null in UserFrienlyError: " + this.description);
         }
     */
  }

  public UserFriendlyError(String description, CompilationUnit compilationUnit,
      ASTImpl ast) {
    this(description, compilationUnit);

    if (ast != null && ast.getLine() > 0 && ast.getColumn() > 0) {
      this.line = ast.getLine();
      this.column = ast.getColumn();
      this.nodeText = ast.getText();
    } else if (ast != null) {
      ASTImpl newAst = ASTUtil.getFirstNodeOnLine(ast);
      if (newAst == null) {
        this.line = UNKNOWN;
        this.column = UNKNOWN;
      } else {
        this.line = newAst.getLine();
        this.column = 1;
      }
      this.nodeText = ast.getText();
    }
  }

  public UserFriendlyError(String description, CompilationUnit compilationUnit) {
    this(description, compilationUnit, UNKNOWN, UNKNOWN);
  }

  public String getDescription() {
    return this.description;
  }

  public String getNodeText() {
    return this.nodeText;
  }

  public final CompilationUnit getCompilationUnit() {
    return this.compilationUnit;
  }

  public final int getLine() {
    return this.line;
  }

  public final int getColumn() {
    return this.column;
  }

  public final boolean hasLineAndColumn() {
    return this.line != UNKNOWN && this.column != UNKNOWN;
  }

  public final boolean hasLocation() {
    return this.compilationUnit != null && hasLineAndColumn();
  }

  public String toString() {
    String position;

    if (hasLineAndColumn()) {
      position = this.line + ":" + this.column;
    } else {
      position = "";
    }

    String sourceName;

    if (this.compilationUnit != null) {
      sourceName = this.compilationUnit.getDisplayPath();
    } else {
      sourceName = "<unknown source file>";
    }

    return sourceName + " " + position + " - " + description;
  }

  public final boolean equals(Object anotherObject) {
    if (anotherObject instanceof UserFriendlyError && anotherObject != null) {
      UserFriendlyError anotherError = (UserFriendlyError) anotherObject;

      return
          anotherError.getColumn() == this.getColumn() &&
          anotherError.getLine() == this.getLine() &&
          anotherError.getDescription().equals(this.getDescription()) &&
          anotherError.getAbsoluteSourcePath().equals(this.
          getAbsoluteSourcePath());
    } else {
      return false;
    }
  }

  private String getAbsoluteSourcePath() {
    if (getCompilationUnit() == null) {
      return "<unknown source file>";
    } else {
      return this.getCompilationUnit().getSource().getAbsolutePath();
    }
  }

  public final int hashCode() {
    return getDescription().hashCode();
  }

  public final boolean contains(final LocationAware la) {
    if (this.getCompilationUnit() != null && this.hasLineAndColumn()) {
      if (this.getCompilationUnit() == la.getCompilationUnit()) {
        if ((this.getLine() == la.getStartLine()
            && this.getColumn() >= la.getStartColumn())
            || (this.getLine() == la.getEndLine()
            && this.getColumn() <= la.getEndColumn())
            || (this.getLine() > la.getStartLine()
            && this.getLine() < la.getEndLine())) {
          return true;
        }
      }
    }

    return false;
  }
}
