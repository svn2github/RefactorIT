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
import net.sf.refactorit.classmodel.BinType;
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
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.TreeRefactorItContext;

import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 *
 * @author  Arseni Grigorjev
 */
public class FloatEqualComparisionRule extends AuditRule {
  public static final String NAME = "float_equal_compare";

  /*
   * The goal is to find comparision of floating point
   * variables with == operator (e.g. if (float == float) {...})
   */
  public void visit(BinLogicalExpression expression) {
    
    int assignmentType = expression.getAssigmentType();
    
    // must be "==" or "!=" operator
    if (assignmentType == JavaTokenTypes.EQUAL
        || assignmentType == JavaTokenTypes.NOT_EQUAL) {
      // to store return types of left and right parts of expression
      BinPrimitiveType leftType = null, rightType = null;
      
      // get return types, if non-primitive type then null
      leftType = getPrimitiveReturnType(expression.getLeftExpression());
      rightType = getPrimitiveReturnType(expression.getRightExpression());

      if (leftType != null && rightType != null
        && (leftType.isFloatingPointType() || rightType.isFloatingPointType())){
        addViolation(new FloatEqualComparision(
            expression, assignmentType, 
            leftType.getQualifiedName(), 
            rightType.getQualifiedName()));
      }
    }
    super.visit(expression);
  }

  private static BinPrimitiveType getPrimitiveReturnType(BinExpression expr){
    try{
      BinType btype;
      btype = expr.getReturnType().getBinType();
      return btype instanceof BinPrimitiveType ? (BinPrimitiveType) btype : null;
    } catch(NullPointerException e) {
      return null;
    }
  }
}

class FloatEqualComparision extends AwkwardExpression {

  public FloatEqualComparision(BinLogicalExpression expression, 
      int assignmentType, String leftType, String rightType) {
    super(expression, "Dangerous comparision between '" 
        + leftType + "' and '" + rightType + "' with " 
        + (assignmentType == JavaTokenTypes.EQUAL ? "'=='" : "'!='") 
        + " operator.", "refact.audit.float_equal_compare");
  }

  public BinMember getSpecificOwnerMember() {
    return getSourceConstruct().getEnclosingStatement().getParentMember();
  }

  public List getCorrectiveActions() {
    return Collections.singletonList(CorrectFloatEqualComparision.instance);
  }
}

/*
 * This corrective action replaces dangerous code "var1 == var2" with
 * Math.abs(var1-var2) < prescision
 */
class CorrectFloatEqualComparision extends MultiTargetCorrectiveAction {
  public static final CorrectFloatEqualComparision instance
      = new CorrectFloatEqualComparision();

  private String precis;

  public String getKey() {
    return "refactorit.audit.action.float_equal_compare.correct";
  }

  public String getName() {
    return "Change to more safer comparision";
  }

 /*
  * asks user to enter prescision, and then runs super.run()
  */
  public Set run(TreeRefactorItContext context, List violations) {
    precis = "0.0001"; // default value to display to the user
    String msg_part1 = "This action will change your comparision of\n" +
        "'var1 == var2' to more correct style, like\n\n" +
        "    Math.abs(var1 - var2) < precision;\n\n";
    String msg_part2 = "Please enter precision (e.g. 0.0001):";

    if (!isTestRun()) {
      while (true) {
        precis = RitDialog.showInputDialog(
            context, msg_part1 + msg_part2, precis);

        if (precis == null){
          return Collections.EMPTY_SET;
        }

        try {
          double val = Double.parseDouble(precis);

          if (val <= 0){
            msg_part2 = "You have entered a wrong number!\n " +
              "Please enter another (e.g. 0.0001):";
            continue;
          }

          if (val < 0.000001){
            precis = "" + Double.parseDouble(precis);
          }

          break;
        } catch (NumberFormatException e) {
          msg_part2 = "You have entered a wrong number!\n " +
              "Please enter another (e.g. 0.0001):";
          continue;
        }
      }
    }

    return super.run(context, violations);
  }

  protected Set process(TreeRefactorItContext context, TransformationManager manager, RuleViolation violation) {
    if (!(violation instanceof FloatEqualComparision)) {
      return Collections.EMPTY_SET; // foreign violation - do nothing
    }

    BinSourceConstruct srcConstr =
        ((AwkwardSourceConstruct) violation).getSourceConstruct();

    CompilationUnit compilationUnit = violation.getCompilationUnit();

    // erase old line
    StringEraser eraser = new StringEraser(
        compilationUnit, violation.getAst().getParent(), true);
    eraser.setRemoveLinesContainingOnlyComments(true);
    manager.add(eraser);

    // create correct line
    BinExpression leftExpr =
        ((BinLogicalExpression) srcConstr).getLeftExpression();
    BinExpression rightExpr =
        ((BinLogicalExpression) srcConstr).getRightExpression();

    String leftText = leftExpr.getText();
    String rightText = rightExpr.getText();
    if (needBraces(leftExpr)){
      leftText = "(" + leftText + ")";
    }
    if (needBraces(rightExpr)){
      rightText = "(" + rightText + ")";
    }

    String space = (FormatSettings.isUseSpacesNearBraces() ? " " : "");
    String correctLine = "Math.abs(" + space + leftText + " - " + rightText +
      space + ") < " + precis;

    // insert line
    StringInserter inserter = new StringInserter(
        compilationUnit,
        srcConstr.getStartLine(),
        srcConstr.getStartColumn(),
        correctLine);
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
