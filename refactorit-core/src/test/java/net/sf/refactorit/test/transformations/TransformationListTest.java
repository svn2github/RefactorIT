/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.transformations;



import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.source.edit.Editor;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.transformations.TransformationList;

import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TransformationListTest extends TestCase {

  private Project project;

  public TransformationListTest(String name) {
    super(name);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite(TransformationListTest.class);
    suite.setName("Transformation List tests");
    return suite;
  }

  public void testClearTransformationList() {
    TransformationList transList = new TransformationList();

    CompilationUnit unit = (CompilationUnit) getTestProject()
        .getCompilationUnits().get(0);

    Editor editor = new StringEraser(unit, 0, 0);
    transList.add(editor);

    transList.clear();
    assertTrue("Transformation List shall be cleared, i.e. empty",
        transList.getTransformationsCount() == 0);
  }
  
  public void testAddOneEditor() {
    TransformationList transList = new TransformationList();

    CompilationUnit unit = (CompilationUnit) getTestProject()
        .getCompilationUnits().get(0);

    Editor editor = new StringEraser(unit, 0, 0);
    transList.add(editor);

    assertTrue("Only one editor must be added to Transformation List",
        transList.getTransformationsCount() == 1);

    for (Iterator i = transList.iterator(); i.hasNext();) {
      Object obj = i.next();
      assertTrue("Editor in Transformation List must be StringEraser type",
          obj instanceof StringEraser);
    }

  }

  public void testAddSimularEditors() {
    TransformationList transList = new TransformationList();
    CompilationUnit unit = (CompilationUnit) getTestProject()
        .getCompilationUnits().get(0);

    Editor editor = new StringEraser(unit, 0, 0);
    transList.add(editor);
    transList.add(editor);
    transList.add(editor);
    
    assertTrue("Only one editor must be added to Transformation List",
        transList.getTransformationsCount() == 1);
    
    Editor editor2 = new StringEraser(unit, 0, 1);
    transList.add(editor2);
    transList.add(editor);
    transList.add(editor2);
    assertTrue("Only two editors must be in Transformation List",
        transList.getTransformationsCount() == 2);    
  }
  

  

  public void testAddEditorSequence() {
    TransformationList transList = new TransformationList();
    CompilationUnit unit = (CompilationUnit) getTestProject()
        .getCompilationUnits().get(0);

    Editor editor = new StringEraser(unit, 0, 0);
    Editor editor2 = new StringEraser(unit, 0, 1);
    Editor editor3 = new StringEraser(unit, 0, 2);
    Editor editor4 = new StringEraser(unit, 0, 3);
    transList.add(editor);
    transList.add(editor3);
    transList.add(editor2);
    transList.add(editor4);
    
    Iterator i = transList.iterator();
    assertTrue("Editors sequence is the same as was added", 
        editor == i.next() && 
        editor3 == i.next() && 
        editor2 == i.next() && 
        editor4 == i.next());         
  }
  
  public void testMerge() {
    CompilationUnit unit = (CompilationUnit) getTestProject()
    .getCompilationUnits().get(0);
    
    TransformationList transList1 = new TransformationList();
    TransformationList transList2 = new TransformationList();
    Editor editor11 = new StringEraser(unit, 0, 1);
    Editor editor12 = new StringEraser(unit, 0, 2);
    transList1.add(editor11);
    transList1.add(editor12);
    
    Editor editor21 = new StringEraser(unit, 0, 1);
    Editor editor22 = new StringEraser(unit, 0, 2);
    Editor editor23 = new StringEraser(unit, 0, 3);
    transList2.add(editor21);
    transList2.add(editor22);    
    transList2.add(editor23);
    
    transList1.merge(transList2);
    
    assertTrue("Only 5 editors must be in Transformation List",
        transList1.getTransformationsCount() == 5);
    
    Iterator i = transList1.iterator();
    assertTrue("Editors sequence is the same as was added", 
        editor11 == i.next() && 
        editor12 == i.next() && 
        editor21 == i.next() && 
        editor22 == i.next() &&
        editor23 == i.next());             
  }
  
  public void testListStatus() {
    RefactoringStatus status1 = new RefactoringStatus();
    status1.addEntry("Warning 1", RefactoringStatus.WARNING);
    status1.addEntry("Warning 2", RefactoringStatus.WARNING);
    
    TransformationList transList1 = new TransformationList();
    transList1.getStatus().merge(status1);
    
    RefactoringStatus status2 = new RefactoringStatus();
    status2.addEntry("Error 1", RefactoringStatus.ERROR);
    status2.addEntry("Error 2", RefactoringStatus.ERROR);    
    TransformationList transList2 = new TransformationList();
    transList2.getStatus().merge(status2);
    
    transList1.merge(transList2);
    Iterator i = transList1.getStatus().getEntries().iterator();
    assertEquals("Statuses is in right order ", i.next().toString(), "Warning 1");
    assertEquals("Statuses is in right order ", i.next().toString(), "Warning 2");
    assertEquals("Statuses is in right order ", i.next().toString(), "Error 1");
    assertEquals("Statuses is in right order ", i.next().toString(), "Error 2");
  }
  // ------------------------------------------------------------
  private Project getTestProject() {
    project = null;
    try {
      String content = "public class myClass {}";
      project = Utils.createTestRbProjectFromString(content);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return project;
  }

}
