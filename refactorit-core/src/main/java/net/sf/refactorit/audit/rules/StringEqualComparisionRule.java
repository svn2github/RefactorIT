/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules;

import net.sf.refactorit.audit.AuditRule;
import net.sf.refactorit.audit.AwkwardExpression;
import net.sf.refactorit.audit.MultiTargetCorrectiveAction;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.expressions.BinArrayUseExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinIncDecExpression;
import net.sf.refactorit.classmodel.expressions.BinLiteralExpression;
import net.sf.refactorit.classmodel.expressions.BinLogicalExpression;
import net.sf.refactorit.classmodel.expressions.BinMemberInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.module.TreeRefactorItContext;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Arseni Grigorjev
 */
public class StringEqualComparisionRule extends AuditRule {
  public static final String NAME = "str_equal_compare";

  public void visit(BinLogicalExpression expression) {

    int assignmentType = expression.getAssigmentType();

    // must be "==" or "!=" operator
    if (assignmentType == JavaTokenTypes.EQUAL
        || assignmentType == JavaTokenTypes.NOT_EQUAL) {
      // to store return types of left and right parts of expression
      BinTypeRef leftType = null, rightType = null;

      // get return types, if non-primitive type then null
      try{
        leftType = expression.getLeftExpression().getReturnType();
        rightType = expression.getRightExpression().getReturnType();
      } catch (NullPointerException e){}

      // get typeRef for java.lang.String from project
      BinTypeRef stringType = expression.getParentType().getProject()
          .getTypeRefForName("java.lang.String");

      if ((leftType != null && rightType != null)
          && (leftType.equals(stringType) && rightType.equals(leftType))){
        addViolation(new StringEqualComparision(expression, assignmentType));
      }
    }
    super.visit(expression);
  }

}

class StringEqualComparision extends AwkwardExpression {
  private int assignmentType;

  public StringEqualComparision(
      BinLogicalExpression expression, int assignmentType){
    super(expression, "Two strings compared with "
        + (assignmentType == JavaTokenTypes.EQUAL ? "'=='" : "'!='")
        + " operator -- expected "
        + (assignmentType == JavaTokenTypes.EQUAL ? "" : "!")
        + "str1.equals(str2)", "refact.audit.str_equal_compare");
    this.assignmentType = assignmentType;
  }

  public BinMember getSpecificOwnerMember() {
    return getSourceConstruct().getEnclosingStatement().getParentMember();
  }

  public List getCorrectiveActions() {
    return Collections.singletonList(ChangeToEqualsAction.instance);
  }

  public int getAssignmentType() {
    return this.assignmentType;
  }
}

class ChangeToEqualsAction extends MultiTargetCorrectiveAction {
  public static final ChangeToEqualsAction instance
      = new ChangeToEqualsAction();

  public String getKey() {
    return "refactorit.audit.action.string_equal_compare.correct";
  }

  public String getName() {
    return "Replace with 'str1.equals(str2)'";
  }

  protected Set process(TreeRefactorItContext context, TransformationManager manager, RuleViolation violation){
    if (!(violation instanceof StringEqualComparision)) {
      return Collections.EMPTY_SET; // foreign violation - do nothing
    }

    BinLogicalExpression logicalExpr = (BinLogicalExpression)
        ((AwkwardExpression) violation).getSourceConstruct();

    CompilationUnit compilationUnit = logicalExpr.getCompilationUnit();

    BinExpression leftExpr = logicalExpr.getLeftExpression();
    BinExpression rightExpr = logicalExpr.getRightExpression();

    // create new line
    boolean needBraces = needBraces(leftExpr);
    int asgnType = ((StringEqualComparision) violation).getAssignmentType();

    String newLine;
    if(rightExpr instanceof BinLiteralExpression){
    	newLine = (asgnType == JavaTokenTypes.EQUAL ? "" : "!")
    	+ (needBraces ? "(" : "") + rightExpr.getText()
        + (needBraces ? ")" : "") + ".equals(" + leftExpr.getText() + ")";
    } else {
    	newLine = (asgnType == JavaTokenTypes.EQUAL ? "" : "!")
    	+ (needBraces ? "(" : "") + leftExpr.getText()
        + (needBraces ? ")" : "") + ".equals(" + rightExpr.getText() + ")";
    }


    // erase old line
    StringEraser eraser = new StringEraser(
        compilationUnit,
        logicalExpr.getStartLine(),
        logicalExpr.getStartColumn()-1,
        logicalExpr.getEndLine(),
        logicalExpr.getEndColumn()-1, true, false);
    eraser.setRemoveLinesContainingOnlyComments(true);
    manager.add(eraser);

    // insert line
    StringInserter inserter = new StringInserter(
        compilationUnit,
        logicalExpr.getStartLine(),
        logicalExpr.getStartColumn(),
        newLine);
    manager.add(inserter);

    return Collections.singleton(compilationUnit);
  }

 /**
  * determins wich expression types need to be placed
  * in braces when doing substraction
  */
  private static boolean needBraces(BinExpression expr){
    if (expr instanceof BinVariableUseExpression
      || expr instanceof BinLiteralExpression
      || expr instanceof BinMemberInvocationExpression
      || expr instanceof BinArrayUseExpression
      || expr instanceof BinIncDecExpression){
      return false;
    }
    return true;
  }
}
