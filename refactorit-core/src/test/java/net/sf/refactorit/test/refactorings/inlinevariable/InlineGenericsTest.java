/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings.inlinevariable;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.parser.FastJavaLexer;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.refactorings.RefactoringTestCase;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Test InlineVariable refactoring correctly add casts to generic types to avoid
 *  creating uncompilable code
 *
 * @author Arseni Grigorjev
 */
public class InlineGenericsTest extends RefactoringTestCase {
  private static String fakeTestName = null;

  private int oldJvmMode;

  public InlineGenericsTest(String name) {
    super(name);
  }

  public String getTemplate() {
    return "InlineTemp/InlineGenerics/A_<test_name>_<in_out>.java";
  }

  public static Test suite() {
    return new TestSuite(InlineGenericsTest.class);
  }

  public void setUp(){
    oldJvmMode = Project.getDefaultOptions().getJvmMode();
    Project.getDefaultOptions().setJvmMode(FastJavaLexer.JVM_50);
  }

  public void tearDown(){
    Project.getDefaultOptions().setJvmMode(oldJvmMode);
  }

  public void assertWorks(String var) throws Exception {
    Project inProject = getMutableProject(getInitialProject());
    RwRefactoringTestUtils.assertRefactoring(
        AllTests.createRefactoring(var, inProject), getExpectedProject(),
        inProject);
  }

  public String getName() {
    if (fakeTestName != null) {
      return fakeTestName;
    } else {
      return super.getName();
    }
  }

  // Tests

  public void test0() throws Exception {
    assertWorks("list");
  }

  public void test1() throws Exception {
    assertWorks("list");
  }

  public void test2() throws Exception {
    assertWorks("list");
  }

  public void test3() throws Exception {
    assertWorks("listOfStrings");
  }

  public void testHierarchy() throws Exception {
    assertWorks("c");
  }
}

