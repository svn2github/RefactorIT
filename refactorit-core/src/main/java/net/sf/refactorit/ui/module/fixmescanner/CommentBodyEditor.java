/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.fixmescanner;


import net.sf.refactorit.loader.Comment;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.StringTokenizer;


public class CommentBodyEditor {
  /**
   * As an exception it creates one space in the beginning of each line that starts with an '*' --
   * this makes javadoc comments look well aligned.
   */
  public static String trimAllLines(String text) {
    String result = "";

    StringTokenizer lines = new StringTokenizer(text, "\n");
    while (lines.hasMoreTokens()) {
      result += identBlockCommentLine(lines.nextToken().trim()) + "\r\n";
    }

    return result.trim();
  }

  private static String identBlockCommentLine(String line) {
    if (line.startsWith("*")) {
      return " " + line;
    } else {
      return line;
    }
  }

  public static String identLines(String lines, int spaceCount) {
    StringBuffer result = new StringBuffer();
    StringTokenizer l = new StringTokenizer(lines, "\r\n");

    while (l.hasMoreTokens()) {
      for (int i = 0; i < spaceCount; i++) {
        result.append(" ");
      }

      result.append(l.nextToken());
      if (l.hasMoreTokens()) {
        result.append("\r\n");
      }
    }

    return result.toString();
  }

  public static Calendar extractTimestamp(String commentText,
      DateFormat preferredFormat) {
    Calendar result = extractTimestampWithSpecifiedFormat(commentText,
        preferredFormat);
    if (result != null) {
      return result;
    }

    TimestampFormat[] formats = TimestampFormat.
        getAvailableFormatsSortedAlphabetically();
    for (int i = 0; i < formats.length; i++) {
      result = extractTimestampWithSpecifiedFormat(commentText,
          formats[i].getDateFormat());
      if (result != null) {
        return result;
      }
    }

    return null;
  }

  private static Calendar extractTimestampWithSpecifiedFormat(String
      commentText, DateFormat format) {
    int scanPos = 0;
    while (commentText.indexOf("(", scanPos) >= 0) {
      int openingBrace = commentText.indexOf("(", scanPos + 1);
      int closingBrace = commentText.indexOf(")", openingBrace + 1);

      if (openingBrace < 0 || closingBrace < 0) {
        return null;
      }

      try {
        Calendar result = Calendar.getInstance();

        result.setTime(format.parse(commentText.substring(openingBrace + 1,
            closingBrace)));
        return result;
      } catch (ParseException e) {
        // Ignore & continue searching
      }

      scanPos = openingBrace + 1;
    }

    return null;
  }

  public static String createTimestamp(Calendar time, DateFormat format) {
    return "(" + format.format(time.getTime()) + ")";
  }

  /** NOTE: Comment.getText() *always* ends with a line break */
  public static Comment combine(Comment first, Comment second) {
    Comment result = new Comment(
        first.getText() + getNewline(first) + second.getText(),
        first.getStartLine(),
        first.getStartColumn()
        );

    result.setCompilationUnit(first.getCompilationUnit());

    return result;
  }

  private static String getNewline(Comment comment) {
    String compilationUnitContent = comment.getCompilationUnit().getContent();
    if (compilationUnitContent.indexOf("\r\n") > 0) {
      return "\r\n";
    } else if (compilationUnitContent.indexOf("\r") > 0) {
      return "\r";
    } else if (compilationUnitContent.indexOf("\n") > 0) {
      return "\n";
    } else {
      return System.getProperty("line.separator");
    }
  }
}
