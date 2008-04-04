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
import net.sf.refactorit.classmodel.statements.BinVariableDeclaration;
import net.sf.refactorit.classmodel.statements.BinWhileStatement;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.query.BinItemVisitor;
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
public class ForinWhileIteratorCorrectiveAction
        extends
          ForinIteratorCorrectiveAction {
  private static ForinWhileIteratorCorrectiveAction instance;

  static ForinWhileIteratorCorrectiveAction getInstance() {
    if(instance==null) {
      instance=new ForinWhileIteratorCorrectiveAction();
    }
    return instance;
  }
  protected Set process(TreeRefactorItContext context, TransformationManager manager, RuleViolation violation) {
    if (!(violation instanceof ForinWhileIteratorViolation)) {
      return Collections.EMPTY_SET; // foreign violation - do nothing
    }
    ForinWhileIteratorViolation forinViolation = (ForinWhileIteratorViolation) violation;
    CompilationUnit compilationUnit = violation.getCompilationUnit();

    BinSourceConstruct srcConstr = ((AwkwardSourceConstruct) violation)
            .getSourceConstruct();
    BinWhileStatement whileStatement = (BinWhileStatement) srcConstr;
    eraseIteratorDeclaration(manager, forinViolation.getIteratorDeclStatement());
    eraseWhileDeclarationInit(manager, compilationUnit, whileStatement);
    //arrayType.g
    BinTypeRef objectRef = srcConstr.getParentMember().getProject()
            .getTypeRefForName("java.lang.Object");

    BinMethodInvocationExpression nextCallExpr = forinViolation
            .getNextCallExpression();
    String itemVarName;
    if (isLocalVariableDeclarationPart(nextCallExpr)) {
      itemVarName=((BinLocalVariable)nextCallExpr.getParent()).getName();
      removeItemVarDeclaration(manager, compilationUnit, nextCallExpr);
    } else {
      itemVarName= createItemVarName(forinViolation);
      substituteNextCallWithItemVar(manager, compilationUnit, itemVarName, nextCallExpr);
    }
    createForinInit(manager, objectRef, forinViolation, whileStatement,
            itemVarName);
    return Collections.singleton(compilationUnit);
  }

  public class ItemVarUsagesParser extends BinItemVisitor {

  }

  /**
   * @param manager
   * @param iteratorDeclStatement
   */
  private void eraseIteratorDeclaration(TransformationManager manager,
          BinVariableDeclaration iteratorDeclStatement) {
    StringEraser eraser = new StringEraser(iteratorDeclStatement
            .getCompilationUnit(), iteratorDeclStatement.getRootAst(), true);
    eraser.setRemoveLinesContainingOnlyComments(true);
    eraser.setTrimTrailingSpace(true);
    manager.add(eraser);
  }

  /**
   * @param manager
   * @param compilationUnit
   * @param forStatement
   */
  private void eraseWhileDeclarationInit(TransformationManager manager,
          CompilationUnit compilationUnit, BinWhileStatement whileStatement) {
    StringEraser eraser = new StringEraser(compilationUnit, whileStatement
            .getStartPosition(), whileStatement.getCondition().getEndPosition());
    manager.add(eraser);
  }

  private int getNextSemicolonPosition(CompilationUnit compilationUnit,
          int endPosition) {
    String content = compilationUnit.getContent();
    while (endPosition < content.length()
            && (content.charAt(endPosition) != ';' || Comment.findAt(
                    compilationUnit, endPosition) != null)) {
      endPosition++;
    }
    return ++endPosition;
  }

  /**
   * @param forinViolation
   * @param compilationUnit
   * @param whileStatement
   * @return
   */
  private void createForinInit(TransformationManager manager,
          BinTypeRef itemTypeRef, ForinWhileIteratorViolation forinViolation,
          BinWhileStatement whileStatement, String itemVarName) {
    final SourceCoordinate insertAt = new SourceCoordinate(whileStatement
            .getStartLine(), whileStatement.getStartColumn());

    BinLocalVariable local = new BinLocalVariable(itemVarName, itemTypeRef, 0);
    //    local.setExpression(forinViolation.getArrayUses()[0].getArrayExpression());

    String newVar = ((BinLocalVariableFormatter)local.getFormatter()).formHeader() + " : "
            + forinViolation.getIterableExpression().getText();

    StringInserter inserter = new StringInserter(whileStatement
            .getCompilationUnit(), insertAt, "for(" + newVar);
    manager.add(inserter);
  }

  /**
   * @param forinViolation
   * @return
   */
  private String createItemVarName(ForinWhileIteratorViolation forinViolation) {
    String conveinientVarName = NameUtil
            .extractConvenientVariableNameForType(forinViolation
                    .getIterableExpression().getReturnType())
            + "Item";
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
  private String createNonConflictingName(
          ForinWhileIteratorViolation forinViolation, String conveinientVarName) {
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
              ((BinWhileStatement) forinViolation.getSourceConstruct())
                      .getStatementList());
      forinViolation.getSourceConstruct().getParentMember().defaultTraverse(
              duplicatesFinder);
    } while (duplicatesFinder.getDuplicates().size() > 0);
    return newName;
  }

  public String getKey() {
    return "refactorit.audit.action.forin.introduce.from.while.iterator";
  }

  public String getName() {
    return "Introduce jdk5.0 for/in construct (while-loops iterator traversal)";
  }

}
