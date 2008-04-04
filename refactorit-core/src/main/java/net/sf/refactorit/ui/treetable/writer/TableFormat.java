/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.treetable.writer;

import java.util.List;

/**
 * @deprecated. XSL output is used now. See ReportGenerator. 
 * Remove this class after it is used nowhere
 */
public abstract class TableFormat {
  public abstract void startColumn(StringBuffer result, boolean isHeader);

  public abstract void endColumn(StringBuffer result, boolean isHeader,
      boolean isLastColumn);

  public abstract void startNewLine(StringBuffer result);

  public abstract void startPage(final String title, final StringBuffer result);

  public abstract void endPage(final StringBuffer result);

  public abstract void startTable(final StringBuffer result);

  public abstract void endTable(final StringBuffer result);

  public abstract String formatColumnContents(String plainText);

  public void endLine(final StringBuffer result) {
    result.append('\n');
  }

  public void addColumns(List contents, StringBuffer result, boolean isHeader) {
    for (int i = 0; i < contents.size(); i++) {
      createColumn((String) contents.get(i),
          isHeader, i == contents.size() - 1, result);
    }
  }

  private void createColumn(final String name, final boolean isHeader,
      final boolean isLastColumn, final StringBuffer result) {
    startColumn(result, isHeader);
    result.append(formatColumnContents(name));
    endColumn(result, isHeader, isLastColumn);
  }
}
