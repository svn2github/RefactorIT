/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.j2se5;

import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinExpressionList;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.SourceConstruct;
import net.sf.refactorit.classmodel.expressions.BinAssignmentExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.classmodel.expressions.MethodOrConstructorInvocationExpression;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.query.usage.Finder;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.filters.BinMethodSearchFilter;
import net.sf.refactorit.query.usage.filters.BinVariableSearchFilter;
import net.sf.refactorit.query.usage.filters.SearchFilter;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.transformations.TransformationManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Arseni Grigorjev
 */
public class GenericsArgumentsAnalyzer {
  public static final SearchFilter FILTER_METHOD = new BinMethodSearchFilter(
      true, false, true, true, false, false, false, false, true);
  public static final SearchFilter FILTER_VAR = new BinVariableSearchFilter(
      true, true, false, false, false);
  
  private final BinVariable target;

  private GenericsDigraph graph = null;
  private Set alreadyPopulated = new HashSet(20);
  private Map binaryMethods = null;

  public GenericsArgumentsAnalyzer(final BinVariable target) {
    this.target = target;
  }
  
  public RefactoringStatus run(){
    final RefactoringStatus status = new RefactoringStatus();
    try {

      setUp();

//      System.err.println("PHASE 1: populate graph");
      populateGraph();
//     System.out.println("Graph: " + graph);
//      System.err.println("    graph size: " + graph.getSize());
     
      //graph.debugResults("Before"); // FIXME
 
//      System.err.println("PHASE 2: stabilize graph");

      tooComplex |= !graph.stabilize();
      
      if (tooComplex){
        status.addEntry("The generics corrective action is still in the beta stage.\n"
            + "Given target appeared to be too complex to be completely analyzed\n"
            + "and behaviour preserving refactoring is not guaranteed.\n\n"
            + "Do you want to continue?\n"
            + "If you choose Yes you will get the preview dialog and can cancel there as well.",
            RefactoringStatus.QUESTION);
      }
      
//      System.err.println("PHASE 3: check completeness");
      graph.checkCompleteness(status);

      //graph.debugResults("After"); // FIXME
    
    } catch (Exception e){
      AppRegistry.getExceptionLogger().error(e, "Exception during analysis", this);
      status.addEntry("Internal error during analysis", RefactoringStatus.ERROR);
//      e.printStackTrace(System.out);
    }
    return status;
  }

  private void setUp() {
    graph = new GenericsDigraph(target);
    binaryMethods = new HashMap(10);
    deep = 0;
  }

  static int deep = 0;
  boolean tooComplex = false;
  
  private void populateGraph(){
    List dependants = new ArrayList(50);
    dependants.add(target);
    
    for (int i = 0; i < dependants.size(); i++){ // size is changed inside loop!
      if (i > 1000){ // guard
        tooComplex = true;
        return;
      }
      BinVariable currentDependant = (BinVariable) dependants.get(i);
      if (!alreadyPopulated.contains(currentDependant)){
        dependants.addAll(populateGraph(currentDependant));
        alreadyPopulated.add(currentDependant);
      }
    }
  }
  
  private List populateGraph(BinVariable variable){
    //System.err.println("populate " + variable + "@" + (variable.getCompilationUnit() != null ? variable.getCompilationUnit().getName() : "binary"));
    
    List result;

    final BinTypeRef[] typeArguments = variable.getTypeRef().getTypeArguments();
    if (typeArguments == null || typeArguments.length == 0){
      buildEdgesAndCollectDependants(variable);
      graph.truncateNodesBehindWildcards(variable);
      result = graph.extractAllDependantsOf(variable);
    } else {
      result = Collections.EMPTY_LIST;
    }
    return result;
  }
    
  private void buildEdgesAndCollectDependants(final BinVariable variable){
    final List invocations = Finder.getInvocations(variable, FILTER_VAR);

    if (variable instanceof BinParameter  && ((BinParameter) variable)
        .getMethod() != null){
      analyzeParameterHierarchy((BinParameter) variable);
      analyzePassedArguments((BinParameter) variable);
    } else {
      graph.registerDependancy(variable, extractFrom(variable.getExpression()));
    }

    InvocationData usage;
    for (int i = 0, max = invocations.size(); i < max; ++i){
      usage = (InvocationData) invocations.get(i);
      analyzeUsage(variable, usage.getInConstruct(), usage.getInConstruct()
          .getParent());
    }
  }

  private void analyzeUsage(final Object targetItem,
      SourceConstruct targetConstruct, BinItemVisitable parentItem) {
    if (parentItem instanceof BinAssignmentExpression){
      final BinExpression leftExpr = ((BinAssignmentExpression) parentItem)
          .getLeftExpression();
      final BinExpression rightExpr = ((BinAssignmentExpression) parentItem)
          .getRightExpression();
      graph.registerDependancy(extractFrom(leftExpr), extractFrom(rightExpr));

    } else if (parentItem instanceof BinVariable && targetConstruct
        .getRootAst().getParent().getParent().getType()
        != JavaTokenTypes.COLON){
      // case: List someList = our variable
      graph.registerDependancy(parentItem, targetItem);

    } else if (parentItem instanceof BinExpressionList){
      // case: our variable is passed to method as argument
      final int index = ((BinExpressionList) parentItem).getExpressionIndex(
          (BinExpression) targetConstruct);
      final BinMethod method = ((MethodOrConstructorInvocationExpression)
          parentItem.getParent()).getMethod();
      graph.registerDependancy(method.getParameters()[index], targetItem);

    } else if (parentItem instanceof BinMethodInvocationExpression) {
      graph.registerDependancy(parentItem, targetItem);
    }
  }
  
  private Object extractFrom(BinExpression expr) {
    BinVariable candidate = null;
    if (expr instanceof BinVariableUseExpression){
      candidate = ((BinVariableUseExpression) expr).getVariable();
    } else if (expr instanceof BinFieldInvocationExpression){
      candidate = ((BinFieldInvocationExpression) expr).getField();
    }

    if (isValidCandidateVariable(candidate)){
      return candidate;
    } else if (expr instanceof MethodOrConstructorInvocationExpression){
      return expr;
    } else {
      return null;
    }
  }

  private boolean hasBinaryMethodsInHierarchy(final BinMethod method){
    if (method instanceof BinConstructor){
      return method.getCompilationUnit() == null;
    }

    final BinMethod topMethod = getTopMethod(method);

    Boolean hasBinary = (Boolean) binaryMethods.get(topMethod);
    if (hasBinary == null){
      final List methods = method.getOwner().getBinCIType()
          .getSubMethods(method);
      methods.addAll(method.findAllOverrides());
      methods.add(method);

      for (Iterator it = methods.iterator(); it.hasNext(); ){
        BinMethod curMethod = (BinMethod) it.next();
        if (curMethod.getCompilationUnit() == null){
          hasBinary = new Boolean(true);
          break;
        }
      }
      hasBinary = new Boolean(false);
      binaryMethods.put(topMethod, hasBinary);
    } 
    
    return hasBinary.booleanValue();
  }

  private boolean isValidCandidateVariable(final BinVariable var) {
    return var != null && var.getCompilationUnit() != null
        && ( !(var instanceof BinParameter)
        || ((BinParameter) var).getMethod() == null
        || !hasBinaryMethodsInHierarchy(((BinParameter) var).getMethod()));
  }
  
  private void analyzePassedArguments(final BinParameter param){

    final BinMethod method = param.getMethod();
    final int index = param.getIndex();
    final List invocations = Finder.getInvocations(getTopMethod(method),
        FILTER_METHOD);

    MethodOrConstructorInvocationExpression invocationExpr;
    
    for (int i = 0, max_i = invocations.size(); i < max_i; i++){
      invocationExpr = (MethodOrConstructorInvocationExpression)
          ((InvocationData) invocations.get(i)).getInConstruct();

      if (invocationExpr.getMethod() == method){
        BinExpression[] expressionList = invocationExpr.getExpressionList()
            .getExpressions();
        graph.registerDependancy(param, extractFrom(expressionList[index]));
      }
    }
  }

  private void analyzeParameterHierarchy(final BinParameter parameter){
    final BinMethod method = parameter.getMethod();
    if (method instanceof BinConstructor){
      return;
    }

    final BinMethod topMethod = getTopMethod(method);
    final int place = parameter.getIndex();
    final BinParameter topParameter = topMethod.getParameters()[place];

    if (method != topMethod){
      graph.registerDependancy(parameter, topParameter);
    } else {
      final List methods = method.getOwner().getBinCIType().getSubMethods(method);
      methods.addAll(method.findAllOverrides());
      for (Iterator it = methods.iterator(); it.hasNext(); ){
        BinMethod curMethod = (BinMethod) it.next();
        if (!curMethod.getOwner().getBinCIType().isInterface()){
          graph.registerDependancy(parameter, curMethod.getParameters()[place]);
        }
      }
    }
  }

  public static BinMethod getTopMethod(final BinMethod forMethod){
    List topMethods = forMethod.getTopMethods();
    if (topMethods == null || topMethods.size() == 0){
      return forMethod;
    }
    return (BinMethod) topMethods.get(0);
  }

  public void createEditors(final TransformationManager manager) {
    graph.createEditors(manager);
  }
}
