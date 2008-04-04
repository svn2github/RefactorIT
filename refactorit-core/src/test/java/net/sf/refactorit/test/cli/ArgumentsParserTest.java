/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.cli;

import net.sf.refactorit.cli.ArgumentsParser;
import net.sf.refactorit.common.util.StringUtil;

import junit.framework.TestCase;


public class ArgumentsParserTest extends TestCase {
  private ArgumentsParser p;

  public void testExistenceOfParameter() {
    p = new ArgumentsParser(new String[] {"-sourcepath"});
    assertTrue(p.hasParameter(ArgumentsParser.SOURCEPATH));

    p = new ArgumentsParser(new String[] {"-classpath", ".", "-sourcepath"});
    assertTrue(p.hasParameter(ArgumentsParser.SOURCEPATH));

    p = new ArgumentsParser(new String[] {"-other"});
    assertFalse(p.hasParameter(ArgumentsParser.SOURCEPATH));
  }

  public void testPathParameter() {
    p = new ArgumentsParser(new String[] {});
    assertEquals("", p.getPathParameterValue(ArgumentsParser.SOURCEPATH));

    p = new ArgumentsParser(new String[] {"-sourcepath", "x"});
    assertEquals("x", p.getPathParameterValue(ArgumentsParser.SOURCEPATH));

    p = new ArgumentsParser(new String[] {"-sourcepath", "x", "y"});
    assertEquals("x" + StringUtil.PATH_SEPARATOR + "y",
        p.getPathParameterValue(ArgumentsParser.SOURCEPATH));

    p = new ArgumentsParser(new String[] {"-sourcepath", "x", "-classpath"});
    assertEquals("x", p.getPathParameterValue(ArgumentsParser.SOURCEPATH));

    p = new ArgumentsParser(new String[] {"-sourcepath", "-classpath"});
    assertEquals("", p.getPathParameterValue(ArgumentsParser.SOURCEPATH));
  }

  public void testStringConstructor() {
    p = new ArgumentsParser("-classpath");
    assertTrue(p.hasParameter(ArgumentsParser.CLASSPATH));
  }

  public void testUnknownTag() {
    p = new ArgumentsParser("-unknown_tag");
    assertEquals("[-unknown_tag]", p.getUnknownTags().toString());

    p = new ArgumentsParser("-unknown_tag a");
    assertEquals("[-unknown_tag]", p.getUnknownTags().toString());

    p = new ArgumentsParser("-notused");
    assertEquals("[]", p.getUnknownTags().toString());

    p = new ArgumentsParser("-metrics");
    assertEquals("[]", p.getUnknownTags().toString());
  }

}
