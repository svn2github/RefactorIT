/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.classmodel;



import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinExpressionList;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.test.Utils;

import org.apache.log4j.Category;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/** Test driver for {@link BinMethodInvocationExpression}. */
public final class BinMethodInvocationExpressionTest extends TestCase {

  private static final Category cat =
      Category.getInstance(BinMethodInvocationExpressionTest.class.getName());

  public BinMethodInvocationExpressionTest(final String name) {
    super(name);
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite(BinMethodInvocationExpressionTest.class);
    suite.setName("BinMethodInvocationExpression tests");
    return suite;
  }

  /**
   * Tests method {@link BinMethod#isApplicable(BinMethod)}
   */
  public final void testIsApplicable() {
    cat.info("Testing isApplicable(BinMethod)");

    final Project project = Utils.createFakeProject();

    final BinTypeRef stringRef
        = project.createCITypeRefForType(
        Utils.createClass("java.lang.String",
        project.getObjectRef().getBinCIType()));

    final BinCIType test
        = Utils.createClass("Test", stringRef.getBinCIType());

    final BinMethod method =
        new BinMethod("method",
        new BinParameter[] {new BinParameter(
        "a", stringRef, 0)}
        ,
        BinPrimitiveType.VOID_REF,
        BinModifier.PUBLIC,
        BinMethod.Throws.NO_THROWS);
    test.addDeclaredMethod(method);

    final BinMethod methodS =
        new BinMethod("method",
        new BinParameter[] {
        new BinParameter("a", test.getTypeRef(), 0)}
        ,
        BinPrimitiveType.VOID_REF,
        BinModifier.PUBLIC,
        BinMethod.Throws.NO_THROWS);
    test.addDeclaredMethod(methodS);

    final BinMethodInvocationExpression invocation
        = new BinMethodInvocationExpression(
        methodS, null, new BinExpressionList(new BinParameter[] {
        new BinParameter("1", test.getTypeRef(), 0)}),
        test.getTypeRef(),
        null
        );

    assertTrue("\"method\" has the same name as in \"method\" expression",
        method.getName().equals(invocation.getMethod().getName()));

    assertEquals(
        "\"method\" has the same number of parameters as in \"method\" expression",
        method.getParameters().length,
        invocation.getMethod().getParameters().length);

    assertTrue("\"method\" is applicable in \"method\" expression",
        invocation.getMethod().isApplicable(method));

    cat.info("SUCCESS");
  }
}
