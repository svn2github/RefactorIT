/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.j2se5;

import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.statements.BinVariableDeclaration;
import net.sf.refactorit.query.usage.Finder;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.module.TreeRefactorItContext;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class ForinIteratorCorrectiveAction
        extends
          J2Se5CorrectiveAction {

  public boolean isMultiTargetsSupported(){
    return true;
  }
            
  protected abstract Set process(TreeRefactorItContext context,
      final TransformationManager manager, RuleViolation violation);

  /**
   * @param manager
   * @param compilationUnit
   * @param itemVarName
   * @param nextCallExpr
   */
  protected void substituteNextCallWithItemVar(TransformationManager manager,
          CompilationUnit compilationUnit, String itemVarName,
          BinMethodInvocationExpression nextCallExpr) {
    StringEraser eraser = new StringEraser(compilationUnit, nextCallExpr
            .getStartPosition(), nextCallExpr.getEndPosition());
    manager.add(eraser);
    StringInserter inserter = new StringInserter(compilationUnit, nextCallExpr
            .getStartLine(), nextCallExpr.getStartColumn(), itemVarName);
    manager.add(inserter);
  }

  /**
   * @param nextCallExpr
   * @return
   */
  protected boolean isLocalVariableDeclarationPart(
          BinMethodInvocationExpression nextCallExpr) {
    return nextCallExpr.getParent() instanceof BinLocalVariable
            && nextCallExpr.getParent().getParent() instanceof BinVariableDeclaration;
  }

  /**
   * @param manager
   * @param itemVarName
   * @param nextCallExpr
   * @param itemInvocations
   */
  protected void substituteItemInvocations(TransformationManager manager,
          String itemVarName, BinMethodInvocationExpression nextCallExpr,
          List itemInvocations) {
    StringEraser eraser;
    Iterator i = itemInvocations.iterator();
    while (i.hasNext()) {
      InvocationData itemInvocation = (InvocationData) i.next();
      itemInvocation.getWhereAst().getText();
      eraser = new StringEraser(nextCallExpr.getCompilationUnit(),
              itemInvocation.getWhereAst(), false);
      manager.add(eraser);
      StringInserter inserter = new StringInserter(nextCallExpr
              .getCompilationUnit(), itemInvocation.getWhereAst()
              .getStartLine(), itemInvocation.getWhereAst().getStartColumn(),
              itemVarName);
      manager.add(inserter);
    }
  }

  /**
   * @param manager
   * @param compilationUnit
   * @param itemVarName
   * @param nextCallExpr
   */
  protected void removeItemVarDeclaration(TransformationManager manager,
          CompilationUnit compilationUnit,
          BinMethodInvocationExpression nextCallExpr) {
    BinLocalVariable itemVar = (BinLocalVariable) nextCallExpr.getParent();
    BinVariableDeclaration itemVarDecl = (BinVariableDeclaration) itemVar
            .getParent();
    List itemInvocations = Finder.getInvocations(itemVar);
    StringEraser eraser = new StringEraser(compilationUnit, itemVarDecl
            .getRootAst(), true);
    eraser.setRemoveLinesContainingOnlyComments(true);
    eraser.setTrimTrailingSpace(true);
    manager.add(eraser);
    //    substituteItemInvocations(manager, itemVarName, nextCallExpr,
    //            itemInvocations);
  }

}
