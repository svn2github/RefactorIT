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
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.refactorings.ImportUtils;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.source.edit.CompoundASTImpl;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.transformations.TransformationManager;

import org.apache.log4j.Category;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author vadim
 */
public class CleanImportsTest extends RefactoringTestCase {
  /** Logger instance. */
  private static final Category cat =
      Category.getInstance(CleanImportsTest.class.getName());

  public CleanImportsTest(String name) {
    super(name);
  }

  public String getTemplate() {
    return "CleanImports/<stripped_test_name>/<in_out>";
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite(CleanImportsTest.class);
    suite.setName("CleanImports tests");
    return suite;
  }

  protected void setUp() throws Exception {
    FormatSettings.applyStyle(new FormatSettings.AqrisStyle());
  }

  private void cleanImports(String typeName) throws Exception {
    cat.info("Testing " + getStrippedTestName());
    Project project = getMutableProject();

    List compilationUnits = project.getCompilationUnits();
    final ASTImpl[][] unusedImports = new ASTImpl[compilationUnits.size()][];

    for (int i = 0, max = compilationUnits.size(); i < max; i++) {
      unusedImports[i] = ImportUtils.listUnusedImports((CompilationUnit) compilationUnits.
          get(i));
    }

    List usages = new ArrayList();

    for (int i = 0, max = compilationUnits.size(); i < max; i++) {
      for (int j = 0; j < unusedImports[i].length; j++) {
        usages.add(
            new InvocationData(null, compilationUnits.get(i), unusedImports[i][j]));
      }
    }

    TransformationManager manager = new TransformationManager(null);

    for (int i = 0, max = usages.size(); i < max; i++) {
      InvocationData id = (InvocationData) usages.get(i);
      CompilationUnit sf = id.getCompilationUnit();
      ASTImpl importNode = id.getWhereAst();

      CompoundASTImpl node = new CompoundASTImpl(importNode.getParent());
      final StringEraser eraser = new StringEraser(sf, node, true);
      manager.add(eraser);
    }

    RefactoringStatus status = manager.performTransformations();
    assertTrue("perform change: " + status.getAllMessages(), status.isOk());

    final Project expected = getExpectedProject();
    RwRefactoringTestUtils.assertSameSources("", expected, project);
  }

  public void testCleanImports1() throws Exception {
    cleanImports("A");
  }
}
