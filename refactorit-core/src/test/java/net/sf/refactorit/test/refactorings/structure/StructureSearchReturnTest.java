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
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.query.structure.FindRequest;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * This class tests Structure search for method return types. Uses test-project,
 * located in folder: "test/projects/StructureSearch/Return/in"
 * 
 * @author Sergey Fedulov
 */
public class StructureSearchReturnTest extends AbstractStructureSearchTest {

  public StructureSearchReturnTest(String name) {
    super(name);
  }
  
  public static Test suite() {
    final TestSuite suite = new TestSuite(StructureSearchReturnTest.class);
    suite.setName("Structure Search Method Return");
    return suite;
  }

  public String getTemplate() {
    return "StructureSearch/Return/in";
  }

  int getSearchtype() {
    return FindRequest.RETURNSEARCH;
  }

  BinTypeRef getFoundTypeRef(BinItem foundItem) {
    BinMethod foundBinMethod = null;
    if (foundItem instanceof BinMethod){
      foundBinMethod = (BinMethod) foundItem;
    } else {
      fail("found binary member is not a binary method " + foundItem);
    }
    
    return foundBinMethod.getReturnType();
  }
  
  public void testString() throws Exception {
    checkMatches("java.lang.String", false, 2);
  }

  public void testStringSubtypes() throws Exception {
    checkMatches("java.lang.String", true, 2);
  }

  public void testObject() throws Exception {
    checkMatches("java.lang.Object", false, 2);
  }

  public void testObjectSubtypes() throws Exception {
    checkMatches("java.lang.Object", true, 10);
  }

  public void testBigDecimal() throws Exception {
    checkMatches("java.math.BigDecimal", false, 2);
  }

  public void testBigDecimalSubtypes() throws Exception {
    checkMatches("java.math.BigDecimal", true, 2);
  }
  
  public void testStringArray() throws Exception {
    checkMatches("java.lang.String[][][]", false, 1);
  }

  public void testStringArraySubtypes() throws Exception {
    checkMatches("java.lang.String[][][]", true, 1);
  }
  
  public void testObjectArray() throws Exception {
    checkMatches("java.lang.Object[][]", false, 1);
  }
  
  public void testObjectArraySubtypes() throws Exception {
    checkMatches("java.lang.Object[][]", true, 5);
  }
  
  public void testBigDecimalArray() throws Exception {
    checkMatches("java.math.BigDecimal[][][]", false, 1);
  }

  public void testBigDecimalArraySubtypes() throws Exception {
    checkMatches("java.math.BigDecimal[][][]", true, 1);
  }
  
  public void testPrimitiveDouble() throws Exception {
    checkMatches("double", false, 1);
  }
  
  public void testPrimitiveDoubleSubtypes() throws Exception {
    checkMatches("double", true, 1);
  }
  
  public void testPrimitiveShort() throws Exception {
    checkMatches("short", false, 4);
  }
  
  public void testPrimitiveShortSubtypes() throws Exception {
    checkMatches("short", true, 4);
  }
  
  public void testNonExistingPrimitiveFloat() throws Exception {
    checkMatches("float", false, 0);
  }
  
  public void testNonExistingPrimitiveFloatSubtypes() throws Exception {
    checkMatches("float", true, 0);
  }
}
