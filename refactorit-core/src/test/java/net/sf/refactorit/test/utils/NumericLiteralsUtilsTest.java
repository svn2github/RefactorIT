/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.utils;

import net.sf.refactorit.utils.NumericLiteralsUtils;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author Arseni Grigorjev
 */
public class NumericLiteralsUtilsTest extends TestCase {
  
  public NumericLiteralsUtilsTest(String name) {
    super(name);
  }
  
  public static Test suite() {
    return new TestSuite(NumericLiteralsUtilsTest.class);
  }
  
  public void testValidNumericLiterals() {
    assertTrue(NumericLiteralsUtils.isValidNumLiteral("5"));
    assertTrue(NumericLiteralsUtils.isValidNumLiteral("5.6"));
    assertTrue(NumericLiteralsUtils.isValidNumLiteral("5L"));
    assertTrue(NumericLiteralsUtils.isValidNumLiteral("555555555555L"));
    assertTrue(NumericLiteralsUtils.isValidNumLiteral("5.4D"));
    assertTrue(NumericLiteralsUtils.isValidNumLiteral("3.4444444444D"));
    assertTrue(NumericLiteralsUtils.isValidNumLiteral("5.555e55"));
    assertTrue(NumericLiteralsUtils.isValidNumLiteral("5.555e555"));
    assertTrue(NumericLiteralsUtils.isValidNumLiteral("555555555555"));
    assertTrue(NumericLiteralsUtils.isValidNumLiteral("0x003"));
  }
  
  public void testInvalidNumericLiterals() {
    assertFalse(NumericLiteralsUtils.isValidNumLiteral("_5"));
    assertFalse(NumericLiteralsUtils.isValidNumLiteral("5.6x11"));
    assertFalse(NumericLiteralsUtils.isValidNumLiteral("5555555f55555L"));
    assertFalse(NumericLiteralsUtils.isValidNumLiteral("5.4De33"));
    assertFalse(NumericLiteralsUtils.isValidNumLiteral("3,4444444444D"));
    assertFalse(NumericLiteralsUtils.isValidNumLiteral("5.5f55e55"));
    assertFalse(NumericLiteralsUtils.isValidNumLiteral("543a5"));
    assertFalse(NumericLiteralsUtils.isValidNumLiteral(".5435e3d4"));
  }
  
}
