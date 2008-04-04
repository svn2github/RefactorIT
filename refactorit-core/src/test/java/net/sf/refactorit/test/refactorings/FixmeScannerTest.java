/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings;



import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.ui.JWordDialog;
import net.sf.refactorit.ui.module.fixmescanner.FixmeCommentFinder;
import net.sf.refactorit.ui.module.fixmescanner.FixmeScannerTreeModel;
import net.sf.refactorit.ui.treetable.PositionableTreeNode;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class FixmeScannerTest extends TestCase {
  private Project testProject;
  private List fixmeWords;

  public FixmeScannerTest(String name) {
    super(name);
  }

  public static Test suite() throws Exception {
    final TestSuite suite = new TestSuite(FixmeScannerTest.class);
    suite.setName("FIXME Scanner");
    return suite;
  }

  public void setUp() throws Exception {
    testProject = Utils.createTestRbProject(
        Utils.getTestProjects().getProject("FIXME Scanner"));

    testProject.getProjectLoader().build();

    fixmeWords = new ArrayList(2);
    fixmeWords.add("FIXME");
    fixmeWords.add("TODO");
  }

  public void testFixmeCount() {
    FixmeScannerTreeModel model = new FixmeScannerTreeModel(
        testProject.getCompilationUnits(), fixmeWords, false, true, 0, 0);

    int fixmeCount = model.getChildCount(
        model.getChild(model.getChild(model.getRoot(), 0), 0));

    assertEquals(12, fixmeCount);
  }

  public void testFindingInRange() {
    int methodStartLine = 16;
    int methodEndLine = 22;

    FixmeScannerTreeModel model = new FixmeScannerTreeModel(
        testProject.getCompilationUnits(), fixmeWords, false, false,
        methodStartLine, methodEndLine);

    int fixmeCount = model.getChildCount(
        model.getChild(model.getChild(model.getRoot(), 0), 0));

    assertEquals(1, fixmeCount);
  }

  public void testNoExtraLinesAppearWhenUnitingMultilineComments() {
    FixmeScannerTreeModel model = new FixmeScannerTreeModel(
        testProject.getCompilationUnits(), fixmeWords, false, true, 0, 0);

    Object parentOfAllCommentNodes = model.getChild(
        model.getChild(model.getRoot(), 0), 0);

    PositionableTreeNode twoLineComment = (PositionableTreeNode)
        model.getChild(parentOfAllCommentNodes, 1);

    assertEquals(
        "// FIXME - a two-line fixme\r\n" +
        "// This is the second line of the two-line one",
        twoLineComment.getUserObject().toString());
  }

  public void testRegexp() throws Exception {
    Project p = Utils.createTestRbProjectFromString(
        "public class X {\n" +
        "  // asd FIXME\n" + // Note that FIXME is not the first word in the comment
        "}", "X.java", null);

    FixmeCommentFinder finder = new FixmeCommentFinder(
        CollectionUtil.singletonArrayList(
            new JWordDialog.Word("FIXME", true)));

    List result = finder.getFixmeComments(
        (CompilationUnit) p.getCompilationUnits().get(0), true, -1, -1);

    assertEquals(1, result.size());

    assertEquals("Comment: \"// asd FIXME\" 2:3 - 2:15",
        result.get(0).toString());
  }

  public void testCommentInsideMethod() throws Exception {
    Project p = Utils.createTestRbProjectFromString(
        "public class X {\n" +
        "  public void m() {\n" +
        "    int i; // asd FIXME\n" +
        "  }\n" +
        "}", "X.java", null);

    FixmeCommentFinder finder = new FixmeCommentFinder(
        CollectionUtil.singletonArrayList(
            new JWordDialog.Word("FIXME", true)));

    List result = finder.getFixmeComments(
        (CompilationUnit) p.getCompilationUnits().get(0), true, -1, -1);

    assertEquals(1, result.size());

    assertEquals("Comment: \"// asd FIXME\" 3:12 - 3:24",
        result.get(0).toString());
  }

  public void testAfterRebuild() throws Exception {
    Project p = Utils.createTestRbProjectFromString(
        "public class X {\n" +
        "  public void m() {\n" +
        "    int i; // asd FIXME\n" +
        "  }\n" +
        "}", "X.java", null);

    p.getProjectLoader().build(null, true);

    FixmeCommentFinder finder = new FixmeCommentFinder(
        CollectionUtil.singletonArrayList(
            new JWordDialog.Word("FIXME", true)));

    List result = finder.getFixmeComments(
        (CompilationUnit) p.getCompilationUnits().get(0), true, -1, -1);

    assertEquals(1, result.size());
    assertEquals("Comment: \"// asd FIXME\" 3:12 - 3:24",
        result.get(0).toString());
  }
}
