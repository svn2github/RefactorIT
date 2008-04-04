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
import net.sf.refactorit.audit.AwkwardSourceConstruct;
import net.sf.refactorit.audit.MultiTargetCorrectiveAction;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.classmodel.BinSourceConstruct;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.expressions.BinArithmeticalExpression;
import net.sf.refactorit.classmodel.expressions.BinArrayUseExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinIncDecExpression;
import net.sf.refactorit.classmodel.expressions.BinLiteralExpression;
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
 * the goal is to find integer divison in floating point context
 *
 * @author  Arseni Grigorjev
 */

public class IntDivFloatContextRule extends AuditRule {
  public static final String NAME = "int_division";  
  
  public void visit(BinArithmeticalExpression expression){
    if (expression.getType() == JavaTokenTypes.DIV){
      BinPrimitiveType leftType = null, rightType = null;

      // get return types, if non-primitive type then null
      leftType = DivisionContextAnalyzer.getPrimitiveReturnType(expression
          .getLeftExpression());
      rightType = DivisionContextAnalyzer.getPrimitiveReturnType(expression
          .getRightExpression());
      
      if (leftType != null && rightType != null
          && (leftType.isIntegerType() && rightType.isIntegerType())){
        // run division context analyzer
        DivisionContextAnalyzer analyzer = new DivisionContextAnalyzer(expression);
        expression.getParent().accept(analyzer);
        if (analyzer.isFloatingPointContext()){
          addViolation(new IntDivFloatContext(analyzer.getLocationExpression(),
              analyzer.getLocationString()));
        }
      }
    }
    
    super.visit(expression);
  }
}

class IntDivFloatContext extends AwkwardExpression {
  public IntDivFloatContext(BinExpression expression, String msg) {
    super(expression, "Integer division in floating point context: in " + msg, "refact.audit.integer_division");
  }
  
  public List getCorrectiveActions() {
    return Collections.singletonList(AddCastToFloatAction.instance);
  }
  
  public BinMember getSpecificOwnerMember() {
    return getSourceConstruct().getEnclosingStatement().getParentMember();
  }
}

/*
 * this corrective action adds casts to float to upper part of
 * integer division, e.g.  b/a  -> ((float) b)/a
 */
class AddCastToFloatAction extends MultiTargetCorrectiveAction {
  public static final AddCastToFloatAction instance 
      = new AddCastToFloatAction();
    
  public String getKey() {
    return "refactorit.audit.action.int_division.add_cast";
  }
  
  public String getName() {
    return "Add cast to 'float'";
  }
  
   public String getMultiTargetName() {
    return "Add cast(s) to 'float'";
  }
  
  protected Set process(TreeRefactorItContext context, TransformationManager manager, RuleViolation violation){
    if (!(violation instanceof IntDivFloatContext)) {
      return Collections.EMPTY_SET; // foreign violation - do nothing
    }
    
    BinSourceConstruct srcConstr
        = ((AwkwardSourceConstruct) violation).getSourceConstruct();
    CompilationUnit compilationUnit = violation.getCompilationUnit();
    BinExpression leftExpr 
        = ((BinArithmeticalExpression) srcConstr).getLeftExpression();
    if (leftExpr == null) {
      return Collections.EMPTY_SET;
    }
    // erase old line
    StringEraser eraser = new StringEraser(compilationUnit, 
        leftExpr.getStartLine(), leftExpr.getStartColumn() - 1,
        leftExpr.getEndLine(), leftExpr.getEndColumn() - 1);
    eraser.setRemoveLinesContainingOnlyComments(true);
    manager.add(eraser);
    
    // create correct line
    String correctLine = leftExpr.getText();
    if (needBraces(leftExpr)){
      correctLine = "(" + correctLine + ")";
    }
    correctLine = "((float) " + correctLine + ")";
    
    // insert new line
    StringInserter inserter = new StringInserter(
        compilationUnit, 
        leftExpr.getStartLine(),
        leftExpr.getStartColumn() - 1,
        correctLine);
    manager.add(inserter);
    
    return Collections.singleton(compilationUnit);
  }
  
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
