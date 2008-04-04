/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.text;

import net.sf.refactorit.vfs.Source;


/**
 * Represents a line in a non-java source file
 *
 * @author  tanel
 */
public class Line {
  private Source source;
  private int lineNumber;
  private String content;

  /**
   * @param source
   * @param lineNumber
   * @param content
   */
  public Line(Source source, int lineNumber, String content) {
    this.source = source;
    this.lineNumber = lineNumber;
    this.content = content;
  }

  public String getContent() {
    return this.content;
  }

  public void setContent(final String content) {
    this.content = content;
  }

  public int getLineNumber() {
    return this.lineNumber;
  }

  public void setLineNumber(final int lineNumber) {
    this.lineNumber = lineNumber;
  }

  public Source getSource() {
    return this.source;
  }
}
