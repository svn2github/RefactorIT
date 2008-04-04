/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings;



import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinSelection;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.parser.FastJavaLexer;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.extract.ExtractMethod;
import net.sf.refactorit.refactorings.extract.ExtractMethodAnalyzer;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.NullDialogManager;
import net.sf.refactorit.utils.RefactorItConstants;
import net.sf.refactorit.vfs.local.LocalSource;

import org.apache.log4j.Category;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Test driver for
 * {@link net.sf.refactorit.refactorings.extract.ExtractMethod ExtractMethod}.
 */
public class ExtractMethodTest extends RefactoringTestCase {
  private static final boolean POSTPONED = false;

  /** Logger instance. */
  private static final Category cat =
      Category.getInstance(ExtractMethodTest.class.getName());

  public ExtractMethodTest(String name) {
    super(name);
    ExtractMethodAnalyzer.showDebugMessages = false; // to be sure
    ExtractMethod.generateJavadocTemplate = false;
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite(ExtractMethodTest.class);
    suite.setName("Extract Method tests");
    return suite;
  }

  protected void setUp() throws Exception {
    FormatSettings.applyStyle(new FormatSettings.AqrisStyle());
  }

  public String getTemplate() {
    return "ExtractMethod/Imported/<extra_name>/A_<test_name>.java";
  }

  /**
   * Gets Extract Method project defined in projects.xml.
   *
   * @param name name of the project.
   * @return project. Never returns <code>null</code>.
   */
  private Project getProjectIn(String name) throws Exception {
    return Utils.createTestRbProjectFromXml("ExtractMethod_" + name + "_in");
  }

  /**
   * Gets Extract Method project defined in projects.xml.
   *
   * @param name name of the project.
   * @return project. Never returns <code>null</code>.
   */
  private Project getProjectOut(String name) throws Exception {
    return Utils.createTestRbProjectFromXml("ExtractMethod_" + name + "_out");
  }

  /**
   * Tests extracting method, takes project defined in projects.xml file.
   *
   * @param projectName of the project to test. <code>/in</code> is
   *        appended to the name to get name of "in" project.
   *        <code>/out</code> is appended to get name of "out" project.
   */
  private void extractMethod(String projectName) throws Exception {
    extractMethod(getProjectIn(projectName), getProjectOut(projectName),
        "newmethod", BinModifier.PACKAGE_PRIVATE);
  }

  /**
   * Tests extracting method.
   *
   * @param projectIn original project
   * @param projectOut project to compare to
   * @param newMethodName name of the method to be created
   */
  private void extractMethod(Project projectIn, Project projectOut,
      String newMethodName, int modifier) throws Exception {

    final Project project = getMutableProject(projectIn);

    renameToOut(project);

    BinSelection selection = BinSelectionFinder.findSelectionIn(project);
    cat.debug("Extracting: " + selection);

    ExtractMethod extractor
        = new ExtractMethod(new NullContext(project), selection);
    extractor.setMethodName(newMethodName);
    extractor.setModifier(modifier);

    RefactoringStatus status = extractor.checkPreconditions();
    assertTrue("Allows extract: "
        + status.getAllMessages()
        + "\n <SEL>"
        + StringUtil.printableLinebreaks(selection.getText())
        + "</SEL>", status.isOk());

    status = extractor.checkUserInput();
    assertTrue("Checked user input: "
        + status.getAllMessages()
        + "\n <SEL>"
        + StringUtil.printableLinebreaks(selection.getText())
        + "</SEL>", status.isOk());

    status = extractor.apply();

    assertTrue("Failed: " + status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources(
        "Extracted selection", projectOut, project);
  }

  private void definedProjectTest() throws Exception {
    String testName = getName();
    if (testName.startsWith("test")) {
      testName = testName.substring(4);
    }

    cat.info("Testing " + testName);
    extractMethod(testName);
    cat.info("SUCCESS");
  }

  private void importedProjectTest(String name) throws Exception {
    String testName = getName();
    if (testName.startsWith("test")) {
      testName = testName.substring(4);
    }

    cat.info("Testing " + name + "/" + testName);
    extractMethod(getInitialProject(name + "_in"),
        getExpectedProject(name + "_out"),
        "extracted", BinModifier.PROTECTED);
    cat.info("SUCCESS");
  }

  private void selectionTest(int startLine, int startColumn,
      int endLine, int endColumn) throws Exception {
    cat.info("Testing selection/" + getName());

    Project project = getInitialProject("selection");
    project.getProjectLoader().build();
    BinSelection selection = BinSelectionFinder.findSelectionIn(project);
    assertEquals("Selection start line",
        startLine, selection.getStartSourceCoordinate().getLine());
    assertEquals("Selection start column",
        startColumn, selection.getStartSourceCoordinate().getColumn());
    assertEquals("Selection end line",
        endLine, selection.getEndSourceCoordinate().getLine());
    assertEquals("Selection end column",
        endColumn, selection.getEndSourceCoordinate().getColumn());

    cat.info("SUCCESS");
  }

  private void dangerousSelectionTest() throws Exception {
    dangerousSelectionTest(getInitialProject("dangerousSelection"));
  }

  private void dangerousSelectionTest(final Project project) throws Exception {
    cat.info("Testing invalidSelection/" + getName());

    BinSelection selection = BinSelectionFinder.findSelectionIn(project);
    cat.debug("Checking: " + selection);

    ExtractMethod extractor
        = new ExtractMethod(new NullContext(project), selection);
    extractor.setMethodName("newmethod");
    extractor.setModifier(BinModifier.PACKAGE_PRIVATE);

    assertTrue("Selection should be dangerous\n" + "<SEL>"
        + StringUtil.printableLinebreaks(selection.getText())
        + "</SEL>",
        extractor.checkPreconditions().isInfoOrWarning());
    cat.info("SUCCESS");
  }

  private void invalidSelectionTest() throws Exception {
    invalidSelectionTest(getInitialProject("invalidSelection"));
  }

  private void invalidSelectionTest(final Project project) throws Exception {
    cat.info("Testing invalidSelection/" + getName());

    BinSelection selection = BinSelectionFinder.findSelectionIn(project);
    cat.debug("Checking: " + selection);

    ExtractMethod extractor
        = new ExtractMethod(new NullContext(project), selection);
    extractor.setMethodName("newmethod");
    extractor.setModifier(BinModifier.PACKAGE_PRIVATE);

    assertTrue("Selection should be invalid\n" + "<SEL>"
        + StringUtil.printableLinebreaks(selection.getText())
        + "</SEL>", extractor.checkPreconditions().isErrorOrFatal());
    cat.info("SUCCESS");
  }

  private void validSelectionTest() throws Exception {
    validSelectionTest(getInitialProject("validSelection"));
  }

  private void validSelectionTest(Project project) throws Exception {
    cat.info("Testing validSelection/" + getName());

    BinSelection selection = BinSelectionFinder.findSelectionIn(project);
    cat.debug("Checking: " + selection);

    ExtractMethod extractor
        = new ExtractMethod(new NullContext(project), selection);
    extractor.setMethodName("newmethod");
    extractor.setModifier(BinModifier.PACKAGE_PRIVATE);

    assertTrue("Selection should be valid\n" + "<SEL>"
        + StringUtil.printableLinebreaks(selection.getText())
        + "</SEL>",
        extractor.checkPreconditions().isOk());
    cat.info("SUCCESS");
  }

  private void validSelectionTestChecked() throws Exception {
    importedProjectTest("validSelection");
  }

  private void semicolonTest() throws Exception {
    importedProjectTest("semicolon");
  }

  private void tryTest() throws Exception {
    importedProjectTest("try");
  }

  private void localsTest() throws Exception {
    importedProjectTest("locals");
  }

  private void expressionTest() throws Exception {
    importedProjectTest("expression");
  }

  private void nestedTest() throws Exception {
    importedProjectTest("nested");
  }

  private void returnTest() throws Exception {
    importedProjectTest("return");
  }

  private void branchTest() throws Exception {
    importedProjectTest("branch");
  }

  protected void duplicatesTest() throws Exception {
    importedProjectTest("duplicates");
  }

  protected void initializerTest() throws Exception {
    importedProjectTest("initializer");
  }

//  private void errorTest() throws Exception {
//    importedProjectTest("error");
//  }

  //
  // TESTS
  //

  /**
   * Tests benchmark1 from
   * http://c2.com/cgi/wiki?RefactoringBenchmarksForExtractMethod.
   */
  public void testBenchmark1() throws Exception {
    definedProjectTest();
  }

  /**
   * Tests benchmark2 from
   * http://c2.com/cgi/wiki?RefactoringBenchmarksForExtractMethod.
   */
  public void testBenchmark2() throws Exception {
    definedProjectTest();
  }

  /**
   * Tests benchmark3 from
   * http://c2.com/cgi/wiki?RefactoringBenchmarksForExtractMethod.
   */
  public void testBenchmark3() throws Exception {
    definedProjectTest();
  }

  /**
   * Tests benchmark4 from
   * http://c2.com/cgi/wiki?RefactoringBenchmarksForExtractMethod.
   */
  public void testBenchmark4() throws Exception {
    definedProjectTest();
  }

  /**
   * Tests benchmark5 from
   * http://c2.com/cgi/wiki?RefactoringBenchmarksForExtractMethod.
   */
  public void testBenchmark5() throws Exception {
    definedProjectTest();
  }

  public void testExtractExpression1() throws Exception {
    definedProjectTest();
  }

  public void testBug1317() throws Exception {
    definedProjectTest();
  }

  public void testBug1401() throws Exception {
    definedProjectTest();
  }

  public void testBug1402() throws Exception {
    definedProjectTest();
  }

  public void testBug1450() throws Exception {
    definedProjectTest();
  }

  /**
   * Tests extracting variable declared within loop
   */
  public void testVarInLoop() throws Exception {
    definedProjectTest();
  }

  /**
   * Tests extracting variable declared within loop, but used later within the
   * loop also
   */
  public void testVarInLoopReturn() throws Exception {
    definedProjectTest();
  }

  /**
   * Tests extracting new expression which constructor may throw an exception
   */
  public void testExceptionByNew() throws Exception {
    definedProjectTest();
  }

  /**
   * Generic test for vars and exception used in FQN and thus don't need
   * importing
   */
  public void testFqnUsage() throws Exception {
    definedProjectTest();
  }

  /**
   * Generic test inner classes
   */
  public void testInnerClassesInGenerics() throws Exception {
    int oldJvmMode = Project.getDefaultOptions().getJvmMode();
    Project.getDefaultOptions().setJvmMode(FastJavaLexer.JVM_50);
    definedProjectTest();
    Project.getDefaultOptions().setJvmMode(oldJvmMode);
  }

  /**
   * Tests renaming a parameter used in several places within the block
   */
  public void testParamRename() throws Exception {
    paramRenamingExtract(new int[] {0}
        , new String[] {"testItem"});
  }

  private void paramRenamingExtract(
      final int[] newParamIds, final String[] newParamNames) throws Exception {

    String testName = getName();
    if (testName.startsWith("test")) {
      testName = testName.substring(4);
    }
    cat.info("Testing " + testName);

    final Project project = getMutableProject(getProjectIn(testName));

    BinSelection selection = BinSelectionFinder.findSelectionIn(project);
    cat.debug("Extracting: " + selection);

    ExtractMethod extractor
        = new ExtractMethod(new NullContext(project), selection);
    extractor.setMethodName("newmethod");
    extractor.setModifier(BinModifier.PACKAGE_PRIVATE);

    extractor.setNewParameterIds(newParamIds);
    extractor.setNewParameterNames(newParamNames);

    RefactoringStatus status = extractor.checkPreconditions();
    assertTrue("Allows extract: "
        + status.getAllMessages()
        + "\n <SEL>"
        + StringUtil.printableLinebreaks(selection.getText())
        + "</SEL>", status.isOk());

    status = extractor.apply();

    assertTrue("Extracted successfully", status.isOk());

    RwRefactoringTestUtils.assertSameSources(
        "Extracted selection", getProjectOut(testName), project);
    cat.info("SUCCESS");
  }

  public void testBug1529() throws Exception {
    validSelectionTest(
        Utils.createTestRbProject(Utils.getTestProjects().getProject(
        "ExtractMethod_Bug1529")));
  }

  public void testBug1529_inside_expression() throws Exception {
    validSelectionTest(
        Utils.createTestRbProject(Utils.getTestProjects().getProject(
        "ExtractMethod_Bug1529_inside_expression")));
  }

  public void testBug1529_inside_method_call() throws Exception {
    validSelectionTest(
        Utils.createTestRbProject(Utils.getTestProjects().getProject(
        "ExtractMethod_Bug1529_inside_method_call")));
  }

  public void testBug1619StringLiteral() throws Exception {
    validSelectionTest(
        Utils.createTestRbProject(Utils.getTestProjects().getProject(
        "ExtractMethod_Bug1619_StringLiteral")));
  }

  public void testBug1619CharLiteral() throws Exception {
    validSelectionTest(
        Utils.createTestRbProject(Utils.getTestProjects().getProject(
        "ExtractMethod_Bug1619_CharLiteral")));
  }

  public void testBug1619BothLiterals() throws Exception {
    validSelectionTest(
        Utils.createTestRbProject(Utils.getTestProjects().getProject(
        "ExtractMethod_Bug1619_BothLiterals")));
  }

  /**
   * Var defined within loop and used later in the loop.
   */
  public void testBug1581() throws Exception {
    definedProjectTest();
  }

  /**
   * Var defined within if not returned.
   */
  public void testBug1606() throws Exception {
    definedProjectTest();
  }

  /**
   * Final var turnes into non-final on return from new method.
   */
  public void testBug1607() throws Exception {
    definedProjectTest();
  }

  public void testBug1634() throws Exception {
    validSelectionTest(
        Utils.createTestRbProject(Utils.getTestProjects().getProject(
        "ExtractMethod_Bug1634")));
  }

  public void testBug1634_2() throws Exception {
    validSelectionTest(
        Utils.createTestRbProject(Utils.getTestProjects().getProject(
        "ExtractMethod_Bug1634_2")));
  }

  public void testBug1667() throws Exception {
    // FIXME it fails here, but works manually, real magic :(
    System.out.println(getClass().getName() + "::" + getName() + " disabled");
    //definedProjectTest();
  }

  public void testBug1871() throws Exception {
    paramRenamingExtract(new int[] {1, 0}
        , new String[] {"x", "param"});
  }

  public void testBug1787() throws Exception {
    definedProjectTest();
  }

  public void testBug1938() throws Exception {
    definedProjectTest();
  }

  public void testBug2016() throws Exception {
    definedProjectTest();
  }

  public void testBug1904() throws Exception {
    // FIXME it fails here, since defining selection with comments breaks
    // analysis with partly selected expression
    System.out.println(getClass().getName() + "::" + getName() + " disabled");
    //definedProjectTest();
  }

  public void testBug2139() throws Exception {
    invalidSelectionTest(
        Utils.createTestRbProject(Utils.getTestProjects().getProject(
        "ExtractMethod_Bug2139")));
  }

  public void testIssue238() throws Exception {
    invalidSelectionTest(
        Utils.createTestRbProject(Utils.getTestProjects().getProject(
        "ExtractMethod_Issue238")));
  }

  public void testBug2255() throws Exception {
    definedProjectTest();
  }

  public void testExtractInAnonymous() throws Exception {
    definedProjectTest();
  }

  public void testTypeParams1() throws Exception {
    int oldJvm = Project.getDefaultOptions().getJvmMode();
    Project.getDefaultOptions().setJvmMode(FastJavaLexer.JVM_50);
    try {
      definedProjectTest();
    } finally {
      Project.getDefaultOptions().setJvmMode(oldJvm);
    }
  }

  //
  // Imported tests
  //

  //=====================================================================================
  // Testing selections
  //=====================================================================================

  public void test1() throws Exception {
    selectionTest(6, 5, 6, 20);
  }

  public void test2() throws Exception {
    selectionTest(6, 5, 6, 15);
  }

  public void test3() throws Exception {
    selectionTest(6, 10, 6, 20);
  }

  public void test4() throws Exception {
    selectionTest(6, 10, 6, 15);
  }

  //=====================================================================================
  // Testing invalid selections
  //=====================================================================================

  //---- Misc

  public void test010() throws Exception {
    invalidSelectionTest();
  }

  public void test011() throws Exception {
    invalidSelectionTest();
  }

  public void test012() throws Exception {
    invalidSelectionTest();
  }

  public void test013() throws Exception {
    invalidSelectionTest();
  }

  public void test014() throws Exception {
    invalidSelectionTest();
  }

  //---- Switch / Case

  public void test020() throws Exception {
    invalidSelectionTest();
  }

  public void test021() throws Exception {
    invalidSelectionTest();
  }

  public void test022() throws Exception {
    invalidSelectionTest();
  }

  //---- Block

  public void test030() throws Exception {
    invalidSelectionTest();
  }

  public void test031() throws Exception {
    invalidSelectionTest();
  }

  //---- For

  public void test040() throws Exception {
    invalidSelectionTest();
  }

  public void test041() throws Exception {
    if (POSTPONED) {
      invalidSelectionTest();
    }
  }

  public void test042() throws Exception {
    if (POSTPONED) {
      invalidSelectionTest();
    }
  }

  public void test043() throws Exception {
    invalidSelectionTest();
  }

  public void test044() throws Exception {
    invalidSelectionTest();
  }

  public void test045() throws Exception {
    invalidSelectionTest();
  }

  public void test046() throws Exception {
    invalidSelectionTest();
  }

  public void test047() throws Exception {
    invalidSelectionTest();
  }

  public void test048() throws Exception {
    if (POSTPONED) {
      invalidSelectionTest();
    }
  }

  //---- While

  public void test050() throws Exception {
    invalidSelectionTest();
  }

  public void test051() throws Exception {
    invalidSelectionTest();
  }

  public void test052() throws Exception {
    invalidSelectionTest();
  }

  //---- do / While

  public void test060() throws Exception {
    invalidSelectionTest();
  }

  public void test061() throws Exception {
    invalidSelectionTest();
  }

  public void test062() throws Exception {
    invalidSelectionTest();
  }

// NOTE: possible bug in test, we managed to make it work fine :)
  /*  public void test063() throws Exception {
      invalidSelectionTest();
    }*/

  //---- switch

  public void test070() throws Exception {
    invalidSelectionTest();
  }

  public void test071() throws Exception {
    invalidSelectionTest();
  }

  public void test072() throws Exception {
    invalidSelectionTest();
  }

  public void test073() throws Exception {
    invalidSelectionTest();
  }

  //---- if then else

  public void test080() throws Exception {
    invalidSelectionTest();
  }

  public void test081() throws Exception {
    invalidSelectionTest();
  }

  public void test082() throws Exception {
    invalidSelectionTest();
  }

  public void test083() throws Exception {
    invalidSelectionTest();
  }

  public void test084() throws Exception {
    invalidSelectionTest();
  }

  public void test085() throws Exception {
    if (POSTPONED) {
      invalidSelectionTest();
    }
  }

  //---- Break

  public void test090() throws Exception {
    invalidSelectionTest();
  }

  public void test091() throws Exception {
    invalidSelectionTest();
  }

  public void test092() throws Exception {
    invalidSelectionTest();
  }

  public void test093() throws Exception {
    invalidSelectionTest();
  }

  public void test094() throws Exception {
    invalidSelectionTest();
  }

  public void test095() throws Exception {
    invalidSelectionTest();
  }

  public void test096() throws Exception {
    invalidSelectionTest();
  }

  //---- Try / catch / finally

  public void test100() throws Exception {
    invalidSelectionTest();
  }

  public void test101() throws Exception {
    invalidSelectionTest();
  }

  public void test102() throws Exception {
    invalidSelectionTest();
  }

  public void test103() throws Exception {
    invalidSelectionTest();
  }

  public void test104() throws Exception {
    invalidSelectionTest();
  }

  public void test105() throws Exception {
    invalidSelectionTest();
  }

  public void test106() throws Exception {
    invalidSelectionTest();
  }

  public void test107() throws Exception {
    invalidSelectionTest();
  }

  public void test108() throws Exception {
    invalidSelectionTest();
  }

  public void test109() throws Exception {
    invalidSelectionTest();
  }

  public void test110() throws Exception {
    invalidSelectionTest();
  }

  public void test111() throws Exception {
    invalidSelectionTest();
  }

  public void test112() throws Exception {
    invalidSelectionTest();
  }

  public void test113() throws Exception {
    invalidSelectionTest();
  }

  public void test114() throws Exception {
    invalidSelectionTest();
  }

  public void test115() throws Exception {
    invalidSelectionTest();
  }

  //---- invalid local var selection

  public void test120() throws Exception {
    invalidSelectionTest();
  }

  public void test121() throws Exception {
    invalidSelectionTest();
  }

  public void test122() throws Exception {
    invalidSelectionTest();
  }

  public void test123() throws Exception {
    invalidSelectionTest();
  }

  //---- invalid local type selection

  public void test130() throws Exception {
    invalidSelectionTest();
  }

  public void test131() throws Exception {
    invalidSelectionTest();
  }

  //---- invalid return statement selection

  public void test140() throws Exception {
    invalidSelectionTest();
  }

  public void test141() throws Exception {
    invalidSelectionTest();
  }

  public void test142() throws Exception {
    invalidSelectionTest();
  }

  public void test143() throws Exception {
    invalidSelectionTest();
  }

  public void test144() throws Exception {
    invalidSelectionTest();
  }

  public void test145() throws Exception {
    invalidSelectionTest();
  }

  public void test146() throws Exception {
    invalidSelectionTest();
  }

  public void test147() throws Exception {
    invalidSelectionTest();
  }

  public void test148() throws Exception {
    invalidSelectionTest();
  }

  public void test149() throws Exception {
    invalidSelectionTest();
  }

  //---- Synchronized statement

  public void test150() throws Exception {
    invalidSelectionTest();
  }

  public void test151() throws Exception {
    invalidSelectionTest();
  }

  public void test152() throws Exception {
    invalidSelectionTest();
  }

  public void test153() throws Exception {
    invalidSelectionTest();
  }

  public void test160() throws Exception {
    if (POSTPONED) {
      invalidSelectionTest();
    }
  }

  public void test161() throws Exception {
    invalidSelectionTest();
  }

  //----- local declarations

  public void test170() throws Exception {
    if (POSTPONED) {
      invalidSelectionTest();
    }
  }

  public void test171() throws Exception {
    if (POSTPONED) {
      invalidSelectionTest();
    }
  }

  public void test172() throws Exception {
    invalidSelectionTest();
  }

  public void test173() throws Exception {
    if (RefactorItConstants.runNotImplementedTests) {
      invalidSelectionTest();
    }
  }

  //---- Constructor

  public void test180() throws Exception {
    System.out.println(getClass().getName() + "::" + getName() + " disabled");
    invalidSelectionTest();
  }

  public void test181() throws Exception {
    System.out.println(getClass().getName() + "::" + getName() + " disabled");
    invalidSelectionTest();
  }

  //---- More return statement handling

  public void test190() throws Exception {
    invalidSelectionTest();
  }

  public void test191() throws Exception {
    invalidSelectionTest();
  }

  public void test192() throws Exception {
    dangerousSelectionTest();
  }

  public void test193() throws Exception {
    invalidSelectionTest();
  }

  //====================================================================================
  // Testing valid selections
  //=====================================================================================

  //---- Misc

  public void test200() throws Exception {
    validSelectionTest();
  }

  public void test201() throws Exception {
    validSelectionTest();
  }

  public void test202() throws Exception {
    validSelectionTest();
  }

  public void test203() throws Exception {
    validSelectionTest();
  }

  //---- Block

  public void test230() throws Exception {
    validSelectionTest();
  }

  public void test231() throws Exception {
    validSelectionTest();
  }

  public void test232() throws Exception {
    validSelectionTest();
  }

  public void test233() throws Exception {
    validSelectionTest();
  }

  public void test234() throws Exception {
    validSelectionTest();
  }

  public void test235() throws Exception {
    validSelectionTest();
  }

  //---- For statement

  public void test240() throws Exception {
    validSelectionTest();
  }

  public void test241() throws Exception {
    validSelectionTest();
  }

  public void test244() throws Exception {
    if (POSTPONED) {
      validSelectionTest();
    }
  }

  public void test245() throws Exception {
    validSelectionTest();
  }

  public void test246() throws Exception {
    validSelectionTest();
  }

  public void test247() throws Exception {
    if (POSTPONED) {
      validSelectionTest();
    }
  }

  public void test248() throws Exception {
    validSelectionTest();
  }

  public void test249() throws Exception {
    validSelectionTest();
  }

  //---- While statement

  public void test250() throws Exception {
    if (POSTPONED) {
      validSelectionTest();
    }
  }

  public void test251() throws Exception {
    validSelectionTest();
  }

  public void test252() throws Exception {
    if (POSTPONED) {
      validSelectionTest();
    }
  }

  public void test253() throws Exception {
    if (POSTPONED) {
      validSelectionTest();
    }
  }

  public void test254() throws Exception {
    validSelectionTest();
  }

  public void test255() throws Exception {
    validSelectionTest();
  }

  //---- do while statement

  public void test260() throws Exception {
    validSelectionTest();
  }

  public void test261() throws Exception {
    validSelectionTest();
  }

  public void test262() throws Exception {
    validSelectionTest();
  }

  public void test263() throws Exception {
    validSelectionTest();
  }

  //---- switch

  public void test270() throws Exception {
    validSelectionTest();
  }

  public void test271() throws Exception {
    validSelectionTest();
  }

  public void test272() throws Exception {
    validSelectionTest();
  }

  public void test273() throws Exception {
    validSelectionTest();
  }

  public void test274() throws Exception {
    validSelectionTest();
  }

  public void test275() throws Exception {
    validSelectionTest();
  }

  //---- if then else

  public void test280() throws Exception {
    validSelectionTest();
  }

  public void test281() throws Exception {
    if (POSTPONED) {
      validSelectionTest();
    }
  }

  public void test282() throws Exception {
    validSelectionTest();
  }

  public void test283() throws Exception {
    validSelectionTest();
  }

  public void test284() throws Exception {
    validSelectionTest();
  }

  public void test285() throws Exception {
    if (POSTPONED) {
      validSelectionTest();
    }
  }

  //---- try / catch / finally

  public void test300() throws Exception {
    validSelectionTest();
  }

  public void test301() throws Exception {
    validSelectionTest();
  }

  public void test302() throws Exception {
    validSelectionTest();
  }

  public void test304() throws Exception {
    validSelectionTest();
  }

  public void test305() throws Exception {
    validSelectionTest();
  }

  public void test306() throws Exception {
    validSelectionTest();
  }

  public void test307() throws Exception {
    validSelectionTest();
  }

  public void test308() throws Exception {
    validSelectionTest();
  }

  public void test309() throws Exception {
    validSelectionTest();
  }

  public void test310() throws Exception {
    validSelectionTest();
  }

  public void test311() throws Exception {
    validSelectionTest();
  }

  //---- Synchronized statement

  public void test350() throws Exception {
    validSelectionTest();
  }

  public void test351() throws Exception {
    validSelectionTest();
  }

  public void test352() throws Exception {
    validSelectionTest();
  }

  public void test353() throws Exception {
    validSelectionTest();
  }

  public void test360() throws Exception {
    validSelectionTestChecked();
  }

  public void test361() throws Exception {
    validSelectionTestChecked();
  }

  public void test362() throws Exception {
    validSelectionTestChecked();
  }

  public void test363() throws Exception {
    int oldJvm = Project.getDefaultOptions().getJvmMode();
    Project.getDefaultOptions().setJvmMode(FastJavaLexer.JVM_14);
    try {
      validSelectionTestChecked();
    } finally {
      Project.getDefaultOptions().setJvmMode(oldJvm);
    }
  }

  public void test364() throws Exception {
    validSelectionTestChecked();
  }

  public void test365() throws Exception {
    validSelectionTestChecked();
  }

  //====================================================================================
  // Testing Extracted result
  //====================================================================================

  //---- Test semicolon

  public void test400() throws Exception {
    semicolonTest();
  }

  public void test401() throws Exception {
    semicolonTest();
  }

  public void test402() throws Exception {
    semicolonTest();
  }

  public void test403() throws Exception {
    semicolonTest();
  }

  public void test404() throws Exception {
    semicolonTest();
  }

  public void test406() throws Exception {
    semicolonTest();
  }

  public void test407() throws Exception {
    semicolonTest();
  }

  public void test409() throws Exception {
    semicolonTest();
  }

  //---- Test Try / catch block

  public void test450() throws Exception {
    tryTest();
  }

  public void test451() throws Exception {
    tryTest();
  }

  public void test452() throws Exception {
    tryTest();
  }

  public void test453() throws Exception {
    tryTest();
  }

  public void test454() throws Exception {
    tryTest();
  }

  public void test455() throws Exception {
    tryTest();
  }

  public void test456() throws Exception {
    tryTest();
  }

  public void test457() throws Exception {
    tryTest();
  }

  public void test458() throws Exception {
    tryTest();
  }

  public void test459() throws Exception {
    tryTest();
  }

  public void test460() throws Exception {
    tryTest();
  }

  public void test461() throws Exception {
    tryTest();
  }

  public void test462() throws Exception {
    tryTest();
  }

  //---- Test local vars and types

  public void test500() throws Exception {
    localsTest();
  }

  public void test501() throws Exception {
    localsTest();
  }

  public void test502() throws Exception {
    localsTest();
  }

  public void test503() throws Exception {
    localsTest();
  }

  public void test504() throws Exception {
    localsTest();
  }

  public void test505() throws Exception {
    localsTest();
  }

  public void test506() throws Exception {
    localsTest();
  }

  public void test507() throws Exception {
    localsTest();
  }

  public void test508() throws Exception {
    localsTest();
  }

  public void test509() throws Exception {
    localsTest();
  }

  public void test510() throws Exception {
    localsTest();
  }

  public void test511() throws Exception {
    localsTest();
  }

  public void test512() throws Exception {
    localsTest();
  }

  public void test513() throws Exception {
    localsTest();
  }

  public void test514() throws Exception {
    localsTest();
  }

  public void test515() throws Exception {
    localsTest();
  }

  public void test516() throws Exception {
    if (POSTPONED) {
      localsTest();
    }
  }

  public void test517() throws Exception {
    localsTest();
  }

  public void test518() throws Exception {
    localsTest();
  }

  public void test519() throws Exception {
    localsTest();
  }

  public void test520() throws Exception {
    localsTest();
  }

  public void test521() throws Exception {
    localsTest();
  }

  public void test522() throws Exception {
    localsTest();
  }

  public void test523() throws Exception {
    localsTest();
  }

  public void test524() throws Exception {
    localsTest();
  }

  public void test525() throws Exception {
    localsTest();
  }

  public void test526() throws Exception {
    localsTest();
  }

  public void test527() throws Exception {
    localsTest();
  }

  public void test528() throws Exception {
    localsTest();
  }

  public void test530() throws Exception {
    localsTest();
  }

  public void test531() throws Exception {
    localsTest();
  }

  public void test532() throws Exception {
    localsTest();
  }

  public void test533() throws Exception {
    localsTest();
  }

  public void test534() throws Exception {
    localsTest();
  }

  public void test535() throws Exception {
    localsTest();
  }

  public void test536() throws Exception {
    localsTest();
  }

  public void test537() throws Exception {
    localsTest();
  }

  public void test538() throws Exception {
    localsTest();
  }

  public void test539() throws Exception {
    localsTest();
  }

  public void test540() throws Exception {
    localsTest();
  }

  public void test541() throws Exception {
    localsTest();
  }

  public void test542() throws Exception {
    localsTest();
  }

  public void test543() throws Exception {
    localsTest();
  }

  public void test550() throws Exception {
    localsTest();
  }

  public void test551() throws Exception {
    localsTest();
  }

  public void test552() throws Exception {
    localsTest();
  }

  public void test553() throws Exception {
    localsTest();
  }

  public void test554() throws Exception {
    localsTest();
  }

  public void test555() throws Exception {
    localsTest();
  }

  public void test556() throws Exception {
    localsTest();
  }

  public void test557() throws Exception {
    localsTest();
  }

  public void test558() throws Exception {
    localsTest();
  }

  public void test559() throws Exception {
    localsTest();
  }

  public void test560() throws Exception {
    localsTest();
  }

  public void test561() throws Exception {
    localsTest();
  }

  public void test562() throws Exception {
    localsTest();
  }

  public void test563() throws Exception {
    localsTest();
  }

  public void test564() throws Exception {
    localsTest();
  }

  public void test565() throws Exception {
    localsTest();
  }

  public void test566() throws Exception {
    localsTest();
  }

  public void test567() throws Exception {
    localsTest();
  }

  public void test568() throws Exception {
    localsTest();
  }

  public void test569() throws Exception {
    localsTest();
  }

  //---- Test expressions

  public void test600() throws Exception {
    expressionTest();
  }

  public void test601() throws Exception {
    expressionTest();
  }

  public void test602() throws Exception {
    expressionTest();
  }

  public void test603() throws Exception {
    expressionTest();
  }

  public void test604() throws Exception {
    expressionTest();
  }

  public void test605() throws Exception {
    expressionTest();
  }

  public void test606() throws Exception {
    expressionTest();
  }

  public void test607() throws Exception {
    expressionTest();
  }

  public void test608() throws Exception {
    expressionTest();
  }

  public void test609() throws Exception {
    expressionTest();
  }

  public void test610() throws Exception {
    expressionTest();
  }

  public void test611() throws Exception {
    expressionTest();
  }

  public void test612() throws Exception {
    expressionTest();
  }

  public void test613() throws Exception {
    if (POSTPONED) {
      expressionTest();
    }
  }

  public void test614() throws Exception {
    if (POSTPONED) {
      expressionTest();
    }
  }

  public void test615() throws Exception {
    expressionTest();
  }

  public void test616() throws Exception {
    expressionTest();
  }

  public void test617() throws Exception {
    expressionTest();
  }

  public void test618() throws Exception {
    expressionTest();
  }

  public void test619() throws Exception {
    expressionTest();
  }

  public void test620() throws Exception {
    definedProjectTest(); // contains 2 files
  }

  public void test621() throws Exception {
    expressionTest();
  }

  public void test622() throws Exception {
    expressionTest();
  }

  //---- Test nested methods and constructor

  public void test650() throws Exception {
    nestedTest();
  }

  public void test651() throws Exception {
    nestedTest();
  }

  public void test652() throws Exception {
    nestedTest();
  }

  public void test653() throws Exception {
    nestedTest();
  }

  public void test654() throws Exception {
    nestedTest();
  }

  //---- Extracting method containing a return statement.

  public void test700() throws Exception {
    returnTest();
  }

  public void test701() throws Exception {
    returnTest();
  }

  public void test702() throws Exception {
    returnTest();
  }

  public void test703() throws Exception {
    returnTest();
  }

  public void test704() throws Exception {
    if (POSTPONED) {
      returnTest();
    }
  }

  public void test705() throws Exception {
    returnTest();
  }

  public void test706() throws Exception {
    returnTest();
  }

  public void test707() throws Exception {
    returnTest();
  }

  public void test708() throws Exception {
    if (POSTPONED) {
      returnTest();
    }
  }

  public void test709() throws Exception {
    returnTest();
  }

  public void test710() throws Exception {
    returnTest();
  }

  public void test711() throws Exception {
    returnTest();
  }

  public void test712() throws Exception {
    returnTest();
  }

  public void test713() throws Exception {
    if (POSTPONED) {
      returnTest();
    }
  }

  public void test714() throws Exception {
    returnTest();
  }

  public void test715() throws Exception {
    returnTest();
  }

  public void test716() throws Exception {
    returnTest();
  }

  public void test717() throws Exception {
    returnTest();
  }

  public void test718() throws Exception {
    returnTest();
  }

  public void test719() throws Exception {
    returnTest();
  }

  public void test720() throws Exception {
    returnTest();
  }

  public void test721() throws Exception {
    returnTest();
  }

  public void test722() throws Exception {
    returnTest();
  }

  public void test723() throws Exception {
    returnTest();
  }

  public void test724() throws Exception {
    returnTest();
  }

  public void test725() throws Exception {
    returnTest();
  }

  public void test726() throws Exception {
    returnTest();
  }

  public void test727() throws Exception {
    if (!RefactorItConstants.runNotImplementedTests) {return;
    }
    returnTest();
  }

  public void test728() throws Exception {
    if (!RefactorItConstants.runNotImplementedTests) {return;
    }
    returnTest();
  }

  //---- Branch statements

  public void test750() throws Exception {
    branchTest();
  }

  public void test751() throws Exception {
    branchTest();
  }

  public void test752() throws Exception {
    branchTest();
  }

  public void test753() throws Exception {
    branchTest();
  }

  public void test754() throws Exception {
    branchTest();
  }

  public void test755() throws Exception {
    branchTest();
  }

  //---- Test for projects with compiler errors

// 20020425: We don't support extracting of error blocks at the moment
  /*
    public void test800() throws Exception {
      errorTest();
    }

    public void test801() throws Exception {
      errorTest();
    }

    public void test802() throws Exception {
      errorTest();
    }*/



  //---- Test for projects with compiler errors

// 20020425: We don't support extracting of error blocks at the moment
  /*
    public void test800() throws Exception {
      errorTest();
    }

    public void test801() throws Exception {
      errorTest();
    }

    public void test802() throws Exception {
      errorTest();
    }*/

  //---- Test parameter name changes -------------------------------------------

//  private void invalidParameterNameTest(String[] newNames) throws Exception {
//    performTest(fgTestSetup.getParameterNamePackage(), "A", INVALID_SELECTION,
//        null,
//        newNames,
//        null);
//  }
//
//  private void parameterNameTest(String[] newNames,
//      int[] newOrder) throws Exception {
//    performTest(fgTestSetup.getParameterNamePackage(), "A", COMPARE_WITH_OUTPUT,
//        "parameterName_out", newNames, newOrder);
//  }
//
//  public void test900() throws Exception {
//    invalidParameterNameTest(new String[] {"y"});
//  }
//
//  public void test901() throws Exception {
//    invalidParameterNameTest(new String[] {null, "i"});
//  }
//
//  public void test902() throws Exception {
//    invalidParameterNameTest(new String[] {"System"});
//  }
//
//  public void test903() throws Exception {
//    parameterNameTest(new String[] {"xxx", "yyyy"}
//        , null);
//  }
//
//  public void test904() throws Exception {
//    parameterNameTest(new String[] {"xx", "zz"}
//        , new int[] {1, 0});
//  }
//
//  public void test905() throws Exception {
//    parameterNameTest(new String[] {"message"}
//        , null);
//  }
//
//  public void test906() throws Exception {
//    parameterNameTest(new String[] {"xxx"}
//        , null);
//  }

  //---- Test duplicate code snippets ------------------------------------------

  public void test950() throws Exception {
    if (!RefactorItConstants.runNotImplementedTests) {return;
    }
    duplicatesTest();
  }

  public void test951() throws Exception {
    if (!RefactorItConstants.runNotImplementedTests) {return;
    }
    duplicatesTest();
  }

  public void test952() throws Exception {
    if (!RefactorItConstants.runNotImplementedTests) {return;
    }
    duplicatesTest();
  }

  public void test953() throws Exception {
    if (!RefactorItConstants.runNotImplementedTests) {return;
    }
    duplicatesTest();
  }

  public void test954() throws Exception {
    if (!RefactorItConstants.runNotImplementedTests) {return;
    }
    duplicatesTest();
  }

  public void test955() throws Exception {
    if (!RefactorItConstants.runNotImplementedTests) {return;
    }
    duplicatesTest();
  }

  public void test956() throws Exception {
    if (!RefactorItConstants.runNotImplementedTests) {return;
    }
    duplicatesTest();
  }

  //---- Test code in initializers ---------------------------------------------

  public void test1000() throws Exception {
    initializerTest();
  }

  public void test1001() throws Exception {
    initializerTest();
  }

  public void test1002() throws Exception {
    initializerTest();
  }

  public void test1003() throws Exception {
    initializerTest();
  }

  //----

  public void testErrorMessagesForReadOnlyFiles() throws Exception {
    LocalSource.fakeReadOnly = true;
    try {
      NullDialogManager d = (NullDialogManager) DialogManager.getInstance();
      d.customErrorString = "";

      try {
        definedProjectTest();
      } catch (AssertionFailedError ignore) {
      }

      assertTrue("Was: <" + d.customErrorString + ">",
          d.customErrorString.indexOf("Can not modify source") >= 0);
    } finally {
      LocalSource.fakeReadOnly = false;
    }
  }
}
