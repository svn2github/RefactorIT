/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.parser;

import net.sf.refactorit.vfs.Source;

import rantlr.Token;


/** AST node with both start and end coordinates. */
public class SimpleASTImpl extends TreeASTImpl {

  public SimpleASTImpl() {
//System.err.println("ASTImplImpl()");
  }

  public SimpleASTImpl(final int type, final String text) {
//System.err.println("ASTImplImpl()");
    initialize(type, text);
  }

  public SimpleASTImpl(final Token tok) {
//System.err.println("ASTImplImpl(" + tok + ")");
    initialize(tok);
  }

  /** Get the token text for this node */
  public String getText() {
    return this.text;
  }

  /** Set the token text for this node */
  public final void setText(final String text) {
    this.text = text;
  }

  public final void setColumn(final int column) {
    this.startColumn = (short) column;
  }

  public final int getColumn() {
    return this.startColumn;
  }

  public final void setLine(final int line) {
    this.startLine = (short) line;
  }

  public final int getLine() {
    return this.startLine;
  }

  public final void setStartLine(final int line) {
    setLine(line);
  }

  public final int getStartLine() {
    return getLine();
  }

  public final void setStartColumn(final int column) {
    setColumn(column);
  }

  public final int getStartColumn() {
    return getColumn();
  }

  public final void setEndColumn(final int column) {
    this.endColumn = (short) column;
  }

  public final int getEndColumn() {
    if (this.endColumn < 0) {
      return this.startColumn;
    }
    return this.endColumn;
  }

  public final void setEndLine(final int line) {
    this.endLine = (short) line;
  }

  public final int getEndLine() {
    if (this.endLine < 0) {
      return this.startLine;
    }
    return this.endLine;
  }

  public final Source getSource() {
    return this.source;
  }

  public final void setSource(final Source source) {
    this.source = source;
  }

  public final int getType() {
    return this.type;
  }

  public final void setType(final int type) {
    this.type = type;
  }

  private String text;
  private int type;

  private short startLine = -1;
  private short startColumn = -1;
  private short endLine = -1;
  private short endColumn = -1;

  private Source source = null;
}
