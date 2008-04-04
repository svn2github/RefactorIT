/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query;


import net.sf.refactorit.source.SourceCoordinate;

import java.util.ArrayList;


/**
 * Note: line and column numbers start from 1, *not* 0.
 */
public final class LineIndexer {
  private final int[] lineStarts;
  private final long lastModified;

  public LineIndexer(String content, long lastModified) {
    // FIXME: maybe this has bugs - please fix them
    this.lastModified = lastModified;
    int currentLine = 0;

    ArrayList tmpLineArray = new ArrayList();

    tmpLineArray.add(new Integer(0));

    for (int i = 0; i < content.length(); ++i) {
      boolean atTheEndOfLine = false;
      char c = content.charAt(i);
      if (c == '\r') {
        if (i != content.length() - 1 && content.charAt(i + 1) == '\n') {
          i++;
        }
        atTheEndOfLine = true;
      }

      if (c == '\n') {
        atTheEndOfLine = true;
      }

      if (atTheEndOfLine) {
        currentLine++;
        tmpLineArray.add(new Integer(i + 1));
      }

    } // for ends

    lineStarts = new int[tmpLineArray.size()];
    for (int i = 0; i < tmpLineArray.size(); ++i) {
      lineStarts[i] = ((Integer) tmpLineArray.get(i)).intValue();
    }

  }

  public final int coordinateToPos(SourceCoordinate coordinate) {
    return lineColToPos(coordinate.getLine(), coordinate.getColumn());
  }

  /**
   * @param line starts from 1
   * @param column starts from 1
   * @return position starting from 0
   */
  public final int lineColToPos(int line, int column) {

    try {
      return lineStarts[line - 1] + column - 1;
    } catch (ArrayIndexOutOfBoundsException e) {
      throw new IllegalArgumentException("Line number " + line
          + " is not in range 1.." + lineStarts.length);
    }
  }

  public final long lastModified() {
    return this.lastModified;
  }

  /**
   * @param absolutePosition starts from 0
   * @return line & col start from 1
   */
  public final SourceCoordinate posToLineCol(int absolutePosition) {
    for (int line = 1; line < lineStarts.length; line++) {
      if (lineStarts[line] > absolutePosition) {
        return new SourceCoordinate(line,
            absolutePosition - lineStarts[line - 1] + 1);
      }
    }

    return new SourceCoordinate(lineStarts.length,
        absolutePosition - lineStarts[lineStarts.length - 1] + 1);
  }

  public final int getLineCount() {
    return this.lineStarts.length;
  }
}
