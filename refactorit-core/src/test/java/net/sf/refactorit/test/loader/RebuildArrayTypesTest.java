/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.loader;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.vfs.Source;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * This test is here to test following situation: <br>
 * There is class A and class B. Class B uses an array of A (A[]). Some
 * refactoring makes changes to A and calls incremental rebuild. Incremental
 * rebuild logic marks A compilation unit to be rebuild and searches for all
 * usages of class A in project. Before I fixed the bug, it had found the
 * array type A[] and just deleted it from the classmodel, without rebuilding
 * it. That caused some RIT functions to crash. <p>
 * 
 * This test forces an incremental rebuild on a compilation unit from
 * project, and then checks if Project.getTypeRefForName(<array type>) equals
 * null.
 *
 * @author Arseni Grigorjev
 */
public class RebuildArrayTypesTest extends TestCase {
  Project project = null;
  
  /** Creates a new instance of RebuildArrayTypesTest */
  public RebuildArrayTypesTest() {
    
  }
  
  public static Test suite() {
    return new TestSuite(RebuildArrayTypesTest.class);
  }
  
  public void setUp(){
    try{
      project = Utils.createTestRbProject("ProjectLoader/RebuildArrayTypesTest");
      project.getProjectLoader().build();
    } catch (Exception e){
      fail("Could not load test project.");
    }
  }
  
  public void testIncrementalRebuild(){
    
    // get the needed compilation unit and mark it 'changed'
    Source rebuildSource = null;
    try {
      rebuildSource 
          = project.getCompilationUnitForName("WillBeChangedAndRebuilt.java")
          .getSource();
    } catch (NullPointerException e){
      fail("Could not get the requested compilation unit`s source");
    }
    
    project.getProjectLoader().forceSourceModified(rebuildSource);
    
    // force incremental rebuild on project
    try{
      project.getProjectLoader().build(null, false);
    } catch (Exception e){
      fail("Could not rebuild project");
    }
        
    // assert the arraytype not-null
    assertNotNull("Array type broken after incremental rebuild!", 
        project.getTypeRefForName(
            "[LRebuildArrayTypesTest.WillBeChangedAndRebuilt;"));
  }
}
