/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.introducetemp;


import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinExpressionList;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinInterface;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.classmodel.BinSelection;
import net.sf.refactorit.classmodel.BinSourceConstruct;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.SourceConstruct;
import net.sf.refactorit.classmodel.expressions.BinArithmeticalExpression;
import net.sf.refactorit.classmodel.expressions.BinAssignmentExpression;
import net.sf.refactorit.classmodel.expressions.BinConstructorInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinLogicalExpression;
import net.sf.refactorit.classmodel.statements.BinExpressionStatement;
import net.sf.refactorit.classmodel.statements.BinForStatement;
import net.sf.refactorit.classmodel.statements.BinLocalVariableDeclaration;
import net.sf.refactorit.classmodel.statements.BinStatement;
import net.sf.refactorit.classmodel.statements.BinVariableDeclaration;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.query.SinglePointVisitor;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.SelectionAnalyzer;
import net.sf.refactorit.refactorings.extract.VariableUseAnalyzer;
import net.sf.refactorit.source.UserFriendlyError;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.module.RefactorItContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * @author Anton Safonov
 */
public class IntroduceTempAnalyzer {
  private final IdeWindowContext context;

  private final SelectionAnalyzer selectionAnalyzer;
  private final RefactoringStatus status = new RefactoringStatus();

  private BinExpression selectedExpression = null;
  private BinExpression[] extractableExpressions = null;

  private List fqnTypes = new ArrayList();

  public IntroduceTempAnalyzer(
      RefactorItContext context, BinSelection selection
  ) {
    this.context = context;
    this.selectionAnalyzer = new SelectionAnalyzer(selection);

    if (selection.getText().trim().length() == 0) {
      this.status.addEntry("Nothing selected.", RefactoringStatus.ERROR);
      return;
    }

    Project project = context.getProject();
    if ((project.getProjectLoader().getErrorCollector()).hasUserFriendlyErrors()) {
      Iterator it = (project.getProjectLoader().getErrorCollector()).getUserFriendlyErrors();
      while (it.hasNext()) {
        UserFriendlyError error = (UserFriendlyError) it.next();
        if (error.getCompilationUnit() != null && error.hasLineAndColumn()) {
          if (error.contains(selection)) {
            this.status.addEntry(
                "Project has errors within bounds of current selection!",
                RefactoringStatus.ERROR);
            return;
          }
        } else {
          this.status.addEntry(
              "Project has some errors which location can not be correctly identified!",
              RefactoringStatus.ERROR);
          return;
        }
      }
    }

    if (this.selectionAnalyzer.getRangeMember() == null) {
      this.status.addEntry("Can not analyze selection", RefactoringStatus.ERROR);
      return;
    }

    //<FIX>
    BinItemVisitable parent = selectionAnalyzer.getRangeMember().getParent();
    while (parent != null) {

      if (parent instanceof BinClass) {
        break;
      }

      if (parent instanceof BinInterface) {
        this.status.addEntry
            ("Cannot operate within interface.", RefactoringStatus.ERROR);
        return;
      }
      parent = parent.getParent();
    }
    //</FIX>

    if (this.selectionAnalyzer.getRangeMember() instanceof BinField
        || (this.selectionAnalyzer.getRangeMember() instanceof BinConstructor
        && ((BinConstructor)this.selectionAnalyzer.getRangeMember()).
        isSynthetic())) {
      this.status.addEntry(
          "Expected to operate within a method, constructor or initializer",
          RefactoringStatus.ERROR);
      return;
    }

    if (getExtractableExpressions().length == 0) {
      this.status.addEntry("No any valid expression to replace",
          RefactoringStatus.ERROR);
      return;
    }

    BinTypeRef returnType = this.selectedExpression.getReturnType();
    if ((returnType != null) && (returnType.equals(BinPrimitiveType.VOID_REF))) {
      this.status.addEntry("Can't extract an expression returning void",
          RefactoringStatus.ERROR);
      return;
    }

    if (isParentConstructorInvocation()) {
      this.status.addEntry("Can't extract from a constructor invocation",
          RefactoringStatus.ERROR);
      return;
    }

    if (getTopLimit(this.selectedExpression).isAfter(this.selectedExpression)) {
      this.status.addEntry("Uses variables just defined",
          RefactoringStatus.ERROR);
      return;
    }

    if (!this.selectionAnalyzer.isBracketsBalanced()) {
      this.status.addEntry("Selection contains wrong number of brackets",
          RefactoringStatus.ERROR);
      return;
    }

    if (!this.selectionAnalyzer.isAllChildrenSelected(this.selectedExpression)) {
      this.status.addEntry(
          "Not all children of selected expression were selected also",
          RefactoringStatus.ERROR);
      return;
    }

    if (isForInitializer()) {
      this.status.addEntry("Can't extract \"for\" loop initializer or updater",
          RefactoringStatus.ERROR);
      return;
    }

    String partlySelected = this.selectionAnalyzer.getPartlyInRangeConstructs();
    while (partlySelected.startsWith("(") && partlySelected.endsWith(")")) {
      partlySelected = partlySelected.substring(1,
          partlySelected.length() - 1).trim();
    }
    if (partlySelected.length() > 0) {
      if (Assert.enabled) {
        System.err.println("partlySelected: \"" + partlySelected + "\"");
      }
      this.status.addEntry(
          "There is partly selected constructs in the current selection",
          RefactoringStatus.ERROR);
      return;
    }

    if (!analyzeVarUsages(getExtractableExpressions())) {
      this.status.addEntry(
          "The programm functionality will change because of variable usages selected",
          RefactoringStatus.ERROR);
      return;
    }
  }

  private boolean analyzeVarUsages(BinExpression[] exprs) {
    final List result = new ArrayList();

    for (int k = 0, maxExpr = exprs.length; k < maxExpr; k++) {
      final VariableUseAnalyzer varAnalyzer = new VariableUseAnalyzer(
          context, getRangeMember(), CollectionUtil.singletonArrayList(exprs[k]));

      boolean add = true;
      final BinLocalVariable[] allVars = varAnalyzer.getAllVariables();
      for (int i = 0; i < allVars.length; ++i) {
        final VariableUseAnalyzer.VarInfo info = varAnalyzer.getVarInfo(allVars[
            i]);

        if (info.changedInside && info.usedAfter
            && info.changesBeforeUseAfter != VariableUseAnalyzer.VarInfo.YES
            && k > 0) {
          add = false;
        }

        if (info.usedInside && info.changedBeforeInSameStatement) {
        	add = false;
        }
        if (info.changedInside && info.usedBeforeInSameStatement) {
        	add = false;
        }

      }

      if (add) {
        this.fqnTypes.addAll(varAnalyzer.getFqnTypes());
        result.add(exprs[k]);
      }
    }

    this.extractableExpressions
        = (BinExpression[]) result.toArray(new BinExpression[result.size()]);

    return result.size() > 0;
  }

  public BinExpression[] getExtractableExpressions() {
    if (this.extractableExpressions == null) {
      this.selectedExpression = this.selectionAnalyzer.findTopExpression();
      List exprs = new ArrayList();
      if (this.selectedExpression != null) {
        exprs.add(this.selectedExpression);

        final List visitables = new ArrayList();
        visitables.add(getTopLimit(this.selectedExpression));
        visitables.addAll(((BinStatement) visitables.get(0)).getSiblings());

        exprs.addAll(findSameExpressions(this.selectedExpression, visitables));
        exprs = filterOutStandaloneExpressions(exprs);
      }
      this.extractableExpressions = (BinExpression[]) exprs.toArray(
          new BinExpression[exprs.size()]);
    }

    return this.extractableExpressions;
  }

  public BinExpression getSelectedExpression() {
    return this.selectedExpression;
  }

  private List filterOutStandaloneExpressions(final List exprs) {
    Collections.sort(exprs, BinMember.PositionSorter.getInstance());

    final boolean preserveSelected
        = this.selectedExpression.getParent() instanceof BinExpressionStatement;

    final Iterator it = exprs.iterator();
    boolean beforeSelected = true;
    boolean first = true;
    while (it.hasNext()) {
      final BinExpression expr = (BinExpression) it.next();

      if (expr == this.selectedExpression) {
        beforeSelected = false;
      } else {
        final boolean standaloneExpr = expr.getParent()
            instanceof BinExpressionStatement;

        if (preserveSelected) {
          if (standaloneExpr || beforeSelected) {
            it.remove();
          }
        } else {
          // we can reuse only first standalone expression
          if (standaloneExpr && !first) {
            it.remove();
          }
        }
      }

      first = false;
    }

    return exprs;
  }

  public BinMember getRangeMember() {
    return this.selectionAnalyzer.getRangeMember();
  }

  public RefactoringStatus getStatus() {
    return this.status;
  }

  private List findSameExpressions(final BinExpression top,
      final List visitables) {
    final List same = new ArrayList();

    class SameFinder extends SinglePointVisitor {
      public void onEnter(Object x) {
        if (x != top && x.getClass().equals(top.getClass())) {
          if (x instanceof BinArithmeticalExpression) {
            morphXTowardTop((BinArithmeticalExpression) x,
                (BinArithmeticalExpression) top);
          }

          if (top.isSame((BinItem) x)) {
            // not assigned to?
            if (!(((BinSourceConstruct) x).getParent() instanceof
                BinAssignmentExpression
                && ((BinAssignmentExpression) ((BinSourceConstruct) x).
                getParent())
                .getLeftExpression() == x)) {
              same.add(x);
            }
          }
        }
      }

      private void morphXTowardTop(final BinArithmeticalExpression x,
          final BinArithmeticalExpression top) {
        List topDecomposed = top.decompose();
        List xDecomposed = x.decompose();
        List starts = new ArrayList();
        List ends = new ArrayList();
        if (xDecomposed.size() >= topDecomposed.size()) {
          for (int i = 0,
              max = xDecomposed.size() - topDecomposed.size() + 1;
              i < max; i++) {
            if (BinArithmeticalExpression.isSame(topDecomposed,
                xDecomposed.subList(i, i + topDecomposed.size()))) {
              starts.add(new Integer(i));
              ends.add(new Integer(i + topDecomposed.size()));
              i += topDecomposed.size() - 1;
            }
          }
        }
        if (starts.size() > 0) {
          x.group(
              (Integer[]) starts.toArray(new Integer[starts.size()]),
              (Integer[]) ends.toArray(new Integer[ends.size()]));
        }
      }

      public void onLeave(Object x) {
      }

      public boolean shouldVisitContentsOf(BinItem x) {
        if (x instanceof BinConstructorInvocationExpression) {
          return false; // skip - we can't put anything infront of it
        }

        if (x instanceof BinSourceConstruct
            && x.getParent() instanceof BinExpressionList
            && x.getParent()
            .getParent() instanceof BinForStatement) {
          return false; // those we can't extract
        }

        return true;
      }
    }


    SameFinder finder = new SameFinder();
    for (int i = 0, max = visitables.size(); i < max; i++) {
      ((BinItem) visitables.get(i)).accept(finder);
    }

    return same;
  }

  public boolean isFirstExpressionBecomesInitializer() {
    return getExtractableExpressions()[0].getParent()
        instanceof BinExpressionStatement;
  }

  public BinStatement getTopLimit() {
    return getTopLimit(this.selectedExpression);
  }

  private static BinStatement getTopLimit(BinExpression expr) {
    BinItemVisitable top = null;

    BinMember exprParentMember = expr.getParentMember();
    final VariableUseAnalyzer varAnalyzer = new VariableUseAnalyzer(
        IDEController.getInstance().createProjectContext(),
        exprParentMember, CollectionUtil.singletonArrayList(expr));

    final BinLocalVariable[] allVars = varAnalyzer.getAllVariables();
    for (int i = 0; i < allVars.length; i++) {
      final VariableUseAnalyzer.VarInfo info = varAnalyzer.getVarInfo(allVars[i]);
      if (!info.usedInside && !info.changedInside && !info.declaredInside) {
        continue;
      }
      if (exprParentMember != allVars[i].getParentMember()) {
        continue; // var is inside inner type
      }

      final LocationAware declaredAt = allVars[i].getWhereDeclared();

      if (top == null) {
        if (declaredAt instanceof BinMethod) {
          top = ((BinMethod) declaredAt).getBody();
        } else {
          top = (BinItemVisitable) declaredAt;
        }
      } else {
        if (declaredAt.isAfter((LocationAware) top)) {
          top = (BinItemVisitable) declaredAt;
        }
      }

      // we can't use var before it was declared and "for" is a bit harder to check
      if (top instanceof BinVariableDeclaration
          && top.getParent() instanceof BinForStatement) {
        // can't cross limits of for, so new var must be in the body
        top = ((BinForStatement) top.getParent()).getStatementList();
      }
    }

    if (top == null) { // no vars used - scope is whole member body
      top = expr.getEnclosingStatement();
      while (top.getParent() instanceof BinStatement) {
        top = top.getParent();
      }
    }

    if (top instanceof BinExpression) {
      return ((BinExpression) top).getEnclosingStatement();
    } else {
      return (BinStatement) top;
    }
  }

  private boolean isParentConstructorInvocation() {
    final BinExpression[] exprs = getExtractableExpressions();
    for (int i = 0; i < exprs.length; i++) {
      BinSourceConstruct expr = exprs[i];
      while (expr != null && expr.getParent() instanceof BinSourceConstruct) {
        if (expr instanceof BinConstructorInvocationExpression) {
          return true;
        }
        expr = (BinSourceConstruct) expr.getParent();
      }
    }

    return false;
  }

  private boolean isForInitializer() {
    final BinExpression[] exprs = getExtractableExpressions();
    for (int i = 0; i < exprs.length; i++) {
      BinExpression expr = exprs[i];

      if (expr.getParent() instanceof BinExpressionList
          && expr.getParent()
          .getParent() instanceof BinForStatement) {
        return true;
      }
    }

    return false;
  }

  boolean canBeDeclaredInForStatement() {

    if(!isExprInForCondition(getSelectedExpression())) {
      return false;
    }

    if (!(isConditionExprAndDeclaredVarHaveTheSameTypeInForStatement(
        getForStatement(getSelectedExpression()), getSelectedExpression()) ||
        isEmtyForInitStatement())) {
      return false;
    }

    return true;
  }

  private boolean isExprInForCondition(final BinExpression expr) {

    if (expr != null) {
      if (expr.getParent() instanceof BinLogicalExpression &&
          expr.getParent().getParent() instanceof BinForStatement) {
        return true;
      }
      if (expr.getParent() instanceof BinLogicalExpression &&
          expr.getParent().getParent() instanceof BinLogicalExpression) {

        return isExprInForCondition((BinExpression) expr.getParent());
      }

    }
    return false;
  }

  protected BinForStatement getForStatement(final BinExpression expr) {
    if (expr.getParent() instanceof BinLogicalExpression &&
        expr.getParent().getParent() instanceof BinForStatement) {
      return (BinForStatement) expr.getParent().getParent();
    }
    if (expr.getParent() instanceof BinLogicalExpression &&
        expr.getParent().getParent() instanceof BinLogicalExpression) {
      return getForStatement((BinExpression) expr.getParent());
    }


    return null;
  }

  public static boolean isConditionExprAndDeclaredVarHaveTheSameTypeInForStatement(
      final BinForStatement fSt, final BinExpression expr) {

    if(fSt == null) {
      return false;
    }
    SourceConstruct initStatement = fSt.getInitSourceConstruct();
    if (initStatement != null
        && initStatement instanceof BinLocalVariableDeclaration) {

      BinLocalVariableDeclaration lVar =
          (BinLocalVariableDeclaration) initStatement;

      if(lVar.getVariables()[0].getTypeRef() == expr.getReturnType()) {
        return true;
      }
    }

    return false;
  }

  protected boolean isEmtyForInitStatement() {

    BinForStatement fSt = getForStatement(getSelectedExpression());

    return fSt !=null && fSt.getInitSourceConstruct() == null;
  }

}
