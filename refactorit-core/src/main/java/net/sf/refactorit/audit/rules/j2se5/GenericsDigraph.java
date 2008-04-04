/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.audit.rules.j2se5;

import net.sf.refactorit.classmodel.BinExpressionList;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.expressions.BinAssignmentExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.classmodel.expressions.MethodOrConstructorInvocationExpression;
import net.sf.refactorit.common.util.graph.MatrixDigraph;
import net.sf.refactorit.parser.JavaTokenTypes;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.transformations.TransformationManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author  Arseni Grigorjev
 */
public class GenericsDigraph extends MatrixDigraph {
  
  private GenericsNode centerNode;
  private boolean stable = false;
  
  // GENERICS NODES FACTORY
  private Map nodesMap = new HashMap(20);

  private GenericsNode getNodeInstanceFor(final Object obj) {
    GenericsNode node = (GenericsNode) nodesMap.get(obj);
    if (node == null){
      node = GenericsNode.getInstance(obj);
      if (node != null){
        nodesMap.put(obj, node);
      }
    }
    return node;
  }
  
  public GenericsDigraph(final BinVariable target) {
    this.centerNode = getNodeInstanceFor(target);
    addVertex(centerNode);
  }
  
  public GenericsNode getCenterNode(){
    return centerNode;
  }
  
  public void prepareNodes(){
    for (Iterator vertices = getVertices().iterator(); vertices.hasNext(); ){
      try {
        ((GenericsNode) vertices.next()).prepare();
      } catch (GenericsNodeUnresolvableException e){
        removeVertex(e.getNode());
      }
    }
  }
  
  public boolean stabilize() {
    final List vertices = getVertices();
    
    int pass = 0;
    do {
//      System.err.println("[Graph Pass " + (nPass) + "]");
//      System.out.println("[Graph Pass " + (nPass++) + "]");
      pass++;
      if (pass > 20){
        return false;
      }

      clearAllFlags();

      collectFromBelowUpwards(centerNode);
      for (int i = 0, max_i = vertices.size(); i < max_i; i++){
//        System.out.println("{outter entrance}");
        collectFromBelowUpwards((GenericsNode) vertices.get(i));
      }
    } while (!stable);
    return true;
  }
  
  public void collectFromBelowUpwards(GenericsNode currentNode){
    if (currentNode.isResolved() || currentNode.isLocked()){
      return;
    }
    currentNode.setLocked(true);
    
//    System.out.println("<-collect- (" + currentNode + ")");

    try {
      final List outgoingNodes = getOutgoingNodes(currentNode);
      
      for (int i = 0, max_i = outgoingNodes.size(); i < max_i; i++){
        collectFromBelowUpwards((GenericsNode) outgoingNodes.get(i));
      }

      if (currentNode.softImport(outgoingNodes)){
        stable = false;
      }

      for (int i = 0, max_i = outgoingNodes.size(); i < max_i; i++){
        distributeFromTopToDown((GenericsNode) outgoingNodes.get(i),
            currentNode);
      }

      currentNode.setResolved(true);
      currentNode.setLocked(false);
    } catch (GenericsNodeUnresolvableException e){
      removeVertexWithBrothers(e.getNode());
      removeUnconnectedGraphParts(centerNode);
    }
  }

  private void distributeFromTopToDown(GenericsNode currentNode,
      GenericsNode incomingNode) {
    if (currentNode.isLocked()){ // FIXME: isResolved() too ?
      return;
    }
    currentNode.setLocked(true);
    
//    System.out.println("-distrib-> (" + currentNode + ")");
    
    try {
      if (currentNode.hardImport(incomingNode)){
//        System.out.println("unstable after hard import");
        stable = false;

        GenericsNode outgoingNode;
        final List outgoingNodes = getOutgoingNodes(currentNode);
        for (int i = 0, max_i = outgoingNodes.size(); i < max_i; i++){
          outgoingNode = (GenericsNode) outgoingNodes.get(i);

          distributeFromTopToDown(outgoingNode, currentNode);
        }
      } else {
//        System.out.println("no changes after hard import - breaking");
      }
    } catch (GenericsNodeUnresolvableException e){
      removeVertexWithBrothers(e.getNode());
      removeUnconnectedGraphParts(centerNode);
    }

    currentNode.setLocked(false);
  }
  
  private void clearAllFlags(){
    stable = true;

    final List vertices = getVertices();

    GenericsNode currentNode;
    for (int i = 0, max_i = vertices.size(); i < max_i; i++){
      currentNode = (GenericsNode) vertices.get(i);
      currentNode.setLocked(false);
      currentNode.setResolved(false);
    }
  }
  
  public String toString(){
    String result = "";
    result += "graph vercities:\n";
    for (final Iterator it = getVertices().iterator(); it.hasNext(); ){
      result += "    " + it.next() + "\n";
    }
    
    result += "graph edges:\n";
    for (final Iterator it = getVertices().iterator(); it.hasNext();) {
      Object element = it.next();
 
      List list = getOutgoingNodes(element);
      
      for (int i = 0; i < list.size(); ++i) {
        Object outNode = list.get(i);
        result += "    "+element+"---->"+outNode + "\n";
      }
      
    }

    return result;
  }

  public void addEdge(Object a, Object b){
    if (!a.equals(b)){
      addVertex(a);
      addVertex(b);
      super.addEdge(a, b);
    }
  }

  public void addVertex(Object newNode){
    ((GenericsNode) newNode).setInGraph(true);
    super.addVertex(newNode);
  }

  public void removeVertex(final Object obj){
//    System.out.println("REMOVE (!!!): " + obj);
    ((GenericsNode) obj).setInGraph(false);
    super.removeVertex(obj);
  }

  public void removeVertexWithBrothers(final Object obj){
//    System.out.println("REMOVE_ext (!!!): " + obj);
    final GenericsNode node = (GenericsNode) obj;
    boolean removeBrothers = node.isMethodParameterNode();
    final List outgoingNodes = removeBrothers ? getOutgoingNodes(node) : null;
    removeVertex(node);

    if (removeBrothers){
      for (final Iterator it = outgoingNodes.iterator(); it.hasNext(); ){
        GenericsNode nextNode = (GenericsNode) it.next();
        if (nextNode.isMethodParameterNode() && haveSameMethodHierarchy(
            (BinParameter) node.getVariable(),
            (BinParameter) nextNode.getVariable())){
          removeVertex(nextNode);
        }
      }
    }
  }

  public boolean haveSameMethodHierarchy(BinParameter param1, BinParameter param2) {
    if (param1.getIndex() != param2.getIndex()){
      return false;
    }

    final List topMethods1 = param1.getMethod().getTopMethods();
    final List topMethods2 = param2.getMethod().getTopMethods();

    if (topMethods1.size() > 0 && topMethods2.size() > 0){
      return topMethods1.equals(topMethods2);
    } else if (topMethods1.size() == 0 && topMethods2.size() == 0){
      return param1.getMethod().equals(param2.getMethod());
    } else if (topMethods1.size() == 0){
      return topMethods2.contains(param1.getMethod());
    } else if (topMethods2.size() == 0){
      return topMethods1.contains(param2.getMethod());
    } else {
      return false;
    }
  }

  public void debugResults(String when){
      System.out.println("=====================================================");
      System.out.println("[ARS>>] "+when+" graph analysis: ");
    List vertices = getVertices();
    for (int i = 0; i < vertices.size(); i++){
      GenericsNode node = (GenericsNode) vertices.get(i);
      if (node.isVariableNode()){
          System.out.println("[GRAPH DEBUG] current node: " + node.getVariable());
      }
      node.debugStates();
    }
  }

  public void checkCompleteness(final RefactoringStatus status) {
    if (!centerNode.isInGraph()){
      status.addEntry("Corrective action could not propose type arguments for target.",
          RefactoringStatus.CANCEL);
    } else {
      GenericsNode currentNode;
      for (final Iterator it = getVertices().iterator(); it.hasNext(); ){
        currentNode = (GenericsNode) it.next();
        if (currentNode.isInGraph() && currentNode.getPositionToEdit() != null
            && currentNode.hasUnresolvedVariants()){
          status.addEntry("Not enough information to introduce type arguments",
              RefactoringStatus.CANCEL);
          break;
        }
      }
    }
  }
  
  public void createEditors(final TransformationManager manager){
    GenericsNode currentNode;
    for (final Iterator it = getVertices().iterator(); it.hasNext(); ){
      currentNode = (GenericsNode) it.next();
      if (currentNode.isInGraph()){
        currentNode.createEditors(manager);
      }
    }
  }

  public void registerDependancy(Object leftItem, Object rightItem) {
    final GenericsNode leftNode = getNodeInstanceFor(leftItem);
    final GenericsNode rightNode = getNodeInstanceFor(rightItem);

    if (leftNode == null || rightNode == null){
      return;
    } else if (leftNode.isVariableNode() && rightNode.isVariableNode()){
      addEdge(leftNode, rightNode);
    } else if (leftNode.isVariableNode()){
      buildRightGatewayChain(leftItem, rightItem);
    } else if (rightNode.isVariableNode()){
      buildLeftGatewayChain(leftItem, rightItem);
    } else {
      // should never happen actually
    }
  }

  private void buildRightGatewayChain(final Object leftItem,
      final Object rightItem) {
    Object nextExpr = rightItem;
    GenericsNode newLeftNode = getNodeInstanceFor(leftItem);
    GenericsNode newRightNode = getNodeInstanceFor(rightItem);

    do {
      addEdge(newLeftNode, newRightNode);
      if (nextExpr instanceof BinMethodInvocationExpression) {
        nextExpr = ((BinMethodInvocationExpression) nextExpr).getExpression();
      } else {
        nextExpr = null;
      }
      newLeftNode = newRightNode;
      newRightNode = getNodeInstanceFor(nextExpr);
    } while (nextExpr instanceof MethodOrConstructorInvocationExpression
        && newRightNode != null && newLeftNode.dependsInOnOut());

    if (nextExpr instanceof BinVariableUseExpression){
      final BinLocalVariable variable = ((BinVariableUseExpression) nextExpr)
          .getVariable();
      final GenericsNode variableNode = getNodeInstanceFor(variable);
      if (variableNode != null){
        addEdge(newLeftNode, variableNode);
      }
    }
  }

  private void buildLeftGatewayChain(final Object leftItem,
      final Object rightItem) {
    Object parentItem = leftItem;
    Object lastMethodInvocationExpr = leftItem;
    GenericsNode newLeftNode = getNodeInstanceFor(leftItem);
    GenericsNode newRightNode = getNodeInstanceFor(rightItem);
    
    do {
      addEdge(newLeftNode, newRightNode);
      lastMethodInvocationExpr = parentItem;
      parentItem = ((BinItemVisitable) parentItem).getParent();
      newRightNode = newLeftNode;
      newLeftNode = getNodeInstanceFor(parentItem);
    } while (parentItem instanceof MethodOrConstructorInvocationExpression
        && newLeftNode != null && newRightNode.dependsInOnOut());
    
    GenericsNode variableNode = null;
    if (parentItem instanceof BinVariable && 
        ((BinVariable) parentItem).getExprNode().getParent().getParent()
        .getType() != JavaTokenTypes.COLON){
      variableNode = getNodeInstanceFor(parentItem);
          
    } else if (parentItem instanceof BinAssignmentExpression){
      BinVariable variable = ((BinVariableUseExpression)
          ((BinAssignmentExpression) parentItem).getLeftExpression())
          .getVariable();
      variableNode = getNodeInstanceFor(variable);
      
    } else if (parentItem instanceof BinExpressionList){
      final int index = ((BinExpressionList) parentItem).getExpressionIndex(
        (BinExpression) lastMethodInvocationExpr);
      final BinMethod method = ((MethodOrConstructorInvocationExpression)
          ((BinItemVisitable) parentItem).getParent()).getMethod();
      variableNode = getNodeInstanceFor(method.getParameters()[index]);
    }

    if (variableNode != null){
      addEdge(variableNode, newRightNode);
    }
  }

  public void truncateNodesBehindWildcards(final BinVariable variable) {
    final GenericsNode node = getNodeInstanceFor(variable);
    collectFromBelowUpwards(node);
    if (node.hasWildcardVariants()){
      removeAllEdges(node);
      removeUnconnectedGraphParts(getCenterNode());
    }
  }

  public List extractAllDependantsOf(final BinVariable variable) {
    final GenericsNode node = getNodeInstanceFor(variable);
    final List outgoingNodes = getOutgoingNodes(node);
    final List incomingNodes = getIncomingNodes(node);
    final List dependants = new ArrayList((int) Math.ceil(
        (outgoingNodes.size() + incomingNodes.size())*1.2));
    for (Iterator it = incomingNodes.iterator(); it.hasNext(); ){
      GenericsNode currentNode = (GenericsNode) it.next();
      BinVariable dependant = extractDependantFromIncoming(currentNode);
      if (dependant != null){
        dependants.add(dependant);
      }
    }
    for (Iterator it = outgoingNodes.iterator(); it.hasNext(); ){
      GenericsNode currentNode = (GenericsNode) it.next();
      BinVariable dependant = extractDependantFromOutgoing(currentNode);
      if (dependant != null){
        dependants.add(dependant);
      }
    }
    return dependants;
  }
  
  private BinVariable extractDependantFromIncoming(GenericsNode node){
    if (!node.isInGraph()){
      return null;
    } else if (node.isVariableNode() && node.getCompilationUnit() != null
        && !node.hasTypeArguments()){
      return node.getVariable();
    } else if (node.isVariableNode()){
      return null;
    } else {
      List incoming = getIncomingNodes(node);
      if (incoming.size() > 0){
        return extractDependantFromIncoming((GenericsNode) incoming.get(0));
      } else {
        return null;
      }
    }
  }
  
  private BinVariable extractDependantFromOutgoing(GenericsNode node){
    if (!node.isInGraph()){
      return null;
    } else if (node.isVariableNode()){
      return node.getVariable();
    } else {
      List outgoing = getOutgoingNodes(node);
      if (outgoing.size() > 0){
        return extractDependantFromOutgoing((GenericsNode) outgoing.get(0));
      } else {
        return null;
      }
    }
  }
}
