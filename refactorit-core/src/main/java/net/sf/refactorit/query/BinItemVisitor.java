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


public class BinItemVisitor {
  private final boolean skipSynthetic;

  public BinItemVisitor() {
    this.skipSynthetic = false;
  }

  public BinItemVisitor(final boolean skipSynthetic) {
    this.skipSynthetic = skipSynthetic;
  }

  public final boolean isSkipSynthetic() {
    return this.skipSynthetic;
  }

  public void visit(BinAssertStatement x) {
    x.defaultTraverse(this);
  }

  public void visit(BinArithmeticalExpression x) {
    x.defaultTraverse(this);
  }

  public void visit(BinArrayInitExpression x) {
    x.defaultTraverse(this);
  }

  public void visit(BinArrayUseExpression x) {
    x.defaultTraverse(this);
  }

  public void visit(BinLocalVariableDeclaration x) {
    x.defaultTraverse(this);
  }

  public void visit(BinFieldDeclaration x) {
    x.defaultTraverse(this);
  }

  public void visit(BinAssignmentExpression x) {
    x.defaultTraverse(this);
  }

  public void visit(BinBreakStatement x) {
    x.defaultTraverse(this);
  }

  public void visit(BinCITypesDefStatement x) {
    x.defaultTraverse(this);
  }

  public void visit(BinCastExpression x) {
    x.defaultTraverse(this);
  }

  public void visit(BinCITypeExpression x) {
    x.defaultTraverse(this);
  }
  
  public void visit(BinAnnotationExpression x) {
    x.defaultTraverse(this);
  }

  public void visit(BinConditionalExpression x) {
    x.defaultTraverse(this);
  }

  public void visit(BinConstructorInvocationExpression x) {
    x.defaultTraverse(this);
  }

  public void visit(BinEmptyExpression x) {
    x.defaultTraverse(this);
  }

  public void visit(BinEmptyStatement x) {
    x.defaultTraverse(this);
  }

  public void visit(BinExpression x) {
    x.defaultTraverse(this);
  }

  public void visit(BinExpressionList x) {
    x.defaultTraverse(this);
  }

  public void visit(BinExpressionStatement x) {
    x.defaultTraverse(this);
  }

  public void visit(BinFieldInvocationExpression x) {
    x.defaultTraverse(this);
  }

  public void visit(BinForStatement x) {
    x.defaultTraverse(this);
  }

  public void visit(BinIfThenElseStatement x) {
    x.defaultTraverse(this);
  }

  public void visit(BinIncDecExpression x) {
    x.defaultTraverse(this);
  }

  public void visit(BinInstanceofExpression x) {
    x.defaultTraverse(this);
  }

  public void visit(BinLabeledStatement x) {
    x.defaultTraverse(this);
  }

  public void visit(BinLiteralExpression x) {
    x.defaultTraverse(this);
  }

  public void visit(BinLogicalExpression x) {
    x.defaultTraverse(this);
  }

  public void visit(BinMethodInvocationExpression x) {
    x.defaultTraverse(this);
  }

  public void visit(BinNewExpression x) {
    x.defaultTraverse(this);
  }

  public void visit(BinPackage x) {
    x.defaultTraverse(this);
  }

  public void visit(BinReturnStatement x) {
    x.defaultTraverse(this);
  }

  public void visit(BinStatement x) {
    x.defaultTraverse(this);
  }

  public void visit(BinStatementList x) {
    x.defaultTraverse(this);
  }

  public void visit(BinStringConcatenationExpression x) {
    x.defaultTraverse(this);
  }

  public void visit(BinSwitchStatement x) {
    x.defaultTraverse(this);
  }

  public void visit(BinSwitchStatement.Case x) {
    x.defaultTraverse(this);
  }

  public void visit(BinSynchronizedStatement x) {
    x.defaultTraverse(this);
  }

  public void visit(BinThrowStatement x) {
    x.defaultTraverse(this);
  }

  public void visit(BinTryStatement x) {
    x.defaultTraverse(this);
  }

  public void visit(BinTryStatement.TryBlock x) {
    x.defaultTraverse(this);
  }

  public void visit(BinTryStatement.CatchClause x) {
    x.defaultTraverse(this);
  }

  public void visit(BinTryStatement.Finally x) {
    x.defaultTraverse(this);
  }

  public void visit(BinUnaryExpression x) {
    x.defaultTraverse(this);
  }

  public void visit(BinVariableUseExpression x) {
    x.defaultTraverse(this);
  }

  public void visit(BinWhileStatement x) {
    x.defaultTraverse(this);
  }

  public void visit(BinCIType x) {
    x.defaultTraverse(this);
  }

  public void visit(BinConstructor x) {
    x.defaultTraverse(this);
  }

  public void visit(BinField x) {
    x.defaultTraverse(this);
  }

  public void visit(BinLocalVariable x) {
    x.defaultTraverse(this);
  }

  public void visit(BinInitializer x) {
    x.defaultTraverse(this);
  }

  public void visit(BinMethod x) {
    x.defaultTraverse(this);
  }

  public void visit(BinMethod.Throws x) {
    x.defaultTraverse(this);
  }

  public void visit(Project x) {
    x.defaultTraverse(this);
  }

  public void visit(CompilationUnit x) {
    x.defaultTraverse(this);
  }

  public void visit(BinItem x) {
    // NOTE: it can come here in e.g. MissingBinMember
    //Assert.must(false, "This should never be called!");
    x.defaultTraverse(this);
  }
}
