/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.classmodel;


import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.expressions.BinCastExpression;
import net.sf.refactorit.classmodel.expressions.BinLiteralExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinStringConcatenationExpression;
import net.sf.refactorit.classmodel.statements.BinExpressionStatement;
import net.sf.refactorit.classmodel.statements.BinStatement;
import net.sf.refactorit.classmodel.statements.BinStatementList;
import net.sf.refactorit.test.Utils;

import org.apache.log4j.Category;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test driver for {@link BinStringConcatenationExpression}.
 */
public final class BinStringConcatenationExpressionTest extends TestCase {
  /**
   * Logger instance.
   */
  private static final Category cat =
      Category.getInstance(BinStringConcatenationExpressionTest.class.getName());

  public BinStringConcatenationExpressionTest(String name) {
    super(name);
  }

  public static final Test suite() {
    final TestSuite suite = new TestSuite(BinStringConcatenationExpressionTest.class);
    suite.setName("BinStringConcatenationExpression tests");
    return suite;
  }

  /**
   * Tests whether source tree is correctly created.
   */
  public void testTree() throws Exception {
    cat.info("Testing whether source tree is correctly created");

    final BinStatement[] statements = getTestMethodBody("main").getStatements();

    {
      // Statement:
      // System.out.println("Hello" + tmp + (13 + 15));
      final BinExpressionStatement statement =
          (BinExpressionStatement) statements[1];
      assertEquals(BinMethodInvocationExpression.class,
          statement.getExpression().getClass());

      // "Hello" + tmp + (13 + 15)
      final BinStringConcatenationExpression expression =
          (BinStringConcatenationExpression)
          ((BinMethodInvocationExpression) statement.getExpression())
          .getExpressionList().getExpressions()[0];
      assertEquals("Return type of expression",
          "java.lang.String",
          expression.getReturnType().getQualifiedName());

      final BinStringConcatenationExpression leftExpression =
          (BinStringConcatenationExpression) expression.getLeftExpression();

      assertEquals("Return type of leftExpression",
          "java.lang.String",
          leftExpression.getReturnType().getQualifiedName());
    }

    {
      // Statement:
      // System.out.println(tmp + "Hello" + (13 + 15));
      final BinExpressionStatement statement =
          (BinExpressionStatement) statements[2];
      assertEquals(BinMethodInvocationExpression.class,
          statement.getExpression().getClass());

      // tmp + "Hello" + (13 + 15)
      final BinStringConcatenationExpression expression =
          (BinStringConcatenationExpression)
          ((BinMethodInvocationExpression) statement.getExpression())
          .getExpressionList().getExpressions()[0];
      assertEquals("Return type of expression",
          "java.lang.String",
          expression.getReturnType().getQualifiedName());

      final BinStringConcatenationExpression leftExpression =
          (BinStringConcatenationExpression) expression.getLeftExpression();

      assertEquals("Return type of leftExpression",
          "java.lang.String",
          leftExpression.getReturnType().getQualifiedName());
    }

    {
      // Statement:
      // System.out.println((String) null + null);
      final BinExpressionStatement statement =
          (BinExpressionStatement) statements[3];
      assertEquals(BinMethodInvocationExpression.class,
          statement.getExpression().getClass());

      // (String) null + null
      final BinStringConcatenationExpression expression =
          (BinStringConcatenationExpression)
          ((BinMethodInvocationExpression) statement.getExpression())
          .getExpressionList().getExpressions()[0];
      assertEquals("Return type of expression",
          "java.lang.String",
          expression.getReturnType().getQualifiedName());

      final BinCastExpression leftExpression =
          (BinCastExpression) expression.getLeftExpression();
      final BinLiteralExpression rightExpression =
          (BinLiteralExpression) expression.getRightExpression();

      assertEquals("Return type of leftExpression",
          "java.lang.String",
          leftExpression.getReturnType().getQualifiedName());

      assertNull("Return type of rightExpression",
          rightExpression.getReturnType());
    }

    {
      // Statement:
      // System.out.println(null + (String) null);
      final BinExpressionStatement statement =
          (BinExpressionStatement) statements[4];
      assertEquals(BinMethodInvocationExpression.class,
          statement.getExpression().getClass());

      // null + (String) null
      final BinStringConcatenationExpression expression =
          (BinStringConcatenationExpression)
          ((BinMethodInvocationExpression) statement.getExpression())
          .getExpressionList().getExpressions()[0];
      assertEquals("Return type of expression",
          "java.lang.String",
          expression.getReturnType().getQualifiedName());

      final BinLiteralExpression leftExpression =
          (BinLiteralExpression) expression.getLeftExpression();
      final BinCastExpression rightExpression =
          (BinCastExpression) expression.getRightExpression();

      assertNull("Return type of leftExpression",
          leftExpression.getReturnType());

      assertEquals("Return type of rightExpression",
          "java.lang.String",
          rightExpression.getReturnType().getQualifiedName());
    }

    {
      // Statement:
      // System.out.println((String) null + (String) null);
      final BinExpressionStatement statement =
          (BinExpressionStatement) statements[5];
      assertEquals(BinMethodInvocationExpression.class,
          statement.getExpression().getClass());

      // (String) null + (String) null
      final BinStringConcatenationExpression expression =
          (BinStringConcatenationExpression)
          ((BinMethodInvocationExpression) statement.getExpression())
          .getExpressionList().getExpressions()[0];
      assertEquals("Return type of expression",
          "java.lang.String",
          expression.getReturnType().getQualifiedName());

      final BinCastExpression leftExpression =
          (BinCastExpression) expression.getLeftExpression();
      final BinCastExpression rightExpression =
          (BinCastExpression) expression.getRightExpression();

      assertEquals("Return type of leftExpression",
          "java.lang.String",
          leftExpression.getReturnType().getQualifiedName());

      assertEquals("Return type of rightExpression",
          "java.lang.String",
          rightExpression.getReturnType().getQualifiedName());
    }

    cat.info("SUCCESS");
  }

  /**
   * Gets class with test cases.
   *
   * @return class.
   */
  private BinClass getTestClass() throws Exception {
    final Project project =
        Utils.createTestRbProject(Utils.getTestProjects().getProject(
        "BinStringConcatenationExpression"));
    project.getProjectLoader().build();

    return (BinClass) project.getTypeRefForName("Test").getBinType();
  }

  /**
   * Gets body of method from test class by name.
   *
   * @param name method name.
   *
   * @return method body <code>null</code> if not found.
   */
  private BinStatementList getTestMethodBody(String name) throws Exception {

    final BinMethod methods[] = getTestClass().getDeclaredMethods();
    for (int i = 0; i < methods.length; i++) {
      final BinMethod method = methods[i];
      if (name.equals(method.getName())) {
        return method.getBody();
      }
    }

    return null;
  }
}
