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
 * This visitor is intended to be registered in
 * {@link DelegatingVisitor#registerDelegate(DelegateVisitor)}.<br>
 * Then it's <code>visit(x)</code> and <code>leave</code> methods will be called
 * by that supervising visitor on traversal of classmodel tree.<br>
 *
 * @author Anton Safonov
 */
public class DelegateVisitor {

  public void visit(Project x) {
  }

  public void visit(CompilationUnit x) {
  }

  public void visit(BinArithmeticalExpression x) {
  }

  public void visit(BinArrayInitExpression x) {
  }

  public void visit(BinArrayUseExpression x) {
  }

  public void visit(BinLocalVariableDeclaration x) {
  }

  public void visit(BinFieldDeclaration x) {
  }

  public void visit(BinAssignmentExpression x) {
  }

  public void visit(BinBreakStatement x) {
  }

  public void visit(BinCITypesDefStatement x) {
  }

  public void visit(BinCastExpression x) {
  }

  public void visit(BinCITypeExpression x) {
  }

  public void visit(BinConditionalExpression x) {
  }

  public void visit(BinConstructorInvocationExpression x) {
  }

  public void visit(BinEmptyExpression x) {
  }

  public void visit(BinEmptyStatement x) {
  }

  public void visit(BinExpression x) {
  }

  public void visit(BinExpressionList x) {
  }

  public void visit(BinExpressionStatement x) {
  }

  public void visit(BinFieldInvocationExpression x) {
  }

  public void visit(BinForStatement x) {
  }

  public void visit(BinIfThenElseStatement x) {
  }

  public void visit(BinIncDecExpression x) {
  }

  public void visit(BinInstanceofExpression x) {
  }

  public void visit(BinLabeledStatement x) {
  }

  public void visit(BinLiteralExpression x) {
  }

  public void visit(BinLocalVariable x) {
  }

  public void visit(BinLogicalExpression x) {
  }

  public void visit(BinMethodInvocationExpression x) {
  }

  public void visit(BinNewExpression x) {
  }

  public void visit(BinAnnotationExpression x) {
  }

  public void visit(BinPackage x) {
  }

  public void visit(BinReturnStatement x) {
  }

  public void visit(BinStatement x) {
  }

  public void visit(BinStatementList x) {
  }

  public void visit(BinStringConcatenationExpression x) {
  }

  public void visit(BinSwitchStatement x) {
  }

  public void visit(BinSwitchStatement.Case x) {
  }

  public void visit(BinSynchronizedStatement x) {
  }

  public void visit(BinThrowStatement x) {
  }

  public void visit(BinTryStatement x) {
  }

  public void visit(BinTryStatement.TryBlock x) {
  }

  public void visit(BinTryStatement.CatchClause x) {
  }

  public void visit(BinTryStatement.Finally x) {
  }

  public void visit(BinUnaryExpression x) {
  }

  public void visit(BinVariableUseExpression x) {
  }

  public void visit(BinWhileStatement x) {
  }

  public void visit(BinCIType x) {
  }

  public void visit(BinConstructor x) {
  }

  public void visit(BinField x) {
  }

  public void visit(BinInitializer x) {
  }

  public void visit(BinMethod x) {
  }

  public void visit(BinMethod.Throws x) {
  }

  public void visit(BinItem x) {
  }

  public void leave(Project x) {
  }

  public void leave(CompilationUnit x) {
  }

  public void leave(BinArithmeticalExpression x) {
  }

  public void leave(BinArrayInitExpression x) {
  }

  public void leave(BinArrayUseExpression x) {
  }

  public void leave(BinLocalVariableDeclaration x) {
  }

  public void leave(BinFieldDeclaration x) {
  }

  public void leave(BinAssignmentExpression x) {
  }

  public void leave(BinBreakStatement x) {
  }

  public void leave(BinCITypesDefStatement x) {
  }

  public void leave(BinCastExpression x) {
  }

  public void leave(BinCITypeExpression x) {
  }

  public void leave(BinConditionalExpression x) {
  }

  public void leave(BinConstructorInvocationExpression x) {
  }

  public void leave(BinEmptyExpression x) {
  }

  public void leave(BinEmptyStatement x) {
  }

  public void leave(BinExpression x) {
  }

  public void leave(BinExpressionList x) {
  }

  public void leave(BinExpressionStatement x) {
  }

  public void leave(BinFieldInvocationExpression x) {
  }

  public void leave(BinAnnotationExpression x) {
  }

  public void leave(BinForStatement x) {
  }

  public void leave(BinIfThenElseStatement x) {
  }

  public void leave(BinIncDecExpression x) {
  }

  public void leave(BinInstanceofExpression x) {
  }

  public void leave(BinLabeledStatement x) {
  }

  public void leave(BinLiteralExpression x) {
  }

  public void leave(BinLocalVariable x) {
  }

  public void leave(BinLogicalExpression x) {
  }

  public void leave(BinNewExpression x) {
  }

  public void leave(BinPackage x) {
  }

  public void leave(BinReturnStatement x) {
  }

  public void leave(BinStatement x) {
  }

  public void leave(BinStatementList x) {
  }

  public void leave(BinStringConcatenationExpression x) {
  }

  public void leave(BinSwitchStatement x) {
  }

  public void leave(BinSwitchStatement.Case x) {
  }

  public void leave(BinSynchronizedStatement x) {
  }

  public void leave(BinThrowStatement x) {
  }

  public void leave(BinTryStatement x) {
  }

  public void leave(BinTryStatement.TryBlock x) {
  }

  public void leave(BinTryStatement.CatchClause x) {
  }

  public void leave(BinTryStatement.Finally x) {
  }

  public void leave(BinUnaryExpression x) {
  }

  public void leave(BinVariableUseExpression x) {
  }

  public void leave(BinWhileStatement x) {
  }

  public void leave(BinCIType x) {
  }

  public void leave(BinConstructor x) {
  }

  public void leave(BinField x) {
  }

  public void leave(BinInitializer x) {
  }

  public void leave(BinMethod x) {
  }

  public void leave(BinMethod.Throws x) {
  }

  public void leave(BinItem x) {
  }

}
