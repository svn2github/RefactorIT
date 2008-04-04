/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings;



import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.parser.FastJavaLexer;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.changesignature.ChangeMethodSignatureRefactoring;
import net.sf.refactorit.refactorings.changesignature.MethodSignatureChange;
import net.sf.refactorit.refactorings.changesignature.NewParameterInfo;
import net.sf.refactorit.refactorings.changesignature.ParameterInfo;
import net.sf.refactorit.refactorings.common.Permutation;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.ui.RuntimePlatform;
import net.sf.refactorit.utils.RefactorItConstants;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author Tonis Vaga
 */
public class ChangeMethodSignatureTest extends RefactoringTestCase {
  public ChangeMethodSignatureTest() {
    super(ChangeMethodSignatureTest.class.getName());
  }

  private static final String REFACTORING_PATH = "ChangeSignature/";

  private static final boolean RUN_CONSTRUCTOR_TEST = true;

  private boolean canModify;

  public ChangeMethodSignatureTest(String name) {
    super(name);
  }

//  public String getTemplate() {
//   return "ChangeSignature/<test_name><in_out>";
// }

  int oldJvmMode = FastJavaLexer.JVM_14;

  protected void setUp() throws Exception {
    FormatSettings.applyStyle(new FormatSettings.AqrisStyle());
    GlobalOptions.setOption(FormatSettings.PROP_FORMAT_TAB_SIZE, Integer.toString(4));
    GlobalOptions.setOption(FormatSettings.PROP_FORMAT_BLOCK_INDENT, Integer.toString(4));
    GlobalOptions.setOption(FormatSettings.PROP_FORMAT_USE_SPACES_IN_PLACE_OF_TABS, "false");
    oldJvmMode = Project.getDefaultOptions().getJvmMode();
    Project.getDefaultOptions().setJvmMode(FastJavaLexer.JVM_50);
  }

  protected void tearDown() throws Exception {
    Project.getDefaultOptions().setJvmMode(oldJvmMode);
  }

  protected String getRefactoringPath() {
    return REFACTORING_PATH;
  }

  public static Test suite() {
    TestSuite suite = new TestSuite(ChangeMethodSignatureTest.class);
    return suite;
  }

  protected Project getProject(boolean canModify,
      boolean input) throws Exception {
    this.canModify = canModify;

    Project result = input ? getExpectedProject() : getMutableProject();
    result.getProjectLoader().build();
    assertTrue(CollectionUtil.toList((result.getProjectLoader().getErrorCollector()).getUserFriendlyErrors()).toString(),
        !(result.getProjectLoader().getErrorCollector()).hasErrors());
    return result;
  }

  private static void addParameters(MethodSignatureChange change,
      NewParameterInfo[] newParamInfos,
      int[] newIndices) {
    if (newParamInfos == null || newIndices == null) {
      return;
    }

    for (int i = newIndices.length - 1; i >= 0; i--) {
      change.addParameter(newParamInfos[i], newIndices[i]);
    }
  }

  private void helperAdd(String[] signature, TestParameterInfo[] newParamInfos,
      int[] newIndices) throws Exception {
    addTest(signature, newParamInfos, newIndices, false, 0);
  }

  private void helperDoAll(String typeName, String methodName,
      String[] signature,
      TestParameterInfo[] newParamInfos, int[] newIndices,
      String[] oldParamNames, String[] newParamNames,
      String[] newParameterTypeNames, int[] permutation,
      int newVisibility, int[] deleted,
      String returnTypeName, boolean fails,
      int expectedSeverity) throws
      Exception {

    Project pr = getProject(!fails, false);

    Project expected = getProject(!fails, true);

    BinMethod method = getMethod(typeName, methodName, signature, pr);

    assertTrue("Return type changing not supported", returnTypeName == null);

    ChangeMethodSignatureRefactoring ref = new ChangeMethodSignatureRefactoring(
        method);

    MethodSignatureChange change = ref.createSingatureChange();
    change.setTestRun(true);
    BinTypeRef[] newTypes = null;

    if (newParameterTypeNames != null) {
      newTypes = extractParameterTypes(newParameterTypeNames, pr);
    }

    renameParameters(change, oldParamNames, newParamNames, newTypes);

    if (deleted != null) {
      change.deleteParameters(deleted);
    }

    if (newParamInfos != null) {
      addParameters(change, createNewParamInfos(newParamInfos, pr), newIndices);
    }

    if (permutation != null) {
      Permutation perm = new Permutation(permutation);
      for (int i = 0; i < perm.size(); i++) {
        if (perm.getIndex(i) < 0) {
          perm.setIndex(i, i);
        }

      }
      change.reorderParameters(perm);
    }

    change.setAccessModifier(newVisibility);

    if (returnTypeName != null) {

      BinTypeRef returnType = extractType(pr, returnTypeName);
      assertTrue("return type == null, " + returnTypeName, returnType != null);
      change.setReturnType(returnType);
    }

    ref.setChange(change);

    if (fails) {
      assertFails(ref, expectedSeverity, pr, expected);
    } else {
      assertRefactoring(pr, expected, ref);
    }

  }

  private void helper1(String[] newOrder, String[] signature, boolean fails,
      int expectedSeverity) throws Exception {
    helper1(newOrder, signature, null, null, fails, expectedSeverity);
  }

  private void helper1(String[] newOrder, String[] signature, String[] oldNames,
      String[] newNames, boolean fails, int expectedSeverity) throws
      Exception {

    Project pr = getProject(!fails, false);

    Project expected = getProject(!fails, true);

    BinMethod method = getMethod(null, "m", signature, pr);

    ChangeMethodSignatureRefactoring ref = new ChangeMethodSignatureRefactoring(
        method);

    MethodSignatureChange change = ref.createSingatureChange();
    change.setTestRun(true);
    reorderParameters(change, newOrder);

    renameParameters(change, oldNames, newNames, null);
//    assertTrue ("case not supported ",oldNames == newNames && newNames ==null);

    ref.setChange(change);

    if (fails) {
      assertFails(ref, expectedSeverity, pr, expected);

    } else {
      assertRefactoring(pr, expected, ref);
    }

  }
  private void assertRefactoring(final Project pr, final Project expected,
      final ChangeMethodSignatureRefactoring ref) {
    RwRefactoringTestUtils.assertRefactoring(ref, expected, pr);
  }

  private void helperFail(String[] newOrder, String[] signature,
      int expectedSeverity) throws Exception {
    helper1(newOrder, signature, true, expectedSeverity);
  }

  private void reorderParameters(final MethodSignatureChange change,
      final String[] newOrder) {

    int[] order = new int[newOrder.length];

    BinParameter pars[] = change.getMethod().getParameters();

    for (int parIndex = 0; parIndex < pars.length; parIndex++) {

      for (int i = 0; i < newOrder.length; i++) {

        if (pars[parIndex].getName().equals(newOrder[i])) {
          order[parIndex] = i;
          break;
        }
      }
    }

    final Permutation permutation = new Permutation(order);

    change.reorderParameters(permutation);
  }

  private void assertFails(final ChangeMethodSignatureRefactoring ref,
      final int expectedSeverity, Project workProject, Project expected) {
    RefactoringStatus status = ref.checkPreconditions();

    if (!status.isCancel() && !status.isErrorOrFatal()) {
      status.merge(ref.checkUserInput());
    }
    if (IDEController.getInstance().getPlatform() == IDEController.TEST
        && !(status.isCancel() || status.isErrorOrFatal())) {
            ref.apply();
      RwRefactoringTestUtils.assertSameSources("cannot modify ", workProject,
          expected);
    }

    assertTrue("Expected severity: " + expectedSeverity + ", was: " +
        status.getSeverity(), status.getSeverity() == expectedSeverity);
  }

  private BinMethod getMethod(String typeName, String methodName,
      final String[] signature, final Project pr) {
    if (typeName == null) {
      typeName = "A";
    }
    final BinTypeRef findType = findType(pr, typeName);

    if (findType == null) {
      assertTrue("Type " + typeName + " not found", false);
    }
    BinCIType classA = findType.getBinCIType();
    BinTypeRef parTypes[] = extractParameterTypes(signature, pr);

    BinMethod method;
    if (methodName.equals(classA.getName())) {
      // find constructor
      method = ((BinClass) classA).getConstructor(parTypes);
    } else {
      method = classA.getDeclaredMethod(methodName, parTypes);
    }

    assertTrue("method " + methodName + "(" + Arrays.asList(parTypes) +
        ") does not exist in class " + classA.getQualifiedName(), method != null);

    return method;
  }

  private void helperAddFail(String[] signature, TestParameterInfo[] newPars,
      int[] newIndices,
      int expectedSeverity) throws
      Exception {

    addTest(signature, newPars, newIndices, true, expectedSeverity);
  }

  private void addTest(final String[] signature,
      final TestParameterInfo[] newPars, final int[] newIndices,
      boolean fails, final int expectedSeverity) throws
      Exception {

    Project pr = getProject(!fails, false);
    Project expectedProject = getProject(!fails, true);
    expectedProject.getProjectLoader().build();

    BinMethod method = getMethod(null, "m", signature, pr);

    assertTrue("method does not exist", method != null);

    NewParameterInfo[] newParamInfos = createNewParamInfos(newPars, pr);

    ChangeMethodSignatureRefactoring ref = new ChangeMethodSignatureRefactoring(
        method);

    MethodSignatureChange change = ref.createSingatureChange();
    change.setTestRun(true);
    Assert.must(newParamInfos.length == newIndices.length);

    for (int i = 0; i < newParamInfos.length; i++) {
      change.addParameter(newParamInfos[i], newIndices[i]);
    }

    ref.setChange(change);

    Project expected = getProject(!fails, true);

    if (fails) {
      assertFails(ref, expectedSeverity, pr, expected);
    } else {

      assertRefactoring(pr, expected, ref);
    }
  }

  private void helperDoAllFail(String methodName,
      String[] signature,
      TestParameterInfo newParamInfos[],
      int[] newIndices,
      String[] oldParamNames,
      String[] newParamNames,
      int[] permutation,
      int newVisibility,
      int[] deleted,
      int expectedSeverity) throws Exception {

    helperDoAll("A", methodName, signature, newParamInfos, newIndices,
        oldParamNames,
        newParamNames, null, permutation, newVisibility, deleted, null, true,
        expectedSeverity);
  }

  //------- tests

  public void testFail0() throws Exception {
    helperFail(new String[] {"j", "i"}
        , new String[] {"I", "I"}
        , RefactoringStatus.ERROR);
  }

  public void testFail1() throws Exception {
    //TODO: is duplicate of testFail0()
    helperFail(new String[] {"j", "i"}
        , new String[] {"I", "I"}
        , RefactoringStatus.ERROR);
  }

  static class TestParameterInfo {

    String newTypes, newNames, newDefaultValues;

    public TestParameterInfo(String newTypes, String newNames,
        String newDefaultValues) {
      this.newDefaultValues = newDefaultValues;
      this.newNames = newNames;
      this.newTypes = newTypes;
    }
  }


  public void testFailAdd2() throws Exception {
    String[] signature = {"I"};
    String[] newNames = {"x"};
    String[] newTypes = {"int"};
    String[] newDefaultValues = {"0"};
    int[] newIndices = {0};
    helperAddFail(signature,
        createTestParamInfos(newTypes, newNames, newDefaultValues),
        newIndices, RefactoringStatus.ERROR);
  }

  public void testFailAdd3() throws Exception {
    String[] signature = {"I"};
    String[] newNames = {"x"};
    String[] newTypes = {"int"};
    String[] newDefaultValues = {"not good"};
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = {0};
    helperAddFail(signature, newParamInfo, newIndices, RefactoringStatus.ERROR);
  }

  public void testFailAdd4() throws Exception {
    String[] signature = {"I"};
    String[] newNames = {"x"};
    String[] newTypes = {"not a type"};
    String[] newDefaultValues = {"0"};
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = {0};
    helperAddFail(signature, newParamInfo, newIndices, RefactoringStatus.ERROR);
  }

  public void testFailDoAll5() throws Exception {
    String[] signature = {"I"};
    String[] newNames = null;
    String[] newTypes = null;
    String[] newDefaultValues = null;
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = null;

    String[] oldParamNames = {"i", "j"};
    String[] newParamNames = {"i", "j"};
    int[] permutation = null;
    int[] deletedIndices = {0};
    int newVisibility = BinModifier.INVALID;
    int expectedSeverity = RefactoringStatus.ERROR;

//    TestParameterInfo[] parObject=new NewParametersObect(newTypes);
    helperDoAllFail("m", signature, newParamInfo, newIndices, oldParamNames,
        newParamNames, permutation, newVisibility, deletedIndices,
        expectedSeverity);
  }

  public void testFailDoAll6() throws Exception {
    String[] signature = {"I"};
    String[] newNames = {"a"};
    String[] newTypes = {"Certificate"};
    String[] newDefaultValues = {"null"};
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = {0};

    String[] oldParamNames = {};
    String[] newParamNames = {};
    int[] permutation = {0};
    int[] deletedIndices = {0};
    int newVisibility = BinModifier.INVALID;
    int expectedSeverity = RefactoringStatus.ERROR;
    helperDoAllFail("m", signature, newParamInfo, newIndices, oldParamNames,
        newParamNames, permutation, newVisibility, deletedIndices,
        expectedSeverity);
  }

  public void testFailDoAll7() throws Exception {
    String[] signature = {"I"};
    String[] newNames = {"a"};
    String[] newTypes = {"Fred"};
    String[] newDefaultValues = {"null"};
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = {0};

    String[] oldParamNames = {};
    String[] newParamNames = {};
    int[] permutation = {0};
    int[] deletedIndices = {0};
    int newVisibility = BinModifier.INVALID;
    int expectedSeverity = RefactoringStatus.ERROR;
    helperDoAllFail("m", signature, newParamInfo, newIndices, oldParamNames,
        newParamNames, permutation, newVisibility, deletedIndices,
        expectedSeverity);
  }

  //---------
  public void test0() throws Exception {
    helper1(new String[] {"j", "i"}
        , new String[] {"I", "I"}
        , false, 0);
  }

  public void test1() throws Exception {
    helper1(new String[] {"j", "i"}
        , new String[] {"I", "I"}
        , false, 0);
  }

  public void test2() throws Exception {
    helper1(new String[] {"j", "i"}
        , new String[] {"I", "I"}
        , false, 0);
  }

  public void test3() throws Exception {
    helper1(new String[] {"j", "i"}
        , new String[] {"I", "I"}
        , false, 0);
  }

  public void test4() throws Exception {
    helper1(new String[] {"j", "i"}
        , new String[] {"I", "I"}
        , false, 0);
  }

  public void test5() throws Exception {
    helper1(new String[] {"j", "i"}
        , new String[] {"I", "I"}
        , false, 0);
  }

  public void test6() throws Exception {
    helper1(new String[] {"k", "i", "j"}
        , new String[] {"I", "I", "I"}
        , false, 0);
  }

  public void test7() throws Exception {
    helper1(new String[] {"i", "k", "j"}
        , new String[] {"I", "I", "I"}
        , false, 0);
  }

  public void test8() throws Exception {
    helper1(new String[] {"k", "j", "i"}
        , new String[] {"I", "I", "I"}
        , false, 0);
  }

  public void test9() throws Exception {
    helper1(new String[] {"j", "i", "k"}
        , new String[] {"I", "I", "I"}
        , false, 0);
  }

  public void test10() throws Exception {
    helper1(new String[] {"j", "k", "i"}
        , new String[] {"I", "I", "I"}
        , false, 0);
  }

  public void test11() throws Exception {
    helper1(new String[] {"j", "k", "i"}
        , new String[] {"I", "I", "I"}
        , false, 0);
  }

  public void test12() throws Exception {
    if (RefactorItConstants.runNotImplementedTests) {
      // comments changing test
      helper1(new String[] {"j", "k", "i"}
          , new String[] {"I", "I", "I"}
          , false, 0);
    }
  }

  public void test13() throws Exception {

    if (RefactorItConstants.runNotImplementedTests) {
      // comments changing test
      helper1(new String[] {"j", "k", "i"}
          , new String[] {"I", "I", "I"}
          , false, 0);
    }
  }

  public void test14() throws Exception {
    helper1(new String[] {"j", "i"}
        , new String[] {"I", "I"}
        , false, 0);
  }

  public void test15() throws Exception {
    helper1(new String[] {"b", "i"}
        , new String[] {"I", "Z"}
        , false, 0);
  }

  public void test16() throws Exception {
    helper1(new String[] {"b", "i"}
        , new String[] {"I", "Z"}
        , false, 0);
  }

  public void test17() throws Exception {
    //exception because of bug 11151
    helper1(new String[] {"b", "i"}
        , new String[] {"I", "Z"}
        , false, 0);
  }

  public void test18() throws Exception {
    //exception because of bug 11151
    helper1(new String[] {"b", "i"}
        , new String[] {"I", "Z"}
        , false, 0);
  }

  public void test19() throws Exception {
//		printTestDisabledMessage("bug 7274 - reorder parameters: incorrect when parameters have more than 1 modifiers");
    helper1(new String[] {"b", "i"}
        , new String[] {"I", "Z"}
        , false, 0);
  }

  public void test20() throws Exception {
//		printTestDisabledMessage("bug 18147");
    helper1(new String[] {"b", "a"}
        , new String[] {"I", "[I"}
        , false, 0);
  }

//constructor tests
  public void test21() throws Exception {
    if (!RUN_CONSTRUCTOR_TEST) {
      printTestDisabledMessage("disabled for constructors for now");
      return;
    }
    String[] signature = {"I", "I"};
    TestParameterInfo[] newParamInfo = null;
    int[] newIndices = null;

    String[] oldParamNames = {"a", "b"};
    String[] newParamNames = {"a", "b"};
    int[] permutation = {1, 0};
    int newVisibility = BinModifier.INVALID; //retain
    int[] deleted = null;
    String newReturnTypeName = null;
    helperDoAll("A", "A", signature, newParamInfo, newIndices, oldParamNames,
        newParamNames, null, permutation, newVisibility, deleted,
        newReturnTypeName, false, 0);
  }

  public void test22() throws Exception {
    if (!RUN_CONSTRUCTOR_TEST) {
      printTestDisabledMessage("disabled for constructors for now");
      return;
    }
    String[] signature = {"I", "I"};
    TestParameterInfo[] newParamInfo = null;
    int[] newIndices = null;

    String[] oldParamNames = {"a", "b"};
    String[] newParamNames = {"a", "b"};
    int[] permutation = {1, 0};
    int newVisibility = BinModifier.INVALID; //retain
    int[] deleted = null;
    String newReturnTypeName = null;
    helperDoAll("A", "A", signature, newParamInfo, newIndices, oldParamNames,
        newParamNames, null, permutation, newVisibility, deleted,
        newReturnTypeName, false, 0);
  }

  public void test23() throws Exception {
    if (!RUN_CONSTRUCTOR_TEST) {
      printTestDisabledMessage("disabled for constructors for now");
      return;
    }
    String[] signature = {"I", "I"};
    TestParameterInfo[] newParamInfo = null;
    int[] newIndices = null;

    String[] oldParamNames = {"a", "b"};
    String[] newParamNames = {"a", "b"};
    int[] permutation = {1, 0};
    int newVisibility = BinModifier.INVALID; //retain
    int[] deleted = null;
    String newReturnTypeName = null;
    helperDoAll("A", "A", signature, newParamInfo, newIndices, oldParamNames,
        newParamNames, null, permutation, newVisibility, deleted,
        newReturnTypeName, false, 0);
  }

  public void test24() throws Exception {
    if (!RUN_CONSTRUCTOR_TEST) {
      printTestDisabledMessage("disabled for constructors for now");
      return;
    }
//		if (true){
//			printTestDisabledMessage("Bug 24230");
//			return;
//		}
    String[] signature = {"I", "I"};
    TestParameterInfo[] newParamInfo = null;
    int[] newIndices = null;

    String[] oldParamNames = {"a", "b"};
    String[] newParamNames = {"a", "b"};
    int[] permutation = {1, 0};
    int newVisibility = BinModifier.INVALID; //retain
    int[] deleted = null;
    String newReturnTypeName = null;
    helperDoAll("A", "A", signature, newParamInfo, newIndices, oldParamNames,
        newParamNames, null, permutation, newVisibility, deleted,
        newReturnTypeName, false, 0);
  }

  public void test25() throws Exception {
    if (!RUN_CONSTRUCTOR_TEST) {
      printTestDisabledMessage("disabled for constructors for now");
      return;
    }
    String[] signature = {"I", "I"};
    TestParameterInfo[] newParamInfo = null;
    int[] newIndices = null;

    String[] oldParamNames = {"a", "b"};
    String[] newParamNames = {"a", "b"};
    int[] permutation = {1, 0};
    int newVisibility = BinModifier.INVALID; //retain
    int[] deleted = null;
    String newReturnTypeName = null;
    helperDoAll("Outer$A", "A", signature, newParamInfo, newIndices,
        oldParamNames,
        newParamNames, null, permutation, newVisibility, deleted,
        newReturnTypeName, false, 0);
  }

  public void test26() throws Exception {
    if (!RUN_CONSTRUCTOR_TEST) {
      printTestDisabledMessage("disabled for constructors for now");
      return;
    }
    String[] signature = {"I", "I"};
    TestParameterInfo[] newParamInfo = null;
    int[] newIndices = null;

    String[] oldParamNames = {"a", "b"};
    String[] newParamNames = {"a", "b"};
    int[] permutation = {1, 0};
    int newVisibility = BinModifier.INVALID; //retain
    int[] deleted = null;
    String newReturnTypeName = null;
    helperDoAll("A", "A", signature, newParamInfo, newIndices, oldParamNames,
        newParamNames, null, permutation, newVisibility, deleted,
        newReturnTypeName, false, 0);
  }

  public void testRenameReorder26() throws Exception {
    helper1(new String[] {"a", "y"}
        , new String[] {"Z", "I"}
        , new String[] {"y", "a"}
        , new String[] {"zzz", "bb"}
        , false, 0);
  }

  public void testRenameReorder27() throws Exception {
    helper1(new String[] {"a", "y"}
        , new String[] {"Z", "I"}
        , new String[] {"y", "a"}
        , new String[] {"yyy", "a"}
        , false, 0);
  }

  public void testAdd28() throws Exception {
    String[] signature = {"I"};
    String[] newNames = {"x"};
    String[] newTypes = {"int"};
    String[] newDefaultValues = {"0"};
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = {1};
    helperAdd(signature, newParamInfo, newIndices);
  }

  public void testAdd29() throws Exception {
    String[] signature = {"I"};
    String[] newNames = {"x"};
    String[] newTypes = {"int"};
    String[] newDefaultValues = {"0"};
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = {0};
    helperAdd(signature, newParamInfo, newIndices);
  }

  public void testAdd30() throws Exception {
    String[] signature = {"I"};
    String[] newNames = {"x"};
    String[] newTypes = {"int"};
    String[] newDefaultValues = {"0"};
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = {1};
    helperAdd(signature, newParamInfo, newIndices);
  }

  public void testAdd31() throws Exception {
    String[] signature = {"I"};
    String[] newNames = {"x"};
    String[] newTypes = {"int"};
    String[] newDefaultValues = {"0"};
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = {1};
    helperAdd(signature, newParamInfo, newIndices);
  }

  public void testAdd32() throws Exception {
    String[] signature = {"I"};
    String[] newNames = {"x"};
    String[] newTypes = {"int"};
    String[] newDefaultValues = {"0"};
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = {0};
    helperAdd(signature, newParamInfo, newIndices);
  }

  public void testAdd33() throws Exception {
    String[] signature = {};
    String[] newNames = {"x"};
    String[] newTypes = {"int"};
    String[] newDefaultValues = {"0"};
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = {0};
    helperAdd(signature, newParamInfo, newIndices);
  }

  public void testAddReorderRename34() throws Exception {
    String[] signature = {"I", "Z"};
    String[] newNames = {"x"};
    String[] newTypes = {"Object"};
    String[] newDefaultValues = {"null"};
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = {1};

    String[] oldParamNames = {"iii", "j"};
    String[] newParamNames = {"i", "jj"};
    int[] permutation = {2, 1, 0};
    int[] deletedIndices = null;
    int newVisibility = BinModifier.INVALID; //retain
    String newReturnTypeName = null;
    helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames,
        newParamNames, null, permutation, newVisibility, deletedIndices,
        newReturnTypeName, false, 0);
  }

  public void testAll35() throws Exception {
    String[] signature = {"I", "Z"};
    String[] newNames = null;
    String[] newTypes = null;
    String[] newDefaultValues = null;
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = null;

    String[] oldParamNames = {"iii", "j"};
    String[] newParamNames = oldParamNames;
    int[] permutation = {0, 1};
    int[] deletedIndices = null;
    int newVisibility = BinModifier.PUBLIC;
    String newReturnTypeName = null;
    helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames,
        newParamNames, null, permutation, newVisibility, deletedIndices,
        newReturnTypeName, false, 0);
  }

  public void testAll36() throws Exception {
    String[] signature = {"I", "Z"};
    String[] newNames = null;
    String[] newTypes = null;
    String[] newDefaultValues = null;
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = null;

    String[] oldParamNames = {"iii", "j"};
    String[] newParamNames = oldParamNames;
    int[] permutation = {0, 1};
    int[] deletedIndices = null;
    int newVisibility = BinModifier.PRIVATE;
    String newReturnTypeName = null;
    helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames,
        newParamNames, null, permutation, newVisibility, deletedIndices,
        newReturnTypeName, false, 0);
  }

  public void testAll37() throws Exception {
    String[] signature = {"I", "Z"};
    String[] newNames = null;
    String[] newTypes = null;
    String[] newDefaultValues = null;
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = null;

    String[] oldParamNames = {"iii", "j"};
    String[] newParamNames = oldParamNames;
    int[] permutation = {0, 1};
    int[] deletedIndices = null;
    int newVisibility = BinModifier.PROTECTED;
    String newReturnTypeName = null;
    helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames,
        newParamNames, null, permutation, newVisibility, deletedIndices,
        newReturnTypeName, false, 0);
  }

  public void testAll38() throws Exception {
    String[] signature = {"I", "Z"};
    String[] newNames = null;
    String[] newTypes = null;
    String[] newDefaultValues = null;
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = null;

    String[] oldParamNames = {"iii", "j"};
    String[] newParamNames = oldParamNames;
    int[] permutation = {0, 1};
    int[] deletedIndices = null;
    int newVisibility = BinModifier.PROTECTED;
    String newReturnTypeName = null;
    helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames,
        newParamNames, null, permutation, newVisibility, deletedIndices,
        newReturnTypeName, false, 0);
  }

  public void testAll39() throws Exception {
    String[] signature = {"I", "Z"};
    String[] newNames = {"x"};
    String[] newTypes = {"Object"};
    String[] newDefaultValues = {"null"};
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = {1};

    String[] oldParamNames = {"iii", "j"};
    String[] newParamNames = {"i", "jj"};
    int[] permutation = {2, -1, 0};
    int[] deletedIndices = null;
    int newVisibility = BinModifier.PUBLIC;
    String newReturnTypeName = null;
    helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames,
        newParamNames, null, permutation, newVisibility, deletedIndices,
        newReturnTypeName, false, 0);
  }

  public void testAll40() throws Exception {
    String[] signature = {"I", "Z"};
    String[] newNames = {"x"};
    String[] newTypes = {"int[]"};
    String[] newDefaultValues = {"null"};
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = {1};

    String[] oldParamNames = {"iii", "j"};
    String[] newParamNames = {"i", "jj"};
    int[] permutation = {2, 1, 0};
    int[] deletedIndices = null;
    int newVisibility = BinModifier.PUBLIC;
    String newReturnTypeName = null;
    helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames,
        newParamNames, null, permutation, newVisibility, deletedIndices,
        newReturnTypeName, false, 0);
  }

  public void testAll41() throws Exception {
    String[] signature = {"I"};
    String[] newNames = null;
    String[] newTypes = null;
    String[] newDefaultValues = null;
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = null;

    String[] oldParamNames = {"i"};
    String[] newParamNames = {"i"};
    int[] permutation = null; //{0};
    int[] deletedIndices = {0};
    int newVisibility = BinModifier.INVALID;
    String newReturnTypeName = null;
    helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames,
        newParamNames, null, permutation, newVisibility, deletedIndices,
        newReturnTypeName, false, 0);
  }

  public void testAll42() throws Exception {
    String[] signature = {"I"};
    String[] newNames = {"i"};
    String[] newTypes = {"int"};
    String[] newDefaultValues = {"0"};
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = {0};

    String[] oldParamNames = {"i"};
    String[] newParamNames = {"i"};
    int[] permutation = {0}; //{0, -1};
    int[] deletedIndices = {0};
    int newVisibility = BinModifier.INVALID;
    String newReturnTypeName = null;
    helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames,
        newParamNames, null, permutation, newVisibility, deletedIndices,
        newReturnTypeName, false, 0);
  }

  public void testAll43() throws Exception {
    String[] signature = {"I", "I"};
    String[] newNames = null;
    String[] newTypes = null;
    String[] newDefaultValues = null;
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = null;

    String[] oldParamNames = {"i", "j"};
    String[] newParamNames = {"i", "j"};
    int[] permutation = {0}; //{1, 0};
    int[] deletedIndices = {0};
    int newVisibility = BinModifier.INVALID;
    String newReturnTypeName = null;
    helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames,
        newParamNames, null, permutation, newVisibility, deletedIndices,
        newReturnTypeName, false, 0);
  }

  public void testAll44() throws Exception {
    if (true) { // disabled in eclipse
      printTestDisabledMessage("need to decide how to treat compile errors");
      return;
    }
    String[] signature = {"I", "I"};
    String[] newNames = null;
    String[] newTypes = null;
    String[] newDefaultValues = null;
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = null;

    String[] oldParamNames = {"i", "j"};
    String[] newParamNames = {"i", "j"};
    int[] permutation = {0, 1};
    int[] deletedIndices = {0};
    int newVisibility = BinModifier.INVALID;
    String newReturnTypeName = "boolean";
    helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames,
        newParamNames, null, permutation, newVisibility, deletedIndices,
        newReturnTypeName, false, 0);
  }

  public void testAll45() throws Exception {
    if (true) {
      printTestDisabledMessage("need to decide how to treat compile errors");
      return;
    }

    String[] signature = {"I", "I"};
    String[] newNames = null;
    String[] newTypes = null;
    String[] newDefaultValues = null;
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = null;

    String[] oldParamNames = {"i", "j"};
    String[] newParamNames = {"i", "j"};
    String[] newParamTypeNames = {"int", "boolean"};
    int[] permutation = {0, 1};
    int[] deletedIndices = {0};
    int newVisibility = BinModifier.INVALID;
    String newReturnTypeName = null;
    helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames,
        newParamNames, newParamTypeNames, permutation, newVisibility,
        deletedIndices, newReturnTypeName, false, 0);
  }

  public void testAll46() throws Exception {
    if (!RUN_CONSTRUCTOR_TEST) {
      printTestDisabledMessage("disabled for constructors for now");
      return;
    }

    String[] signature = {};
    String[] newNames = {"i"};
    String[] newTypes = {"int"};
    String[] newDefaultValues = {"1"};
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = {0};

    String[] oldParamNames = {};
    String[] newParamNames = {};
    int[] permutation = {0};
    int[] deletedIndices = null;
    int newVisibility = BinModifier.INVALID;
    String newReturnTypeName = null;
    helperDoAll("A", "A", signature, newParamInfo, newIndices, oldParamNames,
        newParamNames, null, permutation, newVisibility, deletedIndices,
        newReturnTypeName, false, 0);
  }

  public void testAll47() throws Exception {
    if (!RUN_CONSTRUCTOR_TEST) {
      printTestDisabledMessage("disabled for constructors for now");
      return;
    }

    String[] signature = {};
    String[] newNames = {"i"};
    String[] newTypes = {"int"};
    String[] newDefaultValues = {"1"};
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = {0};

    String[] oldParamNames = {};
    String[] newParamNames = {};
    int[] permutation = {0};
    int[] deletedIndices = null;
    int newVisibility = BinModifier.INVALID;
    String newReturnTypeName = null;
    helperDoAll("A", "A", signature, newParamInfo, newIndices, oldParamNames,
        newParamNames, null, permutation, newVisibility, deletedIndices,
        newReturnTypeName, false, 0);
  }

  public void testAll48() throws Exception {
    if (!RUN_CONSTRUCTOR_TEST) {
      printTestDisabledMessage("disabled for constructors for now");
      return;
    }

    String[] signature = {};
    String[] newNames = {"i"};
    String[] newTypes = {"int"};
    String[] newDefaultValues = {"1"};
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = {0};

    String[] oldParamNames = {};
    String[] newParamNames = {};
    int[] permutation = {0};
    int[] deletedIndices = null;
    int newVisibility = BinModifier.INVALID;
    String newReturnTypeName = null;
    helperDoAll("A", "A", signature, newParamInfo, newIndices, oldParamNames,
        newParamNames, null, permutation, newVisibility, deletedIndices,
        newReturnTypeName, false, 0);
  }

  public void testAll49() throws Exception {
    if (!RUN_CONSTRUCTOR_TEST) {
      printTestDisabledMessage("disabled for constructors for now");
      return;
    }

    String[] signature = {};
    String[] newNames = {"i"};
    String[] newTypes = {"int"};
    String[] newDefaultValues = {"1"};
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = {0};

    String[] oldParamNames = {};
    String[] newParamNames = {};
    int[] permutation = {0};
    int[] deletedIndices = null;
    int newVisibility = BinModifier.INVALID;
    String newReturnTypeName = null;
    helperDoAll("A", "A", signature, newParamInfo, newIndices, oldParamNames,
        newParamNames, null, permutation, newVisibility, deletedIndices,
        newReturnTypeName, false, 0);
  }

  public void testAll50() throws Exception {

    if (!RUN_CONSTRUCTOR_TEST) {
      printTestDisabledMessage("disabled for constructors for now");
      return;
    }

    if (!RefactorItConstants.runNotImplementedTests) {
      printTestDisabledMessage("testAll50 not implemented");
      return;
    }

    String[] signature = {};
    String[] newNames = {"i"};
    String[] newTypes = {"int"};
    String[] newDefaultValues = {"1"};
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = {0};

    String[] oldParamNames = {};
    String[] newParamNames = {};
    int[] permutation = {0};
    int[] deletedIndices = null;
    int newVisibility = BinModifier.INVALID;
    String newReturnTypeName = null;
    helperDoAll("A", "A", signature, newParamInfo, newIndices, oldParamNames,
        newParamNames, null, permutation, newVisibility, deletedIndices,
        newReturnTypeName, false, 0);
  }

  public void testAll51() throws Exception {
    if (!RUN_CONSTRUCTOR_TEST) {
      printTestDisabledMessage("disabled for constructors for now");
      return;
    }

    String[] signature = {};
    String[] newNames = {"i"};
    String[] newTypes = {"int"};
    String[] newDefaultValues = {"1"};
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = {0};

    String[] oldParamNames = {};
    String[] newParamNames = {};
    int[] permutation = {0};
    int[] deletedIndices = null;
    int newVisibility = BinModifier.INVALID;
    String newReturnTypeName = null;
    helperDoAll("F$A", "A", signature, newParamInfo, newIndices, oldParamNames,
        newParamNames, null, permutation, newVisibility, deletedIndices,
        newReturnTypeName, false, 0);
  }

  public void testAll52() throws Exception {
    if (!RUN_CONSTRUCTOR_TEST) {
      printTestDisabledMessage("disabled for constructors for now");
      return;
    }

    String[] signature = {};
    String[] newNames = {"i"};
    String[] newTypes = {"int"};
    String[] newDefaultValues = {"1"};
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = {0};

    String[] oldParamNames = {};
    String[] newParamNames = {};
    int[] permutation = {0};
    int[] deletedIndices = null;
    int newVisibility = BinModifier.INVALID;
    String newReturnTypeName = null;
    helperDoAll("A", "A", signature, newParamInfo, newIndices, oldParamNames,
        newParamNames, null, permutation, newVisibility, deletedIndices,
        newReturnTypeName, false, 0);
  }

  public void testAll53() throws Exception {
    String[] signature = {"I"};
    String[] newNames = {"a"};
    String[] newTypes = {"java.util.HashSet"};
    String[] newDefaultValues = {"null"};
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = {0};

    String[] oldParamNames = {};
    String[] newParamNames = {};
    int[] permutation = {0};
    int[] deletedIndices = {0};
    int newVisibility = BinModifier.INVALID;
    String newReturnTypeName = null;
    helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames,
        newParamNames, null, permutation, newVisibility, deletedIndices,
        newReturnTypeName, false, 0);
  }

  public void testAll54() throws Exception {
    String[] signature = {"I"};
    String[] newNames = {"a"};
    String[] newTypes = {"java.util.List"};
    String[] newDefaultValues = {"null"};
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = {0};

    String[] oldParamNames = {};
    String[] newParamNames = {};
    int[] permutation = {0};
    int[] deletedIndices = {0};
    int newVisibility = BinModifier.INVALID;
    String newReturnTypeName = null;
    helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames,
        newParamNames, null, permutation, newVisibility, deletedIndices,
        newReturnTypeName, false, 0);
  }

  public void testAll55() throws Exception {
//		printTestDisabledMessage("test for bug 32654 [Refactoring] Change method signature with problems");
    String[] signature = {"[QObject;", "I", "Z"};
    String[] newNames = {"e"};
    String[] newTypes = {"boolean"};
    String[] newDefaultValues = {"true"};
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = {2};
    helperAdd(signature, newParamInfo, newIndices);

  }

  public void testAll56() throws Exception {
    if (!RUN_CONSTRUCTOR_TEST) {
      printTestDisabledMessage("disabled for constructors for now");
      return;
    }

//		printTestDisabledMessage("test for 38366 ArrayIndexOutOfBoundsException in change signeture [refactoring] ");
    String[] signature = {"QEvaViewPart;", "I"};
    String[] newNames = null;
    String[] newTypes = null;
    String[] newDefaultValues = null;
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = {};

    String[] oldParamNames = {"part", "title"};
    String[] newParamNames = {"part", "title"};
    int[] permutation = {0};
    int[] deletedIndices = {0};
    int newVisibility = BinModifier.PUBLIC;
    String newReturnTypeName = null;
    helperDoAll("HistoryFrame", "HistoryFrame", signature, newParamInfo,
        newIndices,
        oldParamNames, newParamNames, null, permutation, newVisibility,
        deletedIndices, newReturnTypeName, false, 0);
  }

  public void testAll57() throws Exception {
//		printTestDisabledMessage("test for 39633 classcast exception when refactoring change method signature [refactoring]");
//		if (true)
//			return;
    String[] signature = {"I", "QString;", "QString;"};
    String[] newNames = null;
    String[] newTypes = null;
    String[] newDefaultValues = null;
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = null;

    String[] oldParamNames = {"i", "hello", "goodbye"};
    String[] newParamNames = oldParamNames;
    int[] permutation = {0, 2, 1};
    int[] deletedIndices = {};
    int newVisibility = BinModifier.PUBLIC;
    String newReturnTypeName = null;
    helperDoAll("TEST$X", "method", signature, newParamInfo, newIndices,
        oldParamNames, newParamNames, null, permutation, newVisibility,
        deletedIndices, newReturnTypeName, false, 0);
  }

  public void testAll58() throws Exception {
    String[] signature = {"I", "[[[QString;"};
    String[] newNames = null;
    String[] newTypes = null;
    String[] newDefaultValues = null;
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = null;

    String[] oldParamNames = {"a", "b"};
    String[] newParamNames = {"abb", "bbb"};
    int[] permutation = {1, 0};
    int[] deletedIndices = {};
    int newVisibility = BinModifier.PUBLIC;
    String newReturnTypeName = null;
    helperDoAll("A", "m", signature, newParamInfo, newIndices, oldParamNames,
        newParamNames, null, permutation, newVisibility, deletedIndices,
        newReturnTypeName, false, 0);
  }

  public void testAll59() throws Exception {
    String[] signature = {"I","QString;"};
    String[] newNames = null;
    String[] newTypes = null;
    String[] newDefaultValues = null;
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = null;

    String[] oldParamNames = {"x","s"};
    String[] newParamNames = {"x","s"};
    int[] permutation = null; //{0};
    int[] deletedIndices = {1};
    int newVisibility = BinModifier.NONE;
    String newReturnTypeName = null;
    helperDoAll("Ax", "myMeth", signature, newParamInfo, newIndices, oldParamNames,
        newParamNames, null, permutation, newVisibility, deletedIndices,
        newReturnTypeName, false, 0);
  }

  public void testAll60() throws Exception {
/*
    String[] signature = {"I"};
    String[] newNames = {"str"};
    String[] newTypes = {"String"};
    String[] newDefaultValues = {"null"};
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = {1};

    String[] oldParamNames = {"x"};
    String[] newParamNames = {"x"};
    int[] permutation = {};
    int[] deletedIndices = null;
    int newVisibility = BinModifier.NONE;
    String newReturnTypeName = null;
    helperDoAll("Bx", "myMeth", signature, newParamInfo, newIndices, oldParamNames,
        newParamNames, null, permutation, newVisibility, deletedIndices,
        newReturnTypeName, false, 0);*/

    String[] signature = {"I"};
    String[] newNames = {"str"};
    String[] newTypes = {"String"};
    String[] newDefaultValues = {"null"};
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = {1};

    String[] oldParamNames = {"x"};
    String[] newParamNames = {"x"};
    int[] permutation = null;
    int[] deletedIndices = null;
    int newVisibility = BinModifier.NONE; //retain
    String newReturnTypeName = null;
    helperDoAll("Bx", "myMeth", signature, newParamInfo, newIndices, oldParamNames,
        newParamNames, null, permutation, newVisibility, deletedIndices,
        newReturnTypeName, false, 0);
  }

  public void testAddRecursive1() throws Exception {
    printTestDisabledMessage(
        "strange NPE: method binding for super.m(1) in class Super");
//		String[] signature= {"I"};
//		String[] newNames= {"bool"};
//		String[] newTypes= {"boolean"};
//		String[] newDefaultValues= {"true"};
//		TestParameterInfo[] newParamInfo= createTestParamInfos(newTypes, newNames, newDefaultValues);
//		int[] newIndices= {1};
//		helperAdd(signature, newParamInfo, newIndices);
  }

  public void testXXX() throws Exception {
    String initialProjectContent = "package testPackage;\n" +
    "\n" +
    "class A {\n" +
    "  public void XXX(int arg) {\n" +
    "    arg = 4;\n" +
    "  }\n" +
    "}\n" +
    "\n" +
    "public class B extends A {\n" +
    "  public void XXX(int s) {" +
    "  }\n" +
    "}";

String expectedProjectContent = "package testPackage;\n" +
	"\n" +
	"class A {\n" +
	"  public void XXX(long arg) {\n" +
	"    arg = 4;\n" +
	"  }\n" +
	"}\n" +
	"\n" +
	"public class B extends A {\n" +
	"  public void XXX(long s) {" +
	"  }\n" +
	"}";

	Project initialProject = Utils.createTestRbProjectFromString(new Utils.
	TempCompilationUnit[] {
	new Utils.TempCompilationUnit(
	    initialProjectContent,
	"B.java", "testPackage"
	)});

	Project expectedProject = Utils.createTestRbProjectFromString(new Utils.
	TempCompilationUnit[] {
	new Utils.TempCompilationUnit(
	    expectedProjectContent,
	"B.java", "testPackage"
	)});


	BinTypeRef type = initialProject.getTypeRefForName("testPackage.B");
	BinMethod method = type.getBinCIType().getDeclaredMethods()[0];

	ChangeMethodSignatureRefactoring ref = new ChangeMethodSignatureRefactoring(method);

	MethodSignatureChange change = ref.createSingatureChange();
	change.setTestRun(true);
	change.changeParameterType(0, BinPrimitiveType.LONG_REF);
	ref.setChange(change);

    RwRefactoringTestUtils.assertRefactoring(ref, initialProject, expectedProject);
  }


  private BinTypeRef findType(Project pr, String className) {

    if (className.equals("String")) {
      className = "java.lang.String";
    } else if (className.equals("Object")) {
      className = "java.lang.Object";
    }
    int arrIndex = className.indexOf('[');

    int dimension = 0;
    if (arrIndex > 0) {
      String arrStr = className;
      className = className.substring(0, arrIndex);
      do {
        ++dimension;
        if (arrIndex == arrStr.length() - 1) {
          arrStr = "";
        } else {
          arrStr = arrStr.substring(arrIndex + 1);
        }
        arrIndex = arrStr.indexOf('[');

      } while (arrIndex > 0);

    }

    BinTypeRef result = pr.findTypeRefForName(className);

    if (result == null) {
      result = pr.findTypeRefForName("p." + className);
    }

    if (result == null) {
      result = pr.findPrimitiveTypeForName(className);
    }

    if (result == null) {

      BinPackage[] packages = pr.getAllPackages();

      for (int i = 0; result == null && i < packages.length; i++) {
        BinPackage pack = packages[i];

        String qName = pack.getQualifiedName();

        if (qName.length() > 0) {
          qName += ".";
        }
        qName += className;

        result = pr.findTypeRefForName(qName);
      }

    }
    if (result == null && ChangeMethodSignatureRefactoring.debug) {

      System.out.println("type not found in project for name " + className);
    }
    if (dimension > 0) {
      result = pr.createArrayTypeForType(result, dimension);
    }

    return result;
  }

  private BinTypeRef[] extractParameterTypes(String[] signature,
      Project project) {

    BinTypeRef result[] = new BinTypeRef[signature.length];

    for (int i = 0; i < signature.length; i++) {

      final String typeName = signature[i];

      BinTypeRef type = extractType(project, typeName);
      result[i] = type;
    }
    return result;
  }

  /**
   * @param project
   * @param typeName
   * @return
   */
  private BinTypeRef extractType(final Project project, final String typeName) {
    int dimension = 0;

    String sign = typeName;

    sign.trim();

    if (sign.endsWith(";")) {
      sign = sign.substring(0, sign.length() - 1);
    }

    while (sign.startsWith("[")) {
      sign = sign.substring(1);
      ++dimension;
    }

    BinType type = null;

    if (sign.equals("I")) {
      type = BinPrimitiveType.INT;
    } else if (sign.equals("Z")) {
      type = BinPrimitiveType.BOOLEAN;
    } else if (sign.equals("J")) {
      type = BinPrimitiveType.LONG;
    } else if (sign.startsWith("Q")) {
      String typeNameA = sign.substring(1);

      BinTypeRef typeRef = findType(project, typeNameA);

//      if(type==null) {
//        typeRef=findType(project,"p."+typeNameA);
//      }

      assertTrue("Could not find type " + typeNameA, typeRef != null);
      type = typeRef.getBinCIType();
    } else {
      assertTrue("signature type not supported " + sign, false);
    }

    if (dimension > 0) {
      type = project.createArrayTypeForType(type.getTypeRef(),
          dimension).getBinType();
    }

//    System.out.println("extracted parameter type "+type.getTypeRef()+
//                       " from signature "+typeName2);
//    System.out.println("dimension == "+dimension);

    return type.getTypeRef();
  }

  public void testException01() throws Exception {
//		String[] signature= {"J"};
//		String[] remove= {};
//		String[] add= {"java.util.zip.ZipException"};
    printTestDisabledMessage("testException01 is disabled now");
    /*
       helperException(signature, remove, add);
     */
  }

  public void testEnum01() throws Exception {
    String[] signature = {"Z", "I"};
    String[] newNames = null;
    String[] newTypes = null;
    String[] newDefaultValues = null;
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes,
        newNames,
        newDefaultValues);
    int[] newIndices = null;

    String[] oldParamNames = {"a", "b"};
    String[] newParamNames = {"a", "b"};
    int[] permutation = {1, 0};
    int[] deletedIndices = {};
    int newVisibility = BinModifier.INVALID;
    String newReturnTypeName = null;
    helperDoAll("Numbers", "Numbers", signature, newParamInfo, newIndices,
        oldParamNames,
        newParamNames, null, permutation, newVisibility, deletedIndices,
        newReturnTypeName, false, 0);
  }

  public void testEnum02() throws Exception {
    String[] signature = {"I"};
    String[] newNames = null;
    String[] newTypes = null;
    String[] newDefaultValues = null;
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes,
        newNames,
        newDefaultValues);
    int[] newIndices = null;

    String[] oldParamNames = {"a"};
    String[] newParamNames = {"a"};
    int[] permutation = null;
    int[] deletedIndices = {0};
    int newVisibility = BinModifier.INVALID;
    String newReturnTypeName = null;
    helperDoAll("Numbers", "Numbers", signature, newParamInfo, newIndices,
        oldParamNames,
        newParamNames, null, permutation, newVisibility, deletedIndices,
        newReturnTypeName, false, 0);
  }

  public void testEnum03() throws Exception {
    String[] signature = {};
    String[] newNames = {"a"};
    String[] newTypes = {"int"};
    String[] newDefaultValues = {"1"};
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes,
        newNames,
        newDefaultValues);
    int[] newIndices = {0};

    String[] oldParamNames = {};
    String[] newParamNames = {};
    int[] permutation = null;
    int[] deletedIndices = {};
    int newVisibility = BinModifier.INVALID;
    String newReturnTypeName = null;
    helperDoAll("Numbers", "Numbers", signature, newParamInfo, newIndices,
        oldParamNames,
        newParamNames, null, permutation, newVisibility, deletedIndices,
        newReturnTypeName, false, 0);
  }

  /** Bug RIM-181 */
  public void testNpeWithHierarchy() throws Exception {
    helper1(new String[] {"s"}, new String[] {"I"},
        new String[] {"s"}, new String[] {"newname"},
        false, RefactoringStatus.OK);
  }

  public void testDSF1() throws Exception {
    String[] signature = {"I", "I", "I"};
    String[] newNames = null;
    String[] newTypes = null;
    String[] newDefaultValues = null;
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = null;

    String[] oldParamNames = {"a", "b", "c"};
    String[] newParamNames = {"a", "b", "c"};
    int[] permutation = null; //{0};
    int[] deletedIndices = {1};
    int newVisibility = BinModifier.INVALID;
    String newReturnTypeName = null;
    helperDoAll("Test1", "method", signature, newParamInfo, newIndices, oldParamNames,
        newParamNames, null, permutation, newVisibility, deletedIndices,
        newReturnTypeName, false, 0);
  }

  public void testDSF2() throws Exception {
    String[] signature = {"I", "I", "I"};
    String[] newNames = null;
    String[] newTypes = null;
    String[] newDefaultValues = null;
    TestParameterInfo[] newParamInfo = createTestParamInfos(newTypes, newNames,
        newDefaultValues);
    int[] newIndices = null;

    String[] oldParamNames = {"a", "b", "c"};
    String[] newParamNames = {"a", "b", "c"};
    int[] permutation = null; //{0};
    int[] deletedIndices = {1};
    int newVisibility = BinModifier.INVALID;
    String newReturnTypeName = null;
    helperDoAll("Test2", "method", signature, newParamInfo, newIndices, oldParamNames,
        newParamNames, null, permutation, newVisibility, deletedIndices,
        newReturnTypeName, false, 0);
  }

  private NewParameterInfo[] createNewParamInfos(TestParameterInfo[] newPars,
      Project project) {
    NewParameterInfo result[] = new NewParameterInfo[newPars.length];

    for (int i = 0; i < result.length; i++) {
      BinTypeRef type = findType(project, newPars[i].newTypes);

      result[i] = new NewParameterInfo(type, newPars[i].newNames,
          newPars[i].newDefaultValues, 0);
    }
    return result;
  }

  private TestParameterInfo[] createTestParamInfos(String[] newTypes,
      String[] newNames,
      String[] newDefaultValues) {
    if (newTypes == null || newNames == null) {
      return null;
    }
    TestParameterInfo result[] = new TestParameterInfo[newTypes.length];

    for (int i = 0; i < result.length; i++) {

      result[i] = new TestParameterInfo(newTypes[i], newNames[i],
          newDefaultValues[i]);
    }
    return result;
  }

  public String getTemplate() {
    String result = "ChangeSignature/" + (canModify ? "canModify"
        : "cannotModify") + "/A_<test_name>_";
    if (canModify) {
      result += "<in_out>.java";
    } else {
      result += "in.java";
    }
    return result;
  }

  private void printTestDisabledMessage(String string) {
    RuntimePlatform.console.println("Change Signature test disabled: " + string);
  }

  private void renameParameters(MethodSignatureChange change,
      String[] oldParamNames, String[] newParamNames,
      BinTypeRef[] newParameterTypes) {

    if (oldParamNames == null || newParamNames == null) {
      return;
    }

    for (int i = 0; i < oldParamNames.length; i++) {
      List parList = change.getParametersList();

      int parIndex = 0;
      for (Iterator iter = parList.iterator(); iter.hasNext(); ++parIndex) {
        ParameterInfo item = (ParameterInfo) iter.next();

        if (item.getName().equals(oldParamNames[i])) {
          change.renamePrameter(parIndex, newParamNames[i]);

          if (newParameterTypes != null) {
            change.changeParameterType(parIndex, newParameterTypes[i]);
//            item.changeType(newParameterTypes[i]);
          }
        }
      }
    }
  }
}
