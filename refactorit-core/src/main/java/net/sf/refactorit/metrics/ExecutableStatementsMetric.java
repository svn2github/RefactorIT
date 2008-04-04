/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.metrics;


import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinExpressionList;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.expressions.BinArithmeticalExpression;
import net.sf.refactorit.classmodel.expressions.BinArrayUseExpression;
import net.sf.refactorit.classmodel.expressions.BinAssignmentExpression;
import net.sf.refactorit.classmodel.expressions.BinCastExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinIncDecExpression;
import net.sf.refactorit.classmodel.expressions.BinLogicalExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinStringConcatenationExpression;
import net.sf.refactorit.classmodel.expressions.BinUnaryExpression;
import net.sf.refactorit.classmodel.statements.BinExpressionStatement;
import net.sf.refactorit.classmodel.statements.BinForStatement;
import net.sf.refactorit.classmodel.statements.BinStatement;
import net.sf.refactorit.classmodel.statements.BinStatementList;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.test.Utils;

import org.apache.log4j.Category;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Executable Statements (EXEC) metric.
 */
public class ExecutableStatementsMetric {

  /** Hidden constructor. */
  private ExecutableStatementsMetric() {}

  /**
   * Calculates executable statements (EXEC) metric for the statement.
   *
   * @param statement statement.
   *
   * @return number executable statements in the <code>statement</code>.
   */

  public static int calculate(BinStatement statement) {
    final ExecutableStatementsVisitor visitor =
        new ExecutableStatementsVisitor();
    visitor.visit(statement);
    return visitor.getMetricValue();
  }

  /**
   * Visitor counting executable statements.
   * Executable statements roughly as defined
   * <a href="http://www.iro.umontreal.ca/labs/gelo/datrix/refmanuals/metricdoc-4.1.pdf">here</a>
   */
  private static class ExecutableStatementsVisitor extends BinItemVisitor {
    /** Value of the metric. */
    private int metricValue = 0;
    /**
     * Gets value of the metric.
     *
     * @return value.
     */

    public int getMetricValue() {
      return metricValue;
    }

    private void incrementIfNeeded(BinExpression expression) {
      if (hasBlockAsParent(expression) || isPartOfFor(expression)) {
        metricValue++;
      }
    }

    private static boolean isPartOfFor(BinExpression expression) {
      BinItemVisitable parent = expression.getParent();
      if (parent instanceof BinExpressionList) {
        if (parent.getParent() instanceof BinForStatement) {
          return true;
        }
      }
      return false;
    }

    private static boolean hasBlockAsParent(BinExpression expression) {
      BinItemVisitable parent = expression.getParent();
      if (parent instanceof BinExpressionStatement) {
        if (parent.getParent() instanceof BinStatementList) {
          return true;
        }
      }
      return false;
    }

    public void visit(BinArithmeticalExpression expression) {
      incrementIfNeeded(expression);
    }

    public void visit(BinAssignmentExpression expression) {
      incrementIfNeeded(expression);
    }

    public void visit(BinArrayUseExpression expression) {
      incrementIfNeeded(expression);
    }

    public void visit(BinCastExpression expression) {
      incrementIfNeeded(expression);
    }

    public void visit(BinFieldInvocationExpression expression) {
      incrementIfNeeded(expression);
    }

    public void visit(BinIncDecExpression expression) {
      incrementIfNeeded(expression);
    }

    public void visit(BinLogicalExpression expression) {
      incrementIfNeeded(expression);
    }

    public void visit(BinMethodInvocationExpression expression) {
      incrementIfNeeded(expression);
    }

    public void visit(BinStringConcatenationExpression expression) {
      incrementIfNeeded(expression);
    }

    public void visit(BinUnaryExpression expression) {
      incrementIfNeeded(expression);
    }

  }


  /** Test driver for {@link ExecutableStatementsMetric}. */
  public static class TestDriver extends TestCase {
    /**
     * Logger instance.
     */

    private static final Category cat =
        Category.getInstance(TestDriver.class.getName());

    public TestDriver(String name) {
      super(name);
    }

    public static Test suite() {
      final TestSuite suite = new TestSuite(TestDriver.class);
      suite.setName("ExecutableStatementsMetric tests");

      return suite;
    }

    /**
     * Tests executable statements of method Test.a.
     */
    public void testMethodA() throws Exception {
      cat.info("Testing executable statements of method Test.a");

      final BinStatement a = getTestMethodBody("a");

      assertEquals("EXEC(a)", 9, ExecutableStatementsMetric.calculate(a));

      cat.info("SUCCESS");
    }

    /**
     * Gets class with test cases.
     *
     * @return class.
     */
    private BinClass getTestClass() throws Exception {
      final Project project =
          Utils.createTestRbProject(Utils.getTestProjects().getProject("EXEC"));
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

      // FIXME: here we also want all methods that a class defines a body for?
      //  what do we want here?
      //  the point of this FIXME is to replace getMethods with something
      // like getDefinedMethods because it's not very clear what should happen, if you do getMethods
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
}
