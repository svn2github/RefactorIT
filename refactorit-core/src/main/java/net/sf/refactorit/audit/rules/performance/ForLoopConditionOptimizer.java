/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.performance;

import net.sf.refactorit.audit.AuditRule;
import net.sf.refactorit.audit.AwkwardExpression;
import net.sf.refactorit.audit.CorrectiveAction;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinSelection;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinLogicalExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.statements.BinForStatement;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.refactorings.introducetemp.IntroduceTemp;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;
import net.sf.refactorit.ui.module.TreeRefactorItContext;
import net.sf.refactorit.ui.module.introducetemp.IntroduceTempAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



public class ForLoopConditionOptimizer extends AuditRule {
  public static final String NAME = "optimize_for_condition";

  private Set methodsToCheck;
  private HashSet cache = new HashSet();

  public void init() {
    methodsToCheck = new HashSet();

    //FIXME: if above are not present in classpath,
    //  so it is probably the J2ME where is no Collection interface,
    // need check & add standart collections under J2ME
    try {
      methodsToCheck.add(getProject().getTypeRefForName("java.util.Collection")
          .getBinCIType().getDeclaredMethod("size", BinTypeRef.NO_TYPEREFS));
    } catch (NullPointerException e) {}

    try {
      methodsToCheck.add(getProject().getTypeRefForName("java.util.Map")
          .getBinCIType().getDeclaredMethod("size", BinTypeRef.NO_TYPEREFS));
    } catch (NullPointerException e) {}

    try {
      methodsToCheck.add(getProject().getTypeRefForName(
          "java.lang.CharSequence")
          .getBinCIType().getDeclaredMethod("length", BinTypeRef.NO_TYPEREFS));
    } catch (NullPointerException e) {}

    try {
      methodsToCheck.add(getProject().getTypeRefForName(
          "java.util.Hashtable")
          .getBinCIType().getDeclaredMethod("size", BinTypeRef.NO_TYPEREFS));
    } catch (NullPointerException e) {}

    try {
      methodsToCheck.add(getProject().getTypeRefForName(
          "java.util.Vector")
          .getBinCIType().getDeclaredMethod("size", BinTypeRef.NO_TYPEREFS));
    } catch (NullPointerException e) {}


  }

  public void visit(BinForStatement x) {

    BinExpression condExpr = x.getCondition();
    if (condExpr != null && condExpr instanceof BinLogicalExpression) {
      BinLogicalExpression logicalExpr = (BinLogicalExpression) condExpr;

      BinMethodInvocationExpression[] inv =
          findAllMethodsInvocInLogicalExpr(logicalExpr);

      for (int i = 0; i < inv.length; i++) {
        if (isSuitableForOptimization(x, inv[i])) {
          addViolation(new ForLoopPerformance(inv[i]));
        }
      }
    }

    super.visit(x);
  }

  private BinMethodInvocationExpression[] findAllMethodsInvocInLogicalExpr(
      final BinLogicalExpression logicalExpr) {
    MethodInvVisitor visitor = new MethodInvVisitor();
    logicalExpr.accept(visitor);

    return visitor.getResult();
  }

  private boolean contain(BinMethod method) {
    List tops = method.getTopMethods();

    if (cache.contains(method)) {
      return true;
    }

    for (int i = 0, size = tops.size(); i < size; i++) {
      if (methodsToCheck.contains(tops.get(i))) {
        cache.add(method);
        return true;
      }
    }
    return false;
  }

  private boolean isSuitableForOptimization(final BinForStatement fSt,
      final BinMethodInvocationExpression expr) {

    // method should be from Collection, Map or CharSequence (size or length)
    // should return int 
    
    if (contain(expr.getMethod())) {
      return true;
      /**
       * FIXED: Oleg
       * DATE: 04.08.2005
       */
      /*SourceConstruct initStatement = fSt.getInitSourceConstruct();
      if (initStatement == null) {
        return true;
      }

      if (initStatement instanceof BinLocalVariableDeclaration) {
        BinLocalVariableDeclaration lVar =
            (BinLocalVariableDeclaration) initStatement;

        if (lVar.getVariables()[0].getTypeRef() == expr.getReturnType()) {
          return true;
        }
      }*/
    }

    return false;
  }

  private class MethodInvVisitor extends BinItemVisitor {
    private ArrayList result = new ArrayList();

    public void visit(BinMethodInvocationExpression x) {
      result.add(x);
      super.visit(x);
    }

    public BinMethodInvocationExpression[] getResult() {
      return (BinMethodInvocationExpression[])this.result.toArray(new
          BinMethodInvocationExpression[0]);
    }
  }
}

class ForLoopPerformance extends AwkwardExpression {
  public ForLoopPerformance(BinExpression exp) {
    super(exp, "It would be better to replace each method call on variable", null);
  }

  public List getCorrectiveActions() {
    return Collections.singletonList(OptimizeAction.INSTANCE);
  }
}

class OptimizeAction extends CorrectiveAction {
  public static OptimizeAction INSTANCE = new OptimizeAction();

  public String getName() {
    return "Replace method invocation on variable";
  }

  public String getKey() {
    return "refactorit.audit.action.forloopcondition.optimize";
  }

  public boolean isMultiTargetsSupported() {
    return false;
  }

  public Set run(final TreeRefactorItContext context, List violations) {
    if (!(violations.get(0) instanceof ForLoopPerformance)) {
      return Collections.EMPTY_SET;
    }

    BinExpression expr = (BinExpression) ((ForLoopPerformance) violations.get(0)).
        getSourceConstruct();

    final BinSelection selection = new BinSelection(expr.getCompilationUnit(),
        expr.getText(),
        expr.getStartPosition(), expr.getEndPosition());

    RefactorItAction introduce;

    if(!isTestRun()) {
      introduce = ModuleManager.getAction(selection.getClass(),
          IntroduceTempAction.KEY);

    } else {
      introduce = new IntroduceTempAction() {
        public boolean showIntroduceTempDialog(IdeWindowContext context,
            IntroduceTemp extractor) {

          String defaultName = "max";
          String name = defaultName;
          int c = 0;
          do{

            extractor.setNewVarName(name);
            extractor.setReplaceAll(false);
            extractor.setDeclareFinal(false);
            extractor.setDeclareInForStatement(true);

            if (!extractor.checkUserInput().isOk()) {
              name = defaultName + c;
              c++;
              if(c < 20) {
                continue;
              } else {
                throw new java.lang.UnsupportedOperationException(
                    "Should never come there");
              }
            } else {
              break;
            }

          }while(true);

          return true;
        }
      };

    }

    if (introduce.run(context, selection)) {
      return Collections.singleton(expr.getCompilationUnit());
    } else {
      return Collections.EMPTY_SET;
    }

  }
}
