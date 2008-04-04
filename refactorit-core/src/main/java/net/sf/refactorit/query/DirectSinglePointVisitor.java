/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinExpressionList;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinInitializer;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.expressions.BinAnnotationExpression;
import net.sf.refactorit.classmodel.expressions.BinArithmeticalExpression;
import net.sf.refactorit.classmodel.expressions.BinArrayInitExpression;
import net.sf.refactorit.classmodel.expressions.BinArrayUseExpression;
import net.sf.refactorit.classmodel.expressions.BinAssignmentExpression;
import net.sf.refactorit.classmodel.expressions.BinCITypeExpression;
import net.sf.refactorit.classmodel.expressions.BinCastExpression;
import net.sf.refactorit.classmodel.expressions.BinConditionalExpression;
import net.sf.refactorit.classmodel.expressions.BinConstructorInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinEmptyExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinIncDecExpression;
import net.sf.refactorit.classmodel.expressions.BinInstanceofExpression;
import net.sf.refactorit.classmodel.expressions.BinLiteralExpression;
import net.sf.refactorit.classmodel.expressions.BinLogicalExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinNewExpression;
import net.sf.refactorit.classmodel.expressions.BinStringConcatenationExpression;
import net.sf.refactorit.classmodel.expressions.BinUnaryExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.classmodel.statements.BinAssertStatement;
import net.sf.refactorit.classmodel.statements.BinBreakStatement;
import net.sf.refactorit.classmodel.statements.BinCITypesDefStatement;
import net.sf.refactorit.classmodel.statements.BinEmptyStatement;
import net.sf.refactorit.classmodel.statements.BinExpressionStatement;
import net.sf.refactorit.classmodel.statements.BinFieldDeclaration;
import net.sf.refactorit.classmodel.statements.BinForStatement;
import net.sf.refactorit.classmodel.statements.BinIfThenElseStatement;
import net.sf.refactorit.classmodel.statements.BinLabeledStatement;
import net.sf.refactorit.classmodel.statements.BinLocalVariableDeclaration;
import net.sf.refactorit.classmodel.statements.BinReturnStatement;
import net.sf.refactorit.classmodel.statements.BinStatement;
import net.sf.refactorit.classmodel.statements.BinStatementList;
import net.sf.refactorit.classmodel.statements.BinSwitchStatement;
import net.sf.refactorit.classmodel.statements.BinSynchronizedStatement;
import net.sf.refactorit.classmodel.statements.BinThrowStatement;
import net.sf.refactorit.classmodel.statements.BinTryStatement;
import net.sf.refactorit.classmodel.statements.BinWhileStatement;


public abstract class DirectSinglePointVisitor extends BinItemVisitor {
  public abstract void doVisit(BinItem x);

  public void visit(BinAssertStatement x) {
    doVisit(x);
  }

  public void visit(BinArithmeticalExpression x) {
    doVisit(x);
  }

  public void visit(BinAnnotationExpression x) {
    doVisit(x);
  }

  public void visit(BinArrayInitExpression x) {
    doVisit(x);
  }

  public void visit(BinArrayUseExpression x) {
    doVisit(x);
  }

  public void visit(BinLocalVariableDeclaration x) {
    doVisit(x);
  }

  public void visit(BinFieldDeclaration x) {
    doVisit(x);
  }

  public void visit(BinAssignmentExpression x) {
    doVisit(x);
  }

  public void visit(BinBreakStatement x) {
    doVisit(x);
  }

  public void visit(BinCITypesDefStatement x) {
    doVisit(x);
  }

  public void visit(BinCastExpression x) {
    doVisit(x);
  }

  public void visit(BinCITypeExpression x) {
    doVisit(x);
  }

  public void visit(BinConditionalExpression x) {
    doVisit(x);
  }

  public void visit(BinConstructorInvocationExpression x) {
    doVisit(x);
  }

  public void visit(BinEmptyExpression x) {
    doVisit(x);
  }

  public void visit(BinEmptyStatement x) {
    doVisit(x);
  }

  public void visit(BinExpression x) {
    doVisit(x);
  }

  public void visit(BinExpressionList x) {
    doVisit(x);
  }

  public void visit(BinExpressionStatement x) {
    doVisit(x);
  }

  public void visit(BinFieldInvocationExpression x) {
    doVisit(x);
  }

  public void visit(BinForStatement x) {
    doVisit(x);
  }

  public void visit(BinIfThenElseStatement x) {
    doVisit(x);
  }

  public void visit(BinIncDecExpression x) {
    doVisit(x);
  }

  public void visit(BinInstanceofExpression x) {
    doVisit(x);
  }

  public void visit(BinLabeledStatement x) {
    doVisit(x);
  }

  public void visit(BinLiteralExpression x) {
    doVisit(x);
  }

  public void visit(BinLogicalExpression x) {
    doVisit(x);
  }

  public void visit(BinMethodInvocationExpression x) {
    doVisit(x);
  }

  public void visit(BinNewExpression x) {
    doVisit(x);
  }

  public void visit(BinPackage x) {
    doVisit(x);
  }

  public void visit(BinReturnStatement x) {
    doVisit(x);
  }

  public void visit(BinStatement x) {
    doVisit(x);
  }

  public void visit(BinStatementList x) {
    doVisit(x);
  }

  public void visit(BinStringConcatenationExpression x) {
    doVisit(x);
  }

  public void visit(BinSwitchStatement x) {
    doVisit(x);
  }

  public void visit(BinSwitchStatement.Case x) {
    doVisit(x);
  }

  public void visit(BinSynchronizedStatement x) {
    doVisit(x);
  }

  public void visit(BinThrowStatement x) {
    doVisit(x);
  }

  public void visit(BinTryStatement x) {
    doVisit(x);
  }

  public void visit(BinTryStatement.TryBlock x) {
    doVisit(x);
  }

  public void visit(BinTryStatement.CatchClause x) {
    doVisit(x);
  }

  public void visit(BinTryStatement.Finally x) {
    doVisit(x);
  }

  public void visit(BinUnaryExpression x) {
    doVisit(x);
  }

  public void visit(BinVariableUseExpression x) {
    doVisit(x);
  }

  public void visit(BinWhileStatement x) {
    doVisit(x);
  }

  public void visit(BinCIType x) {
    doVisit(x);
  }

  public void visit(BinConstructor x) {
    doVisit(x);
  }

  public void visit(BinField x) {
    doVisit(x);
  }

  public void visit(BinLocalVariable x) {
    doVisit(x);
  }

  public void visit(BinInitializer x) {
    doVisit(x);
  }

  public void visit(BinMethod x) {
    doVisit(x);
  }

  public void visit(BinMethod.Throws x) {
    doVisit(x);
  }

  public void visit(BinItem x) {
    // NOTE: it can come here in e.g. MissingBinMember
    //Assert.must(false, "This should never be called!");
    doVisit(x);
  }
}
