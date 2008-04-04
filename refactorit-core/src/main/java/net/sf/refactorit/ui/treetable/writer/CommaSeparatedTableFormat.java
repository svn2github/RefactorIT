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
public class CommaSeparatedTableFormat extends PlainTextTableFormat {
  private static final String QUOTE = "\"";

  protected String getDelimiter() {
    return ",";
  }

  public String formatColumnContents(String plainText) {
    return escapeColumnContents(super.formatColumnContents(plainText));
  }

  public String escapeColumnContents(String s) {
    if (s.indexOf(getDelimiter()) >= 0 || s.indexOf(QUOTE) >= 0) {
      return QUOTE + StringUtil.replace(s, QUOTE, QUOTE + QUOTE) +
          QUOTE;
    } else {
      return s;
    }
  }
}
