/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.cli;

import net.sf.refactorit.ui.treetable.writer.CommaSeparatedTableFormat;

import junit.framework.TestCase;


public class CommaSeparatedTableFormatTest extends TestCase {
  private CommaSeparatedTableFormat t = new CommaSeparatedTableFormat();

  public void testSimpleEscape() {
    assertEquals("", t.escapeColumnContents(""));
    assertEquals("a", t.escapeColumnContents("a"));
    assertEquals("a a", t.escapeColumnContents("a a"));
  }

  public void testComma() {
    assertEquals("\",\"", t.escapeColumnContents(","));
  }

  public void testQuotesInside() {
    assertEquals("\",,,\"\",,,\"", t.escapeColumnContents(",,,\",,,"));
  }

  public void testQuotesWithoutDelimiters() {
    assertEquals("\"xxx\"\"xxx\"", t.escapeColumnContents("xxx\"xxx"));
  }
}
