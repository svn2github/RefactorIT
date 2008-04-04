/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.inlinevariable;

import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinSpecificTypeRef;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.SourceConstruct;
import net.sf.refactorit.classmodel.expressions.BinAssignmentExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.statements.BinExpressionStatement;
import net.sf.refactorit.classmodel.statements.BinVariableDeclaration;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.filters.BinVariableSearchFilter;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.transformations.CastTransformation;
import net.sf.refactorit.transformations.DeleteTransformation;
import net.sf.refactorit.transformations.RenameTransformation;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.utils.GenericsUtil;
import net.sf.refactorit.utils.MethodInvocationUtils;

import java.util.Iterator;
import java.util.List;


/**
 *
 * @author  RISTO A
 */
final class Inliner {
  private TransformationList transList;

  public Inliner(TransformationList transList) {
    this.transList = transList;
  }

  public void inline(BinVariable var, List invocations) {

    inlineVarUsages(var, invocations, getExprNodeText(var, invocations));
    removeVarDeclaration(var);

  }

  /**
   * @param var
   * @param invocations
   * @return
   */
  private String getExprNodeText(final BinVariable var, final List invocations) {
    String exprNodeText = var.getExprNodeText();
    if (exprNodeText != null) {
      return exprNodeText;
    }

    List writes = new BinVariableSearchFilter(false, true, true, false, false).
        filter(invocations, var.getOwner().getProject());

    InvocationData invoc = (InvocationData) writes.get(0);
    BinItemVisitable item = invoc.getInConstruct().getParent();

    if (item instanceof BinAssignmentExpression) {
      exprNodeText = (((BinAssignmentExpression) item).getRightExpression()).
          getText();
      invocations.remove(invoc);
      item = item.getParent();
      if (item instanceof BinExpressionStatement) {
        StringEraser eraser = new StringEraser((BinExpressionStatement) item);
        eraser.setRemoveLinesContainingOnlyComments(true);
        transList.add(eraser);
      }
    }
    return exprNodeText;
  }

  private void inlineVarUsages(BinVariable var, List invocations,
      String exprNodeText) {
    for (int i = 0; i < invocations.size(); i++) {
      inline((InvocationData) invocations.get(i), var, exprNodeText);
    }
  }

  private void inline(InvocationData usage, BinVariable var,
      String exprNodeText) {
    InlinedExpression inlined = new InlinedExpression(var, usage.getWhereAst(),
        exprNodeText);

    transList.add(new RenameTransformation(usage.getCompilationUnit(),
        usage.getWhereAst(), inlined.getStringForm()));

    if (requiresCast(var, usage.getInConstruct())){
      final BinExpression expressionToCast = (BinExpression) usage
          .getInConstruct().getParent();
      // Following check insures that cast will be made only to real type,
      //  not to type parameter of some sort
      if (!expressionToCast.getReturnType().getBinCIType().isTypeParameter()){
        transList.add(new CastTransformation(expressionToCast, expressionToCast
            .getReturnType()));
      }
    }
  }

  private void removeVarDeclaration(BinVariable var) {
    BinVariableDeclaration declaration = (BinVariableDeclaration) var.
        getWhereDeclared();
    transList.add(new DeleteTransformation(var, declaration));
  }
  
  /**
   * List<String> list = new ArrayList();<br>
   * <br>
   * public void someMethod(){<br>
   * &nbsp;&nbsp;String s = list.get(0);<br>
   * }<br>
   * <br>
   * If You try to inline this code without cast, it will be not compilable:<br>
   * &nbsp;&nbsp;String s = new ArrayList().get(0);
   * <br>
   * This is why we should check for such case, and make following:<br>
   * &nbsp;&nbsp;String s = (String) new ArrayList().get(0);<br>
   *
   * @param var variable, which we are inlining
   * @param inConstruct source construct, where the variable is used
   */
  private static boolean requiresCast(BinVariable var,
      SourceConstruct inConstruct){
    final BinTypeRef[] varTypeArguments = var.getTypeRef().getTypeArguments();
    if (varTypeArguments != null && varTypeArguments.length > 0
        && (var.getExpression().getReturnType().getTypeArguments() == null
        || var.getExpression().getReturnType().getTypeArguments().length == 0)){
      return returnTypeWillChange(var, inConstruct)
          && willProduceUncompilableCode(var, inConstruct);
    }
    return false;
  }

  /**
   * @param var
   * @param inConstruct
   *
   * @return true, if replacing return types of member, invoked on var to Object
   *    will produce uncompilable code
   */
  private static boolean willProduceUncompilableCode(final BinVariable var,
      final SourceConstruct inConstruct) {
    BinItemVisitable parentItem = inConstruct.getParent().getParent();
    if (parentItem instanceof BinAssignmentExpression){
      BinExpression leftExpr = ((BinAssignmentExpression) parentItem)
          .getLeftExpression();
      BinTypeRef assignToType = leftExpr.getReturnType();
      if (!assignToType.equals(assignToType.getProject().getObjectRef())){
        return true;
      }
    } else if (parentItem instanceof BinVariable) {
      BinTypeRef assignToType = ((BinVariable) parentItem).getTypeRef();
      if (!assignToType.equals(assignToType.getProject().getObjectRef())){
        return true;
      }
    } else if (MethodInvocationUtils.confusesMethodResolution(
        (BinExpression) inConstruct.getParent(),
        var.getProject().getObjectRef())){
      return true;
    } else if (parentItem instanceof BinFieldInvocationExpression){
      BinField field = ((BinFieldInvocationExpression) parentItem)
          .getField();
      if (!field.getOwner().equals(field.getProject().getObjectRef())){
        return true;
      }
    } else if (parentItem instanceof BinMethodInvocationExpression){
      BinMethod invokedMethod = ((BinMethodInvocationExpression) parentItem)
          .getMethod();
      boolean overridesObjectMethod = false;
      for (Iterator topMethods = invokedMethod.getTopMethods().iterator(); 
          topMethods.hasNext();) {
        BinMethod topMethod = (BinMethod) topMethods.next();
        if (topMethod.getOwner().equals(topMethod.getProject()
            .getObjectRef())){
          overridesObjectMethod = true;
          break;
        }
      }

      if (!overridesObjectMethod){
        return true;
      }
    }
    return false;
  }

  
  /**
   * @param inConstruct variable use expression
   * @param var variable to inline
   *
   * @return true, if return type of field/method depend on var type arguments
   */
  private static boolean returnTypeWillChange(BinVariable var,
      SourceConstruct inConstruct){
    BinItemVisitable parentItem = inConstruct.getParent();
    final BinTypeRef[] varTypeArguments = var.getTypeRef().getTypeArguments();
    if (parentItem == null){
      return false;
    } else if (parentItem instanceof BinMethodInvocationExpression){
      for (int i = 0; i < varTypeArguments.length; i++){
        if (GenericsUtil.methodReturnTypeDependsOnTypeParameter(
            ((BinMethodInvocationExpression) parentItem).getMethod(), 
            ((BinSpecificTypeRef) varTypeArguments[i])
            .getCorrespondingTypeParameter())){
          return true;
        }
      }
    } else if (parentItem instanceof BinFieldInvocationExpression){
      for (int i = 0; i < varTypeArguments.length; i++){
        if (GenericsUtil.fieldTypeDependsOnTypeParameter(
            ((BinFieldInvocationExpression) parentItem).getField(), 
            ((BinSpecificTypeRef) varTypeArguments[i])
            .getCorrespondingTypeParameter())){
          return true;
        }
      }
    }
    return false;
  }
}
