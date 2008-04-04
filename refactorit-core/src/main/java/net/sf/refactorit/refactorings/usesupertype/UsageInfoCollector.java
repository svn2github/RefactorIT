/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.usesupertype;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinCITypeRef;
import net.sf.refactorit.classmodel.BinEnumConstant;
import net.sf.refactorit.classmodel.BinExpressionList;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.SourceConstruct;
import net.sf.refactorit.classmodel.expressions.BinArrayInitExpression;
import net.sf.refactorit.classmodel.expressions.BinArrayUseExpression;
import net.sf.refactorit.classmodel.expressions.BinAssignmentExpression;
import net.sf.refactorit.classmodel.expressions.BinCITypeExpression;
import net.sf.refactorit.classmodel.expressions.BinCastExpression;
import net.sf.refactorit.classmodel.expressions.BinConditionalExpression;
import net.sf.refactorit.classmodel.expressions.BinConstructorInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinLogicalExpression;
import net.sf.refactorit.classmodel.expressions.BinMemberInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinNewExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.classmodel.statements.BinIfThenElseStatement;
import net.sf.refactorit.classmodel.statements.BinReturnStatement;
import net.sf.refactorit.classmodel.statements.BinVariableDeclaration;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.MapPair;
import net.sf.refactorit.common.util.graph.Digraph;
import net.sf.refactorit.common.util.graph.DigraphTopologicalSorter;
import net.sf.refactorit.parser.FastJavaLexer;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.ManagingIndexer;
import net.sf.refactorit.utils.ProgressShower;
import net.sf.refactorit.utils.RefactorItConstants;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;



/**
 *
 *
 * @author Tonis Vaga
 */
public class UsageInfoCollector {
  private BinTypeRef targetType;

  private final static Logger log=AppRegistry.getLogger(UsageInfoCollector.class);

  /**
   * list of all type usages
   */
  private List usages=new ArrayList();

  /**
   * map where keys are variables or parameters and values are corresponding type usage
   * NB: all same BinParameter's in overwritten methods point to same type usage
   */
  private Map varMap=new HashMap();

  /**
   *  keys are BinMethods and values are corresponding ReturnValueUsage's
   * NB: all overwritten methods point to same usage
   */
  private Map returnValueUsages=new HashMap();

  /**
   * general usages map, experiment, remove later if not needed
   */
  private Map otherUsagesMap=new HashMap();

  private ProgressShower progress;

  /**
   * Case when we use supertype on variable or method, so only single place can be edited
   */
  private List restrictedUsages = new ArrayList(1);

  private List subtypes = new ArrayList();

  private List sortedUsagesList;

  /**
   * true if generate report if can't convert to supertype
   */
  private boolean report;



  private void processInvocations(List invocationData) {
    if (invocationData.size() == 0) {
      if (UseSuperTypeRefactoring.debug) {
        log.debug("No invocations");
      }
    }

    for (int i = 0; i < invocationData.size(); i++) {
      InvocationData data = (InvocationData) invocationData.get(i);
      if (UseSuperTypeRefactoring.debug) {
        log.debug("data = " + data);
      }

      processInvocation(data);
      progress.showProgress(i, invocationData.size());
    }
  }

  private void processInvocation(InvocationData data) {
    SourceConstruct inConstruct = data.getInConstruct();

    if (inConstruct == null) {
      processReturnTypeData(data);
    } else if (inConstruct instanceof BinCITypeExpression) {
      processBinCITypeExpression((BinCITypeExpression) inConstruct);
    } else if (inConstruct instanceof BinReturnStatement) {
      processReturnStatement((BinReturnStatement) inConstruct);
    } else if (inConstruct instanceof BinMemberInvocationExpression) {
      processBinMemberInvocation((BinMemberInvocationExpression) inConstruct);
    } else if (inConstruct instanceof BinVariableUseExpression) {
      processVariableOrMethodUseExpression((BinVariableUseExpression)
          inConstruct);
    } else if (inConstruct instanceof BinVariable) {
      BinItem member = null;
      BinVariable var = (BinVariable) inConstruct;
      addVarUsage(var, member);
      BinMember assignMember = getMemberFromExpression(var.getExpression());
      if (assignMember != null && (assignMember instanceof BinMethod)) {
        addReturnValueUsage(assignMember, var);
      }
    } else if (inConstruct instanceof BinCastExpression) {
      // find out from what this BinCastExpression depends on
      MapPair dependency = processMemberExpressionParent((BinExpression)
          inConstruct, (BinItem) inConstruct);
      if (dependency != null && (dependency.getKey() instanceof BinItem)) {
        addUsage((BinItem) dependency.getKey(), (BinItem) dependency.getValue());
      }
    }
  }

  private void processBinCITypeExpression(final BinCITypeExpression x) {
    if (x.getParent() instanceof BinMemberInvocationExpression) {
      processBinMemberInvocation((BinMemberInvocationExpression) x.getParent());
    }

    usages.add(new BinCITypeExpressionUsage(x));
  }

  private void processVariableOrMethodUseExpression(BinExpression expr) {
    if (!(expr instanceof BinVariableUseExpression)
        && !(expr instanceof BinMemberInvocationExpression)) {
      Assert.must(false, "wrong expression " + expr);
      return;
    }

    BinItem member = null;
    if (UseSuperTypeRefactoring.debug) {
      log.debug("inContstruct " + expr + ", parent: " +
          expr.getParent().getClass() + " " +
          expr.getParent());
    }

    if (expr instanceof BinVariableUseExpression) {
      member = ((BinVariableUseExpression) expr).getVariable();
    } else {
      member = ((BinMemberInvocationExpression) expr).getMember();
    }

    MapPair pair;
    BinExpression exprToProcess = expr;

    if (expr.getParent() instanceof BinArrayUseExpression) {
//      BinItemVisitable pp = expr.getParent().getParent();
//      if ( pp instanceof BinReturnStatement ) {
//        //addReturnValueUsage();
//      } else if (!(pp instanceof BinExpression) && !(pp instanceof BinExpressionList)) {
//        DebugInfo.trace("Unexpected parent type " + pp+" class == "+pp.getClass());
//        return;
//      }
      exprToProcess = (BinExpression) expr.getParent();
    } else if(expr.getParent() instanceof BinArrayInitExpression) {
      exprToProcess = (BinExpression) expr.getParent();
      if(exprToProcess.getParent() instanceof BinExpression){
        exprToProcess = (BinExpression) exprToProcess.getParent();
      }
    } else if (expr.getParent() instanceof BinConditionalExpression) {
      if(member instanceof BinVariable 
          && Project.getProjectFor(member).getOptions().getJvmMode() != FastJavaLexer.JVM_50) {
        VariableUsage usg = getVarUsage((BinVariable)member);
        usg.setRestricted(true);
      } 
      exprToProcess = (BinExpression) expr.getParent();
    }
    
    pair = processMemberExpressionParent(exprToProcess, member);
    
    if (pair != null && pair.getKey() != null) {
      addUsage((BinItem) pair.getKey(), (BinItem) pair.getValue());
    }
  }

  private MapPair processMemberExpressionParent(BinExpression expr,
      BinItem member) {
    
    MapPair pair = null;
    BinItem left = member;
    BinItem right = null;
    BinItem parent = (BinItem) expr.getParent();

    if (parent instanceof BinLogicalExpression) {
      return null;
    }

    if (parent instanceof BinArrayInitExpression) {
      BinArrayInitExpression arrayInit = (BinArrayInitExpression) parent;
      right = getLeftSide(arrayInit);
      left = getDependentFromItem(member);
    } else if (parent instanceof BinExpressionList) {
      BinMethod method = null;

      if (parent.getParent() instanceof BinMethodInvocationExpression) {
        method = ((BinMethodInvocationExpression) parent.getParent()).getMethod();
      } else if (parent.getParent() instanceof
          BinConstructorInvocationExpression) {
        method = (((BinConstructorInvocationExpression) parent.getParent()).
            getConstructor());
      } else if (parent.getParent() instanceof BinNewExpression) {
        BinNewExpression newExpr = (BinNewExpression) parent.getParent();
            method = newExpr.getConstructor();
        if (method == null) {
          return null;
        }
      } else {
        AppRegistry.getLogger(this.getClass()).debug("expression list but not method invocation?? : "
        + parent.getParent());
      }
      if (method != null) {
        BinExpressionList exprList = (BinExpressionList) parent;
        int paramIndex = exprList.getExpressionIndex(expr);
        BinParameter param = method.getParameters()[paramIndex];
        left = member;
        right = (param);
      }
    } else if (parent instanceof BinAssignmentExpression) {
      BinAssignmentExpression assignment = (BinAssignmentExpression) parent;
      if (UseSuperTypeRefactoring.debug) {
        log.debug("processing assingment " + assignment + " left ==" +
            assignment.getLeftExpression());
      }
      BinMember rightMember = null;
      if (assignment.getLeftExpression() == expr) {
        rightMember = getMemberFromExpression(assignment.getRightExpression());
        if (rightMember instanceof BinVariable) {
          right = member;
          left = (rightMember);
        }
      } else if (assignment.getRightExpression() == expr) {
        right = getMemberFromExpression(assignment.getLeftExpression());
        left = member;
      } else {
        if (UseSuperTypeRefactoring.debug) {
          log.debug("assignment expression with parent " + parent +
              " ignored");
        }
      }
    } else if (parent instanceof BinMemberInvocationExpression) {
      left = member;
      right = parent;
    } else if ((parent instanceof BinVariable)
        && (parent.getParent() instanceof BinVariableDeclaration)) {
      left = member;
      right = parent;
    } else if (parent instanceof BinReturnStatement) {
      BinReturnStatement returnSt = (BinReturnStatement) parent;

      left = member;
      right = returnSt.getMethod();
    } else {
      if (RefactorItConstants.debugInfo) {
        if (parent instanceof BinCastExpression) {
          // ignore, should get processd elsewhere or doesn't matter
        } else if (parent instanceof BinIfThenElseStatement) {
          // doesn't matter
        } else {
          if (UseSuperTypeRefactoring.debug) {
            log.debug("Ignored memberUseExpression " +
                expr + " with parent " + parent.getClass());
          }
        }
      }
    }

    if (left != null && right != null) {
      pair = new MapPair(left, right);
    } else {
      if (UseSuperTypeRefactoring.debug) {
        log.debug("ignoring item: left=" + left + ", right=" + right);
      }
    }

    return pair;
  }

  private void processBinMemberInvocation(BinMemberInvocationExpression expr) {
    if (expr.getMember() instanceof BinEnumConstant) {
      return;
    }
    checkReturnValueUsage(expr);
    BinExpression expr2 = expr.getExpression();

    // don't need check method parameters
    if (expr2 instanceof BinMemberInvocationExpression) {
      checkReturnValueUsage((BinMemberInvocationExpression) expr2);
    }

    processVariableOrMethodUseExpression(expr);
  }

  private static BinParameter getParameter(BinExpression expr,
      BinExpressionList parList) {
    BinItemVisitable parent = expr.getParent();
    if ((parent instanceof BinExpressionList)
        && (parent.getParent() instanceof BinMethodInvocationExpression)) {
      int index = parList.getExpressionIndex(expr);

      BinParameter param = ((BinMethodInvocationExpression) parent.getParent())
          .getMethod().getParameters()[index];
      return param;
    } else {
      return null;
    }
  }

  private void checkReturnValueUsage(BinMemberInvocationExpression memInv) {
    if (UseSuperTypeRefactoring.debug) {
      log.debug("checking returnVal for " + memInv);
    }

    BinMember member = memInv.getMember();
    if (!checkIsTargetOrTargetArray(memInv.getReturnType())) {
      return;
    }

    BinItem parent = (BinItem) memInv.getParent();
    if (UseSuperTypeRefactoring.debug) {
      log.debug("target Type, parent=" + memInv.getParent());
    }

    BinItem leftItem = getLeftItem(memInv);
    if (leftItem == null) {
      return;
    }

    BinItem usg = parent;
    if (usg instanceof BinLogicalExpression) {
      usg = null;
    }

    if (member instanceof BinMethod) {
      ensureReturnValUsage((BinMethod) member);
      ReturnValueUsage usage = (ReturnValueUsage) returnValueUsages.get(
          member);
      usage.addUsage(usg);
      returnValueUsages.put( member,  usage);
    } else {
      addVarUsage((BinVariable) member, usg);
    }
  }

  private boolean isTargetType(BinVariable var) {
    BinTypeRef typeRef = var.getTypeRef();
    return checkIsTargetOrTargetArray(typeRef);
  }

  private void processReturnTypeData(InvocationData data) {
    if (!(data.getWhere() instanceof BinMethod)) {
      return;
    }
    BinMethod method = (BinMethod) data.getWhere();
    if (checkIsTargetOrTargetArray(method.getReturnType())) {
      ensureReturnValUsage(method);
    }
  }
   /**
   *
   * create empty usage in map if doesn't exist
   * @param method
   */
  private ReturnValueUsage ensureReturnValUsage(BinMethod method) {
    ReturnValueUsage usage = (ReturnValueUsage) returnValueUsages.get(method);

    if (usage != null) {
      return usage;
    }
    usage = new ReturnValueUsage(method);
    usages.add(usage);
    if(!returnValueUsages.containsKey(method)) {
      List overriddes = method.findAllOverridesOverriddenInHierarchy();
      overriddes.add(method);
      BinMethod item;
  
      for (Iterator iter = overriddes.iterator(); iter.hasNext(); ) {
        item = (BinMethod) iter.next();
        if ( returnValueUsages.put(item, usage) != null ) {
          log.warn("overwriting return value usage for "+item.getQualifiedName());
        }
      }
    }

    return usage;
  }
  /**
   * Contains info which usages we can convert to supertype
   * and failure info for others
   *
   */
  public static class ConvertResult {
    Set usagesToChange;
    BinCIType supertype;
    /**
     * @param superClass
     */
    public ConvertResult(BinCIType superClass) {
      supertype=superClass;
    }

    public Set getUsagesToChange() {
      return usagesToChange;
    }

    /**
     * map of reasons while converting to supertype fails, key is type usage and
     * value is list of reasons
     */
    Map failuresMap=new HashMap();
    /**
     */
    public List getAllFailures() {
      List result=new ArrayList();
      for (Iterator iter = failuresMap.values().iterator(); iter.hasNext();) {
        List element = (List) iter.next();
        result.addAll(element);
      }
      return result;
    }
  }
  /**
   * Computes convert result for collected usage info
   * @param superClass to use
   */
  public ConvertResult computeConvertResult(BinCIType superClass) {
    List resolvedList = new ArrayList(sortedUsagesList.size());

    List resolvedMembers = new ArrayList(sortedUsagesList.size());

    SuperClassInfo superInf = new SuperClassInfo(superClass.getTypeRef());
    
    
    final ArrayList singleElementList=new ArrayList(1);
    singleElementList.add("");

    ConvertResult result=new ConvertResult(superClass);

    for (int i = 0; i < sortedUsagesList.size(); ++i) {
      Object item = sortedUsagesList.get(i);
      List usagesList;

      if ( item instanceof List ) {
        usagesList=(List) item;
        // cyclic components, can be resolved only together, add to list and remove if fails
        addResolvedMembers(resolvedMembers,usagesList);
      } else {
        singleElementList.set(0,item);
        usagesList=singleElementList;
      }
      boolean resolved=true;



      List failuresList=null;
      for (int j = 0; j < usagesList.size(); ++j) {
        TypeUsage usage = (TypeUsage) usagesList.get(j);

        if ( report ) {
          failuresList=new ArrayList();
        }
        if (restrictedUsages.size() > 0 && !restrictedUsages.contains(usage)) {
          resolved=false;
        } else if ( !usage.checkCanUseSuper(superInf, resolvedMembers,failuresList)) {

          if ( failuresList != null ) {
            result.failuresMap.put(usage,failuresList);
          }
          resolved=false;
        }
        if (!resolved) {
          if (UseSuperTypeRefactoring.debug) {
            log.debug("REJECTED usage: " + usage + " with dependencies "
                + usage.getDependsFrom());
          }
          break;
        }

      }

      if ( resolved ) {
        resolvedList.addAll(usagesList);
        addResolvedMembers(resolvedMembers, usagesList);
      } else {
        if ( usagesList.size() > 1 ) {
          // remove previously added cyclic components
          removeResolvedMembers(resolvedMembers,usagesList);
        }
      }

    }

    checkMultiDeclarationVariables(resolvedList);
    result.usagesToChange=new HashSet(resolvedList);

    return result;
  }

  private void checkForDuplicateUsages() {
    Set testSet=new HashSet(usages);
    if ( testSet.size() != usages.size() ) {
      ArrayList duplicates = new ArrayList(usages);

      for (Iterator iter = testSet.iterator(); iter.hasNext();) {
        duplicates.remove(iter.next());
      }
      log.warn("found folowing duplicate usages: "+duplicates);
    }
  }

  /**
   * @param resolvedMembers
   * @param usages
   * @returns methods which override + methods which method overrides
   */
  private static void removeResolvedMembers(List resolvedMembers, List usages) {
    for (int i = 0; i < usages.size(); ++i) {
      TypeUsage usage = (TypeUsage) usages.get(i);
      if (usage instanceof ParameterUsage) {
        CollectionUtil.removeAll(resolvedMembers,((ParameterUsage) usage).getOverridenParameters() );
      }
      resolvedMembers.remove(usage.getWhat());

    }

  }

  private static void addResolvedMembers(List resolvedMembers,List usages) {

    for (int i = 0; i < usages.size(); ++i) {
      TypeUsage usage = (TypeUsage) usages.get(i);
      if (usage instanceof ParameterUsage) {
        CollectionUtil.addAll(resolvedMembers,
            ((ParameterUsage) usage).getOverridenParameters());
      }
      resolvedMembers.add(usage.getWhat());

    }

  }

  private void processReturnStatement(BinReturnStatement expr) {
    if (!checkIsTargetOrTargetArray(expr.getReturnExpression().getReturnType())) {
      Assert.must(false);
      return;
    }
    BinExpression returnExpr = expr.getReturnExpression();

    if (returnExpr instanceof BinMemberInvocationExpression) {
      addReturnValueUsage(((BinMemberInvocationExpression) returnExpr).
          getMember(), expr);
    } else if (returnExpr instanceof BinVariableUseExpression) {
      addReturnValueUsage(((BinVariableUseExpression) returnExpr).getVariable(),
          expr);
    }
  }

  private static BinMember getMemberFromExpression(BinExpression expr) {
    if (expr instanceof BinMemberInvocationExpression) {
      return ((BinMemberInvocationExpression) expr).getMember();
    }
    
    if (expr instanceof BinArrayUseExpression) {
      while(expr instanceof BinArrayUseExpression){
        expr = ((BinArrayUseExpression) expr).getArrayExpression();
      } 
    }
    
    if (expr instanceof BinFieldInvocationExpression){
      return ((BinFieldInvocationExpression)expr).getField();
    } 
    
    if (expr instanceof BinVariableUseExpression) {
      return (((BinVariableUseExpression) expr).getVariable());
    } else {
      return null;
    }
  }

  private void addReturnValueUsage(BinMember member, BinItem item) {
    if (member instanceof BinVariable) {
      BinVariable var = (BinVariable) member;
      if (!checkIsTargetOrTargetArray(var.getTypeRef())) {
        return;
      }
      addVarUsage(var, item);
    } else if (member instanceof BinMethod) {
      BinMethod method = (BinMethod) member;
      if (!checkIsTargetOrTargetArray(method.getReturnType())) {
        return;
      }
      if (UseSuperTypeRefactoring.debug) {
        log.debug("adding return value usage " + method + " :" + item);
      }
      ensureReturnValUsage(method);
      ReturnValueUsage usg = (ReturnValueUsage) returnValueUsages.get(member);
      usg.addUsage(item);
    }
  }

  private void addVarUsage(BinVariable var, BinItem member) {
    if (var == null) {
      return;
    }
    if (var == member) {
      if (UseSuperTypeRefactoring.debug) {
        log.debug("addVarUsage where var == member :" + var);
      }
      member = null;
    }
    if (!isTargetType(var)) {
      if (UseSuperTypeRefactoring.debug) {
        log.debug("variable  " + var + " ignored, not target type");
      }
      return;
    }
    
    VariableUsage usg = getVarUsage(var);
    
    if (var != member) {
      if (UseSuperTypeRefactoring.debug) {
        log.debug("adding var usage for " + var + ": " + member);
      }
      usg.addUsage(member);
    } else {
      if (UseSuperTypeRefactoring.debug) {
        log.debug("addVarUsage: rejected usage where member == var " +
            var);
      }
    }
  }
  
  private VariableUsage getVarUsage(BinVariable var) {
    VariableUsage usg = (VariableUsage) varMap.get(var);
    if (usg == null) {
      usg=VariableUsage.create(var);
    }
    usages.add(usg);
    if (usg instanceof ParameterUsage) {
      // map overriden parameters to same usg
      ParameterUsage pUsg = (ParameterUsage) usg;
      BinParameter pars[] = pUsg.getOverridenParameters();
      for (int i = 0; i < pars.length; i++) {
        if ( varMap.put( pars[i], usg) != null ) {
          log.warn("overwriting parameter usage "+pars[i].getQualifiedName());
        }
      }
    }
    varMap.put(var, usg);
    return usg;
  }

  private static BinItem getLeftItem(BinMemberInvocationExpression memInv) {
    BinItem parent = (BinItem) memInv.getParent();
    if ((parent instanceof BinVariable) || (parent instanceof BinMember)
        || (parent instanceof BinMemberInvocationExpression)) {
      return parent;
    }
    if (parent instanceof BinLogicalExpression) {
      return (((BinLogicalExpression) parent).getLeftExpression());
    } else {
      return null;
    }
  }
  private List sortTopologically(final List srcList) {
    Set processingSet = new LinkedHashSet(srcList);

    if ( processingSet.size() < 2) {
      return (new ArrayList(processingSet));
    }

//    if ( !RefactorItConstants.developingMode ) {
//      return sortTopologicallyOld(srcList);
//    }

    Map what2Usage=new HashMap(processingSet.size()+10);

    for (int i = 0; i < usages.size(); ++i) {
      TypeUsage item = (TypeUsage) usages.get(i);

      if ( what2Usage.put(item.getWhat(),item) != null ) {
        // FIXME: check this case
        log.warn("existing mapping found for "+item);
      }

    }
    what2Usage.putAll(varMap);
    what2Usage.putAll(returnValueUsages);

    Digraph graph=new Digraph(processingSet);

    TypeUsage item;


    for (Iterator iter = processingSet.iterator(); iter.hasNext(); ) {
      item = (TypeUsage) iter.next();
      Set dependsFrom = item.getDependsFrom();
      for (Iterator iterator = dependsFrom.iterator(); iterator.hasNext();) {
        Object dep = iterator.next();
        Object outNode = what2Usage.get(dep);

        if ( outNode == null ) {
          // probably dependency which we don't change, for example
          // some method parameter which is not target type but some supertype of it
          // If we don't edit it then we don't need to sort it
          if ( UseSuperTypeRefactoring.debug ) {
            log.debug("usage not found for dependent item "+dep);
          }
        } else {
          graph.addEdge(item,outNode);
        }


      }

    }
    DigraphTopologicalSorter sorter=new DigraphTopologicalSorter(graph);

    return sorter.sortTopologically();
  }


//  private List sortTopologicallyOld(final List srcList) {
//    Set processingSet = new HashSet(srcList);
//
//    if ( processingSet.size() < 2) {
//      return (new ArrayList(processingSet));
//    }
//    List result = new ArrayList(srcList.size());
//    MultiValueMap reverseDeps = new MultiValueMap(srcList.size());
//    int srcSize = srcList.size();
//
//    Map dependenciesCount = new HashMap(srcSize + 1, 1.0F);
//    TypeUsage item;
//    for (Iterator iter = processingSet.iterator(); iter.hasNext(); ) {
//      item = (TypeUsage) iter.next();
//
//      Set dependsFrom = item.getDependsFrom();
//      dependenciesCount.put(item, new Integer(dependsFrom.size()));
//
//      for (Iterator iter2 = dependsFrom.iterator(); iter2.hasNext(); ) {
//        BinItem depItem = (BinItem) iter2.next();
//        reverseDeps.put(depItem, item);
//      }
//
//    }
//
//    boolean finished;
//    do {
//      finished = true;
//      for (Iterator iter = processingSet.iterator(); iter.hasNext(); ) {
//        item = (TypeUsage) iter.next();
//        Integer depSize = (Integer) dependenciesCount.get(item);
//        if (depSize.intValue() == 0) {
//          iter.remove();
//          result.add(item);
//          finished = false;
//          List rd = reverseDeps.get(item.getWhat());
//          if (rd != null) {
//            Iterator iter2 = rd.iterator();
//            while (iter2.hasNext()) {
//              TypeUsage tu = (TypeUsage) iter2.next();
//              Integer nconnect = (Integer) dependenciesCount.get(tu);
//              if (nconnect != null) {
//                Assert.must(nconnect.intValue() > 0);
//                dependenciesCount.put(tu,new Integer(nconnect.intValue() - 1) );
//              }
//            }
//          }
//        }
//      }
//
//      if (processingSet.size() == 0) {
//        finished = true;
//      }
//    } while (!finished);
//    if (processingSet.size() > 0) {
//      log.debug(
//          "Topological sorting find following elements with CYCLIC dependencies " +
//          processingSet);
//      result.addAll(processingSet);
//    }
//    if (result.size() == 0) {
//      return (new ArrayList(0));
//    } else {
//      return result;
//    }
//  }
  private void addUsage(BinItem target, BinItem uses) {
    if ((target instanceof BinVariable) || (target instanceof BinMethod)) {
      addReturnValueUsage((BinMember) target, uses);
    } else
    if (target instanceof BinCastExpression) {
      addCastUsage((BinCastExpression) target, uses);
    }
  }

  private BinItem getLeftSide(BinArrayInitExpression arrayInit) {
    BinItemVisitable parent = arrayInit.getParent();
    BinItem rootParent;
    if (parent instanceof BinNewExpression) {
      rootParent = (BinItem) parent.getParent();
    } else {
      rootParent = (BinItem) parent;
    }
    if (parent instanceof BinAssignmentExpression) {
      rootParent = ((BinAssignmentExpression) parent).getLeftExpression();
    }
    if (rootParent instanceof BinExpressionList) {
      BinParameter par = getParameter((BinExpression) arrayInit.getParent(),
          (BinExpressionList) rootParent);
      return (par);
    }
    if (rootParent instanceof BinVariable) {
      return rootParent;
    }
    if (rootParent instanceof BinVariableUseExpression) {
      return (((BinVariableUseExpression) rootParent).getVariable());
    }
    if (rootParent instanceof BinFieldInvocationExpression) {
      return (((BinFieldInvocationExpression) rootParent).getField());
    } else {
      return null;
    }
  }

  private BinItem getDependentFromItem(BinItem item) {
    if (item instanceof BinMember) {
      return item;
    }
    if (item instanceof BinVariableUseExpression) {
      return (((BinVariableUseExpression) item).getVariable());
    }
    if (item instanceof BinMemberInvocationExpression) {
      return item;
    } else {
      return null;
    }
  }

  private void addCastUsage(BinCastExpression target, BinItem uses) {

    CastUsage castUsg=(CastUsage) otherUsagesMap.get(target);

    if ( castUsg != null ) {
      //log.debug("found cast usage for "+target);
      castUsg.addUsage(uses);
    } else {
      castUsg = new CastUsage(target, uses);
      otherUsagesMap.put(target,castUsg);
      usages.add(castUsg);
    }
    if (UseSuperTypeRefactoring.debug) {
      log.debug("adding cast usage " + castUsg);
    }
  }

  private boolean checkIsTargetOrTargetArray(BinTypeRef ref) {
    BinTypeRef nonArrayType = ref.getNonArrayType();

//    if (nonArrayType.getBinType().isTypeParameter()) {
//      List supertypes = nonArrayType.getSupertypes();
//      for (int i = 0, max = supertypes.size(); i < max; i++) {
//        if (subtypes.contains(supertypes.get(i))) {
//          return false;
//        }
//      }
//      return true;
//    } else {
      return subtypes.contains(nonArrayType);
//    }
  }

  private static void checkMultiDeclarationVariables(Collection membersToChange) {
    List multiDeclVariables = new ArrayList(membersToChange.size() + 1);
    Iterator iter = membersToChange.iterator();
    do {
      if (!iter.hasNext()) {
        break;
      }
      TypeUsage usage = (TypeUsage) iter.next();
      if (usage.getWhat() instanceof SourceConstruct) {
        SourceConstruct item = (SourceConstruct) usage.getWhat();
        if (item instanceof BinVariable) {
          BinVariable target = (BinVariable) item;
          if (target.getParent() instanceof BinVariableDeclaration) {
            BinVariableDeclaration varDecl = (BinVariableDeclaration) target.
                getParent();
            BinVariable vars[] = varDecl.getVariables();
            if (vars.length > 1) {
              multiDeclVariables.add(target);
              iter.remove();
            }
          }
        }
      }
    } while (true);
    Set declsToAdd = new HashSet();
    for (int i = 0; i < multiDeclVariables.size(); i++) {
      BinVariableDeclaration variableDecl = (BinVariableDeclaration) ((
          BinVariable)
          multiDeclVariables.get(i)).getParent();
      BinVariable vars[] = variableDecl.getVariables();
      boolean add = true;
      int j = 0;
      do {
        if (j >= vars.length) {
          break;
        }
        if (!multiDeclVariables.contains(( (vars[j])))) {
          add = false;
          break;
        }
        j++;
      } while (true);
      if (add) {
        declsToAdd.add(variableDecl);
      }
    }

    BinVariableDeclaration item;
    for (Iterator iter2 = declsToAdd.iterator(); iter2.hasNext(); ) {
      item = (BinVariableDeclaration) iter2.next();
      membersToChange.add(VariableUsage.create(item.getVariables()[0]));
    }

  }

  public void collectUsages(ProgressShower pr) {
    List invocationData;

    Assert.must(pr != null);

    this.progress = pr;

    if (restrictedUsages.size() > 0) {
      invocationData = getRestrictedUsages();
    } else {
      ManagingIndexer supervisor = new ManagingIndexer();
      Project project = null;

      progress.showMessage("Finding usages of subtypes...");

      for (int i = 0; i < subtypes.size(); ++i) {
        BinCITypeRef subtype = (BinCITypeRef) subtypes.get(i);

        new UseSuperTypeIndexer(supervisor, subtype.getBinCIType());

        if (project == null) {
          project = subtype.getProject();
        }
      }
      if (project != null) {
        project.accept(supervisor);
        invocationData = supervisor.getInvocations();
      } else { // hmm??? isn't it an error
        invocationData = CollectionUtil.EMPTY_ARRAY_LIST;
      }
    }

    progress.showMessage("Computing possible changes");
    processInvocations(invocationData);

    if ( log.isDebugEnabled() ) {
      checkForDuplicateUsages();
    }
    sortedUsagesList = sortTopologically(usages);
  }

  /**
   * precond: {@link #restrictedUsages} contains only VariableUsages
   *  or ReturnValueUsages (method)
   */
  private List getRestrictedUsages() {

    List invocationData = new ArrayList();

    for (Iterator iter = restrictedUsages.iterator(); iter.hasNext(); ) {
      TypeUsage item = (TypeUsage) iter.next();

      if (item instanceof ParameterUsage) {

        ParameterUsage usg = (ParameterUsage) item;

        BinParameter pars[] = usg.getOverridenParameters();

        for (int i = 0; i < pars.length; i++) {
          invocationData.addAll(UseSuperTypeUtil.getMemberInvocations(pars[i]));
        }

      } else if (item instanceof ReturnValueUsage) {
        ReturnValueUsage usg = (ReturnValueUsage) item;

        List overriddenMethods = UseSuperTypeUtil.getAllOverrides(usg.method);

        // get usages for overrides

        for (Iterator oIter = overriddenMethods.iterator(); oIter.hasNext(); ) {
          BinMethod method = (BinMethod) oIter.next();
          invocationData.addAll(UseSuperTypeUtil.getMemberInvocations(method));
        }

      }
      invocationData.addAll(UseSuperTypeUtil.getMemberInvocations((BinMember)
          item.getWhat()));
    }
    return invocationData;
  }
  /**
   * @param targetItems list of BinMembers to search for possible replacement
   */
  public UsageInfoCollector(List targetItems) {

    for (int i = 0; i < targetItems.size(); ++i) {

      BinMember targetItem = (BinMember) targetItems.get(i);
      BinTypeRef tmp = UseSuperTypeUtil.getTargetType(targetItem);

      Assert.must(tmp.isReferenceType(), " wrong target type '" + tmp+"'");

      targetType = tmp;

      subtypes.add(targetType);

      if (targetItem instanceof BinVariable) {
        BinVariable var = (BinVariable) targetItem;
        addVarUsage(var, null);
        final VariableUsage usg = (VariableUsage) varMap.get(var);
        Assert.must(usg != null);
        restrictedUsages.add(usg);
      } else if (targetItem instanceof BinMethod) {
        BinMethod method = (BinMethod) targetItem;
        ensureReturnValUsage(method);

        ReturnValueUsage usg = (ReturnValueUsage) returnValueUsages.get(method);
        Assert.must(usg != null);
        restrictedUsages.add(usg);
      }
    }


    }

  /**
   *
   * @param report true if collect report why can't convert
   */
  public void setCollectReport(boolean report) {
    this.report=report;
  }

}
