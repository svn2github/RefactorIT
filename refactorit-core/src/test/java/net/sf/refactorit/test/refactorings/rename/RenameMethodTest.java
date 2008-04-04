/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings.rename;



import net.sf.refactorit.classfile.ClassUtil;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.rename.RenameMethod;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.refactorings.NullContext;
import net.sf.refactorit.test.refactorings.RefactoringTestCase;

import org.apache.log4j.Category;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;


public class RenameMethodTest extends RefactoringTestCase {

  private static final Category cat
      = Category.getInstance(RenameMethodTest.class.getName());

  public static String filesToString(Project p) {
    StringBuffer result = new StringBuffer("\n");
    List sources = p.getCompilationUnits();
    for (int i = 0; i < sources.size(); ++i) {
      CompilationUnit source = (CompilationUnit) sources.get(i);
      result.append("\n---- " + source.getDisplayPath() + " -------\n");
      result.append(source.getContent());
    }

    return result.toString();
  }

  public static BinTypeRef[] convertSingature(Project p, String signature[]) {
    BinTypeRef[] result = new BinTypeRef[signature.length];

    for (int i = 0; i < result.length; ++i) {
      result[i] = convertSignature(p, signature[i]);
    }

    return result;
  }

  public static BinTypeRef convertSignature(Project p, String signature) {
    BinTypeRef result = null;
    int dimension = 0;

    if (signature.endsWith(";")) {
      String converted = null;
      if ("[QString;".equals(signature)) {
        converted = "java.lang.String";
        dimension = 1;
      } else if ("QString;".equals(signature)) {
        converted = "java.lang.String";
      } else if ("QObject;".equals(signature)) {
        converted = "java.lang.Object";
      } else {
        throw new RuntimeException("dont know how to convert " + signature);
      }

      result = p.getTypeRefForName(converted);

    } else {
      String converted = ClassUtil.getNameForDescriptor(signature);
      result = p.findPrimitiveTypeForName(converted);
    }

    if (result == null) {
      throw new RuntimeException("Did not find signature for " + signature);
    }

    if (dimension != 0) {
      result = p.createArrayTypeForType(result, dimension);
    }
    return result;
  }

  public RenameMethodTest() {
    super("RenameMethod");
  }

  public static Test suite() {
    TestSuite suite = new TestSuite(RenameMethodTest.class);
    suite.setName("Rename Method Tests");

    suite.addTest(RenameVirtualMethodInClassTest.suite());
    suite.addTest(RenameMethodInInterfaceTest.suite());
    suite.addTest(RenamePrivateMethodTest.suite());
    suite.addTest(RenameStaticMethodTest.suite());
    return suite;
  }

  public String getTemplate() {
    return "RenameMethod/<stripped_test_name>/<in_out>";
  }

  protected void setUp() throws Exception {
    FormatSettings.applyStyle(new FormatSettings.AqrisStyle());
  }

  private void doRename(String typeName,
      String oldMethodName, String newMethodName,
      boolean renameSupers, boolean renameSubs) throws Exception {
    doRename(typeName, oldMethodName, newMethodName, renameSupers, renameSubs,
        false);
  }

  private void doRename(String typeName,
      String oldMethodName, String newMethodName,
      boolean renameSupers, boolean renameSubs,
      boolean mustFail) throws Exception {

    cat.info("Testing " + getStrippedTestName());

    final Project project = getMutableProject();
    BinCIType type = project.getTypeRefForName(typeName).getBinCIType();
    BinMethod[] methods = type.getDeclaredMethods();
    assertTrue("there are methods", methods.length > 0);
    BinMethod method = null;
    for (int i = 0; i < methods.length; i++) {
      if (oldMethodName.equals(methods[i].getName())) {
        method = methods[i];
        break;
      }
    }
    assertNotNull("found method", method);

    RenameMethod renamer = new RenameMethod(new NullContext(project), method);

    renamer.setMustCheckOverrides(false);
    renamer.setSubtypes(renameSubs);
    renamer.setSupertypes(renameSupers);
    renamer.setNewName(newMethodName);

    RefactoringStatus status = renamer.checkPreconditions();
    status.merge(renamer.checkUserInput());

    status.merge(renamer.apply());

    if (mustFail) {
      assertFalse("Rename must have failed", status.isOk());
    } else {
      assertTrue("Performed method rename: " + status.getAllMessages(),
          status.isOk());

      RwRefactoringTestUtils.assertSameSources(
          "After method rename", getExpectedProject(), project);
    }

    cat.info("SUCCESS");
  }

  public void testRenameSubMethodUsages() throws Exception {
    doRename("Test", "method", "method1", true, true);
  }

  public void testRenameThisUsagesInSubClass() throws Exception {
    doRename("Test", "method", "method1", true, true);
  }

  public void testRenameThisUsagesInSubClass2() throws Exception {
    doRename("Test", "anotherMethod", "anotherMethod1", true, true);
  }

  public void testBug2056() throws Exception {
    doRename("a.Test", "somethingWithString", "something", true, true, true);
  }

}
