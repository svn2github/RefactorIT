/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings.structure;

import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.expressions.BinCastExpression;
import net.sf.refactorit.query.structure.FindRequest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class StructureSearchTypeCastTest extends AbstractStructureSearchTest {

  public StructureSearchTypeCastTest(String name) {
    super(name);
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite(StructureSearchTypeCastTest.class);
    suite.setName("Structure Search Type Cast");
    return suite;
  }

  public String getTemplate() {
    return "StructureSearch/TypeCast/in";
  }
  
  int getSearchtype() {
    return FindRequest.TYPECASTSEARCH;
  }

  BinTypeRef getFoundTypeRef(BinItem foundItem) {
    BinCastExpression foundCast = null;
    if (foundItem instanceof BinCastExpression){
      foundCast = (BinCastExpression) foundItem;
    } else {
      fail("found binary member is not a binary method " + foundItem);
    }
    
    return foundCast.getReturnType();
  }

  public void testString() throws Exception {
    checkMatches("java.lang.String", false, 3);
  }

  public void testStringSubtypes() throws Exception {
    checkMatches("java.lang.String", true, 3);
  }

  public void testObject() throws Exception {
    checkMatches("java.lang.Object", false, 2);
  }

  public void testObjectSubtypes() throws Exception {
    checkMatches("java.lang.Object", true, 8);
  }

  public void testTest() throws Exception {
    checkMatches("Test", false, 1);
  }

  public void testTestSubtypes() throws Exception {
    checkMatches("Test", true, 1);
  }

  public void testBigDecimal() throws Exception {
    checkMatches("java.math.BigDecimal", false, 1);
  }

  public void testBigDecimalSubtypes() throws Exception {
    checkMatches("java.math.BigDecimal", true, 1);
  }

  public void testBigInteger() throws Exception {
    checkMatches("java.math.BigInteger", false, 0);
  }

  public void testBigIntegerSubtypes() throws Exception {
    checkMatches("java.math.BigInteger", true, 0);
  }
  
  public void testPtimitiveInt() throws Exception {
    checkMatches("int", false, 2);
  }

  public void testPtimitiveIntSubtypes() throws Exception {
    checkMatches("int", true, 2);
  }
  
  public void testPtimitiveIntArray() throws Exception {
    checkMatches("int[]", false, 1);
  }

  public void testPtimitiveIntArraySubtypes() throws Exception {
    checkMatches("int[]", true, 1);
  }
}
