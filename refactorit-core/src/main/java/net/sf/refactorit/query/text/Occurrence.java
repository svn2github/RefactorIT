/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.text;

/**
 * Represents an occurance of some text search.
 *
 * @author  tanel
 */
public class Occurrence {

  private Line line;
  private int startPos;
  private int endPos;

  /**
   * @param line
   * @param startPos
   * @param endPos
   */
  public Occurrence(Line line, int startPos, int endPos) {
    this.line = line;
    this.startPos = startPos;
    this.endPos = endPos;
  }

  public Line getLine() {
    return line;
  }

  public int getStartPos() {
    return startPos;
  }

  public int getEndPos() {
    return endPos;
  }

  public String getText() {
    return getLine().getContent().substring(startPos, endPos);
  }
}
