/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.loader;



import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.query.usage.ManagingIndexer;
import net.sf.refactorit.query.usage.MethodIndexer;
import net.sf.refactorit.query.usage.filters.BinMethodSearchFilter;
import net.sf.refactorit.source.SourceParsingException;
import net.sf.refactorit.source.UserFriendlyError;
import net.sf.refactorit.test.Utils;

import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class ParsingErrorsTest extends TestCase {
  public ParsingErrorsTest(java.lang.String testName) {
    super(testName);
  }

  public static Test suite() {
    return new TestSuite(ParsingErrorsTest.class);
  }

  //---- Test methods -----------------------------------------------

  public void testWhereUsedWhenANonUserClassHasAMissingSuperclass() throws
      Exception {
    final Project testProject = createAndLoadProject("ProjectLoader/MissingSuperclass");

    assertEquals("HasAMissingSuperclass.java",
        getFileNameOfFirstUserFriendlyError(testProject));
    assertUsageCount(1, "usedMethod", "B", testProject);
  }

  public void testWhereUsedWhenANonUserClassHasBadMethodReturnTypes() throws
      Exception {
    final Project testProject = createAndLoadProject("ProjectLoader/BadMethodReturnTypes");

    assertEquals("HasBadMethodReturnTypes.java",
        getFileNameOfFirstUserFriendlyError(testProject));
    assertUsageCount(1, "usedMethod", "B", testProject);
  }

  public void testWhereUsedWhenATheUsedClassHasABadMethodSignature() throws
      Exception {
    final Project testProject = createAndLoadProject("ProjectLoader/ErrorsInUsedClass");

    assertEquals("UsedClass.java",
        getFileNameOfFirstUserFriendlyError(testProject));

    assertUsageCount(1, "usedMethod", "UsedClass", testProject);
  }

  public void testWhereUsedWithTokenErrors() throws Exception {
    final Project testProject = createAndLoadProject("ProjectLoader/TokenErrors");

    assertEquals("TokenErrors.java",
        getFileNameOfFirstUserFriendlyError(testProject));
    assertUsageCount(1, "usedMethod", "UsedClass", testProject);
  }

  public void testConstructorNamedAfterWrongClass() throws Exception {
    final Project testProject = createAndLoadProject(
        "ProjectLoader/ConstructorNamedAfterWrongClass");

    assertEquals("ConstructorNamedAfterWrongClass.java",
        getFileNameOfFirstUserFriendlyError(testProject));
    assertUsageCount(1, "usedMethod", "UsedClass", testProject);
  }

  public void testAbstractMethodMustNotHaveBody() throws Exception {
    final Project testProject = createAndLoadProject("ProjectLoader/VariousParsingErrors");

    // This also ensures that method bodies are built -- this brings out the UserFriendlyError we need
    assertUsageCount(1, "usedMethod", "UsedClass", testProject);

    assertTrue("must have errors", hasErrors("AbstractMethodWithBody.java",
        testProject));
  }

  public void testJavaReckognizerErrors() throws Exception {
    final Project testProject = createAndLoadProject("ProjectLoader/VariousParsingErrors");

    // This also ensures that method bodies are built -- this brings out the UserFriendlyError we need
    assertUsageCount(1, "usedMethod", "UsedClass", testProject);

    assertTrue("must have errors", hasErrors("JavaReckognizerError.java",
        testProject));
  }

  //-- Util methods ---------------------------------------------------------

  private static Project createAndLoadProject(String projectFolderName) throws
      Exception {
    final Project result = Utils.createTestRbProject(projectFolderName);

    try {
      result.getProjectLoader().build();
    } catch (SourceParsingException e) {
      assertTrue("SPE should just inform user",
          e.justInformsThatUserFriendlyErrorsExist());
    }

    return result;
  }

  private static String getFileNameOfFirstUserFriendlyError(Project project) {
    if (!(project.getProjectLoader().getErrorCollector()).hasUserFriendlyErrors()) {
      return "- No errors -";
    }

    Iterator i = (project.getProjectLoader().getErrorCollector()).getUserFriendlyErrors();
    UserFriendlyError userFriendlyError = (UserFriendlyError) i.next();
    return userFriendlyError.getCompilationUnit().getSource().getRelativePath();
  }

  private static boolean hasErrors(String fileName, Project project) {
    for (Iterator i = (project.getProjectLoader().getErrorCollector()).getUserFriendlyErrors(); i.hasNext(); ) {
      UserFriendlyError userFriendlyError = (UserFriendlyError) i.next();
      if (userFriendlyError.getCompilationUnit().getSource().getRelativePath()
          .equals(fileName)) {
        return true;
      }
    }

    return false;
  }

  private static void assertUsageCount(int expectedUsageCount,
      String methodName, String className, Project project) {
    BinTypeRef testTypeRef = project.getTypeRefForName(
        className);

    BinMethod usedMethod = testTypeRef.getBinCIType()
        .getDeclaredMethod(methodName, BinTypeRef.NO_TYPEREFS);

    ManagingIndexer supervisor = new ManagingIndexer();
    new MethodIndexer(supervisor, usedMethod,
        new BinMethodSearchFilter(
        true, true, true, true, true, false, true, false, false));
    supervisor.visit(project);

    assertEquals(expectedUsageCount, supervisor.getInvocations().size());
  }
}
