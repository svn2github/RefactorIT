/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings.structure;

import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.query.structure.FindRequest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This class tests "Structure field search". Using test-project,
 * located in folder: "test/projects/StructureSearch/Field/in"
 * 
 * @author Sergey Fedulov
 */
public class StructureSearchFieldTest extends AbstractStructureSearchTest {
  
  public StructureSearchFieldTest(String name) {
    super(name);
  }
  
  public static Test suite() {
    final TestSuite suite = new TestSuite(StructureSearchFieldTest.class);
    suite.setName("Structure Search Field");
    return suite;
  }
  
  public String getTemplate() {
    return "StructureSearch/Field/in";
  }
  
  public int getSearchtype(){
    return FindRequest.FIELDSEARCH;
  }
  
  BinTypeRef getFoundTypeRef(BinItem foundItem){
    BinField foundBinField = null;
    if (foundItem instanceof BinField){
      foundBinField = (BinField) foundItem; 
    } else {
      fail("found binary member is not a binary field " + foundItem);
    }
    
    return foundBinField.getTypeRef();
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
    checkMatches("java.lang.Object", true, 13);
  }

  public void testBigDecimal() throws Exception {
    checkMatches("java.math.BigDecimal", false, 1);
  }

  public void testBigDecimalSubtypes() throws Exception {
    checkMatches("java.math.BigDecimal", true, 1);
  }
  
  public void testStringArray() throws Exception {
    checkMatches("java.lang.String[][][]", false, 1);
  }

  public void testStringArraySubtypes() throws Exception {
    checkMatches("java.lang.String[][][]", true, 1);
  }
  
  public void testObjectArray() throws Exception {
    checkMatches("java.lang.Object[]", false, 1);
  }
  
  public void testObjectArraySubtypes() throws Exception {
    checkMatches("java.lang.Object[]", true, 5);
  }
  
  public void testBigDecimalArray() throws Exception {
    checkMatches("java.math.BigDecimal[]", false, 0);
  }

  public void testBigDecimalArraySubtypes() throws Exception {
    checkMatches("java.math.BigDecimal[]", true, 0);
  }
  
  public void testPrimitiveIntArray() throws Exception {
    checkMatches("int[]", true, 4);
  }
  
  public void testNonExistPrimitiveDoubleArray() throws Exception {
    checkMatches("double[]", true, 0);
  }
  
  public void testPrimitiveDoubleArray() throws Exception {
    checkMatches("double[][]", true, 2);
  }
  
  public void testNonExistPrimitiveChar() throws Exception {
    checkMatches("char", true, 1);
  }
  
  public void testPrimitiveCharArray() throws Exception {
    checkMatches("char[][][][][]", true, 1);
  }
  
  public void testNonExistPrimitiveByte() throws Exception {
    checkMatches("byte", true, 0);
  }
  
  public void testPrimitiveBoolean() throws Exception {
    checkMatches("boolean", true, 2);
  }
  
  public void testPrimitiveFloat() throws Exception {
    checkMatches("float", true, 2);
  }
}
