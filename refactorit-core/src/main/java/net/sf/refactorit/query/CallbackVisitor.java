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
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
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


/**
 * It calls given {@link CallbackModel} methods on visiting each item in
 * classmodel.
 *
 * @author Anton Safonov
 */
public final class CallbackVisitor extends DelegatingVisitor {
  private CallbackModel model;

  public CallbackVisitor(CallbackModel model) {
    super();
    this.model = model;
  }

  public BinItem getCurrentLocation() {
    return super.getCurrentLocation();
  }

  public BinTypeRef getCurrentType() {
    return super.getCurrentType();
  }

  public void visit(BinAnnotationExpression x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinAssertStatement x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(Project x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(CompilationUnit x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinArithmeticalExpression x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinArrayInitExpression x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinArrayUseExpression x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinLocalVariableDeclaration x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinFieldDeclaration x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinAssignmentExpression x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinBreakStatement x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinCITypesDefStatement x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinCastExpression x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinCITypeExpression x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinConditionalExpression x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinConstructorInvocationExpression x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinEmptyExpression x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinEmptyStatement x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinExpression x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinExpressionList x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinExpressionStatement x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinFieldInvocationExpression x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinForStatement x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinIfThenElseStatement x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinIncDecExpression x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinInstanceofExpression x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinLabeledStatement x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinLiteralExpression x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinLocalVariable x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinLogicalExpression x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinMethodInvocationExpression x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinNewExpression x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinPackage x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinReturnStatement x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinStatement x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinStatementList x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinStringConcatenationExpression x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinSwitchStatement x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinSwitchStatement.Case x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinSynchronizedStatement x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinThrowStatement x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinTryStatement x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinTryStatement.TryBlock x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinTryStatement.CatchClause x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinTryStatement.Finally x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinUnaryExpression x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinVariableUseExpression x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinWhileStatement x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinCIType x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinConstructor x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinField x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinInitializer x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinMethod x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinMethod.Throws x) {
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

  public void visit(BinItem x) {
    // NOTE: it can come here in e.g. MissingBinMember
    //Assert.must(false, "This should never be called on: " + x);
    this.model.callback(x);
    this.model.goDown();
    super.visit(x);
    this.model.goUp();
  }

}
