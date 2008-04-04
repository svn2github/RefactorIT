/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.common.util.graph;


import net.sf.refactorit.common.util.CollectionUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

/**
 * ConnectedComponentsFinderTest
 * 
 * @author <a href="mailto:tonis.vaga@aqris.com>Tonis Vaga</a>
 * @version $Revision: 1.1 $ $Date: 2005/12/12 09:54:16 $
 */
public class DigraphTopologicalSorterTest extends TestCase {

  private String a="a";
  private String b="b";
  private String c="c";
  private String d="d";


  private String e="e";
  
  private String f="f";
  private String g="g";
  
  private String h="h";
  private Digraph cormensGraph;
  
  protected void setUp() throws Exception {
    buildCormensGraph();
  }
  
  /**
   * @param result
   * @param v
   */
  private List getComponentForVertex(List result, Object v) {
    for (int i = 0; i < result.size(); ++i) {
      if ( result.get(i) instanceof List ) {
        List list = (List) result.get(i);
        
        if ( list.contains(v) ) {
          return list;
        }
      }
      
    }
    return null;
  }

  
  public void testSingleNodeCycle() {
    
    String vertex="";
    Digraph graph = new Digraph(new HashSet(Collections.singletonList(vertex)));

    graph.addEdge(vertex,vertex); 

    List result = new DigraphTopologicalSorter(graph).getStrongComponents();
    
    assertEquals(1, result.size());
    assertEquals(vertex,result.get(0));
  }
  
  public void testTwoNodesCycle() {
    String v1="v1";
    String v2="v2";
    
    Set vertices=new HashSet();
    vertices.add(v1);
    vertices.add(v2);

    Digraph graph = new Digraph(new HashSet(
        vertices));

    graph.addEdge(v1,v2);
    graph.addEdge(v2,v1);
    
    List result = new DigraphTopologicalSorter(graph).getStrongComponents();
    
    assertEquals(1, result.size());
    assertEquals(2,((List)result.get(0)).size());
    
  }
 
  /**
   * From Cormen's book, figure 23.9, half of it 
   *
   */
  public void testThreeComponents() {
   
    Set vertices=new HashSet(Arrays.asList(new String[] { a,b,e,f,g,h}));
    
    Digraph graph=new Digraph(vertices);
    graph.addEdge(a,b);
    graph.addEdge(b,e);
    graph.addEdge(b,f);
    graph.addEdge(e,a);
    graph.addEdge(e,f);
    
    graph.addEdge(f,g);
    graph.addEdge(g,f);
    graph.addEdge(g,h);

    
   

    List result = new DigraphTopologicalSorter(graph).getStrongComponents();
    
    assertEquals(3, result.size());
    
    // single component
    assertTrue( result.toString(),result.contains(h));
    List c1=getComponentForVertex(result,a);
    
    assertEquals(3, c1.size() ); 
    assertTrue ( c1.contains(b));
    assertTrue ( c1.contains(e));
    
    List c2=getComponentForVertex(result,f);
    
    assertEquals(2,c2.size());
    assertTrue( c2.contains(g));


  }
  
  /**
   * From Cormen's book, figure 23.9 
   *
   */
  public void testCormensExample() {
    
    List result = new DigraphTopologicalSorter(cormensGraph).getStrongComponents();
    
    assertEquals(4, result.size());
    
    // single component
    assertTrue( result.toString(),result.contains(h));
    List c1=getComponentForVertex(result,a);
    
    assertEquals(3, c1.size() ); 
    assertTrue ( c1.contains(b));
    assertTrue ( c1.contains(e));
    
    List c2=getComponentForVertex(result,f);
    
    assertEquals(2,c2.size());
    assertTrue( c2.contains(g));
    
    List c3=getComponentForVertex(result,d);
    
    assertEquals( 2,c3.size());
    assertTrue ( c3.contains(c));

  }
  private void buildCormensGraph() {
    cormensGraph = new Digraph(
            new HashSet(Arrays.asList(new String[] { a,b,c,d,e,f,g,h})));
    cormensGraph.addEdge(a,b);
    
    
    cormensGraph.addEdge(b,e);
    cormensGraph.addEdge(b,f);
    cormensGraph.addEdge(e,a);
    cormensGraph.addEdge(e,f);
    
    cormensGraph.addEdge(f,g);
    cormensGraph.addEdge(g,f);
    cormensGraph.addEdge(g,h);

    
    // new edges
    cormensGraph.addEdge(b,c);
    
    cormensGraph.addEdge(c,g);
    cormensGraph.addEdge(c,d);
    cormensGraph.addEdge(d,c);
    
    cormensGraph.addEdge(d,h);
    
    assertTrue(cormensGraph.isValid());
  }
  
  public void testAcyclicConverterTwoNodes() {
    
    Digraph graph=new Digraph();
    graph.addVertex(a);
    graph.addVertex(b);
    graph.addEdge(a,b);
    graph.addEdge(b,a);
    
    DigraphTopologicalSorter finder = new DigraphTopologicalSorter(graph);
    
    Digraph dag=finder.convertToAcyclic();
    
    assertTrue(dag.toString(),dag.isValid());
    List vertices = dag.getVertices();
    assertEquals(1,vertices.size());
    List component = (List) vertices.get(0);
    
    assertTrue(component.contains(a));
    assertTrue(component.contains(b));
     
    assertTrue(dag.getEdgesMap().isEmpty());
    
    
  }
  
  public void testAcyclicConverterThreeNodes() {
    
    Digraph graph=new Digraph();
    graph.addVertex(a);
    graph.addVertex(b);
    graph.addVertex(c);
    
    graph.addEdge(a,b);
    graph.addEdge(b,a);
    graph.addEdge(b,c);
    
    DigraphTopologicalSorter finder = new DigraphTopologicalSorter(graph);
    
    Digraph dag=finder.convertToAcyclic();
    
    assertTrue(dag.isValid());
    List vertices = dag.getVertices();
    
    assertEquals(2,vertices.size());
    List component = getComponentForVertex(vertices,a);
    assertEquals(2,component.size());
    assertTrue( component.contains(a));
    
    assertTrue ( vertices.contains(c));
    
    assertEquals(1,dag.getEdgesMap().size());
    assertTrue(dag.getOutgoingNodes(component).contains(c));
  }
  public void testAcyclicConverterCormensGraph() {
    Digraph dag = new DigraphTopologicalSorter(cormensGraph).convertToAcyclic();
    
    List vertices = dag.getVertices();
    assertEquals(4, vertices.size());
    
    
    List component4=getComponentForVertex(vertices,a);
    List component3=getComponentForVertex(vertices,c);
    
    List component2=getComponentForVertex(vertices,g);

    
    assertTrue( dag.getOutgoingNodes(h).isEmpty());
    
    
    assertTrue(dag.getOutgoingNodes(component4).containsAll(
        CollectionUtil.toList(component3, component2)));
    
    // todo: finish the test, check all nodes :)
    
  }


  public void testSortTwoNodes() {
    
    Set vertices=new HashSet();
    vertices.add(a);
    vertices.add(b);
    Digraph graph=new Digraph(vertices);
    graph.addEdge(a,b);
    
    DigraphTopologicalSorter finder=new DigraphTopologicalSorter(graph);
    List result = finder.sortTopologically();
    
    assertEquals(2, result.size());
    assertEquals(b,result.get(0));
    assertEquals(a,result.get(1));
  }


  
  public void testSortCormensGraph() {
    List result = new DigraphTopologicalSorter(cormensGraph).sortTopologically();
    
    assertEquals(4, result.size());
    
    
    Object component1=h;

    List component4=getComponentForVertex(result,a);
    List component3=getComponentForVertex(result,c);
    
    List component2=getComponentForVertex(result,g);

    
    assertEquals(component1,result.get(0));
    
    assertEquals(component2,result.get(1));
    
    assertEquals(component3,result.get(2));
    
    assertEquals(component4,result.get(3));
    
  }
  

}
