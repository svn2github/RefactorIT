/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.j2se5;

import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.classmodel.TypeConversionRules;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinNewExpression;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.transformations.TransformationManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author  Arseni Grigorjev
 */
public abstract class GenericsNode {

  private boolean locked = false;
  private boolean resolved = false;
  private boolean inGraph = false;

  public static GenericsNode getInstance(Object item){
    GenericsNode node = null;
    if (item instanceof BinMethodInvocationExpression){ 
      BinMethod method = ((BinMethodInvocationExpression) item).getMethod();
      if ((method.getReturnType().getTypeArguments() != null
          && method.getReturnType().getTypeArguments().length > 0)){
        node = new GenericsGatewayNode((BinMethodInvocationExpression) item);
      }
    } else if (item instanceof BinNewExpression){
      node = new GenericsGatewayNode((BinNewExpression) item);
    } else if (item instanceof BinVariable) {
      BinVariable var = (BinVariable) item;
      if (hasTypeParameters(var) && isNotFromBinaryOrHasTypeArguments(var)){
        node = new GenericsVariableNode(var);
      }
    } else {
      node = null;
    }
    return node;
  }

  private static boolean isNotFromBinaryOrHasTypeArguments(BinVariable item) {
    final BinTypeRef[] typeArguments = item.getTypeRef().getTypeArguments();
    return item.getCompilationUnit() != null
        || (typeArguments == null && typeArguments.length == 0);
  }

  private static boolean hasTypeParameters(final BinVariable item) {
    final BinTypeRef[] typeParameters = item.getTypeRef().getBinCIType()
        .getTypeParameters();
    return typeParameters != null && typeParameters.length > 0;
  }
  
  // These methods define in/out interfaces for the nodes (for softImport()
  //  and hardImport() logic).
  public abstract BinTypeRef[] getInAcceptors();
  
  public abstract BinTypeRef[] getOutAcceptors();
  
  public abstract GenericsVariantsManager getInVariantsFor(BinTypeRef typeParameter);
  
  public abstract GenericsVariantsManager getOutVariantsFor(BinTypeRef typeParameter);
  
  public abstract boolean dependsInOnOut();
  
  public abstract void prepare() throws GenericsNodeUnresolvableException;
  
  public abstract ASTImpl getPositionToEdit();

  public abstract boolean hasTypeArguments();
  
  public abstract CompilationUnit getCompilationUnit();
  
  public abstract LocationAware getContext();
  
  public boolean isPrepared(){
    return true;
  }
  
  public boolean isVariableNode(){
    return false;
  }

  public BinVariable getVariable(){
    throw new UnsupportedOperationException(
        "This method is only available for GenericsVariableNode(s)");
  }

  public void createEditors(final TransformationManager manager){
    final ASTImpl pos = getPositionToEdit();
    if (pos != null){
      final String stringToInsert = generateTypeArgumentsString();
      if (stringToInsert != null && stringToInsert.length() > 0) {
        manager.add(new StringInserter(getCompilationUnit(),
            pos.getEndLine(), pos.getEndColumn() - 1, stringToInsert));
      }
    }
  }

  public void debugStates() {
    String result = this + "\n";
    result += "in graph: " + (isInGraph() ? "YES" : "NO") + "\n";
    BinTypeRef[] acceptors;
    if (!isVariableNode()){
      result += "IN: \n";
      acceptors = getInAcceptors();
      for (int i = 0; i < acceptors.length; i++){
        result += "for " + acceptors[i].getQualifiedName() + ": \n";
        result += getInVariantsFor(acceptors[i]);
      }
      result += "OUT: \n";
      acceptors = getOutAcceptors();
      for (int i = 0; i < acceptors.length; i++){
        result += "for " + acceptors[i].getQualifiedName() + ": \n";
        result += getOutVariantsFor(acceptors[i]);
      }
    } else {
      acceptors = getInAcceptors();
      for (int i = 0; i < acceptors.length; i++){
        result += "for " + acceptors[i].getQualifiedName() + ": \n";
        result += getInVariantsFor(acceptors[i]);
      }
    }
    System.out.println(result);
  }

  public boolean isLocked() {
    return this.locked;
  }

  public void setLocked(final boolean locked) {
    this.locked = locked;
  }

  public boolean isResolved() {
    return this.resolved;
  }

  public void setResolved(final boolean resolved) {
    this.resolved = resolved;
  }
  
  public boolean isInGraph() {
    return this.inGraph;
  }

  public void setInGraph(final boolean inGraph) {
    this.inGraph = inGraph;
  }
  
  protected void searchForVariants(BinTypeRef signature, BinTypeRef usedType,
      boolean forbidWildcard) throws GenericsNodeUnresolvableException {
    if (usedType == null || signature.isPrimitiveType()){
      return;
    }
    usedType = ensureNonPrimitiveTypeRef(usedType);

    if (signature.getBinCIType().isTypeParameter()
        && getOutVariantsFor(signature) != null){
      consumeUsage(signature, usedType, forbidWildcard);
    } else {
      final BinTypeRef[] signatureTypeArguments = signature.getTypeArguments();
      final BinTypeRef[] usageTypeArguments = usedType.getTypeArguments();

      if (signatureTypeArguments != null && usageTypeArguments != null){
        for (int i = 0; i < signatureTypeArguments.length
            && i < usageTypeArguments.length; i++){
          searchForVariants(signatureTypeArguments[i], usageTypeArguments[i],
              forbidWildcard);
        }
      }
    }
  }

  protected BinTypeRef ensureNonPrimitiveTypeRef(BinTypeRef usedType) {
    if (usedType.isPrimitiveType()){
      usedType = getCompilationUnit().getProject().getTypeRefForName(
          (TypeConversionRules.getBoxingObjectByPrimitive(usedType
          .getQualifiedName())));
    }
    return usedType;
  }

  protected void consumeUsage(final BinTypeRef typeParameter,
      final BinTypeRef usedType, final boolean forbidWildcard)
      throws GenericsNodeUnresolvableException {

    final GenericsVariantsManager currentVariants = getOutVariantsFor(
        typeParameter);
    final GenericsVariantsManager usageVariants = new GenericsVariantsManager(
        usedType, false);

    if (forbidWildcard){
      currentVariants.forbidWildcardRecursively();
    }

    try {
      currentVariants.importSoft(Collections.singletonList(usageVariants),
          getContext());
    } catch (GenericsVariantsImportException e){
      throw new GenericsNodeUnresolvableException(e.getMessage(), this);
    }
  }
  
  public boolean softImport(final List nodes)
      throws GenericsNodeUnresolvableException {
    boolean madeChanges = false;
    try {
      prepare();

      final LocationAware context = getContext();

      List variants;
      GenericsNode nextNode;
      final BinTypeRef[] outAcceptors = getOutAcceptors();
      for (int i = 0; i < outAcceptors.length; i++){
        variants = new ArrayList(nodes.size());
        for (final Iterator it = nodes.iterator(); it.hasNext(); ){
          nextNode = (GenericsNode) it.next();
          nextNode.prepare();
          variants.add(nextNode.getInVariantsFor(outAcceptors[i]));
        }
        madeChanges |= getOutVariantsFor(outAcceptors[i]).importSoft(variants,
            context);
      }
    } catch (GenericsVariantsImportException e) {
      throw new GenericsNodeUnresolvableException(e.getMessage(), this);
    }
    return madeChanges;
  }

  public boolean hardImport(final GenericsNode from)
      throws GenericsNodeUnresolvableException {
    boolean madeChanges = false;
    try {
      final LocationAware context = getContext();
      GenericsVariantsManager fromVariants;
      GenericsVariantsManager thisVariants;
      
      final BinTypeRef[] fromOutAcceptors = from.getOutAcceptors();
      for (int i = 0; i < fromOutAcceptors.length; i++){
        fromVariants = from.getOutVariantsFor(fromOutAcceptors[i]);
        thisVariants = getInVariantsFor(fromOutAcceptors[i]);
        madeChanges |= thisVariants.importHard(fromVariants, context);
      }
    } catch (GenericsVariantsImportException ex) {
      throw new GenericsNodeUnresolvableException(ex.getMessage(), this);
    } catch (UnsupportedOperationException e){
      return false;
    }
    return madeChanges;
  }

  public String generateTypeArgumentsString(){
    final StringBuffer buf = new StringBuffer();
    buf.append('<');
    
    GenericsVariantsManager currentVariant;
    final BinTypeRef[] typeParameters = getInAcceptors();
    for (int i = 0; i < typeParameters.length; i++){
      if (i > 0){
        buf.append(", ");
      }
      currentVariant = getInVariantsFor(typeParameters[i]);
      buf.append(currentVariant.generateTypeString());
    }

    if (buf.length() == 1) {
      return ""; // no type parameters added
    }

    buf.append('>');
    return buf.toString();
  }
  
  public boolean hasUnresolvedVariants() {
    GenericsVariantsManager currentVariant;
    final BinTypeRef[] typeParameters = getInAcceptors();
    for (int i = 0; i < typeParameters.length; i++){
      currentVariant = getInVariantsFor(typeParameters[i]);
      if (currentVariant.hasUnresolvedVariants()){
        return true;
      }
    }
    return false;
  }
  
  public boolean hasWildcardVariants() {
    GenericsVariantsManager currentVariant;
    final BinTypeRef[] typeParameters = getInAcceptors();
    for (int i = 0; i < typeParameters.length; i++){
      currentVariant = getInVariantsFor(typeParameters[i]);
      if (currentVariant.hasWildcardVariants()){
        return true;
      }
    }
    return false;
  }

  public boolean isMethodParameterNode() {
    return isVariableNode() && getVariable() instanceof BinParameter
        && ((BinParameter) getVariable()).getMethod() != null;
  }
}
