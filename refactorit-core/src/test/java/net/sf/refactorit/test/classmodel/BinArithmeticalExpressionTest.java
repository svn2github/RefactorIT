/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.classmodel;



import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.expressions.BinArithmeticalExpression;
import net.sf.refactorit.classmodel.expressions.BinLogicalExpression;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.test.refactorings.BinSelectionFinder;

import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class BinArithmeticalExpressionTest extends TestCase {

  public BinArithmeticalExpressionTest(final String name) {
    super(name);
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite(BinArithmeticalExpressionTest.class);
    suite.setName("BinArithmeticalExpression tests");
    return suite;
  }
  
  public static void testArithmeticalExpression(String firstType, String operation,
      String secondType, String returnType) {
    /* constructs a following code:
    * class X {
    *   void foo() {
    *     firstType a;
    *     secondType b;
    *     a operation b;
    *   }
  	*}
    * and tests it */
    String code = "class X { \n" + 
    " void foo() \n " + 
    "{ \n" + 
    firstType + " a; \n" + 
    secondType +" b; \n" + 
    "System.out.println(/*[*/a " + operation + " b/*]*/); \n" 
    + "}\n" 
    + "}";

    Project project = Utils.createTestRbProjectFromString(code);
    List list = BinSelectionFinder.getSelectedItemsFromProject(project);
    String actualReturnType = null;
    for (Iterator it = list.iterator(); it.hasNext();) {
      Object o = it.next();
      if (o instanceof BinArithmeticalExpression) {
        actualReturnType = ((BinArithmeticalExpression) o).getReturnType().getQualifiedName();
        break;
      } else if(o instanceof BinLogicalExpression) {
        actualReturnType = ((BinLogicalExpression) o).getReturnType().getQualifiedName();
      }
    }
    assertNotNull("Expression type is not resolved", actualReturnType);
    
    assertEquals("" + firstType + " " + operation + " " + secondType
        + " shall be " + returnType, returnType, actualReturnType);

  }

  public void test_byte_byte_Expression1() {
    testArithmeticalExpression("byte", "+", "byte", "int");
  }
  
  public void test_byte_byte_Expression2() { 
    testArithmeticalExpression("byte", "/", "byte", "int");
  }
  
  public void test_byte_byte_Expression3() {
    testArithmeticalExpression("byte", "%", "byte", "int");
  }
  
  public void test_byte_byte_Expression4() {
    testArithmeticalExpression("byte", "*", "byte", "int");
  }
  
  public void test_byte_byte_Expression5() {
    testArithmeticalExpression("byte", "-", "byte", "int");
  }
  
  public void test_byte_byte_Expression6() {
    testArithmeticalExpression("byte", "&", "byte", "int");
  }
  
  public void test_byte_byte_Expression7() {
    testArithmeticalExpression("byte", "^", "byte", "int");
  }
  
  public void test_byte_byte_Expression8() {
    testArithmeticalExpression("byte", "|", "byte", "int");
  }
  
  
  public void test_Byte_byte_Expression() {
    testArithmeticalExpression("Byte", "+", "byte", "int");
  }
  
  public void test_byte_Byte_Expression() {
    testArithmeticalExpression("byte", "+", "Byte", "int");
  }
  
  public void test_Byte_Byte_Expression() {
    testArithmeticalExpression("Byte", "+", "Byte", "int");
  }
  
  public void test_short_short_Expression() {
    testArithmeticalExpression("short", "+", "short", "int");
  }
  
  public void test_Short_short_Expression() {
    testArithmeticalExpression("Short", "+", "short", "int");
  }
 
  public void test_short_Short_Expression() {
    testArithmeticalExpression("short", "+", "Short", "int");
  }
  
  public void test_Short_Short_Expression() {
    testArithmeticalExpression("Short", "+", "Short", "int");
  }

  public void test_short_int_Expression() {
    testArithmeticalExpression("short", "+", "int", "int");
  }
  
  public void test_Short_int_Expression() {
    testArithmeticalExpression("Short", "+", "int", "int");
  }
  
  public void test_short_Integer_Expression() {
    testArithmeticalExpression("short", "+", "Integer", "int");
  }
  
  public void test_Short_Integer_Expression() {
    testArithmeticalExpression("Short", "+", "Integer", "int");
  }
  
  public void test_Long_Integer_Expression() {
    testArithmeticalExpression("Long", "+", "Integer", "long");
  }
  
  public void test_Float_Integer_Expression() {
    testArithmeticalExpression("Float", "+", "Integer", "float");
  }
  
  public void test_Double_Integer_Expression() {
    testArithmeticalExpression("Double", "+", "Integer", "double");
  }
  
  public void test_Integer_byte_Expression() {
    testArithmeticalExpression("Integer", "+", "byte", "int");
  }
  
  public void test_Integer_float_Expression() {
    testArithmeticalExpression("Integer", "+", "float", "float");
  }

}
