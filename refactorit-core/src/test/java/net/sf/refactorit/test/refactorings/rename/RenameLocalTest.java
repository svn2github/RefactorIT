/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings.rename;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.statements.BinIfThenElseStatement;
import net.sf.refactorit.classmodel.statements.BinTryStatement;
import net.sf.refactorit.classmodel.statements.BinVariableDeclaration;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.parser.SimpleASTImpl;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.rename.RenameLocal;
import net.sf.refactorit.refactorings.rename.RenameMultiLocal;
import net.sf.refactorit.refactorings.rename.RenameType;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.test.refactorings.NullContext;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import org.apache.log4j.Category;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class RenameLocalTest extends TestCase {
  /** Logger instance. */
  private static final Category cat =
      Category.getInstance(RenameLocalTest.class.getName());

  public RenameLocalTest(String name) {
    super(name);
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite(RenameLocalTest.class);
    suite.setName("Rename Local Variable");
    return suite;
  }

  protected void setUp() {
  }

  protected void tearDown() {
  }

  public void testRenameParametersInHierarchy() throws Exception {
    // Kirill Buhhalko //
    // this test only do renames in parameters of method, separately
    // (not in hierachy) throw RenameMultiLocal, which must work as RenameLocal
    // in that case

    Project project = RwRefactoringTestUtils.createMutableProject(Utils.
        createTestRbProject(
        "RenameLocal/RenameParametersInHierarchy/in"));

    Project projectOut = Utils.
        createTestRbProject("RenameLocal/RenameParametersInHierarchy/out1");

    try {
      project.getProjectLoader().build();
    } catch (Exception e) {
      fail("Could not load test project.");
    }

    String[] classes = {"A", "B", "B1", "C"};

    for (int x = 0; x < classes.length; x++) {

      BinCIType type = project.findTypeRefForName(classes[x]).
          getBinCIType(); ;
      BinMethod method = type.getAccessibleMethods("method", type)[0];
      BinParameter[] params = method.getParameters();

      String[] newParameterNames = new String[] {"aa", "bb", "cc"};

      for (int i = 0; i < params.length; i++) {
        final RenameMultiLocal renameMultiLocal
            = new RenameMultiLocal(new NullContext(project), params[i]);

        renameMultiLocal.setRenameInJavadocs(true);
        renameMultiLocal.setNewName(newParameterNames[i]);
        renameMultiLocal.setIncludeOverridedMethods(false);

        final RefactoringStatus status = renameMultiLocal.checkPreconditions();
        status.merge(renameMultiLocal.checkUserInput());

        status.merge(renameMultiLocal.apply());

        if (!status.isOk()) {
          fail("Renaming " + params[i].getQualifiedName()
              + " -> " + newParameterNames[i] + " failed."
              + " Message: " + status.getAllMessages());
        }

        project.getProjectLoader().build(null, false);
        // type was rebuilt, so have to find everything again
        type = project.findTypeRefForName(classes[x]).
            getBinCIType();
        method = type.getAccessibleMethods("method", type)[0];
        params = method.getParameters();
      }

    }

    RwRefactoringTestUtils.assertSameSources(
        "Extracted selection", projectOut, project);

    cat.info("SUCCESS");
  }

  public void testRenameParametersInHierarchy2() throws Exception {
    // Kirill Buhhalko //
    // this test do renames in parameters, but it use
    // rename in hierachy, so renaming in one class must rename parameters
    // of method in other classes too

    Project projectOut = Utils.
        createTestRbProject("RenameLocal/RenameParametersInHierarchy/out2");

    Project project;


    String[] classes = {"A", "B1", "C", "B"};

    for (int x = 0; x < classes.length; x++) {

      project  = RwRefactoringTestUtils.createMutableProject(Utils.
        createTestRbProject(
        "RenameLocal/RenameParametersInHierarchy/in"));

      try {
        project.getProjectLoader().build();
      } catch (Exception e) {
        fail("Could not load test project.");
      }

      BinCIType type = project.findTypeRefForName(classes[x]).
          getBinCIType(); ;
      BinMethod method = type.getAccessibleMethods("method", type)[0];
      BinParameter[] params = method.getParameters();

      String[] newParameterNames = new String[] {"aa", "bb", "cc"};

      for (int i = 0; i < params.length; i++) {
        final RenameMultiLocal renameMultiLocal
            = new RenameMultiLocal(new NullContext(project), params[i]);

        renameMultiLocal.setRenameInJavadocs(true);
        renameMultiLocal.setNewName(newParameterNames[i]);
        renameMultiLocal.setIncludeOverridedMethods(true);
        final RefactoringStatus status = renameMultiLocal.checkPreconditions();
        status.merge(renameMultiLocal.checkUserInput());

        status.merge(renameMultiLocal.apply());

        if (!status.isOk()) {
          fail("Renaming " + params[i].getQualifiedName()
              + " -> " + newParameterNames[i] + " failed."
              + " Message: " + status.getAllMessages());
        }

        project.getProjectLoader().build(null, false);
        // type was rebuilt, so have to find everything again
        type = project.findTypeRefForName(classes[x]).
            getBinCIType();
        method = type.getAccessibleMethods("method", type)[0];
        params = method.getParameters();
      }


      // rename parameters of method in any class, must, rename in all hierarhy
      if (classes[x].equals("B")) {
        projectOut = Utils.
        createTestRbProject("RenameLocal/RenameParametersInHierarchy/out3");
      }

      RwRefactoringTestUtils.assertSameSources(
        "Renaming in class " + classes[x] , projectOut, project);
    }

    cat.info("SUCCESS");
  }

  public void testRenameParametersInHierarchy3() throws Exception {
    // Kirill Buhhalko //
    // this test do renames in parameters, but it use
    // rename in hierachy, so renaming in one class must rename parameters
    // of method in other classes too..
    // !!there in hierarchy are some parameters, which already has new name,
    // but renaming must success anyway.

    Project projectOut = Utils.
        createTestRbProject("RenameLocal/RenameParametersInHierarchy/out_");

    Project project;


    String[] classes = {"A", "B1", "C"};

    for (int x = 0; x < classes.length; x++) {

      project  = RwRefactoringTestUtils.createMutableProject(Utils.
        createTestRbProject(
        "RenameLocal/RenameParametersInHierarchy/in_"));

      try {
        project.getProjectLoader().build();
      } catch (Exception e) {
        fail("Could not load test project.");
      }

      BinCIType type = project.findTypeRefForName(classes[x]).
          getBinCIType(); ;
      BinMethod method = type.getAccessibleMethods("method", type)[0];
      BinParameter[] params = method.getParameters();

      String[] newParameterNames = new String[] {"aaa", "bbb", "ccc"};

      for (int i = 0; i < params.length; i++) {
        final RenameMultiLocal renameMultiLocal
            = new RenameMultiLocal(new NullContext(project), params[i]);

        renameMultiLocal.setRenameInJavadocs(true);
        renameMultiLocal.setNewName(newParameterNames[i]);
        renameMultiLocal.setIncludeOverridedMethods(true);
        final RefactoringStatus status = renameMultiLocal.checkPreconditions();
        status.merge(renameMultiLocal.checkUserInput());

        status.merge(renameMultiLocal.apply());

        if (!status.isOk()) {
          fail("Renaming " + params[i].getQualifiedName()
              + " -> " + newParameterNames[i] + " failed."
              + " Message: " + status.getAllMessages());
        }

        project.getProjectLoader().build(null, false);
        // type was rebuilt, so have to find everything again
        type = project.findTypeRefForName(classes[x]).
            getBinCIType();
        method = type.getAccessibleMethods("method", type)[0];
        params = method.getParameters();
      }


      // rename parameters of method in any class, must, rename in all hierarhy
      if (classes[x].equals("B")) {
        projectOut = Utils.
        createTestRbProject("RenameLocal/RenameParametersInHierarchy/out3");
      }

      RwRefactoringTestUtils.assertSameSources(
        "Renaming in class " + classes[x] , projectOut, project);
    }

    cat.info("SUCCESS");
  }



  public void testRenameSeveralOnSameLine() throws Exception {
    cat.info("Testing rename several locals on the line");

    final Project project =
        RwRefactoringTestUtils.createMutableProject(
        Utils.createTestRbProject(Utils.getTestProjects()
        .getProject("RenameLocal_several_on_same_line_in")));
    try {
      project.getProjectLoader().build();
    } catch (Exception e) {
      if ((project.getProjectLoader().getErrorCollector()).hasUserFriendlyErrors()) {
        fail((project.getProjectLoader().getErrorCollector()).getUserFriendlyErrors().next().toString());
      }
    }

    BinCIType type
        = project.findTypeRefForName("Test").getBinCIType();
    BinMethod method = type.getAccessibleMethods("method", type)[0];
    BinParameter[] params = method.getParameters();

    String[] newParameterNames = new String[] {"aaaaa", "u"};

    for (int i = 0; i < params.length; i++) {
      final RenameLocal renameLocal
          = new RenameLocal(new NullContext(project), params[i]);
      renameLocal.setRenameInJavadocs(true);
      renameLocal.setNewName(newParameterNames[i]);
      final RefactoringStatus status = renameLocal.checkPreconditions();
      status.merge(renameLocal.checkUserInput());

      status.merge(renameLocal.apply());

      if (!status.isOk()) {
        fail("Renaming " + params[i].getQualifiedName()
            + " -> " + newParameterNames[i] + " failed."
            + " Message: " + status.getAllMessages());
      }

      project.getProjectLoader().build(null, false);
      // type was rebuilt, so have to find everything again
      type = project.findTypeRefForName("Test").getBinCIType();
      method = type.getAccessibleMethods("method", type)[0];
      params = method.getParameters();
    }

    RenameType renameType
        = new RenameType(new NullContext(project), type);
    renameType.setRenameInJavadocs(true);
    renameType.setNewName("TestXXX");
    final RefactoringStatus status = renameType.checkPreconditions();
    status.merge(renameType.checkUserInput());

    status.merge(renameType.apply());

    if (!status.isOk()) {
      fail("Renaming " + type.getQualifiedName()
          + " -> TestXXX failed. Message to user: " + status.getAllMessages());
    }

    final Project expected =
        Utils.createTestRbProject(Utils.getTestProjects()
        .getProject("RenameLocal_several_on_same_line_out"));
    RwRefactoringTestUtils.assertSameSources(
        "Renamed locals", expected, project);

    cat.info("SUCCESS");
  }

  public void testRenameIfVarWithSameNameExistsButInDifferentScope() throws
      Exception {
    cat.info(
        "Testing rename if variable with same name exists but in different scope");

    final Project project =
        RwRefactoringTestUtils.createMutableProject(
        Utils.createTestRbProject(Utils.getTestProjects()
        .getProject("RenameLocal_same_name_in_different_scope_in")));
    try {
      project.getProjectLoader().build();
    } catch (Exception e) {
      if ((project.getProjectLoader().getErrorCollector()).hasUserFriendlyErrors()) {
        fail((project.getProjectLoader().getErrorCollector()).getUserFriendlyErrors().next().toString());
      }
    }

    BinCIType type
        = project.findTypeRefForName("Test").getBinCIType();
    BinMethod method = type.getAccessibleMethods("f", type)[0];

    BinIfThenElseStatement s1
        = (BinIfThenElseStatement) method.getBody().getStatements()[0];
    BinVariableDeclaration s2
        = (BinVariableDeclaration) s1.getTrueList().getStatements()[0];
    BinLocalVariable var = (BinLocalVariable) s2.getVariables()[0];
    String newParameterName = "c2";

    final RenameLocal renameLocal
        = new RenameLocal(new NullContext(project), var);

    renameLocal.setRenameInJavadocs(true);
    renameLocal.setNewName(newParameterName);

    RefactoringStatus status = renameLocal.checkPreconditions();
    status.merge(renameLocal.checkUserInput());

    status.merge(renameLocal.apply());

    assertTrue("Renaming " + var.getQualifiedName()
        + " -> " + newParameterName + " succeeded: " + status.getAllMessages(),
        status.isOk());

    final Project expected =
        Utils.createTestRbProject(Utils.getTestProjects()
        .getProject("RenameLocal_same_name_in_different_scope_out"));
    RwRefactoringTestUtils.assertSameSources(
        "Renamed locals", expected, project);

    cat.info("SUCCESS");
  }

  public void testInAccessibleScope() throws Exception {
    cat.info(
        "Testing rename if variable with same name exists in upper/inner scopes");

    final Project project =
        Utils.createTestRbProject(Utils.getTestProjects()
        .getProject("RenameLocal_same_name_in_accessible_scope"));
    project.getProjectLoader().build();

    BinCIType type
        = project.getTypeRefForName("Test").getBinCIType();
    BinMethod method = type.getDeclaredMethods()[0];

    BinTryStatement s1
        = (BinTryStatement) method.getBody().getStatements()[1];
    BinLocalVariable var = s1.getCatches()[0].getParameter();

    final RenameLocal renameLocal
        = new RenameLocal(new NullContext(project), var);

    renameLocal.setNewName("iii");
    RefactoringStatus status = renameLocal.checkPreconditions();
    status.merge(renameLocal.checkUserInput());
    assertFalse("Renaming " + var.getQualifiedName() + " -> "
        + renameLocal.getNewName() + " not allowed.", status.isOk());
    status.clear();

    renameLocal.setNewName("jjj");
    status = renameLocal.checkPreconditions();
    status.merge(renameLocal.checkUserInput());
    assertFalse("Renaming " + var.getQualifiedName() + " -> "
        + renameLocal.getNewName() + " not allowed.", status.isOk());
    status.clear();

    renameLocal.setNewName("kkk");
    status = renameLocal.checkPreconditions();
    status.merge(renameLocal.checkUserInput());
    assertFalse("Renaming " + var.getQualifiedName() + " -> "
        + renameLocal.getNewName() + " not allowed.", status.isOk());
    status.clear();

    renameLocal.setNewName("lll");
    status = renameLocal.checkPreconditions();
    status.merge(renameLocal.checkUserInput());
    assertTrue("Renaming " + var.getQualifiedName() + " -> "
        + renameLocal.getNewName() + " allowed.", status.isOk());

    cat.info("SUCCESS");
  }

  public void testBug2209() throws Exception {
    cat.info("Testing bug #2209");
    final Project project =
        Utils.createTestRbProject(Utils.getTestProjects().getProject(
        "bug #2209"));
    project.getProjectLoader().build();
    assertFalse("project ok", (project.getProjectLoader().getErrorCollector()).hasUserFriendlyErrors());
    final BinCIType testClass = project.getTypeRefForName(
        "bug2209.Test").getBinCIType();
    SimpleASTImpl ast = new SimpleASTImpl();
    ast.setLine(5);
    ast.setColumn(10);
    BinTreeTableNode treeTableNode = new BinTreeTableNode(testClass);
    treeTableNode.addAst(ast);
    final List asts = treeTableNode.getAsts();
    ASTImpl newAst = (ASTImpl) asts.get(0);
    assertTrue("Same asts are in the treeTableNode", newAst == ast);
    cat.info("SUCCESS");
  }

}
