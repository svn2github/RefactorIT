/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings;



import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.ui.module.calltree.CallTreeModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import org.apache.log4j.Category;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author Anton Safonov
 */
public class CallTreeTest extends RefactoringTestCase {

  /** Logger instance. */
  private static final Category cat =
      Category.getInstance(CallTreeTest.class.getName());

  public CallTreeTest(String name) {
    super(name);
  }

  public String getTemplate() {
    return "CallTree/<stripped_test_name>/<in_out>";
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite(CallTreeTest.class);
    suite.setName("CallTree tests");
    return suite;
  }

  protected void setUp() throws Exception {
    FormatSettings.applyStyle(new FormatSettings.AqrisStyle());
  }

  public void testBug2060() throws Exception {
    cat.info("Testing " + getStrippedTestName());

    final Project project = Utils.createTestRbProject(
        Utils.getTestProjects().getProject("CallTree_bug2060"));
    project.getProjectLoader().build();

    final BinTypeRef subTest = project.getTypeRefForName(
        "y.SubTest");
    assertNotNull(subTest);

    final BinMethod method
        = subTest.getBinCIType().getDeclaredMethod("method", BinParameter.NO_PARAMS);
    assertNotNull(method);

    final CallTreeModel model = new CallTreeModel(project, method);

    BinTreeTableNode root = (BinTreeTableNode) model.getRoot();
    assertNotNull(root);

    assertEquals("root has single invocation", 1, root.getChildCount());

    root = (BinTreeTableNode) root.getChildAt(0);
    assertEquals(
        subTest.getBinCIType().getDeclaredMethod("methodXXX",
        BinParameter.NO_PARAMS),
        root.getBin());

    assertEquals("second level node has single invocation", 1,
        root.getChildCount());

    root = (BinTreeTableNode) root.getChildAt(0);
    assertEquals(
        ((BinClass) project.getTypeRefForName("x.AnotherTest")
        .getBinCIType()).getInitializers()[0],
        root.getBin());

    assertEquals("third level node has no more invocations", 0,
        root.getChildCount());

    cat.info("SUCCESS");
  }
}
