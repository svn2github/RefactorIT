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

/**
 * Creates a weighted graph where nodes are - specified objects and 
 * values are - the minimal distance between them
 * WeightedGraph allow to get distance from any node to any other node or to
 * get distance from one node to every other.
 * 
 *  For getting distances - Floyd algorithm is used (N^3 complexity)
 */
public class WeightedGraph {
  public class Entry {
    private final Object object;
    private final int length;

    protected Entry(Object object, int length) {
      this.object = object;
      this.length = length;
    }
    
    public Object getObject() {
      return object;
    }

    public int getLength() {
      return length;
    }
    
  }

  public static final int DEFAULT_INITIAL_SIZE = 25;
  public static final float DEFAULT_INITIAL_FACTOR = 1.25F;
  public static final byte INFINITY = Byte.MAX_VALUE; // Does this look like infinity?
  
  ArrayList nodes = new ArrayList();
  private byte[][] matrix = new byte[0][0];
  private float increaseFactor;
  private boolean changed = false;
  
  public WeightedGraph(int initialCapacity, float increaseFactor) {
    this.increaseFactor = increaseFactor;
    matrix = resizeMatrix(matrix, initialCapacity);
  }
  
  public WeightedGraph() {
    this(DEFAULT_INITIAL_SIZE, DEFAULT_INITIAL_FACTOR);
  }
  
  public void add(Object object1, Object object2, int distance) {
    add(object1, object2, (byte)(distance >= INFINITY ? (INFINITY - 1): distance));
  }
  
  public void add(Object object1, Object object2, byte distance) {
    int index1 = nodes.indexOf(object1);
    int index2 = nodes.indexOf(object2);
    
    if(index1 == -1) {
      index1 = addNode(object1);
    }
    
    if(index2 == -1) {
      index2 = addNode(object2);
    }
    
    ensureCapacity(Math.max(index1, index2) + 1);
    
    setDistance(index1, index2, distance);
  }
  
 

  /**
   * Finds the shortest path between two nodes 
   * @param node1
   * @param node2
   * @return the shortest path between two nodes. May return INFINITY constant defined in this class
   */
  public int distance(Object object1, Object object2) {
    ensureDistances();    
    
    int index1 = nodes.indexOf(object1);
    int index2 = nodes.indexOf(object2);
    
    if(index1 == -1 || index2 == -1) {
      return INFINITY;
    }
    
    byte distance = matrix[index1][index2];
    return distance;
  }
  
  /**
   *  Updates pathes between nodes. This function is based on Floyd's algorithm
   */
  private void ensureDistances() {
    if(!changed) {
      return; // we have already ensured the distance
    }
    int i, j, k;
    // for each route via k from i to j pick any better routes and
    // replace A[i][j] path with sum of paths i-k and j-k
    for (k = 0; k < matrix.length; k++) {
      for (i = 0; i < matrix.length; i++) {
        for (j = 0; j < matrix.length; j++) {
          if (matrix[i][k] + matrix[k][j] < matrix[i][j]) {
            int sum = matrix[i][k] + matrix[k][j];
            matrix[i][j] = sum >= INFINITY ? INFINITY - 1: (byte)sum;
          }
        }
      }
    }
    
    changed = false;
  }

  /**
   * Prepares the specified matrix, i.e. fills it with INFINITY values everywhere
   * where nodes are set to zero. Main diagonal is set to zero.
   * @param matrix
   */
  private void prepareMatrix(byte[][] matrix) {
    int i;
    int j;
    // set all unconnected positions to infinity
    for (i = 0; i < matrix.length; i++) {
      for (j = 0; j < matrix.length; j++) {
        if (matrix[i][j] == 0) {
          matrix[i][j] = INFINITY; 
        }
      }
    }

    // set the diagonals to zero
    for (i = 0; i < matrix.length; i++) {
      matrix[i][i] = 0;
    }
  }


  // class functions
  
  private void setDistance(int index1, int index2, byte distance) {
    if (matrix[index1][index2] == INFINITY
        || distance < matrix[index1][index2]) {
        matrix[index1][index2] = distance;
        matrix[index2][index1] = distance;
      }
  }
  
  protected int addNode(Object object) {
    nodes.add(object);
    setChanged();
    return nodes.indexOf(object);
  }
  
  private void setChanged() {
    this.changed = true;
  }

  protected void ensureCapacity(int neededCapacity) {
    int newCapacity = matrix.length;
    while (neededCapacity > newCapacity) {
      newCapacity = (int) Math.ceil(increaseFactor * newCapacity);
    }
    
    if (newCapacity != matrix.length) {
      matrix = resizeMatrix(matrix, newCapacity);
    }
  }

  protected byte[][] resizeMatrix(byte[][] matrix, final int newCapacity) {
    byte[][] newMatrix = new byte[newCapacity][newCapacity];
    prepareMatrix(newMatrix);
    
    for (int i = 0; i < matrix.length; i++){
      System.arraycopy(matrix[i], 0, newMatrix[i], 0, matrix.length);
    }
    
    
    return newMatrix;
  }

  public Entry[] getDependenciesFor(Object object) {
    ensureDistances();
    
    ArrayList list = new ArrayList();
    
    int index = nodes.indexOf(object);
    if(index != -1) {
      byte row[] = matrix[index];
      for(int i = 0; i < row.length && i < nodes.size(); i++) {
        if(i == index) {
          continue;
        }
        
        if(row[i] == INFINITY ) {
          list.add(new Entry(nodes.get(i), 0));
        } else {
          list.add(new Entry(nodes.get(i), row[i]));
        }
      }
    }
    
    return (Entry[])list.toArray(new Entry[list.size()]);
  }
}
