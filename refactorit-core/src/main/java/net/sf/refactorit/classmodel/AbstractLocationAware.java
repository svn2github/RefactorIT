/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel;

import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.utils.FileUtil;


/**
 * @author Anton Safonov
 */
public abstract class AbstractLocationAware extends BinItem implements
    LocationAware {

  public abstract CompilationUnit getCompilationUnit();

  public abstract int getStartLine();

  public abstract int getEndLine();

  public abstract int getStartColumn();

  public abstract int getEndColumn();

  public final boolean contains(final LocationAware other) {
    if (other == null) {
      return false;
    }

    if (this == other) {
      return true;
    }

    boolean contains = this.getCompilationUnit() == other.getCompilationUnit();

    if (contains) {
      contains = this.getStartLine() <= other.getStartLine()
          && this.getEndLine() >= other.getEndLine();
    }

    if (contains && this.getStartLine() == other.getStartLine()
        && this.getStartColumn() > other.getStartColumn()) {
      contains = false;
    }

    if (contains && this.getEndLine() == other.getEndLine()
        && this.getEndColumn() < other.getEndColumn()) {
      contains = false;
    }

//    if (!contains && other instanceof BinSourceConstruct) {
//      // this is e.g. for arithmetical expressions
//      BinSourceConstruct oth = (BinSourceConstruct) other;
//      while (oth != null && oth != this
//          && oth.getParent() instanceof BinSourceConstruct) {
//        oth = (BinSourceConstruct) oth.getParent();
//      }
//      contains = oth == this;
//    }

    return contains;
  }

  public final boolean isPreprocessedSource() {
    CompilationUnit source = getCompilationUnit();
    if (source == null) {
      return false;
    }
    return!(FileUtil.isJavaFile(source.getName()));
  }

  public final int getStartPosition() {
    CompilationUnit compilationUnit = getCompilationUnit();
    if (compilationUnit == null || compilationUnit.getSource() == null) {
      return 0;
    }
    return compilationUnit.getSource().getPosition(getStartLine(), getStartColumn());
  }

  public final int getEndPosition() {
    CompilationUnit compilationUnit = getCompilationUnit();
    if (compilationUnit == null || compilationUnit.getSource() == null) {
      return 0;
    }
    return compilationUnit.getSource().getPosition(getEndLine(), getEndColumn());
  }

  public String getText() {
    CompilationUnit compilationUnit = getCompilationUnit();
    if (compilationUnit == null || compilationUnit.getSource() == null) {
      return "";
    }
    return compilationUnit.getSource().getText(
        getStartLine(), getStartColumn(), getEndLine(), getEndColumn());
  }

  /** @return indent in the beginning of line the construct resides on */
  public final int getIndent() {
    CompilationUnit compilationUnit = getCompilationUnit();
    if (compilationUnit == null || compilationUnit.getSource() == null) {
      return 0;
    }

    final int tabSize = FormatSettings.getTabSize();
    int indent = 0;
    int pos = compilationUnit.getSource().getLineIndexer().lineColToPos(getStartLine(),
        1);
    String content = compilationUnit.getSource().getContentString();
    while (pos < content.length()
        && Character.isWhitespace(content.charAt(pos))) {
      if (content.charAt(pos) == '\t') {
        indent += tabSize;
      } else {
        ++indent;
      }
      ++pos;
    }

    return indent;
  }

  /**
   * @param other other statement
   * @return true if <code>this</code> is residing after given construct
   */
  public final boolean isAfter(LocationAware other) {
    return getStartPosition() > other.getStartPosition();
  }

// Decided not to hack it this way! [Anton]
//  /** Needed to get ast back after it was indexed.
//   *  Some time ago there was a direct link ASTTree <-> CompilationUnit,
//   *  but for JSP files  ASTTree may hold ASTs from several files, thus you can
//   *  go ASTTree(index) -> CompilationUnit, but you can't get back.
//   */
//  public void setAstTree(ASTTree astTree) {
//    this.astTree = astTree;
//  }
//
//  public ASTTree getAstTree() {
//    return this.astTree;
//  }
//
//  private ASTTree astTree = null;

}
