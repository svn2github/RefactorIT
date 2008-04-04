/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
/* $Id: CreateMissingMethodTest.java,v 1.10 2005/06/16 12:24:26 kirill Exp $ */
package net.sf.refactorit.test.refactorings;



import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.createmissing.CreateMethodContext;
import net.sf.refactorit.refactorings.createmissing.CreateMissingMethodRefactoring;
import net.sf.refactorit.source.MethodNotFoundError;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.test.RwRefactoringTestUtils;

import org.apache.log4j.Category;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author Anton Safonov
 */
public class CreateMissingMethodTest extends RefactoringTestCase {

  /** Logger instance. */
  private static final Category cat =
      Category.getInstance(CreateMissingMethodTest.class.getName());

  public CreateMissingMethodTest(String name) {
    super(name);
  }

  public String getTemplate() {
    return "CreateMissingMethod/<stripped_test_name>/<in_out>";
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite(CreateMissingMethodTest.class);
    suite.setName("CreateMissingMethod tests");
    return suite;
  }

  protected void setUp() throws Exception {
    FormatSettings.applyStyle(new FormatSettings.AqrisStyle());
  }

  /**
   * @throws Exception
   */
  private void attemptFixingAllUserFriendlyErrors(boolean createStaticMethods) throws Exception {
    cat.info("Testing " + getStrippedTestName());

    final Project project = getMutableProject();
    project.discoverAllUsedTypes();

    Iterator it = (project.getProjectLoader().getErrorCollector()).getUserFriendlyErrors();
    assertTrue("There are errors to be fixed.", it.hasNext());
    List nodes = new ArrayList();
    while (it.hasNext()) {
      MethodNotFoundError next;
      Object error = it.next();
      try {
        next = (MethodNotFoundError) error;
      } catch(ClassCastException e) {
        throw new RuntimeException("Source parsing error: " + error, e);
      }
      CreateMethodContext node = new CreateMethodContext(next);
      node.setStaticMethod(createStaticMethods);
      nodes.add(node);
    }

    final CreateMissingMethodRefactoring refactoring
        = new CreateMissingMethodRefactoring(new NullContext(project),
        (CreateMethodContext[]) nodes.toArray(
            new CreateMethodContext[nodes.size()]));
    final RefactoringStatus status = refactoring.checkPreconditions();
    status.merge(refactoring.checkUserInput());
    status.merge(refactoring.apply());

    assertTrue("Created missing method", status.isOk());

    RwRefactoringTestUtils.assertSameSources("", getExpectedProject(), project);

    cat.info("SUCCESS");
  }

  public void testAddingImportOfArrayType() throws Exception {
    attemptFixingAllUserFriendlyErrors(true);
  }

  public void testNullParameter() throws Exception {
    attemptFixingAllUserFriendlyErrors(false);
  }

  public void testNoParameters() throws Exception {
    attemptFixingAllUserFriendlyErrors(false);
  }

  public void testAnonymousClassParameter() throws Exception {
    attemptFixingAllUserFriendlyErrors(false);
  }

  public void testInterfaceMethod() throws Exception {
    // method in interface should not have body
    attemptFixingAllUserFriendlyErrors(false);
  }

  public void testGuessReturnTypeInReturnStatement() throws Exception {
    attemptFixingAllUserFriendlyErrors(false);
  }

  public void testGuessReturnTypeUsingClassCastExpression() throws Exception {
    attemptFixingAllUserFriendlyErrors(false);
  }
}
