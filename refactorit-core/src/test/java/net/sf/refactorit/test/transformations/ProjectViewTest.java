/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.transformations;

import net.sf.refactorit.transformations.AbstractAnalyzer;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.transformations.view.triads.NameTriad;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author  Arseni Grigorjev
 */
public class ProjectViewTest extends TestCase {

  public ProjectViewTest(String name) {
    super(name);
  }
  
  public static Test suite() {
    TestSuite suite = new TestSuite(ProjectViewTest.class);
    suite.setName("ProjectView Tests");
    return suite;
  }

  public void testTransactionNotStarted(){
    TransformationManager manager = new TransformationManager(null);
    try {
      manager.updateView(null, new NameTriad(new Object(), "newName"));
      fail("Illegal state exception expected.");
    } catch (IllegalStateException e){}
  }
  
  public void testSeveralTransactions(){
    TransformationManager manager = new TransformationManager(null);
    int[] transactionIds = new int[] {
      manager.beginViewUpdateTransaction(null),
      manager.beginViewUpdateTransaction(null),
      manager.beginViewUpdateTransaction(null),
      manager.beginViewUpdateTransaction(null),
      manager.beginViewUpdateTransaction(null)
    };
    for (int i = 0; i < transactionIds.length; i++){
      assertEquals("Transaction IDs do not match!", transactionIds[i], i);
    }
  }
  
  public void testNoCurrentTransaction(){
    TransformationManager manager = new TransformationManager(null);
    assertEquals(manager.getCurrentTransactionId(), -1);
  }
  
  public void testIndexingWhenKillingTransaction(){
    TransformationManager manager = new TransformationManager(null);
    int[] transactionIds = new int[] {
      manager.beginViewUpdateTransaction(null),
      manager.beginViewUpdateTransaction(null),
      manager.beginViewUpdateTransaction(null),
      manager.beginViewUpdateTransaction(null),
      manager.beginViewUpdateTransaction(null)
    };
    for (int i = 0; i < transactionIds.length; i++){
      if (i%2 == 0){
        manager.killViewUpdateTransaction(transactionIds[i]);
      }
    }
    for (int i = 0; i < transactionIds.length; i++){
      assertTrue("Killed wrong transaction or indexing problems!",
          i%2 != 0 || manager.isKilledViewUpdateTransaction(transactionIds[i]));
    }
  }
    
  public void testDisablingTransaction(){
    TransformationManager manager = new TransformationManager(null);
    int id = manager.beginViewUpdateTransaction(null);
    manager.disableViewUpdateTransaction(id);
    assertTrue("Didn`t disable the transaction",
        !manager.isActiveViewUpdateTransaction(id));
  }
  
  public void testNotifiedAboutViewUpdate(){
    TransformationManager manager = new TransformationManager(null);
    TestActuator actuator1 = new TestActuator(manager);
    TestActuator actuator2 = new TestActuator(manager);
    manager.registerTransformationActuator(actuator1);
    manager.registerTransformationActuator(actuator2);
    
    manager.beginViewUpdateTransaction(actuator1);
    manager.updateView(actuator1, new NameTriad(new Object(), "newName"));
    manager.endViewUpdateTransaction(actuator1);
    
    assertFalse("Actuator 1 should not be notified (he is caller!)",
        actuator1.isNotifiedAboutViewUpdate());
    assertTrue("Actuator 2 should be notified", actuator2
        .isNotifiedAboutViewUpdate());
  }
  
  public void testTriadsHandling(){
    TransformationManager manager = new TransformationManager(null);
    TestActuator actuator = new TestActuator(manager);
    manager.registerTransformationActuator(actuator);
    
    List triads1 = generateTriadsList(4);
    List triads2 = generateTriadsList(2);
    List triads3 = generateTriadsList(1);
    
    int id1 = manager.beginViewUpdateTransaction(actuator);
    manager.updateView(actuator, triads1);
    manager.endViewUpdateTransaction(actuator);
    int id2 = manager.beginViewUpdateTransaction(actuator);
    manager.updateView(actuator, triads2);
    manager.endViewUpdateTransaction(actuator);
    int id3 = manager.beginViewUpdateTransaction(actuator);
    manager.updateView(actuator, triads3);
    manager.endViewUpdateTransaction(actuator);
    
    List compareTo = new ArrayList(4+2+1);
    compareTo.addAll(triads1);
    compareTo.addAll(triads2);
    compareTo.addAll(triads3);
    checkEqualValueSets(manager.getProjectView().getAllTriads(), compareTo);
    
    manager.disableViewUpdateTransaction(id2);
    compareTo.clear();
    compareTo.addAll(triads1);
    compareTo.addAll(triads3);
    checkEqualValueSets(manager.getProjectView().getAllTriads(), compareTo);
    
    manager.enableViewUpdateTransaction(id2);
    compareTo.addAll(triads2);
    checkEqualValueSets(manager.getProjectView().getAllTriads(), compareTo);
  }
  
  private List generateTriadsList(int n){
    List result = new ArrayList(n);
    for (int i = 0; i < n; i++){
      result.add(new NameTriad(new Object(), "newName" + i));
    }
    return result;
  }

  public void checkEqualValueSets(List list, List compareTo) {
    boolean equals = true;
    if (list.size() != compareTo.size()){
      equals = false;
    } else {
      int i = 0;
      for (Iterator it = list.iterator(); it.hasNext(); ){
        if (compareTo.contains(it.next())){
          i++;
        }
      }
      if (i != compareTo.size()){
        equals = false;
      }
    }
    
    assertTrue("Value sets do not equal.", equals);
  }
}

class TestActuator extends AbstractAnalyzer {
  
  private boolean notifiedAboutViewUpdate = false;
  private boolean notifiedAboutConflicts = false;
  
  public TestActuator(TransformationManager manager){
    super(manager);
  }
  
  public TransformationList performChange() {
    return null;
  }
  
  public void notifyViewUpdated() {
    notifiedAboutViewUpdate = true;
  }
  
  public void notifyConflicts() {
    notifiedAboutConflicts = true;
  }

  public boolean isNotifiedAboutConflicts() {
    return this.notifiedAboutConflicts;
  }

  public boolean isNotifiedAboutViewUpdate() {
    return this.notifiedAboutViewUpdate;
  }
}
