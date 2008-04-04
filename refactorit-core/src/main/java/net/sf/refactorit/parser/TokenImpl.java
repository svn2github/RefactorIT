/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.parser;

import net.sf.refactorit.common.util.StringUtil;


public final class TokenImpl extends rantlr.ConcreteToken
    implements TokenExt, JavaTokenTypes {
  public static int count = 0;

  private short startLine;
  private short startColumn;
  private short endLine;
  private short endColumn;
  private String text;

  public TokenImpl(final int t,
      final int tokenStartLine, final int tokenStartColumn,
      final int tokenEndLine, final int tokenEndColumn) {
    this(t, tokenStartLine, tokenStartColumn);
    this.endLine = (short) tokenEndLine;
    this.endColumn = (short) tokenEndColumn;
  }

  public TokenImpl(final int t,
      final int tokenStartLine, final int tokenStartColumn) {
    super(t);
    this.startLine = (short) tokenStartLine;
    this.startColumn = (short) tokenStartColumn;
    ++count;
  }

  public final int getColumn() {
    return startColumn;
  }

  public final int getLine() {
    return startLine;
  }

  public final String getText() {
    return text;
  }

  public final void setColumn(final int c) {
    startColumn = (short) c;
  }

  public final void setLine(final int l) {
    startLine = (short) l;
  }

  public final void setText(final String t) {
    text = t;
  }

  public final String toString() {
    return StringUtil.printableLinebreaks(getText())
        + " [" + getStartLine() + ":" + getStartColumn() + " - "
        + getEndLine() + ":" + getEndColumn() + "]";
  }

  public final int getStartLine() {
    return this.startLine;
  }

  public final int getStartColumn() {
    return this.startColumn;
  }

  public final int getEndLine() {
    return this.endLine;
  }

  public final void setEndLine(final int endLine) {
    this.endLine = (short) endLine;
  }

  public final int getEndColumn() {
    return this.endColumn;
  }

  public final void setEndColumn(final int endColumn) {
    this.endColumn = (short) endColumn;
  }

  public final void reinit(final int t,
      final int tokenStartLine, final int tokenStartColumn,
      final int tokenEndLine, final int tokenEndColumn) {
    setType(t);
    this.startLine = (short) tokenStartLine;
    this.startColumn = (short) tokenStartColumn;
    this.endLine = (short) tokenEndLine;
    this.endColumn = (short) tokenEndColumn;
    this.text = null;
  }

  public final void clean() {
    setType(INVALID_TYPE);
    this.startLine = 0;
    this.startColumn = 0;
    this.endLine = 0;
    this.endColumn = 0;
    this.text = null;
  }
}
