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
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.TreeRefactorItContext;
import net.sf.refactorit.utils.MethodInvocationUtils;

import javax.swing.JOptionPane;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Arseni Grigorjev
 */
public class RedundantUnboxingRule extends J2Se5AuditRule {
  public static final String NAME = "redundant_unboxing";

  private Map unboxingTypes = new HashMap();
  private String[] classNumberMethods = { "byteValue", "doubleValue",
      "floatValue", "intValue", "longValue", "shortValue" };

  public void init(){
    final Project project = getProject();
    registerWrapperType(project, "java.lang.Integer"  , "intValue");
    registerWrapperType(project, "java.lang.Long"     , "longValue");
    registerWrapperType(project, "java.lang.Short"    , "shortValue");
    registerWrapperType(project, "java.lang.Byte"     , "byteValue");
    registerWrapperType(project, "java.lang.Float"    , "floatValue");
    registerWrapperType(project, "java.lang.Double"   , "doubleValue");

    registerWrapperType(project, "java.lang.Boolean"  , "booleanValue");
    registerWrapperType(project, "java.lang.Character", "charValue");
  }

  private void registerWrapperType(final Project project, final String typeName,
      final String methodName) {
    unboxingTypes.put(project.getTypeRefForName(typeName), methodName);
  }

  public void visit(final BinMethodInvocationExpression expr){
    String methodName = (String) unboxingTypes.get(expr.getInvokedOn());
    if (methodName != null){
      // is it Integer.intValue() ?
      boolean unboxingToBoxedType = expr.getMethod().getName().equals(
          methodName);
      // is it Integer.floatValue() ?
      boolean unboxingToOtherType = Arrays.binarySearch(classNumberMethods,
          expr.getMethod().getName()) >= 0 && !unboxingToBoxedType;

      if ((unboxingToBoxedType || unboxingToOtherType)
          && expr.getMethod().getParameters().length == 0){
        if (MethodInvocationUtils.confusesMethodResolution(expr,
            expr.getInvokedOn())){
          addViolation(new ProbablyRedundantUnboxing(expr,
              ProbablyRedundantUnboxing.METHOD_RESOLUTION));
        } else if (unboxingToOtherType){
          addViolation(new ProbablyRedundantUnboxing(expr,
              ProbablyRedundantUnboxing.PRECISION_LOSS));
        } else {
          addViolation(new RedundantUnboxing(expr));
        }
      }
    }
    super.visit(expr);
  }
}

class RedundantUnboxing extends AwkwardExpression {
  RedundantUnboxing(final BinExpression expression){
    super(expression, "Redundant unboxing", "refact.audit.redundant_unboxing");
  }

  public List getCorrectiveActions(){
    return Collections.singletonList(RemoveUnboxingAction.INSTANCE);
  }

  public BinMember getSpecificOwnerMember(){
    return getSourceConstruct().getParentMember();
  }
}

class ProbablyRedundantUnboxing extends AwkwardExpression {
  public static final String METHOD_RESOLUTION
      = "Removing will confuse method overload resolution!";
  public static final String PRECISION_LOSS
      = "Removing may cause precision loss!";

  private final String warningMessage;

  ProbablyRedundantUnboxing(final BinExpression expression,
      final String warningMessage){
    super(expression, "Probably a redundant unboxing: "
        + warningMessage, "refact.audit.redundant_unboxing");
    this.warningMessage = warningMessage;
  }

  public final String getWarningMessage(){
    return this.warningMessage;
  }

  public final List getCorrectiveActions(){
    return Collections.singletonList(UnsafeRemoveUnboxingAction.INSTANCE);
  }

  public final BinMember getSpecificOwnerMember(){
    return getSourceConstruct().getParentMember();
  }
}

class RemoveUnboxingAction extends J2Se5CorrectiveAction {
  static final RemoveUnboxingAction INSTANCE = new RemoveUnboxingAction();

  public boolean isMultiTargetsSupported(){
    return true;
  }
  
  public String getKey() {
    return "refactorit.audit.action.remove_unboxing.safe";
  }

  public String getName() {
    return "Remove redundant unboxing";
  }

  public String getMultiTargetName() {
    return "Remove redundant unboxing(s)";
  }

  protected Set process(TreeRefactorItContext context, final TransformationManager manager, RuleViolation violation) {
    if (!(violation instanceof RedundantUnboxing)) {
      return Collections.EMPTY_SET; // foreign violation - do nothing
    }

    simplyRemoveUnboxing((BinMethodInvocationExpression)
        ((AwkwardSourceConstruct) violation).getSourceConstruct(), manager);

    return Collections.singleton(violation.getCompilationUnit());
  }

  public static void simplyRemoveUnboxing(
      final BinMethodInvocationExpression expr,
      final TransformationManager manager){
    String invokedOnString = expr.getExpression().getText();
    manager.add(new StringEraser(expr.getCompilationUnit(),
        expr.getCompoundAst(), false));
    manager.add(new StringInserter(expr.getCompilationUnit(),
        expr.getNameAst().getStartLine(),
        expr.getNameAst().getStartColumn()-1,
        invokedOnString));
  }
}

class UnsafeRemoveUnboxingAction extends J2Se5CorrectiveAction {
  static final UnsafeRemoveUnboxingAction INSTANCE
      = new UnsafeRemoveUnboxingAction();

  public boolean isMultiTargetsSupported(){
    return false;
  }
  
  public String getKey() {
    return "refactorit.audit.action.remove_unboxing.unsafe";
  }

  public String getName() {
    return "Remove probably redundant unboxing";
  }

  public Set run(TreeRefactorItContext context, List violations){
    RuleViolation violation = (RuleViolation) violations.get(0);
    if (!(violation instanceof ProbablyRedundantUnboxing)) {
      return Collections.EMPTY_SET; // foreign violation - do nothing
    }

    String msg = "This action will probably change functionality of Your code:"+
        "\n" + ((ProbablyRedundantUnboxing) violation).getWarningMessage();
    int userSelection = RitDialog.showConfirmDialog(context, msg, "WARNING!",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
    if (userSelection == JOptionPane.OK_OPTION){
      return super.run(context, violations);
    } else {
      return Collections.EMPTY_SET;
    }
  }
  
  protected Set process(TreeRefactorItContext context, TransformationManager manager, RuleViolation violation) {
    if (!(violation instanceof ProbablyRedundantUnboxing)) {
      return Collections.EMPTY_SET; // foreign violation - do nothing
    }
    RemoveUnboxingAction.simplyRemoveUnboxing((BinMethodInvocationExpression)
        ((AwkwardSourceConstruct) violation).getSourceConstruct(), manager);
    return Collections.singleton(violation.getCompilationUnit());
  }
  
}

