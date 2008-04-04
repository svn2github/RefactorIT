/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.common.util.graph;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class WeightedGraphTest extends TestCase {
  
  public WeightedGraphTest(String name) {
    super(name);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite(WeightedGraphTest.class, "Dependecies Graph Test");
    return suite;
  }
  
  public void testMatrixResize() {
    WeightedGraph graph = new WeightedGraph();
    graph.add("x", "y", 3);
    graph.add("x", "y", 5);
    graph.add("y", "z", 10);
  }
  
  public void testDistance1() {
    WeightedGraph graph = new WeightedGraph();
    graph.add("1", "2", 10);
    assertEquals("Distance between 1 and 2 is 10", 10, graph.distance("1", "2"));
  }
  
  public void testDistance2() {
    WeightedGraph graph = new WeightedGraph();
    graph.add("1", "2", 10);
    assertEquals("Distance between 2 and 1 is 10", 10, graph.distance("2", "1"));
  }  
  
  public void testDistance3() {
    WeightedGraph graph = new WeightedGraph();
    graph.add("1", "2", 10);
    graph.add("2", "3", 10);
    assertEquals("Distance between 1 and 3 is 20", 20, graph.distance("1", "3"));
  }
  
  public void testDistance4() {
    WeightedGraph graph = new WeightedGraph(4, 2F);
    graph.add("1", "2", 1);
    graph.add("2", "3", 2);
    graph.add("3", "4", 5);
    assertEquals("Distance between 1 and 4 is 8", 8, graph.distance("1", "4"));
  }
  
  public void testDistance5() {
    WeightedGraph graph = new WeightedGraph(4, 2F);
    graph.add("1", "2", 1);
    graph.add("3", "2", 2);
    graph.add("3", "4", 5);
    assertEquals("Distance between 1 and 4 is 8", 8, graph.distance("1", "4"));
  }

  public void testDistance6() {
    WeightedGraph graph = new WeightedGraph(10, 2F);
    graph.add("A", "B", 3);
    graph.add("B", "C", 1);
    graph.add("C", "D", 2);
    graph.add("D", "J", 1);
    graph.add("D", "I", 12);
    graph.add("D", "E", 6);
    graph.add("D", "F", 7);
    graph.add("D", "H", 1);
    graph.add("J", "I", 3);
    graph.add("J", "H", 3);
    graph.add("E", "G", 5);
    graph.add("F", "G", 10);
    assertEquals("Distance between J and F is 8", 8, graph.distance("J", "F"));
    assertEquals("Distance between J and F is 8", 2, graph.distance("J", "H"));
  }
  
  public void testDistance7() {
    WeightedGraph graph = new WeightedGraph(4, 2F);
    graph.add("1", "2", 1);
    graph.add("3", "2", 5);
    graph.add("1", "3", 2);
    graph.add("3", "4", 10);
    assertEquals("Distance between 1 and 4 is 12", 12, graph.distance("1", "4"));
  }
  
  public void testDependencyEntries() {
    WeightedGraph graph = new WeightedGraph(4, 2F);
    graph.add("1", "2", 1);
    graph.add("3", "2", 5);
    graph.add("1", "3", 2);
    graph.add("3", "4", 10);
    WeightedGraph.Entry[] entries = graph.getDependenciesFor("1");
    assertEquals("There is 3 dependent nodes", 3, entries.length);
    
    for(int i = 0; i < entries.length; i++) {
      Object obj = entries[i].getObject();
      if("2".equals(obj)) {
        assertEquals("Distance between 1 and 2 is 1", 1, entries[i].getLength());
      } else if("3".equals(obj)) {
        assertEquals("Distance between 1 and 3 is 2", 2, entries[i].getLength());
      } else if("4".equals(obj)) {
        assertEquals("Distance between 1 and 4 is 12", 12, entries[i].getLength());
      } else {
        assertFalse("Detected wrong object '" + obj + "'", true);
      }
    }
  }
  
}
