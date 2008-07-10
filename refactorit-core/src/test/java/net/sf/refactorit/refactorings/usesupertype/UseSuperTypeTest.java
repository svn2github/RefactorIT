/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.usesupertype;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.parser.FastJavaLexer;
import net.sf.refactorit.query.ItemByNameFinder;
import net.sf.refactorit.refactorings.Refactoring;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.refactorings.NullContext;
import net.sf.refactorit.test.refactorings.RefactoringTestCase;
import net.sf.refactorit.utils.RefactorItConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author Tonis Vaga
 */
public class UseSuperTypeTest extends RefactoringTestCase {
  private int oldJvmMode;

  public UseSuperTypeTest() {
    super(UseSuperTypeTest.class.getName());
  }

  public void setUp(){
    oldJvmMode = Project.getDefaultOptions().getJvmMode();
    setDefaultMode();
  }

  public void tearDown(){
    Project.getDefaultOptions().setJvmMode(oldJvmMode);
  }

  private void setDefaultMode() {
    Project.getDefaultOptions().setJvmMode(FastJavaLexer.JVM_50);
  }

  private void setJavaMode14() {
    Project.getDefaultOptions().setJvmMode(FastJavaLexer.JVM_14);
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite(UseSuperTypeTest.class);
    suite.setName("UseSuperTypeTest");
    return suite;
  }

  public String getTemplate() {
    return "UseSuperType/<test_name>/<in_out>";
  }

  public static UseSuperTypeRefactoring createRefactoring(String[] subclassNames,
      String superTypeName, Project project) {
    return createRefactoring(subclassNames,null,superTypeName,project);
  }

  public static UseSuperTypeRefactoring createRefactoring(
      String[] targetClassNames, String memberName,
      String superTypeName, Project project) {
    BinCIType superClass = ItemByNameFinder.findBinCIType(project, superTypeName);

    BinMember target = superClass;

    BinTypeRef subclass;

    List subclasses = new ArrayList();
    for (int j = 0; j < targetClassNames.length; j++) {
    	String targetClassName = targetClassNames[j];

    	if ("".equals(targetClassName)) {
	      subclass = null;
	    } else {
	      if (targetClassName != null && !"".equals(targetClassName)) {
	        targetClassName = getQualifiedNameFor(targetClassName, project);
	      }
	
	      BinCIType type = ItemByNameFinder.findBinCIType(project, targetClassName);
	
	      if (type == null) {
	        throw new RuntimeException("type " + targetClassName
	            + " not found in project");
	      }

	      subclass = type.getTypeRef();
	
	      if (memberName != null) {
	        // target is field or method
	        target = ItemByNameFinder.findBinField(type, memberName);
	
	        if (target == null) {
	          target = ItemByNameFinder.findBinMethod(type, memberName, null);
	        }
	
	        if (target == null) {
	          target = ItemByNameFinder.findLocalVariable(type, memberName);
	        }
	
	        if (target == null) {
	          throw new RuntimeException(
	              "couldn't find member " + memberName + " in " + subclass);
	        }
	
	        subclass = UseSuperTypeUtil.getTargetType(target);
	      }
	    }

	    if (subclass != null) {
	        subclasses.add(subclass);
	    }
    }
    

//   assertTrue ( clazz.getTypeRef().getAllSupertypes().contains(superClass.getTypeRef()) );
    final UseSuperTypeRefactoring refactoring
        = new UseSuperTypeRefactoring(target, new NullContext(project));

    refactoring.setSupertype(superClass.getTypeRef());
    refactoring.setSubtypes(subclasses);

    return refactoring;
  }
  
  private void validatePassingTest(String[] targetClassNames, String memberName,
	      String superTypeFullName, boolean updateInstanceOf, boolean applyTest) {
	  UseSuperTypeRefactoring refactoring;

	  Project expected;
    Project inProject;

    try {
      inProject = getMutableProject(getInitialProject());

      inProject.getOptions().setJvmMode(Project.getDefaultOptions().getJvmMode());

      refactoring = createRefactoring(targetClassNames, memberName,
          superTypeFullName, inProject);

      refactoring.setUseInstanceOf(updateInstanceOf);
      expected = getExpectedProject();
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw ((RuntimeException) e);
      } else {
        throw new RuntimeException(e);
      }
    }

    if (applyTest) {
      RwRefactoringTestUtils.assertRefactoring(
          refactoring, expected, inProject);
    } else {
      UseSuperTypeTest.assertCannotApplyRefactoring(
          refactoring, expected, inProject);
    }
  }

  private void validatePassingTest(String targetClassName, String memberName,
      String superTypeFullName, boolean updateInstanceOf, boolean applyTest) {
	  validatePassingTest(new String[] {targetClassName}, memberName, 
	  		superTypeFullName, updateInstanceOf, applyTest);
  }

  private void validatePassingTest(String[] targetClassNames, 
      String superTypeFullName, boolean applyTest) {
  	validatePassingTest(targetClassNames, null,
        superTypeFullName, false, applyTest);
  }

  private static String getQualifiedNameFor(final String className,
      final Project inProject) {
    BinPackage packages[] = inProject.getAllPackages();

    String fullClassName = null;

    for (int index = 0; index < packages.length; index++) {
      if (packages[index].findTypeForShortName(className) != null) {
        fullClassName = packages[index].getQualifiedForShortname(className);
      }
    }

    return fullClassName;
  }

  private void validatePassingTest(String subclassName, String superTypeFullName,
      boolean canApply) {
    validatePassingTest(subclassName, null, superTypeFullName, false, canApply);
  }

  //---------------tests ----------------------

  public void testMy4() throws Exception {
    validatePassingTest("A", "I", false);
  }

  public void testMy5() throws Exception {
    validatePassingTest("A", "I", false);
  }

  public void testMy6() throws Exception {
    validatePassingTest("A", "I", true);
  }

  public void testMy7() throws Exception {
    validatePassingTest("A", "java.lang.Exception", true);
  }

  public void testMy8() throws Exception {
    validatePassingTest("A", "I", false);
  }

  public void testAmbigiousResult() throws Exception {
    if (RefactorItConstants.runNotImplementedTests) {
      validatePassingTest("A", "I1", false);
    }
  }

  public void testCyclicDependency1() throws Exception {
    //cyclic dependecies test
      validatePassingTest("A", "I", true);
  }

  public void testCyclicDependency2() throws Exception {
    //cyclic dependecies test
      validatePassingTest("A", "I", true);
  }

  public void testSyntheticConstructor() throws Exception {
      validatePassingTest("B", "I", true);
  }

  public void testSyntheticConstructorFails() throws Exception {
      validatePassingTest("B", "I", false);
  }

  public void testAllSubtypes()  {
      validatePassingTest("", "I", true);
  }

  public void testAllSubtypes2() {
      validatePassingTest("", "I", true);
  }

  public void testSingleUsageNotRefrencedSuper() throws Exception {
    Project project = getInitialProject();
    project.getProjectLoader().build();

    BinTypeRef[] supertypes = createRefactoring(new String[] {"A"},
        "list", "java.util.List", project).getPossibleSupertypes();

    BinTypeRef collection = project.findTypeRefForName("java.util.Collection");

    assertTrue(Arrays.asList(supertypes).contains(collection));

    validatePassingTest("A","list","java.util.Collection",false,true);
  }

  public void testDerivedFromObject() {
      validatePassingTest("B", "A", true);
  }

  public void testMultipleCatchBlocks() {
    validatePassingTest("MyException", "java.lang.Exception", false);
  }

  public void testSameSignature() {
      validatePassingTest("", "I", true);
  }

  public void testUsageFromOtherFile() {
      validatePassingTest("", "java.lang.Comparable", false);
  }

  public void testMy11() {
    validatePassingTest("StaticReference", "IReference", true);
  }

  public void testArrayType1() {
    validatePassingTest("A", "p.B", true);
  }

  public void testArrayType2() {
    validatePassingTest("A", "p.B", true);
  }

  public void testRenameType() {
    validatePassingTest("A", "p.p2.B", true);
  }

  public void testNew0() {
    validatePassingTest("A", "java.lang.Object", true);
  }

  public void testNew1() {
//		printTestDisabledMessage("bug 23597 ");
    validatePassingTest("A", "java.lang.Object", true);
  }

  public void testNew2() {
    validatePassingTest("A", "java.lang.Object", false);
  }

  public void testNew3() {
    validatePassingTest("A", "java.lang.Object", true);
  }

  public void testNew4() {
    validatePassingTest("A", "java.lang.Object", false);
  }

  public void testNew5() {
    validatePassingTest("A", "java.lang.Object", true);
  }

  public void testNew6() {
    validatePassingTest("A", "java.lang.Object", true);
    //validatePassingTest("Test", "a", "java.lang.Object", false, true);
  }

  public void testNew7() {
    validatePassingTest("A", "p.B", true);
  }

  public void testNew8() {
    validatePassingTest("A", "p.B", true);
  }

  public void testNew9() {
    validatePassingTest("A", "java.lang.Object", true);
  }

  public void testNew10() {
    validatePassingTest("A", "p.B", true);
  }

  public void testNew11() {
    validatePassingTest("A", "p.B", true);
  }

  public void testNew12() {
    validatePassingTest("A", "p.B", true);
  }

  public void testNew13() {
    validatePassingTest("A", "p.B", true);
  }

  public void testNew14() {
    validatePassingTest("A", "p.B", true);
  }

  public void testNew15() {
    validatePassingTest("A", "p.B", false);
  }

  public void testNew16() {
//		printTestDisabledMessage("instanceof ");
    validatePassingTest("A", "p.B", false);
  }

  public void testNew17() {
    validatePassingTest("A", "p.C", true);
  }

  public void testNew18() {
    validatePassingTest("A", "p.B", true);
  }

  public void testNew19() {
    validatePassingTest("A", "p.I", true);
  }

  public void testNew20() {
//		printTestDisabledMessage("http://dev.eclipse.org/bugs/show_bug.cgi?id=23829");
    validatePassingTest("A", "p.B", true);
  }

  public void testNew21() {
    validatePassingTest("A", "java.lang.Object", false);
  }

  public void testNew22() {
    validatePassingTest("A", "p.B", true);
  }

  public void testNew23() {
    validatePassingTest("A", "java.lang.Object", false);
  }

  public void testNew24() {
    validatePassingTest("A", "java.lang.Object", false);
  }

  public void testNew25() {
    validatePassingTest("A", "java.lang.Object", false);
  }

  public void testNew26() {
    validatePassingTest("A", "java.lang.Object", false);
  }

  public void testNew27() {
    validatePassingTest("A", "p.B", true);
  }

  public void testNew28() {
    validatePassingTest("A", "p.B", false);
  }

  public void testNew29() {
//		printTestDisabledMessage("bug 24278");
    validatePassingTest("A", "p.B", false);
  }

  public void testNew30() {
//		printTestDisabledMessage("bug 24278");
    validatePassingTest("A", "p.B", false);
  }

  public void testNew31() {
//		printTestDisabledMessage("bug 24278");
    validatePassingTest("A", "p.B", false);
  }

  public void testNew32() {
//		printTestDisabledMessage();
    validatePassingTest("A", "p.B", true);
  }

  public void testNew33() {
//		printTestDisabledMessage("bug 26282");
    validatePassingTest("A", "java.util.Vector", true);
  }

  public void testNew34() {
//		printTestDisabledMessage("bug 26282");
    validatePassingTest("A", "java.util.Vector", true);
  }

  public void testNew35() {
//		printTestDisabledMessage("bug 26282");
    validatePassingTest("A", "java.util.Vector", true);
  }

  public void testNew36() {
//		printTestDisabledMessage("bug 26288");
    validatePassingTest("A", "p.B", true);
  }

  public void testNew37() {
//		printTestDisabledMessage("bug 26288");
    validatePassingTest("A", "p.B", true);
  }

  public void testNew38() {
//		printTestDisabledMessage("bug 40373");
    validatePassingTest("A", "p.B", true);
  }

  /* i had to rename tests 0-15 because of cvs problems*/

  public void test0_() {
    validatePassingTest("A", "p.I", false);
  }

  public void test1_() {
    validatePassingTest("A", "p.I", false);
  }

  public void test2_() {
    validatePassingTest("A", "p.I", false);
  }

  public void test3_() {
    validatePassingTest("A", "p.I", false);
  }

  public void test4_() {
    validatePassingTest("A", "p.I", false);
  }

  public void test5_() {
    validatePassingTest("A", "p.I", false);
  }

  public void test6_() {
    validatePassingTest("A", "p.I", false);
  }

  public void test7_() {
    validatePassingTest("A", "p.I", false);
  }

  public void test8_() {
    validatePassingTest("A", "p.I", false);
  }

  public void test9_() {
    validatePassingTest("A", "p.I", false);
  }

  public void test10_() {
    validatePassingTest("A", "p.I", false);
  }

  public void test11_() {
    validatePassingTest("A", "p.I", false);
  }

  public void test12_() {
    validatePassingTest("A", "p.I", true);
  }

  public void test13_() {
    validatePassingTest("A", "p.I", false);
  }

  public void test14_() {
    validatePassingTest("A", "p.I", true);
  }

  public void test15_() {
    validatePassingTest("A", "p.I", true);
  }

  public void test16() {
    validatePassingTest("A", "p.I", true);
  }

  public void test17() {
    validatePassingTest("A", "p.I", false);
  }

  public void test18() {
    validatePassingTest("A", "p.I", true);
  }

  public void test19() {
    validatePassingTest("A", "p.I", false);
  }

  public void test20() {
    validatePassingTest("A", "p.I", true);
  }

  public void test21() {
    //disable for exceptions
//	validatePassingTest("A", new String[]{"A", "I"}, "p.I");
  }

  public void test22() {
    validatePassingTest("A", "p.I", false);
  }

  public void test23() {
    validatePassingTest("A", "p.I", true);
  }

  public void test24() {
    validatePassingTest("A", "p.I", true);
  }

  public void test25() {
    validatePassingTest("A", "p.I", true);
  }

  public void test26() {
    validatePassingTest("A", "p.I", true);
  }

  public void test27() {
    validatePassingTest("A", "p.I", false);
  }

  public void test28() {
//		printTestDisabledMessage("bug 22883");
    validatePassingTest("A", "p.I", true);
  }

  public void test29() {
    validatePassingTest("A", "p.I", false);
  }

  public void test30() {
    validatePassingTest("A", "p.I", false);
  }

  public void test31() {
    // needs type changes dependencies resolving
    validatePassingTest("A", "p.I", true);
  }

  public void test32() {
    // needs type changes dependencies resolving
    validatePassingTest("A", "p.I", true);
  }

  public void test33() {
    validatePassingTest("A", "p.I", false);
  }

  public void test34() {
    // needs type changes dependencies resolving
    validatePassingTest("A", "p.I", true);
  }

  public void test35() {
    validatePassingTest("A", "p.I", false);
  }

  public void test36() {
    validatePassingTest("A", "p.I", false);
  }

  public void test37() {
    // needs type changes dependencies resolving
    validatePassingTest("A", "p.I", true);
  }

  public void test38() {
    validatePassingTest("A", "p.I", true);
  }

  public void test39() {
    validatePassingTest("A", "p.I", false);
  }

  public void test40() {
    validatePassingTest("A", "p.I", false);
  }

  public void test41() {
    validatePassingTest("A", "p.I", false);
  }

  public void test42() {
    validatePassingTest("A", "p.I", false);
  }

  public void test43() {
    validatePassingTest("A", "p.I", true);
  }

  public void test44() {
    validatePassingTest("A", "p.I", true);
  }

  public void test45() {
    validatePassingTest("A", "p.I", false);
  }

  public void test46() {
    validatePassingTest("A", "p.I", true);
  }

  public void test47() {
    validatePassingTest("A", "p.I", false);
  }

  public void test48() {
    validatePassingTest("A", "p.I", false);
  }

  public void test49() {
    validatePassingTest("A", "p.I", false);
  }

  public void test50() {
    validatePassingTest("A", "p.I", false);
  }

  public void test51() {
    validatePassingTest("A", "p.I", true);
  }

  public void test52() {
    validatePassingTest("A", "p.I", false);
  }

  public void test53()  {
    // needs type changes dependencies resolving
    validatePassingTest("A", "p.I", true);
  }

  public void test54() {
    validatePassingTest("A", "p.I", true);
  }

  public void test55() {
    validatePassingTest("A", "p.I", false);
  }

  public void test56() {
    validatePassingTest("A", "p.I", false);
  }

  public void test57() {
    validatePassingTest("A", "p.I", true);
  }

  public void test58() {
    validatePassingTest("A", "p.I", false);
  }

  public void test59() {
    validatePassingTest("A", "p.I", false);
  }

  public void test60() {
    validatePassingTest("A", "p.I", false);
  }

  public void test61() {
    validatePassingTest("A", "p.I", false);
  }

  public void test62() {
    validatePassingTest("A", "p.I", false);
  }

  public void test63() {
    validatePassingTest("A", "p.I", false);
  }

  public void test64() {
    // needs type changes dependencies resolving
    validatePassingTest("A", "p.I", true);
  }

  public void test65() {
    validatePassingTest("A", "p.I", false);
  }

  public void test66() {
    validatePassingTest("A", "p.I", true);
  }

  public void test67() {
//    if (RefactorItConstants.runNotImplementedTests) {

    // should pass!?
    validatePassingTest("A", "p.I", true);
//    } else {
//      org.apache.log4j.Logger.getLogger(UseSuperTypeTest.class).debug("test67 disabled");
//    }
  }

  public void test68() {
    validatePassingTest("A", "p.I", false);
  }

  public void test69() {
    // needs type changes dependencies resolving
    validatePassingTest("A", "p.I", true);
  }

  public void test70() {
    validatePassingTest("A", "p.I", false);
  }

  public void test71() {
    validatePassingTest("A", "p.I", true);
  }

  public void test72() {
    validatePassingTest("A", "p.I", true);
  }

  public void test73() {
    validatePassingTest("A", "p.I", false);
  }

  public void test74() {
    validatePassingTest("B", "p.I", true);
  }

  public void test75() {
    validatePassingTest("B", "p.I1", true);
  }

  public void test76() {
    validatePassingTest(new String[] {"C","B"}, "p.I", true);
  }

  public void test77()  {
  	validatePassingTest(new String[] {"C","B"}, "p.I", true);
  }

  public void test78()  {
    validatePassingTest("B", "p.I", false);
  }

  public void test79()  {
    validatePassingTest("B", "p.I", true);
  }

  public void test80()  {
    validatePassingTest("B", "p.I", true);
  }

  public void test81()  {
    validatePassingTest("B", "p.I", false);
  }

  public void test82() {
    validatePassingTest("B", "p.I", true);
  }

  public void test82java1_4() {
    setJavaMode14();
    validatePassingTest("B", "p.I", false);
    setDefaultMode();
  }

  public void test83() {
    validatePassingTest("I2", "p.I1", true);
  }

  public void test84() {
    validatePassingTest("I2", "p.I1", false);
  }

  // Not implemented yet, fails
  public void test85() {
    validatePassingTest("A", "I", false);
  }

  public void testIssue243()  {
  	validatePassingTest("p1.A", "p1.List", true);
  }

  public void testGenerics1() throws Exception {
    validatePassingTest("A", "list", "java.util.List", false, true);
  }

  public void testGenerics2() throws Exception {
    validatePassingTest("A", "foo", "A$G1", false, true);
  }

  /**
   *
   */
  public static void assertCannotApplyRefactoring(Refactoring
      refactoring, Project before, Project after) {
    RefactoringStatus status = refactoring.checkPreconditions();

    Assert.assertTrue(status.getAllMessages(), status.isOk());

    status.merge(refactoring.checkUserInput());

    if (!status.isErrorOrFatal()) {
      //status.merge(TransformationManager.performTransformationFor(refactoring));
      status.merge(refactoring.apply());
    }

    boolean canceled = status.isCancel();

    if (!canceled && UseSuperTypeRefactoring.debug
        && (before != null || after != null)) {
      // hack to see quickly what refactoring did wrong

      RwRefactoringTestUtils.assertSameSources("Assert cannot apply",
          before, after);
    }

    Assert.assertTrue(status.getAllMessages(), canceled);
  }
}
