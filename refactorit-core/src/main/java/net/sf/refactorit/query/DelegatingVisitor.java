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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * This is a supervising visitor which traverses the classmodel tree and for
 * each item calls appropriate <code>visit</code> and <code>leave</code> methods
 * of registered delegates.
 *
 * @author Anton Safonov
 */
public class DelegatingVisitor extends AbstractIndexer {
  private DelegateVisitor[] delegates = new DelegateVisitor[0];
  private int delegatesNum = 0;

  public DelegatingVisitor() {
  }

  public DelegatingVisitor(final boolean skipSynthetic) {
    super(skipSynthetic);
  }

  public void registerDelegate(DelegateVisitor delegate) {
    List dels = new ArrayList(Arrays.asList(delegates));
    if (!dels.contains(delegate)) {
      dels.add(delegate);
      ++this.delegatesNum;
      delegates = (DelegateVisitor[]) dels.toArray(
          new DelegateVisitor[dels.size()]);
    }
  }

  public final void setDelegates(final DelegateVisitor[] delegates) {
    this.delegates = delegates;
    this.delegatesNum = this.delegates.length;
  }

  public final DelegateVisitor[] getDelegates() {
    return this.delegates;
  }

  // Visitors

  public void visit(final Project x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final CompilationUnit x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinArithmeticalExpression x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinArrayInitExpression x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinArrayUseExpression x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinLocalVariableDeclaration x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinFieldDeclaration x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinAssignmentExpression x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinBreakStatement x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinCITypesDefStatement x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinCastExpression x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinCITypeExpression x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinConditionalExpression x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinConstructorInvocationExpression x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinEmptyExpression x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinAnnotationExpression x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }
  
  public void visit(final BinEmptyStatement x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinAssertStatement x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinExpression x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinExpressionList x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinExpressionStatement x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinFieldInvocationExpression x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinForStatement x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinIfThenElseStatement x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinIncDecExpression x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinInstanceofExpression x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinLabeledStatement x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinLiteralExpression x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinLocalVariable x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinLogicalExpression x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinMethodInvocationExpression x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinNewExpression x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinPackage x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinReturnStatement x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinStatement x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinStatementList x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinStringConcatenationExpression x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinSwitchStatement x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinSwitchStatement.Case x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinSynchronizedStatement x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinThrowStatement x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinTryStatement x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinTryStatement.TryBlock x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinTryStatement.CatchClause x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinTryStatement.Finally x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinUnaryExpression x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinVariableUseExpression x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinWhileStatement x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinMethod.Throws x) {
    int i;
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }

  public void visit(final BinCIType x) {
    int i;

    final BinTypeRef curType = super.getCurrentType();
    final BinItem curLocation;
    if (curType != null) {
      curLocation = super.getCurrentLocation();
    } else {
      curLocation = null;
    }

    super.setCurrentType(x.getTypeRef());
    super.setCurrentLocation(null);

    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }

    // FIXME: change it if you know how to call BinItemVisitor.visit(x)
    // without calling AbstractIndexer
    x.defaultTraverse(this);

    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }

    super.setCurrentType(curType);
    super.setCurrentLocation(curLocation);
  }

  public void visit(final BinConstructor x) {
    int i;
    super.setCurrentLocation(x);

    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }

    // FIXME: change it if you know how to call BinItemVisitor.visit(x)
    // without calling AbstractIndexer
    x.defaultTraverse(this);

    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }

    super.setCurrentLocation(null);
  }

  public void visit(final BinField x) {
    int i;

    if (super.getCurrentType() == null) {
      super.setCurrentType(x.getOwner());
    }
    super.setCurrentLocation(null);

    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }

    super.setCurrentLocation(null);
  }

  public void visit(final BinInitializer x) {
    int i;
    super.setCurrentLocation(x);

    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }

    // FIXME: change it if you know how to call BinItemVisitor.visit(x)
    // without calling AbstractIndexer
    x.defaultTraverse(this);

    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }

    super.setCurrentLocation(null);
  }

  public void visit(final BinMethod x) {
    int i;
    super.setCurrentLocation(x);

    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }

    // FIXME: change it if you know how to call BinItemVisitor.visit(x)
    // without calling AbstractIndexer
    x.defaultTraverse(this);

    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }

    super.setCurrentLocation(null);
  }

  public void visit(final BinItem x) {
    int i;

    // NOTE: it can come here in e.g. MissingBinMember
    //Assert.must(false, "This should never be called!");
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].visit(x);
    }
    super.visit(x);
    for (i = 0; i < this.delegatesNum; i++) {
      delegates[i].leave(x);
    }
  }
}
