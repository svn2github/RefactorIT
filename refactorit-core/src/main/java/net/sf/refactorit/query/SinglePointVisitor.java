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

/**
 * Override onEnter and onLeave!
 */
public abstract class SinglePointVisitor extends AbstractIndexer {

  /**
   * onEnter will be called on visiting *before* starting to traverse children
   */
  public abstract void onEnter(Object o);

  /**
   * onLeave will be called on visiting *after* starting to traverse children

   */

  public abstract void onLeave(Object o);

  public void visit(BinAssertStatement x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinArithmeticalExpression x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }
  
  public void visit(BinAnnotationExpression x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinArrayInitExpression x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinArrayUseExpression x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinLocalVariableDeclaration x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinFieldDeclaration x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinAssignmentExpression x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinBreakStatement x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinCITypesDefStatement x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinCastExpression x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinCITypeExpression x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinConditionalExpression x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinConstructorInvocationExpression x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinEmptyExpression x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinEmptyStatement x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinExpression x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinExpressionList x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinExpressionStatement x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinFieldInvocationExpression x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinForStatement x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinIfThenElseStatement x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinIncDecExpression x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinInstanceofExpression x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinLabeledStatement x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinLiteralExpression x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinLogicalExpression x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinMethodInvocationExpression x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinNewExpression x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinPackage x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinReturnStatement x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinStatement x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinStatementList x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinStringConcatenationExpression x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinSwitchStatement x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinSwitchStatement.Case x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinSynchronizedStatement x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinThrowStatement x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinTryStatement x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinTryStatement.TryBlock x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinTryStatement.CatchClause x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinTryStatement.Finally x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinUnaryExpression x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinVariableUseExpression x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinWhileStatement x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinCIType x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinConstructor x) {

    setCurrentLocation(x);

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

    setCurrentLocation(null);

  }

  public void visit(BinField x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinLocalVariable x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinInitializer x) {

    setCurrentLocation(x);

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

    setCurrentLocation(null);

  }

  public void visit(BinMethod x) {

    setCurrentLocation(x);

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

    setCurrentLocation(null);

  }

  public void visit(BinMethod.Throws x) {

    onEnter(x);

    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }

    onLeave(x);

  }

  public void visit(BinItem x) {
    // NOTE: it can come here in e.g. MissingBinMember
    //Assert.must(false, "This should never be called!");
    if (shouldVisitContentsOf(x)) {
      super.visit(x);
    }
  }

  /**
   * For overriding.
   */
  public boolean shouldVisitContentsOf(BinItem x) {
    return true;
  }
}
