/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.utils;

import net.sf.refactorit.source.SourceCoordinate;


public final class LinePositionUtil {
  private static int tabSize = 2; // FIXME should be bug here when real != 2!!!

  public static void setTabSize(int newTabSize) {
    tabSize = newTabSize;
  }

  public static SourceCoordinate convert(int requiredPosition, String source) {
    source = useUnixNewlines(source);

    SourceCoordinate location = new SourceCoordinate(1, 1);

    for (int sourcePos = 0; sourcePos < source.length(); sourcePos++) {
      if (sourcePos == requiredPosition) {
        return location;
      }

      updateLocationForNextPosition(location, source.charAt(sourcePos));
    }

    if (requiredPosition == source.length()) {
      return location;
    } else {
      throw new IllegalArgumentException("requiredPosition " + requiredPosition
          + " is out of source bounds [0.." + source.length() + "]");
    }
  }

  private static void updateLocationForNextPosition(SourceCoordinate location,
      char currentChar) {
    if (currentChar == '\n' || currentChar == '\r') {
      location.setLine(location.getLine() + 1);
      location.setColumn(1);
    } else if (currentChar == '\t') {
      location.setColumn(location.getColumn() + tabSize);
    } else {
      location.setColumn(location.getColumn() + 1);
    }
  }

  public static String useUnixNewlines(String source) {
    StringBuffer result = new StringBuffer();

    for (int i = 0; i < source.length() - 1; i++) {
      if (source.charAt(i) == '\r') {
        if (source.charAt(i + 1) != '\n') {
          result.append('\n');
        }
      } else {
        result.append(source.charAt(i));

      }
    }
    if (source.length() >= 1) {
      result.append(source.charAt(source.length() - 1));

    }
    return result.toString();
  }

  public static String extractLine(int wantedLineNr, String content) {
    content = useUnixNewlines(content);
    wantedLineNr--; // FIXME: make everyone to use line starting from 0, not from 1
    int lineStartPos = 0;

    for (int line = 0; line < wantedLineNr; line++) {
      lineStartPos = nextLineStartPos(content, lineStartPos);
    }

    int nextLineStartPos = nextLineStartPos(content, lineStartPos);
    if (nextLineStartPos == 0) {
      return content.substring(lineStartPos);
    } else {
      return content.substring(lineStartPos, nextLineStartPos - 1);
    }
  }

  private static int nextLineStartPos(String content, int lineStartPos) {
    return content.indexOf("\n", lineStartPos) + 1;
  }

  public static boolean between(int column, int line, int startCol,
      int startLine, int endCol, int endLine) {
    if (line < startLine || line > endLine) {
      return false;
    }

    if (line == startLine && column < startCol) {
      return false;
    }

    if (line == endLine && column > endCol) {
      return false;
    }

    return true;
  }
}
