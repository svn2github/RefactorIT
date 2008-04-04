/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.extract;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinExpressionList;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.classmodel.BinSelection;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinTypeRefManager;
import net.sf.refactorit.classmodel.BinTypeRefVisitor;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinMemberInvocationExpression;
import net.sf.refactorit.classmodel.statements.BinStatement;
import net.sf.refactorit.classmodel.statements.BinStatementList;
import net.sf.refactorit.classmodel.statements.BinTryStatement;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.refactorings.LocationAwareImpl;
import net.sf.refactorit.refactorings.SelectionAnalyzer;
import net.sf.refactorit.source.UserFriendlyError;
import net.sf.refactorit.source.format.BinFormatter;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.module.RefactorItContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ExtractMethodAnalyzer {
  public static boolean showDebugMessages = false;

  private final IdeWindowContext context;

  public final SelectionAnalyzer selectionAnalyzer;

  private ReturnThrowAnalyzer returnThrowAnalyzer = null;

  private List parameters = new ArrayList();

  private List declaredVariables = new ArrayList();

  private List returnVariables = new ArrayList();

  private List needsDeclarationWithin = new ArrayList();

  private List needsDeclarationBefore = new ArrayList();

  private Set fqnTypes = new HashSet();

  private MultiValueMap usageMap = null;

  private ReturnType returnType = null;

  public String WARNING;

  public static class ReturnType {
    public static final int UNDEFINED = 0;

    public static final int RETURN = 1;

    public static final int VARIABLE = 2;

    public static final int EXPRESSION = 3;

    public static final int NO_ERROR = 0;

    public static final int VAR_AND_EXPRESSION_ERROR = 1;

    public static final int TOO_MANY_VARS_ERROR = 2;

    public static final int RETURN_AND_VAR_ERROR = 3;

    public static final int PARTIAL_RETURN_ERROR = 4;

    public int status = UNDEFINED;

    public int error = NO_ERROR;

    public BinLocalVariable variable = null;

    public BinTypeRef typeRef = BinPrimitiveType.VOID_REF;

    public String toString() {
      return "Status: " + status + ", error: " + error + ", var: "
          + (variable != null ? variable.getName() : "null") + ", type: "
          + typeRef.getName();
    }
  }

  public ExtractMethodAnalyzer(RefactorItContext context, BinSelection selection) {
    this.context = context;

    selectionAnalyzer = new SelectionAnalyzer(selection);

    Project project = context.getProject();
    if ((project.getProjectLoader().getErrorCollector())
        .hasUserFriendlyErrors()) {
      Iterator it = (project.getProjectLoader().getErrorCollector())
          .getUserFriendlyErrors();
      while (it.hasNext()) {
        UserFriendlyError error = (UserFriendlyError) it.next();
        if (error.getCompilationUnit() != null && error.hasLineAndColumn()) {
          if (error.contains(selection)) {
            selectionAnalyzer
                .addErrorMessage("Project has errors within bounds of current selection!");
            return;
          }
        } else {
          selectionAnalyzer
              .addErrorMessage("Project has some errors which location can not be correctly identified!");
          return;
        }
      }
    }

    boolean wrong = false;
    boolean expr = false;
    boolean stat = false;
    final List lasToMove = selectionAnalyzer.getSelectedItems();
    for (int i = 0; i < lasToMove.size(); i++) {
      final LocationAware toMove = (LocationAware) lasToMove.get(i);

      if ((!(toMove instanceof BinStatement)
          && !(toMove instanceof BinStatementList)
          && !(toMove instanceof BinExpression)
          && !(toMove instanceof BinExpressionList)
          && !(toMove instanceof Comment) && !(toMove instanceof LocationAwareImpl))
          || (toMove instanceof BinStatementList && ((BinStatementList) toMove)
              .getParent() instanceof BinMethod)) {
        if (showDebugMessages) {
          System.err.println("ToMove: " + toMove + ", class: "
              + toMove.getClass().getName());
        }
        wrong = true;
        break;
      } else if (toMove instanceof BinTryStatement.TryBlock
          || toMove instanceof BinTryStatement.CatchClause
          || toMove instanceof BinTryStatement.Finally
          || (toMove instanceof BinStatementList && (((BinStatementList) toMove)
              .getParent() instanceof BinTryStatement.TryBlock
              || ((BinStatementList) toMove).getParent() instanceof BinTryStatement.CatchClause || ((BinStatementList) toMove)
              .getParent() instanceof BinTryStatement.Finally))) {
        wrong = true;
        break;
      } else if (toMove instanceof BinExpression) {
        if (stat || expr) { // only single standalone expression alowed
          wrong = true;
          break;
        }
        expr = true;
      } else if (toMove instanceof BinStatement) {
        if (expr) {
          wrong = true;
          break;
        }
        stat = true;
      }
    }
    if (wrong) {
      selectionAnalyzer
          .addErrorMessage("Selected block should represent a statement, a block of statements "
              + "\nor an expression.");
      return;
    }

    String partlySelected = selectionAnalyzer.getPartlyInRangeConstructs();
    if (partlySelected.length() > 0) {
      selectionAnalyzer
          .addErrorMessage("There is partly selected constructs in the current selection.");
      if (showDebugMessages) {
        System.err.println("PARTLY IN RANGE = " + partlySelected);
      }
      return;
    }

    if (getRangeMember() == null) {
      selectionAnalyzer.addErrorMessage("Can not analyze selection!");
      return;
    }

    BinExpression topExpression = selectionAnalyzer.findTopExpression();
    if (topExpression != null
        && !selectionAnalyzer.isAllChildrenSelected(topExpression)) {
      selectionAnalyzer
          .addErrorMessage("Not all children of selected expression were selected also.");
      return;
    }

    if (topExpression == null
        && !BinStatement.isAllSameLevel(selectionAnalyzer.getSelectedItems())) {
      selectionAnalyzer
          .addErrorMessage("Not all statements are on the same level.");
      return;
    }

    if (usesLocalInnersDefinedOutside()) {
      selectionAnalyzer
          .addErrorMessage("There is selected an usage of a local type" + "\n"
              + "which was defined before the selection.");
      return;
    }

    if (localInnersDefinedInsideUsedOutside()) {
      selectionAnalyzer
          .addErrorMessage("A local type defined inside selection is used outside.");
      return;
    }

    if (!selectionAnalyzer.isBracketsBalanced()) {
      selectionAnalyzer
          .addErrorMessage("Selection contains wrong number of brackets.");
      return;
    }

    /*
     * if (showDebugMessages) {
     * JOptionPane.showMessageDialog(Project.getParentWindow(),
     * getSelectionMessage()); }
     */

    if (selectionAnalyzer.getInRangeConstructs().size() > 0) {
      analyzeVariables();
    }

    String msg = null;
    switch (getReturnType().error) {
    case ReturnType.VAR_AND_EXPRESSION_ERROR:
      msg = "Present selection extracts an expression," + "\n"
          + "but needs to return the following variables also:" + "\n";
      msg += variablesToString();
      break;

    case ReturnType.TOO_MANY_VARS_ERROR:
      msg = "Present selection requires returning more than one variable:"
          + "\n";
      msg += variablesToString();
      break;

    case ReturnType.RETURN_AND_VAR_ERROR:
      msg = "Present selection contains a return statement," + "\n"
          + "but needs to return the following variables also:" + "\n";
      msg += variablesToString();
      break;

    case ReturnType.PARTIAL_RETURN_ERROR:
      msg = "Present selection contains return statements," + "\n"
          + "but not in all branches.";
      break;

    default:
      msg = null;
    }
    if (msg != null) {
      selectionAnalyzer.addErrorMessage(msg);
      return;
    }

    if (selectionAnalyzer.isBreakSelected()) {
      // TODO: would be nicer to set error location so user could scroll
      // to it?
      selectionAnalyzer.addErrorMessage("There is a 'break' statement selected"
          + "\n" + "that breaks to a statement not selected.");
      return;
    }

    if (selectionAnalyzer.isContinueSelected()) {
      // TODO: would be nicer to set error location so user could scroll
      // to it?
      selectionAnalyzer
          .addErrorMessage("There is a 'continue' statement selected" + "\n"
              + "that continues to a statement not selected.");
      return;
    }

    if (selectionAnalyzer.isSuperOrThisSelected()) {
      selectionAnalyzer
          .addErrorMessage("There is selected a \"super\" or \"this\" constructor invocation"
              + "\n" + "which can not be extracted.");
      return;
    }

    if (showDebugMessages) {
      RitDialog.showMessageDialog(IDEController.getInstance()
          .createProjectContext(), getChangesMessage());
    }

    clearUpFqnTypes();
  }

  private String variablesToString() {
    String vars = new String();
    for (int i = 0; i < this.returnVariables.size(); i++) {
      if (i > 0) {
        vars += ",\n";
      }
      vars += "  "
          + BinFormatter.formatWithTypeNestedGenerics(
              (BinVariable) this.returnVariables.get(i)).replaceAll("$", ".");
    }
    return vars;
  }

  private boolean usesLocalInnersDefinedOutside() {
    return checkForLocalTypes(selectionAnalyzer.getInRangeConstructs(), false);
  }

  private boolean localInnersDefinedInsideUsedOutside() {
    return checkForLocalTypes(selectionAnalyzer.getAfterRangeConstructs(), true);
  }

  private boolean checkForLocalTypes(final List constructs,
      final boolean checkInside) {
    final boolean[] flag = new boolean[] { false };

    class TypeRefVisitor extends BinTypeRefVisitor {
      TypeRefVisitor() {
        setCheckTypeSelfDeclaration(true);
        setIncludeNewExpressions(true);
      }

      public void visit(BinTypeRef data) {
        BinType type = data.getTypeRef().getBinType();
        if (type instanceof BinCIType
            && ((BinCIType) type).isLocal()
            && selectionAnalyzer.getLocalTypesDefinedWithin().contains(type) == checkInside) {
          flag[0] = true;
        } else {
          super.visit(data);
        }
      }
    }
    ;

    TypeRefVisitor visitor = new TypeRefVisitor();

    for (int i = 0, max = constructs.size(); i < max; i++) {
      final Object construct = constructs.get(i);
      if (construct instanceof BinTypeRefManager) {
        ((BinTypeRefManager) construct).accept(visitor);
        if (flag[0]) {
          return true;
        }
      }
    }

    return false;
  }

  private void analyzeVariables() {
    List debug = new ArrayList();

    RenamingVariableUseAnalyzer useAnalyzer = new RenamingVariableUseAnalyzer(
        context, getRangeMember(), selectionAnalyzer.getSelectedItems());

    this.fqnTypes.addAll(useAnalyzer.getFqnTypes());

    BinLocalVariable[] allVars = useAnalyzer.getAllVariables();
    for (int i = 0; i < allVars.length; ++i) {
      final BinLocalVariable aVar = allVars[i];
      final boolean freezeFinal = allVars[i].isFinal();
      final VariableUseAnalyzer.VarInfo info = useAnalyzer.getVarInfo(aVar);

      if (!info.declaredInOuter) {
        if (info.usedInside && !info.declaredInside && !info.declaredInLocal) {
          if ((!info.usedBefore && !info.changedBefore)
              || info.changesBeforeUseInside == VariableUseAnalyzer.VarInfo.YES) {
            // FIXME (TANEL) causes problem with anonymous
            CollectionUtil.addNew(needsDeclarationWithin, aVar);
          } else {
            CollectionUtil.addNew(parameters, aVar);
            if (!info.changedInside) {
              aVar.setModifiers(BinModifier.FINAL);
            }
          }
        }
        if (info.changedInside) {
          if (!freezeFinal) {
            aVar.setModifiers(0); // set off FINAL
          }

          if (info.usedAfter
              && info.changesBeforeUseAfter != VariableUseAnalyzer.VarInfo.YES) {
            CollectionUtil.addNew(returnVariables, aVar);
          }
          if (!info.declaredInside && !info.declaredInLocal) {
            if ((!info.usedBefore && !info.changedBefore)
                || info.changesBeforeUseInside == VariableUseAnalyzer.VarInfo.YES) {
              CollectionUtil.addNew(needsDeclarationWithin, aVar);
            } else {
              CollectionUtil.addNew(parameters, aVar);
            }
          }
        }
        if (info.declaredInside) {
          CollectionUtil.addNew(declaredVariables, aVar);
          if (info.usedAfter || info.changedAfter) {
            if (info.changesBeforeUseAfter != VariableUseAnalyzer.VarInfo.YES) {
              CollectionUtil.addNew(returnVariables, aVar);
            } else {
              CollectionUtil.addNew(needsDeclarationBefore, aVar);
            }
          }
        }
      }
      debug.add(info);
    }

    if (showDebugMessages) {
      RitDialog.showMessageDialog(IDEController.getInstance()
          .createProjectContext(), debug.toString());
    }

    this.usageMap = useAnalyzer.getUsageMap();
  }

  public List getLasToMove() {
    LocationAware la = selectionAnalyzer.constructExpressionsLA();
    if (la == null) {
      return selectionAnalyzer.getSelectedItems();
    } else {
      List list = new ArrayList();
      list.add(la);
      return list;
    }
  }

  public MultiValueMap getUsageMap() {
    return usageMap;
  }

  public Set getFqnTypes() {
    return this.fqnTypes;
  }

  public BinLocalVariable[] getParameters() {
    return (BinLocalVariable[]) this.parameters
        .toArray(new BinLocalVariable[this.parameters.size()]);
  }

  private void clearUpFqnTypes() {
    List importedNames = getRangeMember().getCompilationUnit()
        .getImportedTypeNames();
    if (importedNames != null) {
      Iterator it = this.fqnTypes.iterator();
      while (it.hasNext()) {
        final BinTypeRef typeRef = (BinTypeRef) it.next();
        if (importedNames.contains(typeRef)) {
          it.remove();
        }
      }
    }
  }

  public ReturnType getReturnType() {
    if (returnType == null) {
      // showDebugMessages = true;
      returnThrowAnalyzer = new ReturnThrowAnalyzer(context, getRangeMember(),
          selectionAnalyzer.getInRangeConstructs());
      // showDebugMessages = false;

      this.fqnTypes.addAll(returnThrowAnalyzer.getFqnTypes());

      returnType = new ReturnType();

      BinExpression topExpression = selectionAnalyzer.findTopExpression();
      if (topExpression != null) {
        returnType.typeRef = topExpression.getReturnType();

        // small hack when user selected just null
        if (returnType.typeRef == null) {
          returnType.typeRef = BinPrimitiveType.VOID_REF;
        }

        if (!returnType.typeRef.equals(BinPrimitiveType.VOID_REF)) {
          returnType.status = ReturnType.EXPRESSION;
        }
      }

      if (this.returnVariables.size() > 0) {
        returnType.variable = (BinLocalVariable) this.returnVariables.get(0);
        returnType.typeRef = returnType.variable.getTypeRef();

        if (returnType.status != ReturnType.UNDEFINED) {
          returnType.error = ReturnType.VAR_AND_EXPRESSION_ERROR;
        } else {
          if (this.returnVariables.size() > 1) {
            returnType.error = ReturnType.TOO_MANY_VARS_ERROR;
          } else {
            returnType.status = ReturnType.VARIABLE;
          }
        }
      }

      ReturnThrowAnalyzer.ReturnThrowInfo returnInfo = returnThrowAnalyzer
          .getReturnInfo();
      if (returnType.error == ReturnType.NO_ERROR && returnInfo != null) {
        switch (returnInfo.status) {
        case ReturnThrowAnalyzer.ReturnThrowInfo.PART_MIX:
        case ReturnThrowAnalyzer.ReturnThrowInfo.PART_RET:
          if (returnType.status != ReturnType.UNDEFINED) {
            returnType.error = ReturnType.RETURN_AND_VAR_ERROR;
          } else
          if (returnType.typeRef != BinPrimitiveType.VOID_REF
              || returnInfo.returnType != BinPrimitiveType.VOID_REF) {
            returnType.error = ReturnType.PARTIAL_RETURN_ERROR;
          } else {
            WARNING = "Execution flow of the your code may have changed - \n"
                + "the statements following the selected (extracted) "
                + "block were executed conditionally, \nbut now will "
                + "always be executed.";
          }
          break;

        case ReturnThrowAnalyzer.ReturnThrowInfo.MIXED:
        case ReturnThrowAnalyzer.ReturnThrowInfo.RETURN:
          if (returnType.status != ReturnType.UNDEFINED) {
            returnType.error = ReturnType.RETURN_AND_VAR_ERROR;
          } else {
            returnType.status = ReturnType.RETURN;
            returnType.typeRef = returnInfo.returnType;
            if (returnType.typeRef == null) {
              BinMember rangeMember = getRangeMember();
              if (rangeMember instanceof BinMethod) {
                returnType.typeRef = ((BinMethod) rangeMember).getReturnType();
              } else if (rangeMember instanceof BinConstructor) {
                returnType.typeRef = ((BinConstructor) rangeMember)
                    .getReturnType();
              } else {
                returnType.typeRef = BinPrimitiveType.VOID_REF;
                System.err.println("returnInfo.typeRef is null :(((");
              }
            }
          }
          break;

        default: // no action
        }

      }

      if (returnType.typeRef.isReferenceType()
          && returnType.typeRef.getBinCIType().isAnonymous()) {
        returnType.typeRef = returnType.typeRef.getSuperclass();
      }
    }

    if (showDebugMessages) {
      System.err.println("ReturnType: " + returnType);
    }

    return returnType;
  }

  public BinLocalVariable[] getDeclared() {
    return (BinLocalVariable[]) this.declaredVariables
        .toArray(new BinLocalVariable[this.declaredVariables.size()]);
  }

  public BinLocalVariable[] getNeedsDeclarationWithin() {
    return (BinLocalVariable[]) this.needsDeclarationWithin
        .toArray(new BinLocalVariable[this.needsDeclarationWithin.size()]);
  }

  public BinLocalVariable[] getNeedsDeclarationBefore() {
    return (BinLocalVariable[]) this.needsDeclarationBefore
        .toArray(new BinLocalVariable[this.needsDeclarationBefore.size()]);
  }

  public BinTypeRef[] getThrownExceptions() {
    return returnThrowAnalyzer.getThrownExceptions();
  }

  public boolean canBeStatic() {
    // if old one is static, then new one will be static for sure
    if (getRangeMember() != null && getRangeMember().isStatic()) {
      return true;
    }

    for (int i = 0; i < selectionAnalyzer.getInRangeConstructs().size(); ++i) {
      final Object construct = selectionAnalyzer.getInRangeConstructs().get(i);
      if (construct instanceof BinMemberInvocationExpression) {
        final BinMemberInvocationExpression expr = (BinMemberInvocationExpression) construct;
        final BinMember member = expr.getMember();

        if (!member.isStatic() && !expr.isOutsideMemberInvocation()) {
          return false;
        }
      }
    }

    return true;
  }

  public String getChangesMessage() {
    StringBuffer result = new StringBuffer(200);
    result.append("Parameters : \n" + parameters);
    result.append("\n\nNeeding declaring variables : \n"
        + needsDeclarationWithin);
    result.append("\n\nDeclared variables : \n" + declaredVariables);
    result.append("\n\nReturns : \n" + returnVariables);
    result.append("\n\nThrown exceptions : \n"
        + Arrays.asList(getThrownExceptions()));

    return result.toString();
  }

  public boolean hasErrors() {
    return selectionAnalyzer.hasErrors();
  }

  public String getErrorMessage() {
    return selectionAnalyzer.getErrorMessage();
  }

  public BinMember getRangeMember() {
    return selectionAnalyzer.getRangeMember();
  }

}
