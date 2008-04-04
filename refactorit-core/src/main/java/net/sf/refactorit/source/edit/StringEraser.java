/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.source.edit;


import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.query.LineIndexer;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.source.Position;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.transformations.TransformationList;

import java.io.IOException;
import java.util.List;


public class StringEraser extends DefaultEditor {
  /** Starts with 1 */
  private int startLine;
  /** Starts with 0 */
  private int startColumn;
  /** Starts with 1 */
  private int endLine;
  /** Starts with 0 */
  private int endColumn;

  private boolean trimTrailingSpace = false;
  private boolean removePunctuation = false;
  private boolean removeLinesContainingOnlyComments = false;

  /**
   *
   * @param input
   * @param line starts with 1
   * @param column starts with 0
   * @param length
   */
  public StringEraser(SourceHolder input, int line, int column, int length) {
    super(input);

    this.startLine = line;
    this.startColumn = column;
    this.endLine = this.startLine;
    this.endColumn = this.startColumn + length;
  }

  public StringEraser(SourceHolder input,
      SourceCoordinate startCoordinate,
      SourceCoordinate endCoordinate) {
    this(input, startCoordinate.getLine(), startCoordinate.getColumn() - 1,
        endCoordinate.getLine(), endCoordinate.getColumn() - 1);
  }

  public StringEraser(LocationAware i) {
    this(i.getCompilationUnit(), i.getStartLine(), i.getStartColumn() - 1,
        i.getEndLine(), i.getEndColumn() - 1);
  }

  public StringEraser(LocationAware i, boolean trimTrailingSpace) {
    this(i.getCompilationUnit(), i.getStartLine(), i.getStartColumn() - 1,
        i.getEndLine(), i.getEndColumn() - 1);
    this.trimTrailingSpace = trimTrailingSpace;
  }

  /**
   *
   * @param input
   * @param startLine starts with 1!!!
   * @param startColumn starts with 0!!!
   * @param endLine starts with 1!!!
   * @param endColumn starts with 0!!!
   */
  public StringEraser(SourceHolder input,
      int startLine, int startColumn,
      int endLine, int endColumn) {
    super(input);

    this.startLine = startLine;
    this.startColumn = startColumn;
    this.endLine = endLine;
    this.endColumn = endColumn;
  }

  /**
   * Erase whole source
   * @param input SourceHolder
   */
  public StringEraser(SourceHolder input) {
    this(input, 0, (int) input.getSource().length());
  }

  public StringEraser(SourceHolder input, Position interval) {
    this(input, interval.getStart(), interval.getEnd());
  }

  public StringEraser(SourceHolder input, int startPos, int endPos) {
    super(input);
    final LineIndexer indexer = getTarget().getSource().getLineIndexer();

    SourceCoordinate coordinate = indexer.posToLineCol(startPos);
    this.startLine = coordinate.getLine();
    this.startColumn = coordinate.getColumn() - 1;
    coordinate = indexer.posToLineCol(endPos);
    this.endLine = coordinate.getLine();
    this.endColumn = coordinate.getColumn() - 1;
  }

  /**
   * @param input
   * @param node
   * @param removePunctuationMarks when true removes dot, semicolon
   * and comma after node and comma before node if any
   */
  public StringEraser(SourceHolder input, ASTImpl node,
      boolean removePunctuationMarks) {
    super(input);

    this.startLine = node.getStartLine();
    this.startColumn = node.getStartColumn() - 1;
    this.endLine = node.getEndLine();
    this.endColumn = node.getEndColumn() - 1;
    if (removePunctuationMarks) {
      removePunctuation(input, node, false);
    }
  }

  public StringEraser(SourceHolder input, int startLine, int startColumn,
      int endLine, int endColumn,
      boolean removePunctuationMarks, boolean skipRemovingAfter) {
    super(input);

    this.startLine = startLine;
    this.startColumn = startColumn;
    this.endLine = endLine;
    this.endColumn = endColumn;

    if (removePunctuationMarks) {
      removePunctuation(input, null, skipRemovingAfter);
    }
  }

  private void removePunctuation(SourceHolder source, final ASTImpl node,
      boolean skipRemovingAfter) {
    if (!(source instanceof CompilationUnit)) {
      if (Assert.enabled) {
        Assert.must(false, "called with strange source: " + source);
      }
      return; // let's make it safe
    }
    final CompilationUnit compilationUnit = (CompilationUnit) source;
    final String content = compilationUnit.getSource().getContentString();
    final LineIndexer indexer = compilationUnit.getSource().getLineIndexer();

    boolean foundPunctuation = false;
    boolean foundSemicolon = false;

    int end = indexer.lineColToPos(this.endLine, this.endColumn + 1);
    if (!skipRemovingAfter) {
      end = StringUtil.findPostfixWhitespace(content, end);
      if (end < content.length()
          && (content.charAt(end) != '.'
          && content.charAt(end) != ';'
          && content.charAt(end) != ',')) {
        Comment comment = Comment.findAt(compilationUnit, end);
        while (comment != null) {
          end = indexer.lineColToPos(comment.getEndLine(), comment.getEndColumn());
          end = StringUtil.findPostfixWhitespace(content, end);
          if (end < content.length()) {
            comment = Comment.findAt(compilationUnit, end);
          } else {
            comment = null;
          }
        }
      }

      if (end < content.length()
          && (content.charAt(end) == '.'
          || content.charAt(end) == ';'
          || content.charAt(end) == ',')) {
        ++end;
        if (content.charAt(end - 1) == ';') {
          foundSemicolon = true;

          boolean hasSomethingInfront = true;
          int temp = indexer.lineColToPos(this.startLine, this.startColumn + 1);
          while (temp - 1 >= 0
              && Character.isWhitespace(content.charAt(temp - 1))) {
            if (content.charAt(temp - 1) == '\r'
                || content.charAt(temp - 1) == '\n') {
              hasSomethingInfront = false;
              break;
            }
            --temp;
          }
          if (hasSomethingInfront && temp == 0) {
            hasSomethingInfront = false;
          }

          while (end < content.length()
              && (content.charAt(end) == ' ' || content.charAt(end) == '\t')) {
            ++end;
          }
          if (!hasSomethingInfront) {
            end = StringUtil.moveOneLinebreakForth(content, end);
          }
        } else {
          end = StringUtil.findPostfixWhitespace(content, end);
        }
        foundPunctuation = true;
      }
      //    if (!foundPunctuation
      //        && end < content.length() && content.charAt(end) == ')') {
      //      foundPunctuation = true;
      //    }
      if (!foundPunctuation) {
        end = indexer.lineColToPos(this.endLine, this.endColumn + 1);
      }
    }

    int start = indexer.lineColToPos(this.startLine, this.startColumn + 1);

    if (!foundPunctuation) {
      start = StringUtil.findPrefixWhitespace(content, start);
      if (start - 1 >= 0 && content.charAt(start - 1) != ',') {
        Comment comment = Comment.findAt(compilationUnit, start - 1);
        while (comment != null) {
          start = indexer.lineColToPos(comment.getStartLine(),
              comment.getStartColumn());
          start = StringUtil.findPrefixWhitespace(content, start);
          if (start - 1 >= 0) {
            comment = Comment.findAt(compilationUnit, start - 1);
          } else {
            comment = null;
          }
        }
      }
      if (start - 1 >= 0 && content.charAt(start - 1) == ',') {
        --start;
        start = StringUtil.findPrefixWhitespace(content, start);
        foundPunctuation = true;
      }
      if (start - 1 >= 0 && content.charAt(start - 1) == '(') {
        foundPunctuation = true;
      }
      if (!foundPunctuation) {
        start = indexer.lineColToPos(this.startLine, this.startColumn + 1);
      }
    } else {
      if (foundSemicolon) {
        while (start - 1 >= 0
            && (content.charAt(start - 1) == ' '
            || content.charAt(start - 1) == '\t')) {
          --start;
        }
      }
    }

    if (!foundPunctuation
        && end < content.length() && content.charAt(end) == ')') {
      foundPunctuation = true;
    }

    if (!foundPunctuation) {
      if (Assert.enabled) {
        /*Assert.must(false,
         "Erasing strange node without punctuation around as was requested: "
            + node + ", source: " + source);*/
      }
    } else {
      SourceCoordinate coordinate = indexer.posToLineCol(start);
      this.startLine = coordinate.getLine();
      this.startColumn = coordinate.getColumn() - 1;

      coordinate = indexer.posToLineCol(end);
      this.endLine = coordinate.getLine();
      this.endColumn = coordinate.getColumn() - 1;
    }

    removePunctuation = true;
  }

  public void setTrimTrailingSpace(final boolean trimTrailingSpace) {
    this.trimTrailingSpace = trimTrailingSpace;
  }

  public void setRemoveLinesContainingOnlyComments(boolean b) {
    removeLinesContainingOnlyComments = b;
  }

  //public List apply(SourceManager manager) throws IOException {
  public RefactoringStatus apply(LineManager manager) {
    RefactoringStatus status = new RefactoringStatus();

    try {
      for (int cur = this.startLine; cur <= this.endLine; cur++) {
        try {
          Line line = manager.getLine(getTarget(), cur);

          int start = 0, end = line.length();
          if (cur == this.startLine) {
            start = this.startColumn;
          }
          if (cur == this.endLine) {
            end = this.endColumn;
          }

          if (trimTrailingSpace && !removePunctuation) {
            while (start > 0
                && Character.isWhitespace(line.charAt(start - 1))) {
              --start;
            }

            while (end < line.length()
                && Character.isWhitespace(line.charAt(end))) {
              if (line.charAt(end) == '\r' || line.charAt(end) == '\n') {
                // if there is nothing left infront so we can take linebreak also
                if (start != 0) {
                  break;
                }
              }

              ++end;
            }
          }

//        if (start >= 0 && end <= line.length()) {
//          throw new IndexOutOfBoundsException(
//              "Wrong delete from: \""
//              + StringUtil.printableLinebreaks(line.toString())
//              + "\" length: " + line.length()
//              + ",\nstart: " + start + ", end: " + end
//              + "(" + this + ")");
//        }

//      System.err.println("Delete from: \""
//          + StringUtil.printableLinebreaks(line.toString())
//          + "\" length: " + line.length()
//          + ",\nstart: " + start + ", end: " + end
//          + "(" + this + ") - \"" + line.substring(start, end) + "\"");

          line.delete(start, end);

          if (removeLinesContainingOnlyComments &&
              StringUtil.containsOnlyWhitespaceAndComments(line.getContent())) {
            line.clear();
          }
        } catch (IndexOutOfBoundsException e) {
          e.printStackTrace(System.err);

          status.addEntry(getTarget().getSource().getDisplayPath()
              + " - " + cur,
              CollectionUtil.singletonArrayList(e), RefactoringStatus.FATAL);
        }
      }

      if (EditorManager.debug) {
        manager.dumpSource(System.err, getTarget());
      }
    } catch (IOException e) {
      status.addEntry(e, RefactoringStatus.FATAL);
    }

    return status;
  }

  public static void addNodeRemovingEditors(final TransformationList transList,
      List astsToRemove, SourceHolder source, boolean trimTrailingSpace) {
    for (int i = 0; i < astsToRemove.size(); i++) {
      ASTImpl node = (ASTImpl) astsToRemove.get(i);
      node = new CompoundASTImpl(node);
      //System.err.println("import node: " + node);

      final StringEraser eraser = new StringEraser(source, node, true);
      eraser.setTrimTrailingSpace(trimTrailingSpace);
      transList.add(eraser);
    }
  }

  public String toString() {
    String name = this.getClass().getName();
    return name.substring(name.lastIndexOf('.') + 1) + ": "
        + getTarget().getSource() + " - "
        + startLine + ":" + startColumn
        + " - " + endLine + ":" + endColumn;
  }

}
