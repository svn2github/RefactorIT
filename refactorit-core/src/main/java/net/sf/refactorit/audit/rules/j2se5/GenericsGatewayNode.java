/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.j2se5;

import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinNewExpression;
import net.sf.refactorit.classmodel.expressions.MethodOrConstructorInvocationExpression;
import net.sf.refactorit.common.util.ClassUtil;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.JavaTokenTypes;

import java.util.HashMap;

/**
 *
 * @author Arseni Grigorjev
 */
public class GenericsGatewayNode extends GenericsNode {

  private MethodOrConstructorInvocationExpression expression;
  
  private BinTypeRef[] outAcceptors;
  private HashMap outVariants;

  private BinTypeRef[] inAcceptors;
  private HashMap inVariants;

  public GenericsGatewayNode(MethodOrConstructorInvocationExpression expr){
    this.expression = expr;
   
    final BinTypeRef returnType = expr.getReturnType();
    final BinTypeRef[] typeArguments = returnType.getTypeArguments();

    inAcceptors = returnType.getBinCIType().getTypeParameters();
    outAcceptors = createOutAcceptors();

    outVariants = new HashMap(outAcceptors.length);
    
    for (int i = 0; i < outAcceptors.length; i++){
      outVariants.put(outAcceptors[i], new GenericsVariantsManager());
    }

    if (expr instanceof BinMethodInvocationExpression){
      final BinTypeRef invokedOn = ((BinMethodInvocationExpression) expr)
          .getInvokedOn();

      GenericsVariantsManager.linkAllSupertypes(invokedOn, outAcceptors,
          outVariants);

      inVariants = new HashMap(inAcceptors.length);
      for (int i = 0; i < inAcceptors.length; i++){
        inVariants.put(inAcceptors[i], GenericsVariantsManager.linkTypeArgument(
            typeArguments[i], getLinkableOutTypeParameters(), outVariants));
      }
    } else if (hasTypeArguments()){
      inVariants = new HashMap(inAcceptors.length);
      for (int i = 0; i < inAcceptors.length; i++){
        inVariants.put(inAcceptors[i], new GenericsVariantsManager(
            ((BinNewExpression) expr).getTypeArguments()[i], true));
      }
    } else {
      inVariants = outVariants;
//      for (int i = 0; i < inAcceptors.length; i++){
//        inVariants.put(inAcceptors[i], new GenericsVariantsManager());
//      }
    }

    GenericsVariantsManager.linkAllSupertypes(returnType, inAcceptors,
        inVariants);
  }

  private BinTypeRef[] getLinkableOutTypeParameters() {
    return (BinTypeRef[]) outVariants.keySet().toArray(
        new BinTypeRef[outVariants.keySet().size()]);
  }

  private BinTypeRef[] createOutAcceptors() {
    if (expression instanceof BinMethodInvocationExpression
        && ((BinMethodInvocationExpression) expression)
        .getExpression() != null){
      return ((BinMethodInvocationExpression) expression).getInvokedOn()
          .getBinCIType().getTypeParameters();
    } else {
      return inAcceptors;//new BinTypeRef[0];
    }
  }
  
  public String toString(){
    return ClassUtil.getShortClassName(this) + "[" + getInvocationExpressionText() + "]";
  }
  
  private String getInvocationExpressionText(){
    if (expression instanceof BinMethodInvocationExpression){
      return ((BinMethodInvocationExpression) expression).getText();
    } else if (expression instanceof BinNewExpression){
      return ((BinNewExpression) expression).getText();
    } else {
      return "<unknown invocation expression>";
    }
  }

  public BinTypeRef[] getInAcceptors() {
    return inAcceptors;
  }
  
  public GenericsVariantsManager getInVariantsFor(final BinTypeRef typeParameter) {
    return (GenericsVariantsManager) inVariants.get(typeParameter);
  }
  
  public BinTypeRef[] getOutAcceptors() {
    return outAcceptors;
  }
  
  public GenericsVariantsManager getOutVariantsFor(BinTypeRef typeParameter) {
    return (GenericsVariantsManager) outVariants.get(typeParameter);
  }
  
  public boolean dependsInOnOut() {
    boolean result = false;
    if (expression instanceof BinNewExpression){
      result = true;
    } else {
       final GenericsVariantsManager[] out = new GenericsVariantsManager[
          outAcceptors.length];
      for (int i = 0; i < out.length; i++){
        out[i] = getOutVariantsFor(outAcceptors[i]);
      }

      GenericsVariantsManager variant;
      for (int i = 0; i < inAcceptors.length; i++){
        variant = getInVariantsFor(inAcceptors[i]);
        if (variant.isLinkedToManagers(out)){
          result = true;
          break;
        }
      }
    }
    return result;
  }
  
  public boolean usagesResolved = false;
  
  public void prepare() throws GenericsNodeUnresolvableException {
    if (usagesResolved){
      return;
    }
    usagesResolved = true;

    if (expression.getExpressionList() != null){ // method has arguments
      BinExpression[] argumentExpressions = expression.getExpressionList()
          .getExpressions();
      BinParameter[] methodParameters = expression.getMethod().getParameters();
      for (int i = 0; i < argumentExpressions.length; i++){
        searchForVariants(methodParameters[i].getTypeRef(),
            argumentExpressions[i].getReturnType(), true);
      }
    }
  }
  
  public ASTImpl getPositionToEdit() {
    if (expression instanceof BinNewExpression && !hasTypeArguments()){
      ASTImpl current = ((BinNewExpression) expression).getTypeRef()
          .getNode();
      if (current.getType() == JavaTokenTypes.DOT){
        return (ASTImpl) ((ASTImpl) current.getFirstChild()).getNextSibling();
      } else {
        return current;
      }
    } else {
      return null;
    }
  }
  
  public boolean hasTypeArguments() {
    if (expression instanceof BinNewExpression){
      final BinTypeRef type = ((BinNewExpression) expression).getTypeRef();
      return type.getTypeArguments() != null
          && type.getTypeArguments().length > 0;
    }
    return false;
  }
  
  public CompilationUnit getCompilationUnit() {
    return expression.getCompilationUnit();
  }
  
  public LocationAware getContext() {
    return (LocationAware) expression;
  }
}
