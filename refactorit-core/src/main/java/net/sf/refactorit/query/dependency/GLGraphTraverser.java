/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.dependency;

import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.ui.graph.BinEdge;
import net.sf.refactorit.ui.graph.BinNode;

import com.touchgraph.graphlayout.Node;
import com.touchgraph.graphlayout.TGPanel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GLGraphTraverser {

  private boolean tooDeep;
  private int loopCount = 0;
  TGPanel panel;
  public static final int MAX_LOOPS = 5000;

  public GLGraphTraverser(TGPanel panel) {
    this.panel = panel;
  }

  public List findLoops() {
    MultiValueMap graph = createGraph(panel);
    cutGraph(graph);
    List loops = new ArrayList();
    while(!graph.keySet().isEmpty()) {
      BinNode node = (BinNode)graph.keySet().iterator().next();
      List path = new ArrayList();
      path.add(node);
      dfsLoops(node, graph, path, loops);
      graph.clearKey(node);
      cutGraph(graph);
    }
    return loops;
  }

  /**
   * Depth first search method for graph
   * @param node - current node
   * @param graph - graph structure
   * @param path - path to current node
   * @param loops - List of loop paths
   */
  private void dfsLoops(BinNode node, MultiValueMap graph, List path, List loops) {
    //printLoop(path);
    List nextNodes = graph.get(node);
    BinNode root = (BinNode)path.get(0);
    node.setVisited(true);
    for(int i=0; i<nextNodes.size(); i++) {
      BinNode next = (BinNode)nextNodes.get(i);
      if(next.equals(root)){
        loops.add(new ArrayList(path));
        loopCount++;
      } else if(!next.isVisited() && !(loopCount>MAX_LOOPS || isTooDeep())) {
        path.add(next);
        dfsLoops(next, graph, path, loops);
        path.remove(path.size()-1);
      }
    }
    node.setVisited(false);
  }

  private boolean isTooDeep() {
    if (!tooDeep) {
      tooDeep = Runtime.getRuntime().freeMemory() < ((long) 1 << 10); // 1MB
      if (tooDeep) {
        System.runFinalization();
        System.gc();
        tooDeep = Runtime.getRuntime().freeMemory() < ((long) 1 << 10); // 1MB
      }
    }

    return tooDeep;
  }

  /*private void dfsMapLoops(BinNode node, MultiValueMap graph, Map path, List loops) {
    //printLoop(path);
    List nextNodes = graph.get(node);
    node.setVisited(true);
    for(int i=0; i<nextNodes.size(); i++) {
      BinNode next = (BinNode)nextNodes.get(i);

      if(path.keySet().contains(next)){
        loops.add(new HashMap(path));
      } else if(!next.isVisited()) {

        dfsMapLoops(next, graph, path, loops);
        path.remove(node);
      }
    }
    node.setVisited(false);
  }*/

  /**
   * Graph elements should already be contained in GraphPanel, prepare graph
   */
  private MultiValueMap createGraph(TGPanel panel) {
    Iterator it = panel.getAllEdges();
    MultiValueMap graph = new MultiValueMap();

    if(it!=null) {
      while(it.hasNext()) {
        BinEdge e = (BinEdge)it.next();
        graph.put(e.getFrom(), e.getTo());
        if(e.isBidirectional()) {
          graph.put(e.getTo(), e.getFrom());
        }
      }
    }
    return graph;
  }

  /**
   * cut redundant graph edges, that can not participate in loops
   * @param graph
   */
  private void cutGraph(MultiValueMap graph) {
    List remove = new ArrayList();
    do {
      remove.clear();
      for(Iterator iter = graph.keySet().iterator(); iter.hasNext(); ) {
        Node n = (Node)iter.next();
        if(!graph.contains(n)){
          remove.add(n);
        }
      }
      graph.keySet().removeAll(remove);
    } while(remove.size()>0);

    MultiValueMap removeAll = new MultiValueMap();
    do {
      removeAll.clear();
      for(Iterator iter = graph.keySet().iterator();iter.hasNext();) {
        Node n = (Node)iter.next();
        List l = (List)graph.get(n);
        for(int i=0; i<l.size(); i++) {
          Node tmp = (Node)l.get(i);
          if(!graph.keySet().contains(tmp)){
            removeAll.put(n,tmp);
          }
        }
      }
      graph.removeAll(removeAll);
    } while(!removeAll.keySet().isEmpty());
    remove.clear();
    removeAll.clear();
  }

  // for debug purposes only
  public static void printLoop(List loop) {
    System.err.print("[ ");
    for(int i=0; i<loop.size(); i++) {
      System.err.print(((BinNode)loop.get(i)).getID());
      if(i!=loop.size()-1){
        System.err.print(" -> ");
      }
    }
    System.err.println(" ]");
  }

  // for debug purposes only
  public static void printLoops(List loops) {
    System.err.println("Loops("+loops.size()+"):");
    for(int i = 0; i<loops.size(); i++) {
	  	printLoop((List)loops.get(i));
    }
    System.err.println("-----------------");
  }

  public static void printMapLoops(List loops) {
    System.err.println("Loops("+loops.size()+"):");
    for(int i = 0; i<loops.size(); i++) {
      printMapLoop((Map)loops.get(i));
    }
  }

  public static void printMapLoop(Map loop){
    Iterator it = loop.keySet().iterator();

    if(!it.hasNext()){
      return;
    }
    System.err.print("[ ");
    BinNode first = (BinNode)it.next();
    BinNode next = (BinNode)loop.get(first);
    while(true) {
      System.err.print(next.getID());
      next = (BinNode)loop.get(next);
      if(next.equals(first)) {
        break;
      }
      System.err.print(" -> ");
    }
    System.err.println("]");
  }

  public static void printBinItemMapLoops(List loops) {
    System.err.println("Loops("+loops.size()+"):");
    for(int i = 0; i<loops.size(); i++) {
      printBinItemMapLoop((Map)loops.get(i));
    }
  }

  public static void printBinItemMapLoop(Map loop){
    Iterator it = loop.keySet().iterator();

    if(!it.hasNext()){
      return;
    }
    System.err.print("[ ");
    BinItem first = (BinItem)it.next();
    BinItem next = (BinItem)loop.get(first);
    while(true) {
      System.err.print(next);
      next = (BinItem)loop.get(next);
      if(next.equals(first)) {
        break;
      }
      System.err.print(" -> ");
    }
    System.err.println("]");
  }
}
