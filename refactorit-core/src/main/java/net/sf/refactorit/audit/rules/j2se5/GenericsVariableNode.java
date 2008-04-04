/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.j2se5;

import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.classmodel.expressions.BinAssignmentExpression;
import net.sf.refactorit.classmodel.expressions.BinCastExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.common.util.ClassUtil;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.query.usage.Finder;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.filters.BinVariableSearchFilter;
import net.sf.refactorit.query.usage.filters.SearchFilter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Arseni Grigorjev
 */
public class GenericsVariableNode extends GenericsNode {

  public static final SearchFilter FILTER_VAR = new BinVariableSearchFilter(
      true, true, false, false, false);

  private BinVariable variable;
  
  private Map variants;
  private BinTypeRef[] acceptors;
  private boolean usagesResolved = false;

  public GenericsVariableNode(final BinVariable variable){
    this.variable = variable;

    final BinTypeRef variableType = variable.getTypeRef();
    this.acceptors = variableType.getTypeParameters();
    this.variants = new HashMap(acceptors.length);

    if (hasTypeArguments()){
      BinTypeRef[] typeArguments = variable.getTypeRef().getTypeArguments();
      for (int i = 0; i < typeArguments.length; i++){
        variants.put(acceptors[i], new GenericsVariantsManager(typeArguments[i],
            true));
      }
    } else {
      for (int i = 0; i < acceptors.length; i++){
        variants.put(acceptors[i], new GenericsVariantsManager());
      }
    }

    GenericsVariantsManager.linkAllSupertypes(variableType, acceptors,
        variants);
  }

  public void prepare() throws GenericsNodeUnresolvableException {
    if (!hasTypeArguments() && !usagesResolved) {
      usagesResolved = true;
      for (Iterator it = Finder.getInvocations(this.variable, FILTER_VAR)
          .iterator(); it.hasNext(); ){
        collectFromUsage((InvocationData) it.next());
      }
    }
  }
 
  private void collectFromUsage(InvocationData usage)
      throws GenericsNodeUnresolvableException {
    BinItemVisitable parentExpression = usage.getInConstruct().getParent();

    if (parentExpression instanceof BinMethodInvocationExpression){
      if (((BinMethodInvocationExpression) parentExpression).getExpression()
          == usage.getInConstruct()){

        if (parentExpression.getParent() instanceof BinCastExpression){
          BinTypeRef methodReturnType = ((BinMethodInvocationExpression)
              parentExpression).getMethod().getReturnType();
          
          searchForVariants(methodReturnType, ((BinCastExpression)
              parentExpression.getParent()).getReturnType(), false);
        }

        BinExpression[] argumentExpressions = ((BinMethodInvocationExpression)
            parentExpression).getExpressionList().getExpressions();
        BinParameter[] methodParameters = ((BinMethodInvocationExpression)
            parentExpression).getMethod().getParameters();
        for (int i = 0; i < argumentExpressions.length; i++){
          searchForVariants(methodParameters[i].getTypeRef(),
              argumentExpressions[i].getReturnType(), true);
        }
      }
    } else if (parentExpression instanceof BinFieldInvocationExpression){
      BinTypeRef fieldType = ((BinFieldInvocationExpression) parentExpression)
          .getField().getTypeRef();
      if (parentExpression.getParent() instanceof BinCastExpression){
        searchForVariants(fieldType, ((BinCastExpression) parentExpression
            .getParent()).getReturnType(), false);
      } else if (parentExpression.getParent() instanceof BinAssignmentExpression){
        if (((BinAssignmentExpression) parentExpression.getParent())
            .getLeftExpression() == parentExpression){
          searchForVariants(fieldType,
              ((BinAssignmentExpression) parentExpression.getParent())
              .getRightExpression().getReturnType(), true);
        }
      }
    }
  }

  public BinVariable getVariable(){
    return variable;
  }

  public boolean isVariableNode() {
    return true;
  }
  
  public String toString(){
    String var;
    try {
      var = variable.getName();
      try {
        var += "@" + variable.getCompilationUnit().getName();
        var += ":" + variable.getStartLine();
      } catch (NullPointerException ex) {
        var += "@null";
      }
    } catch (NullPointerException e) {
      var = "null";
    }
    return ClassUtil.getShortClassName(this) + " [" + var + "]";
  }

  public BinTypeRef[] getInAcceptors() {
    return acceptors;
  }
  
  public BinTypeRef[] getOutAcceptors() {
    return acceptors;
  }
  
  public GenericsVariantsManager getInVariantsFor(final BinTypeRef typeParameter) {
    return getVariantsFor(typeParameter);
  }

  public GenericsVariantsManager getOutVariantsFor(final BinTypeRef typeParameter) {
    return getVariantsFor(typeParameter);
  }
  
  public GenericsVariantsManager getVariantsFor(final BinTypeRef typeParameter) {
    return (GenericsVariantsManager) variants.get(typeParameter);
  }
  
  public boolean dependsInOnOut() {
    return true; // In and Out are same!
  }
  
  public ASTImpl getPositionToEdit() {
    if (!hasTypeArguments()){
      ASTImpl current = variable.getTypeAst();
      if (current != null && current.getType() == JavaTokenTypes.DOT) {
        return (ASTImpl) ((ASTImpl) current.getFirstChild()).getNextSibling();
      } else {
        return current;
      }
    }
    return null;
  }
  
  public boolean hasTypeArguments() {
    return variable.getTypeRef().getTypeArguments() != null
        && variable.getTypeRef().getTypeArguments().length > 0;
  }
  
  public CompilationUnit getCompilationUnit() {
    return variable.getCompilationUnit();
  }
  
  public LocationAware getContext() {
    return variable;
  }
}
