/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings.javadoc;



import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.rename.RenameMethod;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.refactorings.NullContext;
import net.sf.refactorit.test.refactorings.RefactoringTestCase;

import org.apache.log4j.Category;

import junit.framework.Test;
import junit.framework.TestSuite;


public class RenameJavadocMethodTest extends RefactoringTestCase {
  private static final Category cat =
      Category.getInstance(RenameJavadocMethodTest.class.getName());

  public RenameJavadocMethodTest(String name) {
    super(name);
  }

  public String getTemplate() {
    return "RenameJavadoc/MethodTests/<stripped_test_name>/<in_out>";
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite(RenameJavadocMethodTest.class);
    suite.setName("Rename Javadoc Methods");
    return suite;
  }

  private void renameMustWork(String methodName, String className,
      BinParameter[] params, Project project) throws Exception {
    cat.info("Testing " + getStrippedTestName());

    if (project == null) {
      project = getMutableProject();
    }

    BinTypeRef aRef = project.findTypeRefForName(className);
    BinMethod method = aRef.getBinCIType().getDeclaredMethod(methodName, params);

    String newName = methodName + "_ren";
    RenameMethod renameMethod
        = new RenameMethod(new NullContext(project), method);
    renameMethod.setRenameInJavadocs(true);
    renameMethod.setNewName(newName);

    RefactoringStatus status = renameMethod.checkPreconditions();
    status.merge(renameMethod.checkUserInput());
    
    status.merge(renameMethod.apply());

    assertTrue("Renamed " + method.getQualifiedName() + " -> " + newName
        + " - " + status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", getExpectedProject(), project);

    cat.info("SUCCESS");
  }

  public void testWork1() throws Exception {
    renameMustWork("method2", "com.p1.Class1", new BinParameter[] {}
        , null);
  }

  public void testWork2() throws Exception {
    renameMustWork("inner", "com.p1.Class1$Inner", new BinParameter[] {}
        , null);
  }

  public void testWork3() throws Exception {
    renameMustWork("method1", "com.p2.Class2", new BinParameter[] {}
        , null);
  }

  public void testWork4() throws Exception {
    renameMustWork("method10", "com.p1.Class1", new BinParameter[] {
        new BinParameter("a", BinPrimitiveType.INT_REF, 0)}
        , null);
  }

  public void testWork5() throws Exception {
    renameMustWork("method12", "com.p1.Class1", new BinParameter[] {
        new BinParameter("a", BinPrimitiveType.INT_REF, 0),
        new BinParameter("b", BinPrimitiveType.CHAR_REF, 0)}
        , null);
  }

  public void testWork6() throws Exception {
    final Project project = getMutableProject();

    BinTypeRef typeRef1 = project.getTypeRefForName("com.p1.Class1");
    BinTypeRef typeRef2 = project.getTypeRefForName("java.lang.String");

    renameMustWork("method13", "com.p1.Class1", new BinParameter[] {
        new BinParameter("a", typeRef1, 0),
        new BinParameter("b", typeRef2, 0)}
        ,
        project);
  }
}
