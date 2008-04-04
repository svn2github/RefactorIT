/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.fixmescanner;

import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.transformations.TransformationManager;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;



public class Timestamper {
  private List fixmeWords;
  private List compilationUnitsToScan;

  private boolean scanAllLines;
  private int startLine;
  private int stopLine;

  private IsFixmeCommentChecker checker;

  public Timestamper(List compilationUnitsToScan, List fixmeWords,
      boolean scanAllLines, int startLine, int stopLine) {
    this.fixmeWords = fixmeWords;
    this.compilationUnitsToScan = compilationUnitsToScan;

    this.scanAllLines = scanAllLines;
    this.startLine = startLine;
    this.stopLine = stopLine;

    checker = new IsFixmeCommentChecker(fixmeWords);
  }

  private boolean hasTimestampSomewhere(Comment comment, DateFormat format) {
    return CommentBodyEditor.extractTimestamp(comment.getText(), format) != null;
  }

  public void applyTimestamp(Calendar timestamp,
      DateFormat format) throws IOException {
    for (Iterator i = this.compilationUnitsToScan.iterator(); i.hasNext(); ) {
      CompilationUnit compilationUnit = (CompilationUnit) i.next();

      FixmeCommentFinder fixmeCommentFinder = new FixmeCommentFinder(this.
          fixmeWords);

      // Let's get original Comment instances so we can update them in the cache;
      // if multiline comments were appended we'd get Comment instances that were not present
      // in CompilationUnit's cahce and the updateCommentCacheInCompilationUnit would fail.
      fixmeCommentFinder.setUniteMultilineComments(false);

      for (Iterator c = fixmeCommentFinder.getFixmeComments(compilationUnit,
          this.scanAllLines, this.startLine, this.stopLine).iterator();
          c.hasNext(); ) {
        Comment comment = (Comment) c.next();
        if (!hasTimestampSomewhere(comment, format)) {
          applyTimestamp(comment, compilationUnit, timestamp, format);
        }
      }
    }
  }

  private static String intoUnixNewlines(String s) {
    s = StringUtil.replace(s, "\r\n", "\n");
    s = StringUtil.replace(s, "\r", "\n");

    return s;
  }

  private Location getLineAndColumnForPos(int position, String text) {
    net.sf.refactorit.utils.LinePositionUtil.setTabSize(1);
    net.sf.refactorit.source.SourceCoordinate location =
        net.sf.refactorit.utils.LinePositionUtil.convert(position, text);

    return new Location(location.getLine(), location.getColumn());
  }

  private int getFixmeWordEndPos(String s) {
    if (checker.isFixmeComment(s)) {
      if (checker.getMatchedWord().isRegularExpression) {
        return checker.getMatchingPositionOnTrimmedLine();
      } else {
        return checker.getMatchingPositionOnTrimmedLine()
            + checker.getMatchedWord().word.length();
      }
    } else {
      return -1;
    }
  }

  private void insertStringToFile(CompilationUnit compilationUnit, Location location,
      String str) throws IOException {
    TransformationManager manager = new TransformationManager(null);
    manager.add(
        new StringInserter(compilationUnit, location.line, location.column - 1, str));

    // FIXME check for result status!!!
    manager.performTransformations();
  }

  private Location getFixmeWordEndLocation(String s) {
    s = intoUnixNewlines(s);

    if (getFixmeWordEndPos(s) >= 0) {
      return getLineAndColumnForPos(getFixmeWordEndPos(s), s);
    } else {
      return null;
    }
  }

  private void applyTimestamp(Comment comment, CompilationUnit compilationUnit,
      Calendar time, DateFormat format) throws IOException {
    Location breakPos = getFixmeWordEndLocation(comment.getText());
    if (breakPos == null) {
      return;
    }

    if (breakPos.line == 1) {
      breakPos.column += comment.getStartColumn() - 1;
    }
    breakPos.line += comment.getStartLine() - 1;

    insertStringToFile(compilationUnit, breakPos,
        CommentBodyEditor.createTimestamp(time, format));

    updateCommentCacheInCompilationUnit(compilationUnit, comment,
        getFixmeWordEndPos(comment.getText()),
        CommentBodyEditor.createTimestamp(time, format));
  }

  /**
   * @param  comment *MUST* be the same *instance* that is in
   *     CompilationUnit's cache, otherwise this method won't update
   *     it - don't use 'united' comments here.
   */
  // FIXME shouldn't it just be rebuilded normally???
  private void updateCommentCacheInCompilationUnit(
      CompilationUnit compilationUnit, Comment comment, int posInText, String timestamp
      ) {
    String newText = comment.getText().substring(0, posInText) +
        timestamp + comment.getText().substring(posInText);

    comment.changeText(newText);
  }

  private static class Location {
    public int line;
    public int column;

    public Location(int line, int column) {
      this.line = line;
      this.column = column;
    }
  }
}
