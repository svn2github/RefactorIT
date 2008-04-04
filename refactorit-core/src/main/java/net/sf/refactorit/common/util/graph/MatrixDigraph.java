/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.common.util.graph;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author  Arseni Grigorjev
 */
public class MatrixDigraph {
  
  public static final int DEFAULT_INITIAL_SIZE = 25;
  public static final float DEFAULT_INITIAL_FACTOR = 2F;

  private boolean[][] matrix;
  private float increaseFactor;

  private List verticies = new ArrayList(DEFAULT_INITIAL_SIZE);
  
  private int size = 0;

  public MatrixDigraph(int initialCapacity, float increaseFactor) {
     this.increaseFactor = increaseFactor;
     matrix = new boolean[initialCapacity][initialCapacity];
  }
  
  public MatrixDigraph() {
    this(DEFAULT_INITIAL_SIZE, DEFAULT_INITIAL_FACTOR);
  }
  
  public void set(final int x, final int y, final boolean value){
    ensureCapacity(Math.max(x, y) + 1);
    matrix[x][y] = value;
  }
  
  public boolean valueAt(final int x, final int y){
    return matrix[x][y];
  }

  public void ensureCapacity(int neededCapacity) {
    int newCapacity = matrix.length;
    while(neededCapacity > newCapacity){
      newCapacity = (int) Math.ceil(increaseFactor*newCapacity);
    }
    
    if (newCapacity != matrix.length){
      resizeMatrix(newCapacity);
    }
  }

  private void resizeMatrix(final int newCapacity) {
    boolean[][] newMatrix = new boolean[newCapacity][newCapacity];
    for (int i = 0; i < matrix.length; i++){
      System.arraycopy(matrix[i], 0, newMatrix[i], 0, matrix.length);
    }
    matrix = newMatrix;
  }

  public List getVertices(){ // FIXME: return only not removed vercities
    return verticies;
  }
  
  public void addEdge(Object a, Object b){
    final int indexOfA = getIndexOf(a);
    final int indexOfB = getIndexOf(b);
    if (indexOfA >= 0 && indexOfB >= 0){
      set(indexOfA, indexOfB, true);
    }
  }

  public List getOutgoingNodes(Object node) {
    final List outgoingNodes = new ArrayList(verticies.size());
    int nodeIndex = getIndexOf(node);
    for (int i = 0, max_i = verticies.size(); i < max_i; i++){
      if (valueAt(nodeIndex, i)){
        outgoingNodes.add(verticies.get(i));
      }
    }
    return outgoingNodes;
  }
  
  public List getIncomingNodes(Object node){
    final List incomingNodes = new ArrayList(verticies.size());
    int nodeIndex = getIndexOf(node);
    for (int i = 0, max_i = verticies.size(); i < max_i; i++){
      if (valueAt(i, nodeIndex)){
        incomingNodes.add(verticies.get(i));
      }
    }
    return incomingNodes;
  }
  
  public List getAllConnectedNodes(Object node){
    final List nodes = new ArrayList(verticies.size());
    int nodeIndex = getIndexOf(node);
    for (int i = 0, max_i = verticies.size(); i < max_i; i++){
      if (valueAt(i, nodeIndex) || valueAt(nodeIndex, i)){
        nodes.add(verticies.get(i));
      }
    }
    return nodes;
  }

  private int getIndexOf(final Object node) {
    return verticies.indexOf(node);
  }

  public void addVertex(Object newNode) {
    if (!verticies.contains(newNode)){
      size++;
      verticies.add(newNode);
      ensureCapacity(verticies.size());
    }
  }
  
  public void removeVertex(int index){
    size--;
    removeAllEdges(index);
  }

  public void removeAllEdges(final int index) {
    for (int i = 0, max_i = verticies.size(); i < max_i; i++){
      set(index, i, false);
      set(i, index, false);
    }
  }
  
  public void removeAllEdges(final Object node){
    removeAllEdges(getIndexOf(node));
  }
  
  public void removeVertex(Object node) {
    removeVertex(getIndexOf(node));
  }

  public void removeUnconnectedGraphParts(final Object node) {
    removeUnconnectedGraphParts(getIndexOf(node));
  }
  
  public void removeUnconnectedGraphParts(final int index){
    boolean[] connected = new boolean[verticies.size()];
    removeUnconnectedGraphParts(index, connected);
    for (int i = 0, max_i = verticies.size(); i < max_i; i++){
      if (!connected[i]){
        removeVertex(verticies.get(i));
      }
    }
  }
  
  public void removeUnconnectedGraphParts(final int index,
      final boolean[] connected){
    connected[index] = true;
    for (int i = 0, max_i = verticies.size(); i < max_i; i++){
      if (!connected[i] && (valueAt(index, i) || valueAt(i, index))){
        removeUnconnectedGraphParts(i, connected);
      }
    }
  }

  public int getSize() {
    return size;
  }
}
