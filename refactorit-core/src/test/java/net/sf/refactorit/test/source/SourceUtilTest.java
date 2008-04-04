/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.source;



import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinInterface;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.common.util.FileCopier;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.commonIDE.FastItemFinder;
import net.sf.refactorit.test.Utils;

import java.io.File;
import java.util.Arrays;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 *
 * @author tanel
 */
public class SourceUtilTest extends TestCase {

  byte[] content = null;

  public SourceUtilTest(java.lang.String testName) {
    super(testName);
  }

  public static void main(java.lang.String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  public static Test suite() {
    TestSuite suite = new TestSuite(SourceUtilTest.class);
    return suite;
  }

  public void setUp() throws Exception {
    File compilationUnit = new File(new File(Utils.getTestProjectsDirectory(),
        "GetCurrentBinClass"), "Test.java");
    content = FileCopier.readFileToString(compilationUnit).getBytes();
  }

  public void testNull() throws Exception {
    assertBinClass(null, 1, 5);
  }

  public void testPackage() throws Exception {
    assertBinClass(BinPackage.class, 1, 10);
    assertBinClass(BinPackage.class, 1, 16);
    assertBinClass(BinPackage.class, 4, 13);
    assertBinClass(BinPackage.class, 33, 23);
  }

  public void testCIType() throws Exception {
    assertBinClasses(new Class[] {BinClass.class, BinInterface.class}
        , 5, 26);
    assertBinClass(BinClass.class, 10, 16);
    assertBinClass(BinClass.class, 10, 37);
    assertBinClass(BinInterface.class, 10, 63);
    assertBinClasses(new Class[] {BinClass.class, BinInterface.class}
        , 12, 6);
    assertBinClasses(new Class[] {BinClass.class, BinInterface.class}
        , 20, 14);
    assertBinClasses(new Class[] {BinClass.class, BinInterface.class}
        , 24, 48);
    assertBinClasses(new Class[] {BinClass.class, BinInterface.class}
        , 26, 30);
    assertBinClasses(new Class[] {BinClass.class, BinInterface.class}
        , 31, 10);
    assertBinClasses(new Class[] {BinClass.class, BinInterface.class}
        , 31, 28);
    assertBinClass(BinClass.class, 41, 23);
    assertBinClass(BinClass.class, 42, 33);
  }

  public void testClassStaticField() throws Exception {
    // TODO: because this cannot be detected from AST tree, we expect null
    //assertBinClass(BinCIType.class, 27, 10);
    assertBinClass(null, 27, 10);
  }

  public void testMethod() throws Exception {
    assertBinClass(BinMethod.class, 20, 22);
    assertBinClass(BinMethod.class, 24, 21);
    assertBinClass(BinMethod.class, 27, 21);
    assertBinClass(BinMethod.class, 35, 22);
    assertBinClass(BinMethod.class, 36, 22);
    assertBinClass(BinMethod.class, 37, 22);
    assertBinClass(BinMethod.class, 43, 33);
    assertBinClass(BinMethod.class, 44, 50);
  }

  public void testField() throws Exception {
    assertBinClass(BinField.class, 12, 11);
    assertBinClass(BinField.class, 21, 14);
    assertBinClass(BinField.class, 45, 24);
    assertBinClass(BinField.class, 46, 12);
  }

  public void testLocalVariable() throws Exception {
    assertBinClass(BinLocalVariable.class, 25, 10);
    assertBinClass(BinLocalVariable.class, 31, 20);
    assertBinClass(BinLocalVariable.class, 35, 13);
    assertBinClass(BinLocalVariable.class, 36, 6);
    assertBinClass(BinLocalVariable.class, 41, 11);
    assertBinClass(BinLocalVariable.class, 48, 15);
    assertBinClass(BinLocalVariable.class, 48, 23);
    assertBinClass(BinLocalVariable.class, 48, 31);
    assertBinClass(BinLocalVariable.class, 48, 31);
    assertBinClass(BinLocalVariable.class, 49, 8);
  }

  public void testParameters() throws Exception {
    assertBinClass(BinParameter.class, 24, 41);
    assertBinClass(BinParameter.class, 25, 17);
    assertBinClass(BinParameter.class, 26, 14);
    assertBinClass(BinParameter.class, 27, 30);
    assertBinClass(BinParameter.class, 31, 38);
  }

  public void testUndetectable() throws Exception {
    // TODO: because this cannot be detected from AST tree, we expect null
    //assertBinClass(BinField.class, 37, 14);
    assertBinClass(null, 37, 14);

    //assertBinClass(BinParameter.class, 47, 14);
    assertBinClass(null, 47, 14);
  }

  public void testParameter() throws Exception {
    assertBinClass(BinParameter.class, 24, 39);
    assertBinClass(BinParameter.class, 24, 55);
    assertBinClass(BinParameter.class, 32, 25);
    assertBinClass(BinParameter.class, 40, 47);
  }

  public void testConstructor() throws Exception {
    assertBinClass(BinConstructor.class, 16, 12);
  }

  private void assertBinClass(Class expectedClass, int row,
      int col) throws Exception {
    if (expectedClass == null) {
      assertBinClasses(null, row, col);
    } else {
      assertBinClasses(new Class[] {expectedClass}
          , row, col);
    }
  }

  private void assertBinClasses(Class[] expectedClasses, int row,
      int col) throws Exception {
//System.out.println("Testing location " + row + ":" + col);
    String strContent = new String(content);
    String lines[] = StringUtil.split(strContent, "\n");
    Class[] actual = FastItemFinder.getCurrentBinClass(
        "Test.java", strContent, row, col);
    if (actual == expectedClasses) {
      return;
    }
    if ((actual == null && expectedClasses != null)
        || (actual != null && expectedClasses == null)
        || (actual.length != expectedClasses.length)) {
      throw new AssertionFailedError(
          "Expected "
          + (expectedClasses == null ? "null"
          : Arrays.asList(expectedClasses).toString())
          + " at pos " + col + ":\n" + lines[row - 1] + "\n"
          + "Actual: "
          + (actual == null ? "null" : Arrays.asList(actual).toString()));
    }

    for (int i = 0; i < actual.length; i++) {
      if (!actual[i].equals(expectedClasses[i])) {
        throw new AssertionFailedError(
            "Expected " + expectedClasses[i]
            + " at pos " + col + ":\n" + lines[row - 1] + "\n"
            + "Actual: " + actual[i]);
      }
    }
  }

}
