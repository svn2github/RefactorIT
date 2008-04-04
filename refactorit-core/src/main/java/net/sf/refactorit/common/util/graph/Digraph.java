/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.common.util.graph;


import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.MultiValueMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * DiGraph - directed graph
 * 
 * @author <a href="mailto:tonis.vaga@aqris.com>Tonis Vaga</a>
 * @version $Revision: 1.1 $ $Date: 2005/12/09 12:02:16 $
 */
public class Digraph  {
  private Set vertices;
   private MultiValueMap map;
   
   private static final List emptyList=new ArrayList(0); 
   
   public Digraph() {
     vertices=new HashSet();
     map=new MultiValueMap();
   }
   
  /**
   * 
   * precondition: vertices contains all vertices attainable using {@link #getOutgoingNodes(Object)} on it
   * precond: each vertice is unique considering equals operator
   * 
   */
  public Digraph(Set vertices) {
    this.vertices=vertices;
    map=new MultiValueMap(vertices.size());
  }
  /**
   * @param vertices
   * @param map
   */
  private Digraph(Set vertices, MultiValueMap map) {
    this.vertices=vertices;
    this.map=map;
  }

  
  public List getVertices() {
    return new ArrayList(vertices);
  }

  public List getOutgoingNodes(Object vertex) {
    List result = map.get(vertex);
    
    if ( result == null ) {
      result=emptyList;
    }
    return result;
  }
  /**
   * Transposes graph, vertices are same but edges are in opposite direction
   */
  public Digraph transpose() {
    final MultiValueMap reverseDeps=new MultiValueMap();
    
    for (Iterator iter = vertices.iterator(); iter.hasNext();) {
      
      Object element =  iter.next();
      
      List outNodes=getOutgoingNodes(element);
      for (int j = 0; j < outNodes.size(); ++j) {
        Object node = outNodes.get(j);
        reverseDeps.putAll(node,element);
      } 
    }
    Digraph result = new Digraph(new HashSet(vertices),reverseDeps);
    
    
    
    return result;
  }
  
  /**
   * @param vertex
   */
  public void addVertex(Object vertex) {
    vertices.add(vertex);
  }
  /**
   */
  MultiValueMap getEdgesMap() {
    return map;
  }
  
  /**
   * @param a
   * @param b
   */
  public void addEdge(Object a, Object b) {
    if ( Assert.enabled ) {
      Assert.must( vertices.contains(a) && vertices.contains(b));
    }
    map.put(a,b);
  }
 
  /**
   * @param vertex
   * @param outNodes
   */
  public void addEdges(Object vertex, Set outNodes) {
    for (Iterator iter = outNodes.iterator(); iter.hasNext();) {
      addEdge(vertex,iter.next());
    }
    // doesn't work, double nodes
//    if ( Assert.enabled ) {
//      Assert.must(vertices.containsAll(outNodes));
//    }
//    map.putAll(vertex,outNodes);
  }
  /**
   * @return true if is valid graph, false if some edges contain vertices which
   *         are not in graph
   */
  public boolean isValid() {
    
    for (Iterator iter = map.keySet().iterator(); iter.hasNext();) {
      Object key = iter.next();
      if ( !vertices.contains(key)) {
        return false;
      }
      if ( !vertices.containsAll(map.get(key)) ) {
        return false;
      }
      
    }
    return true;
  }
  public String toString() {
    StringBuffer result = new StringBuffer("vertices :"+vertices);
    result.append(", edges: ");
    
    
    for (Iterator iter = map.keySet().iterator(); iter.hasNext();) {
      Object element = iter.next();
 
      List list = map.get(element);
      
      for (int i = 0; i < list.size(); ++i) {
        Object outNode = list.get(i);
        result.append("["+element+","+outNode+"], ");
      }
      
    }
    return result.toString();
  }

}
