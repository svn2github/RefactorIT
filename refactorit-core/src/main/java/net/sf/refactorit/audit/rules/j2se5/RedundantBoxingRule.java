/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.audit.rules.j2se5;

import net.sf.refactorit.audit.AwkwardExpression;
import net.sf.refactorit.audit.AwkwardSourceConstruct;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.TypeConversionRules;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinMemberInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinNewExpression;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.TreeRefactorItContext;
import net.sf.refactorit.utils.MethodInvocationUtils;

import javax.swing.JOptionPane;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Arseni Grigorjev
 */
public class RedundantBoxingRule extends J2Se5AuditRule {
  public static final String NAME = "redundant_boxing";

  private List boxingTypes = new ArrayList();

  public void init(){
    final Project project = getProject();
    boxingTypes.add(project.getTypeRefForName("java.lang.Short"));
    boxingTypes.add(project.getTypeRefForName("java.lang.Integer"));
    boxingTypes.add(project.getTypeRefForName("java.lang.Long"));
    boxingTypes.add(project.getTypeRefForName("java.lang.Short"));
    boxingTypes.add(project.getTypeRefForName("java.lang.Byte"));
    boxingTypes.add(project.getTypeRefForName("java.lang.Float"));
    boxingTypes.add(project.getTypeRefForName("java.lang.Double"));
    boxingTypes.add(project.getTypeRefForName("java.lang.Boolean"));
    boxingTypes.add(project.getTypeRefForName("java.lang.Character"));
    super.init();
  }

  public void visit(final BinNewExpression newExpr){
    if (boxingTypes.contains(newExpr.getTypeRef())
        && !(newExpr.getParent() instanceof BinMemberInvocationExpression)){
      final BinExpression[] expressions = newExpr.getExpressionList()
        .getExpressions();
      if (expressions.length == 1 && TypeConversionRules.isBoxingConversion(
          expressions[0].getReturnType(), newExpr.getTypeRef())){
        if (!MethodInvocationUtils.confusesMethodResolution(newExpr,
          expressions[0].getReturnType())){
          addViolation(new RedundantBoxing(newExpr));
        } else {
          addViolation(new ProbablyRedundantBoxing(newExpr,
              ProbablyRedundantBoxing.METHOD_RESOLUTION));
        }
      }
    }
    super.visit(newExpr);
  }
}

class RedundantBoxing extends AwkwardExpression {
  RedundantBoxing(final BinNewExpression expression){
    super(expression, "Redundant boxing", "refact.audit.redundant_boxing");
  }

  public List getCorrectiveActions(){
    return Collections.singletonList(RemoveBoxingAction.INSTANCE);
  }

  public BinMember getSpecificOwnerMember(){
    return getSourceConstruct().getParentMember();
  }
}

class ProbablyRedundantBoxing extends AwkwardExpression {
  public static final String METHOD_RESOLUTION
      = "Removing will confuse method overload resolution!";

  private final String warningMessage;

  ProbablyRedundantBoxing(final BinExpression expression,
      final String warningMessage){
    super(expression, "Probably a redundant boxing: "
        + warningMessage, "refact.audit.redundant_boxing");
    this.warningMessage = warningMessage;
  }

  public final String getWarningMessage(){
    return this.warningMessage;
  }

  public final List getCorrectiveActions(){
    return Collections.singletonList(UnsafeRemoveBoxingAction.INSTANCE);
  }

  public final BinMember getSpecificOwnerMember(){
    return getSourceConstruct().getParentMember();
  }
}

class RemoveBoxingAction extends J2Se5CorrectiveAction {
  static final RemoveBoxingAction INSTANCE = new RemoveBoxingAction();

  public boolean isMultiTargetsSupported(){
    return true;
  }
  
  public String getKey() {
    return "refactorit.audit.action.remove_boxing.safe";
  }

  public String getName() {
    return "Remove redundant boxing";
  }

  public String getMultiTargetName() {
    return "Remove redundant boxing(s)";
  }

  protected Set process(TreeRefactorItContext context, final TransformationManager manager, RuleViolation violation) {
    if (!(violation instanceof RedundantBoxing)) {
      return Collections.EMPTY_SET; // foreign violation - do nothing
    }

    simplyRemoveBoxing((BinNewExpression)
        ((AwkwardSourceConstruct) violation).getSourceConstruct(), manager);

    return Collections.singleton(violation.getCompilationUnit());
  }

  public static void simplyRemoveBoxing(
      final BinNewExpression expr,
      final TransformationManager manager){
    String exprListString = expr.getExpressionList().getExpressions()[0]
        .getText();
    manager.add(new StringEraser(expr.getCompilationUnit(),
        expr.getCompoundAst(), false));
    manager.add(new StringInserter(expr.getCompilationUnit(),
        expr.getReturnType().getNode().getStartLine(),
        expr.getReturnType().getNode().getStartColumn() - 1,
        exprListString));
  }
}

class UnsafeRemoveBoxingAction extends J2Se5CorrectiveAction {
  static final UnsafeRemoveBoxingAction INSTANCE
      = new UnsafeRemoveBoxingAction();
  
  public boolean isMultiTargetsSupported(){
    return false;
  }
  
  public String getKey() {
    return "refactorit.audit.action.remove_boxing.unsafe";
  }

  public String getName() {
    return "Remove probably redundant boxing";
  }

  public Set run(TreeRefactorItContext context, List violations){
    RuleViolation violation = (RuleViolation) violations.get(0);
    if (!(violation instanceof ProbablyRedundantBoxing)) {
      return Collections.EMPTY_SET; // foreign violation - do nothing
    }
    String msg = "This action will probably change functionality of Your code:"+
        "\n" + ((ProbablyRedundantBoxing) violation).getWarningMessage();
    int userSelection = RitDialog.showConfirmDialog(context, msg, "WARNING!",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
    if (userSelection == JOptionPane.OK_OPTION){
      return super.run(context, violations);
    } else {
      return Collections.EMPTY_SET;
    }
  }
  
  protected Set process(TreeRefactorItContext context, TransformationManager manager, RuleViolation violation){
    if (!(violation instanceof ProbablyRedundantBoxing)) {
      return Collections.EMPTY_SET; // foreign violation - do nothing
    }
    RemoveBoxingAction.simplyRemoveBoxing((BinNewExpression)
        ((AwkwardSourceConstruct) violation).getSourceConstruct(), manager);
    return Collections.singleton(violation.getCompilationUnit());
  }
  
}
