/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.rename;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinInitializer;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinTypeRefManager;
import net.sf.refactorit.classmodel.BinTypeRefVisitor;
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
import net.sf.refactorit.common.util.FastStack;
import net.sf.refactorit.common.util.graph.WeightedGraph;
import net.sf.refactorit.query.DelegateVisitor;
import net.sf.refactorit.query.DelegatingVisitor;


public class DependenciesFinder extends DelegateVisitor {
  
  private TypeRefVisitor refVisitor = new TypeRefVisitor();

  private class TypeRefVisitor extends BinTypeRefVisitor {
    // The BinItem that was discovered while visiting
    private BinItem foundItem;
    
    public TypeRefVisitor() {
      setCheckTypeSelfDeclaration(false);
      setIncludeNewExpressions(true); // will handle with a hack, check visit(BinNew..)
    }

    public void visit(final BinTypeRef typeRef) {
        if (typeRef.getNonArrayType() != null) { // null happens sometimes after parsing warnings
          setFoundItem(typeRef.getNonArrayType().getBinType());
          return; // don't go deeper, already found
        }

      super.visit(typeRef);
    }
    
    public void setFoundItem(BinItem foundItem) {
      this.foundItem = foundItem;
    }
    
    public BinItem getFoundItem() {
      return this.foundItem;
    }

    public boolean isFound() {
      return this.foundItem != null;
    }
  }
  
  
  
  public interface Rule {
    boolean isOkFor(Object o);
  }
  
  private final Rule rule;

  private FastStack nodesHistory = new FastStack();
  private DelegatingVisitor supervisor;
  
  private WeightedGraph graph = new WeightedGraph();
  
  
  public DependenciesFinder(DelegatingVisitor supervisor, Rule rule) {
    super();
    this.supervisor = supervisor;
    this.supervisor.registerDelegate(this);
    this.rule = rule;
  }
 
  

  private void checkNodeLink(BinTypeRefManager manager) {
    manager.accept(refVisitor);
    if(refVisitor.isFound()) {
      BinItem item = refVisitor.getFoundItem();
      refVisitor.setFoundItem(null);
      checkItem(item);
    }
  }
  
  private void checkItem(BinItem item) {
    nodeDetected(item);
    nodeExits(item);
  }
 
  
  private int length;
  
  private void nodeDetected(Object x) {
    if(rule.isOkFor(x)) {
      if(!nodesHistory.isEmpty()) {
        graph.add(nodesHistory.peek(), x, length + 1);
      }
      
      nodesHistory.push(x);
      
      length = 0;
    } else {
      length++;
    }
  }
    
  private void nodeExits(Object x) {
    if(rule.isOkFor(x)) {
        nodesHistory.pop();
        length = 0;
    } else {
      if(length > 0) {
        length--;
      }
    }
    
  }
  
   public void visit(Project x) {
  }
  
  public void visit(CompilationUnit x) {    
    nodesHistory.push(x.getPackage());
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
    checkItem(x.getReturnType().getBinCIType());
  }

  public void visit(BinBreakStatement x) {
  }

  public void visit(BinCITypesDefStatement x) {
  }

  public void visit(BinCastExpression x) {    
    checkNodeLink(x);
  }

  public void visit(BinCITypeExpression x) {
    checkNodeLink(x);
  }

  public void visit(BinConditionalExpression x) { 
  }

  public void visit(BinConstructorInvocationExpression x) {
    checkNodeLink(x);
  }

  public void visit(BinEmptyExpression x) {
  }

  public void visit(BinEmptyStatement x) {
  }

  public void visit(BinExpression x) {
  }

  public void visit(BinExpressionStatement x) {
  }

  public void visit(BinFieldInvocationExpression x) {
     checkItem(x.getField());
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
    nodeDetected(x);
    checkNodeLink(x);
  }

  public void visit(BinLogicalExpression x) {
  }

  public void visit(BinMethodInvocationExpression x) {    
    checkNodeLink(x);
  }

  public void visit(BinNewExpression x) {    
    checkNodeLink(x);
  }

  public void visit(BinAnnotationExpression x) {    
    checkNodeLink(x);
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
    checkNodeLink(x);
    nodeDetected(x);
  }

  public void visit(BinConstructor x) {    
    checkNodeLink(x);
    nodeDetected(x);
  }

  public void visit(BinField x) {    
    checkNodeLink(x);
    nodeDetected(x);
  }

  public void visit(BinInitializer x) {    
    nodeDetected(x);
  }

  public void visit(BinMethod x) {    
    nodeDetected(x);
    checkNodeLink(x);
  }

  public void visit(BinMethod.Throws x) {    
    checkNodeLink(x);
  }

  public void visit(BinItem x) {
  }
 
  
  
  
  public void leave(Project x) {
  }

  public void leave(CompilationUnit x) {
    nodesHistory.pop();
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
    nodeExits(x);
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
    nodeExits(x);
  }

  public void leave(BinConstructor x) {
    nodeExits(x);
  }

  public void leave(BinField x) {
    nodeExits(x);
  }

  public void leave(BinInitializer x) {
    nodeExits(x);
  }

  public void leave(BinMethod x) {
    nodeExits(x);
  }

  public void leave(BinMethod.Throws x) {
  }

  public void leave(BinItem x) {
  }

  public WeightedGraph getDependenciesGraph() {
    return graph;
  }

  
}
