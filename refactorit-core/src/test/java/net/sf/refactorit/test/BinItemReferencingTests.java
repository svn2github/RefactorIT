/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.references.BinItemReference;
import net.sf.refactorit.classmodel.references.CacheableReference;
import net.sf.refactorit.classmodel.references.Referable;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.query.SinglePointVisitor;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;


public abstract class BinItemReferencingTests extends TestCase {
  public BinItemReferencingTests(String testName) {
    super(testName);
  }

  boolean oldCacheEnabled = true;

  public void setUp() throws Exception {
    oldCacheEnabled = BinItemReference.cacheEnabled;
  }

  public void tearDown(){
    BinItemReference.cacheEnabled = oldCacheEnabled;
  }

  protected abstract Project getLoadedProject();

  protected abstract List getCompilationUnitsToTest();

  ////////////////////// TESTS //////////////////////

  public void testWholeProject() {
    // we put real project, we expect to recieve real project too
    Project loadedProject = getLoadedProject();
		BinItemReference reference = loadedProject.createReference();
    Project restoredProject = (Project) reference.restore(null);
    assertNotNull(restoredProject);
    assertEquals(loadedProject, restoredProject);

    // legacy - we give null, we expect active project
    reference = BinItemReference.create(null);
    assertEquals(Project.class, reference.restore(null).getClass());

    // legacy - we give Object, we expect active project
    reference = BinItemReference.create(new Object());
    assertEquals(Project.class, reference.restore(null).getClass());
  }

  public void testArray() {
    // Take 2 random (but different) items: one file, on type
    CompilationUnit compilationUnit = (CompilationUnit) getCompilationUnitsToTest().get(0);
    BinCIType type = ((BinTypeRef) getLoadedProject().
        getDefinedTypes().get(0)).getBinCIType();

    // Now test with an array that contains them...
    BinItemReference reference = BinItemReference.create(
        new Object[] {compilationUnit, type});
    Object[] result = (Object[])
        reference.restore(getLoadedProject());
    assertEquals(2, result.length);
    assertTrue(compilationUnit == result[0]);
    assertTrue(type == result[1]);
  }

  public void testList() {
    // Take 2 random (but different) items: one file, on type
    CompilationUnit compilationUnit = (CompilationUnit) getCompilationUnitsToTest().get(0);
    BinCIType type = ((BinTypeRef) getLoadedProject().
        getDefinedTypes().get(0)).getBinCIType();

    // Now test with a list that contains them...
    BinItemReference reference = BinItemReference.create(
        Arrays.asList(new Object[] {compilationUnit, type}));
    List result = (List) reference.restore(getLoadedProject());
    assertEquals(2, result.size());
    assertTrue(compilationUnit == result.get(0));
    assertTrue(type == result.get(1));
  }
/*
  public void testProjectGivenInCreateMethod() {
    CompilationUnit randomFile = (CompilationUnit) getCompilationUnitsToTest().get(0);

    BinItemReference okReference = BinItemReference.create(randomFile,
        getLoadedProject());
    assertTrue(randomFile == okReference.findBinObject());

    BinItemReference notOkReference = BinItemReference.create(randomFile);
    try {
      notOkReference.findBinObject();
      fail("Had to throw an exception");
    } catch (Exception expected) {}
  }*/

  public void testCompilationUnit() {
    List compilationUnitsToTest = getCompilationUnitsToTest();

    for (int i = 0; i < compilationUnitsToTest.size(); i++) {
      CompilationUnit compilationUnit = (CompilationUnit) compilationUnitsToTest.get(i);
      testReferencing(compilationUnit);
    }
  }

  public void testPackage() {
    for (int i = 0; i < getLoadedProject().getAllPackages().length; i++) {
      testReferencing(getLoadedProject().getAllPackages()[i]);
    }
  }

  public void testAllReferables(){
    visitWholeSource(new SinglePointVisitor(){
      public void onEnter(Object referableObj){
        if (referableObj instanceof Referable){
          testReferencing((Referable) referableObj);
          testReferencingWithCache((Referable) referableObj);
        }
      }

      public void onLeave(Object o){

      }
    });
  }

  /////////////////////////// UTILITY METHODS /////////////////////////////

  private void visitWholeSource(BinItemVisitor visitor) {
    for (Iterator i = getCompilationUnitsToTest().iterator(); i.hasNext(); ) {
      visitEntireCompilationUnit((CompilationUnit) i.next(), visitor);
    }
  }

  /**
   * Use this method instead of visitWholeSource(BinItemVisitor) in
   * very slow tests because this method allows to limit the amount
   * of source files to be visited.
   */
  private void visitWholeSource(BinItemVisitor visitor, int fileLimit) {
    int fileCount = 0;

    for (Iterator i = getCompilationUnitsToTest().iterator();
        i.hasNext() && fileCount < fileLimit; fileCount++) {
      visitEntireCompilationUnit((CompilationUnit) i.next(), visitor);
    }
  }

  private void visitEntireCompilationUnit(CompilationUnit compilationUnit,
      BinItemVisitor visitor) {
    Assert.assertNotNull("CompilationUnit is not null", compilationUnit);
    final List definedTypes = compilationUnit.getDefinedTypes();
    Assert.assertNotNull("sf.getDefinedTypes() is not null", definedTypes);
    for (Iterator i = definedTypes.iterator(); i.hasNext(); ) {
      visitCiTypeRef((BinTypeRef) i.next(), visitor);
    }
  }

  private void visitCiTypeRef(BinTypeRef typeRef, BinItemVisitor visitor) {
    typeRef.getBinCIType().accept(visitor);
  }

  void testReferencing(Referable binItem) {
    BinItemReference.cacheEnabled = false;
    assertTrue("binItem is not null", binItem != null);
    assertTrue("binItem is not an Object", binItem.getClass() != Object.class);

    BinItemReference reference = BinItemReference.create(binItem);
    if (!binItem.equals(reference.restore(getLoadedProject()))) {
      fail("Not found: instance of " + binItem.getClass().getName() + " - "
          + binItem);
    }
  }

  void testReferencingWithCache(Referable binItem) {
    BinItemReference.cacheEnabled = true;
    assertTrue("binItem is not null", binItem != null);
    assertTrue("binItem is not an Object", binItem.getClass() != Object.class);

    BinItemReference reference = BinItemReference.create(binItem);
    if (reference instanceof CacheableReference){
      if (!binItem.equals(reference.restore(getLoadedProject()))) {
        fail("Not found: instance of " +
            binItem.getClass().getName() + " - " + binItem);
      }
    }
  }
}
