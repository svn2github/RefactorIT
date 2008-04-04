/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.treetable.writer;

import net.sf.refactorit.common.util.StringUtil;

/**
 * @deprecated. XSL output is used now. See ReportGenerator. 
 * Remove this class after it is used nowhere
 */
public class HtmlTableFormat extends TableFormat {
  public void endLine(final StringBuffer result) {
    result.append("</TR>");

    super.endLine(result);
  }

  public void endColumn(StringBuffer result, boolean isHeader,
      boolean isLastColumn) {
    if (isHeader) {
      result.append("</th>");
    } else {
      result.append("</TD>");
    }
  }

  public void startColumn(StringBuffer result, boolean isHeader) {
    if (isHeader) {
      result.append("<th align=center>");
    } else {
      result.append("<TD>");
    }
  }

  public void startNewLine(StringBuffer result) {
    result.append("<TR>");
  }

  public void endPage(final StringBuffer result) {
    result.append("</body>\n");
    result.append("</html>\n");
  }

  public void endTable(final StringBuffer result) {
    result.append("\n");
    result.append("</table>\n");
  }

  public void startTable(final StringBuffer result) {
    result.append("<table border=1>\n");
  }

  public String formatColumnContents(String plainText) {
    return StringUtil.textIntoHTML(plainText);
  }

  public void startPage(String title, StringBuffer result) {
    if (title == null) {
      title = "";

    }
    result.append("<html>\n");
    result.append("<title>" + title + "</title>\n");
    result.append("<body>\n");
  }

}
