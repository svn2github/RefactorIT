/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.j2se5;


import net.sf.refactorit.audit.AwkwardSourceConstruct;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinSourceConstruct;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.statements.BinForStatement;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.refactorings.LocalVariableDuplicatesFinder;
import net.sf.refactorit.refactorings.NameUtil;
import net.sf.refactorit.source.SourceCoordinate;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.source.format.BinLocalVariableFormatter;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.module.TreeRefactorItContext;

import java.util.Collections;
import java.util.Set;

/**
 * @author Juri Reinsalu
 */
public class ForinForIteratorCorrectiveAction extends ForinIteratorCorrectiveAction {
  private static ForinForIteratorCorrectiveAction instance;

  public static ForinForIteratorCorrectiveAction getInstance() {
    if(instance==null) {
      instance=new ForinForIteratorCorrectiveAction();
    }
    return instance;
  }
  protected Set process(TreeRefactorItContext context, TransformationManager manager, RuleViolation violation) {
    if (!(violation instanceof ForinForIteratorViolation)) {
      return Collections.EMPTY_SET; // foreign violation - do nothing
    }
    ForinForIteratorViolation forinViolation = (ForinForIteratorViolation) violation;
    CompilationUnit compilationUnit = violation.getCompilationUnit();

    BinSourceConstruct srcConstr = ((AwkwardSourceConstruct) violation)
            .getSourceConstruct();
    BinForStatement forStatement = (BinForStatement) srcConstr;
    eraseForDeclarationInternals(manager, compilationUnit, forStatement);
    //arrayType.g
    BinTypeRef objectRef=srcConstr.getParentMember().getProject().getTypeRefForName("java.lang.Object");
    
    BinMethodInvocationExpression nextCallExpr=forinViolation.getNextCallExpression();   
    String itemVarName;
    if (isLocalVariableDeclarationPart(nextCallExpr)) {
      itemVarName=((BinLocalVariable)nextCallExpr.getParent()).getName();
      removeItemVarDeclaration(manager, compilationUnit, nextCallExpr);
    } else {
      itemVarName= createItemVarName(forinViolation);
      substituteNextCallWithItemVar(manager, compilationUnit, itemVarName, nextCallExpr);
    }
    createForinInit(manager,objectRef , forinViolation, 
            forStatement, itemVarName);
    return Collections.singleton(compilationUnit);
  }

  /**
   * @param manager
   * @param compilationUnit
   * @param forStatement
   */
  private void eraseForDeclarationInternals(TransformationManager manager, CompilationUnit compilationUnit, BinForStatement forStatement) {
    int endPosition = getNextSemicolonPosition(compilationUnit,forStatement.getCondition().getEndPosition());
    StringEraser eraser = new StringEraser(compilationUnit, forStatement
            .getInitSourceConstruct().getStartPosition(), endPosition);
    manager.add(eraser);
  }

  private int getNextSemicolonPosition(CompilationUnit compilationUnit,int endPosition) {
    String content=compilationUnit.getContent();
    while(endPosition<content.length() && (content.charAt(endPosition)!=';' || Comment.findAt(compilationUnit,endPosition)!=null)) {
      endPosition++;
    }
    return ++endPosition;
  }

  /**
   * @param forinViolation
   * @param compilationUnit
   * @param forStatement
   * @return
   */
  private void createForinInit(TransformationManager manager,
          BinTypeRef itemTypeRef, ForinForIteratorViolation forinViolation,
          BinForStatement forStatement, String itemVarName) {
    final SourceCoordinate insertAt = new SourceCoordinate(forStatement
            .getInitSourceConstruct().getStartLine(), forStatement
            .getInitSourceConstruct().getStartColumn());

    BinLocalVariable local = new BinLocalVariable(itemVarName, itemTypeRef, 0);
//    local.setExpression(forinViolation.getArrayUses()[0].getArrayExpression());

    String newVar = ((BinLocalVariableFormatter)local.getFormatter()).formHeader() + " : "
            + forinViolation.getIterableExpression().getText();

    StringInserter inserter = new StringInserter(forStatement
            .getCompilationUnit(), insertAt, newVar);
    manager.add(inserter);
  }

  
  /**
   * @param forinViolation
   * @return
   */
  private String createItemVarName(ForinForIteratorViolation forinViolation) {
    String conveinientVarName = NameUtil
            .extractConvenientVariableNameForType(forinViolation
                    .getIterableVariable().getTypeRef())+"Item";
    return createNonConflictingName(forinViolation, conveinientVarName);
  }

  /**
   * if conflicts occur, tries the same name with a concatenated integer 1, if
   * still not ok, then 2,3,... and so on
   * 
   * @param forinViolation
   * @param conveinientVarName
   * @return
   */
  private String createNonConflictingName(ForinForIteratorViolation forinViolation,
          String conveinientVarName) {
    int incrementBy = 0;
    LocalVariableDuplicatesFinder duplicatesFinder;
    String newName;
    do {
      incrementBy++;
      if (incrementBy > 1)
        newName = conveinientVarName + incrementBy;
      else
        newName = conveinientVarName;
      duplicatesFinder = new LocalVariableDuplicatesFinder(null, newName,
              ((BinForStatement) forinViolation.getSourceConstruct())
                      .getInitSourceConstruct());
      forinViolation.getSourceConstruct().getParentMember().defaultTraverse(
              duplicatesFinder);
    } while (duplicatesFinder.getDuplicates().size() > 0);
    return newName;
  }

  public String getKey() {
    return "refactorit.audit.action.forin.introduce.from.for.iterator";
  }

  public String getName() {
    return "Introduce jdk5.0 for/in construct (for-loops iterator traversal)";
  }

}
