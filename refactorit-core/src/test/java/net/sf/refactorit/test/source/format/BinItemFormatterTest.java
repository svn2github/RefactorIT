/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.source.format;

import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.expressions.BinArithmeticalExpression;
import net.sf.refactorit.classmodel.expressions.BinArrayInitExpression;
import net.sf.refactorit.classmodel.expressions.BinEmptyExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinIncDecExpression;
import net.sf.refactorit.classmodel.expressions.BinLiteralExpression;
import net.sf.refactorit.classmodel.expressions.BinUnaryExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.classmodel.statements.BinLocalVariableDeclaration;
import net.sf.refactorit.classmodel.statements.BinReturnStatement;
import net.sf.refactorit.classmodel.statements.BinStatement;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.parser.SimpleASTImpl;
import net.sf.refactorit.query.AbstractIndexer;
import net.sf.refactorit.source.format.BinItemPrinter;
import net.sf.refactorit.source.format.BinLocalVariableFormatter;
import net.sf.refactorit.source.format.BinMethodFormatter;
import net.sf.refactorit.source.format.BinReturnStatementFormatter;
import net.sf.refactorit.test.Utils;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class BinItemFormatterTest extends TestCase {

  public BinItemFormatterTest(String testName) {
    super(testName);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite(BinItemFormatterTest.class);
    return suite;
  }

  public void test_BinReturnStatementFormatter() {
    BinReturnStatement statement = new BinReturnStatement(null, null);
    BinReturnStatementFormatter formatter = new BinReturnStatementFormatter(
        statement);

    String statementWanted = "return;";
    String statementActual = formatter.print();

    assertEquals("Return statement is printed correctly. ", statementWanted,
        statementActual);
  }


  public void test_BinReturnStatementFormatterWithLiteralExpression	() {
    String literal = "10";
    BinTypeRef literalTypeRef = BinPrimitiveType.INT_REF;
    BinLiteralExpression expression = new BinLiteralExpression(literal,
        literalTypeRef, null);
    BinReturnStatement statement = new BinReturnStatement(expression, null);
    BinReturnStatementFormatter formatter = new BinReturnStatementFormatter(
        statement);

    String statementWanted = "return 10;";
    String statementActual = formatter.print();
    assertEquals("Return statement is printed correctly. ", statementWanted,
        statementActual);
  }


  public void test_BinReturnStatementFormatterWithLiteralExpression2() {
    String literal = BinLiteralExpression.THIS;
    BinTypeRef literalTypeRef = null;
    BinLiteralExpression expression = new BinLiteralExpression(literal,
        literalTypeRef, null);
    BinReturnStatement statement = new BinReturnStatement(expression, null);

    String statementWanted = "return this;";
    String statementActual = statement.getFormatter().print();
    assertEquals("Return statement is printed correctly. ", statementWanted,
        statementActual);
  }


  public void test_BinIncDecExpressionFormatterWithINC() {
    BinExpression literalExpression = new BinLiteralExpression("5", null, null);

    BinIncDecExpression incDecExpression = new BinIncDecExpression(
        literalExpression, JavaTokenTypes.INC, null);

    BinItemPrinter printer = new BinItemPrinter(incDecExpression);

    String expressionWanted = "++5";
    String expressionActual = printer.print();

    assertEquals("Inc/Dec expression is printed correctly. ", expressionWanted,
        expressionActual);
  }


  public void test_BinIncDecExpressionFormatterWithPOST_INC() {
    BinExpression literalExpression = new BinLiteralExpression("100", null, null);

    BinIncDecExpression incDecExpression = new BinIncDecExpression(
        literalExpression, JavaTokenTypes.POST_INC, null);

    BinItemPrinter printer = new BinItemPrinter(incDecExpression);

    String expressionWanted = "100++";
    String expressionActual = printer.print();

    assertEquals("Inc/Dec expression is printed correctly. ", expressionWanted,
        expressionActual);
  }


  public void test_BinIncDecExpressionFormatterWithDEC() {
    BinExpression literalExpression = new BinLiteralExpression("100", null, null);

    BinIncDecExpression incDecExpression = new BinIncDecExpression(
        literalExpression, JavaTokenTypes.DEC, null);

    BinItemPrinter printer = new BinItemPrinter(incDecExpression);

    String expressionWanted = "--100";
    String expressionActual = printer.print();

    assertEquals("Inc/Dec expression is printed correctly. ", expressionWanted,
        expressionActual);
  }


  public void test_BinIncDecExpressionFormatterWithPOST_DEC() {
    BinExpression literalExpression = new BinLiteralExpression("10", null, null);

    BinIncDecExpression incDecExpression = new BinIncDecExpression(
        literalExpression, JavaTokenTypes.POST_DEC, null);

    BinItemPrinter printer = new BinItemPrinter(incDecExpression);

    String expressionWanted = "10--";
    String expressionActual = printer.print();

    assertEquals("Inc/Dec expression is printed correctly. ", expressionWanted,
        expressionActual);
  }


  public void test_BinArithmeticalExpression() {
    SimpleASTImpl ast = new SimpleASTImpl();
    ast.setType(JavaTokenTypes.PLUS);
    ast.setText("+");
    BinLiteralExpression leftExpression = new BinLiteralExpression("0", null,
        null);
    BinLiteralExpression rightExpression = new BinLiteralExpression("9", null,
        null);
    BinArithmeticalExpression expression
        = BinArithmeticalExpression.createSynthetic(
        leftExpression, rightExpression, ast);

    // FormatSettings.isSpaceAroudBinaryOperator() shall return TRUE!
    BinItemPrinter printer = new BinItemPrinter(expression);

    String statementWanted = "0 + 9";
    String statementActual = printer.print();

    assertEquals("Return statement is printed correctly. ", statementWanted,
        statementActual);
  }


  public void test_BinVariableUseExpressionFormatter() {
    String name = "foo";
    BinTypeRef typeRef = BinPrimitiveType.INT_REF;
    System.out.println("Type:" + typeRef);
    int modifiers = BinModifier.PRIVATE;
    BinLocalVariable variable = new BinLocalVariable(name, typeRef, modifiers);

    BinVariableUseExpression useExpression = new BinVariableUseExpression(variable, null);

    BinItemPrinter printer = new BinItemPrinter(useExpression);

    String expressionWanted = "foo";
    String expressionActual = printer.print();

    assertEquals("Local Variable Use Expression is printed correctly. ", expressionWanted,
        expressionActual);
  }


  public void test_BinEmptyExpressionFormatter() {
    BinEmptyExpression expression = new BinEmptyExpression();

    BinItemPrinter printer = new BinItemPrinter(expression);

    String statementWanted = "[]";
    String statementActual = printer.print();

    assertEquals("Empty expression is printed correctly. ", statementWanted,
        statementActual);
  }


  public void test_BinUnaryExpressionFormatterWithUNARY_PLUS() {
    SimpleASTImpl ast = new SimpleASTImpl(JavaTokenTypes.UNARY_PLUS, "+");
    BinLiteralExpression expression = new BinLiteralExpression("123",
        BinPrimitiveType.INT_REF, null);
    BinUnaryExpression unaryExpression = BinUnaryExpression.createSynthetic(
        expression, ast);

    BinItemPrinter printer = new BinItemPrinter(unaryExpression);

    String statementWanted = "+123";
    String statementActual = printer.print();

    assertEquals("Unary expression is printed correctly. ", statementWanted,
        statementActual);
  }


  public void test_BinUnaryExpressionFormatterWithUNARY_MINUS() {
    SimpleASTImpl ast = new SimpleASTImpl(JavaTokenTypes.UNARY_MINUS, "-");
    BinLiteralExpression expression = new BinLiteralExpression("999",
        BinPrimitiveType.INT_REF, null);
    BinUnaryExpression unaryExpression = BinUnaryExpression.createSynthetic(
        expression, ast);

    BinItemPrinter printer = new BinItemPrinter(unaryExpression);

    String statementWanted = "-999";
    String statementActual = printer.print();

    assertEquals("Unary expression is printed correctly. ", statementWanted,
        statementActual);
  }


  public void test_BinUnaryExpressionFormatterWithLNOT() {
    SimpleASTImpl ast = new SimpleASTImpl(JavaTokenTypes.LNOT, "!");
    BinLiteralExpression expression = new BinLiteralExpression("false",
        BinPrimitiveType.BOOLEAN_REF, null);
    BinUnaryExpression unaryExpression = BinUnaryExpression.createSynthetic(
        expression, ast);

    BinItemPrinter printer = new BinItemPrinter(unaryExpression);

    String statementWanted = "!false";
    String statementActual = printer.print();

    assertEquals("Unary expression is printed correctly. ", statementWanted,
        statementActual);
  }


  public void test_BinUnaryExpressionFormatterWithBNOT() {
    SimpleASTImpl ast = new SimpleASTImpl(JavaTokenTypes.BNOT, "~");
    BinLiteralExpression expression = new BinLiteralExpression("123",
        BinPrimitiveType.INT_REF, null);
    BinUnaryExpression unaryExpression = BinUnaryExpression.createSynthetic(
        expression, ast);

    BinItemPrinter printer = new BinItemPrinter(unaryExpression);

    String statementWanted = "~123";
    String statementActual = printer.print();

    assertEquals("Unary expression is printed correctly. ", statementWanted,
        statementActual);
  }
  // ================= BAD TESTS . NEED TO BE REFACTORED =====================
  public void test_LocalVariableFormatter() {
    try {
      String content = "public class myClass { \n " +
      		"public void foo(int x1, int y2) { \n" +
      		" int zxc = 5;\n" +
      		"}\n" +
      		"}";
      Project project = Utils.createTestRbProjectFromString(content);

      class Visitor extends AbstractIndexer {
        public BinLocalVariable localVariable;
        public BinLocalVariableDeclaration declarationStatement;
        public BinMethod method;

        public void visit(BinLocalVariable x) {
          this.localVariable = x;
          super.visit(x);
        }

        public void visit(BinLocalVariableDeclaration x) {
          this.declarationStatement = x;
          super.visit(x);
        }

        public void visit(BinMethod x) {
          this.method = x;
          super.visit(x);
        }
      }

      Visitor visitor = new Visitor();

      project.accept(visitor);
      BinLocalVariableFormatter localFormatter =
        new BinLocalVariableFormatter(visitor.localVariable);

     // BinLocalVariableDeclarationFormatter declarationFormatter =
      //  new BinLocalVariableDeclarationFormatter(visitor.declarationStatement);


      BinMethodFormatter methodFormatter =
        new BinMethodFormatter(visitor.method);

      methodFormatter.print();

      assertEquals("Local Variable print is correct!", localFormatter.print(), "zxc");


    } catch (Exception e) {
      e.printStackTrace();
      assertTrue("Project must load", false);
    }
  }

  public void test_BinMethodFormatter() {
    BinTypeRef type1Ref = BinPrimitiveType.INT_REF;
    BinParameter param1 = new BinParameter("xxx", type1Ref, BinModifier.FINAL);

    BinParameter[] params = new BinParameter[1];
    params[0] = param1;

    BinMethod.Throws[] throwz = BinMethod.Throws.NO_THROWS;

    BinMethod method = new BinMethod("foo", params, BinPrimitiveType.VOID
        .getTypeRef(), BinModifier.PUBLIC, throwz, true);

    BinMethodFormatter formatter = new BinMethodFormatter(method);
    String methodWanted = StringUtil
        .printableLinebreaks("\tpublic void foo(final int xxx) {\r\n\t}\r\n");
    String methodActual = StringUtil.printableLinebreaks(formatter.print());
    assertEquals("Method formatting is equal: ", methodWanted, methodActual);

  }

  public void test_BinStatementListFormatter() {
    BinStatement[] statements = new BinStatement[1];
    statements[0] = new BinReturnStatement(null, null);

    // TODO: implement this test
    //BinStatementList statementList = new BinStatementList(statements, null);
    //BinStatementListFormatter formatter =
    //  new BinStatementListFormatter(statementList);
    //String statementListWanted = "return;";
    //assertEquals("BinStatemeList formatting is ok", )
    //System.out.println("BinStatementList: " + formatter.print());
  }

  public void test_BinArrayInitExpressionPrinting() {
    BinExpression expression1 = new BinLiteralExpression("1",
        BinPrimitiveType.INT_REF, null);
    BinExpression expression2 = new BinLiteralExpression("2",
        BinPrimitiveType.INT_REF, null);
    BinExpression expression3 = new BinLiteralExpression("3",
        BinPrimitiveType.INT_REF, null);

    BinExpression[] expressions = {expression1, expression2, expression3};

    BinArrayInitExpression expression = new BinArrayInitExpression(expressions, null);

    BinItemPrinter printer = new BinItemPrinter(expression);

    String statementWanted = "{ 1, 2, 3 }";
    String statementActual = printer.print();

    assertEquals("Unary expression is printed correctly. ", statementWanted,
        statementActual);
  }

}
