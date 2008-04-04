/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings.introducetemp;


import net.sf.refactorit.classmodel.BinSelection;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.introducetemp.IntroduceTemp;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.refactorings.BinSelectionFinder;
import net.sf.refactorit.test.refactorings.NullContext;
import net.sf.refactorit.test.refactorings.RefactoringTestCase;
import net.sf.refactorit.utils.RefactorItConstants;

import org.apache.log4j.Category;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author Anton Safonov
 */
public class AllTests extends RefactoringTestCase {
  /** Logger instance. */
  private static final Category cat
      = Category.getInstance(CanIntroduceTest.class.getName());

  public AllTests(String name) {
    super(name);
  }

  protected void setUp() throws Exception {
    FormatSettings.applyStyle(new FormatSettings.AqrisStyle());
    GlobalOptions.setOption(FormatSettings.PROP_FORMAT_TAB_SIZE, Integer.toString(4));
    GlobalOptions.setOption(FormatSettings.PROP_FORMAT_USE_SPACES_IN_PLACE_OF_TABS, "false");
  }

  public static Test suite() {
    TestSuite result = new TestSuite("Introduce Temp tests");
    result.addTest(CustomTest.suite());
    result.addTest(CanIntroduceTest.suite());
    result.addTest(CannotIntroduceTest.suite());
    return result;
  }

  public String getTemplate() {
    throw new RuntimeException("Called method which must be overriden");
  }

  public void runTestWithProblems(int startLine, int startColumn, int endLine,
      int endColumn, boolean replaceAll, boolean makeFinal, String tempName,
      String expectedName, int expectedUserInputSeverity) throws Exception {
    cat.info("Testing: " + getTestName());

    final Project project = getMutableProject();
    final CompilationUnit compilationUnit
        = (CompilationUnit) project.getCompilationUnits().get(0);

    final BinSelection selection = BinSelectionFinder.
        getSelectionByHumanCoordinates(
        compilationUnit, startLine, startColumn, endLine, endColumn, 4);
    final String selectionText = selection.getText();

    final IntroduceTemp ref = new IntroduceTemp(
        new NullContext(project), selection);

    RefactoringStatus status = ref.checkPreconditions();

    ref.setReplaceAll(replaceAll);
    ref.setDeclareFinal(makeFinal);
    ref.setNewVarName(tempName);

    // TODO improve heuristics
    if (RefactorItConstants.runNotImplementedTests) {
      assertEquals("temp name incorrectly guessed", expectedName,
          ref.getPossibleName());
    }

    if (status.isOk()) {
      status.merge(ref.checkUserInput());
    }

    if (expectedUserInputSeverity == RefactoringStatus.UNDEFINED) {
      assertTrue("checked conditions: " + status.getAllMessages()
          + ",\n[" + StringUtil.printableLinebreaks(selectionText) + "]",
          status.isOk());
    } else {
      assertEquals("expected to fail: " + status.getAllMessages()
          + ",\n[" + StringUtil.printableLinebreaks(selectionText) + "]",
          expectedUserInputSeverity, status.getSeverity());
      return;
    }
    status.merge(ref.apply());

    assertTrue("performed change: " + status.getAllMessages()
        + ",\n[" + StringUtil.printableLinebreaks(selectionText) + "]",
        status.isOk());

    RwRefactoringTestUtils.assertSameSources("edited well",
        getExpectedProject(), project);

    cat.info("SUCCESS");
  }

  public void runTest(int startLine, int startColumn, int endLine,
      int endColumn,
      boolean replaceAll, boolean makeFinal, String tempName,
      String guessedTempName) throws Exception {
    runTestWithProblems(startLine, startColumn, endLine, endColumn, replaceAll,
        makeFinal, tempName, guessedTempName, RefactoringStatus.UNDEFINED);
  }
}
