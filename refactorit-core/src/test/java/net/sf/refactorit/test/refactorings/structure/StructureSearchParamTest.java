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
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.query.structure.FindRequest;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * This class tests Structure search for method parameters. Uses test-project,
 * located in folder: "test/projects/StructureSearch/Param/in"
 * 
 * @author Sergey Fedulov
 */
public class StructureSearchParamTest extends AbstractStructureSearchTest {

  public StructureSearchParamTest(String name) {
    super(name);
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite(StructureSearchParamTest.class);
    suite.setName("Structure Search Method Parameter");
    return suite;
  }
  
  public String getTemplate() {
    return "StructureSearch/Param/in";
  }

  int getSearchtype() {
    return FindRequest.PARAMSEARCH;
  }

  BinTypeRef getFoundTypeRef(BinItem foundItem) {
    BinMethod foundBinMethod = null;
    if (foundItem instanceof BinMethod){
      foundBinMethod = (BinMethod) foundItem;
    } else {
      fail("found binary member is not a binary method " + foundItem);
    }
    
    BinParameter[] parameters = foundBinMethod.getParameters();
    //Every method in the test class have only 1 parameter
    assertEquals("wrong number of method parameters ", 1, parameters.length);
    
    BinParameter parameter = foundBinMethod.getParameters()[0];
    return parameter.getTypeRef();
  }
  
  public void testString() throws Exception {
    checkMatches("java.lang.String", false, 2);
  }

  public void testStringSubtypes() throws Exception {
    checkMatches("java.lang.String", true, 2);
  }

  public void testObject() throws Exception {
    checkMatches("java.lang.Object", false, 3);
  }

  public void testObjectSubtypes() throws Exception {
    checkMatches("java.lang.Object", true, 9);
  }

  public void testBigDecimal() throws Exception {
    checkMatches("java.math.BigDecimal", false, 1);
  }

  public void testBigDecimalSubtypes() throws Exception {
    checkMatches("java.math.BigDecimal", true, 1);
  }
  
  public void testStringArray() throws Exception {
    checkMatches("java.lang.String[][]", false, 1);
  }

  public void testStringArraySubtypes() throws Exception {
    checkMatches("java.lang.String[][]", true, 1);
  }
  
  public void testObjectArray() throws Exception {
    checkMatches("java.lang.Object[][]", false, 1);
  }
  
  public void testObjectArraySubtypes() throws Exception {
    checkMatches("java.lang.Object[][]", true, 4);
  }
  public void testBigDecimalArray() throws Exception {
    checkMatches("java.math.BigDecimal[]", false, 0);
  }

  public void testBigDecimalArraySubtypes() throws Exception {
    checkMatches("java.math.BigDecimal[]", true, 0);
  }
  
  public void testPrimitivesExcluded1() throws Exception {
    checkMatches("int", false, 4);
  }
  
  public void testPrimitivesIncluded1() throws Exception {
    checkMatches("int", true, 4);
  }
  
  public void testPrimitivesExcluded2() throws Exception {
    checkMatches("int[]", false, 1);
  }
  
  public void testPrimitivesIncluded2() throws Exception {
    checkMatches("int[]", true, 1);
  }
  
  public void testPrimitivesExcluded3() throws Exception {
    checkMatches("int[]", false, 1);
  }
  
  public void testPrimitivesIncluded3() throws Exception {
    checkMatches("int[]", true, 1);
  }
  
  public void testPrimitivesNonExistIncluded() throws Exception {
    checkMatches("byte", false, 0);
  }
  
  public void testPrimitivesNonExistExcluded() throws Exception {
    checkMatches("byte", true, 0);
  }
}
