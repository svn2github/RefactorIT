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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * ConnectedComponentsFinder
 *  Finds strongly connected components in directed graph, creates acyclic graph from them and sorts topologicall after
 *  
 *  From Cormen, Rivest Introduction to Algorithms
 * @author <a href="mailto:tonis.vaga@aqris.com>Tonis Vaga</a>
 * @version $Revision: 1.1 $ $Date: 2005/12/09 12:02:17 $
 */
public class DigraphTopologicalSorter {
  private List cyclicComponents;
  private List nonCyclicComponents;
  private Digraph graph;
  private Digraph transpGraph;

  /**
   */
  public DigraphTopologicalSorter(Digraph graph) {
    this.graph=graph;
    final DfsTraverser traverser = new DfsTraverser(graph,null);
    
    transpGraph = graph.transpose();
    DfsTraverser transTraverser=new DfsTraverser(transpGraph,new Comparator() {

      public int compare(Object o1, Object o2) {
        // sort decreasing by finish time
        return traverser.getVertexInfo(o2).finishTime
                - traverser.getVertexInfo(o1).finishTime;
      }
      
    });
    
    cyclicComponents = transTraverser.getMultiNodeTraverseTrees();
    nonCyclicComponents=transTraverser.getSingleNodeTraverseTrees();
  }
  final class CyclicToAcyclicConverter {
    
    /**
     * 
     */
    public CyclicToAcyclicConverter() {
    }
    public Digraph convert() {
      // create new graph where cyclic components are replaced by single nodes
      
      List newVertices=new ArrayList(cyclicComponents.size()+nonCyclicComponents.size());
      
      newVertices.addAll(cyclicComponents);
      newVertices.addAll(nonCyclicComponents);
      
      Digraph newGraph=new Digraph(new HashSet(newVertices));
     
      for (int i = 0; i < newVertices.size(); ++i) {
        Object element = newVertices.get(i);
        List outNodes = getAllOutNodesForComponent(element);

        // use set to make sure only one edge is added for each component
        Set outComponentsToAdd=new HashSet();
        
        for (int j = 0; j < outNodes.size(); ++j) {
          Object outNode = outNodes.get(j);
          
          Object outNodeComponent=getOutNodeComponent(outNode);
          if ( outNodeComponent == element ) {
            // don't add arcs to self
            continue;
          }
          outComponentsToAdd.add(outNodeComponent);
        }
        newGraph.addEdges(element,outComponentsToAdd);
      }
      return newGraph;
    }
    
    /**
     * @param node
     * @return component into which this node belongs
     */
    private Object getOutNodeComponent(Object node) {

      // non-cyclic component, return
      if ( nonCyclicComponents.contains(node) ) {
        return node;
      } else {
        for (int i = 0; i < cyclicComponents.size(); ++i) {
          List comp =  (List) cyclicComponents.get(i);
          if ( comp.contains(node) ) {
            return comp;
          }
        }
        throw new IllegalStateException("couldn't find component");
      }
    }

    /**
     * 
     * @param obj component
     * @return set of outgoing nodes for component, if it is list returns all outnodes for that
     *   each element only once!!!
     */
    private List getAllOutNodesForComponent(Object obj) {
      if ( cyclicComponents.contains(obj) ) {
        List elList=(List) obj;
        
        Set result=new HashSet();
        for (int i = 0; i < elList.size(); ++i) {
          Object element = elList.get(i);
          result.addAll(graph.getOutgoingNodes(element));
        }
        
        return new ArrayList(result);
      } else {
        return graph.getOutgoingNodes(obj);
      }
    }
    
  }
  /**
   * 
   * @return List of graph vertices, sort topologicall, cyclic component will be
   *         grouped as List
   */
  public List sortTopologically() {
    Digraph graphToSort=graph;
    if ( cyclicComponents.size() > 0 ) {
      graphToSort=convertToAcyclic();
    }
    DfsTraverser dfs = new DfsTraverser(graphToSort,null);
    
    List result=new ArrayList();
    List traverseTrees = dfs.getTraverseTrees();
    
    for (int i = 0; i < traverseTrees.size(); ++i) {
      List tree = (List) traverseTrees.get(i);
      result.addAll(tree);
    }
    return result;
  }

  Digraph convertToAcyclic() {
    return new CyclicToAcyclicConverter().convert();
  }
  
  static final class DfsTraverser {
    private Map infoMap;

    private int time;

    Comparator vertexComparator;

    private Digraph graph;
    
    private List traverseTrees=new ArrayList();
//    private List multiNodeTrees=new ArrayList();
//
//    private List singleNodeTrees=new ArrayList();

    /**
     * @param graph
     * @param comp --
     *          vertex comparator on null when can process vertices in any order
     */
    public DfsTraverser(Digraph graph,Comparator comp) {
      this.graph=graph;
      this.vertexComparator=comp;
      infoMap = new HashMap();
      
      dfs();
    }

    /**
     * List of lists, each is traverse tree, in traverse order
     */
    public List getTraverseTrees() {
      return traverseTrees;
    }

    /**
     * List of multinode traverse trees
     */
    public List getMultiNodeTraverseTrees() {
      List result=new ArrayList();
      for (int i = 0; i < traverseTrees.size(); ++i) {
        List element = (List) traverseTrees.get(i);
        if ( element.size() > 1 ) {
          result.add(element);
        }
       
      }
      return result;
    }
    VertexInfo getVertexInfo(Object element) {
      return (VertexInfo) infoMap.get(element);
    }


    /**
     * depth first search algorithm
     */
    private void dfs() {
      time = 0;

      List nodes = graph.getVertices();
      
      
      // init vertices
      for (int i = 0; i < nodes.size(); ++i) {
        Object node =nodes.get(i);
        infoMap.put(node,new VertexInfo());
      }

      if (vertexComparator != null) {
        Collections.sort(nodes, vertexComparator);
      }
      

      for (int i = 0; i < nodes.size(); ++i) {
        Object element = nodes.get(i);
        
        if (getVertexInfo(element).color == VertexInfo.WHITE ) {
          
          ArrayList tree = new ArrayList();
          traverseTrees.add(tree);
          dfsVisit(element,tree);
        }
      }

    }



    /**
     * @param vx - vertex we are visiting
     * @param currentTree traverse tree we are building now
     */
    private void dfsVisit(Object vx, List currentTree) {
      List outNodes = graph.getOutgoingNodes(vx);
      
      final VertexInfo vertexInfo = getVertexInfo(vx);
      
      vertexInfo.color=VertexInfo.GRAY;
     
      if ( outNodes != null ) {
        if (vertexComparator != null) {
          Collections.sort(outNodes, vertexComparator);
        }
        for (int i = 0; i < outNodes.size(); ++i) {
          Object element = outNodes.get(i);
          VertexInfo info = getVertexInfo(element);
          if (info.color == VertexInfo.WHITE) {
//            if ( traverseTree == null ) {
//              // lazy init, avoid creating list for single leaves
//              traverseTree=new ArrayList();
//              multiNodeTrees.add(traverseTree);
//            }
            info.predecessor=vx;
            dfsVisit(element,currentTree);
          }
        }
      }
      vertexInfo.color=VertexInfo.BLACK;
      vertexInfo.finishTime = ++time;
      currentTree.add(vx);
      
//      if ( traverseTree != null ) {
//        traverseTree.add(vx);
//      } else {
//        // add single component
//        singleNodeTrees.add(vx);
//      }
    }

    /**
     * @return Returns list of the single vertices
     */
    public List getSingleNodeTraverseTrees() {
      List result=new ArrayList();
      for (int i = 0; i < traverseTrees.size(); ++i) {
        List element = (List) traverseTrees.get(i);
        if ( element.size() == 1 ) {
          result.addAll(element);
        }
       
      }
      return result;
    }

  }

  public List getStrongComponents() {
    ArrayList result = new ArrayList(nonCyclicComponents.size()+cyclicComponents.size());
    
    result.addAll(nonCyclicComponents);
    result.addAll(cyclicComponents);
    
    return result;
  }

  static final class VertexInfo {
    public static final int WHITE = 0;
    public static final int GRAY = 1;
    public static final int BLACK =2;

    /**
     * parent node in DFS traverse
     */
    Object predecessor;

    int color=WHITE;
    
    int finishTime = 0;
  }

}
