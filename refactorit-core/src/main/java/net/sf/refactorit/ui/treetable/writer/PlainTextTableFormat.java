/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.treetable.writer;

/**
 * @deprecated. XSL output is used now. See ReportGenerator. 
 * Remove this class after it is used nowhere
 */
public class PlainTextTableFormat extends TableFormat {
  public void endColumn(StringBuffer result, boolean isHeader,
      boolean isLastColumn) {
    if (isHeader) {
      result.append('\"');
    }

    if (!isLastColumn) {
      result.append(getDelimiter());
    }
  }

  public void startColumn(StringBuffer result, boolean isHeader) {
    if (isHeader) {
      result.append('\"');
    }
  }

  public void startNewLine(StringBuffer result) {
  }

  public void endPage(final StringBuffer result) {
  }

  public void endTable(final StringBuffer result) {
  }

  public void startTable(final StringBuffer result) {
  }

  public String formatColumnContents(String plainText) {
    return plainText;
  }

  public void startPage(final String title, final StringBuffer result) {
  }

  protected String getDelimiter() {
    return "\t";
  }
}
