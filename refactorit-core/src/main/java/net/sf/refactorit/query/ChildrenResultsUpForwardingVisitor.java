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
import net.sf.refactorit.common.util.FastStack;

import java.util.ArrayList;


/**
 * Children collect results and pass them to the parent.
 * Usage:
 * 1) Need to override the necessary visit().
 * 2) Need to use getCurrentResults() to pass any data to parent
 * 3) Need to use getTopResults() to get the result array of the parent
 */
public class ChildrenResultsUpForwardingVisitor extends AbstractIndexer {
  private FastStack stack;
  private ArrayList currentResults;

  public ChildrenResultsUpForwardingVisitor() {
    stack = new FastStack();
    currentResults = new ArrayList();
    stack.push(currentResults);
  }
  
  public void visit(BinAnnotationExpression x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinAssertStatement x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinArithmeticalExpression x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinArrayInitExpression x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinArrayUseExpression x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinLocalVariableDeclaration x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinFieldDeclaration x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinAssignmentExpression x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinBreakStatement x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinCITypesDefStatement x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinCastExpression x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinCITypeExpression x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinConditionalExpression x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinConstructorInvocationExpression x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinEmptyExpression x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinEmptyStatement x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinExpression x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinExpressionList x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinExpressionStatement x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinFieldInvocationExpression x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinForStatement x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinIfThenElseStatement x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinIncDecExpression x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinInstanceofExpression x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinLabeledStatement x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinLiteralExpression x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinLogicalExpression x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinMethodInvocationExpression x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinNewExpression x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinPackage x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinReturnStatement x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinStatement x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinStatementList x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinStringConcatenationExpression x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinSwitchStatement x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinSwitchStatement.Case x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinSynchronizedStatement x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinThrowStatement x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinTryStatement x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinTryStatement.TryBlock x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinTryStatement.CatchClause x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinTryStatement.Finally x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinUnaryExpression x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinVariableUseExpression x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinWhileStatement x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinCIType x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinConstructor x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinField x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinLocalVariable x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinInitializer x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinMethod x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinMethod.Throws x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(Project x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(CompilationUnit x) {
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public void visit(BinItem x) {
    // NOTE: it can come here in e.g. MissingBinMember
    //Assert.must(false, "This should never be called!");
    stack.push(new ArrayList());
    super.visit(x);
    currentResults = (ArrayList)stack.pop();
  }

  public ArrayList getCurrentResults() {
    return currentResults;
  }

  public ArrayList getTopResults() {
    return (ArrayList)stack.peek();
  }
}
