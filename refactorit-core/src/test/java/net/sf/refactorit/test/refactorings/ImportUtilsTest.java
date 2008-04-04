/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.refactorings;

import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.refactorings.ImportUtils;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 * @author Arseni Grigorjev
 */
public class ImportUtilsTest extends RefactoringTestCase {

  /** Creates a new instance of ImportUtilsTest */
  public ImportUtilsTest(String name) {
    super(name);
  }

  public String getTemplate() {
    return "ImportUtilsTest/<test_name>/<in_out>";
  }
  
  public static Test suite() {
    return new TestSuite(ImportUtilsTest.class);
  }
  
  public String getName(){
    return StringUtil.capitalizeFirstLetter(super.getName());
  }

  /**
   * 
   * @param typeToImportName qualified name of type which we try to import
   * @param targetTypeName qualified name of type where we want to import
   * @param expected expected return of method ImportUtils.isAmbiguousImport()
   */
  private void performIsAmbiguousImportTest(
      String typeToImportName, String targetTypeName, boolean expected)
      throws Exception {
    Project project = getInitialProject();
    project.getProjectLoader().build();
    
    assertFalse(project.getProjectLoader().getErrorCollector().hasErrors());
    
    BinTypeRef typeToImportRef = project.getTypeRefForName(typeToImportName);
    assertNotNull("Can`t find type: " + typeToImportName, typeToImportRef);
    
    BinTypeRef targetTypeRef = project.getTypeRefForName(targetTypeName);
    assertNotNull("Can`t find type: " + targetTypeName, targetTypeRef);
    
    boolean got = ImportUtils.isAmbiguousImport(typeToImportRef.getBinCIType(),
        targetTypeRef.getBinCIType());
    
    assertEquals("ImportUtils.isAmbiguousImport() returned wrong value",
        expected, got);
  }
  
  /* TESTS */
  
  public void testSimpleAmbiguousImport() throws Exception {
    performIsAmbiguousImportTest("b.InputStream", "a.A", true);
  }
  
  public void testImportNotAmbiguous() throws Exception {
    performIsAmbiguousImportTest("b.InputStream", "a.A", false);
  }
  
  public void testAmbiguousSameNameInTargetPackage() throws Exception {
    performIsAmbiguousImportTest("java.io.InputStream", "a.A", true);
  }
  
  public void testAmbiguousSameNameInnerInTargetPackageNotImported()
      throws Exception {
    performIsAmbiguousImportTest("java.io.InputStream", "a.A", false);
  }
  
  public void testAmbiguousSameNameInnerInTargetPackageImportsDirectly()
      throws Exception {
    performIsAmbiguousImportTest("java.io.InputStream", "a.A", true);
  }
  
  public void testAmbiguousSameNameInnerInTargetPackageStaticImport()
      throws Exception {
    performIsAmbiguousImportTest("java.io.InputStream", "a.A", true);
  }
  
  public void testAmbiguousAlreadyStaticImported() throws Exception {
    performIsAmbiguousImportTest("a.B$InputStream", "a.A", false);
  }
  
  public void testAmbiguousAlreadyImportedDirectly() throws Exception {
    performIsAmbiguousImportTest("java.io.InputStream", "a.A", false);
  }

  public void testAmbiguousAlreadyImportedOnDemand() throws Exception {
    performIsAmbiguousImportTest("java.io.InputStream", "a.A", false);
  }
  
  public void testAmbiguousAlreadyImportedViaSingleStaticImport()
      throws Exception {
    performIsAmbiguousImportTest("b.B$InputStream", "a.A", false);
  }
  
  public void testAmbiguousAlreadyImportedViaStaticImportOnDemand()
      throws Exception {
    performIsAmbiguousImportTest("b.B$InputStream", "a.A", false);
  }
}
