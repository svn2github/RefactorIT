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
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.expressions.BinConditionalExpression;
import net.sf.refactorit.classmodel.expressions.BinLogicalExpression;
import net.sf.refactorit.classmodel.statements.BinForStatement;
import net.sf.refactorit.classmodel.statements.BinIfThenElseStatement;
import net.sf.refactorit.classmodel.statements.BinStatement;
import net.sf.refactorit.classmodel.statements.BinStatementList;
import net.sf.refactorit.classmodel.statements.BinSwitchStatement;
import net.sf.refactorit.classmodel.statements.BinTryStatement;
import net.sf.refactorit.classmodel.statements.BinWhileStatement;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.test.Utils;

import org.apache.log4j.Category;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * McCabe's cyclomatic complexity (CC) metric.
 */
public class CyclomaticComplexityMetric {

  /** Hidden constructor. */
  private CyclomaticComplexityMetric() {}

  /**
   * Calculates cyclomatic complexity for the statement.
   *
   * @param statement statement.
   *
   * @return cyclomatic complexity of the <code>statement</code>.
   */
  public static int calculate(BinStatement statement) {
    final CyclomaticComplexityVisitor visitor =
        new CyclomaticComplexityVisitor();
    visitor.visit(statement);
    return visitor.getMetricValue();
  }

  /**
   * Visitor counting cyclomatic complexity.
   */
  private static class CyclomaticComplexityVisitor extends BinItemVisitor {
    private int metricValue = 1;

    /**
     * Gets value of the metric.
     *
     * @return value.
     */
    public int getMetricValue() {
      return metricValue;
    }

    public void visit(BinIfThenElseStatement expression) {
      metricValue++;
      super.visit(expression);
    }

    public void visit(BinWhileStatement expression) {
      metricValue++;
      super.visit(expression);
    }

    public void visit(BinForStatement expression) {
      metricValue++;
      super.visit(expression);
    }

    public void visit(BinConditionalExpression expression) {
      metricValue++;
      super.visit(expression);
    }
    
    public void visit(BinTryStatement.CatchClause expression) {
      metricValue++;
      super.visit(expression);
    }

    public void visit(BinSwitchStatement.Case expression) {
      // Don't do +1 for default:
      if (expression.isCase()) {
        metricValue++;
      }
      super.visit(expression);
    }

    public void visit(BinLogicalExpression expression){
      int type = expression.getRootAst().getType();
      if ((type == JavaTokenTypes.LOR) || (type == JavaTokenTypes.LAND)) {
        metricValue++;
      }
      super.visit(expression);
    }
  }


  /** Test driver for {@link CyclomaticComplexityMetric}. */
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
      suite.setName("CyclomaticComplexityMetric tests");
      return suite;
    }

    /**
     * Tests complexity of method Test.a.
     */
    public void testMethodA() throws Exception {
      cat.info("Testing complexity of method Test.a");

      final BinStatement a = getTestMethodBody("a");
      assertEquals("CC(a)",
          1,
          CyclomaticComplexityMetric.calculate(a));

      cat.info("SUCCESS");
    }

    /**
     * Tests complexity of method Test.b.
     */
    public void testMethodB() throws Exception {
      cat.info("Testing complexity of method Test.b");

      final BinStatementList stats = getTestMethodBody("b");
      assertEquals("CC(b)",
          2,
          CyclomaticComplexityMetric.calculate(stats));

      assertEquals("CC(System.out.println();)",
          1,
          CyclomaticComplexityMetric.calculate(stats.getStatements()[0]));

      cat.info("SUCCESS");
    }

    /**
     * Tests complexity of method Test.c.
     */
    public void testMethodC() throws Exception {
      cat.info("Testing complexity of method Test.c");

      final BinStatement c = getTestMethodBody("c");
      assertEquals("CC(c)",
          4,
          CyclomaticComplexityMetric.calculate(c));

      cat.info("SUCCESS");
    }

    /**
     * Tests complexity of method Test.d.
     */
    public void testMethodD() throws Exception {
      cat.info("Testing complexity of method Test.d");

      final BinStatement d = getTestMethodBody("d");
      assertEquals("CC(d)",
          7,
          CyclomaticComplexityMetric.calculate(d));

      cat.info("SUCCESS");
    }

    /**
     * Tests complexity of method Test.e.
     */
    public void testMethodE() throws Exception {
      cat.info("Testing complexity of method Test.e");

      final BinStatement e = getTestMethodBody("e");
      assertEquals("CC(e)",
          5,
          CyclomaticComplexityMetric.calculate(e));

      cat.info("SUCCESS");
    }

    /**
     * Tests complexity of method Test.f.
     */
    public void testMethodF() throws Exception {
      cat.info("Testing complexity of method Test.f");

      final BinStatement f = getTestMethodBody("f");
      assertEquals("CC(f)",
          8,
          CyclomaticComplexityMetric.calculate(f));

      cat.info("SUCCESS");
    }

    /**
     * Tests complexity of method Test.g.
     */
    public void testMethodG() throws Exception {
      cat.info("Testing complexity of method Test.g");

      final BinStatement g = getTestMethodBody("g");
      assertEquals("CC(g)",
          2,
          CyclomaticComplexityMetric.calculate(g));

      cat.info("SUCCESS");
    }

    /**
     * Gets class with test cases.
     *
     * @return class.
     */
    private BinClass getTestClass() throws Exception {
      final Project project =
          Utils.createTestRbProject(Utils.getTestProjects().getProject("CC"));
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
    private BinStatementList getTestMethodBody(String name)
        throws Exception {

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
