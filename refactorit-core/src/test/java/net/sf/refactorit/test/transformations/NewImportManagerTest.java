/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.transformations;

import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.source.edit.EditorManager;
import net.sf.refactorit.transformations.NewImportManager;
import net.sf.refactorit.transformations.Transformation;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.transformations.view.triads.NameTriad;
import net.sf.refactorit.transformations.view.triads.OwnerTriad;
import net.sf.refactorit.transformations.view.triads.PackageTriad;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author  Arseni Grigorjev
 */
public class NewImportManagerTest extends TestCase {
  
  /** Creates a new instance of ImportManagerTest */
  public NewImportManagerTest(String name) {
    super(name);
  }
  
  public static Test suite() {
    TestSuite suite = new TestSuite(NewImportManagerTest.class);
    suite.setName("ImportManager Tests");
    return suite;
  }
  
  public void test00(){
    Project project = ProjectViewQueryTest.loadProject("fruits");
    assertNotNull(project);
   
    BinTypeRef typeFruitUtil = project.getTypeRefForName("ee.fruitstore.util.FruitUtil");
    BinTypeRef typeFruit = project.getTypeRefForName("ee.fruitstore.fruits.Fruit");
    BinMethod movedMethod = project.getTypeRefForName("ee.fruitstore.util.AppleUtil")
        .getBinCIType().getDeclaredMethod("printFruit", new BinTypeRef[] {
        typeFruit } );
        
    TransformationManager transofmationManager = new TransformationManager(null);
    NewImportManager importManager = new NewImportManager(transofmationManager);
    transofmationManager.registerTransformationActuator(importManager);

    transofmationManager.beginViewUpdateTransaction(null);
    transofmationManager.updateView(null, new OwnerTriad(movedMethod, typeFruitUtil));
    //List typeRefManagers = ImportUtils.extractAllTypeRefManagers(movedMethod);
    //importManager.discardUsages(typeRefManagers);
    //importManager.requestImports(typeFruitUtil, ImportUtils.extractTypesFromTypeRefManagers(typeRefManagers));
    //importManager.resolveImports();
     
    //importManager.debugVirtualView();
  }
  
  public void test01(){
    Project project = ProjectViewQueryTest.loadProject("fruits");
    assertNotNull(project);
    
    BinTypeRef typeFruit = project.getTypeRefForName("ee.fruitstore.fruits.Fruit");
    
    TransformationManager transofmationManager = new TransformationManager(null);
    transofmationManager.setProject(project);
    NewImportManager importManager = new NewImportManager(transofmationManager);
    transofmationManager.registerTransformationActuator(importManager);

    transofmationManager.beginViewUpdateTransaction(null);
    transofmationManager.updateView(null, new NameTriad(typeFruit.getBinCIType(), "Frukt"));
    long start = System.currentTimeMillis();
    transofmationManager.endViewUpdateTransaction(null);

    debugTestName();
    importManager.debugInvolvedSources();
  }
  
  public void test02(){
    Project project = ProjectViewQueryTest.loadProject("fruits");
    assertNotNull(project);
    
    BinPackage packFruits = project.getPackageForName("ee.fruitstore.fruits");
    
    TransformationManager transofmationManager = new TransformationManager(null);
    transofmationManager.setProject(project);
    NewImportManager importManager = new NewImportManager(transofmationManager);
    transofmationManager.registerTransformationActuator(importManager);

    transofmationManager.beginViewUpdateTransaction(null);
    transofmationManager.updateView(null, new NameTriad(packFruits, "frukty"));
    transofmationManager.endViewUpdateTransaction(null);
    
    debugTestName();
    importManager.debugInvolvedSources();
  }
  
  public void test03(){
    Project project = ProjectViewQueryTest.loadProject("fruits");
    assertNotNull(project);
    
    BinTypeRef typeFruit = project.getTypeRefForName("ee.fruitstore.fruits.Fruit");
    BinPackage packOranges = project.getPackageForName("ee.fruitstore.oranges");
    
    TransformationManager transofmationManager = new TransformationManager(null);
    transofmationManager.setProject(project);
    NewImportManager importManager = new NewImportManager(transofmationManager);
    transofmationManager.registerTransformationActuator(importManager);

    transofmationManager.beginViewUpdateTransaction(null);
    transofmationManager.updateView(null,
        new PackageTriad(typeFruit.getCompilationUnit(), packOranges));
    long start = System.currentTimeMillis();
    transofmationManager.endViewUpdateTransaction(null);

    debugTestName();
    importManager.debugInvolvedSources();
  }

  private void debugTestName() {
    System.out.println("TEST["+getName()+"]");
  }
}

class TestImportTransformation implements Transformation {
  
  public RefactoringStatus apply(EditorManager e) {
    return null;
  }
  
}
