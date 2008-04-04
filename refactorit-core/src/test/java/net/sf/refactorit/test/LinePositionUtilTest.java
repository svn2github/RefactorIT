/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test;

import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.utils.LinePositionUtil;


public class LinePositionUtilTest extends junit.framework.TestCase {
  public LinePositionUtilTest(String name) {
    super(name);
  }

  public static junit.framework.Test suite() {
    return new junit.framework.TestSuite(LinePositionUtilTest.class);
  }

  SourceCoordinate c;

  public void testOneLine() {
    c = LinePositionUtil.convert(0, "..");
    assertEquals(1, c.getColumn());
    assertEquals(1, c.getLine());

    c = LinePositionUtil.convert(1, "..");
    assertEquals(2, c.getColumn());
    assertEquals(1, c.getLine());
  }

  public void testUnixNewline() {
    c = LinePositionUtil.convert(0, "\n");
    assertEquals(1, c.getColumn());
    assertEquals(1, c.getLine());

    c = LinePositionUtil.convert(1, "\n.");
    assertEquals(1, c.getColumn());
    assertEquals(2, c.getLine());

    c = LinePositionUtil.convert(1, "\n\n");
    assertEquals(1, c.getColumn());
    assertEquals(2, c.getLine());
  }

  public void testMacNewline() {
    c = LinePositionUtil.convert(0, "\r");
    assertEquals(1, c.getColumn());
    assertEquals(1, c.getLine());

    c = LinePositionUtil.convert(1, "\r.");
    assertEquals(1, c.getColumn());
    assertEquals(2, c.getLine());

    c = LinePositionUtil.convert(1, "\r\r");
    assertEquals(1, c.getColumn());
    assertEquals(2, c.getLine());
  }

  public void testWindowsNewline() {
    c = LinePositionUtil.convert(0, "\r\n");
    assertEquals(1, c.getColumn());
    assertEquals(1, c.getLine());

    c = LinePositionUtil.convert(1, "\r\n.");
    assertEquals(1, c.getColumn());
    assertEquals(2, c.getLine());

    c = LinePositionUtil.convert(1, "\r\n\r\n");
    assertEquals(1, c.getColumn());
    assertEquals(2, c.getLine());
  }

  public void testTab() {
    LinePositionUtil.setTabSize(4);
    c = LinePositionUtil.convert(1, "\t.");
    assertEquals(5, c.getColumn());
    assertEquals(1, c.getLine());
  }

  public void testExtractLine() {
    assertEquals("test", LinePositionUtil.extractLine(1, "test\n"));
    assertEquals("wow", LinePositionUtil.extractLine(1, "wow\n"));
    assertEquals("", LinePositionUtil.extractLine(1, "\n"));

    assertEquals("++", LinePositionUtil.extractLine(2, "..\n++\n"));
    assertEquals(",", LinePositionUtil.extractLine(2, "..\n,\n"));

    assertEquals("--", LinePositionUtil.extractLine(3, "..\n..\n--\n"));

    assertEquals("test", LinePositionUtil.extractLine(0, "test"));
  }
}
