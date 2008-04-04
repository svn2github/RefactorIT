/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings.inlinemethod;

import net.sf.refactorit.classmodel.BinSelection;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.statements.BinExpressionStatement;
import net.sf.refactorit.classmodel.statements.BinStatement;
import net.sf.refactorit.classmodel.statements.BinStatementList;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.loader.Comment;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.SelectionAnalyzer;
import net.sf.refactorit.refactorings.inlinemethod.InlineMethod;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.refactorings.BinSelectionFinder;
import net.sf.refactorit.test.refactorings.NullContext;
import net.sf.refactorit.test.refactorings.RefactoringTestCase;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.NullDialogManager;
import net.sf.refactorit.utils.RefactorItConstants;
import net.sf.refactorit.vfs.local.LocalSource;

import java.util.Iterator;
import java.util.List;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;



/**
 * @author Anton Safonov
 */
public class InlineMethodTest extends RefactoringTestCase {

  public InlineMethodTest(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(InlineMethodTest.class);
  }

  public String getTemplate() {
    return "InlineMethod/<extra_name>/" + getCapitalizedName() + ".java";
  }

  protected void setUp() throws Exception {
    FormatSettings.applyStyle(new FormatSettings.AqrisStyle());
    GlobalOptions.setOption(FormatSettings.PROP_FORMAT_TAB_SIZE, Integer.toString(4));
    GlobalOptions.setOption(FormatSettings.PROP_FORMAT_BLOCK_INDENT, Integer.toString(4));
    GlobalOptions.setOption(FormatSettings.PROP_FORMAT_USE_SPACES_IN_PLACE_OF_TABS, "false");
  }

  protected void performExpectErrorTest(String inputFolder,  String id) throws Exception {
  	performTest(inputFolder, id, BinSelectionFinder.VALID_SELECTION, null, true);
  }

  protected void performTest(String inputFolder,
      String id, int mode, String outputFolder) throws Exception {
  	performTest(inputFolder, id, mode, outputFolder, false);
  }

  protected void performTest(String inputFolder,
      String id, int mode, String outputFolder, boolean expectErrorInPreconditions) throws Exception {

    Project project = getInitialProject(inputFolder);

    project = getMutableProject(project);

    renameToOut(project);

    if (mode != BinSelectionFinder.INVALID_SELECTION) {
      assertTrue("has sources", project.getCompilationUnits().size() > 0);
    }
    final CompilationUnit compilationUnit = (CompilationUnit) project.getCompilationUnits().get(0);
    final BinSelection selection = BinSelectionFinder.findSelectionIn(
        compilationUnit);
    final String selectionText = selection.getText();

    SelectionAnalyzer analyzer = new SelectionAnalyzer(selection);

    if (mode != BinSelectionFinder.INVALID_SELECTION
        && (project.getProjectLoader().getErrorCollector()).hasErrors()) {
      fail("shouldn't have errors: "
          + CollectionUtil.toList((project.getProjectLoader().getErrorCollector()).getUserFriendlyErrors()));
    }

    List las = analyzer.getSelectedItems();
    BinExpression top = analyzer.findTopExpression();
    BinMethodInvocationExpression expr = null;
    assertTrue("something selected", las.size() > 0 || top != null
        || (project.getProjectLoader().getErrorCollector()).hasUserFriendlyErrors());
    if (top != null) {
      if (top instanceof BinMethodInvocationExpression) {
        expr = (BinMethodInvocationExpression) top;
      } else {
        System.err.println("strange top: " + top);
      }
    } else {
//System.err.println("qqqqqqqqqqqqqqqq");
      Iterator lasIt = las.iterator();
      while (lasIt.hasNext()) {
        Object obj = lasIt.next();
//System.err.println("ttttt: " + obj);
        if (obj instanceof BinMethodInvocationExpression) {
          expr = (BinMethodInvocationExpression) obj;
          break;
        } else if (obj instanceof BinExpressionStatement) {
          if (((BinExpressionStatement) obj).getExpression()
              instanceof BinMethodInvocationExpression) {
            expr = (BinMethodInvocationExpression)
                ((BinExpressionStatement) obj).getExpression();
            break;
          } else {
            System.err.println("strange expr: "
                + ((BinExpressionStatement) obj).getExpression());
          }
        } else if (obj instanceof BinStatementList) {
          if (((BinStatementList) obj).getStatements().length > 1) {
            System.err.println("too much selected: " + obj);
          } else {
            BinStatement stmt = ((BinStatementList) obj).getStatements()[0];
            if (stmt instanceof BinExpressionStatement) {
              if (((BinExpressionStatement) stmt).getExpression()
                  instanceof BinMethodInvocationExpression) {
                expr = (BinMethodInvocationExpression)
                    ((BinExpressionStatement) stmt).getExpression();
                break;
              } else {
                System.err.println("strange expr2: "
                    + ((BinExpressionStatement) stmt).getExpression());
              }
            } else {
              System.err.println("strange stmt2: " + stmt);
            }
          }
        } else if (obj instanceof Comment) {
          // skip
        } else {
          System.err.println("strange stmt: " + obj);
        }
      }
    }

    if (expr == null) {
      if (mode != BinSelectionFinder.INVALID_SELECTION) {
        fail("Failed to analyze selection: \"" + selectionText + "\"");
      }
      return;
    }

    final InlineMethod ref = new InlineMethod(new NullContext(project), expr);

    RefactoringStatus status = ref.checkPreconditions();

    if (mode == BinSelectionFinder.INVALID_SELECTION) {
      return;
    }
    if (expectErrorInPreconditions) {
    	assertTrue("Error in preconditions expected", status.isErrorOrFatal());
    	return;
    }

    if (status.isOk()) {
      status.merge(ref.checkUserInput());
    }

    assertTrue("checked conditions: " + status.getAllMessages()
        + ",\n[" + StringUtil.printableLinebreaks(selectionText) + "]",
        status.isOk());

    if (!status.isOk()) {
      return;
    }
    if(inputFolder == "simple_in") {
//      System.out.println();
    }

    ref.setMethodDeclarationAction(InlineMethod.LEAVE_METHOD_DECLARATION);

    status.merge(ref.apply());

    assertTrue("performed change: " + status.getAllMessages()
        + ",\n[" + StringUtil.printableLinebreaks(selectionText) + "]",
        status.isOk());

    TransformationManager manager = new TransformationManager(null);

    cleanComments(project, manager, "/*[*/");
    cleanComments(project, manager, "/*]*/");

    manager.performTransformations();



    if (mode == BinSelectionFinder.COMPARE_WITH_OUTPUT) {
      RwRefactoringTestUtils.assertSameSources(
          "same sources", getExpectedProject(outputFolder), project);
    }
  }

  /**
   * @param project
   * @param manager
   */
  private void cleanComments(Project project, TransformationManager manager, String toDelete) {
  	List compilationUnits = project.getCompilationUnits();
  	int len = toDelete.length();
  	for (Iterator iter = compilationUnits.iterator(); iter.hasNext();) {
  		CompilationUnit cu = (CompilationUnit) iter.next();
  		String content = cu.getContent();
  		int last = 0;
  		for (int i = content.indexOf(toDelete); i != -1; i = content.indexOf(toDelete, i + len)) {
  			final StringEraser eraser = new StringEraser(cu, i, i + len);
  			eraser.setRemoveLinesContainingOnlyComments(true);
  			manager.add(eraser);
  		}


  	}
  }

  public String getCapitalizedName() {
    String name = super.getName();
    return Character.toUpperCase(name.charAt(0)) + name.substring(1);
  }

  /************************ Invalid Tests ********************************/

  protected void performInvalidTest() throws Exception {
    performTest("invalid", getName(), BinSelectionFinder.INVALID_SELECTION, null);
  }

  public void testRecursion() throws Exception {
    performInvalidTest();
  }

  public void testFieldInitializer() throws Exception {
    performInvalidTest();
  }

  public void testLocalInitializer() throws Exception {
    performInvalidTest();
  }

  public void testInterruptedStatement() throws Exception {
    performInvalidTest();
  }

  public void testMultiLocal() throws Exception {
    performInvalidTest();
  }

  public void testComplexBody() throws Exception {
    performInvalidTest();
  }

  public void testCompileError1() throws Exception {
    performInvalidTest();
  }

  public void testCompileError2() throws Exception {
    performInvalidTest();
  }

  public void testCompileError3() throws Exception {
    performInvalidTest();
  }

  /************************ Simple Tests ********************************/

  private void performSimpleTest() throws Exception {
    performTest("simple_in", getName(), BinSelectionFinder.COMPARE_WITH_OUTPUT,
        "simple_out");
  }

  public void testBasic1() throws Exception {
    performSimpleTest();
  }

  public void testBasic2() throws Exception {
    performSimpleTest();
  }

  public void testEmptyBody() throws Exception {
    performSimpleTest();
  }

  public void testPrimitiveArray() throws Exception {
    performSimpleTest();
  }

  public void testTypeArray() throws Exception {
    performSimpleTest();
  }

  public void testInitializer() throws Exception {
    performSimpleTest();
  }

  public void testSuper() throws Exception {
    performSimpleTest();
  }

  public void testIssue285() throws Exception {
  	performSimpleTest();
  }

  public void testUnusedReturn() throws Exception {
  	performSimpleTest();
  }

  public void testRecursive() throws Exception {
  	performSimpleTest();
  }

  public void testRecursive2() throws Exception {
  	performSimpleTest();
  }

  public void testRecursive3() throws Exception {
  	performSimpleTest();
  }

  public void testPrimitiveParam() throws Exception {
  	performSimpleTest();
  }

  public void testIncDec() throws Exception {
  	performSimpleTest();
  }

  /************************ Argument Tests ********************************/

  private void performArgumentTest() throws Exception {
    performTest("argument_in", getName(),
        BinSelectionFinder.COMPARE_WITH_OUTPUT, "argument_out");
  }

  public void testFieldReference() throws Exception {
    performArgumentTest();
  }

  public void testLocalReferenceUnused() throws Exception {
    performArgumentTest();
  }

  public void testLocalReferenceRead() throws Exception {
    performArgumentTest();
  }

  public void testLocalReferenceRead2() throws Exception {
    performArgumentTest();
  }

  public void testLocalReferenceWrite() throws Exception {
    performArgumentTest();
  }

  public void testLocalReferenceLoop() throws Exception {
    performArgumentTest();
  }

  public void testLocalReferencePrefix() throws Exception {
    performArgumentTest();
  }

  public void testLiteralReferenceRead() throws Exception {
    performArgumentTest();
  }

  public void testLiteralReferenceWrite() throws Exception {
    performArgumentTest();
  }

  public void testParameterNameUsed1() throws Exception {
    performArgumentTest();
  }

  public void testParameterNameUsed2() throws Exception {
    performArgumentTest();
  }

  public void testParameterNameUsed3() throws Exception {
    performArgumentTest();
  }

  public void testParameterNameUsed4() throws Exception {
    performArgumentTest();
  }

  public void testParameterNameUnused1() throws Exception {
    performArgumentTest();
  }

  public void testParameterNameUnused2() throws Exception {
    performArgumentTest();
  }

  public void testParameterNameUnused3() throws Exception {
    performArgumentTest();
  }

  public void testOneRead() throws Exception {
    performArgumentTest();
  }

  public void testTwoReads() throws Exception {
    performArgumentTest();
  }

  public void testWrite() throws Exception {
    performArgumentTest();
  }

  /************************ Name Conflict Tests ********************************/

  private void performNameConflictTest() throws Exception {
    performTest("nameconflict_in", getName(),
        BinSelectionFinder.COMPARE_WITH_OUTPUT, "nameconflict_out");
  }

  public void testSameLocal() throws Exception {
    performNameConflictTest();
  }

  public void testSameType() throws Exception {
    performNameConflictTest();
  }

  public void testSameTypeAfter() throws Exception {
    performNameConflictTest();
  }

  public void testSameTypeInSibling() throws Exception {
    performNameConflictTest();
  }

  public void testLocalInType() throws Exception {
    performNameConflictTest();
  }

  public void testFieldInType() throws Exception {
    performNameConflictTest();
  }

  public void testSwitchStatement() throws Exception {
    performNameConflictTest();
  }

  public void testBlocks() throws Exception {
    performNameConflictTest();
  }

  /************************ Call Tests ********************************/

  private void performCallTest() throws Exception {
    performTest("call_in", getName(), BinSelectionFinder.COMPARE_WITH_OUTPUT,
        "call_out");
  }

  public void testExpressionStatement() throws Exception {
    performCallTest();
  }

  public void testExpressionStatementWithReturn() throws Exception {
    performCallTest();
  }

  public void testStatementWithFunction1() throws Exception {
    performCallTest();
  }

  public void testStatementWithFunction2() throws Exception {
    performCallTest();
  }

  public void testParenthesis() throws Exception {
    performCallTest();
  }

  /************************ Expression Tests ********************************/

  private void performExpressionTest() throws Exception {
    performTest("expression_in", getName(),
        BinSelectionFinder.COMPARE_WITH_OUTPUT, "expression_out");
  }

  public void testSimpleExpression() throws Exception {
    performExpressionTest();
  }

  public void testSimpleExpressionWithStatements() throws Exception {
    performExpressionTest();
  }

  public void testSimpleBody() throws Exception {
    performExpressionTest();
  }

  public void testAssignment() throws Exception {
    performExpressionTest();
  }

  public void testReturnStatement() throws Exception {
    performExpressionTest();
  }

  /************************ Control Statements Tests ********************************/

  private void performControlStatementTest() throws Exception {
    performTest("controlStatement_in", getName(),
        BinSelectionFinder.COMPARE_WITH_OUTPUT, "controlStatement_out");
  }

  public void testContainOnlyComments() throws Exception {
    performControlStatementTest();
  }

  public void testCommentIsLastStatement() throws Exception {
    performControlStatementTest();
  }

  public void testForEmpty() throws Exception {
    performControlStatementTest();
  }

  public void testForOne() throws Exception {
    performControlStatementTest();
  }

  public void testForTwo() throws Exception {
    // formatting
    if (RefactorItConstants.runNotImplementedTests) {
      performControlStatementTest();
    }
  }

  public void testIfThenTwo() throws Exception {
    // formatting
    if (RefactorItConstants.runNotImplementedTests) {
      performControlStatementTest();
    }
  }

  public void testIfElseTwo() throws Exception {
    // formatting
    if (RefactorItConstants.runNotImplementedTests) {
      performControlStatementTest();
    }
  }

  public void testForAssignmentOne() throws Exception {
    performControlStatementTest();
  }

  public void testForAssignmentTwo() throws Exception {
    // formatting
    if (RefactorItConstants.runNotImplementedTests) {
      performControlStatementTest();
    }
  }

  /************************ Receiver Tests ********************************/

  private void performReceiverTest() throws Exception {
    performTest("receiver_in", getName(),
        BinSelectionFinder.COMPARE_WITH_OUTPUT, "receiver_out");
  }

  public void testNoImplicitReceiver() throws Exception {
    performReceiverTest();
  }

  public void testNameThisReceiver() throws Exception {
    performReceiverTest();
  }

  public void testNameImplicitReceiver() throws Exception {
    performReceiverTest();
  }

  public void testExpressionZeroImplicitReceiver() throws Exception {
    performReceiverTest();
  }

  public void testExpressionOneImplicitReceiver() throws Exception {
    performReceiverTest();
  }

  public void testExpressionTwoImplicitReceiver() throws Exception {
    performReceiverTest();
  }

  public void testStaticReceiver() throws Exception {
    performReceiverTest();
  }

  public void testReceiverWithStatic() throws Exception {
    performReceiverTest();
  }

  public void testThisExpression() throws Exception {
    performReceiverTest();
  }

  /************************ Import Tests ********************************/

  private void performImportTest() throws Exception {
    // can't load test normally
    if (RefactorItConstants.runNotImplementedTests) {
      performTest("import_in", getName(),
          BinSelectionFinder.COMPARE_WITH_OUTPUT, "import_out");
    }
  }

  public void testUseArray() throws Exception {
    performImportTest();
  }

  public void testUseInArgument() throws Exception {
    performImportTest();
  }

  public void testUseInClassLiteral() throws Exception {
    performImportTest();
  }

  public void testUseInDecl() throws Exception {
    performImportTest();
  }

  public void testUseInDecl2() throws Exception {
    performImportTest();
  }

  public void testUseInDecl3() throws Exception {
    performImportTest();
  }

  public void testUseInDeclClash() throws Exception {
    performImportTest();
  }

  public void testUseInLocalClass() throws Exception {
    performImportTest();
  }

  /************************ Cast Tests ********************************/

  private void performCastTest() throws Exception {
    performTest("cast_in", getName(), BinSelectionFinder.COMPARE_WITH_OUTPUT,
        "cast_out");
  }

  public void testNotOverloaded() throws Exception {
    performCastTest();
  }

  public void testOverloadedPrimitives() throws Exception {
    performCastTest();
  }

  public void testNotCastableOverloaded() throws Exception {
    performCastTest();
  }

  public void testOverloaded() throws Exception {
    performCastTest();
  }

  public void testHierarchyOverloadedPrimitives() throws Exception {
    performCastTest();
  }

  public void testHierarchyOverloaded() throws Exception {
    performCastTest();
  }

  public void testHierarchyOverloadedPrivate() throws Exception {
    performCastTest();
  }

  public void testReceiverCast() throws Exception {
    performCastTest();
  }

  public void testNoCast() throws Exception {
    performCastTest();
  }

  private void performTooComplexTest() throws Exception {
    performExpectErrorTest("tooComplex", getName());
  }

  public void testDeepReturns() throws Exception {
  	performTooComplexTest();
  }

  public void testDeepReturns2() throws Exception {
  	performTooComplexTest();
  }

  public void testReadOnlySources() throws Exception {
    setName("testNoCast");
    LocalSource.fakeReadOnly = true;
    try {
      NullDialogManager d = (NullDialogManager) DialogManager.getInstance();
      d.customErrorString = "";

      try {
        performCastTest();
      } catch (AssertionFailedError ignore) {}

      assertTrue("Was: <" + d.customErrorString + ">",
          d.customErrorString.startsWith("Can not modify source"));
    } finally {
      LocalSource.fakeReadOnly = false;
    }
  }
  /************************ Vararity Tests ********************************/
  private void performVararityTest() throws Exception {
    performTest("vararity_in", getName(), BinSelectionFinder.COMPARE_WITH_OUTPUT,
        "vararity_out");
  }

  public void testOneObjectArg()throws Exception{
    performVararityTest();
  }

  public void testOneObjectArray0ElementArg()throws Exception{
    performVararityTest();
  }
  public void testOneObjectArray1ElementArg()throws Exception{
    performVararityTest();
  }
  public void testOneObjectArray3ElementsArg()throws Exception{
    performVararityTest();
  }

  /************************ Inners Tests ********************************/
  private void performInnerTest() throws Exception {
    performTest("inner_in", getName(), BinSelectionFinder.COMPARE_WITH_OUTPUT,
        "inner_out");
  }

  public void testIssue1352() throws Exception {
  	performInnerTest();
  }


  /************************ Inners Tests ********************************/
  private void performMultiClassTest() throws Exception {
    performTest("multiClass_in", getName(), BinSelectionFinder.COMPARE_WITH_OUTPUT,
        "multiClass_out");
  }

  public void testBroadedAccess() throws Exception {
  	performMultiClassTest();
  }

}
